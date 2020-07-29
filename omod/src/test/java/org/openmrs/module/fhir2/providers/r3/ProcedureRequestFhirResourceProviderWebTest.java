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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import java.util.Calendar;
import java.util.Collections;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang.time.DateUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirServiceRequestService;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class ProcedureRequestFhirResourceProviderWebTest extends BaseFhirR3ResourceProviderWebTest<ProcedureRequestFhirResourceProvider, ProcedureRequest> {
	
	private static final String SERVICE_REQUEST_UUID = "7d13b03b-58c2-43f5-b34d-08750c51aea9";
	
	private static final String WRONG_SERVICE_REQUEST_UUID = "92b04062-e57d-43aa-8c38-90a1ad70080c";
	
	private static final String LAST_UPDATED_DATE = "eq2020-09-03";
	
	private static final String PATIENT_IDENTIFIER = "h43489-h";
	
	private static final String PATIENT_UUID = "d9bc6c12-6adc-4ca6-8bde-441ec1a1c344";
	
	private static final String PATIENT_GIVEN_NAME = "Hannibal";
	
	private static final String PATIENT_FAMILY_NAME = "Sid";
	
	private static final String PARTICIPANT_GIVEN_NAME = "John";
	
	private static final String PARTICIPANT_FAMILY_NAME = "Doe";
	
	private static final String PARTICIPANT_IDENTIFIER = "1000WF";
	
	private static final String PARTICIPANT_UUID = "b566821c-1ad9-473b-836b-9e9c67688e02";
	
	private static final String ENCOUNTER_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";
	
	private static final String CODE = "5097";
	
	private static final String OCCURRENCE_DATE = "2010-03-31";
	
	@Getter(AccessLevel.PUBLIC)
	private ProcedureRequestFhirResourceProvider resourceProvider;
	
	@Mock
	private FhirServiceRequestService service;
	
	@Captor
	private ArgumentCaptor<TokenAndListParam> tokenAndListParamArgumentCaptor;
	
	@Captor
	private ArgumentCaptor<ReferenceAndListParam> referenceAndListParamArgumentCaptor;
	
	@Captor
	private ArgumentCaptor<DateRangeParam> dateRangeParamArgumentCaptor;
	
	private ProcedureRequest procedureRequest;
	
	@Before
	@Override
	public void setup() throws ServletException {
		resourceProvider = new ProcedureRequestFhirResourceProvider();
		resourceProvider.setServiceRequestService(service);
		procedureRequest = new ProcedureRequest();
		procedureRequest.setId(SERVICE_REQUEST_UUID);
		super.setup();
	}
	
	@Test
	public void getServiceRequestById_shouldReturnServiceRequest() throws Exception {
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);
		
		when(service.get(SERVICE_REQUEST_UUID)).thenReturn(serviceRequest);
		
		MockHttpServletResponse response = get("/ProcedureRequest/" + SERVICE_REQUEST_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void getEncounterByWrongUuid_shouldReturn404() throws Exception {
		MockHttpServletResponse response = get("/ProcedureRequest/" + WRONG_SERVICE_REQUEST_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByPatientUUID() throws Exception {
		verifyUri(String.format("/ProcedureRequest?patient=%s", PATIENT_UUID));
		
		verify(service).searchForServiceRequests(referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PATIENT_UUID));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(null));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByPatientName() throws Exception {
		verifyUri(String.format("/ProcedureRequest?patient.name=%s", PATIENT_GIVEN_NAME));
		
		verify(service).searchForServiceRequests(referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PATIENT_GIVEN_NAME));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(Patient.SP_NAME));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByPatientGivenName() throws Exception {
		verifyUri(String.format("/ProcedureRequest?patient.given=%s", PATIENT_GIVEN_NAME));
		
		verify(service).searchForServiceRequests(referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PATIENT_GIVEN_NAME));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(Patient.SP_GIVEN));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByPatientFamilyName() throws Exception {
		verifyUri(String.format("/ProcedureRequest?patient.family=%s", PATIENT_FAMILY_NAME));
		
		verify(service).searchForServiceRequests(referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PATIENT_FAMILY_NAME));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(Patient.SP_FAMILY));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByPatientIdentifier() throws Exception {
		verifyUri(String.format("/ProcedureRequest?patient.identifier=%s", PATIENT_IDENTIFIER));
		
		verify(service).searchForServiceRequests(referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PATIENT_IDENTIFIER));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(Patient.SP_IDENTIFIER));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsBySubjectUUID() throws Exception {
		verifyUri(String.format("/ProcedureRequest?subject=%s", PATIENT_UUID));
		
		verify(service).searchForServiceRequests(referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PATIENT_UUID));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(null));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsBySubjectName() throws Exception {
		verifyUri(String.format("/ProcedureRequest?subject.name=%s", PATIENT_GIVEN_NAME));
		
		verify(service).searchForServiceRequests(referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PATIENT_GIVEN_NAME));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(Patient.SP_NAME));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsBySubjectGivenName() throws Exception {
		verifyUri(String.format("/ProcedureRequest?subject.given=%s", PATIENT_GIVEN_NAME));
		
		verify(service).searchForServiceRequests(referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PATIENT_GIVEN_NAME));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(Patient.SP_GIVEN));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsBySubjectFamilyName() throws Exception {
		verifyUri(String.format("/ProcedureRequest?subject.family=%s", PATIENT_FAMILY_NAME));
		
		verify(service).searchForServiceRequests(referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PATIENT_FAMILY_NAME));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(Patient.SP_FAMILY));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsBySubjectIdentifier() throws Exception {
		verifyUri(String.format("/ProcedureRequest?subject.identifier=%s", PATIENT_IDENTIFIER));
		
		verify(service).searchForServiceRequests(referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PATIENT_IDENTIFIER));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(Patient.SP_IDENTIFIER));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByParticipantUUID() throws Exception {
		verifyUri(String.format("/ProcedureRequest?requester=%s", PARTICIPANT_UUID));
		
		verify(service).searchForServiceRequests(isNull(), isNull(), isNull(), referenceAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PARTICIPANT_UUID));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(null));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByParticipantName() throws Exception {
		verifyUri(String.format("/ProcedureRequest?requester.name=%s", PARTICIPANT_GIVEN_NAME));
		
		verify(service).searchForServiceRequests(isNull(), isNull(), isNull(), referenceAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PARTICIPANT_GIVEN_NAME));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(Practitioner.SP_NAME));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByParticipantGivenName() throws Exception {
		verifyUri(String.format("/ProcedureRequest?requester.given=%s", PARTICIPANT_GIVEN_NAME));
		
		verify(service).searchForServiceRequests(isNull(), isNull(), isNull(), referenceAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PARTICIPANT_GIVEN_NAME));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(Practitioner.SP_GIVEN));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByParticipantFamilyName() throws Exception {
		verifyUri(String.format("/ProcedureRequest?requester.family=%s", PARTICIPANT_FAMILY_NAME));
		
		verify(service).searchForServiceRequests(isNull(), isNull(), isNull(), referenceAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PARTICIPANT_FAMILY_NAME));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(Practitioner.SP_FAMILY));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByParticipantIdentifier() throws Exception {
		verifyUri(String.format("/ProcedureRequest?requester.identifier=%s", PARTICIPANT_IDENTIFIER));
		
		verify(service).searchForServiceRequests(isNull(), isNull(), isNull(), referenceAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PARTICIPANT_IDENTIFIER));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(Practitioner.SP_IDENTIFIER));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByEncounterUUID() throws Exception {
		verifyUri(String.format("/ProcedureRequest?encounter=%s", ENCOUNTER_UUID));
		
		verify(service).searchForServiceRequests(isNull(), isNull(), referenceAndListParamArgumentCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(ENCOUNTER_UUID));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(null));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByCode() throws Exception {
		verifyUri(String.format("/ProcedureRequest?code=%s", CODE));
		
		verify(service).searchForServiceRequests(isNull(), tokenAndListParamArgumentCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(CODE));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByOccurrence() throws Exception {
		verifyUri(String.format("/ProcedureRequest?occurrence=eq%s", OCCURRENCE_DATE));
		
		verify(service).searchForServiceRequests(isNull(), isNull(), isNull(), isNull(),
		    dateRangeParamArgumentCaptor.capture(), isNull(), isNull());
		
		assertThat(dateRangeParamArgumentCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2010, Calendar.MARCH, 31);
		
		assertThat(dateRangeParamArgumentCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeParamArgumentCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void searchForServiceRequests_shouldHandleComplexQuery() throws Exception {
		verifyUri(
		    String.format("/ProcedureRequest?requester.given=%s&occurrence=eq%s", PARTICIPANT_GIVEN_NAME, OCCURRENCE_DATE));
		
		verify(service).searchForServiceRequests(isNull(), isNull(), isNull(), referenceAndListParamArgumentCaptor.capture(),
		    dateRangeParamArgumentCaptor.capture(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PARTICIPANT_GIVEN_NAME));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(Practitioner.SP_GIVEN));
		
		assertThat(dateRangeParamArgumentCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2010, Calendar.MARCH, 31);
		
		assertThat(dateRangeParamArgumentCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeParamArgumentCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void searchForProcedureRequests_shouldSearchForProcedureRequestsByUUID() throws Exception {
		verifyUri(String.format("/ProcedureRequest?_id=%s", SERVICE_REQUEST_UUID));
		
		verify(service).searchForServiceRequests(isNull(), isNull(), isNull(), isNull(), isNull(),
		    tokenAndListParamArgumentCaptor.capture(), isNull());
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void searchForProcedureRequests_shouldSearchForProcedureRequestsByLastUpdatedDate() throws Exception {
		verifyUri(String.format("/ProcedureRequest?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(service).searchForServiceRequests(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    dateRangeParamArgumentCaptor.capture());
		
		assertThat(dateRangeParamArgumentCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(dateRangeParamArgumentCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeParamArgumentCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	private void verifyUri(String uri) throws Exception {
		when(service.searchForServiceRequests(any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(procedureRequest), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
	}
}
