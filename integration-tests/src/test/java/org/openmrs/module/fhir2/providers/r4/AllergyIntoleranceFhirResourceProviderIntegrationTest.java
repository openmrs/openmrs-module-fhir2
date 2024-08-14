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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class AllergyIntoleranceFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<AllergyIntoleranceFhirResourceProvider, AllergyIntolerance> {
	
	private static final String ALLERGY_INTOLERANCE_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirAllergyIntoleranceDaoImplTest_initial_data.xml";
	
	private static final String JSON_MERGE_PATCH_ALLERGY_PATH = "org/openmrs/module/fhir2/providers/AllergyIntolerance_json_merge_patch.json";
	
	private static final String JSON_PATCH_ALLERGY_PATH = "org/openmrs/module/fhir2/providers/AllergyIntolerance_json_patch.json";
	
	private static final String XML_PATCH_ALLERGY_PATH = "org/openmrs/module/fhir2/providers/AllergyIntolerance_xmlpatch.xml";
	
	private static final String JSON_CREATE_ALLERGY_DOCUMENT = "org/openmrs/module/fhir2/providers/AllergyIntoleranceWebTest_create.json";
	
	private static final String XML_CREATE_ALLERGY_DOCUMENT = "org/openmrs/module/fhir2/providers/AllergyIntoleranceWebTest_create.xml";
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String UNKNOWN_ALLERGY_UUID = "1080AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private AllergyIntoleranceFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(ALLERGY_INTOLERANCE_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingAllergyAsJson() throws Exception {
		MockHttpServletResponse response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		AllergyIntolerance allergyIntolerance = readResponse(response);
		
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
		assertThat(allergyIntolerance, validResource());
	}
	
	@Test
	public void shouldReturnNotFoundWhenAllergyNotFoundAsJson() throws Exception {
		MockHttpServletResponse response = get("/AllergyIntolerance/" + UNKNOWN_ALLERGY_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnExistingAllergyAsXML() throws Exception {
		MockHttpServletResponse response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		AllergyIntolerance allergyIntolerance = readResponse(response);
		
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
		assertThat(allergyIntolerance, validResource());
	}
	
	@Test
	public void shouldReturnNotFoundWhenAllergyNotFoundAsXML() throws Exception {
		MockHttpServletResponse response = get("/AllergyIntolerance/" + UNKNOWN_ALLERGY_UUID).accept(FhirMediaTypes.XML)
		        .go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldCreateNewAllergyAsJson() throws Exception {
		// read JSON record
		String jsonAllergy;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_ALLERGY_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonAllergy = inputStreamToString(is, UTF_8);
		}
		
		// create allergy
		MockHttpServletResponse response = post("/AllergyIntolerance").accept(FhirMediaTypes.JSON).jsonContent(jsonAllergy)
		        .go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		AllergyIntolerance allergy = readResponse(response);
		
		assertThat(allergy, notNullValue());
		assertThat(allergy.getPatient().getReferenceElement().getIdPart(), equalTo("da7f524f-27ce-4bb2-86d6-6d1d05312bd5"));
		assertThat(allergy.getRecorder().getReferenceElement().getIdPart(), equalTo("c98a1558-e131-11de-babe-001e378eb67e"));
		assertThat(allergy.getCategory().get(0).getCode(), equalTo("medication"));
		assertThat(allergy.hasReaction(), is(true));
		assertThat(allergy.getReactionFirstRep().hasSeverity(), is(true));
		assertThat(allergy.getReactionFirstRep().getSeverity().toCode(), equalTo("severe"));
		assertThat(allergy.getReactionFirstRep().getManifestationFirstRep().getCodingFirstRep().getCode(),
		    equalTo("5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		assertThat(allergy.getReactionFirstRep().getManifestationFirstRep().getText(), equalTo("manifestation text"));
		assertThat(allergy.getCode().getCodingFirstRep().getCode(), equalTo("5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		assertThat(allergy.getClinicalStatus().getCodingFirstRep().getSystem(),
		    equalTo("http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical"));
		assertThat(allergy.getClinicalStatus().getCodingFirstRep().getCode(), equalTo("active"));
		assertThat(allergy.getClinicalStatus().getText(), equalTo("Active"));
		assertThat(allergy.getVerificationStatus().getCodingFirstRep().getSystem(),
		    equalTo("http://terminology.hl7.org/CodeSystem/allergyintolerance-verification"));
		assertThat(allergy.getVerificationStatus().getCodingFirstRep().getCode(), equalTo("confirmed"));
		assertThat(allergy.getVerificationStatus().getText(), equalTo("Confirmed"));
		assertThat(allergy.getType().toCode(), equalTo("allergy"));
		assertThat(allergy, validResource());
		
		// try to get new allergy
		response = get("/AllergyIntolerance/" + allergy.getIdElement().getIdPart()).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		AllergyIntolerance newAllergy = readResponse(response);
		
		assertThat(newAllergy.getId(), equalTo(allergy.getId()));
	}
	
	@Test
	public void shouldCreateNewAllergyAsXML() throws Exception {
		// read XML record
		String jsonAllergy;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_CREATE_ALLERGY_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonAllergy = inputStreamToString(is, UTF_8);
		}
		
		// create allergy
		MockHttpServletResponse response = post("/AllergyIntolerance").accept(FhirMediaTypes.XML).xmlContent(jsonAllergy)
		        .go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		AllergyIntolerance allergy = readResponse(response);
		
		assertThat(allergy, notNullValue());
		assertThat(allergy.getPatient().getReferenceElement().getIdPart(), equalTo("da7f524f-27ce-4bb2-86d6-6d1d05312bd5"));
		assertThat(allergy.getRecorder().getReferenceElement().getIdPart(), equalTo("c98a1558-e131-11de-babe-001e378eb67e"));
		assertThat(allergy.getCategory().get(0).getCode(), equalTo("medication"));
		assertThat(allergy.hasReaction(), equalTo(true));
		assertThat(allergy.getReactionFirstRep().hasSeverity(), equalTo(true));
		assertThat(allergy.getReactionFirstRep().getSeverity().toCode(), equalTo("severe"));
		assertThat(allergy.getReactionFirstRep().getManifestationFirstRep().getCodingFirstRep().getCode(),
		    equalTo("5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		assertThat(allergy.getReactionFirstRep().getManifestationFirstRep().getText(), equalTo("manifestation text"));
		assertThat(allergy.getCode().getCodingFirstRep().getCode(), equalTo("5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		assertThat(allergy.getClinicalStatus().getCodingFirstRep().getSystem(),
		    equalTo("http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical"));
		assertThat(allergy.getClinicalStatus().getCodingFirstRep().getCode(), equalTo("active"));
		assertThat(allergy.getClinicalStatus().getText(), equalTo("Active"));
		assertThat(allergy.getVerificationStatus().getCodingFirstRep().getSystem(),
		    equalTo("http://terminology.hl7.org/CodeSystem/allergyintolerance-verification"));
		assertThat(allergy.getVerificationStatus().getCodingFirstRep().getCode(), equalTo("confirmed"));
		assertThat(allergy.getVerificationStatus().getText(), equalTo("Confirmed"));
		assertThat(allergy.getType().toCode(), equalTo("allergy"));
		assertThat(allergy, validResource());
		
		// try to get new allergy
		response = get("/AllergyIntolerance/" + allergy.getIdElement().getIdPart()).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		AllergyIntolerance newAllergy = readResponse(response);
		
		assertThat(newAllergy.getId(), equalTo(allergy.getId()));
	}
	
	@Test
	public void shouldUpdateExistingAllergyAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.JSON).go();
		AllergyIntolerance allergy = readResponse(response);
		
		// update the existing record
		Enumeration<AllergyIntolerance.AllergyIntoleranceCategory> category = new Enumeration<>(
		        new AllergyIntolerance.AllergyIntoleranceCategoryEnumFactory());
		category.setValue(AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT);
		allergy.getCategory().set(0, category);
		
		// send the update to the server
		response = put("/AllergyIntolerance/" + ALLERGY_UUID).jsonContent(toJson(allergy)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read the updated record
		AllergyIntolerance updatedAllergy = readResponse(response);
		
		assertThat(updatedAllergy, notNullValue());
		assertThat(updatedAllergy.getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
		assertThat(updatedAllergy.getCategory().get(0).getCode(), equalTo("environment"));
		assertThat(updatedAllergy, validResource());
		
		// double-check the record returned via get
		response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.JSON).go();
		AllergyIntolerance reReadAllergy = readResponse(response);
		
		assertThat(reReadAllergy.getCategory().get(0).getCode(), equalTo("environment"));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchAllergyIdAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.JSON).go();
		AllergyIntolerance allergy = readResponse(response);
		
		// update the existing record
		allergy.setId(UNKNOWN_ALLERGY_UUID);
		
		// send the update to the server
		response = put("/AllergyIntolerance/" + ALLERGY_UUID).jsonContent(toJson(allergy)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentAllergyAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.JSON).go();
		AllergyIntolerance allergy = readResponse(response);
		
		// update the existing record
		allergy.setId(UNKNOWN_ALLERGY_UUID);
		
		// send the update to the server
		response = put("/AllergyIntolerance/" + UNKNOWN_ALLERGY_UUID).jsonContent(toJson(allergy))
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldUpdateExistingAllergyAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.XML).go();
		AllergyIntolerance allergy = readResponse(response);
		
		// update the existing record
		Enumeration<AllergyIntolerance.AllergyIntoleranceCategory> category = new Enumeration<>(
		        new AllergyIntolerance.AllergyIntoleranceCategoryEnumFactory());
		category.setValue(AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT);
		allergy.getCategory().set(0, category);
		
		// send the update to the server
		response = put("/AllergyIntolerance/" + ALLERGY_UUID).xmlContent(toXML(allergy)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read the updated record
		AllergyIntolerance updatedAllergy = readResponse(response);
		
		assertThat(updatedAllergy, notNullValue());
		assertThat(updatedAllergy.getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
		assertThat(updatedAllergy.getCategory().get(0).getCode(), equalTo("environment"));
		assertThat(updatedAllergy, validResource());
		
		// double-check the record returned via get
		response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.XML).go();
		AllergyIntolerance reReadAllergy = readResponse(response);
		
		assertThat(reReadAllergy.getCategory().get(0).getCode(), equalTo("environment"));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchAllergyIdAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.XML).go();
		AllergyIntolerance allergy = readResponse(response);
		
		// update the existing record
		allergy.setId(UNKNOWN_ALLERGY_UUID);
		
		// send the update to the server
		response = put("/AllergyIntolerance/" + ALLERGY_UUID).xmlContent(toXML(allergy)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentAllergyAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.XML).go();
		AllergyIntolerance allergy = readResponse(response);
		
		// update the existing record
		allergy.setId(UNKNOWN_ALLERGY_UUID);
		
		// send the update to the server
		response = put("/AllergyIntolerance/" + UNKNOWN_ALLERGY_UUID).xmlContent(toXML(allergy)).accept(FhirMediaTypes.XML)
		        .go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldDeleteExistingAllergyAsJson() throws Exception {
		MockHttpServletResponse response = delete("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, statusEquals(HttpStatus.GONE));
	}
	
	@Test
	public void shouldReturnNotFoundWhenDeletingNonExistentAllergyAsJson() throws Exception {
		MockHttpServletResponse response = delete("/AllergyIntolerance/" + UNKNOWN_ALLERGY_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldPatchExistingAllergyUsingJsonMergePatch() throws Exception {
		String jsonAllergyPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_MERGE_PATCH_ALLERGY_PATH)) {
			Objects.requireNonNull(is);
			jsonAllergyPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/AllergyIntolerance/" + ALLERGY_UUID).jsonMergePatch(jsonAllergyPatch)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		AllergyIntolerance allergyIntolerance = readResponse(response);
		
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
		
		//ensure category has been patched
		assertThat(allergyIntolerance.getCategory().get(0).getCode(), equalTo("food"));
		assertThat(allergyIntolerance, validResource());
	}
	
	@Test
	public void shouldPatchExistingAllergyUsingJsonPatch() throws Exception {
		String jsonAllergyPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_ALLERGY_PATH)) {
			Objects.requireNonNull(is);
			jsonAllergyPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/AllergyIntolerance/" + ALLERGY_UUID).jsonPatch(jsonAllergyPatch)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		AllergyIntolerance allergyIntolerance = readResponse(response);
		
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
		
		//ensure category has been patched
		assertThat(allergyIntolerance.getCategory().get(0).getCode(), equalTo("food"));
		assertThat(allergyIntolerance, validResource());
	}
	
	@Test
	public void shouldPatchExistingAllergyUsingXmlPatch() throws Exception {
		String xmlAllergyPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_PATCH_ALLERGY_PATH)) {
			Objects.requireNonNull(is);
			xmlAllergyPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/AllergyIntolerance/" + ALLERGY_UUID).xmlPatch(xmlAllergyPatch)
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		AllergyIntolerance allergyIntolerance = readResponse(response);
		
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
		
		//ensure category has been patched
		assertThat(allergyIntolerance.getCategory().get(0).getCode(), equalTo("food"));
		assertThat(allergyIntolerance, validResource());
	}
	
	@Test
	public void shouldDeleteExistingAllergyAsXML() throws Exception {
		MockHttpServletResponse response = delete("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, statusEquals(HttpStatus.GONE));
	}
	
	@Test
	public void shouldReturnNotFoundWhenDeletingNonExistentAllergyAsXML() throws Exception {
		MockHttpServletResponse response = delete("/AllergyIntolerance/" + UNKNOWN_ALLERGY_UUID).accept(FhirMediaTypes.XML)
		        .go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldSearchForExistingAllergyAsJson() throws Exception {
		MockHttpServletResponse response = get("/AllergyIntolerance").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries,
		    everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/AllergyIntolerance/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(AllergyIntolerance.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		response = get("/AllergyIntolerance?patient.identifier=M4001-1&_sort=severity").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		entries = results.getEntry();
		
		assertThat(entries, everyItem(hasResource(hasProperty("patient",
		    hasProperty("referenceElement", hasProperty("idPart", equalTo("8d703ff2-c3e2-4070-9737-73e713d5a50d")))))));
		assertThat(entries,
		    containsInRelativeOrder(hasResource(hasProperty("criticality", hasProperty("display", equalTo("Low Risk")))),
		        // mild
		        hasResource(hasProperty("criticality", hasProperty("display", equalTo("Unable to Assess Risk")))),
		        // moderate
		        hasResource(hasProperty("criticality", hasProperty("display", equalTo("High Risk")))), // severe
		        hasResource(hasProperty("criticality", equalTo(null))))); // null
		assertThat(entries, everyItem(hasResource(validResource())));
		
	}
	
	@Test
	public void shouldSearchForExistingAllergyAsXML() throws Exception {
		MockHttpServletResponse response = get("/AllergyIntolerance").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries,
		    everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/AllergyIntolerance/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(AllergyIntolerance.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		response = get("/AllergyIntolerance?patient.identifier=M4001-1&_sort=severity").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		entries = results.getEntry();
		
		assertThat(entries, everyItem(hasResource(hasProperty("patient",
		    hasProperty("referenceElement", hasProperty("idPart", equalTo("8d703ff2-c3e2-4070-9737-73e713d5a50d")))))));
		assertThat(entries,
		    containsInRelativeOrder(hasResource(hasProperty("criticality", hasProperty("display", equalTo("Low Risk")))),
		        // mild
		        hasResource(hasProperty("criticality", hasProperty("display", equalTo("Unable to Assess Risk")))),
		        // moderate
		        hasResource(hasProperty("criticality", hasProperty("display", equalTo("High Risk")))), // severe
		        hasResource(hasProperty("criticality", equalTo(null))))); // null
		assertThat(entries, everyItem(hasResource(validResource())));
		
	}
	
	@Test
	public void shouldReturnCountForAllergyIntoleranceAsJson() throws Exception {
		MockHttpServletResponse response = get("/AllergyIntolerance?_summary=count").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(6)));
	}
	
	@Test
	public void shouldReturnCountForAllergyIntoleranceAsXml() throws Exception {
		MockHttpServletResponse response = get("/AllergyIntolerance?_summary=count").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(6)));
	}
	
	@Test
	public void shouldReturnAnEtagHeaderWhenRetrievingAnExistingAllergyIntolerance() throws Exception {
		MockHttpServletResponse response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		
		assertThat(response.getHeader("etag"), notNullValue());
		assertThat(response.getHeader("etag"), startsWith("W/"));
		
		assertThat(response.getContentAsString(), notNullValue());
		
		AllergyIntolerance allergyIntolerance = readResponse(response);
		
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getMeta().getVersionId(), notNullValue());
		assertThat(allergyIntolerance, validResource());
	}
	
	@Test
	public void shouldReturnNotModifiedWhenRetrievingAnExistingAllergyIntoleranceWithAnEtag() throws Exception {
		MockHttpServletResponse response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		assertThat(response.getHeader("etag"), notNullValue());
		
		String etagValue = response.getHeader("etag");
		
		response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.JSON).ifNoneMatchHeader(etagValue).go();
		
		assertThat(response, isOk());
		assertThat(response, statusEquals(HttpStatus.NOT_MODIFIED));
	}
	
}
