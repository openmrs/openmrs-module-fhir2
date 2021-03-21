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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class DiagnosticReportResourceProviderIntegrationTest extends BaseFhirR3IntegrationTest<DiagnosticReportFhirResourceProvider, DiagnosticReport> {
	
	private static final String DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirDiagnosticReportDaoImplTest_initial_data.xml";
	
	private static final String DIAGNOSTIC_REPORT_UUID = "1e589127-f391-4d0c-8e98-e0a158b2be22";
	
	private static final String WRONG_DIAGNOSTIC_REPORT_UUID = "6ebc40fb-fe8b-4208-9526-68375d2cbe1c";
	
	private static final String DIAGNOSTIC_REPORT_CONCEPT_UUID = "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String JSON_CREATE_DIAGNOSTIC_REPORT_DOCUMENT = "org/openmrs/module/fhir2/providers/DiagnosticReport_create_r3.json";
	
	private static final String XML_CREATE_DIAGNOSTIC_REPORT_DOCUMENT = "org/openmrs/module/fhir2/providers/DiagnosticReport_create_r3.xml";
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	private DiagnosticReportFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		
		executeDataSet(DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingDiagnosticReportAsJson() throws Exception {
		MockHttpServletResponse response = get("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		DiagnosticReport diagnosticReport = readResponse(response);
		
		assertThat(diagnosticReport, notNullValue());
		assertThat(diagnosticReport.getIdElement().getIdPart(), equalTo(DIAGNOSTIC_REPORT_UUID));
		
		assertThat(diagnosticReport.hasCategory(), is(true));
		assertThat(diagnosticReport.getCategory().getCodingFirstRep().getCode(), equalTo("LAB"));
		
		assertThat(diagnosticReport.hasSubject(), is(true));
		assertThat(diagnosticReport.getSubject().getReference(), equalTo("Patient/5946f880-b197-400b-9caa-a3c661d23041"));
		
		assertThat(diagnosticReport.hasContext(), is(true));
		assertThat(diagnosticReport.getContext().getReference(), equalTo("Encounter/6519d653-393b-4118-9c83-a3715b82d4ac"));
		
		assertThat(diagnosticReport.hasCode(), is(true));
		assertThat(diagnosticReport.getCode().getCoding(),
		    hasItem(hasProperty("code", equalTo(DIAGNOSTIC_REPORT_CONCEPT_UUID))));
		
		assertThat(diagnosticReport.hasIssued(), is(true));
		assertThat(diagnosticReport.getIssued(),
		    equalTo(Date.from(LocalDateTime.of(2008, 7, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant())));
		
		assertThat(diagnosticReport.hasResult(), is(true));
		assertThat(diagnosticReport.getResult(), hasSize(2));
		assertThat(diagnosticReport.getResult(),
		    hasItem(hasProperty("reference", equalTo("Observation/6f16bb57-12bc-4077-9f49-ceaa9b928669"))));
		
		assertThat(diagnosticReport, validResource());
	}
	
	@Test
	public void shouldReturnNotFoundWhenDiagnosticReportDoesNotExistAsJson() throws Exception {
		MockHttpServletResponse response = get("/DiagnosticReport/" + WRONG_DIAGNOSTIC_REPORT_UUID)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.getIssue(), hasSize(greaterThanOrEqualTo(1)));
		assertThat(operationOutcome.getIssue(),
		    hasItem(hasProperty("severity", equalTo(OperationOutcome.IssueSeverity.ERROR))));
	}
	
	@Test
	public void shouldReturnExistingDiagnosticReportAsXML() throws Exception {
		MockHttpServletResponse response = get("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).accept(FhirMediaTypes.XML)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		DiagnosticReport diagnosticReport = readResponse(response);
		
		assertThat(diagnosticReport, notNullValue());
		assertThat(diagnosticReport.getIdElement().getIdPart(), equalTo(DIAGNOSTIC_REPORT_UUID));
		
		assertThat(diagnosticReport.hasCategory(), is(true));
		assertThat(diagnosticReport.getCategory().getCodingFirstRep().getCode(), equalTo("LAB"));
		
		assertThat(diagnosticReport.hasSubject(), is(true));
		assertThat(diagnosticReport.getSubject().getReference(), equalTo("Patient/5946f880-b197-400b-9caa-a3c661d23041"));
		
		assertThat(diagnosticReport.hasContext(), is(true));
		assertThat(diagnosticReport.getContext().getReference(), equalTo("Encounter/6519d653-393b-4118-9c83-a3715b82d4ac"));
		
		assertThat(diagnosticReport.hasCode(), is(true));
		assertThat(diagnosticReport.getCode().getCoding(),
		    hasItem(hasProperty("code", equalTo(DIAGNOSTIC_REPORT_CONCEPT_UUID))));
		
		assertThat(diagnosticReport.hasIssued(), is(true));
		assertThat(diagnosticReport.getIssued(),
		    equalTo(Date.from(LocalDateTime.of(2008, 7, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant())));
		
		assertThat(diagnosticReport.hasResult(), is(true));
		assertThat(diagnosticReport.getResult(), hasSize(2));
		assertThat(diagnosticReport.getResult(),
		    hasItem(hasProperty("reference", equalTo("Observation/6f16bb57-12bc-4077-9f49-ceaa9b928669"))));
		
		assertThat(diagnosticReport, validResource());
	}
	
	@Test
	public void shouldReturnNotFoundWhenDiagnosticReportDoesNotExistAsXML() throws Exception {
		MockHttpServletResponse response = get("/DiagnosticReport/" + WRONG_DIAGNOSTIC_REPORT_UUID)
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.getIssue(), hasSize(greaterThanOrEqualTo(1)));
		assertThat(operationOutcome.getIssue(),
		    hasItem(hasProperty("severity", equalTo(OperationOutcome.IssueSeverity.ERROR))));
	}
	
	@Test
	public void shouldCreateNewDiagnosticReportAsJson() throws Exception {
		String jsonReport;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_DIAGNOSTIC_REPORT_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonReport = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = post("/DiagnosticReport").accept(FhirMediaTypes.JSON).jsonContent(jsonReport)
		        .go();
		
		assertThat(response, isCreated());
		assertThat(response.getHeader("Location"), containsString("/DiagnosticReport/"));
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		DiagnosticReport diagnosticReport = readResponse(response);
		
		assertThat(diagnosticReport, notNullValue());
		assertThat(diagnosticReport.getIdElement().getIdPart(), notNullValue());
		assertThat(diagnosticReport.getStatus(), equalTo(DiagnosticReport.DiagnosticReportStatus.FINAL));
		assertThat(diagnosticReport.getCategory().getCodingFirstRep().getCode(), equalTo("LAB"));
		
		assertThat(diagnosticReport.getCode().getCoding(), hasSize(greaterThanOrEqualTo(2)));
		assertThat(diagnosticReport.getCode().getCoding().get(0).getSystem(), nullValue());
		assertThat(diagnosticReport.getCode().getCoding().get(0).getCode(), equalTo("5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		assertThat(diagnosticReport.getCode().getCoding().get(1).getSystem(),
		    equalTo("https://openconceptlab.org/orgs/CIEL/sources/CIEL"));
		assertThat(diagnosticReport.getCode().getCoding().get(1).getCode(), equalTo("5085"));
		
		assertThat(diagnosticReport.getSubject().getReference(), equalTo("Patient/5946f880-b197-400b-9caa-a3c661d23041"));
		assertThat(diagnosticReport.getContext().getReference(), equalTo("Encounter/6519d653-393b-4118-9c83-a3715b82d4ac"));
		
		assertThat(diagnosticReport.getIssued(),
		    equalTo(Date.from(LocalDateTime.of(2011, 3, 4, 11, 45, 33).atOffset(ZoneOffset.ofHours(11)).toInstant())));
		
		assertThat(diagnosticReport.getResult(), hasSize(2));
		assertThat(diagnosticReport.getResult(),
		    hasItem(hasProperty("reference", equalTo("Observation/6f16bb57-12bc-4077-9f49-ceaa9b928669"))));
		assertThat(diagnosticReport.getResult(),
		    hasItem(hasProperty("reference", equalTo("Observation/dc386962-1c42-49ea-bed2-97650c66f742"))));
	}
	
	@Test
	public void shouldCreateNewDiagnosticReportAsXML() throws Exception {
		String xmlReport;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_CREATE_DIAGNOSTIC_REPORT_DOCUMENT)) {
			Objects.requireNonNull(is);
			xmlReport = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = post("/DiagnosticReport").accept(FhirMediaTypes.XML).xmlContext(xmlReport).go();
		
		assertThat(response, isCreated());
		assertThat(response.getHeader("Location"), containsString("/DiagnosticReport/"));
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		DiagnosticReport diagnosticReport = readResponse(response);
		
		assertThat(diagnosticReport, notNullValue());
		assertThat(diagnosticReport.getIdElement().getIdPart(), notNullValue());
		assertThat(diagnosticReport.getStatus(), equalTo(DiagnosticReport.DiagnosticReportStatus.FINAL));
		assertThat(diagnosticReport.getCategory().getCodingFirstRep().getCode(), equalTo("LAB"));
		
		assertThat(diagnosticReport.getCode().getCoding(), hasSize(greaterThanOrEqualTo(2)));
		assertThat(diagnosticReport.getCode().getCoding().get(0).getSystem(), nullValue());
		assertThat(diagnosticReport.getCode().getCoding().get(0).getCode(), equalTo("5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		assertThat(diagnosticReport.getCode().getCoding().get(1).getSystem(),
		    equalTo("https://openconceptlab.org/orgs/CIEL/sources/CIEL"));
		assertThat(diagnosticReport.getCode().getCoding().get(1).getCode(), equalTo("5085"));
		
		assertThat(diagnosticReport.getSubject().getReference(), equalTo("Patient/5946f880-b197-400b-9caa-a3c661d23041"));
		assertThat(diagnosticReport.getContext().getReference(), equalTo("Encounter/6519d653-393b-4118-9c83-a3715b82d4ac"));
		
		assertThat(diagnosticReport.getIssued(),
		    equalTo(Date.from(LocalDateTime.of(2011, 3, 4, 11, 45, 33).atOffset(ZoneOffset.ofHours(11)).toInstant())));
		
		assertThat(diagnosticReport.getResult(), hasSize(2));
		assertThat(diagnosticReport.getResult(),
		    hasItem(hasProperty("reference", equalTo("Observation/6f16bb57-12bc-4077-9f49-ceaa9b928669"))));
		assertThat(diagnosticReport.getResult(),
		    hasItem(hasProperty("reference", equalTo("Observation/dc386962-1c42-49ea-bed2-97650c66f742"))));
	}
	
	@Test
	public void shouldUpdateExistingDiagnosticReportAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		DiagnosticReport diagnosticReport = readResponse(response);
		
		// verify condition we will change
		assertThat(diagnosticReport.getStatus(), not(is(DiagnosticReport.DiagnosticReportStatus.FINAL)));
		
		// update the existing record
		diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);
		
		// send the update to the server
		response = put("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).jsonContent(toJson(diagnosticReport))
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read the updated record
		DiagnosticReport updatedDiagnosticReport = readResponse(response);
		
		assertThat(updatedDiagnosticReport, notNullValue());
		assertThat(updatedDiagnosticReport.getIdElement().getIdPart(), equalTo(DIAGNOSTIC_REPORT_UUID));
		assertThat(updatedDiagnosticReport.getStatus(), is(DiagnosticReport.DiagnosticReportStatus.FINAL));
		assertThat(diagnosticReport, validResource());
		
		// double-check the record returned via get
		response = get("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).accept(FhirMediaTypes.JSON).go();
		DiagnosticReport reReadDiagnosticReport = readResponse(response);
		
		assertThat(reReadDiagnosticReport.getStatus(), is(DiagnosticReport.DiagnosticReportStatus.FINAL));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchPatientIdAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		DiagnosticReport diagnosticReport = readResponse(response);
		
		// update the existing record
		diagnosticReport.setId(WRONG_DIAGNOSTIC_REPORT_UUID);
		
		// send the update to the server
		response = put("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).jsonContent(toJson(diagnosticReport))
		        .accept(FhirMediaTypes.JSON).go();
		
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
		MockHttpServletResponse response = get("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		DiagnosticReport diagnosticReport = readResponse(response);
		
		// update the existing record
		diagnosticReport.setId(WRONG_DIAGNOSTIC_REPORT_UUID);
		
		// send the update to the server
		response = put("/DiagnosticReport/" + WRONG_DIAGNOSTIC_REPORT_UUID).jsonContent(toJson(diagnosticReport))
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldUpdateExistingDiagnosticReportAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).accept(FhirMediaTypes.XML)
		        .go();
		DiagnosticReport diagnosticReport = readResponse(response);
		
		// verify condition we will change
		assertThat(diagnosticReport.getStatus(), not(is(DiagnosticReport.DiagnosticReportStatus.FINAL)));
		
		// update the existing record
		diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);
		
		// send the update to the server
		response = put("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).xmlContext(toXML(diagnosticReport))
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read the updated record
		DiagnosticReport updatedDiagnosticReport = readResponse(response);
		
		assertThat(updatedDiagnosticReport, notNullValue());
		assertThat(updatedDiagnosticReport.getIdElement().getIdPart(), equalTo(DIAGNOSTIC_REPORT_UUID));
		assertThat(updatedDiagnosticReport.getStatus(), is(DiagnosticReport.DiagnosticReportStatus.FINAL));
		assertThat(diagnosticReport, validResource());
		
		// double-check the record returned via get
		response = get("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).accept(FhirMediaTypes.JSON).go();
		DiagnosticReport reReadDiagnosticReport = readResponse(response);
		
		assertThat(reReadDiagnosticReport.getStatus(), is(DiagnosticReport.DiagnosticReportStatus.FINAL));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchPatientIdAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).accept(FhirMediaTypes.XML)
		        .go();
		DiagnosticReport diagnosticReport = readResponse(response);
		
		// update the existing record
		diagnosticReport.setId(WRONG_DIAGNOSTIC_REPORT_UUID);
		
		// send the update to the server
		response = put("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).xmlContext(toXML(diagnosticReport))
		        .accept(FhirMediaTypes.XML).go();
		
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
		MockHttpServletResponse response = get("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).accept(FhirMediaTypes.XML)
		        .go();
		DiagnosticReport diagnosticReport = readResponse(response);
		
		// update the existing record
		diagnosticReport.setId(WRONG_DIAGNOSTIC_REPORT_UUID);
		
		// send the update to the server
		response = put("/DiagnosticReport/" + WRONG_DIAGNOSTIC_REPORT_UUID).xmlContext(toXML(diagnosticReport))
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldDeleteExistingDiagnosticReport() throws Exception {
		MockHttpServletResponse response = delete("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		
		response = get("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, statusEquals(HttpStatus.GONE));
	}
	
	@Test
	public void shouldReturnNotFoundWhenDeletingNonExistentDiagnosticReport() throws Exception {
		MockHttpServletResponse response = delete("/DiagnosticReport/" + WRONG_DIAGNOSTIC_REPORT_UUID)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldSearchForAllDiagnosticReportsAsJson() throws Exception {
		MockHttpServletResponse response = get("/DiagnosticReport").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/DiagnosticReport/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(DiagnosticReport.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldReturnSortedAndFilteredSearchResultsForPatientsAsJson() throws Exception {
		MockHttpServletResponse response = get("/DiagnosticReport?patient.given=Collet&_sort=-_lastUpdated")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasResource(
		    hasProperty("subject", hasProperty("reference", equalTo("Patient/5946f880-b197-400b-9caa-a3c661d23041"))))));
		assertThat(entries,
		    containsInRelativeOrder(
		        hasResource(hasProperty(
		            "meta",
		            hasProperty(
		                "lastUpdated",
		                equalTo(Date.from(
		                    LocalDateTime.of(2018, 8, 18, 14, 9, 35).atZone(ZoneId.systemDefault()).toInstant()))))),
		        hasResource(hasProperty("meta", hasProperty("lastUpdated", equalTo(
		            Date.from(LocalDateTime.of(2008, 8, 18, 14, 9, 35).atZone(ZoneId.systemDefault()).toInstant())))))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldSearchForAllDiagnosticReportsAsXML() throws Exception {
		MockHttpServletResponse response = get("/DiagnosticReport").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/DiagnosticReport/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(DiagnosticReport.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldReturnSortedAndFilteredSearchResultsForPatientsAsXML() throws Exception {
		MockHttpServletResponse response = get("/DiagnosticReport?patient.given=Collet&_sort=-_lastUpdated")
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasResource(
		    hasProperty("subject", hasProperty("reference", equalTo("Patient/5946f880-b197-400b-9caa-a3c661d23041"))))));
		assertThat(entries,
		    containsInRelativeOrder(
		        hasResource(hasProperty(
		            "meta",
		            hasProperty(
		                "lastUpdated",
		                equalTo(Date.from(
		                    LocalDateTime.of(2018, 8, 18, 14, 9, 35).atZone(ZoneId.systemDefault()).toInstant()))))),
		        hasResource(hasProperty("meta", hasProperty("lastUpdated", equalTo(
		            Date.from(LocalDateTime.of(2008, 8, 18, 14, 9, 35).atZone(ZoneId.systemDefault()).toInstant())))))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldReturnCountForDiagonosticReportAsJson() throws Exception {
		MockHttpServletResponse response = get("/DiagnosticReport?patient.given=Collet&_summary=count")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		
		assertThat(result, hasProperty("total", equalTo(2)));
	}
	
	@Test
	public void shouldReturnCountForDiagonosticReportAsXml() throws Exception {
		MockHttpServletResponse response = get("/DiagnosticReport?patient.given=Collet&_summary=count")
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		
		assertThat(result, hasProperty("total", equalTo(2)));
	}
}
