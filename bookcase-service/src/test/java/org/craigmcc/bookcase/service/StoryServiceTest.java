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
import org.craigmcc.bookcase.model.Book;
import org.craigmcc.bookcase.model.Story;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.NotFound;
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
import static org.craigmcc.bookcase.model.Constants.BOOK_NAME;
import static org.craigmcc.bookcase.model.Constants.STORY_NAME;
import static org.craigmcc.library.model.Constants.ID_COLUMN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

@Category(ServiceTests.class)
@RunWith(Arquillian.class)
public class StoryServiceTest extends AbstractServiceTest {

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "testStory.jar")
                .addClass(StoryService.class);
        addServiceFixtures(archive, false);
        System.out.println("StoryServiceTest:  Assembled Archive:");
        System.out.println(archive.toString(true));
        return archive;
    }

    @Inject
    StoryService storyService;

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

        List<Story> stories = findAllStories();
        assertThat(stories.size(), is(greaterThan(0)));

        for (Story story : stories) {

            // Delete and verify we can no longer retrieve it
            storyService.delete(story.getId());
            assertThat(findStoryById(story.getId()).isPresent(), is(false));

        }

        assertThat(storyService.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {
        Story Story = new Story();
        Story.setId(Long.MAX_VALUE);
        assertThat(findStoryById(Story.getId()).isPresent(), is(false));
    }

    // find() tests

    @Test
    public void findHappy() throws Exception {

        List<Story> stories = storyService.findAll();
        assertThat(stories.size(), is(greaterThan(0)));

        for (Story story : stories) {
            Story found = storyService.find(story.getId());
            assertThat(found.equals(story), is(true));
        }

    }

    @Test
    public void findNotFound() throws Exception {
        assertThrows(NotFound.class,
                () -> storyService.find(Long.MAX_VALUE));
    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        List<Story> stories = storyService.findAll();
        assertThat(stories.size(), is(greaterThan(0)));

    }

    // findByAnthologyId() tests

    public void findByAnthologyIdHappy() throws Exception {

        Anthology anthology = findFirstAnthology();
        List<Story> stories = storyService.findByAnthologyId(anthology.getId());
        assertThat(stories.size(), is(greaterThan(0)));

        Integer previousOrdinal = null;
        for (Story story : stories) {
            if (previousOrdinal != null) {
                assertThat(story.getOrdinal(), is(greaterThan(previousOrdinal)));
            }
            previousOrdinal = story.getOrdinal();
        }

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {
        Story story = newStory();
        Story inserted = storyService.insert(story);
        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        assertThat(findStoryById(inserted.getId()).isPresent(), is(true));
    }

    @Test
    public void insertBadRequest() throws Exception {

        // Completely empty instance
        final Story story0 = new Story();
        assertThrows(BadRequest.class,
                () -> storyService.insert(story0));

        // Missing anthologyId field
        final Story story3 = newStory();
        story3.setAnthologyId(null);
        assertThrows(BadRequest.class,
                () -> storyService.insert(story3));

        // Invalid anthologyId field
        final Story story4 = newStory();
        story4.setAnthologyId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> storyService.insert(story4));

        // Missing bookId field
        final Story story1 = newStory();
        story1.setBookId(null);
        assertThrows(BadRequest.class,
                () -> storyService.insert(story1));

        // Invalid bookId field
        final Story story2 = newStory();
        story2.setBookId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> storyService.insert(story2));

    }

    @Test
    public void insertNotUnique() throws Exception {
        // No uniqueness constraints to test
    }

    // update() tests

    @Test
    public void updateHappy() throws Exception {

        // Get original entity
        Story original = findFirstStory();

        // Update this entity
        Story story = original.clone();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }
        story.setOrdinal(story.getOrdinal() + 100);
        Story updated = storyService.update(story.getId(), story);

        // Validate this entity
        assertThat(updated.getId(), is(story.getId()));
        assertThat(updated.getPublished(), is(story.getPublished()));
        assertThat(updated.getUpdated(), is(greaterThan(original.getUpdated())));
        assertThat(updated.getVersion(), is(greaterThan(original.getVersion())));
        assertThat(updated.getOrdinal(), is(original.getOrdinal() + 100));

    }

    @Test
    public void updateBadRequest() throws Exception {

        // Get original entity
        Story original = findFirstStory();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }

        // Missing anthologyId field
        final Story story3 = original.clone();
        story3.setAnthologyId(null);
        assertThrows(BadRequest.class,
                () -> storyService.update(story3.getId(), story3));

        // Invalid anthologyId field
        final Story story4 = original.clone();
        story4.setAnthologyId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> storyService.update(story4.getId(),story4));

        // Missing bookId field
        final Story story1 = original.clone();
        story1.setBookId(null);
        assertThrows(BadRequest.class,
                () -> storyService.update(story1.getId(), story1));

        // Invalid bookId field
        final Story story2 = original.clone();
        story2.setBookId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> storyService.update(story2.getId(), story2));

    }

    @Test
    public void updateNotUnique() throws Exception {
        // No uniqueness constraints to test
    }

    // Private Methods -------------------------------------------------------

    private List<Story> findAllStories() {
        return entityManager.createNamedQuery
                (STORY_NAME + ".findAll", Story.class)
                .getResultList();
    }

    private Anthology findFirstAnthology() {
        TypedQuery<Anthology> AnthologyQuery = entityManager.createNamedQuery
                (ANTHOLOGY_NAME + ".findAll", Anthology.class);
        List<Anthology> Anthologyes = AnthologyQuery.getResultList();
        return Anthologyes.get(0);
    }

    private Book findFirstBook() {
        TypedQuery<Book> bookQuery = entityManager.createNamedQuery
                (BOOK_NAME + ".findAll", Book.class);
        List<Book> books = bookQuery.getResultList();
        return books.get(0);
    }

    private Story findFirstStory() {
        List<Story> Storys = findAllStories();
        return Storys.get(0);
    }

    private Optional<Story> findStoryById(Long storyId) {
        TypedQuery<Story> query = entityManager.createNamedQuery
                (STORY_NAME + ".findById", Story.class)
                .setParameter(ID_COLUMN, storyId);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private Story newStory() {
        return new Story(findFirstAnthology().getId(), findFirstBook().getId(), 123);
    }

}
