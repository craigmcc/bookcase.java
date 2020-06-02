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
package org.craigmcc.bookcase.event.converter;

import org.craigmcc.bookcase.event.MutatedModelEvent;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * <p>Attribute Converter for {@link MutatedModelEvent.Type} to String and back.</p>
 */
@Converter(autoApply = true)
public class MutatedModelEventTypeConverter
        implements AttributeConverter<MutatedModelEvent.Type, String> {

    @Override
    public String convertToDatabaseColumn(MutatedModelEvent.Type attribute) {
        return attribute.name();
    }

    @Override
    public MutatedModelEvent.Type convertToEntityAttribute(String dbData) {
        return MutatedModelEvent.Type.valueOf(dbData);
    }

}
