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

import static org.craigmcc.bookcase.model.Anthology.TitleComparator;
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
public class AnthologyUnitTest {

    private Anthology anthology = new Anthology();

    @Test
    public void equalsVerifier() {

        EqualsVerifier.forClass(Anthology.class)
                .usingGetClass()
                .withIgnoredFields("author", "stories",
                        PUBLISHED_COLUMN, UPDATED_COLUMN, VERSION_COLUMN)
                .withPrefabValues(Author.class,
                        new Author("Fred", "Flintstone", "Fred notes"),
                        new Author("Barney", "Rubble", "Barney notes")
                )
                .withPrefabValues(Story.class,
                        new Story(1L, 1L, 1),
                        new Story(1L, 2L, 2)
                )
                .withRedefinedSuperclass()
                .verify();

    }

    @Test
    public void matchTitleNegative() {

        anthology.setTitle("Foo Bar");

        assertThat(anthology.matchTitle("Baz"), not(true));
        assertThat(anthology.matchTitle("baz"), not(true));
        assertThat(anthology.matchTitle("BAZ"), not(true));
        assertThat(anthology.matchTitle(" "), not(true));
        assertThat(anthology.matchTitle(""), not(true));
        assertThat(anthology.matchTitle(null), not(true));

    }

    @Test
    public void matchTitlePositive() {

        anthology.setTitle("Foo Bar");

        // Full string
        assertThat(anthology.matchTitle("Foo Bar"), is(true));
        assertThat(anthology.matchTitle("Foo bar"), is(true));
        assertThat(anthology.matchTitle("foo Bar"), is(true));

        // Prefix
        assertThat(anthology.matchTitle("Foo"), is(true));
        assertThat(anthology.matchTitle("FOo"), is(true));
        assertThat(anthology.matchTitle("foo"), is(true));
        assertThat(anthology.matchTitle("FOO"), is(true));

        // Suffix
        assertThat(anthology.matchTitle("Bar"), is(true));
        assertThat(anthology.matchTitle("BAr"), is(true));
        assertThat(anthology.matchTitle("bar"), is(true));
        assertThat(anthology.matchTitle("BAR"), is(true));

        // Middle
        assertThat(anthology.matchTitle("o B"), is(true));
        assertThat(anthology.matchTitle("o b"), is(true));
        assertThat(anthology.matchTitle("O b"), is(true));
        assertThat(anthology.matchTitle("O B"), is(true));

    }

    @Test
    public void titleComparator() {

        Anthology first = new Anthology();
        first.setTitle("Bar");
        Anthology second = new Anthology();
        second.setTitle("Foo");

        assertThat(TitleComparator.compare(first, second), lessThan(0));
        assertThat(TitleComparator.compare(first, first), comparesEqualTo(0));
        assertThat(TitleComparator.compare(second, second), comparesEqualTo(0));
        assertThat(TitleComparator.compare(second, first), greaterThan(0));
        assertThat(TitleComparator.compare(first, second),
                comparesEqualTo(-TitleComparator.compare(second, first)));

    }

}
