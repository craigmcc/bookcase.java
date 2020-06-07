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
package org.craigmcc.bookcase.endpoint;

import org.craigmcc.bookcase.service.DevModeDepopulateService;
import org.craigmcc.bookcase.service.DevModePopulateService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * <p>Utilities to enable external clients to remotely trigger depopulation and
 * population events.  <strong>IF ENABLED, THESE METHODS WILL ERASE YOUR DATABASE
 * AND REPLACE ITS CONTENTS WITH TEST DATA</strong>.  As a result of this, they will
 * only execute if the appropriate dev mode configuration values are set to true.
 * These methods are not intended for general purpose use, so they are undocumented.</p>
 */
@ApplicationScoped()
@Path("/devmode")
public class DevModeEndpoints {

    // Instance Variables ----------------------------------------------------

    @Inject
    @ConfigProperty(name = "dev.mode.depopulate", defaultValue = "false")
    private boolean devModeDepopulate;

    @Inject
    private DevModeDepopulateService devModeDepopulateService;

    @Inject
    @ConfigProperty(name = "dev.mode.populate", defaultValue = "false")
    private boolean devModePopulate;

    @Inject
    private DevModePopulateService devModePopulateService;

    // Endpoint Methods ------------------------------------------------------

    @POST
    @Path("/depopulate")
    public Response depopulate() {
        if (devModeDepopulate) {
            devModeDepopulateService.depopulate();
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("devMode: depopulation is disabled")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @POST
    @Path("/populate")
    public Response populate() {
        if (devModePopulate) {
            devModePopulateService.populate();
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("devMode: population is disabled")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

}
