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
import org.craigmcc.bookcase.model.Member;
import org.craigmcc.bookcase.model.Series;
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
import static org.craigmcc.bookcase.model.Constants.MEMBER_NAME;
import static org.craigmcc.bookcase.model.Constants.SERIES_ID_COLUMN;
import static org.craigmcc.bookcase.model.Constants.SERIES_NAME;
import static org.craigmcc.bookcase.model.Constants.TITLE_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

@Category(ServiceTests.class)
@RunWith(Arquillian.class)
public class SeriesServiceTest extends AbstractServiceTest {

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "testSeries.jar")
                .addClass(SeriesService.class);
        addServiceFixtures(archive, false);
        System.out.println("SeriesServiceTest:  Assembled Archive:");
        System.out.println(archive.toString(true));
        return archive;
    }

    @Inject
    SeriesService seriesService;

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

        List<Series> serieses = findAllSeries();
        assertThat(serieses.size(), is(greaterThan(0)));

        for (Series series : serieses) {

/*          (Not true for the actual test data)
            // Test data should not have any series with no members
            List<Member> members = findMembersBySeriesId(series.getId());
            assertThat(members.size(), greaterThan(0));
*/

            // Delete and verify we can no longer retrieve it
            seriesService.delete(series);
            assertThat(findSeriesById(series.getId()).isPresent(), is(false));

            // Delete should have cascaded to members
            assertThat(findMembersBySeriesId(series.getId()).size(), is(0));

        }

        assertThat(seriesService.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {
        Series Series = new Series();
        Series.setId(Long.MAX_VALUE);
        assertThat(findSeriesById(Series.getId()).isPresent(), is(false));
    }

    // find() tests

    @Test
    public void findHappy() throws Exception {

        List<Series> serieses = seriesService.findAll();
        assertThat(serieses.size(), is(greaterThan(0)));

        for (Series series : serieses) {
            Series found = seriesService.find(series.getId());
            assertThat(found.equals(series), is(true));
        }

    }

    @Test
    public void findNotFound() throws Exception {
        assertThrows(NotFound.class,
                () -> seriesService.find(Long.MAX_VALUE));
    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        List<Series> serieses = seriesService.findAll();
        assertThat(serieses.size(), is(greaterThan(0)));

        String previousTitle = null;
        for (Series series : serieses) {
            if (previousTitle != null) {
                assertThat(series.getTitle(), is(greaterThan(previousTitle)));
            }
            previousTitle = series.getTitle();
        }

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {
        Series series = newSeries();
        Series inserted = seriesService.insert(series);
        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        assertThat(findSeriesById(inserted.getId()).isPresent(), is(true));
    }

    @Test
    public void insertBadRequest() throws Exception {

        // Completely empty instance
        final Series series0 = new Series();
        assertThrows(BadRequest.class,
                () -> seriesService.insert(series0));

        // Missing authorId field
        final Series series1 = newSeries();
        series1.setAuthorId(null);
        assertThrows(BadRequest.class,
                () -> seriesService.insert(series1));

        // Invalid authorId field
        final Series series2 = newSeries();
        series2.setAuthorId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> seriesService.insert(series1));

        // Missing title field
        final Series series3 = newSeries();
        series3.setTitle(null);
        assertThrows(BadRequest.class,
                () -> seriesService.insert(series3));

    }

    @Test
    public void insertNotUnique() throws Exception {
        // No uniqueness constraints to test
    }

    // update() tests

    @Test
    public void updateHappy() throws Exception {

        // Get original entity
        Series original = findFirstSeriesByTitle("by");

        // Update this entity
        Series series = original.clone();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            /* Ignore */;
        }
        series.setTitle(series.getTitle() + " Updated");
        Series updated = seriesService.update(series);

        // Validate this entity
        assertThat(updated.getId(), is(series.getId()));
        assertThat(updated.getPublished(), is(series.getPublished()));
        assertThat(updated.getUpdated(), is(greaterThan(original.getUpdated())));
        assertThat(updated.getVersion(), is(greaterThan(original.getVersion())));
        assertThat(updated.getTitle(), is(original.getTitle() + " Updated"));

    }

    @Test
    public void updateBadRequest() throws Exception {

        // Get original entity
        Series original = findFirstSeriesByTitle(" by ");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }

        // Completely empty instance
        final Series series0 = new Series();
        assertThrows(BadRequest.class,
                () -> seriesService.update(series0));

        // Missing authorId field
        final Series series1 = original.clone();
        series1.setAuthorId(null);
        assertThrows(BadRequest.class,
                () -> seriesService.update(series1));

        // Invalid authorId field
        final Series series2 = original.clone();
        series2.setAuthorId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> seriesService.update(series1));

        // Missing title field
        final Series series3 = original.clone();
        series3.setTitle(null);
        assertThrows(BadRequest.class,
                () -> seriesService.update(series3));

    }

    @Test
    public void updateNotUnique() throws Exception {
        // No uniqueness constraints to test
    }

    // Private Methods -------------------------------------------------------

    private List<Author> findAllAuthors() {
        return entityManager.createNamedQuery
                (AUTHOR_NAME + ".findAll", Author.class)
                .getResultList();
    }

    private List<Series> findAllSeries() {
        return entityManager.createNamedQuery
                (SERIES_NAME + ".findAll", Series.class)
                .getResultList();
    }

    private Series findFirstSeriesByTitle(String title) {
        List<Series> Series = findSeriesByTitle(title);
        assertThat(Series.size(), is(greaterThan(0)));
        return Series.get(0);
    }

    private List<Member> findMembersBySeriesId(Long SeriesId) {
        TypedQuery<Member> query = entityManager.createNamedQuery
                (MEMBER_NAME + ".findBySeriesId", Member.class);
        query.setParameter(SERIES_ID_COLUMN, SeriesId);
        return query.getResultList();
    }

    private Optional<Series> findSeriesById(Long SeriesId) {
        TypedQuery<Series> query = entityManager.createNamedQuery
                (SERIES_NAME + ".findById", Series.class);
        query.setParameter(ID_COLUMN, SeriesId);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private List<Series> findSeriesByTitle(String title) {
        TypedQuery<Series> query = entityManager.createNamedQuery
                (SERIES_NAME + ".findByTitle", Series.class);
        query.setParameter(TITLE_COLUMN, title);
        return query.getResultList();
    }

    private Series newSeries() {
        List<Author> authors = findAllAuthors();
        assertThat(authors.size(), is(greaterThan(0)));
        return new Series(
                authors.get(0).getId(),
                "Notes about New Series",
                "New Series");
    }

}
