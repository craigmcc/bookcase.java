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
package org.craigmcc.bookcase.client;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(ClientTests.class)
public class AnthologyClientTest extends AbstractClientTest {

    // TODO - need Arquillian and all the test resources from bookcase-service duplicated.
    // TODO - may also need bookcase-service out of test mode.

    // Client Test Methods ---------------------------------------------------

    @Test
    public void testHello() {
        System.out.println("Hello AnthologyService!");
        System.out.println("BASE_URI = " + BASE_URI);
    }

}
