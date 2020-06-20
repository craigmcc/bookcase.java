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
import org.craigmcc.bookcase.model.Series;
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

import static org.craigmcc.bookcase.model.Constants.SERIES_NAME;
import static org.craigmcc.bookcase.model.Constants.TITLE_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@LocalBean
@Stateless
public class SeriesService extends ModelService<Series> {

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
    public @NotNull Series delete(@NotNull Long id) throws InternalServerError, NotFound {

        try {

            Series deleted = entityManager.find(Series.class, id);
            if (deleted != null) {
                entityManager.remove(deleted);
                deleted.setUpdated(LocalDateTime.now());
                deletedSeriesEvent.fire(new DeletedModelEvent(deleted));
                return deleted;
            }

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        throw new NotFound(String.format("id: Missing series %d", id));

    }

    @Override
    public @NotNull Series find(@NotNull Long id) throws InternalServerError, NotFound {

        try {

            TypedQuery<Series> query = entityManager.createNamedQuery
                    (SERIES_NAME + ".findById", Series.class)
                    .setParameter(ID_COLUMN, id);
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
                    (SERIES_NAME + ".findByTitle", Series.class)
                    .setParameter(TITLE_COLUMN, title);
            List<Series> result = query.getResultList();
            return result;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull Series insert(@NotNull Series series) throws BadRequest, InternalServerError, NotUnique {

        try {

            series.setId(null); // Ignore any existing primary key
            series.setPublished(LocalDateTime.now());
            series.setUpdated(series.getPublished());
            entityManager.persist(series);
            entityManager.flush();
            insertedSeriesEvent.fire(new InsertedModelEvent(series));

        } catch (ConstraintViolationException e) {
            throw new BadRequest(formatMessage(e));
        } catch (PersistenceException e) {
            handlePersistenceException(e);
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        return series;
    }

    @Override
    public @NotNull Series update(@NotNull Series series) throws BadRequest, InternalServerError, NotFound, NotUnique {

        Series original = null;

        try {

            original = find(series.getId());
            original.copy(series);
            original.setUpdated(LocalDateTime.now());
            entityManager.merge(original);
            entityManager.flush();
            updatedSeriesEvent.fire(new UpdatedModelEvent(original));

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
