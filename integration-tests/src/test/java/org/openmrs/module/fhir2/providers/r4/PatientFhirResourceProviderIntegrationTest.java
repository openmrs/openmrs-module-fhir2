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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.BaseFhirIntegrationTest;
import org.openmrs.module.fhir2.FhirConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class PatientFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<PatientFhirResourceProvider, Patient> {
	
	private static final String[] PATIENT_SEARCH_DATA_FILES = {
	        "org/openmrs/module/fhir2/api/dao/impl/FhirPatientDaoImplTest_initial_data.xml",
	        "org/openmrs/module/fhir2/api/dao/impl/FhirPatientDaoImplTest_address_data.xml" };
	
	private static final String JSON_CREATE_PATIENT_DOCUMENT = "org/openmrs/module/fhir2/providers/PatientWebTest_create.json";
	
	private static final String XML_CREATE_PATIENT_DOCUMENT = "org/openmrs/module/fhir2/providers/PatientWebTest_create.xml";
	
	private static final String JSON_PATCH_PATIENT_PATH = "org/openmrs/module/fhir2/providers/Patient_json_patch.json";
	
	private static final String JSON_MERGE_PATCH_PATIENT_PATH = "org/openmrs/module/fhir2/providers/Patient_patch.json";
	
	private static final String XML_PATCH_PATIENT_PATH = "org/openmrs/module/fhir2/providers/Patient_xmlpatch.xml";
	
	private static final String PATIENT_UUID = "30e2aa2a-4ed1-415d-84c5-ba29016c14b7";
	
	private static final String WRONG_PATIENT_UUID = "f090747b-459b-4a13-8c1b-c0567d8aeb63";
	
	private static final String PATIENT_UUID_2 = "ca17fcc5-ec96-487f-b9ea-42973c8973e3";
	
	private static final String OBSERVATION_UUID_1 = "99b92980-db62-40cd-8bca-733357c48126";
	
	private static final String OBSERVATION_UUID_2 = "f6ec1267-8eac-415f-a3f0-e47be2c8bb67";
	
	private static final String OBSERVATION_UUID_3 = "be48cdcb-6a76-47e3-9f2e-2635032f3a9a";
	
	private static final String OBSERVATION_UUID_4 = "1ce473c8-3fac-440d-9f92-e10facab194f";
	
	private static final String OBSERVATION_UUID_5 = "b6521c32-47b6-47da-9c6f-3673ddfb74f9";
	
	private static final String OBSERVATION_UUID_6 = "2ed1e57d-9f18-41d3-b067-2eeaf4b30fb0";
	
	private static final String OBSERVATION_UUID_7 = "2f616900-5e7c-4667-9a7f-dcb260abf1de";
	
	private static final String OBSERVATION_UUID_8 = "39fb7f47-e80a-4056-9285-bd798be13c63";
	
	private static final String OBSERVATION_UUID_9 = "e26cea2c-1b9f-4afe-b211-f3ef6c88af6f";
	
	private static final String ENCOUNTER_UUID_1 = "e403fafb-e5e4-42d0-9d11-4f52e89d148c";
	
	private static final String ENCOUNTER_UUID_2 = "6519d653-393b-4118-9c83-a3715b82d4ac";
	
	private static final String ENCOUNTER_UUID_3 = "eec646cb-c847-45a7-98bc-91c8c4f70add";
	
	private static final String MEDICATION_REQUEST_UUID_1 = "e1f95924-697a-11e3-bd76-0800271c1b75";
	
	private static final String MEDICATION_REQUEST_UUID_2 = "921de0a3-05c4-444a-be03-e01b4c4b9142";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private PatientFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		
		for (String search_data : PATIENT_SEARCH_DATA_FILES) {
			executeDataSet(search_data);
		}
	}
	
	@Test
	public void shouldReturnExistingPatientAsJson() throws Exception {
		MockHttpServletResponse response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Patient patient = readResponse(response);
		
		assertThat(patient, notNullValue());
		assertThat(patient.getIdElement().getIdPart(), equalTo(PATIENT_UUID));
		assertThat(patient, validResource());
	}
	
	@Test
	public void shouldReturnNotFoundWhenPatientNotFoundAsJson() throws Exception {
		MockHttpServletResponse response = get("/Patient/" + WRONG_PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnExistingPatientAsXML() throws Exception {
		MockHttpServletResponse response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Patient patient = readResponse(response);
		
		assertThat(patient, notNullValue());
		assertThat(patient.getIdElement().getIdPart(), equalTo(PATIENT_UUID));
		assertThat(patient, validResource());
	}
	
	@Test
	public void shouldReturnNotFoundWhenPatientNotFoundAsXML() throws Exception {
		MockHttpServletResponse response = get("/Patient/" + WRONG_PATIENT_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnPersonAttributesAsExtensions() throws Exception {
		MockHttpServletResponse response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Patient patient = readResponse(response);
		
		assertThat(patient.hasExtension(), is(true));
		assertThat(patient, hasProperty("extension"));
		
		Extension personAttributeExtension = patient.getExtension().get(0);
		
		assertThat(personAttributeExtension, hasProperty("url"));
		assertThat(personAttributeExtension.getUrl(), equalTo(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE));
		assertThat(personAttributeExtension, hasProperty("extension"));
		assertThat(personAttributeExtension.getExtension(), hasSize(2));
		assertThat(personAttributeExtension.getExtensionsByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_TYPE),
		    notNullValue());
		assertThat(personAttributeExtension.getExtensionsByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE),
		    notNullValue());
		
		//Filtering for extensions of PersonAttributes
		List<Extension> personAttributeExtensions = patient.getExtension().stream()
		        .filter(ext -> ext.getUrl().equals(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE))
		        .collect(Collectors.toList());
		
		assertThat(personAttributeExtensions, hasSize(4));
	}
	
	@Test
	public void shouldCreateNewPatientAsJson() throws Exception {
		// read JSON record
		String jsonPatient;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_PATIENT_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonPatient = inputStreamToString(is, UTF_8);
		}
		
		// create patient
		MockHttpServletResponse response = post("/Patient").accept(FhirMediaTypes.JSON).jsonContent(jsonPatient).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getHeader("Location"), containsString("/Patient/"));
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Patient patient = readResponse(response);
		
		assertThat(patient, notNullValue());
		assertThat(patient.getIdElement().getIdPart(), notNullValue());
		assertThat(patient.getName().get(0).getGiven().get(0).toString(), equalToIgnoringCase("Adam"));
		assertThat(patient.getName().get(0).getFamily(), equalToIgnoringCase("John"));
		assertThat(patient.getGender(), equalTo(Enumerations.AdministrativeGender.MALE));
		
		Date birthDate = Date.from(LocalDate.of(2004, 8, 12).atStartOfDay(ZoneId.systemDefault()).toInstant());
		assertThat(patient.getBirthDate(), equalTo(birthDate));
		
		assertThat(patient.getAddress().get(0).getCity(), equalTo("Kampala"));
		assertThat(patient.getAddress().get(0).getState(), equalTo("Mukono"));
		assertThat(patient.getAddress().get(0).getCountry(), equalTo("Uganda"));
		assertThat(patient, validResource());
		
		// try to get new patient
		response = get("/Patient/" + patient.getIdElement().getIdPart()).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Patient newPatient = readResponse(response);
		
		assertThat(newPatient.getId(), equalTo(patient.getId()));
	}
	
	@Test
	public void shouldCreateNewPatientAsXML() throws Exception {
		// read XML record
		String xmlPatient;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_CREATE_PATIENT_DOCUMENT)) {
			Objects.requireNonNull(is);
			xmlPatient = inputStreamToString(is, UTF_8);
		}
		
		// create patient
		MockHttpServletResponse response = post("/Patient").accept(FhirMediaTypes.XML).xmlContent(xmlPatient).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getHeader("Location"), containsString("/Patient/"));
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Patient patient = readResponse(response);
		
		assertThat(patient, notNullValue());
		assertThat(patient.getIdElement().getIdPart(), notNullValue());
		assertThat(patient.getName().get(0).getGiven().get(0).toString(), equalToIgnoringCase("Adam"));
		assertThat(patient.getName().get(0).getFamily(), equalToIgnoringCase("John"));
		assertThat(patient.getGender(), equalTo(Enumerations.AdministrativeGender.MALE));
		
		Date birthDate = Date.from(LocalDate.of(2004, 8, 12).atStartOfDay(ZoneId.systemDefault()).toInstant());
		assertThat(patient.getBirthDate(), equalTo(birthDate));
		
		assertThat(patient.getAddress().get(0).getCity(), equalTo("Kampala"));
		assertThat(patient.getAddress().get(0).getState(), equalTo("Mukono"));
		assertThat(patient.getAddress().get(0).getCountry(), equalTo("Uganda"));
		assertThat(patient, validResource());
		
		// try to get new patient
		response = get("/Patient/" + patient.getIdElement().getIdPart()).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		Patient newPatient = readResponse(response);
		
		assertThat(newPatient.getId(), equalTo(patient.getId()));
	}
	
	@Test
	public void shouldUpdateExistingPatientAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		Patient patient = readResponse(response);
		
		// verify condition we will change
		Date birthDate = DateUtils.truncate(new Date(), Calendar.DATE);
		assertThat(patient.getBirthDate(), not(equalTo(birthDate)));
		
		// update the existing record
		patient.setBirthDate(birthDate);
		
		// send the update to the server
		response = put("/Patient/" + PATIENT_UUID).jsonContent(toJson(patient)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read the updated record
		Patient updatedPatient = readResponse(response);
		
		assertThat(updatedPatient, notNullValue());
		assertThat(updatedPatient.getIdElement().getIdPart(), equalTo(PATIENT_UUID));
		assertThat(updatedPatient.getBirthDate(), equalTo(birthDate));
		assertThat(patient, validResource());
		
		// double-check the record returned via get
		response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		Patient reReadPatient = readResponse(response);
		
		assertThat(reReadPatient.getBirthDate(), equalTo(birthDate));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchPatientIdAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		Patient patient = readResponse(response);
		
		// update the existing record
		patient.setId(WRONG_PATIENT_UUID);
		
		// send the update to the server
		response = put("/Patient/" + PATIENT_UUID).jsonContent(toJson(patient)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentPatientAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		Patient patient = readResponse(response);
		
		// update the existing record
		patient.setId(WRONG_PATIENT_UUID);
		
		// send the update to the server
		response = put("/Patient/" + WRONG_PATIENT_UUID).jsonContent(toJson(patient)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldUpdateExistingPatientAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.XML).go();
		Patient patient = readResponse(response);
		
		// verify condition we will change
		Date birthDate = DateUtils.truncate(new Date(), Calendar.DATE);
		assertThat(patient.getBirthDate(), not(equalTo(birthDate)));
		
		// update the existing record
		patient.setBirthDate(birthDate);
		
		// send the update to the server
		response = put("/Patient/" + PATIENT_UUID).xmlContent(toXML(patient)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read the updated record
		Patient updatedPatient = readResponse(response);
		
		assertThat(updatedPatient, notNullValue());
		assertThat(updatedPatient.getIdElement().getIdPart(), equalTo(PATIENT_UUID));
		assertThat(updatedPatient.getBirthDate(), equalTo(birthDate));
		assertThat(patient, validResource());
		
		// double-check the record returned via get
		response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.XML).go();
		Patient reReadPatient = readResponse(response);
		
		assertThat(reReadPatient.getBirthDate(), equalTo(birthDate));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchPatientIdAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.XML).go();
		Patient patient = readResponse(response);
		
		// update the existing record
		patient.setId(WRONG_PATIENT_UUID);
		
		// send the update to the server
		response = put("/Patient/" + PATIENT_UUID).xmlContent(toXML(patient)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentPatientAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.XML).go();
		Patient patient = readResponse(response);
		
		// update the existing record
		patient.setId(WRONG_PATIENT_UUID);
		
		// send the update to the server
		response = put("/Patient/" + WRONG_PATIENT_UUID).xmlContent(toXML(patient)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	public void shouldPatchExistingPatientUsingJsonMergePatch() throws Exception {
		String jsonPatientPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_MERGE_PATCH_PATIENT_PATH)) {
			Objects.requireNonNull(is);
			jsonPatientPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/Patient/" + PATIENT_UUID).jsonMergePatch(jsonPatientPatch)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response, notNullValue());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Patient patient = readResponse(response);
		
		assertThat(patient, notNullValue());
		assertThat(patient.getIdElement().getIdPart(), equalTo(PATIENT_UUID));
		assertThat(patient, validResource());
		assertThat(patient.getGender(), equalTo(Enumerations.AdministrativeGender.FEMALE));
	}
	
	@Test
	public void shouldPatchExistingResourceUsingJsonPatch() throws Exception {
		String jsonPatientPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_PATIENT_PATH)) {
			Objects.requireNonNull(is);
			jsonPatientPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/Patient/" + PATIENT_UUID).jsonPatch(jsonPatientPatch)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response, notNullValue());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Patient patient = readResponse(response);
		
		assertThat(patient, notNullValue());
		assertThat(patient.getIdElement().getIdPart(), equalTo(PATIENT_UUID));
		assertThat(patient, validResource());
		assertThat(patient.getGender(), equalTo(Enumerations.AdministrativeGender.FEMALE));
	}
	
	@Test
	public void shouldPatchExistingPatientUsingXmlPatch() throws Exception {
		String xmlPatientPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_PATCH_PATIENT_PATH)) {
			Objects.requireNonNull(is);
			xmlPatientPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/Patient/" + PATIENT_UUID).xmlPatch(xmlPatientPatch)
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response, notNullValue());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		
		Patient patient = readResponse(response);
		
		assertThat(patient, notNullValue());
		assertThat(patient.getIdElement().getIdPart(), equalTo(PATIENT_UUID));
		assertThat(patient, validResource());
		assertThat(patient.getGender(), equalTo(Enumerations.AdministrativeGender.FEMALE));
	}
	
	@Test
	public void shouldDeleteExistingPatient() throws Exception {
		MockHttpServletResponse response = delete("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, statusEquals(HttpStatus.GONE));
	}
	
	@Test
	public void shouldReturnNotFoundWhenDeletingNonExistentPatient() throws Exception {
		MockHttpServletResponse response = delete("/Patient/" + WRONG_PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldSearchForAllPatientsAsJson() throws Exception {
		MockHttpServletResponse response = get("/Patient").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Patient/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Patient.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldReturnSortedAndFilteredSearchResultsForPatientsAsJson() throws Exception {
		MockHttpServletResponse response = get("/Patient?family=Doe&_sort=given").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasResource(hasProperty("nameFirstRep", hasProperty("family", startsWith("Doe"))))));
		assertThat(entries,
		    containsInRelativeOrder(
		        hasResource(hasProperty("nameFirstRep", hasProperty("givenAsSingleString", containsString("Jean")))),
		        hasResource(hasProperty("nameFirstRep", hasProperty("givenAsSingleString", containsString("John"))))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldAllowNamedQueryForPatientsAsJson() throws Exception {
		MockHttpServletResponse response = get("/Patient?_query=openmrsPatients&q=Doe&_sort=given")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasResource(hasProperty("nameFirstRep", hasProperty("family", startsWith("Doe"))))));
		assertThat(entries,
		    containsInRelativeOrder(
		        hasResource(hasProperty("nameFirstRep", hasProperty("givenAsSingleString", containsString("Jean")))),
		        hasResource(hasProperty("nameFirstRep", hasProperty("givenAsSingleString", containsString("John"))))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldReturnCountForPatientAsJson() throws Exception {
		MockHttpServletResponse response = get("/Patient?family=Doe&_summary=count").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(3)));
		
	}
	
	@Test
	public void shouldSearchForAllPatientsAsXML() throws Exception {
		MockHttpServletResponse response = get("/Patient").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Patient/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Patient.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldReturnSortedAndFilteredSearchResultsForPatientsAsXML() throws Exception {
		MockHttpServletResponse response = get("/Patient?family=Doe&_sort=given").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasResource(hasProperty("nameFirstRep", hasProperty("family", startsWith("Doe"))))));
		assertThat(entries,
		    containsInRelativeOrder(
		        hasResource(hasProperty("nameFirstRep", hasProperty("givenAsSingleString", containsString("Jean")))),
		        hasResource(hasProperty("nameFirstRep", hasProperty("givenAsSingleString", containsString("John"))))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldAllowNamedQueryForPatientsAsXml() throws Exception {
		MockHttpServletResponse response = get("/Patient?_query=openmrsPatients&q=Doe&_sort=given")
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasResource(hasProperty("nameFirstRep", hasProperty("family", startsWith("Doe"))))));
		assertThat(entries,
		    containsInRelativeOrder(
		        hasResource(hasProperty("nameFirstRep", hasProperty("givenAsSingleString", containsString("Jean")))),
		        hasResource(hasProperty("nameFirstRep", hasProperty("givenAsSingleString", containsString("John"))))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldReturnCountForPatientAsXml() throws Exception {
		MockHttpServletResponse response = get("/Patient?family=Doe&_summary=count").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(3)));
		
	}
	
	@Test
	public void shouldReturnPatientEverythingAsJson() throws Exception {
		MockHttpServletResponse response = get("/Patient/ca17fcc5-ec96-487f-b9ea-42973c8973e3/$everything")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(15)));
		assertThat(result.getEntry(), hasSize(15));
		
		List<Bundle.BundleEntryComponent> entries = result.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/"))));
		
		assertThat(entries, hasCorrectResources(15, getValidResources()));
	}
	
	@Test
	public void shouldReturnForPatientEverythingWhenCountIsSpecifiedAsJson() throws Exception {
		MockHttpServletResponse response = get("/Patient/ca17fcc5-ec96-487f-b9ea-42973c8973e3/$everything?_count=5")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(15)));
		assertThat(result.getEntry(), hasSize(5));
		
		List<Bundle.BundleEntryComponent> entries = result.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/"))));
		
		assertThat(entries, hasCorrectResources(5, getValidResources()));
	}
	
	@Test
	public void shouldReturnPatientEverythingAsXml() throws Exception {
		MockHttpServletResponse response = get("/Patient/ca17fcc5-ec96-487f-b9ea-42973c8973e3/$everything")
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(15)));
		assertThat(result.getEntry(), hasSize(15));
		
		List<Bundle.BundleEntryComponent> entries = result.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/"))));
		
		assertThat(entries, hasCorrectResources(15, getValidResources()));
	}
	
	@Test
	public void shouldReturnForPatientEverythingWhenCountIsSpecifiedAsXml() throws Exception {
		MockHttpServletResponse response = get("/Patient/ca17fcc5-ec96-487f-b9ea-42973c8973e3/$everything?_count=5")
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(15)));
		assertThat(result.getEntry(), hasSize(5));
		
		List<Bundle.BundleEntryComponent> entries = result.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/"))));
		
		assertThat(entries, hasCorrectResources(5, getValidResources()));
	}
	
	@Test
	public void shouldReturnPatientTypeEverythingAsJson() throws Exception {
		MockHttpServletResponse response = get("/Patient/$everything").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(44)));
		assertThat(result.getEntry(), hasSize(44));
		
		List<Bundle.BundleEntryComponent> entries = result.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/"))));
		assertThat(entries, everyItem(hasResource(hasProperty("resourceType", in(getEverythingValidResourceTypes())))));
	}
	
	@Test
	public void shouldReturnForPatientTypeEverythingWhenCountIsSpecifiedAsJson() throws Exception {
		MockHttpServletResponse response = get("/Patient/$everything?_count=5").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(44)));
		assertThat(result.getEntry(), hasSize(5));
		
		List<Bundle.BundleEntryComponent> entries = result.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/"))));
		assertThat(entries, everyItem(hasResource(hasProperty("resourceType", in(getEverythingValidResourceTypes())))));
	}
	
	@Test
	public void shouldReturnPatientTypeEverythingAsXml() throws Exception {
		MockHttpServletResponse response = get("/Patient/$everything").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(44)));
		assertThat(result.getEntry(), hasSize(44));
		
		List<Bundle.BundleEntryComponent> entries = result.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/"))));
		assertThat(entries, everyItem(hasResource(hasProperty("resourceType", in(getEverythingValidResourceTypes())))));
	}
	
	@Test
	public void shouldReturnForPatientTypeEverythingWhenCountIsSpecifiedAsXml() throws Exception {
		MockHttpServletResponse response = get("/Patient/$everything?_count=5").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(44)));
		assertThat(result.getEntry(), hasSize(5));
		
		List<Bundle.BundleEntryComponent> entries = result.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/"))));
		assertThat(entries, everyItem(hasResource(hasProperty("resourceType", in(getEverythingValidResourceTypes())))));
	}
	
	@Test
	public void shouldReturnAnEtagHeaderWhenRetrievingAnExistingPatient() throws Exception {
		MockHttpServletResponse response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		
		assertThat(response.getHeader("etag"), notNullValue());
		assertThat(response.getHeader("etag"), startsWith("W/"));
		
		assertThat(response.getContentAsString(), notNullValue());
		
		Patient patient = readResponse(response);
		
		assertThat(patient, notNullValue());
		assertThat(patient.getMeta().getVersionId(), notNullValue());
		assertThat(patient, validResource());
	}
	
	@Test
	public void shouldReturnNotModifiedWhenRetrievingAnExistingPatientWithAnEtag() throws Exception {
		MockHttpServletResponse response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		assertThat(response.getHeader("etag"), notNullValue());
		
		String etagValue = response.getHeader("etag");
		
		response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).ifNoneMatchHeader(etagValue).go();
		
		assertThat(response, isOk());
		assertThat(response, statusEquals(HttpStatus.NOT_MODIFIED));
	}
	
	@Test
	public void shouldReturnAnUpdatedPatientWithNewEtagWhenRetrievingAnExistingPatientWithAnEtag() throws Exception {
		MockHttpServletResponse response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		assertThat(response.getHeader("etag"), notNullValue());
		
		String etagValue = response.getHeader("etag");
		
		Patient patient = readResponse(response);
		patient.setGender(Enumerations.AdministrativeGender.FEMALE);
		
		//send update to the server
		put("/Patient/" + PATIENT_UUID).jsonContent(toJson(patient)).accept(FhirMediaTypes.JSON).go();
		
		//send a new GET request, with the “If-None-Match” header specifying the ETag that we previously stored
		response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).ifNoneMatchHeader(etagValue).go();
		
		assertThat(response, isOk());
		assertThat(response, statusEquals(HttpStatus.OK));
	}
	
	private Set<String> getValidResources() {
		Set<String> validResources = new HashSet<>();
		validResources.add(PATIENT_UUID_2);
		validResources.add(OBSERVATION_UUID_1);
		validResources.add(OBSERVATION_UUID_2);
		validResources.add(OBSERVATION_UUID_3);
		validResources.add(OBSERVATION_UUID_4);
		validResources.add(OBSERVATION_UUID_5);
		validResources.add(OBSERVATION_UUID_6);
		validResources.add(OBSERVATION_UUID_7);
		validResources.add(OBSERVATION_UUID_8);
		validResources.add(OBSERVATION_UUID_9);
		validResources.add(ENCOUNTER_UUID_1);
		validResources.add(ENCOUNTER_UUID_2);
		validResources.add(ENCOUNTER_UUID_3);
		validResources.add(MEDICATION_REQUEST_UUID_1);
		validResources.add(MEDICATION_REQUEST_UUID_2);
		
		return validResources;
		
	}
	
	private Set<ResourceType> getEverythingValidResourceTypes() {
		Set<ResourceType> validTypes = new HashSet<>();
		
		validTypes.add(ResourceType.Patient);
		validTypes.add(ResourceType.Observation);
		validTypes.add(ResourceType.MedicationRequest);
		validTypes.add(ResourceType.Encounter);
		validTypes.add(ResourceType.DiagnosticReport);
		validTypes.add(ResourceType.AllergyIntolerance);
		validTypes.add(ResourceType.ServiceRequest);
		
		return validTypes;
	}
}
