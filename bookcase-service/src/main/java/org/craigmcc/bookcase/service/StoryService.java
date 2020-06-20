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
import org.craigmcc.bookcase.event.ForStory;
import org.craigmcc.bookcase.event.InsertedModelEvent;
import org.craigmcc.bookcase.event.UpdatedModelEvent;
import org.craigmcc.bookcase.model.Story;
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
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static org.craigmcc.bookcase.model.Constants.ANTHOLOGY_ID_COLUMN;
import static org.craigmcc.bookcase.model.Constants.STORY_NAME;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@LocalBean
@Stateless
public class StoryService extends ModelService<Story> {

    // Instance Variables ----------------------------------------------------

    @Inject
    @ForStory
    private Event<DeletedModelEvent> deletedStoryEvent;

    @PersistenceContext
    protected EntityManager entityManager;

    @Inject
    @ForStory
    private Event<InsertedModelEvent> insertedStoryEvent;

    @Inject
    @ForStory
    private Event<UpdatedModelEvent> updatedStoryEvent;

    // Public Methods --------------------------------------------------------

    @Override
    public @NotNull Story delete(@NotNull Long id) throws InternalServerError, NotFound {

        try {

            Story deleted = entityManager.find(Story.class, id);
            if (deleted != null) {
                entityManager.remove(deleted);
                deleted.setUpdated(LocalDateTime.now());
                deletedStoryEvent.fire(new DeletedModelEvent(deleted));
                return deleted;
            }

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        throw new NotFound(String.format("id: Missing story %d", id));

    }

    @Override
    public @NotNull Story find(@NotNull Long id) throws InternalServerError, NotFound {

        try {

            TypedQuery<Story> query = entityManager.createNamedQuery
                    (STORY_NAME + ".findById", Story.class)
                    .setParameter(ID_COLUMN, id);
            Story result = query.getSingleResult();
            return result;

        } catch (NoResultException e) {
            throw new NotFound("id: Missing story " + id);
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull List<Story> findAll() throws InternalServerError {

        try {

            TypedQuery<Story> query = entityManager.createNamedQuery
                    (STORY_NAME + ".findAll", Story.class);
            List<Story> results = query.getResultList();
            return results;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    public @NotNull List<Story> findByAnthologyId(@NotNull Long anthologyId)
            throws InternalServerError {

        try {

            TypedQuery<Story> query = entityManager.createNamedQuery
                    (STORY_NAME + ".findByAnthologyId", Story.class)
                    .setParameter(ANTHOLOGY_ID_COLUMN, anthologyId);
            List<Story> result = query.getResultList();
            return result;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull Story insert(@NotNull Story story) throws BadRequest, InternalServerError, NotUnique {

        try {

            story.setId(null); // Ignore any existing primary key
            story.setPublished(LocalDateTime.now());
            story.setUpdated(story.getPublished());
            entityManager.persist(story);
            entityManager.flush();
            insertedStoryEvent.fire(new InsertedModelEvent(story));

        } catch (ConstraintViolationException e) {
            throw new BadRequest(formatMessage(e));
        } catch (PersistenceException e) {
            handlePersistenceException(e);
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        return story;

    }

    @Override
    public @NotNull Story update(@NotNull Story story) throws BadRequest, InternalServerError, NotFound, NotUnique {

        Story original = null;

        try {

            original = find(story.getId());
            original.copy(story);
            original.setUpdated(LocalDateTime.now());
            entityManager.merge(original);
            entityManager.flush();
            updatedStoryEvent.fire(new UpdatedModelEvent(original));

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
