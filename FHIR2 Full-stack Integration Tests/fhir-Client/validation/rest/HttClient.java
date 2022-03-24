/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

package org.openmrs.module.fhir2.fhir-server.validation.rest;
/**
 * This interface provides a client API for invoking the FHIR Server's REST API.
 */
public class HttClient {

    private final RestTemplate restTemplate;

    public HttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchData(String uri, HttpMethod method, String body) {
        MultiValueMap<String, String> headers =new LinkedMultiValueMap<>();

        // Query parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri);

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> httpResponse = restTemplate.exchange(builder.build().toUri(), method, httpEntity, String.class);
            return httpResponse.getBody();
        } catch (final HttpClientErrorException e) {
            return e.getResponseBodyAsString();
        }
        catch (final HttpServerErrorException e) {
            return e.getResponseBodyAsString();
        }
    }


}
