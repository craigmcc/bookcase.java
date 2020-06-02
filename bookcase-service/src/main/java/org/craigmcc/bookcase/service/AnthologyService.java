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
import org.craigmcc.bookcase.event.ForAnthology;
import org.craigmcc.bookcase.event.InsertedModelEvent;
import org.craigmcc.bookcase.event.UpdatedModelEvent;
import org.craigmcc.bookcase.exception.BadRequest;
import org.craigmcc.bookcase.exception.InternalServerError;
import org.craigmcc.bookcase.exception.NotFound;
import org.craigmcc.bookcase.exception.NotUnique;
import org.craigmcc.bookcase.model.Anthology;
import org.craigmcc.bookcase.model.Author;

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

import static org.craigmcc.bookcase.model.Constants.ANTHOLOGY_NAME;
import static org.craigmcc.bookcase.model.Constants.AUTHOR_NAME;
import static org.craigmcc.bookcase.model.Constants.TITLE_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@LocalBean
@Stateless
public class AnthologyService implements Service<Anthology> {

    // Instance Variables ----------------------------------------------------

    @Inject
    @ForAnthology
    private Event<DeletedModelEvent> deletedAnthologyEvent;

    @PersistenceContext
    protected EntityManager entityManager;

    @Inject
    @ForAnthology
    private Event<InsertedModelEvent> insertedAnthologyEvent;

    @Inject
    @ForAnthology
    private Event<UpdatedModelEvent> updatedAnthologyEvent;

    @Inject
    private Validator validator;

    // Public Methods --------------------------------------------------------

    @Override
    public Anthology delete(@NotNull Anthology anthology) throws InternalServerError, NotFound {

        try {

            Anthology deleted = entityManager.find(Anthology.class, anthology.getId());
            if (deleted != null) {
                entityManager.remove(deleted);
                deleted.setUpdated(LocalDateTime.now());
                deletedAnthologyEvent.fire(new DeletedModelEvent(deleted));
                return deleted;
            }

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        throw new NotFound(String.format("id: Missing anthology %d", anthology.getId()));

    }

    @Override
    public @NotNull Anthology find(@NotNull Long id) throws InternalServerError, NotFound {

        try {

            TypedQuery<Anthology> query = entityManager.createNamedQuery
                    (ANTHOLOGY_NAME + ".findById", Anthology.class);
            query.setParameter(ID_COLUMN, id);
            Anthology result = query.getSingleResult();
            return result;

        } catch (NoResultException e) {
            throw new NotFound("id: Missing anthology " + id);
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull List<Anthology> findAll() throws InternalServerError {

        try {

            TypedQuery<Anthology> query = entityManager.createNamedQuery
                    (ANTHOLOGY_NAME + ".findAll", Anthology.class);
            List<Anthology> results = query.getResultList();
            return results;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    public @NotNull List<Anthology> findByTitle(@NotNull String title)
            throws InternalServerError {

        try {

            TypedQuery<Anthology> query = entityManager.createNamedQuery
                    (ANTHOLOGY_NAME + ".findByTitle", Anthology.class);
            query.setParameter(TITLE_COLUMN, title);
            List<Anthology> result = query.getResultList();
            return result;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public Anthology insert(@NotNull Anthology anthology) throws BadRequest, InternalServerError, NotUnique {

        try {

            // Precheck validation constraints
            Set<ConstraintViolation<Anthology>> violations = validator.validate(anthology);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            // Precheck authorId foreign key constraint
            TypedQuery<Author> authorQuery = entityManager.createNamedQuery
                    (AUTHOR_NAME + ".findById", Author.class);
            authorQuery.setParameter(ID_COLUMN, anthology.getAuthorId());
            try {
                authorQuery.getSingleResult();
            } catch (NoResultException e) {
                throw new BadRequest(String.format("authorId: Missing author %d", anthology.getAuthorId()));
            } catch (Exception e) {
                throw new InternalServerError
                        (String.format("authorId: Error checking for author %d", anthology.getAuthorId()), e);
            }

            // Perform the requested insert
            anthology.setId(null); // Ignore any existing primary key
            anthology.setPublished(LocalDateTime.now());
            anthology.setUpdated(anthology.getPublished());
            entityManager.persist(anthology);
            insertedAnthologyEvent.fire(new InsertedModelEvent(anthology));
            return anthology;

        } catch (BadRequest e) {
            throw e;
        } catch (ConstraintViolationException e) {
            throw new BadRequest(e.getMessage()); // TODO - format message?
        } catch (EntityExistsException e) {
            throw new NotUnique(String.format("Non-unique insert attempted for %s", anthology.toString()));
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
    public Anthology update(@NotNull Anthology anthology) throws BadRequest, InternalServerError, NotFound, NotUnique {

        try {

            // Precheck validation constraints
            Set<ConstraintViolation<Anthology>> violations = validator.validate(anthology);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            // Precheck authorId foreign key constraint
            TypedQuery<Author> authorQuery = entityManager.createNamedQuery
                    (AUTHOR_NAME + ".findById", Author.class);
            authorQuery.setParameter(ID_COLUMN, anthology.getAuthorId());
            try {
                authorQuery.getSingleResult();
            } catch (NoResultException e) {
                throw new BadRequest(String.format("authorId: Missing author %d", anthology.getAuthorId()));
            } catch (Exception e) {
                throw new InternalServerError
                        (String.format("authorId: Error checking for author %d", anthology.getAuthorId()), e);
            }

            // Perform the requested update
            Anthology original = find(anthology.getId());
            original.copy(anthology);
            original.setUpdated(LocalDateTime.now());
            entityManager.merge(original);
            updatedAnthologyEvent.fire(new UpdatedModelEvent(original));
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
