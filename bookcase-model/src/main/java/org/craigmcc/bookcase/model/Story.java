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
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Comparator;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.craigmcc.bookcase.model.Constants.ANTHOLOGY_ID_COLUMN;
import static org.craigmcc.bookcase.model.Constants.BOOK_ID_COLUMN;
import static org.craigmcc.bookcase.model.Constants.BOOK_TABLE;
import static org.craigmcc.bookcase.model.Constants.ORDINAL_COLUMN;
import static org.craigmcc.bookcase.model.Constants.STORY_NAME;
import static org.craigmcc.bookcase.model.Constants.STORY_TABLE;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@Entity
@Table(
        indexes = {
                @Index(
                        columnList = BOOK_ID_COLUMN + " ASC",
                        name = "IX_" + STORY_TABLE + "_" + BOOK_TABLE
                ),
                @Index(
                        columnList = ANTHOLOGY_ID_COLUMN + " ASC, " + ORDINAL_COLUMN + " ASC",
                        name = "IX_" + STORY_TABLE + "_" + ANTHOLOGY_ID_COLUMN + "_" + ORDINAL_COLUMN
                )
        },
        name = STORY_TABLE
)
@Access(AccessType.FIELD)
@Schema(
        description = "Declares inclusion of a particular book in a particular anthology.  " +
        "Referenced book (and its author) will be nested inside.",
        name = STORY_NAME
)
@NamedQueries({
        @NamedQuery(
                name = STORY_NAME + ".findAll",
                query = "SELECT s FROM " + STORY_NAME + " s " +
                        "ORDER BY s." +  ANTHOLOGY_ID_COLUMN + " ASC, "
                        + "s." + ORDINAL_COLUMN + " ASC"
        ),
        @NamedQuery(
                name = STORY_NAME + ".findByAnthologyId",
                query = "SELECT s FROM " + STORY_NAME + " s " +
                        "WHERE s." + ANTHOLOGY_ID_COLUMN + " = :" + ANTHOLOGY_ID_COLUMN + " " +
                        "ORDER BY s." + ORDINAL_COLUMN + " ASC"
        ),
        @NamedQuery(
                name = STORY_NAME + ".findByBookId",
                query = "SELECT s FROM " + STORY_NAME + " s " +
                        "WHERE s." + BOOK_ID_COLUMN + " = :" + BOOK_ID_COLUMN + " " +
                        "ORDER BY s." + ORDINAL_COLUMN + " ASC"
        ),
        @NamedQuery(
                name = STORY_NAME + ".findById",
                query = "SELECT s FROM " + STORY_NAME + " s " +
                        "WHERE s." + ID_COLUMN + " = :" + ID_COLUMN
        )
})
public class Story extends Model<Story> implements Constants {

    // Instance Variables ----------------------------------------------------

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            foreignKey = @ForeignKey(
                    name = "fk_" + STORY_TABLE + "_" + ANTHOLOGY_TABLE,
                    value = ConstraintMode.CONSTRAINT
            ),
            insertable = false,
            name = ANTHOLOGY_ID_COLUMN,
            referencedColumnName = ID_COLUMN,
            updatable = false
    )
    @Schema(description = "Details about the anthology for this story.")
    private Anthology anthology;

    @Column(
            name = ANTHOLOGY_ID_COLUMN,
            nullable = false
    )
    @NotNull(message = ANTHOLOGY_ID_VALIDATION_MESSAGE)
    @Schema(description = "ID of the anthology for this story.")
    private Long anthologyId;

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            foreignKey = @ForeignKey(
                    name = "fk_" + STORY_TABLE + "_" + BOOK_TABLE,
                    value = ConstraintMode.CONSTRAINT
            ),
            insertable = false,
            name = BOOK_ID_COLUMN,
            referencedColumnName = ID_COLUMN,
            updatable = false
    )
    @Schema(description = "Details about the book for this story")
    private Book book;

    @Column(
            name = BOOK_ID_COLUMN,
            nullable = false
    )
    @NotNull(message = BOOK_ID_VALIDATION_MESSAGE)
    @Schema(description = "ID of the book for this story.")
    private Long bookId;

    @Column(
            name = ORDINAL_COLUMN,
            nullable = true
    )
    @Schema(description = "Sort order (ascending) for books in this anthology.")
    @JsonInclude(NON_NULL)
    private Integer ordinal = 0;

    // Static Variables ------------------------------------------------------

    public static final Comparator<Story> OrdinalComparator = (o1, o2) ->
            o1.getOrdinal().compareTo(o2.getOrdinal());

    // Constructors ----------------------------------------------------------

    public Story() { }

    public Story(
            Long anthologyId,
            Long bookId,
            Integer ordinal
    ) {
        this.anthologyId = anthologyId;
        this.bookId = bookId;
        if (ordinal != null) {
            this.ordinal = ordinal;
        }
    }

    // Property Methods ------------------------------------------------------

/*
    public Anthology getAnthology() {
        return anthology;
    }

    public void setAnthology(Anthology anthology) {
        this.anthology = anthology;
    }
*/

    public Long getAnthologyId() {
        return anthologyId;
    }

    public void setAnthologyId(Long anthologyId) {
        this.anthologyId = anthologyId;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }


    // Public Methods --------------------------------------------------------

    @Override
    public void copy(Story that) {
        this.anthologyId = that.anthologyId;
        this.bookId = that.bookId;
        this.ordinal = that.ordinal;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Story)) {
            return false;
        }
        Story that = (Story) object;
        return new EqualsBuilder()
                .appendSuper(super.equals(object))
                .append(this.anthologyId, that.anthologyId)
                .append(this.bookId, that.bookId)
                .append(this.ordinal, that.ordinal)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.anthologyId)
                .append(this.bookId)
                .append(this.ordinal)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(ANTHOLOGY_ID_COLUMN, this.anthologyId)
                .append(BOOK_ID_COLUMN, this.bookId)
                .append(ORDINAL_COLUMN, this.ordinal)
                .toString();
    }

}
