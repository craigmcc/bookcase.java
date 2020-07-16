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

import org.craigmcc.bookcase.model.Anthology;
import org.craigmcc.bookcase.model.Author;
import org.craigmcc.bookcase.model.Book;
import org.craigmcc.bookcase.model.Series;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.NotFound;
import org.craigmcc.library.shared.exception.NotUnique;
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

import static org.craigmcc.bookcase.model.Constants.ANTHOLOGY_NAME;
import static org.craigmcc.bookcase.model.Constants.AUTHOR_ID_COLUMN;
import static org.craigmcc.bookcase.model.Constants.AUTHOR_NAME;
import static org.craigmcc.bookcase.model.Constants.BOOK_NAME;
import static org.craigmcc.bookcase.model.Constants.FIRST_NAME_COLUMN;
import static org.craigmcc.bookcase.model.Constants.LAST_NAME_COLUMN;
import static org.craigmcc.bookcase.model.Constants.SERIES_NAME;
import static org.craigmcc.library.model.Constants.ID_COLUMN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

@Category(ServiceTests.class)
@RunWith(Arquillian.class)
public class AuthorServiceTest extends AbstractServiceTest {

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "testAuthor.jar")
                .addClass(AuthorService.class);
        addServiceFixtures(archive, false);
        System.out.println("AuthorServiceTest:  Assembled Archive:");
        System.out.println(archive.toString(true));
        return archive;
    }

    @Inject
    AuthorService authorService;

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

        List<Author> authors = findAllAuthors();
        assertThat(authors.size(), is(greaterThan(0)));

        for (Author author : authors) {

/*          (Not true for actual test data)
            // Test data should not have any authors with no anthologies
            List<Anthology> anthologies = findAnthologiesByAuthorId(author.getId());
            assertThat(anthologies.size(), greaterThan(0));
*/

/*          (Not true for actual test data)
            // Test data should not have any authors with no books
            List<Book> books = findBooksByAuthorId(author.getId());
            assertThat(books.size(), greaterThan(0));
*/

/*          (Not true for actual test data)
            // Test data should not have any authors with no series
            List<Series> seriess = findSeriesByAuthorId(author.getId());
            assertThat(series.size(), greaterThan(0));
*/

            // Delete and verify we can no longer retrieve it
            authorService.delete(author.getId());
            assertThat(findAuthorById(author.getId()).isPresent(), is(false));

            // Delete should have cascaded to anthologies/books/series
            assertThat(findAnthologiesByAuthorId(author.getId()).size(), is(0));
            assertThat(findBooksByAuthorId(author.getId()).size(), is(0));
            assertThat(findSeriesByAuthorId(author.getId()).size(), is(0));

        }

        assertThat(authorService.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {
        Author author = new Author();
        author.setId(Long.MAX_VALUE);
        assertThat(findAuthorById(author.getId()).isPresent(), is(false));
    }

    // find() tests

    @Test
    public void findHappy() throws Exception {

        List<Author> authors = authorService.findAll();
        assertThat(authors.size(), is(greaterThan(0)));

        for (Author author : authors) {
            Author found = authorService.find(author.getId());
            assertThat(found.equals(author), is(true));
        }

    }

    @Test
    public void findNotFound() throws Exception {
        assertThrows(NotFound.class,
                () -> authorService.find(Long.MAX_VALUE));
    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        List<Author> authors = authorService.findAll();
        assertThat(authors.size(), is(greaterThan(0)));

        String previousName = null;
        for (Author author : authors) {
            String thisName = author.getLastName() +  "|" + author.getFirstName();
            if (previousName != null) {
                assertThat(thisName, is(greaterThan(previousName)));
            }
            previousName = thisName;
        }

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {
        Author author = newAuthor();
        Author inserted = authorService.insert(author);
        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        assertThat(findAuthorById(inserted.getId()).isPresent(), is(true));
    }

    @Test
    public void insertBadRequest() throws Exception {

        // Completely empty instance
        final Author author0 = new Author();
        assertThrows(BadRequest.class,
                () -> authorService.insert(author0));

        // Missing firstName field
        final Author author1 = newAuthor();
        author1.setFirstName(null);
        author1.setLastName("Bar");
        assertThrows(BadRequest.class,
                () -> authorService.insert(author1));

        // Missing lastName field
        final Author author2 = newAuthor();
        author2.setFirstName("Foo");
        author2.setLastName(null);
        assertThrows(BadRequest.class,
                () -> authorService.insert(author2));

    }

    @Test
    public void insertNotUnique() throws Exception {

        Author author = new Author("Barney", "Rubble", "New notes about Barney");
        author.setId(null);
        assertThrows(NotUnique.class,
                () -> authorService.insert(author));

    }

    // update() tests

    @Test
    public void updateHappy() throws Exception {

        // Get original entity
        Author original = findFirstAuthorByName("Flintstone");

        // Update this entity
        Author author = original.clone();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }
        author.setNotes("Updated");
        Author updated = authorService.update(author.getId(), author);

        // Validate this entity
        assertThat(updated.getId(), is(author.getId()));
        assertThat(updated.getPublished(), is(author.getPublished()));
        assertThat(updated.getUpdated(), is(greaterThan(original.getUpdated())));
        assertThat(updated.getVersion(), is(greaterThan(original.getVersion())));
        assertThat(updated.getFirstName(), is(original.getFirstName()));
        assertThat(updated.getLastName(), is(original.getLastName()));
        assertThat(updated.getNotes(), is("Updated"));

    }

    @Test
    public void updateBadRequest() throws Exception {

        // Get original entity
        Author original = findFirstAuthorByName("Rubble");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }

        // Missing firstName field
        final Author author1 = newAuthor();
        author1.setId(original.getId());
        author1.setFirstName(null);
        author1.setLastName("Bar");
        assertThrows(BadRequest.class,
                () -> authorService.update(author1.getId(), author1));

        // Missing lastName field
        final Author author2 = newAuthor();
        author2.setId(original.getId());
        author2.setFirstName("Foo");
        author2.setLastName(null);
        assertThrows(BadRequest.class,
                () -> authorService.update(author2.getId(), author2));

    }

    @Test
    public void updateNotUnique() throws Exception {

        Author author = findFirstAuthorByName("Flintstone");
        author.setFirstName("Barney");
        author.setLastName("Rubble");
        assertThrows(NotUnique.class,
                () -> authorService.update(author.getId(), author));

    }

    // Private Methods -------------------------------------------------------

    private List<Author> findAllAuthors() {
        return entityManager.createNamedQuery
                (AUTHOR_NAME + ".findAll", Author.class)
                .getResultList();
    }

    private Optional<Author> findAuthorById(Long AuthorId) {
        TypedQuery<Author> query = entityManager.createNamedQuery
                (AUTHOR_NAME + ".findById", Author.class);
        query.setParameter(ID_COLUMN, AuthorId);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private List<Author> findAuthorsByName(String name) {
        TypedQuery<Author> query = entityManager.createNamedQuery
                (AUTHOR_NAME + ".findByName", Author.class);
        query.setParameter(FIRST_NAME_COLUMN, name);
        query.setParameter(LAST_NAME_COLUMN, name);
        return query.getResultList();
    }

    private Author findFirstAuthorByName(String name) {
        List<Author> authors = findAuthorsByName(name);
        assertThat(authors.size(), is(greaterThan(0)));
        return authors.get(0);
    }

    private List<Anthology> findAnthologiesByAuthorId(Long authorId) {
        TypedQuery<Anthology> query = entityManager.createNamedQuery
                (ANTHOLOGY_NAME + ".findByAuthorId", Anthology.class);
        query.setParameter(AUTHOR_ID_COLUMN, authorId);
        return query.getResultList();
    }

    private List<Book> findBooksByAuthorId(Long authorId) {
        TypedQuery<Book> query = entityManager.createNamedQuery
                (BOOK_NAME + ".findByAuthorId", Book.class);
        query.setParameter(AUTHOR_ID_COLUMN, authorId);
        return query.getResultList();
    }

    private List<Series> findSeriesByAuthorId(Long authorId) {
        TypedQuery<Series> query = entityManager.createNamedQuery
                (SERIES_NAME + ".findByAuthorId", Series.class);
        query.setParameter(AUTHOR_ID_COLUMN, authorId);
        return query.getResultList();
    }

    private Author newAuthor() {
        List<Author> authors = findAllAuthors();
        assertThat(authors.size(), is(greaterThan(0)));
        return new Author(
                "Another",
                "Rubble",
                "Notes about Another Rubble"
        );
    }

}
