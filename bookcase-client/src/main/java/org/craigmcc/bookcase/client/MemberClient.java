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

import org.craigmcc.bookcase.exception.BadRequest;
import org.craigmcc.bookcase.exception.InternalServerError;
import org.craigmcc.bookcase.exception.NotFound;
import org.craigmcc.bookcase.exception.NotUnique;
import org.craigmcc.bookcase.model.Member;

import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

public class MemberClient extends AbstractServiceClient<Member> {

    // Instance Variables ----------------------------------------------------

    private final WebTarget memberTarget = getBaseTarget()
            .path(MEMBER_PATH);

    // Public Methods --------------------------------------------------------

    @Override
    public Member delete(@NotNull Long memberId) throws InternalServerError, NotFound {

        Response response = memberTarget
                .path(memberId.toString())
                .request(MediaType.APPLICATION_JSON)
                .delete();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Member.class);
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public Member find(@NotNull Long memberId) throws InternalServerError, NotFound {

        Response response = memberTarget
                .path(memberId.toString())
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Member.class);
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull List<Member> findAll() throws InternalServerError {

        Response response = memberTarget
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(new GenericType<List<Member>>() {});
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    public @NotNull List<Member> findBySeriesId(@NotNull Long seriesId) throws InternalServerError {

        Response response = memberTarget
                .path("series")
                .path(seriesId.toString())
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(new GenericType<List<Member>>() {});
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public Member insert(@NotNull Member member) throws BadRequest, InternalServerError, NotUnique {

        Response response = memberTarget
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(member, MediaType.APPLICATION_JSON));
        if (response.getStatus() == RESPONSE_CREATED) {
            return response.readEntity(Member.class);
        } else if (response.getStatus() == RESPONSE_BAD_REQUEST) {
            throw new BadRequest(response.readEntity(String.class));
        } else if (response.getStatus() == RESPONSE_CONFLICT) {
            throw new NotUnique(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public Member update(@NotNull Member member) throws BadRequest, InternalServerError, NotFound, NotUnique {

        Response response = memberTarget
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(member, MediaType.APPLICATION_JSON));
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Member.class);
        } else if (response.getStatus() == RESPONSE_BAD_REQUEST) {
            throw new BadRequest(response.readEntity(String.class));
        } else if (response.getStatus() == RESPONSE_CONFLICT) {
            throw new NotUnique(response.readEntity(String.class));
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

}
