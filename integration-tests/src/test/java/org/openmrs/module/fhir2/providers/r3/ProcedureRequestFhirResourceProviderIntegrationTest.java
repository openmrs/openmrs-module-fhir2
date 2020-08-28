/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class ProcedureRequestFhirResourceProviderIntegrationTest extends BaseFhirR3IntegrationTest<ProcedureRequestFhirResourceProvider, ProcedureRequest> {
	
	private static final String TEST_ORDER_INITIAL_DATA = "org/openmrs/module/fhir2/api/dao/impl/FhirServiceRequestTest_initial_data.xml";
	
	private static final String PROCEDURE_REQUEST_UUID = "7d96f25c-4949-4f72-9931-d808fbc226de";
	
	private static final String WRONG_PROCEDURE_REQUEST_UUID = "7d96f25c-4949-4f72-9931-d8fdbnsm6de";
	
	private static final String PATIENT_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
	
	private static final String ENCOUNTER_UUID = "y403fafb-e5e4-42d0-9d11-4f52e89d123r";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private ProcedureRequestFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(TEST_ORDER_INITIAL_DATA);
	}
	
	@Test
	public void shouldReturnExistingProcedureRequestAsJson() throws Exception {
		MockHttpServletResponse response = get("/ProcedureRequest/" + PROCEDURE_REQUEST_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		ProcedureRequest serviceRequest = readResponse(response);
		
		assertThat(serviceRequest, notNullValue());
		assertThat(serviceRequest.getIdElement().getIdPart(), equalTo(PROCEDURE_REQUEST_UUID));
		assertThat(serviceRequest, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingProcedureRequestAsJson() throws Exception {
		MockHttpServletResponse response = get("/ProcedureRequest/" + WRONG_PROCEDURE_REQUEST_UUID)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldReturnExistingProcedureRequestAsXML() throws Exception {
		MockHttpServletResponse response = get("/ProcedureRequest/" + PROCEDURE_REQUEST_UUID).accept(FhirMediaTypes.XML)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		ProcedureRequest serviceRequest = readResponse(response);
		
		assertThat(serviceRequest, notNullValue());
		assertThat(serviceRequest.getIdElement().getIdPart(), equalTo(PROCEDURE_REQUEST_UUID));
		assertThat(serviceRequest, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingProcedureRequestAsXML() throws Exception {
		MockHttpServletResponse response = get("/ProcedureRequest/" + WRONG_PROCEDURE_REQUEST_UUID)
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldSearchForExistingProcedureRequestsAsJson() throws Exception {
		MockHttpServletResponse response = get("/ProcedureRequest").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/ProcedureRequest/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(ProcedureRequest.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		response = get("/ProcedureRequest?patient.identifier=101-6&encounter=y403fafb-e5e4-42d0-9d11-4f52e89d123r")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		entries = results.getEntry();
		
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(PATIENT_UUID))))));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("context", hasProperty("reference", endsWith(ENCOUNTER_UUID))))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldSearchForExistingProcedureRequestsAsXML() throws Exception {
		MockHttpServletResponse response = get("/ProcedureRequest").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/ProcedureRequest/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(ProcedureRequest.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		response = get("/ProcedureRequest?patient.identifier=101-6&encounter=y403fafb-e5e4-42d0-9d11-4f52e89d123r")
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		entries = results.getEntry();
		
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(PATIENT_UUID))))));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("context", hasProperty("reference", endsWith(ENCOUNTER_UUID))))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
}
