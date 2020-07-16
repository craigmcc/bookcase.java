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

import org.craigmcc.library.model.Model;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.InternalServerError;
import org.craigmcc.library.shared.exception.NotFound;
import org.craigmcc.library.shared.exception.NotUnique;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * <p>Standard CRUD interface methods for interacting with REST services that
 * implement <code>org.craigmcc.bookcase.service.Service</code>.  Any changes to
 * that public API should be synchronized here.</p>
 *
 * @param <M> Model class for the service this client is interacting with
 */
public abstract class AbstractServiceClient<M extends Model> extends AbstractClient {

    // Manifest Constants ----------------------------------------------------

    // WebTarget path elements (relative to getBaseTarget()) for various model clients
    public static final String ANTHOLOGY_PATH = "/anthologies";
    public static final String AUTHOR_PATH = "/authors";
    public static final String BOOK_PATH = "/books";
    public static final String MEMBER_PATH = "/members";
    public static final String SERIES_PATH = "/series";
    public static final String STORY_PATH = "/stories";

    // Public Methods --------------------------------------------------------

    /**
     * <p>Delete the specified {@link Model} object by identifier.
     * This may cause cascading deletes based on object relationships.</p>
     *
     * @param id Primary key of the specified {@link Model} object.
     *
     * @return The deleted {@link Model} object.
     *
     * @throws InternalServerError If a server level error has occurred.
     * @throws NotFound If no object with the specified primary key can be found.
     */
    public abstract @NotNull M delete(@NotNull Long id) throws InternalServerError, NotFound;

    /**
     * <p>Retrieve and return the specified {@link Model} object by identifier.</p>
     *
     * @param id Primary key of the specified {@link Model} object.
     *
     * @return The matching {@link Model} object.
     *
     * @throws InternalServerError If a server level error has occurred.
     * @throws NotFound If no object with the specified primary key can be found.
     */
    public abstract @NotNull M find(@NotNull Long id) throws InternalServerError, NotFound;

    /**
     * <p>Retrieve and return all {@link Model} objects of the specified type.</p>
     *
     * @return The matching {@Model} objects.
     *
     * @throws InternalServerError If a server level error has occurred.
     */
    public abstract @NotNull List<M> findAll() throws InternalServerError;

    /**
     * <p>Insert and return the specified {@link Model} object.</p>
     *
     * @param model The {@link Model} object to be inserted (any specified PK will be ignored).
     *
     * @return The {@link Model} object with PK and timestamp fields updated.
     *
     * @throws BadRequest If a validation error has occurred.
     * @throws InternalServerError If a server level error has occurred.
     * @throws NotUnique If a uniqueness constraint has been violated.
     */
    public abstract @NotNull M insert(@NotNull M model) throws BadRequest, InternalServerError, NotUnique;

    /**
     * <p>Update and return the specified {@link Model} object.</p>
     *
     * @param id Primary key of the specified {@link Model} object.
     * @param model The {@link Model} object to be updated.
     *
     * @return The {@link Model} object with <code>updated</code> field updated.
     *
     * @throws BadRequest If a validation error has occurred.
     * @throws InternalServerError If a server level error has occurred.
     * @throws NotFound If no object with the specified primary key can be found.
     * @throws NotUnique If a uniqueness constraint has been violated.
     */
    public abstract @NotNull M update(@NotNull Long id, @NotNull M model) throws BadRequest, InternalServerError, NotFound, NotUnique;

}
