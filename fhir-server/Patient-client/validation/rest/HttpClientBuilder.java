/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.fhir2.fhir-server.validation.rest;

public class HttpClientBuilder {
    public HttpClientBuilder() {

    }

    public HttpClient build() {
        return new HttpClient(buildTemplate(getRestBuilder()));
    }
    private RestTemplateBuilder getRestBuilder() {
        return new RestTemplateBuilder(customRestTemplateCustomizer());
    }

    public CustomRestTemplateCustomizer customRestTemplateCustomizer() {
        return new CustomRestTemplateCustomizer();
    }

    private static RestTemplate buildTemplate(RestTemplateBuilder restBuilder) {
        final Map<String, String> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        headers.put(ACCEPT, APPLICATION_JSON_VALUE);
        RestTemplateBuilder httpTemplate = restBuilder.additionalInterceptors(
                new SetHeaderInterceptor(headers));
        return httpTemplate.build();
    }
    
}
