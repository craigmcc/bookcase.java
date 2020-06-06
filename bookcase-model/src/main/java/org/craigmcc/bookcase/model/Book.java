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
import org.craigmcc.bookcase.model.converter.BookLocationConverter;
import org.craigmcc.bookcase.model.validator.ValidBookLocation;
import org.craigmcc.library.model.Model;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.craigmcc.bookcase.model.Constants.AUTHOR_ID_COLUMN;
import static org.craigmcc.bookcase.model.Constants.BOOK_NAME;
import static org.craigmcc.bookcase.model.Constants.BOOK_TABLE;
import static org.craigmcc.bookcase.model.Constants.TITLE_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@Entity(name = BOOK_NAME)
@Table(
        indexes = {
                @Index(
                        columnList = TITLE_COLUMN + " ASC",
                        name = "IX_" + BOOK_TABLE + "_" + TITLE_COLUMN
                )
        },
        name = BOOK_TABLE
)
@Access(AccessType.FIELD)
@NamedQueries({
        @NamedQuery(
                name = BOOK_NAME + ".findAll",
                query = "SELECT b FROM " + BOOK_NAME + " b " +
                        "ORDER BY b." + TITLE_COLUMN + " ASC"
        ),
        @NamedQuery(
                name = BOOK_NAME + ".findByAuthorId",
                query = "SELECT b FROM " + BOOK_NAME + " b " +
                        "WHERE b." + AUTHOR_ID_COLUMN + " = :" + AUTHOR_ID_COLUMN + " " +
                        "ORDER BY b." + TITLE_COLUMN + " ASC"
        ),
        @NamedQuery(
                name = BOOK_NAME + ".findById",
                query = "SELECT b FROM " + BOOK_NAME + " b " +
                        "WHERE b." + ID_COLUMN + "= :" + ID_COLUMN
        ),
        @NamedQuery(
                name = BOOK_NAME + ".findByTitle",
                query = "SELECT b FROM " + BOOK_NAME + " b " +
                        "WHERE LOWER(b." + TITLE_COLUMN + ") LIKE LOWER(CONCAT('%',:" + TITLE_COLUMN + ",'%')) " +
                        "ORDER BY b." + TITLE_COLUMN + " ASC"
        )
})
@Schema(
        description = "An individual book, which may be standalone or part of an anthology.  " +
                      "Referenced author will be nested inside.",
        name = BOOK_NAME
)
public class Book extends Model<Book> implements Constants {

    // Instance Variables ----------------------------------------------------

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            foreignKey = @ForeignKey(
                    name = "fk_" + BOOK_TABLE + "_" + AUTHOR_TABLE,
                    value = ConstraintMode.CONSTRAINT
            ),
            insertable = false,
            name = AUTHOR_ID_COLUMN,
            referencedColumnName = ID_COLUMN,
            updatable = false
    )
    @Schema(description = "Details of the author of this book.")
    private Author author;

    @Column(
            name = AUTHOR_ID_COLUMN,
            nullable = false
    )
    @NotNull(message = AUTHOR_ID_VALIDATION_MESSAGE)
    @Schema(description = "ID of the author of this book.")
    private Long authorId;

    @Column(
            name = GOOGLE_ID,
            nullable = true
    )
    @Schema(description = "Google Books volume identifier for this book")
    private String googleId;

    @Column(
            name = LOCATION_COLUMN,
            nullable = true
    )
    @Convert(converter = BookLocationConverter.class)
    @Schema(description = "Location where this book is stored.")
    @JsonInclude(NON_NULL)
    @ValidBookLocation
    private Location location;

    @OneToMany(
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY,
            mappedBy = BOOK_ID_COLUMN,
            orphanRemoval = true
    )
    @Schema(hidden = true)
    private List<Member> members;

    @Column(
            name = NOTES_COLUMN,
            nullable = true
    )
    @Schema(description = "Optional notes about this book.")
    @JsonInclude(NON_NULL)
    private String notes;

    @Column(
            name = READ_COLUMN,
            nullable = true
    )
    @Schema(description = "Has this book been read?")
    @JsonInclude(NON_NULL)
    private Boolean read = Boolean.FALSE;

    @OneToMany(
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY,
            mappedBy = BOOK_ID_COLUMN,
            orphanRemoval = true
    )
    @Schema(hidden = true)
    private List<Story> stories;

    @Column(
            name = TITLE_COLUMN,
            nullable = false
    )
    @NotBlank(message = TITLE_VALIDATION_MESSAGE)
    @Schema(description = "Title of this book.")
    private String title;

    // Static Variables ------------------------------------------------------

    public static final Comparator<Book> TitleComparator = (o1, o2) ->
            o1.getTitle().compareTo(o2.getTitle());

    // Constructors ----------------------------------------------------------

    public Book() { }

    public Book(
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

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

/*
    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }
*/

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
    public void copy(Book that) {
        this.authorId = that.authorId;
        this.googleId = that.googleId;
        this.location = that.location;
        this.notes = that.notes;
        this.read = that.read;
        this.title = that.title;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Book)) {
            return false;
        }
        Book that = (Book) object;
        return new EqualsBuilder()
                .appendSuper(super.equals(that))
                .append(this.authorId, that.authorId)
                .append(this.googleId, that.googleId)
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
                .append(this.googleId)
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
        if (!this.getTitle().toLowerCase().contains(title.trim().toLowerCase())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(AUTHOR_ID_COLUMN, this.authorId)
                .append(GOOGLE_ID, this.googleId)
                .append(LOCATION_COLUMN, this.location)
                .append(NOTES_COLUMN, this.notes)
                .append(READ_COLUMN, this.read)
                .append(TITLE_COLUMN, this.title)
                .toString();
    }

    /**
     * <p>Where is this book or anthology located in our bookcase?</p>
     */
    @Schema(description = "Denotes location where this book or anthology is physically located.")
    public enum Location {

        @Schema(description = "This book is part of an anthology, not a separate physical object.")
        ANTHOLOGY,

        @Schema(description = "This book or anthology is stored in a physical box.  " +
                "By convention, include a box identifier in the notes.")
        BOX,

        @Schema(description = "This book or anthology is downloaded to the Kindle App (purchased separately).")
        KINDLE,

        @Schema(description = "This book or anthology is downloaded to the Kobo App (purchased separately).")
        KOBO,

        @Schema(description = "This book or anthology was downloaded via Kindle Unlimited, then returned.")
        RETURNED,

        @Schema(description = "This book or anthology has been checked out via Kindle Unlimited.")
        UNLIMITED,

        @Schema(description = "This book or anthology is in some other physical location.")
        OTHER

    }

}
