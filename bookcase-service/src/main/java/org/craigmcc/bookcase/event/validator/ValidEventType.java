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

import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>Validator annotation for the <code>type</code> field on {@link MutatedModelEvent} objects.</p>
 */
@Target({ FIELD, METHOD })
@Retention(RUNTIME)
public @interface ValidEventType {
    Class<?>[] groups() default {};
    String message() default "type: Invalid event type";
    Class<? extends Payload>[] payload() default {};
}
