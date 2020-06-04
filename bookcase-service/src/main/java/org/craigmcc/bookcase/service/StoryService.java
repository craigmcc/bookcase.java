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
import org.craigmcc.bookcase.exception.BadRequest;
import org.craigmcc.bookcase.exception.InternalServerError;
import org.craigmcc.bookcase.exception.NotFound;
import org.craigmcc.bookcase.exception.NotUnique;
import org.craigmcc.bookcase.model.Anthology;
import org.craigmcc.bookcase.model.Book;
import org.craigmcc.bookcase.model.Story;

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

import static org.craigmcc.bookcase.model.Constants.ANTHOLOGY_ID_COLUMN;
import static org.craigmcc.bookcase.model.Constants.ANTHOLOGY_NAME;
import static org.craigmcc.bookcase.model.Constants.BOOK_NAME;
import static org.craigmcc.bookcase.model.Constants.STORY_NAME;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@LocalBean
@Stateless
public class StoryService extends Service<Story> {

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

    @Inject
    private Validator validator;

    // Public Methods --------------------------------------------------------

    @Override
    public Story delete(@NotNull Story story) throws InternalServerError, NotFound {

        try {

            Story deleted = entityManager.find(Story.class, story.getId());
            if (deleted != null) {
                entityManager.remove(deleted);
                deleted.setUpdated(LocalDateTime.now());
                deletedStoryEvent.fire(new DeletedModelEvent(deleted));
                return deleted;
            }

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        throw new NotFound(String.format("id: Missing story %d", story.getId()));

    }

    @Override
    public @NotNull Story find(@NotNull Long id) throws InternalServerError, NotFound {

        try {

            TypedQuery<Story> query = entityManager.createNamedQuery
                    (STORY_NAME + ".findById", Story.class);
            query.setParameter(ID_COLUMN, id);
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
                    (STORY_NAME + ".findByAnthologyId", Story.class);
            query.setParameter(ANTHOLOGY_ID_COLUMN, anthologyId);
            List<Story> result = query.getResultList();
            return result;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public Story insert(@NotNull Story story) throws BadRequest, InternalServerError, NotUnique {

        try {

            // Precheck validation constraints
            Set<ConstraintViolation<Story>> violations = validator.validate(story);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            // Precheck anthologyId foreign key constraint
            TypedQuery<Anthology> anthologyQuery = entityManager.createNamedQuery
                    (ANTHOLOGY_NAME + ".findById", Anthology.class);
            anthologyQuery.setParameter(ID_COLUMN, story.getAnthologyId());
            try {
                anthologyQuery.getSingleResult();
            } catch (NoResultException e) {
                throw new BadRequest(String.format("anthologyId: Missing anthology %d", story.getAnthologyId()));
            } catch (Exception e) {
                throw new InternalServerError
                        (String.format("seriesId: Error checking for anthology %d", story.getAnthologyId()), e);
            }

            // Precheck bookId foreign key constraint
            TypedQuery<Book> bookQuery = entityManager.createNamedQuery
                    (BOOK_NAME + ".findById", Book.class);
            bookQuery.setParameter(ID_COLUMN, story.getBookId());
            try {
                bookQuery.getSingleResult();
            } catch (NoResultException e) {
                throw new BadRequest(String.format("bookId: Missing book %d", story.getBookId()));
            } catch (Exception e) {
                throw new InternalServerError
                        (String.format("bookId: Error checking for book %d", story.getBookId()), e);
            }

            // Perform the requested insert
            story.setId(null); // Ignore any existing primary key
            story.setPublished(LocalDateTime.now());
            story.setUpdated(story.getPublished());
            entityManager.persist(story);
            insertedStoryEvent.fire(new InsertedModelEvent(story));
            return story;

        } catch (BadRequest e) {
            throw e;
        } catch (ConstraintViolationException e) {
            throw new BadRequest(e.getMessage()); // TODO - format message?
        } catch (EntityExistsException e) {
            throw new NotUnique(String.format("Non-unique insert attempted for %s", story.toString()));
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
    public Story update(@NotNull Story story) throws BadRequest, InternalServerError, NotFound, NotUnique {

        try {

            // Precheck validation constraints
            Set<ConstraintViolation<Story>> violations = validator.validate(story);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            // Precheck anthologyId foreign key constraint
            TypedQuery<Anthology> anthologyQuery = entityManager.createNamedQuery
                    (ANTHOLOGY_NAME + ".findById", Anthology.class);
            anthologyQuery.setParameter(ID_COLUMN, story.getAnthologyId());
            try {
                anthologyQuery.getSingleResult();
            } catch (NoResultException e) {
                throw new BadRequest(String.format("anthologyId: Missing anthology %d", story.getAnthologyId()));
            } catch (Exception e) {
                throw new InternalServerError
                        (String.format("seriesId: Error checking for anthology %d", story.getAnthologyId()), e);
            }

            // Precheck bookId foreign key constraint
            TypedQuery<Book> bookQuery = entityManager.createNamedQuery
                    (BOOK_NAME + ".findById", Book.class);
            bookQuery.setParameter(ID_COLUMN, story.getBookId());
            try {
                bookQuery.getSingleResult();
            } catch (NoResultException e) {
                throw new BadRequest(String.format("bookId: Missing book %d", story.getBookId()));
            } catch (Exception e) {
                throw new InternalServerError
                        (String.format("bookId: Error checking for book %d", story.getBookId()), e);
            }

            // Perform the requested update
            Story original = find(story.getId());
            original.copy(story);
            original.setUpdated(LocalDateTime.now());
            entityManager.merge(original);
            updatedStoryEvent.fire(new UpdatedModelEvent(original));
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
