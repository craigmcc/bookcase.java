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
package org.craigmcc.bookcase.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.craigmcc.bookcase.model.Book.TitleComparator;
import static org.craigmcc.library.model.Constants.PUBLISHED_COLUMN;
import static org.craigmcc.library.model.Constants.UPDATED_COLUMN;
import static org.craigmcc.library.model.Constants.VERSION_COLUMN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;

@Category(UnitTests.class)
public class BookUnitTest {

    private Book book = new Book();

    @Test
    public void equalsVerifier() {

        EqualsVerifier.forClass(Book.class)
                .usingGetClass()
                .withIgnoredFields("author", "members", "stories",
                        PUBLISHED_COLUMN, UPDATED_COLUMN, VERSION_COLUMN)
                .withPrefabValues(Author.class,
                        new Author("Fred", "Flintstone", "Fred notes"),
                        new Author("Barney", "Rubble", "Barney notes")
                )
                .withPrefabValues(Member.class,
                        new Member(1L, 1, 2L),
                        new Member(2L, 2, 3L)
                 )
                .withPrefabValues(Story.class,
                        new Story(1L, 2L, 3),
                        new Story(4L, 5L, 6)
                )
                .withRedefinedSuperclass()
                .verify();

    }

    @Test
    public void matchTitleNegative() {

        book.setTitle("Foo Bar");

        assertThat(book.matchTitle("Baz"), not(true));
        assertThat(book.matchTitle("baz"), not(true));
        assertThat(book.matchTitle("BAZ"), not(true));
        assertThat(book.matchTitle(" "), not(true));
        assertThat(book.matchTitle(""), not(true));
        assertThat(book.matchTitle(null), not(true));

    }

    @Test
    public void matchTitlePositive() {

        book.setTitle("Foo Bar");

        // Full string
        assertThat(book.matchTitle("Foo Bar"), is(true));
        assertThat(book.matchTitle("Foo bar"), is(true));
        assertThat(book.matchTitle("foo Bar"), is(true));

        // Prefix
        assertThat(book.matchTitle("Foo"), is(true));
        assertThat(book.matchTitle("FOo"), is(true));
        assertThat(book.matchTitle("foo"), is(true));
        assertThat(book.matchTitle("FOO"), is(true));

        // Suffix
        assertThat(book.matchTitle("Bar"), is(true));
        assertThat(book.matchTitle("BAr"), is(true));
        assertThat(book.matchTitle("bar"), is(true));
        assertThat(book.matchTitle("BAR"), is(true));

        // Middle
        assertThat(book.matchTitle("o B"), is(true));
        assertThat(book.matchTitle("o b"), is(true));
        assertThat(book.matchTitle("O b"), is(true));
        assertThat(book.matchTitle("O B"), is(true));

    }

    @Test
    public void titleComparator() {

        Book first = new Book();
        first.setTitle("Bar");
        Book second = new Book();
        second.setTitle("Foo");

        assertThat(TitleComparator.compare(first, second), lessThan(0));
        assertThat(TitleComparator.compare(first, first), comparesEqualTo(0));
        assertThat(TitleComparator.compare(second, second), comparesEqualTo(0));
        assertThat(TitleComparator.compare(second, first), greaterThan(0));
        assertThat(TitleComparator.compare(first, second),
                comparesEqualTo(-TitleComparator.compare(second, first)));

    }

}
