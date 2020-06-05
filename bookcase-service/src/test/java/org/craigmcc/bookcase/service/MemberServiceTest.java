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
import org.craigmcc.bookcase.model.Book;
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

import static org.craigmcc.bookcase.model.Constants.BOOK_NAME;
import static org.craigmcc.bookcase.model.Constants.MEMBER_NAME;
import static org.craigmcc.bookcase.model.Constants.SERIES_NAME;
import static org.craigmcc.library.model.Constants.ID_COLUMN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

@Category(ServiceTests.class)
@RunWith(Arquillian.class)
public class MemberServiceTest extends AbstractServiceTest {

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "testMember.jar")
                .addClass(MemberService.class);
        addServiceFixtures(archive, false);
        System.out.println("MemberServiceTest:  Assembled Archive:");
        System.out.println(archive.toString(true));
        return archive;
    }

    @Inject
    MemberService memberService;

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

        List<Member> members = findAllMembers();
        assertThat(members.size(), is(greaterThan(0)));

        for (Member member: members) {

            // Delete and verify we can no longer retrieve it
            memberService.delete(member.getId());
            assertThat(findMemberById(member.getId()).isPresent(), is(false));

        }

        assertThat(memberService.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {
        Member member = new Member();
        member.setId(Long.MAX_VALUE);
        assertThat(findMemberById(member.getId()).isPresent(), is(false));
    }

    // find() tests

    @Test
    public void findHappy() throws Exception {

        List<Member> members = memberService.findAll();
        assertThat(members.size(), is(greaterThan(0)));

        for (Member member : members) {
            Member found = memberService.find(member.getId());
            assertThat(found.equals(member), is(true));
        }

    }

    @Test
    public void findNotFound() throws Exception {
        assertThrows(NotFound.class,
                () -> memberService.find(Long.MAX_VALUE));
    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        List<Member> members = memberService.findAll();
        assertThat(members.size(), is(greaterThan(0)));

    }

    // findBySeriesId() tests

    public void findBySeriesIdHappy() throws Exception {

        Series series = findFirstSeries();
        List<Member> members = memberService.findBySeriesId(series.getId());
        assertThat(members.size(), is(greaterThan(0)));

        Integer previousOrdinal = null;
        for (Member member : members) {
            if (previousOrdinal != null) {
                assertThat(member.getOrdinal(), is(greaterThan(previousOrdinal)));
            }
            previousOrdinal = member.getOrdinal();
        }

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {
        Member member = newMember();
        Member inserted = memberService.insert(member);
        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        assertThat(findMemberById(inserted.getId()).isPresent(), is(true));
    }

    @Test
    public void insertBadRequest() throws Exception {

        // Completely empty instance
        final Member member0 = new Member();
        assertThrows(BadRequest.class,
                () -> memberService.insert(member0));

        // Missing bookId field
        final Member member1 = newMember();
        member1.setBookId(null);
        assertThrows(BadRequest.class,
                () -> memberService.insert(member1));

        // Invalid bookId field
        final Member member2 = newMember();
        member2.setBookId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> memberService.insert(member2));

        // Missing seriesId field
        final Member member3 = newMember();
        member3.setSeriesId(null);
        assertThrows(BadRequest.class,
                () -> memberService.insert(member3));

        // Invalid seriesId field
        final Member member4 = newMember();
        member4.setSeriesId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> memberService.insert(member4));

    }

    @Test
    public void insertNotUnique() throws Exception {
        // No uniqueness constraints to test
    }

    // update() tests

    @Test
    public void updateHappy() throws Exception {

        // Get original entity
        Member original = findFirstMember();

        // Update this entity
        Member member = original.clone();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }
        member.setOrdinal(member.getOrdinal() + 100);
        Member updated = memberService.update(member);

        // Validate this entity
        assertThat(updated.getId(), is(member.getId()));
        assertThat(updated.getPublished(), is(member.getPublished()));
        assertThat(updated.getUpdated(), is(greaterThan(original.getUpdated())));
        assertThat(updated.getVersion(), is(greaterThan(original.getVersion())));
        assertThat(updated.getOrdinal(), is(original.getOrdinal() + 100));

    }

    @Test
    public void updateBadRequest() throws Exception {

        // Get original entity
        Member original = findFirstMember();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }

        // Completely empty instance
        final Member member0 = new Member();
        assertThrows(NotFound.class,
                () -> memberService.update(member0));

        // Missing bookId field
        final Member member1 = original.clone();
        member1.setBookId(null);
        assertThrows(BadRequest.class,
                () -> memberService.update(member1));

        // Invalid bookId field
        final Member member2 = original.clone();
        member2.setBookId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> memberService.update(member2));

        // Missing seriesId field
        final Member member3 = original.clone();
        member3.setSeriesId(null);
        assertThrows(BadRequest.class,
                () -> memberService.update(member3));

        // Invalid seriesId field
        final Member member4 = original.clone();
        member4.setSeriesId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> memberService.update(member4));

    }

    @Test
    public void updateNotUnique() throws Exception {
        // No uniqueness constraints to test
    }

    // Private Methods -------------------------------------------------------

    private List<Member> findAllMembers() {
        return entityManager.createNamedQuery
                (MEMBER_NAME + ".findAll", Member.class)
                .getResultList();
    }

    private Book findFirstBook() {
        TypedQuery<Book> bookQuery = entityManager.createNamedQuery
                (BOOK_NAME + ".findAll", Book.class);
        List<Book> books = bookQuery.getResultList();
        return books.get(0);
    }

    private Member findFirstMember() {
        List<Member> members = findAllMembers();
        return members.get(0);
    }

    private Series findFirstSeries() {
        TypedQuery<Series> seriesQuery = entityManager.createNamedQuery
                (SERIES_NAME + ".findAll", Series.class);
        List<Series> serieses = seriesQuery.getResultList();
        return serieses.get(0);
    }

    private Optional<Member> findMemberById(Long memberId) {
        TypedQuery<Member> query = entityManager.createNamedQuery
                (MEMBER_NAME + ".findById", Member.class)
                .setParameter(ID_COLUMN, memberId);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private Member newMember() {
        return new Member(findFirstBook().getId(), 123, findFirstSeries().getId());
    }

}
