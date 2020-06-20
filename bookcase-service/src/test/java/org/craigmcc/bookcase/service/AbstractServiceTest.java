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
package org.craigmcc.bookcase.service;

import org.craigmcc.bookcase.event.DeletedModelEvent;
import org.craigmcc.bookcase.event.converter.MutatedModelEventTypeConverter;
import org.craigmcc.bookcase.event.validator.EventTypeValidator;
import org.craigmcc.bookcase.model.Constants;
import org.craigmcc.bookcase.model.converter.BookLocationConverter;
import org.craigmcc.bookcase.model.validator.BookLocationValidator;
import org.craigmcc.library.model.Model;
import org.craigmcc.library.model.ModelService;
import org.craigmcc.library.shared.exception.BadRequest;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * <p>Abstract base class for service test implementations that facilitates decorating
 * an Arquillian <code>JavaArchive</code> with commonly required fixtures.  Under most
 * circumstances, the deployment method in the actual test will only need to perform
 * <code>addClass()</code> for the specific service implementation being tested.</p>
 */
public abstract class AbstractServiceTest {

    // Protected Methods -----------------------------------------------------

    /**
     * <p>Add fixtures commonly required by service test implementations.</p>
     *
     * @param archive The archive to be decorated
     * @param productionPersistence Add reference to production (if true) or test
     *                              (if false) <code>persistence.xml</code> fixture
     */
    protected static void addServiceFixtures(
            JavaArchive archive,
            boolean productionPersistence
    ) {
        archive.addClasses(AbstractServiceTest.class, ModelService.class);
        archive.addClasses
                (DevModeDepopulateService.class, DevModePopulateService.class);
        archive.addPackages(true,
                "org.apache.commons.lang3"
        );
        archive.addPackages(true,
                MutatedModelEventTypeConverter.class.getPackage(), // org.craigmcc.bookcase.event.converter
                EventTypeValidator.class.getPackage(),     // org.craigmcc.bookcase.event.validator
                Constants.class.getPackage(),              // org.craigmcc.bookcase.model
                BookLocationConverter.class.getPackage(),  // org.craigmcc.bookcase.model.converter
                BookLocationValidator.class.getPackage(),  // org.craigmcc.bookcase.model.validator
                DeletedModelEvent.class.getPackage(),      // org.craigmcc.bookcase.service.event
                Model.class.getPackage(),                  // org.craigmcc.library.model
                BadRequest.class.getPackage()              // org.craigmcc.library.shared.exception
        );
        if (productionPersistence) {
            archive.addAsManifestResource
                    ("META-INF/persistence.xml", "persistence.xml");
        } else {
            archive.addAsManifestResource
                    ("test-persistence.xml", "persistence.xml");
        }
        archive.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

}
