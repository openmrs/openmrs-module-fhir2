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

import com.ibm.fhir.model.resource.Bundle;
/**
 * This interface provides a client API for invoking the FHIR Server's.
 *  Note that in this case openmrs instance is our fhir server REST API.
 */
public interface FHIRClient {

    /**
     *OpenMRS FHIR REST API endpoint base URL (e.g. http://localhost:8080/openmrs/ws/fhir2/R4).
     */
    public static final BASE_URL = "fhirclient.rest.base.url";

    /**
     * Specifies the default mimetype to be used by the FHIRClient instance when invoking
     * OpenMRS FHIR REST APIs.   If not specified a value of "application/fhir+json" will be used.
     */
    public static final MIME_TYPE = "fhirclient.default.mimetype";

    /**
     * Indicates whether OAuth 2.0 should be used when invoking  REST API requests.
     * Valid values are "true" and "false" ( which are the default).   If enabled, then the authorizeURL, tokenURL and grantType properties
     * are required as well.
     */
    public static final String OAUTH2_ENABLED    = "fhirclient.oAuth2.enabled";

    /**
     * The accessToken to use with OAuth 2.0 Authorization.
     */
    public static final String OAUTH2_TOKEN      = "fhirclient.oAuth2.accessToken";

    /**
     * The username to use with Basic Authentication.
     */
    public static final String CLIENT_USERNAME   = "fhirclient.basicauth.username";

    /**
     * The password to use with Basic Authentication.
     */
    public static final String CLIENT_PASSWORD    = "fhirclient.basicauth.password";

    /**
     * This should indicates whether Client Certificate-based Authentication should be used when invoking REST API requests.
     * Valid values are "true" and "false" (the default).  If enabled, then the rest of the "clientauth"-related properties
     * are required as well.
     */
    public static final String CLIENT_AUTH_ENABLED   = "fhirclient.clientauth.enabled";


    /**
     * The client keystore's filename.
     * The client keystore is used to store the client's private/public key pair certificates.
     * When using client certificate-based authentication, this is now the client supplies its identity
     * to the server.
     */
    public static final String KEYSTORE_LOCATION     = "fhirclient.keystore.location";

       /**
     * The client keystore's password.
     */
    public static final String KEYSTORE_PASSWORD     = "fhirclient.keystore.password";

    /**
     * The password associated with the client's certificate within the keystore file.
     */
    public static final String KEYSTORE_KEY_PASSWORD = "fhirclient.keystore.key.password";

    /**
     * This will Indicate whether or not to enable to CXF Logging feature which will log all request and response messages
     * at a level of INFO
     */
    public static final String LOGGING_ENABLED    = "fhirclient.logging.enabled";

    /**
     * The amount of time, in milliseconds, that the consumer will wait for a response before it times out. 0 is infinite.
     * Defaults to 60,000ms (60s)
     */
    public static final String HTTP_TIMEOUT = "fhirclient.http.receive.timeout";

    /**
     * The client preference for whether the server response for modification requests like POST or PUT should include
     * an empty body, the updated resources, or a resource describing the outcome of the interaction.
     *
     * <p>"minimal", "representation", or "OperationOutcome"
     */
    public static final String HTTP_RETURN_PREF = "fhirclient.http.return.pref";

    /**
     * The tenant identifier to use for requests (using the header X-FHIR-TENANT-ID)
     */
    public static final String PROPNAME_TENANT_ID = "fhirclient.tenant.id";

    /**
     * Returns a WebTarget object associated with the REST API endpoint.
     * @return a WebTarget instance that can be used to invoke REST APIs.
     * @throws Exception
     */
    WebTarget getWebTarget() throws Exception;

    /**
     * Returns a JAX-RS 2.0 WebTarget object associated with a given REST API endpoint.
     * @return a WebTarget instance that can be used to invoke REST APIs.
     * @throws Exception
     */
    WebTarget getWebTarget(String baseURL) throws Exception;

    /**
     * Sets the default mime-type to be used by the FHIRClient interface when invoking REST API operations.
     * @param mimeType a string containing the mime-type (e.g. "application/fhir+json")
     * @throws Exception
     */
    void setDefaultMimeType(String mimeType) throws Exception;

    /**
     * Returns a string that represents the default mime-type associated with the FHIRClient interface.
     * @throws Exception
     */
    String getDefaultMimeType() throws Exception;

    /**
     * Sets the OAuth 2.0 access token to be used by the FHIRClient interface for authorization when invoking REST API operations.
     * @param accessToken a string containing the OAuth 2.0 access token
     * @throws Exception
     */
    void setOAuth2AccessToken(String accessToken) throws Exception;

