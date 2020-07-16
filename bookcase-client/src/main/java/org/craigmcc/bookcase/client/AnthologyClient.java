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

import org.craigmcc.bookcase.model.Anthology;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.InternalServerError;
import org.craigmcc.library.shared.exception.NotFound;
import org.craigmcc.library.shared.exception.NotUnique;

import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

public class AnthologyClient extends AbstractServiceClient<Anthology> {

    // Instance Variables ----------------------------------------------------

    private final WebTarget anthologyTarget = getBaseTarget()
            .path(ANTHOLOGY_PATH);

    // Public Methods --------------------------------------------------------

    @Override
    public @NotNull Anthology delete(@NotNull Long anthologyId) throws InternalServerError, NotFound {

        Response response = anthologyTarget
                .path(anthologyId.toString())
                .request(MediaType.APPLICATION_JSON)
                .delete();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Anthology.class);
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull Anthology find(@NotNull Long anthologyId) throws InternalServerError, NotFound {


        Response response = anthologyTarget
                .path(anthologyId.toString())
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Anthology.class);
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull List<Anthology> findAll() throws InternalServerError {

        Response response = anthologyTarget
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(new GenericType<List<Anthology>>() {});
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    /**
     * <p>Return a list of {@link Anthology} objects matching the specified title
     * segment, ordered by title.</p>
     *
     * @param title The title segment to be matched
     */
    public @NotNull List<Anthology> findByTitle(@NotNull String title) throws InternalServerError {

        Response response = anthologyTarget
                .path("title")
                .path(title)
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(new GenericType<List<Anthology>>() {});
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull Anthology insert(@NotNull Anthology anthology) throws BadRequest, InternalServerError, NotUnique {

        Response response = anthologyTarget
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(anthology, MediaType.APPLICATION_JSON));
        if (response.getStatus() == RESPONSE_CREATED) {
            return response.readEntity(Anthology.class);
        } else if (response.getStatus() == RESPONSE_BAD_REQUEST) {
            throw new BadRequest(response.readEntity(String.class));
        } else if (response.getStatus() == RESPONSE_CONFLICT) {
            throw new NotUnique(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull Anthology update(@NotNull Long anthologyId, @NotNull Anthology anthology) throws BadRequest, InternalServerError, NotFound, NotUnique {

        Response response = anthologyTarget
                .path(anthologyId.toString())
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(anthology, MediaType.APPLICATION_JSON));
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Anthology.class);
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
