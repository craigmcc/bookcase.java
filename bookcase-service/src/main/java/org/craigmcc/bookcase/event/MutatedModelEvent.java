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
package org.craigmcc.bookcase.event;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.craigmcc.bookcase.event.validator.ValidEventType;
import org.craigmcc.library.model.Model;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Comparator;

import static org.craigmcc.bookcase.model.Constants.MUTATED_MODEL_EVENT_TABLE;

@Entity
@Table(
        indexes = {
                @Index(columnList = "updated ASC")
        },
        name = MUTATED_MODEL_EVENT_TABLE
)
@Access(AccessType.FIELD)
@Schema(
        description = "Mutated state event to be persisted (ordered by 'updated' property).",
        name = "MutatedModelEvent"
)
public class MutatedModelEvent extends Model<MutatedModelEvent> {

    // Instance Variables ----------------------------------------------------

    @Column(
            columnDefinition = "TEXT",
            nullable = false
    )
    @Schema(description = "Object whose mutated state is documented by this event.")
    @NotBlank(message = "model: Required and must not be blank")
    private String model;  // TODO - convert to serialized form?

    @Column(
            nullable = false
    )
    @Schema(description = "Type of mutation documented by this event.")
    @NotBlank(message = "type: Required and must not be blank")
    @ValidEventType
    private MutatedModelEvent.Type type;

    // Static Variables ------------------------------------------------------

    public static final Comparator<MutatedModelEvent> UpdatedComparator = (o1, o2) ->
            o1.getUpdated().compareTo(o2.getUpdated());

    // Constructors ----------------------------------------------------------

    public MutatedModelEvent() { }

    public MutatedModelEvent(
            @NotNull Model model,
            @NotNull Type type
    ) {
        this.model = model.toString();  // TODO - serialized format?
        this.type = type;
        setPublished(LocalDateTime.now());
        setUpdated(getPublished());
    }

    // Property Methods ------------------------------------------------------

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    // Public Methods --------------------------------------------------------

    @Override
    public void copy(MutatedModelEvent that) {
        this.model = that.model;
        this.type = that.type;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MutatedModelEvent)) {
            return false;
        }
        MutatedModelEvent that = (MutatedModelEvent) object;
        return new EqualsBuilder()
                .appendSuper(super.equals(that))
                .append(this.model, that.model)
                .append(this.type, that.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.model)
                .append(this.model)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("model", this.model)
                .append("type", this.type)
                .toString();
    }

    // Inner Enums -----------------------------------------------------------

    public enum  Type {
        DELETED,
        INSERTED,
        UPDATED
    }

}
