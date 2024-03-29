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

import org.craigmcc.bookcase.model.Anthology;
import org.craigmcc.bookcase.service.AnthologyService;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.InternalServerError;
import org.craigmcc.library.shared.exception.NotFound;
import org.craigmcc.library.shared.exception.NotUnique;
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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

@ApplicationScoped
@Path("/anthologies")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Anthology Endpoints")
public class AnthologyEndpoints {

    // Instance Variables ----------------------------------------------------

    @Inject
    private AnthologyService anthologyService;

    // Static Variables ------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(AnthologyEndpoints.class.getName());

    // Endpoint Methods ------------------------------------------------------

    @DELETE
    @Path("/{anthologyId}")
    @Operation(description = "Delete anthology by ID.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Anthology.class)),
                    description = "The deleted anthology.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing anthology message.",
                    responseCode = "404"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    @Counted
    public Response delete(
            @Parameter(description = "ID of anthology to delete.")
            @PathParam("anthologyId") Long anthologyId
    ) {
        try {
            Anthology anthology = anthologyService.delete(anthologyId);
            return Response.ok(anthology).build();
        } catch (InternalServerError e) {
            LOG.log(SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (NotFound e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Path("/{anthologyId}")
    @Operation(description = "Find anthology by ID.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Anthology.class)),
                    description = "The found anthology.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing anthology message.",
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
            @Parameter(description = "ID of anthology to find.")
            @PathParam("anthologyId") Long anthologyId
    ) {
        try {
            Anthology anthology = anthologyService.find(anthologyId);
            return Response.ok(anthology).build();
        } catch (InternalServerError e) {
            LOG.log(SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (NotFound e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Operation(description = "Find all anthologies, ordered by title.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Anthology.class)),
                    description = "The found anthologies.",
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
        try {
            return Response.ok(anthologyService.findAll()).build();
        } catch (InternalServerError e) {
            LOG.log(SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Path("/title/{title}")
    @Operation(description = "Find anthologies matching title segment.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Anthology.class)),
                    description = "The found anthologies.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    @Counted
    public Response findByTitle(
            @Parameter(description = "Title matching segment of anthologies to find.")
            @PathParam("title") String title
    ) {
        try {
            return Response.ok(anthologyService.findByTitle(title)).build();
        } catch (InternalServerError e) {
            LOG.log(SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @POST
    @Operation(description = "Insert a new anthology.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Anthology.class)),
                    description = "The inserted anthology.",
                    responseCode = "201"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Bad request message.",
                    responseCode = "400"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Uniqueness conflict message.",
                    responseCode = "409"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    @Counted
    public Response insert(
            @Parameter(
                    description = "Anthology to be inserted.",
                    name = "anthology",
                    schema = @Schema(implementation = Anthology.class)
            )
                    Anthology anthology
    ) {
        try {
            anthology = anthologyService.insert(anthology);
            URI uri = UriBuilder.fromResource(AnthologyEndpoints.class)
                    .path(anthology.getId().toString())
                    .build();
            return Response.created(uri)
                    .entity(anthology)
                    .build();
        } catch (BadRequest e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (InternalServerError e) {
            LOG.log(SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (NotUnique e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @PUT
    @Path("/{anthologyId}")
    @Operation(description = "Update an existing anthology.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Anthology.class)),
                    description = "The updated anthology.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Bad request message.",
                    responseCode = "400"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing anthology message.",
                    responseCode = "404"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Uniqueness conflict message.",
                    responseCode = "409"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    @Counted
    public Response update(
            @Parameter(description = "ID of anthology to update.")
            @PathParam("anthologyId") Long anthologyId,
            @Parameter(
                    description = "Anthology to be updated.",
                    name = "anthology",
                    schema = @Schema(implementation = Anthology.class)
            )
            Anthology anthology
    ) {
        try {
            anthology = anthologyService.update(anthologyId, anthology);
            return Response.ok(anthology).build();
        } catch (BadRequest e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (InternalServerError e) {
            LOG.log(SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (NotFound e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (NotUnique e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

    }

}
