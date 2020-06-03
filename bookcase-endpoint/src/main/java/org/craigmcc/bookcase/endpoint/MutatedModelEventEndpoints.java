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

import org.craigmcc.bookcase.event.MutatedModelEvent;
import org.craigmcc.bookcase.exception.NotFound;
import org.craigmcc.bookcase.model.Book;
import org.craigmcc.bookcase.service.MutatedModelEventService;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@ApplicationScoped
@Path("/mutatedModelEvents")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Mutated Model Event Endpoints")
public class MutatedModelEventEndpoints {

    // Instance Variables ----------------------------------------------------

    @Inject
    private MutatedModelEventService mutatedModelEventService;

    // Endpoint Methods ------------------------------------------------------

    @GET
    @Path("/{mutatedModelEventId}")
    @Operation(description = "Find mutated model event by id.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = MutatedModelEvent.class)),
                    description = "The found mutated model event.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing mutated model event message.",
                    responseCode = "404"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    @Counted
    public Response find(
            @Parameter(description = "ID of mutated model event to find.")
            @PathParam("mutatedModelEventId") Long mutatedModelEventId
    ) {
        try {
            MutatedModelEvent mutatedModelEvent = mutatedModelEventService.find(mutatedModelEventId);
            return Response.ok(mutatedModelEvent).build();
        } catch (NotFound e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Operation(description = "Find all mutated model events, ordered by updated timestamp.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Book.class)),
                    description = "The found mutated model events.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    @Counted
    public Response findAll() {
        return Response.ok(mutatedModelEventService.findAll()).build();
    }

}
