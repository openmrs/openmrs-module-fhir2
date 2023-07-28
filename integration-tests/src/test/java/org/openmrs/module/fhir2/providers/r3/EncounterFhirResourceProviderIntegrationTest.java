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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hl7.fhir.dstu3.model.Bundle.BundleType.SEARCHSET;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

public class EncounterFhirResourceProviderIntegrationTest extends BaseFhirR3IntegrationTest<EncounterFhirResourceProvider, Encounter> {
	
	private static final String MEDICATION_REQUEST_QUERY_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirEncounterDaoImplTest_initial_data.xml"; // not loaded for all tests
	
	private static final String ENCOUNTER_JSON_CREATE_ENCOUNTER_PATH = "org/openmrs/module/fhir2/providers/EncounterWebTest_create.json";
	
	private static final String ENCOUNTER_XML_CREATE_ENCOUNTER_PATH = "org/openmrs/module/fhir2/providers/EncounterWebTest_create.xml";
	
	private static final String VISIT_JSON_CREATE_ENCOUNTER_PATH = "org/openmrs/module/fhir2/providers/VisitWebTest_create.json";
	
	private static final String VISIT_XML_CREATE_ENCOUNTER_PATH = "org/openmrs/module/fhir2/providers/VisitWebTest_create.xml";
	
	private static final String ENCOUNTER_JSON_UPDATE_ENCOUNTER_PATH = "org/openmrs/module/fhir2/providers/EncounterWebTest_update.json";
	
	private static final String VISIT_JSON_UPDATE_ENCOUNTER_PATH = "org/openmrs/module/fhir2/providers/VisitWebTest_update.json";
	
	private static final String ENCOUNTER_JSON_UPDATE_ENCOUNTER_PATH_WITH_WRONG_ID = "org/openmrs/module/fhir2/providers/EncounterWebTest_updateWithWrongId.json";
	
	private static final String VISIT_JSON_UPDATE_ENCOUNTER_PATH_WITH_WRONG_ID = "org/openmrs/module/fhir2/providers/VisitWebTest_updateWithWrongId.json";
	
	private static final String ENCOUNTER_UUID = "6519d653-393b-4118-9c83-a3715b82d4ac"; // encounter 3 from standard test dataset
	
	private static final String BAD_ENCOUNTER_UUID = "890bbb70-6a9c-451e-badb-9d1034b1b5er";
	
	private static final String PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041"; // patient 7 from the standard test dataset
	
	private static final String LOCATION_UUID = "8d6c993e-c2cc-11de-8d13-0010c6dffd0f"; // location 1 from the standard test dataset
	
	private static final String PATIENT_GIVEN_NAME = "Collet"; // given name of patient 7 from the standard test dataset
	
	private static final String VISIT_UUID = "1e5d5d48-6b78-11e0-93c3-18a905e044dc";
	
	private static final String BAD_VISIT_UUID = "78aefd46-883d-4526-00de-93842c80ad86";
	
