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

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class MedicationRequestFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<MedicationRequestFhirResourceProvider, MedicationRequest> {
	
	private static final String MEDICATION_REQUEST_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirMedicationRequestDaoImpl_initial_data.xml";
	
	private static final String MEDICATION_REQUEST_UUID = "6d0ae116-707a-4629-9850-f15206e63ab0";
	
	private static final String WRONG_MEDICATION_REQUEST_UUID = "6d0ae116-0000-4629-9850-f15206e63ab0";
	
	private static final String PATIENT_UUID = "86526ed5-3c11-11de-a0ba-001e3766667a";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private MedicationRequestFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(MEDICATION_REQUEST_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingMedicationRequestAsJson() throws Exception {
		MockHttpServletResponse response = get("/MedicationRequest/" + MEDICATION_REQUEST_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		MedicationRequest medicationRequest = readResponse(response);
		
		assertThat(medicationRequest, notNullValue());
		assertThat(medicationRequest.getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
		assertThat(medicationRequest, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingMedicationRequestAsJson() throws Exception {
		MockHttpServletResponse response = get("/MedicationRequest/" + WRONG_MEDICATION_REQUEST_UUID)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldReturnExistingMedicationRequestAsXML() throws Exception {
		MockHttpServletResponse response = get("/MedicationRequest/" + MEDICATION_REQUEST_UUID).accept(FhirMediaTypes.XML)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		MedicationRequest medicationRequest = readResponse(response);
		
		assertThat(medicationRequest, notNullValue());
		assertThat(medicationRequest.getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
		assertThat(medicationRequest, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingMedicationRequestAsXML() throws Exception {
		MockHttpServletResponse response = get("/MedicationRequest/" + WRONG_MEDICATION_REQUEST_UUID)
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldSearchForExistingMedicationRequestsAsJson() throws Exception {
		MockHttpServletResponse response = get("/MedicationRequest").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries,
		    everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/MedicationRequest/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(MedicationRequest.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		response = get("/MedicationRequest?patient.identifier=MO-2").accept(FhirMediaTypes.JSON).go();
		
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
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldSearchForExistingMedicationRequestsAsXML() throws Exception {
		MockHttpServletResponse response = get("/MedicationRequest").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries,
		    everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/MedicationRequest/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(MedicationRequest.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		response = get("/MedicationRequest?patient.identifier=MO-2").accept(FhirMediaTypes.XML).go();
		
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
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldReturnCountForMedicationRequestAsJson() throws Exception {
		MockHttpServletResponse response = get("/MedicationRequest?_summary=count").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(13)));
	}
	
	@Test
	public void shouldReturnCountForMedicationRequestAsXml() throws Exception {
		MockHttpServletResponse response = get("/MedicationRequest?_summary=count").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(13)));
	}
}
