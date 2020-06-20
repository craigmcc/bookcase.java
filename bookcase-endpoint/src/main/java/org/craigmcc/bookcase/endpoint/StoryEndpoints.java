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

import org.craigmcc.bookcase.model.Story;
import org.craigmcc.bookcase.service.StoryService;
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
@Path("/stories")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Story Endpoints")
public class StoryEndpoints {

    // Instance Variables ----------------------------------------------------

    @Inject
    private StoryService storyService;

    // Static Variables ------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(StoryEndpoints.class.getName());

    // Endpoint Methods ------------------------------------------------------

    @DELETE
    @Path("/{storyId}")
    @Operation(description = "Delete story by ID.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Story.class)),
                    description = "The deleted story.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing Story message.",
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
            @Parameter(description = "ID of story to delete.")
            @PathParam("storyId") Long storyId
    ) {
        try {
            Story story = storyService.delete(storyId);
            return Response.ok(story).build();
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
    @Path("/{storyId}")
    @Operation(description = "Find story by ID.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Story.class)),
                    description = "The found story.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing story message.",
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
            @Parameter(description = "ID of story to find.")
            @PathParam("storyId") Long storyId
    ) {
        try {
            Story Story = storyService.find(storyId);
            return Response.ok(Story).build();
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
    @Operation(description = "Find all stories, ordered by anthologyId and ordinal.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Story.class)),
                    description = "The found stories.",
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
            return Response.ok(storyService.findAll()).build();
        } catch (InternalServerError e) {
            LOG.log(SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Path("/anthology/{anthologyId}")
    @Operation(description = "Find stories for the specified anthology ID, ordered by ordinal.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Story.class)),
                    description = "The found stories.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    @Counted
    public Response findByAnthologyId(
            @Parameter(description = "Anthology ID of stories to find.")
            @PathParam("anthologyId") Long anthologyId
    ) {
        try {
            return Response.ok(storyService.findByAnthologyId(anthologyId)).build();
        } catch (InternalServerError e) {
            LOG.log(SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @POST
    @Operation(description = "Insert a new story.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Story.class)),
                    description = "The inserted story.",
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
                    description = "Story to be inserted.",
                    name = "story",
                    schema = @Schema(implementation = Story.class)
            )
                    Story story
    ) {
        try {
            story = storyService.insert(story);
            URI uri = UriBuilder.fromResource(StoryEndpoints.class)
                    .path(story.getId().toString())
                    .build();
            return Response.created(uri)
                    .entity(story)
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
    @Operation(description = "Update an existing story.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Story.class)),
                    description = "The updated story.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Bad request message.",
                    responseCode = "400"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing Story message.",
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
            @Parameter(
                    description = "Story to be updated.",
                    name = "story",
                    schema = @Schema(implementation = Story.class)
            )
                    Story story
    ) {
        try {
            story = storyService.update(story);
            return Response.ok(story).build();
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
