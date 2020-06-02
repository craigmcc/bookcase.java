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

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.craigmcc.library.model.Model;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import java.util.Comparator;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.craigmcc.bookcase.model.Constants.AUTHOR_NAME;
import static org.craigmcc.bookcase.model.Constants.AUTHOR_TABLE;
import static org.craigmcc.bookcase.model.Constants.FIRST_NAME_COLUMN;
import static org.craigmcc.bookcase.model.Constants.LAST_NAME_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@Entity(name = AUTHOR_NAME)
@Table(
        indexes = {
                @Index(
                        columnList = LAST_NAME_COLUMN + " ASC, " + FIRST_NAME_COLUMN + " ASC",
                        name = "IX_" + AUTHOR_TABLE + "_" + LAST_NAME_COLUMN + "_" + FIRST_NAME_COLUMN,
                        unique = true
                )
        },
        name = AUTHOR_TABLE,
        uniqueConstraints = {
                @UniqueConstraint(
                        // TODO - Not creating anything to enforce this in Postgres?
                        columnNames = { LAST_NAME_COLUMN, FIRST_NAME_COLUMN },
                        name = "UK_" + AUTHOR_TABLE + "_" + LAST_NAME_COLUMN + "_" + FIRST_NAME_COLUMN
                )
        }
)
@Access(AccessType.FIELD)
@NamedQueries({
        @NamedQuery(
                name = AUTHOR_NAME + ".findAll",
                query = "SELECT a FROM " + AUTHOR_NAME + " a " +
                        "ORDER BY a." + LAST_NAME_COLUMN + " ASC, a." + FIRST_NAME_COLUMN + " ASC"
        ),
        @NamedQuery(
                name = AUTHOR_NAME + ".findById",
                query = "SELECT a FROM " + AUTHOR_NAME + " a " +
                        "WHERE a." + ID_COLUMN + " = :" + ID_COLUMN
        ),
        @NamedQuery(
                name = AUTHOR_NAME + ".findByName",
                query = "SELECT a FROM " + AUTHOR_NAME + " a " +
                        "WHERE LOWER(a." + FIRST_NAME_COLUMN + ") LIKE LOWER(CONCAT('%',:" + FIRST_NAME_COLUMN + ",'%')) " +
                        "OR LOWER(a." + LAST_NAME_COLUMN + ")  LIKE LOWER(CONCAT('%',:" + LAST_NAME_COLUMN + ",'%')) " +
                        "ORDER BY a." + LAST_NAME_COLUMN + " ASC, a." + FIRST_NAME_COLUMN
        ),
        @NamedQuery(
                name = AUTHOR_NAME + ".findByNameExact",
                query = "SELECT a FROM " + AUTHOR_NAME + " a " +
                        "WHERE a." + FIRST_NAME_COLUMN + " = :" + FIRST_NAME_COLUMN + " " +
                        "AND a." + LAST_NAME_COLUMN + " = :" + LAST_NAME_COLUMN + " " +
                        "ORDER BY a." + LAST_NAME_COLUMN + " ASC, a." + FIRST_NAME_COLUMN
        )
})
@Schema(
        description = "An author of one or more anthologies, books, or series.",
        name = AUTHOR_NAME
)
public class Author extends Model<Author> implements Constants {

    // Instance Variables ----------------------------------------------------

    @OneToMany(
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY,
            mappedBy = AUTHOR_ID_COLUMN,
            orphanRemoval = true
    )
    @OrderBy(TITLE_COLUMN)
    @Schema(hidden = true)
    private List<Anthology> anthologies;

    @OneToMany(
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY,
            mappedBy = AUTHOR_ID_COLUMN,
            orphanRemoval = true
    )
    @OrderBy(TITLE_COLUMN)
    @Schema(hidden = true)
    private List<Book> books;

    @Column(
            name = FIRST_NAME_COLUMN,
            nullable = false
    )
    @NotBlank(message = FIRST_NAME_VALIDATION_MESSAGE)
    @Schema(description = "First name of this author.")
    private String firstName;

    @Column(
            name = LAST_NAME_COLUMN,
            nullable = false
    )
    @NotBlank(message = LAST_NAME_VALIDATION_MESSAGE)
    @Schema(description = "Last name of this author.")
    private String lastName;

    @Column(
            name = NOTES_COLUMN,
            nullable = true
    )
    @Schema(description = "Optional notes about this author.")
    @JsonInclude(NON_NULL)
    private String notes;

    @OneToMany(
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY,
            mappedBy = AUTHOR_ID_COLUMN,
            orphanRemoval = true
    )
    @OrderBy(TITLE_COLUMN)
    @Schema(hidden = true)
    private List<Series> series;

    // Static Variables ------------------------------------------------------

    public static final Comparator<Author> NameComparator = (o1, o2) -> {
        int lastNameComparison = o1.getLastName().compareTo(o2.getLastName());
        if (lastNameComparison != 0) {
            return lastNameComparison;
        } else {
            return o1.getFirstName().compareTo(o2.getFirstName());
        }
    };

    // Constructors ----------------------------------------------------------

    public Author() { }

    public Author(
            String firstName,
            String lastName,
            String notes
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.notes = notes;
    }

    // Property Methods ------------------------------------------------------

/*
    public List<Anthology> getAnthologies() {
        return anthologies;
    }

    public void setAnthologies(List<Anthology> anthologies) {
        this.anthologies = anthologies;
    }
*/

/*
    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }
*/

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

/*
    public List<Series> getSeries() {
        return series;
    }

    public void setSeries(List<Series> series) {
        this.series = series;
    }
*/

    // Public Methods --------------------------------------------------------

    @Override
    public void copy(Author that) {
        this.firstName = that.firstName;
        this.lastName = that.lastName;
        this.notes = that.notes;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Author)) {
            return false;
        }
        Author that = (Author) object;
        return new EqualsBuilder()
                .appendSuper(super.equals(that))
                .append(this.firstName, that.firstName)
                .append(this.lastName, that.lastName)
                .append(this.notes, that.notes)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.firstName)
                .append(this.lastName)
                .append(this.notes)
                .toHashCode();
    }

    /**
     * <p>If <code>names</code> has a space, take the prefix part as the
     * <code>lastName</code> matcher and the suffix part as the <code>firstName</code>
     * matcher.  Otherwise, use the entire string as the matcher for both with an
     * <strong>OR</strong> condition.</p>
     *
     * @param name Matching pattern, optionally with a space to separate last from first
     */
    public boolean matchNames(@NotBlank String name) {
        if ((name == null) || (name.isBlank())) {
            return false;
        }
        String firstName = name.trim();
        String lastName = name.trim();
        int index = name.indexOf(" ");
        if ((index > 0) && (index < name.length() - 1)) {
            firstName = name.substring(0, index).trim();
            lastName = name.substring(index + 1).trim();
        }
        if ((this.firstName.toLowerCase().contains(firstName.toLowerCase())) ||
            (this.lastName.toLowerCase().contains(lastName.toLowerCase()))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(FIRST_NAME_COLUMN, this.firstName)
                .append(LAST_NAME_COLUMN, this.lastName)
                .append(NOTES_COLUMN, this.notes)
                .toString();
    }

}
