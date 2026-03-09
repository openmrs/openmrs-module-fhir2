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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang.time.DateUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class PractitionerFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<PractitionerFhirResourceProvider, Practitioner> {
	
	private static final String PRACTITIONER_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPractitionerDaoImplTest_initial_data.xml";
	
	private static final String JSON_CREATE_PRACTITIONER_DOCUMENT = "org/openmrs/module/fhir2/providers/PractitionerWebTest_create.json";
	
	private static final String XML_CREATE_PRACTITIONER_DOCUMENT = "org/openmrs/module/fhir2/providers/PractitionerWebTest_create.xml";
	
	private static final String JSON_MERGE_PATCH_PRACTITIONER_PATH = "org/openmrs/module/fhir2/providers/Practitioner_json_merge_patch.json";
	
	private static final String JSON_PATCH_PRACTITIONER_PATH = "org/openmrs/module/fhir2/providers/Practitioner_json_patch.json";
	
	private static final String XML_PATCH_PRACTITIONER_PATH = "org/openmrs/module/fhir2/providers/Practitioner_xmlpatch.xml";
	
	private static final String PRACTITIONER_UUID = "f9badd80-ab76-11e2-9e96-0800200c9a66";
	
	private static final String WRONG_PRACTITIONER_UUID = "f8bc0122-21db-4e91-a5d3-92ae01cafe92";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private PractitionerFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(PRACTITIONER_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingPractitionerAsJson() throws Exception {
		MockHttpServletResponse response = get("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Practitioner practitioner = readResponse(response);
		
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
		assertThat(practitioner, validResource());
		assertThat(practitioner.getActive(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenPractitionerNotFoundAsJson() throws Exception {
		MockHttpServletResponse response = get("/Practitioner/" + WRONG_PRACTITIONER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnExistingPractitionerAsXML() throws Exception {
		MockHttpServletResponse response = get("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Practitioner practitioner = readResponse(response);
		
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
		assertThat(practitioner, validResource());
	}
	
	@Test
	public void shouldReturnNotFoundWhenPractitionerNotFoundAsXML() throws Exception {
		MockHttpServletResponse response = get("/Practitioner/" + WRONG_PRACTITIONER_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldCreateNewPractitionerAsJson() throws Exception {
		// read JSON record
		String jsonPractitioner;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_PRACTITIONER_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonPractitioner = inputStreamToString(is, UTF_8);
		}
		
		// create practitioner
		MockHttpServletResponse response = post("/Practitioner").accept(FhirMediaTypes.JSON).jsonContent(jsonPractitioner)
		        .go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getHeader("Location"), containsString("/Practitioner/"));
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Practitioner practitioner = readResponse(response);
		
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getIdElement().getIdPart(), notNullValue());
		assertThat(practitioner.getName().get(0).getGiven().get(0).toString(), equalToIgnoringCase("Adam"));
		assertThat(practitioner.getName().get(0).getFamily(), equalToIgnoringCase("John"));
		assertThat(practitioner.getGender(), equalTo(Enumerations.AdministrativeGender.MALE));
		
		Date birthDate = Date.from(LocalDate.of(2004, 8, 12).atStartOfDay(ZoneId.systemDefault()).toInstant());
		assertThat(practitioner.getBirthDate(), equalTo(birthDate));
		
		assertThat(practitioner.getAddress().get(0).getCity(), equalTo("Kampala"));
		assertThat(practitioner.getAddress().get(0).getState(), equalTo("Mukono"));
		assertThat(practitioner.getAddress().get(0).getCountry(), equalTo("Uganda"));
		assertThat(practitioner, validResource());
		
		// try to get new practitioner
		response = get("/Practitioner/" + practitioner.getIdElement().getIdPart()).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Practitioner newPractitioner = readResponse(response);
		
		assertThat(newPractitioner.getId(), equalTo(practitioner.getId()));
	}
	
	@Test
	public void shouldCreateNewPractitionerAsXML() throws Exception {
		// read XML record
		String xmlPractitioner;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_CREATE_PRACTITIONER_DOCUMENT)) {
			Objects.requireNonNull(is);
			xmlPractitioner = inputStreamToString(is, UTF_8);
		}
		
		// create practitioner
		MockHttpServletResponse response = post("/Practitioner").accept(FhirMediaTypes.XML).xmlContent(xmlPractitioner).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getHeader("Location"), containsString("/Practitioner/"));
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Practitioner practitioner = readResponse(response);
		
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getIdElement().getIdPart(), notNullValue());
		assertThat(practitioner.getName().get(0).getGiven().get(0).toString(), equalToIgnoringCase("Adam"));
		assertThat(practitioner.getName().get(0).getFamily(), equalToIgnoringCase("John"));
		assertThat(practitioner.getGender(), equalTo(Enumerations.AdministrativeGender.MALE));
		
		Date birthDate = Date.from(LocalDate.of(2004, 8, 12).atStartOfDay(ZoneId.systemDefault()).toInstant());
		assertThat(practitioner.getBirthDate(), equalTo(birthDate));
		
		assertThat(practitioner.getAddress().get(0).getCity(), equalTo("Kampala"));
		assertThat(practitioner.getAddress().get(0).getState(), equalTo("Mukono"));
		assertThat(practitioner.getAddress().get(0).getCountry(), equalTo("Uganda"));
		assertThat(practitioner, validResource());
		
		// try to get new practitioner
		response = get("/Practitioner/" + practitioner.getIdElement().getIdPart()).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		Practitioner newPractitioner = readResponse(response);
		
		assertThat(newPractitioner.getId(), equalTo(practitioner.getId()));
	}
	
	@Test
	public void shouldUpdateExistingPractitionerAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.JSON).go();
		Practitioner practitioner = readResponse(response);
		
		// update the existing record
		Date birthDate = DateUtils.truncate(new Date(), Calendar.DATE);
		practitioner.setBirthDate(birthDate);
		
		// send the update to the server
		response = put("/Practitioner/" + PRACTITIONER_UUID).jsonContent(toJson(practitioner)).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read the updated record
		Practitioner updatedPractitioner = readResponse(response);
		
		assertThat(updatedPractitioner, notNullValue());
		assertThat(updatedPractitioner.getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
		assertThat(updatedPractitioner.getBirthDate(), equalTo(birthDate));
		assertThat(updatedPractitioner, validResource());
		
		// double-check the record returned via get
		response = get("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.JSON).go();
		Practitioner reReadPractitioner = readResponse(response);
		
		assertThat(reReadPractitioner.getBirthDate(), equalTo(birthDate));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchPractitionerIdAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.JSON).go();
		Practitioner practitioner = readResponse(response);
		
		// update the existing record
		practitioner.setId(WRONG_PRACTITIONER_UUID);
		
		// send the update to the server
		response = put("/Practitioner/" + PRACTITIONER_UUID).jsonContent(toJson(practitioner)).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentPractitionerAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.JSON).go();
		Practitioner practitioner = readResponse(response);
		
		// update the existing record
		practitioner.setId(WRONG_PRACTITIONER_UUID);
		
		// send the update to the server
		response = put("/Practitioner/" + WRONG_PRACTITIONER_UUID).jsonContent(toJson(practitioner))
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldUpdateExistingPractitionerAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.XML).go();
		Practitioner practitioner = readResponse(response);
		
		// update the existing record
		Date birthDate = DateUtils.truncate(new Date(), Calendar.DATE);
		practitioner.setBirthDate(birthDate);
		
		// send the update to the server
		response = put("/Practitioner/" + PRACTITIONER_UUID).xmlContent(toXML(practitioner)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read the updated record
		Practitioner updatedPractitioner = readResponse(response);
		
		assertThat(updatedPractitioner, notNullValue());
		assertThat(updatedPractitioner.getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
		assertThat(updatedPractitioner.getBirthDate(), equalTo(birthDate));
		assertThat(practitioner, validResource());
		
		// double-check the record returned via get
		response = get("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.XML).go();
		Practitioner reReadPractitioner = readResponse(response);
		
		assertThat(reReadPractitioner.getBirthDate(), equalTo(birthDate));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchPractitionerIdAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.XML).go();
		Practitioner practitioner = readResponse(response);
		
		// update the existing record
		practitioner.setId(WRONG_PRACTITIONER_UUID);
		
		// send the update to the server
		response = put("/Practitioner/" + PRACTITIONER_UUID).xmlContent(toXML(practitioner)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentPractitionerAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.XML).go();
		Practitioner practitioner = readResponse(response);
		
		// update the existing record
		practitioner.setId(WRONG_PRACTITIONER_UUID);
		
		// send the update to the server
		response = put("/Practitioner/" + WRONG_PRACTITIONER_UUID).xmlContent(toXML(practitioner)).accept(FhirMediaTypes.XML)
		        .go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldPatchExistingPractitionerUsingJsonMergePatch() throws Exception {
		String jsonPractitionerPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_MERGE_PATCH_PRACTITIONER_PATH)) {
			Objects.requireNonNull(is);
			jsonPractitionerPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/Practitioner/" + PRACTITIONER_UUID).jsonMergePatch(jsonPractitionerPatch)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Practitioner practitioner = readResponse(response);
		
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
		assertThat(practitioner, validResource());
		
		assertThat(practitioner.getGender(), is(Enumerations.AdministrativeGender.FEMALE));
	}
	
	@Test
	public void shouldPatchExistingPractitionerUsingJsonPatch() throws Exception {
		String jsonPractitionerPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_PRACTITIONER_PATH)) {
			Objects.requireNonNull(is);
			jsonPractitionerPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/Practitioner/" + PRACTITIONER_UUID).jsonPatch(jsonPractitionerPatch)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Practitioner practitioner = readResponse(response);
		
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
		assertThat(practitioner, validResource());
		
		assertThat(practitioner.getGender(), is(Enumerations.AdministrativeGender.FEMALE));
	}
	
	@Test
	public void shouldPatchExistingPractitionerUsingXmlPatch() throws Exception {
		String xmlPractitionerPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_PATCH_PRACTITIONER_PATH)) {
			Objects.requireNonNull(is);
			xmlPractitionerPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/Practitioner/" + PRACTITIONER_UUID).xmlPatch(xmlPractitionerPatch)
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Practitioner practitioner = readResponse(response);
		
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
		assertThat(practitioner, validResource());
		
		assertThat(practitioner.getGender(), is(Enumerations.AdministrativeGender.FEMALE));
	}
	
	@Test
	public void shouldDeleteExistingPractitioner() throws Exception {
		MockHttpServletResponse response = delete("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		response = get("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, statusEquals(HttpStatus.GONE));
	}
	
	@Test
	public void shouldReturnNotFoundWhenDeletingNonExistentPractitioner() throws Exception {
		MockHttpServletResponse response = delete("/Practitioner/" + WRONG_PRACTITIONER_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldSearchForAllPractitionersAsJson() throws Exception {
		MockHttpServletResponse response = get("/Practitioner").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Practitioner/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Practitioner.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldSearchForAllPractitionersAsXML() throws Exception {
		MockHttpServletResponse response = get("/Practitioner").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Practitioner/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Practitioner.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldReturnCountForPractitionerAsJson() throws Exception {
		MockHttpServletResponse response = get("/Practitioner?&_summary=count").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(6)));
	}
	
	@Test
	public void shouldReturnCountForPractitionerAsXml() throws Exception {
		MockHttpServletResponse response = get("/Practitioner?&_summary=count").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(6)));
	}
	
	@Test
	public void shouldReturnAnEtagHeaderWhenRetrievingAnExistingPractitioner() throws Exception {
		MockHttpServletResponse response = get("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		
		assertThat(response.getHeader("etag"), notNullValue());
		assertThat(response.getHeader("etag"), startsWith("W/"));
		
		assertThat(response.getContentAsString(), notNullValue());
		
		Practitioner practitioner = readResponse(response);
		
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getMeta().getVersionId(), notNullValue());
		assertThat(practitioner, validResource());
	}
	
	@Test
	public void shouldReturnNotModifiedWhenRetrievingAnExistingPractitionerWithAnEtag() throws Exception {
		MockHttpServletResponse response = get("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		assertThat(response.getHeader("etag"), notNullValue());
		
		String etagValue = response.getHeader("etag");
		
		response = get("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.JSON).ifNoneMatchHeader(etagValue).go();
		
		assertThat(response, isOk());
		assertThat(response, statusEquals(HttpStatus.NOT_MODIFIED));
	}
}
