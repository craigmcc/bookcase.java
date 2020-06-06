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

/**
 * <p>Manifest constants for columns and tables.</p>
 */
public interface Constants {

    // Per-Column Constants

    String ANTHOLOGY_ID_COLUMN = "anthologyId";
    String ANTHOLOGY_ID_VALIDATION_MESSAGE =
            "anthologyId: Required and must be a valid reference to an Anthology";

    String AUTHOR_ID_COLUMN = "authorId";
    String AUTHOR_ID_VALIDATION_MESSAGE =
            "authorId: Required and must be a valid reference to an Author";

    String BOOK_ID_COLUMN = "bookId";
    String BOOK_ID_VALIDATION_MESSAGE =
            "bookId: Required and must be a valid reference to a Book";

    String FIRST_NAME_COLUMN = "firstName";
    String FIRST_NAME_VALIDATION_MESSAGE =
            "firstName: Required and must not be blank";

    String GOOGLE_ID = "googleId";

    String LAST_NAME_COLUMN = "lastName";
    String LAST_NAME_VALIDATION_MESSAGE =
            "lastName: Required and must not be blank";

    String LOCATION_COLUMN = "location";

    String NAME_COLUMN = "name"; // Pseudo-column for name search criteria
    String NAME_UNIQUE_VALIDATION_MESSAGE =
            "firstName/lastName: Author firstName plus lastName must be unique";

    String NOTES_COLUMN = "notes";

    String ORDINAL_COLUMN = "ordinal";

    String READ_COLUMN = "read";

    String SERIES_ID_COLUMN = "seriesId";
    String SERIES_ID_VALIDATION_MESSAGE =
            "seriesId: Required and must be a valid reference to a Series";

    String TITLE_COLUMN = "title";
    String TITLE_VALIDATION_MESSAGE =
            "title: Required and must not be blank";

    // Per-Table Constants

    String ANTHOLOGY_NAME = "Anthology";
    String ANTHOLOGY_TABLE = "anthologies";

    String AUTHOR_FIELD = "author";
    String AUTHOR_NAME = "Author";
    String AUTHOR_TABLE = "authors";

    String BOOK_NAME = "Book";
    String BOOK_TABLE = "books";

    String MEMBER_NAME = "Member";
    String MEMBER_TABLE = "members";

    String MUTATED_MODEL_EVENT_TABLE = "mutatedModelEvents";

    String SERIES_NAME = "Series";
    String SERIES_TABLE = "series";  // Singular and plural :-)

    String STORY_NAME = "Story";
    String STORY_TABLE = "stories";

}
