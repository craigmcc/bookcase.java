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
import org.craigmcc.bookcase.event.ForAuthor;
import org.craigmcc.bookcase.event.InsertedModelEvent;
import org.craigmcc.bookcase.event.UpdatedModelEvent;
import org.craigmcc.bookcase.exception.BadRequest;
import org.craigmcc.bookcase.exception.InternalServerError;
import org.craigmcc.bookcase.exception.NotFound;
import org.craigmcc.bookcase.exception.NotUnique;
import org.craigmcc.bookcase.model.Author;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static org.craigmcc.bookcase.model.Constants.AUTHOR_NAME;
import static org.craigmcc.bookcase.model.Constants.FIRST_NAME_COLUMN;
import static org.craigmcc.bookcase.model.Constants.LAST_NAME_COLUMN;
import static org.craigmcc.bookcase.model.Constants.NAME_UNIQUE_VALIDATION_MESSAGE;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@LocalBean
@Stateless
public class AuthorService extends Service<Author> {

    // Instance Variables ----------------------------------------------------

    @Inject
    @ForAuthor
    private Event<DeletedModelEvent> deletedAuthorEvent;

    @PersistenceContext
    protected EntityManager entityManager;

    @Inject
    @ForAuthor
    private Event<InsertedModelEvent> insertedAuthorEvent;

    @Inject
    @ForAuthor
    private Event<UpdatedModelEvent> updatedAuthorEvent;

    // Public Methods --------------------------------------------------------

    @Override
    public @NotNull Author delete(@NotNull Long id) throws InternalServerError, NotFound {

        try {

            Author deleted = entityManager.find(Author.class, id);
            if (deleted != null) {
                entityManager.remove(deleted);
                deleted.setUpdated(LocalDateTime.now());
                deletedAuthorEvent.fire(new DeletedModelEvent(deleted));
                return deleted;
            }

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        throw new NotFound(String.format("id: Missing author %d", id));

    }

    @Override
    public @NotNull Author find(@NotNull Long id) throws InternalServerError, NotFound {

        try {

            TypedQuery<Author> query = entityManager.createNamedQuery
                    (AUTHOR_NAME + ".findById", Author.class)
                    .setParameter(ID_COLUMN, id);
            Author result = query.getSingleResult();
            return result;

        } catch (NoResultException e) {
            throw new NotFound("id: Missing author " + id);
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull List<Author> findAll() throws InternalServerError {

        try {

            TypedQuery<Author> query = entityManager.createNamedQuery
                    (AUTHOR_NAME + ".findAll", Author.class);
            List<Author> results = query.getResultList();
            return results;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    public @NotNull List<Author> findByName(@NotBlank String name) throws InternalServerError {

        try {

            String firstName = name;
            String lastName = name;
            int index = name.indexOf(" ");
            if ((index > 0) && (index < name.length() - 1)) {
                firstName = name.substring(0, index).trim();
                lastName = name.substring(index + 1).trim();
            }

            TypedQuery<Author> query = entityManager.createNamedQuery
                    (AUTHOR_NAME + ".findByName", Author.class)
                    .setParameter(FIRST_NAME_COLUMN, firstName)
                    .setParameter(LAST_NAME_COLUMN, lastName);
            List<Author> result = query.getResultList();
            return result;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull Author insert(@NotNull Author author) throws BadRequest, InternalServerError, NotUnique {

        try {

            // Precheck uniqueness constraint
            TypedQuery<Author> authorQuery = entityManager.createNamedQuery
                    (AUTHOR_NAME + ".findByNameExact", Author.class);
            authorQuery.setParameter(FIRST_NAME_COLUMN, author.getFirstName());
            authorQuery.setParameter(LAST_NAME_COLUMN, author.getLastName());
            List<Author> matches = authorQuery.getResultList();
            if (matches.size() > 0) {
                throw new NotUnique(NAME_UNIQUE_VALIDATION_MESSAGE);
            }

            // Perform the requested insert
            author.setId(null); // Ignore any existing primary key
            author.setPublished(LocalDateTime.now());
            author.setUpdated(author.getPublished());
            entityManager.persist(author);
            entityManager.flush();
            entityManager.detach(author);
            insertedAuthorEvent.fire(new InsertedModelEvent(author));
            return author;

        } catch (ConstraintViolationException e) {
            throw new BadRequest(formatMessage(e));
        } catch (EntityExistsException e) {
            throw new NotUnique(NAME_UNIQUE_VALIDATION_MESSAGE);
        } catch (NotUnique e) {
            throw e;
        } catch (PersistenceException e) {
            handlePersistenceException(e);
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        return author;

    }

    @Override
    public @NotNull Author update(@NotNull Author author) throws BadRequest, InternalServerError, NotFound, NotUnique {

        Author original = null;

        try {

            // Precheck uniqueness constraint
            TypedQuery<Author> authorQuery = entityManager.createNamedQuery
                    (AUTHOR_NAME + ".findByNameExact", Author.class);
            authorQuery.setParameter(FIRST_NAME_COLUMN, author.getFirstName());
            authorQuery.setParameter(LAST_NAME_COLUMN, author.getLastName());
            List<Author> matches = authorQuery.getResultList();
            if (matches.size() > 0) {
                for (Author match : matches) {
                    if (match.getId() != author.getId()) {
                        throw new NotUnique(NAME_UNIQUE_VALIDATION_MESSAGE);
                    }
                }
            }

            // Perform the requested update
            original = find(author.getId());
            original.copy(author);
            original.setUpdated(LocalDateTime.now());
            entityManager.merge(original);
            entityManager.flush();
            entityManager.detach(original);
            updatedAuthorEvent.fire(new UpdatedModelEvent(original));

        } catch (ConstraintViolationException e) {
            throw new BadRequest(formatMessage(e));
        } catch (EntityExistsException e) {
            throw new NotUnique(NAME_UNIQUE_VALIDATION_MESSAGE);
        } catch (InternalServerError e) {
            throw e;
        } catch (NotFound e) {
            throw e;
        } catch (NotUnique e) {
            throw e;
        } catch (PersistenceException e) {
            e.printStackTrace();
            handlePersistenceException(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerError(e.getMessage(), e);
        }

        return original;

    }

}
