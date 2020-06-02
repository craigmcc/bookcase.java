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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.craigmcc.bookcase.model.Author.NameComparator;
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
public class AuthorUnitTest {

    private Author author = new Author();

    @Test
    public void equalsVerifier() {

        EqualsVerifier.forClass(Author.class)
                .usingGetClass()
                .withIgnoredFields("anthologies", "books", "series",
                        PUBLISHED_COLUMN, UPDATED_COLUMN, VERSION_COLUMN)
                .withPrefabValues(Anthology.class,
                        new Anthology(1L, Book.Location.KINDLE, null, FALSE, "First Anthology"),
                        new Anthology(1L, Book.Location.KINDLE, null, FALSE, "Second Anthology")
                )
                .withPrefabValues(Book.class,
                        new Book(1L, Book.Location.KOBO, null, TRUE, "First Book"),
                        new Book(1L, Book.Location.KOBO, null, TRUE, "Second Book")
                )
                .withPrefabValues(Series.class,
                        new Series(1L, null, "First Series"),
                        new Series(1L, null, "Second Series")
                )
                .withRedefinedSuperclass()
                .verify();

    }

    @Test
    public void matchNamesNegative() {

        author.setFirstName("Foo");
        author.setLastName("Bar");

        assertThat(author.matchNames(null), not(true));
        assertThat(author.matchNames(""), not(true));

        assertThat(author.matchNames("FooBar"), not(true));
        assertThat(author.matchNames("fooBar"), not(true));
        assertThat(author.matchNames("Foobar"), not(true));
        assertThat(author.matchNames("foobar"), not(true));

        assertThat(author.matchNames("Baz"), not(true));
        assertThat(author.matchNames("baz"), not(true));
        assertThat(author.matchNames("Bop"), not(true));
        assertThat(author.matchNames("bop"), not(true));

        assertThat(author.matchNames("Baz Bop"), not(true));
        assertThat(author.matchNames("baz Bop"), not(true));
        assertThat(author.matchNames("Baz bop"), not(true));
        assertThat(author.matchNames("baz bop"), not(true));

    }

    @Test
    public void matchNamesPositive() {

        author.setFirstName("Foo");
        author.setLastName("Bar");

        assertThat(author.matchNames("Fo"), is(true));
        assertThat(author.matchNames("fo"), is(true));
        assertThat(author.matchNames("AR"), is(true));
        assertThat(author.matchNames("ar"), is(true));

        assertThat(author.matchNames("F B"), is(true));
        assertThat(author.matchNames("F b"), is(true));
        assertThat(author.matchNames("f B"), is(true));
        assertThat(author.matchNames("f b"), is(true));

        assertThat(author.matchNames("O"), is(true));
        assertThat(author.matchNames("o"), is(true));
        assertThat(author.matchNames("A"), is(true));
        assertThat(author.matchNames("a"), is(true));

    }

    @Test
    public void nameComparator() {

        Author first = new Author();
        first.setFirstName("Foo");
        first.setLastName("Bar");
        Author second = new Author();
        second.setFirstName("Baz");
        second.setLastName("Bar");
        Author third = new Author();
        third.setFirstName("Bop");
        third.setLastName("Foo");

        assertThat(NameComparator.compare(first, first), comparesEqualTo(0));
        assertThat(NameComparator.compare(second, second), comparesEqualTo(0));
        assertThat(NameComparator.compare(third, third), comparesEqualTo(0));

        assertThat(NameComparator.compare(first, second), greaterThan(0));
        assertThat(NameComparator.compare(second, first), lessThan(0));
        assertThat(NameComparator.compare(first, second), comparesEqualTo(-NameComparator.compare(second, first)));

        assertThat(NameComparator.compare(first, third), lessThan(0));
        assertThat(NameComparator.compare(third, first), greaterThan(0));
        assertThat(NameComparator.compare(first, third), comparesEqualTo(-NameComparator.compare(third, first)));

        assertThat(NameComparator.compare(second, third), lessThan(0));
        assertThat(NameComparator.compare(third, second), greaterThan(0));
        assertThat(NameComparator.compare(second, third), comparesEqualTo(-NameComparator.compare(third, second)));

    }

}
