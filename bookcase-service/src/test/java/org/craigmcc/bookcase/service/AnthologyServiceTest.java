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
import org.craigmcc.bookcase.model.Anthology;
import org.craigmcc.bookcase.model.Author;
import org.craigmcc.bookcase.model.Book;
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

import static org.craigmcc.bookcase.model.Constants.ANTHOLOGY_ID_COLUMN;
import static org.craigmcc.bookcase.model.Constants.ANTHOLOGY_NAME;
import static org.craigmcc.bookcase.model.Constants.AUTHOR_NAME;
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
public class AnthologyServiceTest extends AbstractServiceTest {

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive archive =
                ShrinkWrap.create(JavaArchive.class, "testAnthology.jar")
                .addClass(AnthologyService.class);
        addServiceFixtures(archive, false);
        System.out.println("AnthologyServiceTest:  Assembled Archive:");
        System.out.println(archive.toString(true));
        return archive;
    }

    @Inject
    AnthologyService anthologyService;

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

        List<Anthology> anthologies = findAllAnthologies();
        assertThat(anthologies.size(), is(greaterThan(0)));

        for (Anthology anthology : anthologies) {

            // Test data should not have any anthologies with no stories
            List<Story> stories = findStoriesByAnthologyId(anthology.getId());
            assertThat(stories.size(), greaterThan(0));

            // Delete and verify we can no longer retrieve it
            anthologyService.delete(anthology.getId());
            assertThat(findAnthologyById(anthology.getId()).isPresent(), is(false));

            // Delete should have cascaded to stories
            assertThat(findStoriesByAnthologyId(anthology.getId()).size(), is(0));

        }

        assertThat(anthologyService.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {
        Anthology anthology = new Anthology();
        anthology.setId(Long.MAX_VALUE);
        assertThat(findAnthologyById(anthology.getId()).isPresent(), is(false));
    }

    // find() tests

    @Test
    public void findHappy() throws Exception {

        List<Anthology> anthologies = anthologyService.findAll();
        assertThat(anthologies.size(), is(greaterThan(0)));

        for (Anthology anthology : anthologies) {
            Anthology found = anthologyService.find(anthology.getId());
            assertThat(found.equals(anthology), is(true));
        }

    }

    @Test
    public void findNotFound() throws Exception {
        assertThrows(NotFound.class,
                () -> anthologyService.find(Long.MAX_VALUE));
    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        List<Anthology> anthologies = anthologyService.findAll();
        assertThat(anthologies.size(), is(greaterThan(0)));

        String previousTitle = null;
        for (Anthology anthology : anthologies) {
            if (previousTitle != null) {
                assertThat(anthology.getTitle(), is(greaterThan(previousTitle)));
            }
            previousTitle = anthology.getTitle();
        }

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {
        Anthology anthology = newAnthology();
        Anthology inserted = anthologyService.insert(anthology);
        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        assertThat(findAnthologyById(inserted.getId()).isPresent(), is(true));
    }

    @Test
    public void insertBadRequest() throws Exception {

        // Completely empty instance
        final Anthology anthology0 = new Anthology();
        assertThrows(BadRequest.class,
                () -> anthologyService.insert(anthology0));

        // Missing authorId field
        final Anthology anthology1 = newAnthology();
        anthology1.setAuthorId(null);
        assertThrows(BadRequest.class,
                () -> anthologyService.insert(anthology1));

        // Invalid authorId field
        final Anthology anthology2 = newAnthology();
        anthology2.setAuthorId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> anthologyService.insert(anthology1));

        // Missing title field
        final Anthology anthology3 = newAnthology();
        anthology3.setTitle(null);
        assertThrows(BadRequest.class,
                () -> anthologyService.insert(anthology3));

    }

    @Test
    public void insertNotUnique() throws Exception {
        // No uniqueness constraints to test
    }

    // update() tests

    @Test
    public void updateHappy() throws Exception {

        // Get original entity
        Anthology original = findFirstAnthologyByTitle("by");

        // Update this entity
        Anthology anthology = original.clone();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }
        anthology.setTitle(anthology.getTitle() + " Updated");
        Anthology updated = anthologyService.update(anthology);

        // Validate this entity
        assertThat(updated.getId(), is(anthology.getId()));
        assertThat(updated.getPublished(), is(anthology.getPublished()));
        assertThat(updated.getUpdated(), is(greaterThan(original.getUpdated())));
        assertThat(updated.getVersion(), is(greaterThan(original.getVersion())));
        assertThat(updated.getTitle(), is(original.getTitle() + " Updated"));

    }

    @Test
    public void updateBadRequest() throws Exception {

        // Get original entity
        Anthology original = findFirstAnthologyByTitle(" by ");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }

        // Completely empty instance
        final Anthology anthology0 = new Anthology();
        assertThrows(NotFound.class,
                () -> anthologyService.update(anthology0));

        // Missing authorId field
        final Anthology anthology1 = original.clone();
        anthology1.setAuthorId(null);
        assertThrows(BadRequest.class,
                () -> anthologyService.update(anthology1));

        // Invalid authorId field
        final Anthology anthology2 = original.clone();
        anthology2.setAuthorId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> anthologyService.update(anthology1));

        // Missing title field
        final Anthology anthology3 = original.clone();
        anthology3.setTitle(null);
        assertThrows(BadRequest.class,
                () -> anthologyService.update(anthology3));

    }

    @Test
    public void updateNotUnique() throws Exception {
        // No uniqueness constraints to test
    }

    // Private Methods -------------------------------------------------------

    private List<Anthology> findAllAnthologies() {
        return entityManager.createNamedQuery
                (ANTHOLOGY_NAME + ".findAll", Anthology.class)
                .getResultList();
    }

    private List<Author> findAllAuthors() {
        return entityManager.createNamedQuery
                (AUTHOR_NAME + ".findAll", Author.class)
                .getResultList();
    }

    private Optional<Anthology> findAnthologyById(Long anthologyId) {
        TypedQuery<Anthology> query = entityManager.createNamedQuery
                (ANTHOLOGY_NAME + ".findById", Anthology.class)
                .setParameter(ID_COLUMN, anthologyId);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private List<Anthology> findAnthologiesByTitle(String title) {
        TypedQuery<Anthology> query = entityManager.createNamedQuery
                (ANTHOLOGY_NAME + ".findByTitle", Anthology.class)
                .setParameter(TITLE_COLUMN, title);
        return query.getResultList();
    }

    private Anthology findFirstAnthologyByTitle(String title) {
        List<Anthology> anthologies = findAnthologiesByTitle(title);
        assertThat(anthologies.size(), is(greaterThan(0)));
        return anthologies.get(0);
    }

    private List<Story> findStoriesByAnthologyId(Long anthologyId) {
        TypedQuery<Story> query = entityManager.createNamedQuery
                (STORY_NAME + ".findByAnthologyId", Story.class)
                .setParameter(ANTHOLOGY_ID_COLUMN, anthologyId);
        return query.getResultList();
    }

    private Anthology newAnthology() {
        List<Author> authors = findAllAuthors();
        assertThat(authors.size(), is(greaterThan(0)));
        return new Anthology(
                authors.get(0).getId(),
                Book.Location.OTHER,
                "Notes about New Anthology",
                true,
                "New Anthology");
    }

}
