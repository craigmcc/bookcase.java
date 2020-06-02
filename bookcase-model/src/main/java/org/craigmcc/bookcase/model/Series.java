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
import static org.craigmcc.bookcase.model.Constants.AUTHOR_ID_COLUMN;
import static org.craigmcc.bookcase.model.Constants.SERIES_NAME;
import static org.craigmcc.bookcase.model.Constants.SERIES_TABLE;
import static org.craigmcc.bookcase.model.Constants.TITLE_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@Entity(name = SERIES_NAME)
@Table(
        indexes = {
                @Index(
                        columnList = TITLE_COLUMN + " ASC",
                        name = "IX_" + SERIES_TABLE + "_" + TITLE_COLUMN
                )
        },
        name = SERIES_TABLE
)
@Access(AccessType.FIELD)
@NamedQueries({
        @NamedQuery(
                name = SERIES_NAME + ".findAll",
                query = "SELECT s FROM " + SERIES_NAME + " s " +
                        "ORDER BY s." + TITLE_COLUMN + " ASC"
        ),
        @NamedQuery(
                name = SERIES_NAME + ".findByAuthorId",
                query = "SELECT s FROM " + SERIES_NAME + " s " +
                        "WHERE s." + AUTHOR_ID_COLUMN + " = :" + AUTHOR_ID_COLUMN + " " +
                        "ORDER BY s." + TITLE_COLUMN + " ASC"
        ),
        @NamedQuery(
                name = SERIES_NAME + ".findById",
                query = "SELECT s FROM " + SERIES_NAME + " s " +
                        "WHERE s." + ID_COLUMN + " = :" + ID_COLUMN
        ),
        @NamedQuery(
                name = SERIES_NAME + ".findByTitle",
                query = "SELECT s FROM " + SERIES_NAME + " s " +
                        "WHERE LOWER(s." + TITLE_COLUMN + ") LIKE LOWER(CONCAT('%',:" + TITLE_COLUMN + ",'%')) " +
                        "ORDER BY s." + TITLE_COLUMN + " ASC"
        )
})
@Schema(
        description = "Name of a collection of books out of the same story line.  " +
                      "Referenced author will be nested inside.",
        name = SERIES_NAME
)
public class Series extends Model<Series> implements Constants {

    // Instance Variables ----------------------------------------------------

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            foreignKey = @ForeignKey(
                    name = "fk_" + SERIES_TABLE + "_" + AUTHOR_TABLE,
                    value = ConstraintMode.CONSTRAINT
            ),
            insertable = false,
            name = AUTHOR_ID_COLUMN,
            referencedColumnName = ID_COLUMN,
            updatable = false
    )
    @Schema(description = "Details of the author of this series.")
    private Author author;

    @Column(
            name = AUTHOR_ID_COLUMN,
            nullable = false
    )
    @NotNull(message = AUTHOR_ID_VALIDATION_MESSAGE)
    @Schema(description = "ID of the author of this series.")
    private Long authorId;

    @OneToMany(
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY,
            mappedBy = SERIES_ID_COLUMN,
            orphanRemoval = true
    )
    @OrderBy(ORDINAL_COLUMN)
    @Schema(hidden = true)
    private List<Member> members;

    @Column(
            name = NOTES_COLUMN,
            nullable = true
    )
    @Schema(description = "Optional notes about this series.")
    @JsonInclude(NON_NULL)
    private String notes;

    @Column(
            name = TITLE_COLUMN,
            nullable = false
    )
    @NotBlank(message = TITLE_VALIDATION_MESSAGE)
    @Schema(description = "Title of this series.")
    private String title;

    // Static Variables ------------------------------------------------------

    public static final Comparator<Series> TitleComparator = (o1, o2) ->
            o1.getTitle().compareTo(o2.getTitle());

    // Constructors ----------------------------------------------------------

    public Series() { }

    public Series(
            Long authorId,
            String notes,
            String title
    ) {
        this.authorId = authorId;
        this.notes = notes;
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

/*
    public List<Member> getMembers() {
        return memberss;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Public Methods --------------------------------------------------------

    @Override
    public void copy(Series that) {
        this.authorId = that.authorId;
        this.notes = that.notes;
        this.title = that.title;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Series)) {
            return false;
        }
        Series that = (Series) object;
        return new EqualsBuilder()
                .appendSuper(super.equals(that))
                .append(this.authorId, that.authorId)
                .append(this.notes, that.notes)
                .append(this.title, that.title)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.authorId)
                .append(this.notes)
                .append(this.title)
                .toHashCode();
    }

    public boolean matchTitle(String title) {
        if ((title == null) || title.isBlank()) {
            return false;
        }
        if (!this.getTitle().toLowerCase().contains(title.toLowerCase())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(AUTHOR_ID_COLUMN, this.authorId)
                .append(NOTES_COLUMN, this.notes)
                .append(TITLE_COLUMN, this.title)
                .toString();
    }

}
