/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.servlet.ServletException;

import java.util.Calendar;
import java.util.Collections;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.openmrs.module.fhir2.api.FhirServiceRequestService;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRequestFhirResourceProviderWebTest extends BaseFhirR4ResourceProviderWebTest<ServiceRequestFhirResourceProvider, ServiceRequest> {
	
	private static final String SERVICE_REQUEST_UUID = "7d13b03b-58c2-43f5-b34d-08750c51aea9";
	
	private static final String WRONG_SERVICE_REQUEST_UUID = "92b04062-e57d-43aa-8c38-90a1ad70080c";
	
	private static final String LAST_UPDATED_DATE = "eq2020-09-03";
	
	@Getter(AccessLevel.PUBLIC)
	private ServiceRequestFhirResourceProvider resourceProvider;
	
	@Mock
	private FhirServiceRequestService service;
	
	@Captor
	private ArgumentCaptor<TokenAndListParam> tokenAndListParamArgumentCaptor;
	
	@Captor
	private ArgumentCaptor<DateRangeParam> dateRangeParamArgumentCaptor;
	
	private ServiceRequest serviceRequest;

	private static final String JSON_CREATE_SERVICE_REQUEST_PATH = "org/openmrs/module/fhir2/providers/ServiceRequestWebTest_create.json";

	private static final String JSON_UPDATE_SERVICE_REQUEST_PATH = "org/openmrs/module/fhir2/providers/ServiceRequestWebTest_update.json";

	private static final String JSON_UPDATE_SERVICE_REQUEST_NO_ID_PATH = "org/openmrs/module/fhir2/providers/ServiceRequestWebTest_UpdateWithoutId.json";

	private static final String JSON_UPDATE_SERVICE_REQUEST_WRONG_ID_PATH = "org/openmrs/module/fhir2/providers/ServiceRequestWebTest_UpdateWithWrongId.json";
	
	@Before
	@Override
	public void setup() throws ServletException {
		resourceProvider = new ServiceRequestFhirResourceProvider();
		resourceProvider.setServiceRequestService(service);
		serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);
		super.setup();
	}
	
	@Test
	public void getServiceRequestById_shouldReturnServiceRequest() throws Exception {
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);
		
		when(service.get(SERVICE_REQUEST_UUID)).thenReturn(serviceRequest);
		
		MockHttpServletResponse response = get("/ServiceRequest/" + SERVICE_REQUEST_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void getEncounterByWrongUuid_shouldReturn404() throws Exception {
		MockHttpServletResponse response = get("/ServiceRequest/" + WRONG_SERVICE_REQUEST_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByUUID() throws Exception {
		verifyUri(String.format("/ServiceRequest?_id=%s", SERVICE_REQUEST_UUID));
		
		verify(service).searchForServiceRequests(tokenAndListParamArgumentCaptor.capture(), isNull());
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByLastUpdatedDate() throws Exception {
		verifyUri(String.format("/ServiceRequest?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(service).searchForServiceRequests(isNull(), dateRangeParamArgumentCaptor.capture());
		
		assertThat(dateRangeParamArgumentCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(dateRangeParamArgumentCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeParamArgumentCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	private void verifyUri(String uri) throws Exception {
		when(service.searchForServiceRequests(any(), any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(serviceRequest), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
	}

	@Test
	public void createServiceRequest_shouldCreateServiceRequest() throws Exception {
		String jsonServiceRequest;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_SERVICE_REQUEST_PATH)) {
			Objects.requireNonNull(is);
			jsonServiceRequest = IOUtils.toString(is, StandardCharsets.UTF_8);
		}

		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);

		when(service.create(any(ServiceRequest.class))).thenReturn(serviceRequest);

		MockHttpServletResponse response = post("/ServiceRequest").jsonContent(jsonServiceRequest)
		        .accept(FhirMediaTypes.JSON).go();

		assertThat(response, isCreated());
	}

	@Test
	public void updateServiceRequest_shouldUpdateExistingServiceRequest() throws Exception {
		String jsonServiceRequest;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_SERVICE_REQUEST_PATH )) {
			Objects.requireNonNull(is);
			jsonServiceRequest = IOUtils.toString(is, StandardCharsets.UTF_8);
		}

		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);

		when(service.update(anyString(), any(ServiceRequest.class))).thenReturn(serviceRequest);

		MockHttpServletResponse response = put("/ServiceRequest/" + SERVICE_REQUEST_UUID).jsonContent(jsonServiceRequest)
				.accept(FhirMediaTypes.JSON).go();

		assertThat(response, isOk());
	}

	@Test
	public void updateServiceRequest_shouldRaiseExceptionForNoId() throws Exception {
		String jsonServiceRequest;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_SERVICE_REQUEST_NO_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonServiceRequest = IOUtils.toString(is, StandardCharsets.UTF_8);
		}

		MockHttpServletResponse response = put("/ServiceRequest/" + SERVICE_REQUEST_UUID).jsonContent(jsonServiceRequest)
				.accept(FhirMediaTypes.JSON).go();

		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
				containsStringIgnoringCase("body must contain an ID element for update"));
	}

	@Test
	public void updateServiceRequest_shouldRaiseExceptionForIdMissMatch() throws Exception {
		String jsonServiceRequest;
		try (InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream(JSON_UPDATE_SERVICE_REQUEST_WRONG_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonServiceRequest = IOUtils.toString(is, StandardCharsets.UTF_8);
		}

		MockHttpServletResponse response = put("/ServiceRequest/" + WRONG_SERVICE_REQUEST_UUID).jsonContent(jsonServiceRequest)
				.accept(FhirMediaTypes.JSON).go();

		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
				containsStringIgnoringCase("body must contain an ID element which matches the request URL"));
	}

	@Test
	public void deleteServiceRequest_shouldDeleteServiceRequest() throws Exception {
		OperationOutcome retVal = new OperationOutcome();
		retVal.setId(SERVICE_REQUEST_UUID);
		retVal.getText().setDivAsString("Deleted successfully");

		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);

		when(service.delete(SERVICE_REQUEST_UUID)).thenReturn(serviceRequest);

		MockHttpServletResponse response = delete("/ServiceRequest/" + SERVICE_REQUEST_UUID).accept(FhirMediaTypes.JSON).go();

		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
}