	private static final String ENCOUNTER_TYPE_UUID = "61ae96f4-6afe-4351-b6f8-cd4fc383cce1"; // encounter type 1 from the standard test dataset
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	private EncounterFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
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
		assertThat(results.getType(), equalTo(SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Encounter"))));
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
		assertThat(results.getType(), equalTo(SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Encounter"))));
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
		assertThat(results.getType(), equalTo(SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Encounter"))));
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
		assertThat(results.getType(), equalTo(SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Encounter"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Encounter.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("display", containsString(PATIENT_GIVEN_NAME))))));
	}
	
	@Test
	public void shouldSearchForEncounterByEncounterTypeAsJson() throws Exception {
		String uri = String.format("/Encounter/?type=%s", ENCOUNTER_TYPE_UUID);
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Encounter"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Encounter.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		assertThat(entries, everyItem(hasResource(hasProperty("type",
		    hasItems(hasProperty("coding", hasItems(hasProperty("code", equalTo(ENCOUNTER_TYPE_UUID)))))))));
		assertThat(entries, everyItem(hasResource(
		    hasProperty("type", hasItems(hasProperty("coding", hasItems(hasProperty("display", equalTo("Scheduled")))))))));
	}
	
	@Test
	public void shouldSearchForEncounterByEncounterTypeAsXML() throws Exception {
		String uri = String.format("/Encounter/?type=%s", ENCOUNTER_TYPE_UUID);
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Encounter"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Encounter.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		assertThat(entries, everyItem(hasResource(hasProperty("type",
		    hasItems(hasProperty("coding", hasItems(hasProperty("code", equalTo(ENCOUNTER_TYPE_UUID)))))))));
		assertThat(entries, everyItem(hasResource(
		    hasProperty("type", hasItems(hasProperty("coding", hasItems(hasProperty("display", equalTo("Scheduled")))))))));
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
		assertThat(results.getType(), equalTo(SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Encounter"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Encounter.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		assertThat(entries, everyItem(hasResource(hasProperty("location",
		    hasItems(hasProperty("location", hasProperty("reference", equalTo("Location/" + LOCATION_UUID))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("location",
		    hasItems(hasProperty("location", hasProperty("display", equalTo("Unknown Location"))))))));
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
		assertThat(results.getType(), equalTo(SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Encounter"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Encounter.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		assertThat(entries, everyItem(hasResource(hasProperty("location",
		    hasItems(hasProperty("location", hasProperty("reference", equalTo("Location/" + LOCATION_UUID))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("location",
		    hasItems(hasProperty("location", hasProperty("display", equalTo("Unknown Location"))))))));
	}
	
	@Test
	public void shouldReturnCountForEncounterAsJson() throws Exception {
		MockHttpServletResponse response = get("/Encounter/?_summary=count").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(9))); // 5 non-voided visits and 4 non-voided encounters in standard test dataset
	}
	
	@Test
	public void shouldReturnCountForEncounterAsXml() throws Exception {
		MockHttpServletResponse response = get("/Encounter/?_summary=count").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(9))); // 5 non-voided visits and 4 non-voided encounters in standard test data
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
	
	@Test
	public void shouldCreateEncounterFromOpenMrsEncounterAsJson() throws Exception {
		String jsonEncounter;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(ENCOUNTER_JSON_CREATE_ENCOUNTER_PATH)) {
			Objects.requireNonNull(is);
			jsonEncounter = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = post("/Encounter").accept(FhirMediaTypes.JSON).jsonContent(jsonEncounter).go();
		
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is((FhirMediaTypes.JSON.toString())));
		assertThat(response.getContentAsString(), notNullValue());
		
		Encounter encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getIdElement().getIdPart(), notNullValue());
		assertThat(encounter.getStatus().getDisplay(), notNullValue());
		assertThat(encounter.getMeta().getTag().get(0).getSystem(), equalTo(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG));
		assertThat(encounter.getType().get(0).getCoding().get(0).getSystem(),
		    equalTo(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI));
		assertThat(encounter.getPeriod().getStart(), notNullValue());
		assertThat(encounter.getLocation().get(0).getLocation().getDisplay(), equalTo("Unknown Location"));
		
		response = get("/Encounter/" + encounter.getIdElement().getIdPart()).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Encounter newEncounter = readResponse(response);
		
		assertThat(newEncounter.getId(), equalTo(encounter.getId()));
	}
	
	@Test
	public void shouldCreateEncounterFromOpenMrsEncounterAsXML() throws Exception {
		String xmlEncounter;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(ENCOUNTER_XML_CREATE_ENCOUNTER_PATH)) {
			Objects.requireNonNull(is);
			xmlEncounter = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = post("/Encounter").accept(FhirMediaTypes.XML).xmlContent(xmlEncounter).go();
		
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is((FhirMediaTypes.XML.toString())));
		assertThat(response.getContentAsString(), notNullValue());
		
		Encounter encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getIdElement().getIdPart(), notNullValue());
		assertThat(encounter.getStatus().getDisplay(), notNullValue());
		assertThat(encounter.getMeta().getTag().get(0).getSystem(), equalTo(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG));
		assertThat(encounter.getType().get(0).getCoding().get(0).getSystem(),
		    equalTo(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI));
		assertThat(encounter.getPeriod().getStart(), notNullValue());
		assertThat(encounter.getLocation().get(0).getLocation().getDisplay(), equalTo("Unknown Location"));
		
		response = get("/Encounter/" + encounter.getIdElement().getIdPart()).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		Encounter newEncounter = readResponse(response);
		
		assertThat(newEncounter.getId(), equalTo(encounter.getId()));
	}
	
	@Test
	public void shouldCreateEncounterFromOpenMrsVisitAsJson() throws Exception {
		String jsonEncounter;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(VISIT_JSON_CREATE_ENCOUNTER_PATH)) {
			Objects.requireNonNull(is);
			jsonEncounter = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = post("/Encounter").accept(FhirMediaTypes.JSON).jsonContent(jsonEncounter).go();
		
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is((FhirMediaTypes.JSON.toString())));
		assertThat(response.getContentAsString(), notNullValue());
		
		Encounter encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getIdElement().getIdPart(), notNullValue());
		assertThat(encounter.getStatus().getDisplay(), notNullValue());
		assertThat(encounter.getMeta().getTag().get(0).getSystem(), equalTo(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG));
		assertThat(encounter.getType().get(0).getCoding().get(0).getSystem(), equalTo(FhirConstants.VISIT_TYPE_SYSTEM_URI));
		assertThat(encounter.getPeriod().getStart(), notNullValue());
		
		response = get("/Encounter/" + encounter.getIdElement().getIdPart()).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Encounter newEncounter = readResponse(response);
		
		assertThat(newEncounter.getId(), equalTo(encounter.getId()));
	}
	
	@Test
	public void shouldCreateEncounterFromOpenMrsVisitAsXML() throws Exception {
		String xmlEncounter;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(VISIT_XML_CREATE_ENCOUNTER_PATH)) {
			Objects.requireNonNull(is);
			xmlEncounter = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = post("/Encounter").accept(FhirMediaTypes.XML).xmlContent(xmlEncounter).go();
		
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is((FhirMediaTypes.XML.toString())));
		assertThat(response.getContentAsString(), notNullValue());
		
		Encounter encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getIdElement().getIdPart(), notNullValue());
		assertThat(encounter.getStatus().getDisplay(), notNullValue());
		assertThat(encounter.getMeta().getTag().get(0).getSystem(), equalTo(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG));
		assertThat(encounter.getType().get(0).getCoding().get(0).getSystem(), equalTo(FhirConstants.VISIT_TYPE_SYSTEM_URI));
		assertThat(encounter.getPeriod().getStart(), notNullValue());
		
		response = get("/Encounter/" + encounter.getIdElement().getIdPart()).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		Encounter newEncounter = readResponse(response);
		
		assertThat(newEncounter.getId(), equalTo(encounter.getId()));
	}
	
	@Test
	@Transactional(readOnly = true)
	public void shouldUpdateExistingEncounterFromOpenMrsEncounterAsJson() throws Exception {
		// Before Update
		MockHttpServletResponse response = get("/Encounter/" + ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Encounter encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter, validResource());
		assertThat(encounter.getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
		
		String jsonEncounter;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(ENCOUNTER_JSON_UPDATE_ENCOUNTER_PATH)) {
			Objects.requireNonNull(is);
			jsonEncounter = inputStreamToString(is, UTF_8);
		}
		
		response = put("/Encounter/" + ENCOUNTER_UUID).jsonContent(jsonEncounter).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
		assertThat(encounter, validResource());
		
		// Double check via get
		response = get("/Encounter/" + ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter, validResource());
		assertThat(encounter.getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	@Transactional(readOnly = true)
	public void shouldUpdateExistingEncounterFromOpenMrsVisitAsJson() throws Exception {
		MockHttpServletResponse response = get("/Encounter/" + VISIT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Encounter encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter, validResource());
		assertThat(encounter.getIdElement().getIdPart(), equalTo(VISIT_UUID));
		
		String jsonEncounter;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(VISIT_JSON_UPDATE_ENCOUNTER_PATH)) {
			Objects.requireNonNull(is);
			jsonEncounter = inputStreamToString(is, UTF_8);
		}
		
		response = put("/Encounter/" + VISIT_UUID).jsonContent(jsonEncounter).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getIdElement().getIdPart(), equalTo(VISIT_UUID));
		assertThat(encounter, validResource());
		
		// Double check via get
		response = get("/Encounter/" + VISIT_UUID).accept(FhirMediaTypes.JSON).go();
		
		encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter, validResource());
		assertThat(encounter.getIdElement().getIdPart(), equalTo(VISIT_UUID));
	}
	
	@Test
	public void shouldThrow404WhenUpdatingNonExistingEncounterAsJson() throws Exception {
		String jsonEncounter;
		try (InputStream is = this.getClass().getClassLoader()
		        .getResourceAsStream(ENCOUNTER_JSON_UPDATE_ENCOUNTER_PATH_WITH_WRONG_ID)) {
			Objects.requireNonNull(is);
			jsonEncounter = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = put("/Encounter/" + BAD_ENCOUNTER_UUID).jsonContent(jsonEncounter)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldThrow404WhenUpdatingNonExistingVisitAsJson() throws Exception {
		String jsonEncounter;
		try (InputStream is = this.getClass().getClassLoader()
		        .getResourceAsStream(VISIT_JSON_UPDATE_ENCOUNTER_PATH_WITH_WRONG_ID)) {
			Objects.requireNonNull(is);
			jsonEncounter = inputStreamToString(is, UTF_8);
		}
		MockHttpServletResponse response = put("/Encounter/" + BAD_VISIT_UUID).jsonContent(jsonEncounter)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldReturnEncountersWithMedicationRequestAsJson() throws Exception {
		
		executeDataSet(MEDICATION_REQUEST_QUERY_INITIAL_DATA_XML); // additional test data from the fhe FHIR Enocounter DAO test we use to test the encountersWithMedicationRequests query
		
		MockHttpServletResponse response = get("/Encounter/?_query=encountersWithMedicationRequests")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(5))); // 3 encounters in FhirEncounterDaoImplTest_initial_data that have associated medication request (not discontinued), and two encounter (3 and 6) with orders in standard test data set, so total = 5
		assertThat(result.getEntry(), hasSize(16)); // there are 8 requests associated  encounter 3 and 6 in the test data, 3 with the 3 encounters from the test data so total elements should be 8 + 3 + 5 =
		
		List<Bundle.BundleEntryComponent> entries = result.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/"))));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("resourceType", in(getEncounterWithMedicationRequestsValidResourceTypes())))));
	}
	
	@Test
	public void shouldReturnEncountersWithMedicationRequestShouldRestrictByDate() throws Exception {
		
		executeDataSet(MEDICATION_REQUEST_QUERY_INITIAL_DATA_XML); // additional test data from the fhe FHIR Enocounter DAO test we use to test the encountersWithMedicationRequests query
		
		MockHttpServletResponse response = get("/Encounter/?_query=encountersWithMedicationRequests&date=ge2009-01-01")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(3))); // 3 encounters in FhirEncounterDaoImplTest_initial_data are from 2010, and two encounter (3 and 6) with orders in standard test data set are from 2008, so searching on 2010, expect only
		assertThat(result.getEntry(), hasSize(6)); // 3 orders with the 3 encounters from the test data so total elements should be 3 + 3 = 6
		
		List<Bundle.BundleEntryComponent> entries = result.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/"))));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("resourceType", in(getEncounterWithMedicationRequestsValidResourceTypes())))));
	}
	
	@Test
	public void shouldReturnEncountersWithMedicationRequestShouldRestrictByPatientName() throws Exception {
		
		executeDataSet(MEDICATION_REQUEST_QUERY_INITIAL_DATA_XML); // additional test data from the fhe FHIR Enocounter DAO test we use to test the encountersWithMedicationRequests query
		
		MockHttpServletResponse response = get(
		    "/Encounter/?_query=encountersWithMedicationRequests&patientSearchTerm=Chebaskwony").accept(FhirMediaTypes.JSON)
		            .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(1))); // patient 7 (Chebashkwony) only have 1 encounter with medication request
		assertThat(result.getEntry(), hasSize(3)); // and that encounter has two requests, so total should be 3
		
		List<Bundle.BundleEntryComponent> entries = result.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/"))));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("resourceType", in(getEncounterWithMedicationRequestsValidResourceTypes())))));
	}
	
	@Test
	public void shouldReturnEncountersWithMedicationRequestShouldRestrictByPatientIdentifier() throws Exception {
		
		executeDataSet(MEDICATION_REQUEST_QUERY_INITIAL_DATA_XML);
		
		MockHttpServletResponse response = get("/Encounter/?_query=encountersWithMedicationRequests&patientSearchTerm=6TS-4")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(1))); // patient 7 (Chebashkwony) only have 1 encounter with medication request
		assertThat(result.getEntry(), hasSize(3)); // and that encounter has two requests, so total should be 3
		
		List<Bundle.BundleEntryComponent> entries = result.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/"))));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("resourceType", in(getEncounterWithMedicationRequestsValidResourceTypes())))));
	}
	
	@Test
	public void shouldReturnEncountersWithMedicationRequestShouldRestrictByLocation() throws Exception {
		
		executeDataSet(MEDICATION_REQUEST_QUERY_INITIAL_DATA_XML); // additional test data from the fhe FHIR Enocounter DAO test we use to test the encountersWithMedicationRequests query
		
		MockHttpServletResponse response = get(
		    "/Encounter/?_query=encountersWithMedicationRequests&location=9356400c-a5a2-4532-8f2b-2361b3446eb8")
		            .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(1))); // only 1 of the encounters (6 in the main test data) is at location 2 ("Xanadu",  9356400c-a5a2-4532-8f2b-2361b3446eb8)
		assertThat(result.getEntry(), hasSize(7)); // there are 6 requests associated, so that plus the tne encounter is 7
		
		List<Bundle.BundleEntryComponent> entries = result.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/"))));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("resourceType", in(getEncounterWithMedicationRequestsValidResourceTypes())))));
	}
	
	@Test
	public void shouldReturnAnEtagHeaderWhenRetrievingAnExistingEncounter() throws Exception {
		MockHttpServletResponse response = get("/Encounter/" + ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		
		assertThat(response.getHeader("etag"), notNullValue());
		assertThat(response.getHeader("etag"), startsWith("W/"));
		
		assertThat(response.getContentAsString(), notNullValue());
		
		Encounter encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getMeta().getVersionId(), notNullValue());
		assertThat(encounter, validResource());
	}
	
	@Test
	public void shouldReturnNotModifiedWhenRetrievingAnExistingEncounterWithAnEtag() throws Exception {
		MockHttpServletResponse response = get("/Encounter/" + ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		assertThat(response.getHeader("etag"), notNullValue());
		
		String etagValue = response.getHeader("etag");
		
		response = get("/Encounter/" + ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).ifNoneMatchHeader(etagValue).go();
		
		assertThat(response, isOk());
		assertThat(response, statusEquals(HttpStatus.NOT_MODIFIED));
	}
	
	private Set<ResourceType> getEncounterWithMedicationRequestsValidResourceTypes() {
		Set<ResourceType> validTypes = new HashSet<>();
		
		validTypes.add(ResourceType.Encounter);
		validTypes.add(ResourceType.MedicationRequest);
		validTypes.add(ResourceType.MedicationDispense);
		
		return validTypes;
	}
}
