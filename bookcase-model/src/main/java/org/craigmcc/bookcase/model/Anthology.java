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
import org.craigmcc.bookcase.model.Book.Location;
import org.craigmcc.library.model.Model;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.craigmcc.bookcase.model.Constants.ANTHOLOGY_NAME;
import static org.craigmcc.bookcase.model.Constants.ANTHOLOGY_TABLE;
import static org.craigmcc.bookcase.model.Constants.AUTHOR_ID_COLUMN;
import static org.craigmcc.bookcase.model.Constants.TITLE_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@Entity(name = ANTHOLOGY_NAME)
@Table(
        indexes = {
                @Index(
                        columnList = TITLE_COLUMN + " ASC",
                        name = "IX_" + ANTHOLOGY_TABLE + "_" + TITLE_COLUMN
                )
        },
        name = ANTHOLOGY_TABLE
)
@Access(AccessType.FIELD)
@NamedQueries({
        @NamedQuery(
                name = ANTHOLOGY_NAME + ".findAll",
                query = "SELECT a FROM " + ANTHOLOGY_NAME + " a " +
                        "ORDER BY a." + TITLE_COLUMN + " ASC"
        ),
        @NamedQuery(
                name = ANTHOLOGY_NAME + ".findByAuthorId",
                query = "SELECT a FROM " + ANTHOLOGY_NAME + " a " +
                        "WHERE a." + AUTHOR_ID_COLUMN + " = :" + AUTHOR_ID_COLUMN + " " +
                        "ORDER BY a." + TITLE_COLUMN + " ASC"
        ),
        @NamedQuery(
                name = ANTHOLOGY_NAME + ".findById",
                query = "SELECT a FROM " + ANTHOLOGY_NAME + " a " +
                        "WHERE a." + ID_COLUMN + " = :" + ID_COLUMN
        ),
        @NamedQuery(
                name = ANTHOLOGY_NAME + ".findByTitle",
                query = "SELECT a FROM " + ANTHOLOGY_NAME + " a " +
                        "WHERE LOWER(a." + TITLE_COLUMN + ") LIKE LOWER(CONCAT('%',:" + TITLE_COLUMN + ",'%')) " +
                        "ORDER BY a." + TITLE_COLUMN + " ASC"
        )
})
@Schema(
        description = "An anthology, which is a collection of books bundled together.",
        name = ANTHOLOGY_NAME
)
public class Anthology extends Model<Anthology> implements Constants {

    // Instance Variables ----------------------------------------------------

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            foreignKey = @ForeignKey(
                    name = "fk_" + ANTHOLOGY_TABLE + "_" + AUTHOR_TABLE,
                    value = ConstraintMode.CONSTRAINT
            ),
            insertable = false,
            name = AUTHOR_ID_COLUMN,
            referencedColumnName = ID_COLUMN,
            updatable = false
    )
    @Schema(description = "Details of the primary author of this anthology.")
    private Author author;

    @Column(
            name = AUTHOR_ID_COLUMN,
            nullable = false
    )
    @NotNull(message = AUTHOR_ID_VALIDATION_MESSAGE)
    @Schema(description = "ID of the primary author (the one Amazon lists first) of this anthology.")
    private Long authorId; // Primary anthology author (i.e. what Amazon lists first)

    @Column(
            name = LOCATION_COLUMN,
            nullable = true
    )
    @Schema(description = "Location where this anthology is stored.  " +
            "By convention, stories in an anthology should be marked as located in an ANTHOLOGY.")
    @JsonInclude(NON_NULL)
    private Book.Location location;

    @Column(
            name = NOTES_COLUMN,
            nullable = true
    )
    @Schema(description = "Optional notes about this anthology.")
    @JsonInclude(NON_NULL)
    private String notes;

    @Column(
            name = READ_COLUMN,
            nullable = true
    )
    @Schema(description = "Has this anthology been read?")
    @JsonInclude(NON_NULL)
    private Boolean read = Boolean.FALSE;

    @OneToMany(
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY,
            mappedBy = ANTHOLOGY_ID_COLUMN,
            orphanRemoval = true
    )
    @OrderBy(ORDINAL_COLUMN)
    @Schema(hidden = true)
    private List<Story> stories;

    @Column(
            name = TITLE_COLUMN,
            nullable = false
    )
    @NotBlank(message = TITLE_VALIDATION_MESSAGE)
    @Schema(description = "Title of this anthology.")
    private String title;

    // Static Variables ------------------------------------------------------

    public static final Comparator<Anthology> TitleComparator = (o1, o2) ->
            o1.getTitle().compareTo(o2.getTitle());

    // Constructors ----------------------------------------------------------

    public Anthology() { }

    public Anthology(
            Long authorId,
            Location location,
            String notes,
            Boolean read,
            String title
    ) {
        this.authorId = authorId;
        this.location = location;
        this.notes = notes;
        if (read != null) {
            this.read = read;
        }
        this.title = title;
    }

    // Property Methods ------------------------------------------------------

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

/*
    public List<Story> getStories() {
        return stories;
    }

    public void setStories(List<Story> stories) {
        this.stories = stories;
    }
*/

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Public Methods --------------------------------------------------------

    @Override
    public void copy(Anthology that) {
        this.authorId = that.authorId;
        this.location = that.location;
        this.notes = that.notes;
        this.read = that.read;
        this.title = that.title;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Anthology)) {
            return false;
        }
        Anthology that = (Anthology) object;
        return new EqualsBuilder()
                .appendSuper(super.equals(that))
                .append(this.authorId, that.authorId)
                .append(this.location, that.location)
                .append(this.notes, that.notes)
                .append(this.read, that.read)
                .append(this.title, that.title)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.authorId)
                .append(this.location)
                .append(this.notes)
                .append(this.read)
                .append(this.title)
                .toHashCode();
    }

    public boolean matchTitle(String title) {
        if ((title == null) || title.isBlank()) {
            return false;
        }
        return this.getTitle().toLowerCase().contains(title.toLowerCase());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(AUTHOR_ID_COLUMN, this.authorId)
                .append(LOCATION_COLUMN, this.location)
                .append(NOTES_COLUMN, this.notes)
                .append(READ_COLUMN, this.read)
                .append(TITLE_COLUMN, this.title)
                .toString();
    }

}
