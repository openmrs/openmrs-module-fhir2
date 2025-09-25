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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;
import static org.openmrs.module.fhir2.providers.r4.BaseUpsertFhirResourceProvider.GP_NAME_SUPPORTED_RESOURCES;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.module.fhir2.BaseFhirIntegrationTest;
import org.openmrs.module.fhir2.FhirConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

@Slf4j
public class MedicationFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<MedicationFhirResourceProvider, Medication> {
	
	private static final String MEDICATION_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_MEDICATION_UUID = "5c9d032b-6092-4052-93d2-a04202b98462";
	
	private static final String MEDICATION_CODE_UUID = "5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String MEDICATION_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirMedicationDaoImplTest_initial_data.xml";
	
	private static final String JSON_MERGE_PATCH_MEDICATION_PATH = "org/openmrs/module/fhir2/providers/Medication_patch.json";
	
	private static final String JSON_PATCH_MEDICATION_PATH = "org/openmrs/module/fhir2/providers/Medication_json_patch.json";
	
	private static final String XML_PATCH_MEDICATION_PATH = "org/openmrs/module/fhir2/providers/Medication_xml_patch.xml";
	
	private static final String JSON_CREATE_MEDICATION_DOCUMENT = "org/openmrs/module/fhir2/providers/MedicationWebTest_create.json";
	
	private static final String JSON_UPSERT_MEDICATION_DOCUMENT = "org/openmrs/module/fhir2/providers/MedicationWebTest_upsert.json";
	
	private static final String XML_CREATE_MEDICATION_DOCUMENT = "org/openmrs/module/fhir2/providers/MedicationWebTest_create.xml";
	
	private static final String XML_UPDATE_MEDICATION_DOCUMENT = "org/openmrs/module/fhir2/providers/MedicationWebTest_update.xml";
	
	private static final String JSON_UPDATE_MEDICATION_DOCUMENT = "org/openmrs/module/fhir2/providers/MedicationWebTest_update.json";
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	private MedicationFhirResourceProvider resourceProvider;
	
	@Autowired
	private ConceptService conceptService;
	
