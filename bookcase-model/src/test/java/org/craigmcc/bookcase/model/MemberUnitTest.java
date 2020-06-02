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

import static org.craigmcc.bookcase.model.Book.Location.OTHER;
import static org.craigmcc.bookcase.model.Member.OrdinalComparator;
import static org.craigmcc.library.model.Constants.PUBLISHED_COLUMN;
import static org.craigmcc.library.model.Constants.UPDATED_COLUMN;
import static org.craigmcc.library.model.Constants.VERSION_COLUMN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

@Category(UnitTests.class)
public class MemberUnitTest {

    @Test
    public void equalsVerifier() {

        EqualsVerifier.forClass(Member.class)
                .usingGetClass()
                .withIgnoredFields("book", "series",
                        PUBLISHED_COLUMN, UPDATED_COLUMN, VERSION_COLUMN)
                .withPrefabValues(Author.class,
                        new Author("Fred", "Flintstone", "Fred notes"),
                        new Author("Barney", "Rubble", "Barney notes")
                )
                .withPrefabValues(Book.class,
                        new Book(1L, OTHER, null, false, "Title 1"),
                        new Book(2L, OTHER, null, false, "Title 2")
                )
                .withPrefabValues(Series.class,
                        new Series(1L, null, "Dummy Title 1"),
                        new Series(2L, null, "Dummy Title 2")
                )
                .withRedefinedSuperclass()
                .verify();

    }

    @Test
    public void ordinalComparator() {

        Member first = new Member();
        first.setOrdinal(1);
        Member second = new Member();
        second.setOrdinal(2);

        assertThat(OrdinalComparator.compare(first, second), lessThan(0));
        assertThat(OrdinalComparator.compare(first, first), comparesEqualTo(0));
        assertThat(OrdinalComparator.compare(second, second), comparesEqualTo(0));
        assertThat(OrdinalComparator.compare(second, first), greaterThan(0));
        assertThat(OrdinalComparator.compare(first, second), comparesEqualTo(-OrdinalComparator.compare(second, first)));

    }

}