    /**
     * Invokes the 'metadata' OpenMRS FHIR REST API operation.
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains a Conformance object which describes the
     * FHIR Server's capabilities
     * @throws Exception
     */
    FHIRResponse metadata(FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'create' OpenMRS FHIR REST API operation.
     * @param resource the FHIR resource to be created
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'create' operation
     * @throws Exception
     */
    FHIRResponse create(Resource resource, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'conditional create'OpenMRS FHIR REST API operation.
     * @param resource the FHIR resource to be created
     * @param parameters search-related query parameters to be included in the request
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'conditional create' operation
     * @throws Exception
     */
    FHIRResponse conditionalCreate(Resource resource, FHIRParameters parameters, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'create' OpenMRS FHIR REST API operation.
     * @param resource the resource (in the form of a JsonObject) to be created
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'create' operation
     * @throws Exception
     */
    FHIRResponse create(JsonObject resource, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'conditional create'OpenMRS FHIR REST API operation.
     * @param resource the resource (in the form of a JsonObject) to be created
     * @param parameters search-related query parameters to be included in the request
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'conditional create' operation
     * @throws Exception
     */
    FHIRResponse conditionalCreate(JsonObject resource, FHIRParameters parameters, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'update' FHIR REST API operation.
     * @param resource the FHIR resource to be updated
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'update' operation
     * @throws Exception
     */
    FHIRResponse update(Resource resource, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'conditional update' OpenMRS FHIR REST API operation.
     * @param resource the FHIR resource to be created
     * @param parameters search-related query parameters to be included in the request
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'conditional update' operation
     * @throws Exception
     */
    FHIRResponse conditionalUpdate(Resource resource, FHIRParameters parameters, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'update' OpenMRS FHIR REST API operation.
     * @param resource the FHIR resource to be updated
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'update' operation
     * @throws Exception
     */
    FHIRResponse update(Resource resource, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'conditional update'OpenMRS FHIR REST API operation.
     * @param resource the FHIR resource to be created
     * @param parameters search-related query parameters to be included in the request
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'conditional update' operation
     * @throws Exception
     */
    FHIRResponse conditionalUpdate(Resource resource, FHIRParameters parameters, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'update' OpenMRS FHIR REST API operation.
     * @param resource the resource (in the form of a JsonObject) to be updated
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'update' operation
     * @throws Exception
     */
    FHIRResponse update(JsonObject resource, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'conditional update'OpenMRS FHIR REST API operation.
     * @param resource the resource (in the form of a JsonObject) to be updated
     * @param parameters search-related query parameters to be included in the request
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'conditional update' operation
     * @throws Exception
     */
    FHIRResponse conditionalUpdate(JsonObject resource, FHIRParameters parameters, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'delete' OpenMRS FHIR REST API operation.
     * @param resourceType a string representing the name of the resource type
     * to be deleted (e.g. "Patient")
     * @param resourceId the id of the resource to be deleted
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'delete' operation
     * @throws Exception
     */
    FHIRResponse delete(String resourceType, String resourceId, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'conditional delete' OpenMRS FHIR REST API operation.
     * @param resourceType a string representing the name of the resource type
     * to be deleted (e.g. "Patient")
     * @param parameters search-related query parameters to be included in the request
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'delete' operation
     * @throws Exception
     */
    FHIRResponse conditionalDelete(String resourceType, FHIRParameters parameters, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'read' OpenMRS FHIR REST API operation.
     * @param resourceType a string representing the name of the resource type
     * to be retrieved (e.g. "Patient")
     * @param resourceId the id of the resource to be retrieved
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'read' operation
     * @throws Exception
     */
    FHIRResponse read(String resourceType, String resourceId, FHIRRequestHeader... headers) throws Exception;


    /**
     * Invokes the 'search' FHIR REST API operation.
     * @param resourceType a string representing the name of the resource type to search for (e.g. "Patient")
     * @param parameters  an optional collection of request parameters for the 'search' operation;
     * may be specified as null if no parameters need to be passed to the 'search' operation
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'search' operation
     * @throws Exception
     */
    FHIRResponse search(String resourceType, FHIRParameters parameters, FHIRRequestHeader... headers) throws Exception;

        /**
     * Invokes the 'validate' FHIR REST API operation.
     * @param resource the resource to be validated
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'validate' operation
     * @throws Exception
     */
    FHIRResponse validate(Resource resource, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'validate' FHIR REST API operation.
     * @param resource the resource (in the form of a JsonObject) to be validated
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'validate' operation
     * @throws Exception
     */
    FHIRResponse validate(JsonObject resource, FHIRRequestHeader... headers) throws Exception;

        /**
     * Invokes the 'invoke' FHIR REST API operation.
     * @param operationName name of the operation to be performed
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'invoke' operation
     * @throws Exception
     */
    FHIRResponse invoke(String operationName, FHIRParameters parameters, FHIRRequestHeader... headers) throws Exception;


    /**
     * Invokes the 'invoke' FHIR REST API operation.
     * @param resourceType the FHIR resource type used in context for the operation
     * @param operationName name of the operation to be performed
     * @param resourceId the FHIR resource instance used in context for the operation
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'invoke' operation
     * @throws Exception
     */
    FHIRResponse invoke(String resourceType, String operationName, String resourceId, FHIRParameters parameters, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'invoke' FHIR REST API operation.
     * @param resourceType the FHIR resource type used in context for the operation
     * @param operationName name of the operation to be performed
     * @param resourceId the FHIR resource instance used in context for the operation
     * @param resource the FHIR resource used in context for the operation
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'invoke' operation
     * @throws Exception
     */
    FHIRResponse invoke(String resourceType, String operationName, String resourceId, Resource resource, FHIRRequestHeader... headers) throws Exception;








    
}
