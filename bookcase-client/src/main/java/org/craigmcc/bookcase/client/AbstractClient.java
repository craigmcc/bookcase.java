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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * <p>Abstract base class for JAX-RS client implementations for the
 * Bookcase Application.</p>
 */
public abstract class AbstractClient {

    // Manifest Constants ----------------------------------------------------

    // Response Status Integer Values
    public static final int RESPONSE_BAD_REQUEST = Response.Status.BAD_REQUEST.getStatusCode();
    public static final int RESPONSE_CONFLICT = Response.Status.CONFLICT.getStatusCode();
    public static final int RESPONSE_CREATED = Response.Status.CREATED.getStatusCode();
    public static final int RESPONSE_NOT_FOUND = Response.Status.NOT_FOUND.getStatusCode();
    public static final int RESPONSE_OK = Response.Status.OK.getStatusCode();

    // Static Variables ------------------------------------------------------

    /**
     * <p>Base <code>WebTarget</code> for the REST API of the Bookcase Application.</p>
     */
    private static WebTarget baseTarget = null;

    /**
     * <p>Base URI for the REST API of the Bookcase Application.
     */
    private static URI baseURI = null;

    // TODO - needs to be configurable
    private static String BASE_URI_STRING = "http://localhost:8080/bookcase/api";

    /**
     * <p>JAX-RS <code>Client</code> for interacting with the server.</p>
     */
    private static Client client = null;

    // Protected Methods -----------------------------------------------------

    public synchronized WebTarget getBaseTarget() {
        client = getClient();
        if (baseTarget == null) {
            baseTarget = client.target(getBaseURI());
        }
        return baseTarget;
    }

    public synchronized URI getBaseURI() {
        if (baseURI == null) {
            try {
                baseURI = new URI(BASE_URI_STRING);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid base URI " + BASE_URI_STRING);
            }
        }
        return baseURI;
    }

    public synchronized Client getClient() {
        if (client == null) {
            client = ClientBuilder.newClient();
        }
        return client;
    }

}
