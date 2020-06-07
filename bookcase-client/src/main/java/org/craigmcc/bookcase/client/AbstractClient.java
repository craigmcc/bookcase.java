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
import java.util.concurrent.TimeUnit;

/**
 * <p>Abstract base class for JAX-RS client implementations for the
 * Bookcase Application.</p>
 */
public abstract class AbstractClient {

    // Manifest Constants ----------------------------------------------------

    /**
     * <p>Default for base URI if not specified.</p>
     */
    public static final String DEFAULT_BASE_URI = "http://localhost:8080/bookcase/api";

    /**
     * <p>System property containing the base URI (including context path and API prefix).
     * If not specified, defaults to DEFAULT_BASE_URI defined above.</p>
     */
    public static final String PROPERTY_BASE_URI = "org.craigmcc.bookcase.client.baseUri";

    /**
     * <p>System property containing the connect timeout in milliseconds.  If not specified,
     * this property is not configured, so the predefined default value will be used.</p>
     */
    public static final String PROPERTY_CONNECT_TIMEOUT = "org.craigmcc.bookcase.client.connectTimeout";

    /**
     * <p>System property containing the read timeout in milliseconds.  If not specified,
     * this property is not configured, so the predefined default value will be used.</p>
     */
    public static final String PROPERTY_READ_TIMEOUT = "org.craigmcc.bookcase.client.readTimeout";

    // Response Status Integer Values
    public static final int RESPONSE_BAD_REQUEST = Response.Status.BAD_REQUEST.getStatusCode();
    public static final int RESPONSE_CONFLICT = Response.Status.CONFLICT.getStatusCode();
    public static final int RESPONSE_CREATED = Response.Status.CREATED.getStatusCode();
    public static final int RESPONSE_FORBIDDEN = Response.Status.FORBIDDEN.getStatusCode();
    public static final int RESPONSE_NO_CONTENT = Response.Status.NO_CONTENT.getStatusCode();
    public static final int RESPONSE_NOT_FOUND = Response.Status.NOT_FOUND.getStatusCode();
    public static final int RESPONSE_OK = Response.Status.OK.getStatusCode();

    // Static Variables ------------------------------------------------------

    /**
     * <p>Base <code>WebTarget</code> for the REST API of the Bookcase Application.</p>
     */
    private static WebTarget baseTarget = null;

    /**
     * <p>JAX-RS <code>Client</code> for interacting with the server.</p>
     */
    private static Client client = null;

    // Protected Methods -----------------------------------------------------

    /**
     * <p>Acquire the {@link WebTarget} for the base URI for the <code>bookcase-endpoint</p>
     * endpoints of the Bookcase Application.</p>
     */
    public synchronized WebTarget getBaseTarget() {
        if (baseTarget == null) {
            URI baseURI;
            try {
                baseURI = new URI(System.getProperty(PROPERTY_BASE_URI, DEFAULT_BASE_URI));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid base URI " +
                        System.getProperty(PROPERTY_BASE_URI, DEFAULT_BASE_URI));
            }
           baseTarget = getClient().target(baseURI);
        }
        return baseTarget;
    }

    /**
     * <p>Acquire the {@link Client} implementation for accessing the <code>bookcase-endpoint</code></p>
     * endpoints of the Bookcase Application.</p>
     */
    public synchronized Client getClient() {
        if (client == null) {
            ClientBuilder clientBuilder = ClientBuilder.newBuilder();
            String value = System.getProperty(PROPERTY_CONNECT_TIMEOUT);
            if (value != null) {
                clientBuilder.connectTimeout(Long.valueOf(value), TimeUnit.MILLISECONDS);
            }
            value = System.getProperty(PROPERTY_READ_TIMEOUT);
            if (value != null) {
                clientBuilder.readTimeout(Long.valueOf(value), TimeUnit.MILLISECONDS);
            }
            client = clientBuilder.build();
        }
        return client;
    }

}
