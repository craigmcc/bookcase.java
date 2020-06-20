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

import org.craigmcc.bookcase.model.Member;
import org.craigmcc.bookcase.service.MemberService;
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
@Path("/members")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Member Endpoints")
public class MemberEndpoints {

    // Instance Variables ----------------------------------------------------

    @Inject
    private MemberService memberService;

    // Static Variables ------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(MemberEndpoints.class.getName());

    // Endpoint Methods ------------------------------------------------------

    @DELETE
    @Path("/{memberId}")
    @Operation(description = "Delete member by ID.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Member.class)),
                    description = "The deleted member.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing member message.",
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
            @Parameter(description = "ID of member to delete.")
            @PathParam("memberId") Long memberId
    ) {
        try {
            Member member = memberService.delete(memberId);
            return Response.ok(member).build();
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
    @Path("/{memberId}")
    @Operation(description = "Find member by ID.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Member.class)),
                    description = "The found member.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing member message.",
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
            @Parameter(description = "ID of member to find.")
            @PathParam("memberId") Long memberId
    ) {
        try {
            Member member = memberService.find(memberId);
            return Response.ok(member).build();
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
    @Operation(description = "Find all Members, ordered by seriesId and ordinal.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Member.class)),
                    description = "The found members.",
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
            return Response.ok(memberService.findAll()).build();
        } catch (InternalServerError e) {
            LOG.log(SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Path("/series/{seriesId}")
    @Operation(description = "Find members for the specified series ID, ordered by ordinal.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Member.class)),
                    description = "The found members.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    @Counted
    public Response findBySeriesId(
            @Parameter(description = "Series ID of members to find.")
            @PathParam("seriesId") Long seriesId
    ) {
        try {
            return Response.ok(memberService.findBySeriesId(seriesId)).build();
        } catch (InternalServerError e) {
            LOG.log(SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @POST
    @Operation(description = "Insert a new member.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Member.class)),
                    description = "The inserted Member.",
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
                    description = "Member to be inserted.",
                    name = "member",
                    schema = @Schema(implementation = Member.class)
            )
                    Member member
    ) {
        try {
            member = memberService.insert(member);
            URI uri = UriBuilder.fromResource(MemberEndpoints.class)
                    .path(member.getId().toString())
                    .build();
            return Response.created(uri)
                    .entity(member)
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
    @Operation(description = "Update an existing member.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(implementation = Member.class)),
                    description = "The updated member.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Bad request message.",
                    responseCode = "400"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing member message.",
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
                    description = "Member to be updated.",
                    name = "member",
                    schema = @Schema(implementation = Member.class)
            )
                    Member member
    ) {
        try {
            member = memberService.update(member);
            return Response.ok(member).build();
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
