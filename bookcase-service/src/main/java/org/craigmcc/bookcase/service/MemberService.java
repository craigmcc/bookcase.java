/*
 * Copyright 2020 craigmcc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.craigmcc.bookcase.service;

import org.craigmcc.bookcase.event.DeletedModelEvent;
import org.craigmcc.bookcase.event.ForMember;
import org.craigmcc.bookcase.event.InsertedModelEvent;
import org.craigmcc.bookcase.event.UpdatedModelEvent;
import org.craigmcc.bookcase.exception.BadRequest;
import org.craigmcc.bookcase.exception.InternalServerError;
import org.craigmcc.bookcase.exception.NotFound;
import org.craigmcc.bookcase.exception.NotUnique;
import org.craigmcc.bookcase.model.Book;
import org.craigmcc.bookcase.model.Member;
import org.craigmcc.bookcase.model.Series;

import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.craigmcc.bookcase.model.Constants.BOOK_NAME;
import static org.craigmcc.bookcase.model.Constants.MEMBER_NAME;
import static org.craigmcc.bookcase.model.Constants.SERIES_ID_COLUMN;
import static org.craigmcc.bookcase.model.Constants.SERIES_NAME;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@LocalBean
@Stateless
public class MemberService extends Service<Member> {

    // Instance Variables ----------------------------------------------------

    @Inject
    @ForMember
    private Event<DeletedModelEvent> deletedMemberEvent;

    @PersistenceContext
    protected EntityManager entityManager;

    @Inject
    @ForMember
    private Event<InsertedModelEvent> insertedMemberEvent;

    @Inject
    @ForMember
    private Event<UpdatedModelEvent> updatedMemberEvent;

    @Inject
    private Validator validator;

    // Public Methods --------------------------------------------------------

    @Override
    public Member delete(@NotNull Member member) throws InternalServerError, NotFound {

        try {

            Member deleted = entityManager.find(Member.class, member.getId());
            if (deleted != null) {
                entityManager.remove(deleted);
                deleted.setUpdated(LocalDateTime.now());
                deletedMemberEvent.fire(new DeletedModelEvent(deleted));
                return deleted;
            }

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        throw new NotFound(String.format("id: Missing member %d", member.getId()));

    }

    @Override
    public @NotNull Member find(@NotNull Long id) throws InternalServerError, NotFound {

        try {

            TypedQuery<Member> query = entityManager.createNamedQuery
                    (MEMBER_NAME + ".findById", Member.class);
            query.setParameter(ID_COLUMN, id);
            Member result = query.getSingleResult();
            return result;

        } catch (NoResultException e) {
            throw new NotFound("id: Missing member " + id);
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull List<Member> findAll() throws InternalServerError {

        try {

            TypedQuery<Member> query = entityManager.createNamedQuery
                    (MEMBER_NAME + ".findAll", Member.class);
            List<Member> results = query.getResultList();
            return results;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    public @NotNull List<Member> findBySeriesId(@NotNull Long seriesId)
            throws InternalServerError {

        try {

            TypedQuery<Member> query = entityManager.createNamedQuery
                    (MEMBER_NAME + ".findBySeriesId", Member.class);
            query.setParameter(SERIES_ID_COLUMN, seriesId);
            List<Member> result = query.getResultList();
            return result;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public Member insert(@NotNull Member member) throws BadRequest, InternalServerError, NotUnique {

        try {

            // Precheck validation constraints
            Set<ConstraintViolation<Member>> violations = validator.validate(member);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            // Precheck bookId foreign key constraint
            TypedQuery<Book> bookQuery = entityManager.createNamedQuery
                    (BOOK_NAME + ".findById", Book.class);
            bookQuery.setParameter(ID_COLUMN, member.getBookId());
            try {
                bookQuery.getSingleResult();
            } catch (NoResultException e) {
                throw new BadRequest(String.format("bookId: Missing book %d", member.getBookId()));
            } catch (Exception e) {
                throw new InternalServerError
                        (String.format("bookId: Error checking for book %d", member.getBookId()), e);
            }

            // Precheck seriesId foreign key constraint
            TypedQuery<Series> seriesQuery = entityManager.createNamedQuery
                    (SERIES_NAME + ".findById", Series.class);
            seriesQuery.setParameter(ID_COLUMN, member.getSeriesId());
            try {
                seriesQuery.getSingleResult();
            } catch (NoResultException e) {
                throw new BadRequest(String.format("seriesId: Missing series %d", member.getSeriesId()));
            } catch (Exception e) {
                throw new InternalServerError
                        (String.format("seriesId: Error checking for series %d", member.getSeriesId()), e);
            }

            // Perform the requested insert
            member.setId(null); // Ignore any existing primary key
            member.setPublished(LocalDateTime.now());
            member.setUpdated(member.getPublished());
            entityManager.persist(member);
            insertedMemberEvent.fire(new InsertedModelEvent(member));
            return member;

        } catch (BadRequest e) {
            throw e;
        } catch (ConstraintViolationException e) {
            throw new BadRequest(e.getMessage()); // TODO - format message?
        } catch (EntityExistsException e) {
            throw new NotUnique(String.format("Non-unique insert attempted for %s", member.toString()));
        } catch (InternalServerError e) {
            throw e;
        } catch (RollbackException | EJBTransactionRolledbackException e) {
            if ((e != null) && (e.getCause() instanceof ConstraintViolationException)) {
                throw new BadRequest(e.getCause().getMessage()); // TODO - format message?
            } else {
                throw new BadRequest(e.getMessage());
            }
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public Member update(@NotNull Member member) throws BadRequest, InternalServerError, NotFound, NotUnique {

        try {

            // Precheck validation constraints
            Set<ConstraintViolation<Member>> violations = validator.validate(member);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            // Precheck bookId foreign key constraint
            TypedQuery<Book> bookQuery = entityManager.createNamedQuery
                    (BOOK_NAME + ".findById", Book.class);
            bookQuery.setParameter(ID_COLUMN, member.getBookId());
            try {
                bookQuery.getSingleResult();
            } catch (NoResultException e) {
                throw new BadRequest(String.format("bookId: Missing book %d", member.getBookId()));
            } catch (Exception e) {
                throw new InternalServerError
                        (String.format("bookId: Error checking for book %d", member.getBookId()), e);
            }

            // Precheck seriesId foreign key constraint
            TypedQuery<Series> seriesQuery = entityManager.createNamedQuery
                    (SERIES_NAME + ".findById", Series.class);
            seriesQuery.setParameter(ID_COLUMN, member.getSeriesId());
            try {
                seriesQuery.getSingleResult();
            } catch (NoResultException e) {
                throw new BadRequest(String.format("seriesId: Missing series %d", member.getSeriesId()));
            } catch (Exception e) {
                throw new InternalServerError
                        (String.format("seriesId: Error checking for series %d", member.getSeriesId()), e);
            }

            // Perform the requested update
            Member original = find(member.getId());
            original.copy(member);
            original.setUpdated(LocalDateTime.now());
            entityManager.merge(original);
            updatedMemberEvent.fire(new UpdatedModelEvent(original));
            return original;

        } catch (BadRequest e) {
            throw e;
        } catch (ConstraintViolationException e) {
            throw new BadRequest(e.getMessage()); // TODO - format message?
        } catch (InternalServerError e) {
            throw e;
        } catch (NotFound e) {
            throw e;
        } catch (RollbackException | EJBTransactionRolledbackException e) {
            if ((e != null) && (e.getCause() instanceof ConstraintViolationException)) {
                throw new BadRequest(e.getCause().getMessage()); // TODO - format message?
            } else {
                throw new BadRequest(e.getMessage());
            }
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

}
