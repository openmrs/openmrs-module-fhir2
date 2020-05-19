/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Collections;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirDiagnosticReportService;
import org.openmrs.module.fhir2.web.servlet.BaseFhirResourceProviderTest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class DiagnosticReportFhirResourceProviderWebTest extends BaseFhirResourceProviderTest<DiagnosticReportFhirResourceProvider, DiagnosticReport> {
	
	private static final String DIAGNOSTIC_REPORT_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";
	
	private static final String WRONG_UUID = "9bf0d1ac-62a8-4440-a5a1-eb1015a7cc65";
	
	private static final String JSON_DIAGNOSTIC_REPORT_PATH = "org/openmrs/module/fhir2/providers/TestDiagnosticReport_CreateUpdate.json";
	
	private static final String JSON_DIAGNOSTIC_REPORT_NO_ID_PATH = "org/openmrs/module/fhir2/providers/TestDiagnosticReport_CreateUpdate_NoId.json";
	
	private static final String JSON_DIAGNOSTIC_REPORT_WRONG_UUID_PATH = "org/openmrs/module/fhir2/providers/TestDiagnosticReport_CreateUpdate_WrongId.json";
	
	private static final String ENCOUNTER_UUID = "6519d653-393b-4118-9c83-a3715b82d4ac";
	
	private static final String PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041";
	
	private static final String PATIENT_GIVEN_NAME = "Collet";
	
	private static final String PATIENT_FAMILY_NAME = "Chebaskwony";
	
	private static final String DIAGNOSTIC_REPORT_CODE = "5497";
	
	private static final String PATIENT_IDENTIFIER = "6TS-4";
	
	@Mock
	private FhirDiagnosticReportService service;
	
	@Getter(AccessLevel.PUBLIC)
	private DiagnosticReportFhirResourceProvider resourceProvider;
	
	@Captor
	ArgumentCaptor<TokenAndListParam> tokenAndListParamCaptor;
	
	@Captor
	ArgumentCaptor<ReferenceAndListParam> referenceAndListParamCaptor;
	
	@Captor
	private ArgumentCaptor<DateRangeParam> dateRangeCaptor;
	
	@Before
	@Override
	public void setup() throws Exception {
		resourceProvider = new DiagnosticReportFhirResourceProvider();
		resourceProvider.setService(service);
		super.setup();
	}
	
	@Test
	public void getDiagnosticReportByUuid_shouldReturnDiagnosticReport() throws Exception {
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		diagnosticReport.setId(DIAGNOSTIC_REPORT_UUID);
		when(service.get(DIAGNOSTIC_REPORT_UUID)).thenReturn(diagnosticReport);
		
		MockHttpServletResponse response = get("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(DIAGNOSTIC_REPORT_UUID));
	}
	
	@Test
	public void getDiagnosticReportByWrongUuid_shouldReturn404() throws Exception {
		when(service.get(WRONG_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/DiagnosticReport/" + WRONG_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void createDiagnosticReport_shouldCreateNewDiagnosticReport() throws Exception {
		String jsonDiagnosticReport;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_DIAGNOSTIC_REPORT_PATH)) {
			jsonDiagnosticReport = IOUtils.toString(is);
		}
		
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		diagnosticReport.setId(DIAGNOSTIC_REPORT_UUID);
		
		when(service.create(any(DiagnosticReport.class))).thenReturn(diagnosticReport);
		
		MockHttpServletResponse response = post("/DiagnosticReport").jsonContent(jsonDiagnosticReport)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isCreated());
	}
	
	@Test
	public void updateDiagnosticReport_shouldUpdateExistingDiagnosticReport() throws Exception {
		String jsonDiagnosticReport;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_DIAGNOSTIC_REPORT_PATH)) {
			jsonDiagnosticReport = IOUtils.toString(is);
		}
		
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		diagnosticReport.setId(DIAGNOSTIC_REPORT_UUID);
		
		when(service.update(anyString(), any(DiagnosticReport.class))).thenReturn(diagnosticReport);
		
		MockHttpServletResponse response = put("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID)
		        .jsonContent(jsonDiagnosticReport).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
	}
	
	@Test
	public void updateDiagnosticReport_shouldErrorForIdMismatch() throws Exception {
		String jsonDiagnosticReport;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_DIAGNOSTIC_REPORT_PATH)) {
			jsonDiagnosticReport = IOUtils.toString(is);
		}
		
		MockHttpServletResponse response = put("/DiagnosticReport/" + WRONG_UUID).jsonContent(jsonDiagnosticReport)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    containsStringIgnoringCase("body must contain an ID element which matches the request URL"));
	}
	
	@Test
	public void updateDiagnosticReport_shouldErrorForNoId() throws Exception {
		String jsonDiagnosticReport;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_DIAGNOSTIC_REPORT_NO_ID_PATH)) {
			jsonDiagnosticReport = IOUtils.toString(is);
		}
		
		MockHttpServletResponse response = put("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID)
		        .jsonContent(jsonDiagnosticReport).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	public void updateDiagnosticReport_shouldErrorForNonexistentDiagnosticReport() throws Exception {
		String jsonDiagnosticReport;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_DIAGNOSTIC_REPORT_WRONG_UUID_PATH)) {
			jsonDiagnosticReport = IOUtils.toString(is);
		}
		
		when(service.update(eq(WRONG_UUID), any(DiagnosticReport.class)))
		        .thenThrow(new MethodNotAllowedException("DiagnosticReport " + WRONG_UUID + " does not exist"));
		
		MockHttpServletResponse response = put("/DiagnosticReport/" + WRONG_UUID).jsonContent(jsonDiagnosticReport)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isMethodNotAllowed());
	}
	
	@Test
	public void findDiagnosticReports_shouldReturnBundleOfDiagnosticReportsWithMatchingEncounterUUID() throws Exception {
		verifyUri(String.format("/DiagnosticReport?encounter=%s", ENCOUNTER_UUID));
		
		verify(service).searchForDiagnosticReports(referenceAndListParamCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(ENCOUNTER_UUID));
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo(null));
	}
	
	@Test
	public void findDiagnosticReports_shouldReturnBundleOfDiagnosticReportsWithMatchingPatientUUID() throws Exception {
		verifyUri(String.format("/DiagnosticReport?patient=%s", PATIENT_UUID));
		
		verify(service).searchForDiagnosticReports(isNull(), referenceAndListParamCaptor.capture(), isNull(), isNull(),
		    isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PATIENT_UUID));
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo(null));
	}
	
	@Test
	public void findDiagnosticReports_shouldReturnBundleOfDiagnosticReportsWithMatchingPatientIdentifier() throws Exception {
		verifyUri(String.format("/DiagnosticReport?patient.identifier=%s", PATIENT_IDENTIFIER));
		
		verify(service).searchForDiagnosticReports(isNull(), referenceAndListParamCaptor.capture(), isNull(), isNull(),
		    isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PATIENT_IDENTIFIER));
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo(Patient.SP_IDENTIFIER));
	}
	
	@Test
	public void findDiagnosticReports_shouldReturnBundleOfDiagnosticReportsWithMatchingPatientName() throws Exception {
		verifyUri(String.format("/DiagnosticReport?patient.name=%s", PATIENT_GIVEN_NAME));
		
		verify(service).searchForDiagnosticReports(isNull(), referenceAndListParamCaptor.capture(), isNull(), isNull(),
		    isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PATIENT_GIVEN_NAME));
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo(Patient.SP_NAME));
	}
	
	@Test
	public void findDiagnosticReports_shouldReturnBundleOfDiagnosticReportsWithMatchingPatientGivenName() throws Exception {
		verifyUri(String.format("/DiagnosticReport?patient.given=%s", PATIENT_GIVEN_NAME));
		
		verify(service).searchForDiagnosticReports(isNull(), referenceAndListParamCaptor.capture(), isNull(), isNull(),
		    isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PATIENT_GIVEN_NAME));
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo(Patient.SP_GIVEN));
	}
	
	@Test
	public void findDiagnosticReports_shouldReturnBundleOfDiagnosticReportsWithMatchingPatientFamilyName() throws Exception {
		verifyUri(String.format("/DiagnosticReport?patient.family=%s", PATIENT_FAMILY_NAME));
		
		verify(service).searchForDiagnosticReports(isNull(), referenceAndListParamCaptor.capture(), isNull(), isNull(),
		    isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PATIENT_FAMILY_NAME));
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo(Patient.SP_FAMILY));
	}
	
	@Test
	public void findDiagnosticReports_shouldReturnBundleOfDiagnosticReportsWithMatchingCode() throws Exception {
		verifyUri(String.format("/DiagnosticReport?code=%s", DIAGNOSTIC_REPORT_CODE));
		
		verify(service).searchForDiagnosticReports(isNull(), isNull(), isNull(), tokenAndListParamCaptor.capture(),
		    isNull());
		assertThat(tokenAndListParamCaptor.getValue(), notNullValue());
		assertThat(
		    tokenAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(DIAGNOSTIC_REPORT_CODE));
	}
	
	@Test
	public void findDiagnosticReports_shouldReturnBundleOfDiagnosticReportsWithMatchingIssueDate() throws Exception {
		verifyUri("/DiagnosticReport?issued=eq2008-08-18");
		
		verify(service).searchForDiagnosticReports(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2008, 7, 18);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void findDiagnosticReports_shouldReturnBundleOfDiagnosticReportsWithIssueDateGreaterThanOrEqualTo()
	        throws Exception {
		verifyUri("/DiagnosticReport?issued=ge2008-08-18");
		
		verify(service).searchForDiagnosticReports(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2008, 7, 18);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void findDiagnosticReports_shouldReturnBundleOfDiagnosticReportsWithIssueDateGreaterThan() throws Exception {
		verifyUri("/DiagnosticReport?issued=gt2008-08-18");
		
		verify(service).searchForDiagnosticReports(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2008, 7, 18);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void findDiagnosticReports_shouldReturnBundleOfDiagnosticReportsWithIssueDateLessThanOrEqualTo()
	        throws Exception {
		verifyUri("/DiagnosticReport?issued=le2008-08-18");
		
		verify(service).searchForDiagnosticReports(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2008, 7, 18);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound(), nullValue());
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void findDiagnosticReports_shouldReturnBundleOfDiagnosticReportsWithIssueDateLessThan() throws Exception {
		verifyUri("/DiagnosticReport?issued=lt2008-08-18");
		
		verify(service).searchForDiagnosticReports(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2008, 7, 18);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound(), nullValue());
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void findDiagnosticReports_shouldReturnBundleWithIssueDateBetween() throws Exception {
		verifyUri("/DiagnosticReport?issued=ge2008-08-18&issued=le2009-07-21");
		
		verify(service).searchForDiagnosticReports(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar lowerBound = Calendar.getInstance();
		lowerBound.set(2008, 7, 18);
		Calendar upperBound = Calendar.getInstance();
		upperBound.set(2009, 6, 21);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(lowerBound.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(upperBound.getTime(), Calendar.DATE)));
	}
	
	private void verifyUri(String uri) throws Exception {
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		diagnosticReport.setId(DIAGNOSTIC_REPORT_UUID);
		when(service.searchForDiagnosticReports(any(), any(), any(), any(), any()))
		        .thenReturn(Collections.singletonList(diagnosticReport));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(DIAGNOSTIC_REPORT_UUID));
	}
}
