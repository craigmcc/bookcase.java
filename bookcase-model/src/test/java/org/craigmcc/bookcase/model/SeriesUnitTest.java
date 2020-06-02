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

import static org.craigmcc.bookcase.model.Series.TitleComparator;
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
public class SeriesUnitTest {

    private Series series = new Series();

    @Test
    public void equalsVerifier() {

        EqualsVerifier.forClass(Series.class)
                .usingGetClass()
                .withIgnoredFields("author", "members",
                        PUBLISHED_COLUMN, UPDATED_COLUMN, VERSION_COLUMN)
                .withPrefabValues(Author.class,
                        new Author("Fred", "Flintstone", "Fred notes"),
                        new Author("Barney", "Rubble", "Barney notes")
                )
                .withPrefabValues(Member.class,
                        new Member(1L, 1, 1L),
                        new Member(1L, 2, 2L)
                )
                .withRedefinedSuperclass()
                .verify();

    }

    @Test
    public void matchTitleNegative() {

        series.setTitle("Foo Bar");

        assertThat(series.matchTitle("Baz"), not(true));
        assertThat(series.matchTitle("baz"), not(true));
        assertThat(series.matchTitle("BAZ"), not(true));
        assertThat(series.matchTitle(" "), not(true));
        assertThat(series.matchTitle(""), not(true));
        assertThat(series.matchTitle(null), not(true));

    }

    @Test
    public void matchTitlePositive() {

        series.setTitle("Foo Bar");

        // Full string
        assertThat(series.matchTitle("Foo Bar"), is(true));
        assertThat(series.matchTitle("Foo bar"), is(true));
        assertThat(series.matchTitle("foo Bar"), is(true));

        // Prefix
        assertThat(series.matchTitle("Foo"), is(true));
        assertThat(series.matchTitle("FOo"), is(true));
        assertThat(series.matchTitle("foo"), is(true));
        assertThat(series.matchTitle("FOO"), is(true));

        // Suffix
        assertThat(series.matchTitle("Bar"), is(true));
        assertThat(series.matchTitle("BAr"), is(true));
        assertThat(series.matchTitle("bar"), is(true));
        assertThat(series.matchTitle("BAR"), is(true));

        // Middle
        assertThat(series.matchTitle("o B"), is(true));
        assertThat(series.matchTitle("o b"), is(true));
        assertThat(series.matchTitle("O b"), is(true));
        assertThat(series.matchTitle("O B"), is(true));

    }

    @Test
    public void titleComparator() {

        Series first = new Series();
        first.setTitle("Bar");
        Series second = new Series();
        second.setTitle("Foo");

        assertThat(TitleComparator.compare(first, second), lessThan(0));
        assertThat(TitleComparator.compare(first, first), comparesEqualTo(0));
        assertThat(TitleComparator.compare(second, second), comparesEqualTo(0));
        assertThat(TitleComparator.compare(second, first), greaterThan(0));
        assertThat(TitleComparator.compare(first, second),
                comparesEqualTo(-TitleComparator.compare(second, first)));
    }

}
