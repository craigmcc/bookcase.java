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
import org.craigmcc.bookcase.model.Author;

import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

public class AuthorClient extends AbstractServiceClient<Author> {

    // Instance Variables ----------------------------------------------------

    private final WebTarget authorTarget = getBaseTarget()
            .path(AUTHOR_PATH);

    // Public Methods --------------------------------------------------------

    @Override
    public Author delete(@NotNull Long authorId) throws InternalServerError, NotFound {

        Response response = authorTarget
                .path(authorId.toString())
                .request(MediaType.APPLICATION_JSON)
                .delete();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Author.class);
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public Author find(@NotNull Long authorId) throws InternalServerError, NotFound {

        Response response = authorTarget
                .path(authorId.toString())
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Author.class);
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull List<Author> findAll() throws InternalServerError {

        Response response = authorTarget
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(new GenericType<List<Author>>() {});
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    public @NotNull List<Author> findByName(@NotNull String name) throws InternalServerError {

        Response response = authorTarget
                .path("name")
                .path(name)
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(new GenericType<List<Author>>() {});
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public Author insert(@NotNull Author author) throws BadRequest, InternalServerError, NotUnique {

        Response response = authorTarget
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(author, MediaType.APPLICATION_JSON));
        if (response.getStatus() == RESPONSE_CREATED) {
            return response.readEntity(Author.class);
        } else if (response.getStatus() == RESPONSE_BAD_REQUEST) {
            throw new BadRequest(response.readEntity(String.class));
        } else if (response.getStatus() == RESPONSE_CONFLICT) {
            throw new NotUnique(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public Author update(@NotNull Author author) throws BadRequest, InternalServerError, NotFound, NotUnique {

        Response response = authorTarget
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(author, MediaType.APPLICATION_JSON));
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Author.class);
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
