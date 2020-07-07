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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirMedicationRequestService;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class MedicationRequestFhirResourceProviderWebTest extends BaseFhirR3ResourceProviderWebTest<MedicationRequestFhirResourceProvider, MedicationRequest> {
	
	private static final String MEDICATION_REQUEST_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String WRONG_MEDICATION_REQUEST_UUID = "c0938432-1691-11df-97a5-7038fd32aaba";
	
	private static final String LAST_UPDATED_DATE = "eq2020-09-03";
	
	private static final String PATIENT_IDENTIFIER = "h43489-h";
	
	private static final String PATIENT_UUID = "d9bc6c12-6adc-4ca6-8bde-441ec1a1c344";
	
	private static final String PATIENT_GIVEN_NAME = "Hannibal";
	
	private static final String PATIENT_FAMILY_NAME = "Sid";
	
	private static final String ENCOUNTER_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";
	
	private static final String PARTICIPANT_GIVEN_NAME = "John";
	
	private static final String PARTICIPANT_FAMILY_NAME = "Doe";
	
	private static final String PARTICIPANT_IDENTIFIER = "1000WF";
	
	private static final String PARTICIPANT_UUID = "b566821c-1ad9-473b-836b-9e9c67688e02";
	
	private static final String MEDICATION_UUID = "c36006e5-9fbb-4f20-866b-0ece245615a1";
	
	private static final String CODE = "d1b98543-10ff-4911-83a2-b7f5fafe2751";
	
	private static final String JSON_CREATE_MEDICATION_REQUEST_PATH = "org/openmrs/module/fhir2/providers/MedicationRequestResourceWebTest_create.json";
	
	private static final String JSON_UPDATE_MEDICATION_REQUEST_PATH = "org/openmrs/module/fhir2/providers/MedicationRequestResourceWebTest_update.json";
	
	private static final String JSON_UPDATE_MEDICATION_REQUEST_NO_ID_PATH = "org/openmrs/module/fhir2/providers/MedicationRequestResourceWebTest_updateWithoutId.json";
	
	private static final String JSON_UPDATE_MEDICATION_REQUEST_WRONG_ID_PATH = "org/openmrs/module/fhir2/providers/MedicationRequestResourceWebTest_updateWithWrongId.json";
	
	@Mock
	private FhirMedicationRequestService fhirMedicationRequestService;
	
	@Getter(AccessLevel.PUBLIC)
	private MedicationRequestFhirResourceProvider resourceProvider;
	
	@Captor
	private ArgumentCaptor<TokenAndListParam> tokenAndListParamArgumentCaptor;
	
	@Captor
	private ArgumentCaptor<DateRangeParam> dateRangeParamArgumentCaptor;
	
	@Captor
	private ArgumentCaptor<ReferenceAndListParam> referenceAndListParamArgumentCaptor;
	
	private org.hl7.fhir.r4.model.MedicationRequest medicationRequest;
	
	@Before
	@Override
	public void setup() throws ServletException {
		resourceProvider = new MedicationRequestFhirResourceProvider();
		resourceProvider.setMedicationRequestService(fhirMedicationRequestService);
		medicationRequest = new org.hl7.fhir.r4.model.MedicationRequest();
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		super.setup();
	}
	
	@Test
	public void getMedicationRequestByUuid_shouldReturnMedication() throws Exception {
		when(fhirMedicationRequestService.get(MEDICATION_REQUEST_UUID)).thenReturn(medicationRequest);
		
		MockHttpServletResponse response = get("/MedicationRequest/" + MEDICATION_REQUEST_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void getMedicationRequestByUuid_shouldReturn404() throws Exception {
		when(fhirMedicationRequestService.get(WRONG_MEDICATION_REQUEST_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/MedicationRequest/" + WRONG_MEDICATION_REQUEST_UUID)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByUUID() throws Exception {
		verifyUri(String.format("/MedicationRequest?_id=%s", MEDICATION_REQUEST_UUID));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(isNull(), isNull(), isNull(), isNull(), isNull(),
		    tokenAndListParamArgumentCaptor.capture(), isNull());
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByLastUpdatedDate() throws Exception {
		verifyUri(String.format("/MedicationRequest?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), dateRangeParamArgumentCaptor.capture());
		
		assertThat(dateRangeParamArgumentCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(dateRangeParamArgumentCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeParamArgumentCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsBySubjectUUID() throws Exception {
		verifyUri(String.format("/MedicationRequest?subject=%s", PATIENT_UUID));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(referenceAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PATIENT_UUID));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    nullValue());
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsBySubjectIdentifier() throws Exception {
		verifyUri(String.format("/MedicationRequest?subject.identifier=%s", PATIENT_IDENTIFIER));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(referenceAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
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
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsBySubjectGivenName() throws Exception {
		verifyUri(String.format("/MedicationRequest?subject.given=%s", PATIENT_GIVEN_NAME));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(referenceAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
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
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsBySubjectFamilyName() throws Exception {
		verifyUri(String.format("/MedicationRequest?subject.family=%s", PATIENT_FAMILY_NAME));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(referenceAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
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
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsBySubjectName() throws Exception {
		verifyUri(String.format("/MedicationRequest?subject.name=%s", PATIENT_GIVEN_NAME));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(referenceAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
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
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsBySubjectGivenNameAndIdentifier()
	        throws Exception {
		verifyUri(String.format("/MedicationRequest?subject.given=%s&subject.identifier=%s", PATIENT_GIVEN_NAME,
		    PATIENT_IDENTIFIER));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(referenceAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(2));
		
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(),
		    hasItem(hasProperty("valuesAsQueryTokens", hasItem(allOf(hasProperty("chain", equalTo(Patient.SP_IDENTIFIER)),
		        hasProperty("value", equalTo(PATIENT_IDENTIFIER)))))));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(),
		    hasItem(hasProperty("valuesAsQueryTokens", hasItem(allOf(hasProperty("chain", equalTo(Patient.SP_GIVEN)),
		        hasProperty("value", equalTo(PATIENT_GIVEN_NAME)))))));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByPatientUUID() throws Exception {
		verifyUri(String.format("/MedicationRequest?patient=%s", PATIENT_UUID));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(referenceAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PATIENT_UUID));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    nullValue());
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByPatientIdentifier() throws Exception {
		verifyUri(String.format("/MedicationRequest?patient.identifier=%s", PATIENT_IDENTIFIER));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(referenceAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
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
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByPatientGivenName() throws Exception {
		verifyUri(String.format("/MedicationRequest?patient.given=%s", PATIENT_GIVEN_NAME));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(referenceAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
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
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByPatientFamilyName() throws Exception {
		verifyUri(String.format("/MedicationRequest?patient.family=%s", PATIENT_FAMILY_NAME));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(referenceAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
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
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByPatientName() throws Exception {
		verifyUri(String.format("/MedicationRequest?patient.name=%s", PATIENT_GIVEN_NAME));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(referenceAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
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
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByPatientGivenNameAndIdentifier()
	        throws Exception {
		verifyUri(String.format("/MedicationRequest?patient.given=%s&patient.identifier=%s", PATIENT_GIVEN_NAME,
		    PATIENT_IDENTIFIER));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(referenceAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(2));
		
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(),
		    hasItem(hasProperty("valuesAsQueryTokens", hasItem(allOf(hasProperty("chain", equalTo(Patient.SP_IDENTIFIER)),
		        hasProperty("value", equalTo(PATIENT_IDENTIFIER)))))));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(),
		    hasItem(hasProperty("valuesAsQueryTokens", hasItem(allOf(hasProperty("chain", equalTo(Patient.SP_GIVEN)),
		        hasProperty("value", equalTo(PATIENT_GIVEN_NAME)))))));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByEncounterUUID() throws Exception {
		verifyUri(String.format("/MedicationRequest?context=%s", ENCOUNTER_UUID));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(isNull(),
		    referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(null));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByParticipantUUID() throws Exception {
		verifyUri(String.format("/MedicationRequest?requester=%s", PARTICIPANT_UUID));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(isNull(), isNull(), isNull(),
		    referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PARTICIPANT_UUID));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    nullValue());
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByParticipantIdentifier() throws Exception {
		verifyUri(String.format("/MedicationRequest?requester.identifier=%s", PARTICIPANT_IDENTIFIER));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(isNull(), isNull(), isNull(),
		    referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull());
		
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
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByParticipantGivenName() throws Exception {
		verifyUri(String.format("/MedicationRequest?requester.given=%s", PARTICIPANT_GIVEN_NAME));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(isNull(), isNull(), isNull(),
		    referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull());
		
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
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByParticipantFamilyName() throws Exception {
		verifyUri(String.format("/MedicationRequest?requester.family=%s", PARTICIPANT_FAMILY_NAME));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(isNull(), isNull(), isNull(),
		    referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull());
		
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
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByParticipantName() throws Exception {
		verifyUri(String.format("/MedicationRequest?requester.name=%s", PARTICIPANT_GIVEN_NAME));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(isNull(), isNull(), isNull(),
		    referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull());
		
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
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByParticipantGivenNameAndIdentifier()
	        throws Exception {
		verifyUri(String.format("/MedicationRequest?requester.given=%s&requester.identifier=%s", PARTICIPANT_GIVEN_NAME,
		    PARTICIPANT_IDENTIFIER));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(isNull(), isNull(), isNull(),
		    referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(2));
		
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(),
		    hasItem(
		        hasProperty("valuesAsQueryTokens", hasItem(allOf(hasProperty("chain", equalTo(Practitioner.SP_IDENTIFIER)),
		            hasProperty("value", equalTo(PARTICIPANT_IDENTIFIER)))))));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(),
		    hasItem(hasProperty("valuesAsQueryTokens", hasItem(allOf(hasProperty("chain", equalTo(Practitioner.SP_GIVEN)),
		        hasProperty("value", equalTo(PARTICIPANT_GIVEN_NAME)))))));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByMedicationUUID() throws Exception {
		verifyUri(String.format("/MedicationRequest?medication=%s", MEDICATION_UUID));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(isNull(), isNull(), isNull(), isNull(),
		    referenceAndListParamArgumentCaptor.capture(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(MEDICATION_UUID));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    nullValue());
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByCode() throws Exception {
		verifyUri(String.format("/MedicationRequest?code=%s", CODE));
		
		verify(fhirMedicationRequestService).searchForMedicationRequests(isNull(), isNull(),
		    tokenAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(CODE));
	}
	
	private void verifyUri(String uri) throws Exception {
		MedicationRequest medicationRequest = new MedicationRequest();
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		when(fhirMedicationRequestService.searchForMedicationRequests(any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(medicationRequest), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void createMedicationRequest_shouldCreateNewMedicationRequest() throws Exception {
		String medicationRequestJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_MEDICATION_REQUEST_PATH)) {
			Objects.requireNonNull(is);
			medicationRequestJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		when(fhirMedicationRequestService.create(any(org.hl7.fhir.r4.model.MedicationRequest.class)))
		        .thenReturn(medicationRequest);
		
		MockHttpServletResponse response = post("/MedicationRequest").jsonContent(medicationRequestJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isCreated());
	}
	
	@Test
	public void updateMedicationRequest_shouldUpdateExistingMedicationRequest() throws Exception {
		String medicationRequestJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_MEDICATION_REQUEST_PATH)) {
			Objects.requireNonNull(is);
			medicationRequestJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		when(fhirMedicationRequestService.update(anyString(), any(org.hl7.fhir.r4.model.MedicationRequest.class)))
		        .thenReturn(medicationRequest);
		
		MockHttpServletResponse response = put("/MedicationRequest/" + MEDICATION_REQUEST_UUID)
		        .jsonContent(medicationRequestJson).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
	}
	
	@Test
	public void updateMedicationRequest_shouldThrowErrorForIdMismatch() throws Exception {
		String medicationRequestJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_MEDICATION_REQUEST_PATH)) {
			Objects.requireNonNull(is);
			medicationRequestJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/MedicationRequest/" + WRONG_MEDICATION_REQUEST_UUID)
		        .jsonContent(medicationRequestJson).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    containsStringIgnoringCase("body must contain an ID element which matches the request URL"));
	}
	
	@Test
	public void updateMedicationRequest_shouldThrowErrorForNoId() throws Exception {
		String medicationRequestJson;
		try (InputStream is = this.getClass().getClassLoader()
		        .getResourceAsStream(JSON_UPDATE_MEDICATION_REQUEST_NO_ID_PATH)) {
			Objects.requireNonNull(is);
			medicationRequestJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/MedicationRequest/" + MEDICATION_REQUEST_UUID)
		        .jsonContent(medicationRequestJson).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	public void updateMedicationRequest_shouldThrowErrorForNonExistentMedicationRequest() throws Exception {
		String medicationRequestJson;
		try (InputStream is = this.getClass().getClassLoader()
		        .getResourceAsStream(JSON_UPDATE_MEDICATION_REQUEST_WRONG_ID_PATH)) {
			Objects.requireNonNull(is);
			medicationRequestJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		when(fhirMedicationRequestService.update(anyString(), any(org.hl7.fhir.r4.model.MedicationRequest.class))).thenThrow(
		    new MethodNotAllowedException("Medication Request " + WRONG_MEDICATION_REQUEST_UUID + " does not exist"));
		
		MockHttpServletResponse response = put("/MedicationRequest/" + WRONG_MEDICATION_REQUEST_UUID)
		        .jsonContent(medicationRequestJson).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isMethodNotAllowed());
	}
	
	@Test
	public void deleteMedicationRequest_shouldDeleteMedicationRequest() throws Exception {
		when(fhirMedicationRequestService.delete(MEDICATION_REQUEST_UUID)).thenReturn(medicationRequest);
		
		MockHttpServletResponse response = delete("/MedicationRequest/" + MEDICATION_REQUEST_UUID)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void deleteMedicationRequest_shouldReturn404ForNonExistingMedicationRequest() throws Exception {
		when(fhirMedicationRequestService.delete(WRONG_MEDICATION_REQUEST_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = delete("/MedicationRequest/" + WRONG_MEDICATION_REQUEST_UUID)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getStatus(), equalTo(404));
	}
	
}
