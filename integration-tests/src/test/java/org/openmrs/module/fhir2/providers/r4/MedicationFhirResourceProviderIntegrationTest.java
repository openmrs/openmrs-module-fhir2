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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.codesystems.MedicationStatus;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.BaseFhirIntegrationTest;
import org.openmrs.module.fhir2.FhirConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

@Slf4j
public class MedicationFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<MedicationFhirResourceProvider, Medication> {
	
	private static final String MEDICATION_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_MEDICATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String MEDICATION_CODE_UUID = "5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String MEDICATION_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirMedicationDaoImplTest_initial_data.xml";
	
	private static final String JSON_CREATE_MEDICATION_DOCUMENT = "org/openmrs/module/fhir2/providers/MedicationResourceWebTest_create.json";
	
	private static final String XML_CREATE_MEDICATION_DOCUMENT = "org/openmrs/module/fhir2/providers/MedicationWebTest_create.xml";
	
	private static final String XML_UPDATE_MEDICATION_DOCUMENT = "org/openmrs/module/fhir2/providers/MedicationWebTest_update.xml";
	
	private static final String JSON_UPDATE_MEDICATION_DOCUMENT = "org/openmrs/module/fhir2/providers/MedicationResourceWebTest_update.json";
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	private MedicationFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(MEDICATION_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingMedicationAsJson() throws Exception {
		MockHttpServletResponse response = get("/Medication/" + MEDICATION_UUID)
		        .accept(BaseFhirIntegrationTest.FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Medication medication = readResponse(response);
		
		assertThat(medication, notNullValue());
		assertThat(medication.getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
		assertThat(medication, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingMedicationAsJson() throws Exception {
		MockHttpServletResponse response = get("/Medication/" + WRONG_MEDICATION_UUID)
		        .accept(BaseFhirIntegrationTest.FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldReturnExistingMedicationAsXML() throws Exception {
		MockHttpServletResponse response = get("/Medication/" + MEDICATION_UUID)
		        .accept(BaseFhirIntegrationTest.FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Medication medication = readResponse(response);
		
		assertThat(medication, notNullValue());
		assertThat(medication.getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
		assertThat(medication, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingMedicationAsXML() throws Exception {
		MockHttpServletResponse response = get("/Medication/" + WRONG_MEDICATION_UUID)
		        .accept(BaseFhirIntegrationTest.FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldCreateNewMedicationAsJson() throws Exception {
		// read JSON record
		String jsonMedication;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_MEDICATION_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonMedication = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		// create medication
		MockHttpServletResponse response = post("/Medication").accept(FhirMediaTypes.JSON).jsonContent(jsonMedication).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Medication medication = readResponse(response);
		assertThat(medication, notNullValue());
		assertThat(medication.getStatus(), is(Medication.MedicationStatus.ACTIVE));
		assertThat(medication.getCode().getCodingFirstRep().getCode(), equalTo(MEDICATION_CODE_UUID));
		
		// try to get new medication
		response = get(medication.getId()).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Medication newMedication = readResponse(response);
		
		assertThat(newMedication.getId(), equalTo(medication.getId()));
		assertThat(newMedication.getStatus(), equalTo(medication.getStatus()));
		
	}
	
	@Test
	public void shouldCreateNewMedicationAsXml() throws Exception {
		String xmlMedication;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_CREATE_MEDICATION_DOCUMENT)) {
			Objects.requireNonNull(is);
			xmlMedication = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		// create medication
		MockHttpServletResponse response = post("/Medication").accept(FhirMediaTypes.XML).xmlContext(xmlMedication).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Medication medication = readResponse(response);
		assertThat(medication, notNullValue());
		assertThat(medication.getStatus(), is(Medication.MedicationStatus.ACTIVE));
		assertThat(medication.getCode().getCodingFirstRep().getCode(), equalTo(MEDICATION_CODE_UUID));
		
		// try to get new medication
		response = get(medication.getId()).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		Medication newMedication = readResponse(response);
		
		assertThat(newMedication.getId(), equalTo(medication.getId()));
		assertThat(newMedication.getStatus(), equalTo(medication.getStatus()));
		
	}
	
	@Test
	public void shouldUpdateExistingMedicationAsJson() throws Exception {
		//Before update
		MockHttpServletResponse response = get("/Medication/" + MEDICATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Medication medication = readResponse(response);
		Extension medExtension = medication.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE);
		Extension strengthExtension = medExtension.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#strength");
		
		assertThat(medication, notNullValue());
		assertThat(medication, validResource());
		assertThat(medication.getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
		assertThat(strengthExtension.getValue().toString(), equalTo("200mg"));
		
		// Get existing medication with updated medication strength
		String jsonMedication;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_MEDICATION_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonMedication = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		//Update
		response = put("/Medication/" + MEDICATION_UUID).jsonContent(jsonMedication).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read updated record
		medication = readResponse(response);
		medExtension = medication.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE);
		strengthExtension = medExtension.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#strength");
		
		assertThat(medication, notNullValue());
		assertThat(medication.getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
		assertThat(medication.getStatus(), is(Medication.MedicationStatus.ACTIVE));
		assertThat(strengthExtension.getValue().toString(), equalTo("800mg"));
		assertThat(medication, validResource());
		
		// Double-check via get
		response = get("/Medication/" + MEDICATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		Medication updatedMedication = readResponse(response);
		medExtension = updatedMedication.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE);
		strengthExtension = medExtension.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#strength");
		
		assertThat(updatedMedication, validResource());
		assertThat(updatedMedication, notNullValue());
		assertThat(updatedMedication.getStatus(), is(Medication.MedicationStatus.ACTIVE));
		assertThat(strengthExtension.getValue().toString(), equalTo("800mg"));
		
	}
	
	@Test
	public void shouldUpdateExistingMedicationAsXml() throws Exception {
		//Before update
		MockHttpServletResponse response = get("/Medication/" + MEDICATION_UUID)
		        .accept(BaseFhirIntegrationTest.FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Medication medication = readResponse(response);
		Extension medExtension = medication.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE);
		Extension strengthExtension = medExtension.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#strength");
		
		assertThat(medication, notNullValue());
		assertThat(medication, validResource());
		assertThat(medication.getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
		assertThat(strengthExtension.getValue().toString(), equalTo("200mg"));
		
		// Get existing medication with updated medication strength
		String xmlMedication;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_UPDATE_MEDICATION_DOCUMENT)) {
			Objects.requireNonNull(is);
			xmlMedication = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		//Update
		response = put("/Medication/" + MEDICATION_UUID).xmlContext(xmlMedication).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read updated record
		medication = readResponse(response);
		medExtension = medication.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE);
		strengthExtension = medExtension.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#strength");
		
		assertThat(medication, notNullValue());
		assertThat(medication.getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
		assertThat(medication.getStatus(), is(Medication.MedicationStatus.ACTIVE));
		assertThat(strengthExtension.getValue().toString(), equalTo("800mg"));
		assertThat(medication, validResource());
		
		// Double-check via get
		response = get("/Medication/" + MEDICATION_UUID).accept(FhirMediaTypes.XML).go();
		
		Medication updatedMedication = readResponse(response);
		medExtension = updatedMedication.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE);
		strengthExtension = medExtension.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_MEDICINE + "#strength");
		
		assertThat(updatedMedication, validResource());
		assertThat(updatedMedication, notNullValue());
		assertThat(updatedMedication.getStatus(), is(Medication.MedicationStatus.ACTIVE));
		assertThat(strengthExtension.getValue().toString(), equalTo("800mg"));
		
	}
	
	@Test
	public void shouldSearchForExistingMedicationsAsJson() throws Exception {
		MockHttpServletResponse response = get("/Medication").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Medication/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Medication.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		response = get(String.format("/Medication?_id=%s", MEDICATION_UUID)).accept(FhirMediaTypes.JSON).go();
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		entries = results.getEntry();
		assertThat(entries, everyItem(hasResource(
		    hasProperty("id", is("http://localhost/ws/fhir2/R4/Medication/1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldSearchForExistingMedicationsAsXml() throws Exception {
		MockHttpServletResponse response = get("/Medication").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Medication/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Medication.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		response = get(String.format("/Medication?_id=%s", MEDICATION_UUID)).accept(FhirMediaTypes.XML).go();
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		entries = results.getEntry();
		assertThat(entries, everyItem(hasResource(
		    hasProperty("id", is("http://localhost/ws/fhir2/R4/Medication/1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
}
