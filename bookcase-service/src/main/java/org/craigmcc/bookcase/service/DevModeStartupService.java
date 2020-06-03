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

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * <p>Development mode startup operations.</p>
 */
@LocalBean
@Startup
@Singleton
public class DevModeStartupService {

    // Instance Variables ----------------------------------------------------

    /**
     * <p>If <code>dev.mode.populate</code> is also set, erase all database data
     * before populating with development mode test data at startup.</p>
     */
    @Inject
    @ConfigProperty(name = "dev.mode.depopulate", defaultValue = "false")
    private boolean devModeDepopulate;

    @Inject
    DevModeDepopulateService devModeDepopulateService;

    /**
     * <p>Populate database with development mode test data at startup.</p>
     */
    @Inject
    @ConfigProperty(name = "dev.mode.populate", defaultValue = "false")
    private boolean devModePopulate;

    @Inject
    DevModePopulateService devModePopulateService;

    // Static Variables ------------------------------------------------------

    private static final Logger LOG =
            Logger.getLogger(DevModeStartupService.class.getSimpleName());

    // Public Methods --------------------------------------------------------

    @PostConstruct
    public void devModeStartup() {
        LOG.info("----- DEVELOPMENT MODE STARTUP OPERATIONS BEGIN -----");
        if (devModeDepopulate && devModePopulate) {
            devModeDepopulateService.depopulate();
        }
        if (devModePopulate) {
            devModePopulateService.populate();
        }
        LOG.info("------ DEVELOPMENT MODE STARTUP OPERATIONS END ------");
    }

}
