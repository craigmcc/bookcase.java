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
import org.craigmcc.bookcase.model.Member;
import org.craigmcc.bookcase.model.Series;
import org.craigmcc.bookcase.model.Story;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * <p>Split out from {@link DevModeStartupService} so that service and integration
 * tests can call it separately if needed.</p>
 */
@LocalBean
@Singleton
public class DevModePopulateService {

    // Instance Variables ----------------------------------------------------

    // Key is "title"
    private Map<String, Anthology> anthologies = new HashMap<>();

    // Key is "lastName|firstName"
    private Map<String, Author> authors = new HashMap<>();

    // Key is "title"
    private Map<String, Book> books = new HashMap<>();

    @PersistenceContext
    private EntityManager entityManager;

    // Key is "title"
    private Map<String, Series> seriesMap = new HashMap<>();

    // Static Variables ------------------------------------------------------

    private static final Logger LOG =
            Logger.getLogger(DevModePopulateService.class.getSimpleName());

    // Public Methods --------------------------------------------------------

    public void populate() {
        LOG.info("----- Populate Development Test Data Begin -----");
        // Populate data in order respecting dependencies
        populateAuthors();
        populateBooks();
        populateAnthologies();
        populateStories();
        populateSerieses();
        populateMembers();
        // Clean up our temporary data maps
        cleanTemporaryMaps();
        LOG.info("------ Populate Development Test Data End ------");
    }

    // Private Methods -------------------------------------------------------

    private void cleanTemporaryMaps() {
        anthologies.clear();
        authors.clear();
        books.clear();
        seriesMap.clear();
    }

    private Long lookupAuthor(String firstName, String lastName) {
        Author author = authors.get(lastName + "|" + firstName);
        if (author == null) {
            throw new IllegalArgumentException("Cannot find author for " + firstName + ", " + lastName);
        } else if (author.getId() == null) {
            throw new IllegalArgumentException("No ID for author " + firstName + ", " + lastName);
        }
        return author.getId();
    }

    private Long lookupAnthology(String title) {
        Anthology anthology = anthologies.get(title);
        if (anthology == null) {
            throw new IllegalArgumentException("Cannot find anthology for " + title);
        } else if (anthology.getId() == null) {
            throw new IllegalArgumentException("No ID for anthology " + title);
        }
        return anthology.getId();
    }

    private Long lookupBook(String title) {
        Book book = books.get(title);
        if (book == null) {
            throw new IllegalArgumentException("Cannot find book for " + title);
        } else if (book.getId() == null) {
            throw new IllegalArgumentException("No ID for book " + title);
        }
        return book.getId();
    }

    private Long lookupSeries(String title) {
        Series series = seriesMap.get(title);
        if (series == null) {
            throw new IllegalArgumentException("Cannot find series for " + title);
        } else if (series.getId() == null) {
            throw new IllegalArgumentException("No ID for series " + title);
        }
        return series.getId();
    }

    private void populateAnthology(Long authorId, Book.Location location, String notes, Boolean read, String title) {
        Anthology anthology = new Anthology(authorId, location, notes, read, title);
        anthology.setPublished(LocalDateTime.now());
        anthology.setUpdated(anthology.getPublished());
        entityManager.persist(anthology);
        anthologies.put(title, anthology);
    }

    private void populateAuthor(String firstName, String lastName, String notes) {
        Author author = new Author(firstName, lastName, notes);
        author.setPublished(LocalDateTime.now());
        author.setUpdated(author.getPublished());
        entityManager.persist(author);
        authors.put(lastName + "|" + firstName, author);
    }

    private void populateBook(Long authorId, Book.Location location, String notes, Boolean read, String title) {
        Book book = new Book(authorId, location, notes, read, title);
        book.setPublished(LocalDateTime.now());
        book.setUpdated(book.getPublished());
        entityManager.persist(book);
        books.put(title, book);
    }

    private void populateMember(Long bookId, Integer ordinal, Long seriesId) {
        Member member = new Member(bookId, ordinal, seriesId);
        member.setPublished(LocalDateTime.now());
        member.setUpdated(member.getPublished());
        entityManager.persist(member);
    }

    private void populateSeries(Long authorId, String notes, String title) {
        Series series = new Series(authorId, notes, title);
        series.setPublished(LocalDateTime.now());
        series.setUpdated(series.getPublished());
        entityManager.persist(series);
        seriesMap.put(title, series);
    }

    private void populateStory(Long anthologyId, Long bookId, Integer ordinal) {
        Story story = new Story(anthologyId, bookId, ordinal);
        story.setPublished(LocalDateTime.now());
        story.setUpdated(story.getPublished());
        entityManager.persist(story);
    }

