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
import org.craigmcc.bookcase.model.Member;
import org.craigmcc.library.model.ModelService;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.InternalServerError;
import org.craigmcc.library.shared.exception.NotFound;
import org.craigmcc.library.shared.exception.NotUnique;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static org.craigmcc.bookcase.model.Constants.MEMBER_NAME;
import static org.craigmcc.bookcase.model.Constants.SERIES_ID_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@LocalBean
@Stateless
public class MemberService extends ModelService<Member> {

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
    public @NotNull Member delete(@NotNull Long id) throws InternalServerError, NotFound {

        try {

            Member deleted = entityManager.find(Member.class, id);
            if (deleted != null) {
                entityManager.remove(deleted);
                deleted.setUpdated(LocalDateTime.now());
                deletedMemberEvent.fire(new DeletedModelEvent(deleted));
                return deleted;
            }

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        throw new NotFound(String.format("id: Missing member %d", id));

    }

    @Override
    public @NotNull Member find(@NotNull Long id) throws InternalServerError, NotFound {

        try {

            TypedQuery<Member> query = entityManager.createNamedQuery
                    (MEMBER_NAME + ".findById", Member.class)
                    .setParameter(ID_COLUMN, id);
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
                    (MEMBER_NAME + ".findBySeriesId", Member.class)
                    .setParameter(SERIES_ID_COLUMN, seriesId);
            List<Member> result = query.getResultList();
            return result;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull Member insert(@NotNull Member member) throws BadRequest, InternalServerError, NotUnique {

        try {

            member.setId(null); // Ignore any existing primary key
            member.setPublished(LocalDateTime.now());
            member.setUpdated(member.getPublished());
            entityManager.persist(member);
            entityManager.flush();
            insertedMemberEvent.fire(new InsertedModelEvent(member));

        } catch (ConstraintViolationException e) {
            throw new BadRequest(formatMessage(e));
        } catch (PersistenceException e) {
            handlePersistenceException(e);
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        return member;

    }

    @Override
    public @NotNull Member update(@NotNull Member member) throws BadRequest, InternalServerError, NotFound, NotUnique {

        Member original = null;

        try {

            original = find(member.getId());
            original.copy(member);
            original.setUpdated(LocalDateTime.now());
            entityManager.merge(original);
            entityManager.flush();
            updatedMemberEvent.fire(new UpdatedModelEvent(original));

        } catch (ConstraintViolationException e) {
            throw new BadRequest(formatMessage(e));
        } catch (InternalServerError e) {
            throw e;
        } catch (NotFound e) {
            throw e;
        } catch (PersistenceException e) {
            handlePersistenceException(e);
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        return original;

    }

}
