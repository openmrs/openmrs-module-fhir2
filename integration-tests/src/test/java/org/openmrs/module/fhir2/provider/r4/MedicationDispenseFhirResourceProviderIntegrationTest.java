/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.provider.r4;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
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
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.ConceptService;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.module.fhir2.providers.r4.BaseFhirR4IntegrationTest;
import org.openmrs.module.fhir2.providers.r4.MedicationDispenseFhirResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class MedicationDispenseFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<MedicationDispenseFhirResourceProvider, MedicationDispense> {
	
	public static final String EXISTING_DISPENSE_UUID = "1bcb299c-b687-11ec-8065-0242ac110002";
	
	public static final String NEW_DISPENSE_UUID = "a15e4988-d07a-11ec-8307-0242ac110002";
	
	private static final String PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041";
	
	private static final String JSON_MERGE_PATCH_DISPENSE_PATH = "org/openmrs/module/fhir2/providers/MedicationDispense_json_merge_patch.json";
	
	private static final String JSON_PATCH_DISPENSE_PATH = "org/openmrs/module/fhir2/providers/MedicationDispense_json_patch.json";
	
	private static final String XML_PATCH_DISPENSE_PATH = "org/openmrs/module/fhir2/providers/MedicationDispense_xml_patch.xml";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private MedicationDispenseFhirResourceProvider resourceProvider;
	
	@Autowired
	ConceptService conceptService;
	
	@Autowired
	FhirConceptSourceService fhirConceptSourceService;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet("org/openmrs/api/include/MedicationDispenseServiceTest-initialData.xml");
		executeDataSet("org/openmrs/module/fhir2/api/dao/impl/FhirMedicationDispenseDaoImplTest_initial_data.xml");
		updateSearchIndex();
	}
	
	@Test
	public void shouldReturnExistingMedicationDispenseAsJson() throws Exception {
		MockHttpServletResponse response = get("/MedicationDispense/" + EXISTING_DISPENSE_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		MedicationDispense medicationDispense = readResponse(response);
		
		assertThat(medicationDispense, notNullValue());
		assertThat(medicationDispense.getIdElement().getIdPart(), equalTo(EXISTING_DISPENSE_UUID));
		assertThat(medicationDispense, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingMedicationDispenseAsJson() throws Exception {
		MockHttpServletResponse response = get("/MedicationDispense/" + NEW_DISPENSE_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldReturnExistingMedicationDispenseAsXML() throws Exception {
		MockHttpServletResponse response = get("/MedicationDispense/" + EXISTING_DISPENSE_UUID).accept(FhirMediaTypes.XML)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		MedicationDispense medicationDispense = readResponse(response);
		
		assertThat(medicationDispense, notNullValue());
		assertThat(medicationDispense.getIdElement().getIdPart(), equalTo(EXISTING_DISPENSE_UUID));
		assertThat(medicationDispense, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingMedicationDispenseAsXML() throws Exception {
		MockHttpServletResponse response = get("/MedicationDispense/" + NEW_DISPENSE_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldSearchForExistingMedicationDispensesAsJson() throws Exception {
		MockHttpServletResponse response = get("/MedicationDispense").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries,
		    everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/MedicationDispense/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(MedicationDispense.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		response = get("/MedicationDispense?patient.identifier=6TS-4").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
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
	public void shouldSearchForExistingMedicationDispensesAsXML() throws Exception {
		MockHttpServletResponse response = get("/MedicationDispense").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries,
		    everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/MedicationDispense/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(MedicationDispense.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		response = get("/MedicationDispense?patient.identifier=6TS-4").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
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
	public void shouldReturnCountForMedicationDispenseAsJson() throws Exception {
		MockHttpServletResponse response = get("/MedicationDispense?_summary=count").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(3)));
	}
	
	@Test
	public void shouldReturnCountForMedicationDispenseAsXml() throws Exception {
		MockHttpServletResponse response = get("/MedicationDispense?_summary=count").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(3)));
	}
	
	@Test
	public void shouldPatchExistingMedicationDispenseUsingJsonMergePatch() throws Exception {
		String jsonMedicationDispensePatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_MERGE_PATCH_DISPENSE_PATH)) {
			Objects.requireNonNull(is);
			jsonMedicationDispensePatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/MedicationDispense/" + EXISTING_DISPENSE_UUID)
		        .jsonMergePatch(jsonMedicationDispensePatch).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		MedicationDispense medicationDispense = readResponse(response);
		
		assertThat(medicationDispense, notNullValue());
		assertThat(medicationDispense.getIdElement().getIdPart(), equalTo(EXISTING_DISPENSE_UUID));
		assertThat(medicationDispense, validResource());
		
		assertThat(medicationDispense.getStatus(), is(MedicationDispense.MedicationDispenseStatus.COMPLETED));
	}
	
	@Test
	public void shouldPatchExistingMedicationDispenseUsingJsonPatch() throws Exception {
		String jsonMedicationDispensePatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_DISPENSE_PATH)) {
			Objects.requireNonNull(is);
			jsonMedicationDispensePatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/MedicationDispense/" + EXISTING_DISPENSE_UUID)
		        .jsonPatch(jsonMedicationDispensePatch).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		MedicationDispense medicationDispense = readResponse(response);
		
		assertThat(medicationDispense, notNullValue());
		assertThat(medicationDispense.getIdElement().getIdPart(), equalTo(EXISTING_DISPENSE_UUID));
		assertThat(medicationDispense, validResource());
		
		assertThat(medicationDispense.getStatus(), is(MedicationDispense.MedicationDispenseStatus.COMPLETED));
	}
	
	@Test
	public void shouldPatchExistingMedicationDispenseUsingXmlPatch() throws Exception {
		String xmlMedicationDispensePatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_PATCH_DISPENSE_PATH)) {
			Objects.requireNonNull(is);
			xmlMedicationDispensePatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/MedicationDispense/" + EXISTING_DISPENSE_UUID)
		        .xmlPatch(xmlMedicationDispensePatch).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		MedicationDispense medicationDispense = readResponse(response);
		
		assertThat(medicationDispense, notNullValue());
		assertThat(medicationDispense.getIdElement().getIdPart(), equalTo(EXISTING_DISPENSE_UUID));
		assertThat(medicationDispense, validResource());
		
		assertThat(medicationDispense.getStatus(), is(MedicationDispense.MedicationDispenseStatus.COMPLETED));
	}
	
	@Test
	public void shouldReturnAnEtagHeaderWhenRetrievingAnExistingMedicationDispense() throws Exception {
		MockHttpServletResponse response = get("/MedicationDispense/" + EXISTING_DISPENSE_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		
		assertThat(response.getHeader("etag"), notNullValue());
		assertThat(response.getHeader("etag"), startsWith("W/"));
		
		assertThat(response.getContentAsString(), notNullValue());
		
		MedicationDispense medicationDispense = readResponse(response);
		
		assertThat(medicationDispense, notNullValue());
		assertThat(medicationDispense.getMeta().getVersionId(), notNullValue());
		assertThat(medicationDispense, validResource());
	}
	
	@Test
	public void shouldReturnNotModifiedWhenRetrievingAnExistingMedicationDispenseWithAnEtag() throws Exception {
		MockHttpServletResponse response = get("/MedicationDispense/" + EXISTING_DISPENSE_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		assertThat(response.getHeader("etag"), notNullValue());
		
		String etagValue = response.getHeader("etag");
		
		response = get("/MedicationDispense/" + EXISTING_DISPENSE_UUID).accept(FhirMediaTypes.JSON)
		        .ifNoneMatchHeader(etagValue).go();
		
		assertThat(response, isOk());
		assertThat(response, statusEquals(HttpStatus.NOT_MODIFIED));
	}
}