    private void populateAnthologies() {
        LOG.info("Populating anthologies begin");
        populateAnthology(lookupAuthor("Wilma", "Flintstone"), Book.Location.KINDLE, null, Boolean.FALSE, "First Anthology By Wilma");
        populateAnthology(lookupAuthor("Fred", "Flintstone"), Book.Location.KOBO, null, Boolean.TRUE, "First Anthology By Fred");
        populateAnthology(lookupAuthor("Barney", "Rubble"), Book.Location.UNLIMITED, null, Boolean.FALSE, "First Anthology By Barney");
        LOG.info("Populating anthologies end");
    }

    private void populateAuthors() {
        LOG.info("Populating authors begin");
        populateAuthor("Wilma", "Flintstone", null);
        populateAuthor("Fred", "Flintstone", null);
        populateAuthor("Barney", "Rubble", null);
        populateAuthor("Betty", "Rubble", null);
        populateAuthor("Pebbles", "Flintstone", null);
        populateAuthor("Bam Bam", "Rubble", null);
        LOG.info("Populating authors end");
    }

    private void populateBooks() {
        LOG.info("Populating books begin");
        populateBook(lookupAuthor("Wilma", "Flintstone"), Book.Location.KINDLE, null, Boolean.TRUE, "Wilma Second Book");
        populateBook(lookupAuthor("Wilma", "Flintstone"), Book.Location.KINDLE, null, Boolean.FALSE, "Wilma First Book");
        populateBook(lookupAuthor("Fred", "Flintstone"), Book.Location.KOBO, null, Boolean.TRUE, "Fred Second Book");
        populateBook(lookupAuthor("Fred", "Flintstone"), Book.Location.KOBO, null, Boolean.FALSE, "Fred First Book");
        populateBook(lookupAuthor("Barney", "Rubble"), Book.Location.UNLIMITED, null, Boolean.TRUE, "Barney Second Book");
        populateBook(lookupAuthor("Barney", "Rubble"), Book.Location.UNLIMITED, null, Boolean.FALSE, "Barney First Book");
        LOG.info("Populating books end");
    }

    private void populateMembers() {
        LOG.info("Populating members begin");
        populateMember(lookupBook("Wilma First Book"), 1, lookupSeries("Second Series By Wilma"));
        populateMember(lookupBook("Wilma Second Book"), 2, lookupSeries("Second Series By Wilma"));
        populateMember(lookupBook("Fred First Book"), 1, lookupSeries("First Series By Fred"));
        populateMember(lookupBook("Fred Second Book"), 2, lookupSeries("First Series By Fred"));
        populateMember(lookupBook("Barney First Book"), 1, lookupSeries("Second Series By Barney"));
        populateMember(lookupBook("Barney Second Book"), 2, lookupSeries("Second Series By Barney"));
        LOG.info("Populating members end");
    }

    private void populateSerieses() {
        LOG.info("Populating series begin");
        populateSeries(lookupAuthor("Wilma", "Flintstone"), null, "Second Series By Wilma");
        populateSeries(lookupAuthor("Wilma", "Flintstone"), null, "First Series By Wilma");
        populateSeries(lookupAuthor("Fred", "Flintstone"), null, "Second Series By Fred");
        populateSeries(lookupAuthor("Fred", "Flintstone"), null, "First Series By Fred");
        populateSeries(lookupAuthor("Barney", "Rubble"), null, "Second Series By Barney");
        populateSeries(lookupAuthor("Barney", "Rubble"), null, "First Series By Barney");
        LOG.info("Populating series end");
    }

    private void populateStories() {
        LOG.info("Populating stories begin");
        populateStory(lookupAnthology("First Anthology By Wilma"), lookupBook("Fred Second Book"), 1);
        populateStory(lookupAnthology("First Anthology By Wilma"), lookupBook("Fred First Book"), 2);
        populateStory(lookupAnthology("First Anthology By Wilma"), lookupBook("Barney Second Book"), 3);
        populateStory(lookupAnthology("First Anthology By Wilma"), lookupBook("Barney First Book"), 4);
        populateStory(lookupAnthology("First Anthology By Fred"), lookupBook("Wilma Second Book"), 1);
        populateStory(lookupAnthology("First Anthology By Fred"), lookupBook("Wilma First Book"), 2);
        populateStory(lookupAnthology("First Anthology By Fred"), lookupBook("Barney Second Book"), 3);
        populateStory(lookupAnthology("First Anthology By Fred"), lookupBook("Barney First Book"), 4);
        populateStory(lookupAnthology("First Anthology By Barney"), lookupBook("Wilma Second Book"), 1);
        populateStory(lookupAnthology("First Anthology By Barney"), lookupBook("Wilma First Book"), 2);
        populateStory(lookupAnthology("First Anthology By Barney"), lookupBook("Fred Second Book"), 3);
        populateStory(lookupAnthology("First Anthology By Barney"), lookupBook("Fred First Book"), 4);
        LOG.info("Populating stories end");
    }

}
