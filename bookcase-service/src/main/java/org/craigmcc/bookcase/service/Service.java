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

import org.craigmcc.bookcase.exception.BadRequest;
import org.craigmcc.bookcase.exception.InternalServerError;
import org.craigmcc.bookcase.exception.NotFound;
import org.craigmcc.bookcase.exception.NotUnique;
import org.craigmcc.library.model.Model;

import javax.persistence.EntityExistsException;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>Abstract base class for persistent storage of {@link Model} objects, and related
 * functional logic.</p>
 */
public abstract class Service<M extends Model> {

    // Public Methods --------------------------------------------------------

    /**
     * <p>Delete the specified {@link Model} object by identifier.
     * This may cause cascading deletes based on object relationships.</p>
     *
     * @param model {@link Model} object to be deleted (only primary key is required)
     *
     * @return The deleted {@link Model} object.
     *
     * @throws InternalServerError If a JPA level error has occurred.
     * @throws NotFound If no object with the specified primary key can be found.
     */
    public abstract @NotNull M delete(@NotNull M model) throws InternalServerError, NotFound;

    /**
     * <p>Retrieve and return the specified {@link Model} object by identifier.</p>
     *
     * @param id Primary key of the specified {@link Model} object.
     *
     * @return The matching {@link Model} object.
     *
     * @throws InternalServerError If a JPA level error has occurred.
     * @throws NotFound If no object with the specified primary key can be found.
     */
    public abstract @NotNull M find(@NotNull Long id) throws InternalServerError, NotFound;

    /**
     * <p>Retrieve and return all {@link Model} objects of the specified type.</p>
     *
     * @return The matching {@Model} objects.
     *
     * @throws InternalServerError If a JPA level error has occurred.
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
     * @throws InternalServerError If a JPA level error has occurred.
     * @throws NotUnique If a JPA {@link EntityExistsException} occurs.
     */
    public abstract @NotNull M insert(@NotNull M model) throws BadRequest, InternalServerError, NotUnique;

    /**
     * <p>Update and return the specified {@link Model} object.</p>
     *
     * @param model The {@link Model} object to be updated.
     *
     * @return The {@link Model} object with <code>updated</code> field updated.
     *
     * @throws BadRequest If a validation error has occurred.
     * @throws InternalServerError If a JPA level error has occurred.
     * @throws NotFound If no object with the specified primary key can be found.
     * @throws NotUnique If a JPA {@link EntityExistsException} occurs.
     */
    public abstract @NotNull M update(@NotNull M model) throws BadRequest, InternalServerError, NotFound, NotUnique;

    // Protected Methods -----------------------------------------------------

    protected String formatMessage(ConstraintViolationException e) {
        StringBuffer sb = new StringBuffer();
        for (ConstraintViolation constraintViolation : e.getConstraintViolations()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(constraintViolation.getMessage());
        }
        return sb.toString();
    }

    protected void handlePersistenceException(@NotNull PersistenceException e) throws BadRequest, InternalServerError {
        if ((e.getCause() != null) && (e.getCause() instanceof ConstraintViolationException)) {
            throw new BadRequest(formatMessage((ConstraintViolationException) e.getCause()));
        } else if (e.getCause() != null) {
            throw new BadRequest("foreignKey: Foreign key or unique key constraint violated");
        } else {
            throw new InternalServerError(e);
        }
    }

}
