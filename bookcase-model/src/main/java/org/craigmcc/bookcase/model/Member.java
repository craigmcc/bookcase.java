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
import static org.craigmcc.bookcase.model.Constants.BOOK_ID_COLUMN;
import static org.craigmcc.bookcase.model.Constants.BOOK_TABLE;
import static org.craigmcc.bookcase.model.Constants.MEMBER_NAME;
import static org.craigmcc.bookcase.model.Constants.MEMBER_TABLE;
import static org.craigmcc.bookcase.model.Constants.ORDINAL_COLUMN;
import static org.craigmcc.bookcase.model.Constants.SERIES_ID_COLUMN;
import static org.craigmcc.bookcase.model.Constants.SERIES_TABLE;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@Entity
@Table(
        indexes = {
                @Index(
                        columnList = BOOK_ID_COLUMN + " ASC",
                        name = "IX_" + MEMBER_TABLE + "_" + BOOK_TABLE
                ),
                @Index(
                        columnList = SERIES_ID_COLUMN + " ASC, " + ORDINAL_COLUMN + " ASC",
                        name = "IX_" + MEMBER_TABLE + "_" + SERIES_TABLE + "_" + ORDINAL_COLUMN
                )
        },
        name = MEMBER_TABLE
)
@Access(AccessType.FIELD)
@Schema(
        description = "Declares membership of a particular book in a particular series.  " +
                      "Referenced book (and its author) will be nested inside.",
        name = MEMBER_NAME
)
@NamedQueries({
        @NamedQuery(
                name = MEMBER_NAME + ".findAll",
                query = "SELECT m FROM " + MEMBER_NAME + " m " +
                        "ORDER BY m." +  SERIES_ID_COLUMN + " ASC, "
                        + "m." + ORDINAL_COLUMN + " ASC"
        ),
        @NamedQuery(
                name = MEMBER_NAME + ".findByBookId",
                query = "SELECT m FROM " + MEMBER_NAME + " m " +
                        "WHERE m." + BOOK_ID_COLUMN + " = :" + BOOK_ID_COLUMN + " " +
                        "ORDER BY m." + ORDINAL_COLUMN + " ASC"
        ),
        @NamedQuery(
                name = MEMBER_NAME + ".findById",
                query = "SELECT m FROM " + MEMBER_NAME + " m " +
                        "WHERE m." + ID_COLUMN + " = :" + ID_COLUMN
        ),
        @NamedQuery(
                name = MEMBER_NAME + ".findBySeriesId",
                query = "SELECT m FROM " + MEMBER_NAME + " m " +
                        "WHERE m." + SERIES_ID_COLUMN + " = :" + SERIES_ID_COLUMN + " " +
                        "ORDER BY m." + ORDINAL_COLUMN + " ASC"
        )
})
public class Member extends Model<Member> implements Constants {

    // Instance Variables ----------------------------------------------------

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            foreignKey = @ForeignKey(
                    name = "fk_" + MEMBER_TABLE + "_" + BOOK_TABLE,
                    value = ConstraintMode.CONSTRAINT
            ),
            insertable = false,
            name = BOOK_ID_COLUMN,
            referencedColumnName = ID_COLUMN,
            updatable = false
    )
    @Schema(description = "Details about the book for this member.")
    private Book book;

    @Column(
            name = BOOK_ID_COLUMN,
            nullable = false
    )
    @NotNull(message = BOOK_ID_VALIDATION_MESSAGE)
    @Schema(description = "ID of the book for this member.")
    private Long bookId;

    @Column(
            name = ORDINAL_COLUMN,
            nullable = true
    )
    @Schema(description = "Sort order (ascending) for books in this series.")
    @JsonInclude(NON_NULL)
    private Integer ordinal = 0;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            foreignKey = @ForeignKey(
                    name = "fk_" + MEMBER_TABLE + "_" + SERIES_TABLE,
                    value = ConstraintMode.CONSTRAINT
            ),
            insertable = false,
            name = SERIES_ID_COLUMN,
            referencedColumnName = ID_COLUMN,
            updatable = false
    )
    @Schema(description = "Details about the series for this member.")
    private Series series;

    @Column(
            name = SERIES_ID_COLUMN,
            nullable = false
    )
    @NotNull(message = SERIES_ID_VALIDATION_MESSAGE)
    @Schema(description = "ID of the series for this member.")
    private Long seriesId;

    // Static Variables ------------------------------------------------------

    public static final Comparator<Member> OrdinalComparator = (o1, o2) ->
            o1.getOrdinal().compareTo(o2.getOrdinal());

    // Constructors ----------------------------------------------------------

    public Member() { }

    public Member(
            Long bookId,
            Integer ordinal,
            Long seriesId
    ) {
        this.bookId = bookId;
        if (ordinal != null) {
            this.ordinal = ordinal;
        }
        this.seriesId = seriesId;
    }

    // Property Methods ------------------------------------------------------

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

/*
    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }
*/

    public Long getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Long seriesId) {
        this.seriesId = seriesId;
    }


// Public Methods --------------------------------------------------------

    @Override
    public void copy(Member that) {
        this.bookId = that.bookId;
        this.ordinal = that.ordinal;
        this.seriesId = that.seriesId;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Member)) {
            return false;
        }
        Member that = (Member) object;
        return new EqualsBuilder()
                .appendSuper(super.equals(object))
                .append(this.bookId, that.bookId)
                .append(this.ordinal, that.ordinal)
                .append(this.seriesId, that.seriesId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.bookId)
                .append(this.ordinal)
                .append(this.seriesId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(BOOK_ID_COLUMN, this.bookId)
                .append(ORDINAL_COLUMN, this.ordinal)
                .append(SERIES_ID_COLUMN, this.seriesId)
                .toString();
    }

}
