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
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class ServiceRequestFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<ServiceRequestFhirResourceProvider, ServiceRequest> {
	
	private static final String TEST_ORDER_INITIAL_DATA = "org/openmrs/module/fhir2/api/dao/impl/FhirServiceRequestTest_initial_data.xml";
	
	private static final String SERVICE_REQUEST_UUID = "7d96f25c-4949-4f72-9931-d808fbc226de";
	
	private static final String WRONG_SERVICE_REQUEST_UUID = "7d96f25c-4949-4f72-9931-d8fdbnsm6de";
	
	private static final String PATIENT_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
	
	private static final String ENCOUNTER_UUID = "y403fafb-e5e4-42d0-9d11-4f52e89d123r";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private ServiceRequestFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(TEST_ORDER_INITIAL_DATA);
	}
	
	@Test
	public void shouldReturnExistingServiceRequestAsJson() throws Exception {
		MockHttpServletResponse response = get("/ServiceRequest/" + SERVICE_REQUEST_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		ServiceRequest serviceRequest = readResponse(response);
		
		assertThat(serviceRequest, notNullValue());
		assertThat(serviceRequest.getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
		assertThat(serviceRequest, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingServiceRequestAsJson() throws Exception {
		MockHttpServletResponse response = get("/ServiceRequest/" + WRONG_SERVICE_REQUEST_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldReturnExistingServiceRequestAsXML() throws Exception {
		MockHttpServletResponse response = get("/ServiceRequest/" + SERVICE_REQUEST_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		ServiceRequest serviceRequest = readResponse(response);
		
		assertThat(serviceRequest, notNullValue());
		assertThat(serviceRequest.getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
		assertThat(serviceRequest, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingServiceRequestAsXML() throws Exception {
		MockHttpServletResponse response = get("/ServiceRequest/" + WRONG_SERVICE_REQUEST_UUID).accept(FhirMediaTypes.XML)
		        .go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldSearchForExistingServiceRequestsAsJson() throws Exception {
		MockHttpServletResponse response = get("/ServiceRequest").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/ServiceRequest/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(ServiceRequest.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		response = get("/ServiceRequest?patient.identifier=101-6&encounter=y403fafb-e5e4-42d0-9d11-4f52e89d123r")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		entries = results.getEntry();
		
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(PATIENT_UUID))))));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("encounter", hasProperty("reference", endsWith(ENCOUNTER_UUID))))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldSearchForExistingServiceRequestsAsXML() throws Exception {
		MockHttpServletResponse response = get("/ServiceRequest").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/ServiceRequest/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(ServiceRequest.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		response = get("/ServiceRequest?patient.identifier=101-6&encounter=y403fafb-e5e4-42d0-9d11-4f52e89d123r")
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		entries = results.getEntry();
		
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(PATIENT_UUID))))));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("encounter", hasProperty("reference", endsWith(ENCOUNTER_UUID))))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldReturnCountForServiceRequestAsJson() throws Exception {
		MockHttpServletResponse response = get("/ServiceRequest?_summary=count").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(4)));
	}
	
	@Test
	public void shouldReturnCountForServiceRequestAsXml() throws Exception {
		MockHttpServletResponse response = get("/ServiceRequest?_summary=count").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(4)));
	}
	
	@Test
	public void shouldReturnAnEtagHeaderWhenRetrievingAnExistingServiceRequest() throws Exception {
		MockHttpServletResponse response = get("/ServiceRequest/" + SERVICE_REQUEST_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		
		assertThat(response.getHeader("etag"), notNullValue());
		assertThat(response.getHeader("etag"), startsWith("W/"));
		
		assertThat(response.getContentAsString(), notNullValue());
		
		ServiceRequest serviceRequest = readResponse(response);
		
		assertThat(serviceRequest, notNullValue());
		assertThat(serviceRequest.getMeta().getVersionId(), notNullValue());
		assertThat(serviceRequest, validResource());
	}
	
	@Test
	public void shouldReturnNotModifiedWhenRetrievingAnExistingServiceRequestWithAnEtag() throws Exception {
		MockHttpServletResponse response = get("/ServiceRequest/" + SERVICE_REQUEST_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		assertThat(response.getHeader("etag"), notNullValue());
		
		String etagValue = response.getHeader("etag");
		
		response = get("/ServiceRequest/" + SERVICE_REQUEST_UUID).accept(FhirMediaTypes.JSON).ifNoneMatchHeader(etagValue)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response, statusEquals(HttpStatus.NOT_MODIFIED));
	}
	
	@Ignore
	public void shouldReturnAnUpdatedServiceRequestWithNewEtagWhenRetrievingAnExistingServiceRequestWithAnEtag()
	        throws Exception {
		MockHttpServletResponse response = get("/ServiceRequest/" + SERVICE_REQUEST_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		assertThat(response.getHeader("etag"), notNullValue());
		
		String etagValue = response.getHeader("etag");
		
		ServiceRequest serviceRequest = readResponse(response);
		
		Date authoredOn = DateUtils.truncate(new Date(), Calendar.DATE);
		serviceRequest.setAuthoredOn(authoredOn);
		
		//send update to the server
		put("/ServiceRequest/" + SERVICE_REQUEST_UUID).jsonContent(toJson(serviceRequest)).accept(FhirMediaTypes.JSON).go();
		
		//send a new GET request, with the “If-None-Match” header specifying the ETag that we previously stored
		response = get("/ServiceRequest/" + SERVICE_REQUEST_UUID).accept(FhirMediaTypes.JSON).ifNoneMatchHeader(etagValue)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response, statusEquals(HttpStatus.OK));
	}
}