	@Autowired
	private AdministrationService adminService;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(MEDICATION_DATA_XML);
		//Clear the global property value if set.
		GlobalProperty gp = adminService.getGlobalPropertyObject(GP_NAME_SUPPORTED_RESOURCES);
		if (gp != null) {
			gp.setValue(null);
			adminService.saveGlobalProperty(gp);
		}
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
			jsonMedication = inputStreamToString(is, UTF_8);
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
		
	}
	
	@Test
	public void shouldCreateNewMedicationAsXml() throws Exception {
		String xmlMedication;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_CREATE_MEDICATION_DOCUMENT)) {
			Objects.requireNonNull(is);
			xmlMedication = inputStreamToString(is, UTF_8);
		}
		
		// create medication
		MockHttpServletResponse response = post("/Medication").accept(FhirMediaTypes.XML).xmlContent(xmlMedication).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Medication medication = readResponse(response);
		assertThat(medication, notNullValue());
		assertThat(medication.getStatus(), is(Medication.MedicationStatus.ACTIVE));
		assertThat(medication.getCode().getCodingFirstRep().getCode(), equalTo(MEDICATION_CODE_UUID));
		
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
			jsonMedication = inputStreamToString(is, UTF_8);
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
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchMedicationIdAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Medication/" + MEDICATION_UUID).accept(FhirMediaTypes.JSON).go();
		Medication medication = readResponse(response);
		
		// update the existing record
		medication.setId(WRONG_MEDICATION_UUID);
		
		// send the update to the server
		response = put("/Medication/" + MEDICATION_UUID).jsonContent(toJson(medication)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentMedicationAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Medication/" + MEDICATION_UUID).accept(FhirMediaTypes.JSON).go();
		Medication medication = readResponse(response);
		
		// update the existing record
		medication.setId(WRONG_MEDICATION_UUID);
		
		// send the update to the server
		response = put("/Medication/" + WRONG_MEDICATION_UUID).jsonContent(toJson(medication)).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
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
			xmlMedication = inputStreamToString(is, UTF_8);
		}
		
		//Update
		response = put("/Medication/" + MEDICATION_UUID).xmlContent(xmlMedication).accept(FhirMediaTypes.XML).go();
		
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
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchMedicationIdAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Medication/" + MEDICATION_UUID).accept(FhirMediaTypes.XML).go();
		Medication medication = readResponse(response);
		
		// update the existing record
		medication.setId(WRONG_MEDICATION_UUID);
		
		// send the update to the server
		response = put("/Medication/" + MEDICATION_UUID).xmlContent(toXML(medication)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentMedicationAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Medication/" + MEDICATION_UUID).accept(FhirMediaTypes.XML).go();
		Medication medication = readResponse(response);
		
		// update the existing record
		medication.setId(WRONG_MEDICATION_UUID);
		
		// send the update to the server
		response = put("/Medication/" + WRONG_MEDICATION_UUID).xmlContent(toXML(medication)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldPatchExistingMedicationUsingJsonMergePatch() throws Exception {
		String jsonMedicationPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_MERGE_PATCH_MEDICATION_PATH)) {
			Objects.requireNonNull(is);
			jsonMedicationPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/Medication/" + MEDICATION_UUID).jsonMergePatch(jsonMedicationPatch)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Medication medication = readResponse(response);
		
		assertThat(medication, notNullValue());
		assertThat(medication.getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
		assertThat(medication, validResource());
		assertThat(medication.getIngredientFirstRep().getItemCodeableConcept().getCodingFirstRep().getCode(),
		    equalTo("5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
	}
	
	@Test
	public void shouldPatchExistingMedicationUsingJsonPatch() throws Exception {
		String jsonMedicationPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_MEDICATION_PATH)) {
			Objects.requireNonNull(is);
			jsonMedicationPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/Medication/" + MEDICATION_UUID).jsonPatch(jsonMedicationPatch)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Medication medication = readResponse(response);
		
		assertThat(medication, notNullValue());
		assertThat(medication.getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
		assertThat(medication, validResource());
		assertThat(medication.getIngredientFirstRep().getItemCodeableConcept().getCodingFirstRep().getCode(),
		    equalTo("5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		
	}
	
	@Test
	public void shouldPatchExistingMedicationUsingXmlPatch() throws Exception {
		String xmlMedicationPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_PATCH_MEDICATION_PATH)) {
			Objects.requireNonNull(is);
			xmlMedicationPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/Medication/" + MEDICATION_UUID).xmlPatch(xmlMedicationPatch)
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Medication medication = readResponse(response);
		
		assertThat(medication, notNullValue());
		assertThat(medication.getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
		assertThat(medication, validResource());
		assertThat(medication.getIngredientFirstRep().getItemCodeableConcept().getCodingFirstRep().getCode(),
		    equalTo("5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
	}
	
	@Test
	public void shouldDeleteExistingMedication() throws Exception {
		MockHttpServletResponse response = delete("/Medication/" + MEDICATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		response = get("/Medication/" + MEDICATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, statusEquals(HttpStatus.GONE));
	}
	
	@Test
	public void shouldReturnNotFoundWhenDeletingNonExistentMedication() throws Exception {
		MockHttpServletResponse response = delete("/Medication/" + WRONG_MEDICATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
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
		    hasProperty("id", startsWith("http://localhost/ws/fhir2/R4/Medication/1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")))));
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
		    hasProperty("id", startsWith("http://localhost/ws/fhir2/R4/Medication/1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldReturnCountForMedicationAsJson() throws Exception {
		MockHttpServletResponse response = get("/Medication?_summary=count").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(4)));
	}
	
	@Test
	public void shouldReturnCountForMedicationAsXml() throws Exception {
		MockHttpServletResponse response = get("/Medication?_summary=count").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(4)));
	}
	
	@Test
	public void shouldReturnAnEtagHeaderWhenRetrievingAnExistingMedication() throws Exception {
		MockHttpServletResponse response = get("/Medication/" + MEDICATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		
		assertThat(response.getHeader("etag"), notNullValue());
		assertThat(response.getHeader("etag"), startsWith("W/"));
		
		assertThat(response.getContentAsString(), notNullValue());
		
		Medication medication = readResponse(response);
		
		assertThat(medication, notNullValue());
		assertThat(medication.getMeta().getVersionId(), notNullValue());
		assertThat(medication, validResource());
	}
	
	@Test
	public void shouldReturnNotModifiedWhenRetrievingAnExistingMedicationWithAnEtag() throws Exception {
		MockHttpServletResponse response = get("/Medication/" + MEDICATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		assertThat(response.getHeader("etag"), notNullValue());
		
		String etagValue = response.getHeader("etag");
		
		response = get("/Medication/" + MEDICATION_UUID).accept(FhirMediaTypes.JSON).ifNoneMatchHeader(etagValue).go();
		
		assertThat(response, isOk());
		assertThat(response, statusEquals(HttpStatus.NOT_MODIFIED));
	}
	
	@Test
	public void shouldCreateANewMedicationWithTheProvidedUuid() throws Exception {
		GlobalProperty gp = adminService.getGlobalPropertyObject(GP_NAME_SUPPORTED_RESOURCES);
		if (gp == null) {
			gp = new GlobalProperty(GP_NAME_SUPPORTED_RESOURCES);
			gp.setDatatypeClassname(FreeTextDatatype.class.getName());
		}
		gp.setValue("Medication");
		adminService.saveGlobalProperty(gp);
		final String uuid = "105bf9c4-8fef-11f0-b303-0242ac180002";
		assertThat(conceptService.getDrug(uuid), nullValue());
		String jsonMedication;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPSERT_MEDICATION_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonMedication = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = put("/Medication/" + uuid).accept(FhirMediaTypes.JSON).jsonContent(jsonMedication)
		        .go();
		
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		Medication medication = readResponse(response);
		assertThat(medication, notNullValue());
		assertThat(medication.getIdElement().getIdPart(), is(uuid));
		assertThat(medication.getStatus(), is(Medication.MedicationStatus.ACTIVE));
		assertThat(medication.getCode().getCodingFirstRep().getCode(), equalTo(MEDICATION_CODE_UUID));
		
	}
	
	@Test
	public void shouldFailToCreateANewMedicationWithTheProvidedUuidIfUpsertIsNotEnabled() throws Exception {
		final String uuid = "105bf9c4-8fef-11f0-b303-0242ac180002";
		assertThat(conceptService.getDrug(uuid), nullValue());
		String jsonMedication;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPSERT_MEDICATION_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonMedication = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = put("/Medication/" + uuid).accept(FhirMediaTypes.JSON).jsonContent(jsonMedication)
		        .go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		OperationOutcome outcome = readOperationOutcome(response);
		OperationOutcomeIssueComponent issue = outcome.getIssue().get(0);
		assertThat(issue.getDiagnostics(), is("Resource of type Medication with ID " + uuid + " is not known"));
	}
}
