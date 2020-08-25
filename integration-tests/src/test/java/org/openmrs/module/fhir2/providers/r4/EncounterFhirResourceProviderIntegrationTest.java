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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class EncounterFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<EncounterFhirResourceProvider, Encounter> {
	
	private static final String[] ENCOUNTER_DATA_XML = {
	        "org/openmrs/module/fhir2/api/dao/impl/FhirEncounterDaoImplTest_initial_data.xml",
	        "org/openmrs/module/fhir2/api/dao/impl/FhirVisitDaoImplTest_initial_data.xml" };
	
	private static final String ENCOUNTER_UUID = "430bbb70-6a9c-4e1e-badb-9d1034b1b5e9";
	
	private static final String BAD_ENCOUNTER_UUID = "890bbb70-6a9c-451e-badb-9d1034b1b5er";
	
	private static final String PATIENT_UUID = "a194be38-271a-44cb-ba3f-f2dbf4831fe9";
	
	private static final String LOCATION_UUID = "c36006e5-9fbb-4f20-866b-0ece245615a1";
	
	private static final String PATIENT_GIVEN_NAME = "John";
	
	private static final String VISIT_UUID = "65aefd46-973d-4526-89de-93842c80ad11";
	
	private static final String BAD_VISIT_UUID = "78aefd46-883d-4526-00de-93842c80ad86";
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	private EncounterFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		
		for (String encounterData : ENCOUNTER_DATA_XML) {
			executeDataSet(encounterData);
		}
	}
	
	@Test
	public void shouldReturnExistingEncounterAsJson() throws Exception {
		MockHttpServletResponse response = get("/Encounter/" + ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Encounter encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
		assertThat(encounter, validResource());
	}
	
	@Test
	public void shouldReturnExistingEncounterFromOpenMrsVisitAsJson() throws Exception {
		MockHttpServletResponse response = get("/Encounter/" + VISIT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Encounter encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getIdElement().getIdPart(), equalTo(VISIT_UUID));
		assertThat(encounter, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingEncounterAsJson() throws Exception {
		MockHttpServletResponse response = get("/Encounter/" + BAD_ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldThrow404ForNonExistingVisitAsJson() throws Exception {
		MockHttpServletResponse response = get("/Encounter/" + BAD_VISIT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldReturnExistingEncounterAsXML() throws Exception {
		MockHttpServletResponse response = get("/Encounter/" + ENCOUNTER_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Encounter encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
		assertThat(encounter, validResource());
	}
	
	@Test
	public void shouldReturnExistingEncounterFromOpenMrsVisitAsXML() throws Exception {
		MockHttpServletResponse response = get("/Encounter/" + VISIT_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Encounter encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getIdElement().getIdPart(), equalTo(VISIT_UUID));
		assertThat(encounter, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingEncounterAsXML() throws Exception {
		MockHttpServletResponse response = get("/Encounter/" + BAD_ENCOUNTER_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldThrow404ForNonExistingVisitAsXML() throws Exception {
		MockHttpServletResponse response = get("/Encounter/" + BAD_VISIT_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldSearchForEncountersByPatientIdAsJson() throws Exception {
		String uri = String.format("/Encounter?subject:Patient=%s", PATIENT_UUID);
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Encounter"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Encounter.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", equalTo("Patient/" + PATIENT_UUID))))));
	}
	
	@Test
	public void shouldSearchForEncountersByPatientIdAsXml() throws Exception {
		String uri = String.format("/Encounter?subject:Patient=%s", PATIENT_UUID);
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Encounter"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Encounter.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", equalTo("Patient/" + PATIENT_UUID))))));
	}
	
	@Test
	public void shouldSearchForEncountersByPatientGivenNameAsJson() throws Exception {
		String uri = String.format("/Encounter/?subject.given=%s", PATIENT_GIVEN_NAME);
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Encounter"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Encounter.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("display", containsString(PATIENT_GIVEN_NAME))))));
	}
	
	@Test
	public void shouldSearchForEncountersByPatientGivenNameAsXml() throws Exception {
		String uri = String.format("/Encounter/?subject.given=%s", PATIENT_GIVEN_NAME);
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Encounter"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Encounter.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("display", containsString(PATIENT_GIVEN_NAME))))));
	}
	
	@Test
	public void shouldSearchForEncountersByLocationIdAsJson() throws Exception {
		String uri = String.format("/Encounter/?location=%s", LOCATION_UUID);
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Encounter"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Encounter.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		assertThat(entries, everyItem(hasResource(hasProperty("location",
		    hasItems(hasProperty("location", hasProperty("reference", equalTo("Location/" + LOCATION_UUID))))))));
		assertThat(entries, everyItem(hasResource(
		    hasProperty("location", hasItems(hasProperty("location", hasProperty("display", equalTo("Test Location"))))))));
	}
	
	@Test
	public void shouldSearchForEncountersByLocationIdAsXml() throws Exception {
		String uri = String.format("/Encounter/?location=%s", LOCATION_UUID);
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Encounter"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Encounter.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		assertThat(entries, everyItem(hasResource(hasProperty("location",
		    hasItems(hasProperty("location", hasProperty("reference", equalTo("Location/" + LOCATION_UUID))))))));
		assertThat(entries, everyItem(hasResource(
		    hasProperty("location", hasItems(hasProperty("location", hasProperty("display", equalTo("Test Location"))))))));
	}
	
	@Test
	public void shouldDeleteExistingEncounter() throws Exception {
		MockHttpServletResponse response = delete("/Encounter/" + ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		response = get("/Encounter/" + ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, statusEquals(HttpStatus.GONE));
	}
	
	@Test
	public void shouldReturnNotFoundWhenDeletingNonExistentEncounter() throws Exception {
		MockHttpServletResponse response = delete("/Encounter/" + BAD_ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
}
