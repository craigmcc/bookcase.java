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
package org.craigmcc.bookcase.model.converter;

import org.craigmcc.bookcase.model.Book;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * <p>Attribute converter for {!link Book.Location} to String and back.</p>
 */
@Converter(autoApply = true)
public class BookLocationConverter
        implements AttributeConverter<Book.Location, String> {

    @Override
    public String convertToDatabaseColumn(Book.Location attribute) {
        if (attribute != null) {
            return attribute.name();
        } else {
            return null;
        }
    }

    @Override
    public Book.Location convertToEntityAttribute(String dbData) {
        if (dbData != null) {
            return Book.Location.valueOf(dbData);
        } else {
            return null;
        }
    }

}

