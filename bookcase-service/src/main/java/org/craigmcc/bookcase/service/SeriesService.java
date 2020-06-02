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
import org.craigmcc.bookcase.event.ForSeries;
import org.craigmcc.bookcase.event.InsertedModelEvent;
import org.craigmcc.bookcase.event.UpdatedModelEvent;
import org.craigmcc.bookcase.exception.BadRequest;
import org.craigmcc.bookcase.exception.InternalServerError;
import org.craigmcc.bookcase.exception.NotFound;
import org.craigmcc.bookcase.exception.NotUnique;
import org.craigmcc.bookcase.model.Author;
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

import static org.craigmcc.bookcase.model.Constants.AUTHOR_NAME;
import static org.craigmcc.bookcase.model.Constants.SERIES_NAME;
import static org.craigmcc.bookcase.model.Constants.TITLE_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@LocalBean
@Stateless
public class SeriesService implements Service<Series> {

    // Instance Variables ----------------------------------------------------

    @Inject
    @ForSeries
    private Event<DeletedModelEvent> deletedSeriesEvent;

    @PersistenceContext
    protected EntityManager entityManager;

    @Inject
    @ForSeries
    private Event<InsertedModelEvent> insertedSeriesEvent;

    @Inject
    @ForSeries
    private Event<UpdatedModelEvent> updatedSeriesEvent;

    @Inject
    private Validator validator;

    // Public Methods --------------------------------------------------------

    @Override
    public Series delete(@NotNull Series series) throws InternalServerError, NotFound {

        try {

            Series deleted = entityManager.find(Series.class, series.getId());
            if (deleted != null) {
                entityManager.remove(deleted);
                deleted.setUpdated(LocalDateTime.now());
                deletedSeriesEvent.fire(new DeletedModelEvent(deleted));
                return deleted;
            }

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        throw new NotFound(String.format("id: Missing series %d", series.getId()));

    }

    @Override
    public @NotNull Series find(@NotNull Long id) throws InternalServerError, NotFound {

        try {

            TypedQuery<Series> query = entityManager.createNamedQuery
                    (SERIES_NAME + ".findById", Series.class);
            query.setParameter(ID_COLUMN, id);
            Series result = query.getSingleResult();
            return result;

        } catch (NoResultException e) {
            throw new NotFound("id: Missing Series " + id);
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull List<Series> findAll() throws InternalServerError {

        try {

            TypedQuery<Series> query = entityManager.createNamedQuery
                    (SERIES_NAME + ".findAll", Series.class);
            List<Series> results = query.getResultList();
            return results;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    public @NotNull List<Series> findByTitle(@NotNull String title)
            throws InternalServerError {

        try {

            TypedQuery<Series> query = entityManager.createNamedQuery
                    (SERIES_NAME + ".findByTitle", Series.class);
            query.setParameter(TITLE_COLUMN, title);
            List<Series> result = query.getResultList();
            return result;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public Series insert(@NotNull Series series) throws BadRequest, InternalServerError, NotUnique {

        try {

            // Precheck validation constraints
            Set<ConstraintViolation<Series>> violations = validator.validate(series);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            // Precheck authorId foreign key constraint
            TypedQuery<Author> authorQuery = entityManager.createNamedQuery
                    (AUTHOR_NAME + ".findById", Author.class);
            authorQuery.setParameter(ID_COLUMN, series.getAuthorId());
            try {
                authorQuery.getSingleResult();
            } catch (NoResultException e) {
                throw new BadRequest(String.format("authorId: Missing author %d", series.getAuthorId()));
            } catch (Exception e) {
                throw new InternalServerError
                        (String.format("authorId: Error checking for author %d", series.getAuthorId()), e);
            }

            // Perform the requested insert
            series.setId(null); // Ignore any existing primary key
            series.setPublished(LocalDateTime.now());
            series.setUpdated(series.getPublished());
            entityManager.persist(series);
            insertedSeriesEvent.fire(new InsertedModelEvent(series));
            return series;

        } catch (BadRequest e) {
            throw e;
        } catch (ConstraintViolationException e) {
            throw new BadRequest(e.getMessage()); // TODO - format message?
        } catch (EntityExistsException e) {
            throw new NotUnique(String.format("Non-unique insert attempted for %s", series.toString()));
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
    public Series update(@NotNull Series series) throws BadRequest, InternalServerError, NotFound, NotUnique {

        try {

            // Precheck validation constraints
            Set<ConstraintViolation<Series>> violations = validator.validate(series);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            // Precheck authorId foreign key constraint
            TypedQuery<Author> authorQuery = entityManager.createNamedQuery
                    (AUTHOR_NAME + ".findById", Author.class);
            authorQuery.setParameter(ID_COLUMN, series.getAuthorId());
            try {
                authorQuery.getSingleResult();
            } catch (NoResultException e) {
                throw new BadRequest(String.format("authorId: Missing author %d", series.getAuthorId()));
            } catch (Exception e) {
                throw new InternalServerError
                        (String.format("authorId: Error checking for author %d", series.getAuthorId()), e);
            }

            // Perform the requested update
            Series original = find(series.getId());
            original.copy(series);
            original.setUpdated(LocalDateTime.now());
            entityManager.merge(original);
            updatedSeriesEvent.fire(new UpdatedModelEvent(original));
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
