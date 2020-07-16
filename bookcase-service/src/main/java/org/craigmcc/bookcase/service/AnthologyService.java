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
import org.craigmcc.bookcase.model.Anthology;
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

import static org.craigmcc.bookcase.model.Constants.ANTHOLOGY_NAME;
import static org.craigmcc.bookcase.model.Constants.TITLE_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@LocalBean
@Stateless
public class AnthologyService extends ModelService<Anthology> {

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
    public @NotNull Anthology delete(@NotNull Long id) throws InternalServerError, NotFound {

        try {

            Anthology deleted = entityManager.find(Anthology.class, id);
            if (deleted != null) {
                entityManager.remove(deleted);
                deleted.setUpdated(LocalDateTime.now());
                deletedAnthologyEvent.fire(new DeletedModelEvent(deleted));
                return deleted;
            }

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        throw new NotFound(String.format("id: Missing anthology %d", id));

    }

    @Override
    public @NotNull Anthology find(@NotNull Long id)
            throws InternalServerError, NotFound {

        if (id == null) {
            throw new NotFound("id: Cannot be null");
        }

        try {

            TypedQuery<Anthology> query = entityManager.createNamedQuery
                    (ANTHOLOGY_NAME + ".findById", Anthology.class)
                    .setParameter(ID_COLUMN, id);
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
                    (ANTHOLOGY_NAME + ".findByTitle", Anthology.class)
                    .setParameter(TITLE_COLUMN, title);
            List<Anthology> result = query.getResultList();
            return result;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull Anthology insert(@NotNull Anthology anthology)
            throws BadRequest, InternalServerError, NotUnique {

        try {

            anthology.setId(null); // Ignore any existing primary key
            anthology.setPublished(LocalDateTime.now());
            anthology.setUpdated(anthology.getPublished());
            entityManager.persist(anthology);
            entityManager.flush();
            insertedAnthologyEvent.fire(new InsertedModelEvent(anthology));

        } catch (ConstraintViolationException e) {
            throw new BadRequest(formatMessage(e));
        } catch (PersistenceException e) {
            handlePersistenceException(e);
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        return anthology;

    }

    @Override
    public @NotNull Anthology update(@NotNull Long anthologyId, @NotNull Anthology anthology)
            throws BadRequest, InternalServerError, NotFound, NotUnique {

        Anthology original = null;

        try {

            original = find(anthologyId);
            original.copy(anthology);
            original.setUpdated(LocalDateTime.now());
            entityManager.merge(original);
            entityManager.flush();
            updatedAnthologyEvent.fire(new UpdatedModelEvent(original));

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
