/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.fhir2.validation.rest.client;

import java.net.URI;
import java.time.Instant;

import javax.ws.rs.core.Response;

/**
 * This interface represents a response received from a OpenMRS FHIR REST API invocation.
 */
public interface FHIRResponse {

    /**
     * This method is used to retrieve the numeric HTTP status code associated with 
     * a FHIR REST API response.
     * @return the HTTP status code
     */
    int getStatus();
       
    /**
     * This method is used to retrieve the value of the Location response HTTP header as a String.
     * @return the Location response header value as a String
     * @throws Exception
     */
    String getLocation() throws Exception;
    
    /**
     * This method is used to retrieve the value of the Location response HTTP header as a URI.
     * @return the Location response header value as a URI
     * @throws Exception
     */
    URI getLocationURI() throws Exception;

    /**
     * This method is used to retrieve the Last-Modified response HTTP header.
     * @return
     * @throws Exception
     */
    Instant getLastModified() throws Exception;

    /**
     * This method is used to retrieve the resource returned in a FHIR REST API response.
     * @param type this should be acode value which indicates the
     * type of return value expected
     * @return
     * @throws Exception
     */
    <T> T getResource(Class<T> type) throws Exception;

        /**
     * Returns whether the response contains a FHIR Resource entity.
     * @return true if the response body is empty, otherwise false 
     */
    boolean isEmpty();

    
}
