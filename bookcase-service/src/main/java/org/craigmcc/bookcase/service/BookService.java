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
import org.craigmcc.bookcase.event.ForBook;
import org.craigmcc.bookcase.event.InsertedModelEvent;
import org.craigmcc.bookcase.event.UpdatedModelEvent;
import org.craigmcc.bookcase.exception.BadRequest;
import org.craigmcc.bookcase.exception.InternalServerError;
import org.craigmcc.bookcase.exception.NotFound;
import org.craigmcc.bookcase.exception.NotUnique;
import org.craigmcc.bookcase.model.Author;
import org.craigmcc.bookcase.model.Book;

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
import static org.craigmcc.bookcase.model.Constants.BOOK_NAME;
import static org.craigmcc.bookcase.model.Constants.TITLE_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@LocalBean
@Stateless
public class BookService implements Service<Book> {

    // Instance Variables ----------------------------------------------------

    @Inject
    @ForBook
    private Event<DeletedModelEvent> deletedBookEvent;

    @PersistenceContext
    protected EntityManager entityManager;

    @Inject
    @ForBook
    private Event<InsertedModelEvent> insertedBookEvent;

    @Inject
    @ForBook
    private Event<UpdatedModelEvent> updatedBookEvent;

    @Inject
    private Validator validator;

    // Public Methods --------------------------------------------------------

    @Override
    public Book delete(@NotNull Book book) throws InternalServerError, NotFound {

        try {

            Book deleted = entityManager.find(Book.class, book.getId());
            if (deleted != null) {
                entityManager.remove(deleted);
                deleted.setUpdated(LocalDateTime.now());
                deletedBookEvent.fire(new DeletedModelEvent(deleted));
                return deleted;
            }

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        throw new NotFound(String.format("id: Missing book %d", book.getId()));

    }

    @Override
    public @NotNull Book find(@NotNull Long id) throws InternalServerError, NotFound {

        try {

            TypedQuery<Book> query = entityManager.createNamedQuery
                    (BOOK_NAME + ".findById", Book.class);
            query.setParameter(ID_COLUMN, id);
            Book result = query.getSingleResult();
            return result;

        } catch (NoResultException e) {
            throw new NotFound("id: Missing book " + id);
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull List<Book> findAll() throws InternalServerError {

        try {

            TypedQuery<Book> query = entityManager.createNamedQuery
                    (BOOK_NAME + ".findAll", Book.class);
            List<Book> results = query.getResultList();
            return results;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    public @NotNull List<Book> findByTitle(@NotNull String title)
            throws InternalServerError {

        try {

            TypedQuery<Book> query = entityManager.createNamedQuery
                    (BOOK_NAME + ".findByTitle", Book.class);
            query.setParameter(TITLE_COLUMN, title);
            List<Book> result = query.getResultList();
            return result;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public Book insert(@NotNull Book book) throws BadRequest, InternalServerError, NotUnique {

        try {

            // Precheck validation constraints
            Set<ConstraintViolation<Book>> violations = validator.validate(book);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            // Precheck authorId foreign key constraint
            TypedQuery<Author> authorQuery = entityManager.createNamedQuery
                    (AUTHOR_NAME + ".findById", Author.class);
            authorQuery.setParameter(ID_COLUMN, book.getAuthorId());
            try {
                authorQuery.getSingleResult();
            } catch (NoResultException e) {
                throw new BadRequest(String.format("authorId: Missing author %d", book.getAuthorId()));
            } catch (Exception e) {
                throw new InternalServerError
                        (String.format("authorId: Error checking for author %d", book.getAuthorId()), e);
            }

            // Perform the requested insert
            book.setId(null); // Ignore any existing primary key
            book.setPublished(LocalDateTime.now());
            book.setUpdated(book.getPublished());
            entityManager.persist(book);
            insertedBookEvent.fire(new InsertedModelEvent(book));
            return book;

        } catch (BadRequest e) {
            throw e;
        } catch (ConstraintViolationException e) {
            throw new BadRequest(e.getMessage()); // TODO - format message?
        } catch (EntityExistsException e) {
            throw new NotUnique(String.format("Non-unique insert attempted for %s", book.toString()));
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
    public Book update(@NotNull Book book) throws BadRequest, InternalServerError, NotFound, NotUnique {

        try {

            // Precheck validation constraints
            Set<ConstraintViolation<Book>> violations = validator.validate(book);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            // Precheck authorId foreign key constraint
            TypedQuery<Author> authorQuery = entityManager.createNamedQuery
                    (AUTHOR_NAME + ".findById", Author.class);
            authorQuery.setParameter(ID_COLUMN, book.getAuthorId());
            try {
                authorQuery.getSingleResult();
            } catch (NoResultException e) {
                throw new BadRequest(String.format("authorId: Missing author %d", book.getAuthorId()));
            } catch (Exception e) {
                throw new InternalServerError
                        (String.format("authorId: Error checking for author %d", book.getAuthorId()), e);
            }

            // Perform the requested update
            Book original = find(book.getId());
            original.copy(book);
            original.setUpdated(LocalDateTime.now());
            entityManager.merge(original);
            updatedBookEvent.fire(new UpdatedModelEvent(original));
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
