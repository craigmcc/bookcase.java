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

import org.craigmcc.bookcase.exception.BadRequest;
import org.craigmcc.bookcase.exception.NotFound;
import org.craigmcc.bookcase.model.Author;
import org.craigmcc.bookcase.model.Book;
import org.craigmcc.bookcase.model.Member;
import org.craigmcc.bookcase.model.Story;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

import static org.craigmcc.bookcase.model.Constants.AUTHOR_NAME;
import static org.craigmcc.bookcase.model.Constants.BOOK_ID_COLUMN;
import static org.craigmcc.bookcase.model.Constants.BOOK_NAME;
import static org.craigmcc.bookcase.model.Constants.MEMBER_NAME;
import static org.craigmcc.bookcase.model.Constants.STORY_NAME;
import static org.craigmcc.bookcase.model.Constants.TITLE_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

@Category(ServiceTests.class)
@RunWith(Arquillian.class)
public class BookServiceTest extends AbstractServiceTest {

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "testBook.jar")
                .addClass(BookService.class);
        addServiceFixtures(archive, false);
        System.out.println("BookServiceTest:  Assembled Archive:");
        System.out.println(archive.toString(true));
        return archive;
    }

    @Inject
    BookService bookService;

    @Inject
    DevModeDepopulateService devModeDepopulateService;

    @Inject
    DevModePopulateService devModePopulateService;

    @PersistenceContext
    EntityManager entityManager;

    // Lifecycle Methods -----------------------------------------------------

    @After
    public void after() {
        devModeDepopulateService.depopulate();
    }

    @Before
    public void before() {
        devModeDepopulateService.depopulate();
        devModePopulateService.populate();
    }

    // Test Methods ----------------------------------------------------------

    // delete() tests

    @Test
    public void deleteHappy() throws Exception {

        List<Book> books = findAllBooks();
        assertThat(books.size(), is(greaterThan(0)));

        for (Book book : books) {

/*          (Not true for actual test data)
            // Test data should not have any books with no members
            List<Member> members = findMembersByBookId(book.getId());
            assertThat(members.size(), greaterThan(0));
*/

/*          (Not true for actual test data)
            // Test data should not have any books with no stories
            List<Story> stories = findStoriesByBookId(book.getId());
            assertThat(stories.size(), greaterThan(0));
*/

            // Delete and verify we can no longer retrieve it
            bookService.delete(book);
            assertThat(findBookById(book.getId()).isPresent(), is(false));

            // Delete should have cascaded to members and stories
            assertThat(findMembersByBookId(book.getId()).size(), is(0));
            assertThat(findStoriesByBookId(book.getId()).size(), is(0));

        }

        assertThat(bookService.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {
        Book book = new Book();
        book.setId(Long.MAX_VALUE);
        assertThat(findBookById(book.getId()).isPresent(), is(false));
    }

    // find() tests

    @Test
    public void findHappy() throws Exception {

        List<Book> books = bookService.findAll();
        assertThat(books.size(), is(greaterThan(0)));

        for (Book book : books) {
            Book found = bookService.find(book.getId());
            assertThat(found.equals(book), is(true));
        }

    }

    @Test
    public void findNotFound() throws Exception {
        assertThrows(NotFound.class,
                () -> bookService.find(Long.MAX_VALUE));
    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        List<Book> books = bookService.findAll();
        assertThat(books.size(), is(greaterThan(0)));

        String previousTitle = null;
        for (Book book : books) {
            if (previousTitle != null) {
                assertThat(book.getTitle(), is(greaterThan(previousTitle)));
            }
            previousTitle = book.getTitle();
        }

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {
        Book book = newBook();
        Book inserted = bookService.insert(book);
        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        assertThat(findBookById(inserted.getId()).isPresent(), is(true));
    }

    @Test
    public void insertBadRequest() throws Exception {

        // Completely empty instance
        final Book book0 = new Book();
        assertThrows(BadRequest.class,
                () -> bookService.insert(book0));

        // Missing authorId field
        final Book book1 = newBook();
        book1.setAuthorId(null);
        assertThrows(BadRequest.class,
                () -> bookService.insert(book1));

        // Invalid authorId field
        final Book book2 = newBook();
        book2.setAuthorId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> bookService.insert(book2));

        // Missing title field
        final Book book3 = newBook();
        book3.setTitle(null);
        assertThrows(BadRequest.class,
                () -> bookService.insert(book3));

    }

    @Test
    public void insertNotUnique() throws Exception {
        // No uniqueness constraints to test
    }

    // update() tests

    @Test
    public void updateHappy() throws Exception {

        // Get original entity
        Book original = findFirstBookByTitle("book");

        // Update this entity
        Book book = original.clone();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }
        book.setTitle(book.getTitle() + " Updated");
        Book updated = bookService.update(book);

        // Validate this entity
        assertThat(updated.getId(), is(book.getId()));
        assertThat(updated.getPublished(), is(book.getPublished()));
        assertThat(updated.getUpdated(), is(greaterThan(original.getUpdated())));
        assertThat(updated.getVersion(), is(greaterThan(original.getVersion())));
        assertThat(updated.getTitle(), is(original.getTitle() + " Updated"));

    }

    @Test
    public void updateBadRequest() throws Exception {

        // Get original entity
        Book original = findFirstBookByTitle("book");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }

        // Completely empty instance
        final Book book0 = new Book();
        assertThrows(BadRequest.class,
                () -> bookService.update(book0));

        // Missing authorId field
        final Book book1 = original.clone();
        book1.setAuthorId(null);
        assertThrows(BadRequest.class,
                () -> bookService.update(book1));

        // Invalid authorId field
        final Book book2 = original.clone();
        book2.setAuthorId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> bookService.update(book1));

        // Missing title field
        final Book book3 = original.clone();
        book3.setTitle(null);
        assertThrows(BadRequest.class,
                () -> bookService.update(book3));

    }

    @Test
    public void updateNotUnique() throws Exception {
        // No uniqueness constraints to test
    }

    // Private Methods -------------------------------------------------------

    private List<Book> findAllBooks() {
        return entityManager.createNamedQuery
                (BOOK_NAME + ".findAll", Book.class)
                .getResultList();
    }

    private List<Author> findAllAuthors() {
        return entityManager.createNamedQuery
                (AUTHOR_NAME + ".findAll", Author.class)
                .getResultList();
    }

    private Optional<Book> findBookById(Long BookId) {
        TypedQuery<Book> query = entityManager.createNamedQuery
                (BOOK_NAME + ".findById", Book.class);
        query.setParameter(ID_COLUMN, BookId);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private List<Book> findBooksByTitle(String title) {
        TypedQuery<Book> query = entityManager.createNamedQuery
                (BOOK_NAME + ".findByTitle", Book.class);
        query.setParameter(TITLE_COLUMN, title);
        return query.getResultList();
    }

    private Book findFirstBookByTitle(String title) {
        List<Book> Books = findBooksByTitle(title);
        assertThat(Books.size(), is(greaterThan(0)));
        return Books.get(0);
    }

    private List<Member> findMembersByBookId(Long BookId) {
        TypedQuery<Member> query = entityManager.createNamedQuery
                (MEMBER_NAME + ".findByBookId", Member.class);
        query.setParameter(BOOK_ID_COLUMN, BookId);
        return query.getResultList();
    }

    private List<Story> findStoriesByBookId(Long BookId) {
        TypedQuery<Story> query = entityManager.createNamedQuery
                (STORY_NAME + ".findByBookId", Story.class);
        query.setParameter(BOOK_ID_COLUMN, BookId);
        return query.getResultList();
    }

    private Book newBook() {
        List<Author> authors = findAllAuthors();
        assertThat(authors.size(), is(greaterThan(0)));
        return new Book(
                authors.get(0).getId(),
                Book.Location.OTHER,
                "Notes about New Book",
                true,
                "New Book");
    }

}
