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
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class PatientFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<PatientFhirResourceProvider, Patient> {
	
	private static final String[] PATIENT_SEARCH_DATA_FILES = {
	        "org/openmrs/module/fhir2/api/dao/impl/FhirPatientDaoImplTest_initial_data.xml",
	        "org/openmrs/module/fhir2/api/dao/impl/FhirPatientDaoImplTest_address_data.xml" };
	
	private static final String JSON_CREATE_PATIENT_DOCUMENT = "org/openmrs/module/fhir2/providers/PatientWebTest_create.json";
	
	private static final String XML_CREATE_PATIENT_DOCUMENT = "org/openmrs/module/fhir2/providers/PatientWebTest_create.xml";
	
	private static final String PATIENT_UUID = "30e2aa2a-4ed1-415d-84c5-ba29016c14b7";
	
	private static final String WRONG_PATIENT_UUID = "f090747b-459b-4a13-8c1b-c0567d8aeb63";
	
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
	public void shouldCreateNewPatientAsJson() throws Exception {
		// read JSON record
		String jsonPatient;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_PATIENT_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonPatient = IOUtils.toString(is, StandardCharsets.UTF_8);
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
			xmlPatient = IOUtils.toString(is, StandardCharsets.UTF_8);
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
}
