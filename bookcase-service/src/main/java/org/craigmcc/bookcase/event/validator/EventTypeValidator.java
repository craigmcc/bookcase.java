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
package org.craigmcc.bookcase.event.validator;

import org.craigmcc.bookcase.event.MutatedModelEvent;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class EventTypeValidator<ValidEventType extends Annotation, String>
        implements ConstraintValidator<ValidEventType, String> {

    private static final Set<java.lang.String> validEventTypes =  new HashSet<>();
    static {
        for (MutatedModelEvent.Type type : MutatedModelEvent.Type.values()) {
            validEventTypes.add(type.name());
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return validEventTypes.contains(value);
    }

}
