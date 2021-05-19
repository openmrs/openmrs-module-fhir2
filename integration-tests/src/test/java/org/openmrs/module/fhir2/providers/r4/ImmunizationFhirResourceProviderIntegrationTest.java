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

import static org.exparity.hamcrest.date.DateMatchers.sameDay;
import static org.exparity.hamcrest.date.DateMatchers.sameInstant;
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
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class ImmunizationFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<ImmunizationFhirResourceProvider, Immunization> {
	
	private static final String IMMUNIZATIONS_METADATA_XML = "org/openmrs/module/fhir2/Immunization_metadata.xml";
	
	private static final String IMMUNIZATION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirImmunizationDaoImplTest_initial_data.xml";
	
	private static final String JSON_CREATE_IMMUNIZATION_DOCUMENT = "org/openmrs/module/fhir2/providers/ImmunizationWebTest_create.json";
	
	private static final String XML_CREATE_IMMUNIZATION_DOCUMENT = "org/openmrs/module/fhir2/providers/ImmunizationWebTest_create.xml";
	
	private static final String IMMUNIZATION_UUID = "28668ca0-d7d7-4314-8a67-70f083bcf8ba";
	
	private static final String UNKNOWN_IMMUNIZATION_UUID = "46fc09a6-3368-4579-849f-98875a7c2d5a";
	
	public static final String PATIENT_UUID = "8d703ff2-c3e2-4070-9737-73e713d5a50d";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private ImmunizationFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		
		executeDataSet(IMMUNIZATIONS_METADATA_XML);
		executeDataSet(IMMUNIZATION_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingImmunizationAsJson() throws Exception {
		MockHttpServletResponse response = get("/Immunization/" + IMMUNIZATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Immunization Immunization = readResponse(response);
		
		assertThat(Immunization, notNullValue());
		assertThat(Immunization.getIdElement().getIdPart(), equalTo(IMMUNIZATION_UUID));
		assertThat(Immunization, validResource());
	}
	
	@Test
	public void shouldReturnNotFoundWhenImmunizationNotFoundAsJson() throws Exception {
		MockHttpServletResponse response = get("/Immunization/" + UNKNOWN_IMMUNIZATION_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnExistingImmunizationAsXML() throws Exception {
		MockHttpServletResponse response = get("/Immunization/" + IMMUNIZATION_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Immunization Immunization = readResponse(response);
		
		assertThat(Immunization, notNullValue());
		assertThat(Immunization.getIdElement().getIdPart(), equalTo(IMMUNIZATION_UUID));
		assertThat(Immunization, validResource());
	}
	
	@Test
	public void shouldReturnNotFoundWhenImmunizationNotFoundAsXML() throws Exception {
		MockHttpServletResponse response = get("/Immunization/" + UNKNOWN_IMMUNIZATION_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldCreateNewImmunizationAsJson() throws Exception {
		// read JSON record
		String jsonImmunization;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_IMMUNIZATION_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonImmunization = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		// create IMMUNIZATION
		MockHttpServletResponse response = post("/Immunization").accept(FhirMediaTypes.JSON).jsonContent(jsonImmunization)
		        .go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Immunization immunization = readResponse(response);
		
		assertThat(immunization, notNullValue());
		assertThat(immunization.getResourceType().toString(), equalTo("Immunization"));
		assertThat(immunization.getStatus().toCode(), equalTo("completed"));
		assertThat(immunization.getVaccineCode().getCodingFirstRep().getCode(),
		    equalTo("15f83cd6-64e9-4e06-a5f9-364d3b14a43d"));
		assertThat(immunization.getPatient().getReferenceElement().getIdPart(),
		    equalTo("8d703ff2-c3e2-4070-9737-73e713d5a50d"));
		assertThat(immunization.getOccurrenceDateTimeType().getValueAsCalendar().getTime(),
		    sameInstant(Date.from(ZonedDateTime.parse("2020-07-08T18:30:00.000Z").toInstant())));
		assertThat(immunization.hasManufacturer(), is(true));
		assertThat(immunization.getManufacturer().getDisplay(), equalTo("ACME"));
		assertThat(immunization.getLotNumber(), equalTo("FOO1234"));
		assertThat(immunization.getExpirationDate(),
		    sameDay(Date.from(ZonedDateTime.parse("2022-07-31T18:30:00.000Z").toInstant())));
		assertThat(immunization.getPerformer().get(0).getActor().getReferenceElement().getIdPart(),
		    equalTo("6f763a67-2bd1-451c-93b9-95caeb36cc24"));
		assertThat(immunization, validResource());
		
		// try to get new immunization
		response = get("/Immunization/" + immunization.getIdElement().getIdPart()).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Immunization newImmunization = readResponse(response);
		
		assertThat(newImmunization.getId(), equalTo(immunization.getId()));
	}
	
	@Test
	public void shouldCreateNewImmunizationAsXML() throws Exception {
		// read XML record
		String xmlImmunization;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_CREATE_IMMUNIZATION_DOCUMENT)) {
			Objects.requireNonNull(is);
			xmlImmunization = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		// create IMMUNIZATION
		MockHttpServletResponse response = post("/Immunization").accept(FhirMediaTypes.XML).xmlContext(xmlImmunization).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Immunization immunization = readResponse(response);
		
		assertThat(immunization, notNullValue());
		assertThat(immunization.getResourceType().toString(), equalTo("Immunization"));
		assertThat(immunization.getStatus().toCode(), equalTo("completed"));
		assertThat(immunization.getVaccineCode().getCodingFirstRep().getCode(),
		    equalTo("15f83cd6-64e9-4e06-a5f9-364d3b14a43d"));
		assertThat(immunization.getPatient().getReferenceElement().getIdPart(),
		    equalTo("8d703ff2-c3e2-4070-9737-73e713d5a50d"));
		assertThat(immunization.getOccurrenceDateTimeType().getValueAsCalendar().getTime(),
		    sameInstant(Date.from(ZonedDateTime.parse("2020-07-08T18:30:00.000Z").toInstant())));
		assertThat(immunization.hasManufacturer(), is(true));
		assertThat(immunization.getManufacturer().getDisplay(), equalTo("ACME"));
		assertThat(immunization.getLotNumber(), equalTo("FOO1234"));
		assertThat(immunization.getExpirationDate(),
		    sameDay(Date.from(ZonedDateTime.parse("2022-07-31T18:30:00.000Z").toInstant())));
		assertThat(immunization.getPerformer().get(0).getActor().getReferenceElement().getIdPart(),
		    equalTo("6f763a67-2bd1-451c-93b9-95caeb36cc24"));
		assertThat(immunization, validResource());
		
		// try to get new IMMUNIZATION
		response = get("/Immunization/" + immunization.getIdElement().getIdPart()).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		Immunization newImmunization = readResponse(response);
		
		assertThat(newImmunization.getId(), equalTo(immunization.getId()));
	}
	
	@Test
	public void shouldUpdateExistingImmunizationAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Immunization/" + IMMUNIZATION_UUID).accept(FhirMediaTypes.JSON).go();
		Immunization immunization = readResponse(response);
		
		// update the existing record
		Date expirationDate = DateUtils.truncate(new Date(), Calendar.DATE);
		immunization.setExpirationDate(expirationDate);
		
		// send the update to the server
		response = put("/Immunization/" + IMMUNIZATION_UUID).jsonContent(toJson(immunization)).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read the updated record
		Immunization updatedImmunization = readResponse(response);
		
		assertThat(updatedImmunization, notNullValue());
		assertThat(updatedImmunization.getIdElement().getIdPart(), equalTo(IMMUNIZATION_UUID));
		assertThat(updatedImmunization.getExpirationDate(), equalTo(expirationDate));
		assertThat(immunization, validResource());
		
		// double-check the record returned via get
		response = get("/Immunization/" + IMMUNIZATION_UUID).accept(FhirMediaTypes.JSON).go();
		Immunization reReadImmunization = readResponse(response);
		
		assertThat(reReadImmunization.getExpirationDate(), equalTo(expirationDate));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchImmunizationIdAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Immunization/" + IMMUNIZATION_UUID).accept(FhirMediaTypes.JSON).go();
		Immunization immunization = readResponse(response);
		
		// update the existing record
		immunization.setId(UNKNOWN_IMMUNIZATION_UUID);
		
		// send the update to the server
		response = put("/Immunization/" + IMMUNIZATION_UUID).jsonContent(toJson(immunization)).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentImmunizationAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Immunization/" + IMMUNIZATION_UUID).accept(FhirMediaTypes.JSON).go();
		Immunization immunization = readResponse(response);
		
		// update the existing record
		immunization.setId(UNKNOWN_IMMUNIZATION_UUID);
		
		// send the update to the server
		response = put("/Immunization/" + UNKNOWN_IMMUNIZATION_UUID).jsonContent(toJson(immunization))
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldUpdateExistingImmunizationAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Immunization/" + IMMUNIZATION_UUID).accept(FhirMediaTypes.XML).go();
		Immunization immunization = readResponse(response);
		
		// update the existing record
		Date expirationDate = DateUtils.truncate(new Date(), Calendar.DATE);
		immunization.setExpirationDate(expirationDate);
		
		// send the update to the server
		response = put("/Immunization/" + IMMUNIZATION_UUID).xmlContext(toXML(immunization)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read the updated record
		Immunization updatedImmunization = readResponse(response);
		
		assertThat(updatedImmunization, notNullValue());
		assertThat(updatedImmunization.getIdElement().getIdPart(), equalTo(IMMUNIZATION_UUID));
		assertThat(updatedImmunization.getExpirationDate(), equalTo(expirationDate));
		assertThat(immunization, validResource());
		
		// double-check the record returned via get
		response = get("/Immunization/" + IMMUNIZATION_UUID).accept(FhirMediaTypes.XML).go();
		Immunization reReadImmunization = readResponse(response);
		
		assertThat(reReadImmunization.getExpirationDate(), equalTo(expirationDate));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchImmunizationIdAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Immunization/" + IMMUNIZATION_UUID).accept(FhirMediaTypes.XML).go();
		Immunization immunization = readResponse(response);
		
		// update the existing record
		immunization.setId(UNKNOWN_IMMUNIZATION_UUID);
		
		// send the update to the server
		response = put("/Immunization/" + IMMUNIZATION_UUID).xmlContext(toXML(immunization)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentImmunizationAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Immunization/" + IMMUNIZATION_UUID).accept(FhirMediaTypes.XML).go();
		Immunization immunization = readResponse(response);
		
		// update the existing record
		immunization.setId(UNKNOWN_IMMUNIZATION_UUID);
		
		// send the update to the server
		response = put("/Immunization/" + UNKNOWN_IMMUNIZATION_UUID).xmlContext(toXML(immunization))
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldDeleteExistingImmunizationAsJson() throws Exception {
		MockHttpServletResponse response = delete("/Immunization/" + IMMUNIZATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		response = get("/Immunization/" + IMMUNIZATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, statusEquals(HttpStatus.GONE));
	}
	
	@Test
	public void shouldReturnNotFoundWhenDeletingNonExistentImmunizationAsJson() throws Exception {
		MockHttpServletResponse response = delete("/Immunization/" + UNKNOWN_IMMUNIZATION_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldDeleteExistingImmunizationAsXML() throws Exception {
		MockHttpServletResponse response = delete("/Immunization/" + IMMUNIZATION_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		response = get("/Immunization/" + IMMUNIZATION_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, statusEquals(HttpStatus.GONE));
	}
	
	@Test
	public void shouldReturnNotFoundWhenDeletingNonExistentImmunizationAsXML() throws Exception {
		MockHttpServletResponse response = delete("/Immunization/" + UNKNOWN_IMMUNIZATION_UUID).accept(FhirMediaTypes.XML)
		        .go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldSearchForExistingImmunizationAsJson() throws Exception {
		MockHttpServletResponse response = get("/Immunization").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Immunization/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Immunization.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		response = get("/Immunization?patient.identifier=M4001-1&_sort=status").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		entries = results.getEntry();
		
		assertThat(entries, everyItem(hasResource(
		    hasProperty("patient", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_UUID)))))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldSearchForExistingImmunizationAsXML() throws Exception {
		MockHttpServletResponse response = get("/Immunization").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Immunization/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Immunization.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		response = get("/Immunization?patient.identifier=M4001-1&_sort=status").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		entries = results.getEntry();
		
		assertThat(entries, everyItem(hasResource(
		    hasProperty("patient", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_UUID)))))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
}
