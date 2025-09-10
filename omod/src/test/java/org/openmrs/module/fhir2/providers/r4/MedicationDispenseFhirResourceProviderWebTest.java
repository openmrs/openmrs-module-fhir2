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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import java.util.Calendar;
import java.util.Collections;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang.time.DateUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirMedicationDispenseService;
import org.openmrs.module.fhir2.api.search.param.MedicationDispenseSearchParams;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class MedicationDispenseFhirResourceProviderWebTest extends BaseFhirR4ResourceProviderWebTest<MedicationDispenseFhirResourceProvider, MedicationDispense> {
	
	private static final String MEDICATION_DISPENSE_UUID = "294face4-a498-4ba3-89a1-ffc505837026";
	
	private static final String WRONG_MEDICATION_DISPENSE_UUID = "391deb85-94a7-4596-84fa-bc178efa9918";
	
	private static final String LAST_UPDATED_DATE = "eq2020-09-03";
	
	private static final String PATIENT_IDENTIFIER = "h43489-h";
	
	private static final String PATIENT_UUID = "d9bc6c12-6adc-4ca6-8bde-441ec1a1c344";
	
	private static final String PATIENT_GIVEN_NAME = "Hannibal";
	
	private static final String PATIENT_FAMILY_NAME = "Sid";
	
	private static final String ENCOUNTER_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";
	
	private static final String MEDICATION_REQUEST_UUID = "c36006e5-9fbb-4f20-866b-0ece245615a1";
	
	@Mock
	private FhirMedicationDispenseService fhirMedicationDispenseService;
	
	@Captor
	private ArgumentCaptor<MedicationDispenseSearchParams> argumentCaptor;
	
	@Getter(AccessLevel.PUBLIC)
	private MedicationDispenseFhirResourceProvider resourceProvider;
	
	private MedicationDispense medicationDispense;
	
	@Before
	@Override
	public void setup() throws ServletException {
		resourceProvider = new MedicationDispenseFhirResourceProvider();
		resourceProvider.setFhirMedicationDispenseService(fhirMedicationDispenseService);
		medicationDispense = new MedicationDispense();
		medicationDispense.setId(MEDICATION_DISPENSE_UUID);
		super.setup();
	}
	
	@Test
	public void getMedicationDispenseByUuid_shouldReturnMedication() throws Exception {
		MedicationDispense medicationDispense = new MedicationDispense();
		medicationDispense.setId(MEDICATION_DISPENSE_UUID);
		when(fhirMedicationDispenseService.get(MEDICATION_DISPENSE_UUID)).thenReturn(medicationDispense);
		
		MockHttpServletResponse response = get("/MedicationDispense/" + MEDICATION_DISPENSE_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), containsString(FhirMediaTypes.JSON.toString()));
		assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(MEDICATION_DISPENSE_UUID));
	}
	
	@Test
	public void getMedicationDispenseByUuid_shouldReturn404() throws Exception {
		when(fhirMedicationDispenseService.get(WRONG_MEDICATION_DISPENSE_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/MedicationDispense/" + WRONG_MEDICATION_DISPENSE_UUID)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void searchForMedicationDispenses_shouldSearchForMedicationDispensesByUUID() throws Exception {
		verifyUri(String.format("/MedicationDispense?_id=%s", MEDICATION_DISPENSE_UUID));
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue(), notNullValue());
		assertThat(argumentCaptor.getValue().getId().getValuesAsQueryTokens(), not(empty()));
		assertThat(
		    argumentCaptor.getValue().getId().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(MEDICATION_DISPENSE_UUID));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldSearchForMedicationDispensesByLastUpdatedDate() throws Exception {
		verifyUri(String.format("/MedicationDispense?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(argumentCaptor.getValue().getLastUpdated().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(argumentCaptor.getValue().getLastUpdated().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldSearchForMedicationDispensesBySubjectUUID() throws Exception {
		verifyUri(String.format("/MedicationDispense?subject=%s", PATIENT_UUID));
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getPatient(), notNullValue());
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens(), not(empty()));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PATIENT_UUID));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    nullValue());
	}
	
	@Test
	public void searchForMedicationDispenses_shouldSearchForMedicationDispensesBySubjectIdentifier() throws Exception {
		verifyUri(String.format("/MedicationDispense?subject.identifier=%s", PATIENT_IDENTIFIER));
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		assertThat(argumentCaptor.getValue().getPatient(), notNullValue());
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens(), not(empty()));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PATIENT_IDENTIFIER));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo(Patient.SP_IDENTIFIER));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldSearchForMedicationDispensesBySubjectGivenName() throws Exception {
		verifyUri(String.format("/MedicationDispense?subject.given=%s", PATIENT_GIVEN_NAME));
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getPatient(), notNullValue());
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens(), not(empty()));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PATIENT_GIVEN_NAME));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo(Patient.SP_GIVEN));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldSearchForMedicationDispensesBySubjectFamilyName() throws Exception {
		verifyUri(String.format("/MedicationDispense?subject.family=%s", PATIENT_FAMILY_NAME));
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getPatient(), notNullValue());
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens(), not(empty()));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PATIENT_FAMILY_NAME));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo(Patient.SP_FAMILY));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldSearchForMedicationDispensesBySubjectName() throws Exception {
		verifyUri(String.format("/MedicationDispense?subject.name=%s", PATIENT_GIVEN_NAME));
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getPatient(), notNullValue());
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens(), not(empty()));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PATIENT_GIVEN_NAME));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo(Patient.SP_NAME));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldSearchForMedicationDispensesBySubjectGivenNameAndIdentifier()
	        throws Exception {
		verifyUri(String.format("/MedicationDispense?subject.given=%s&subject.identifier=%s", PATIENT_GIVEN_NAME,
		    PATIENT_IDENTIFIER));
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getPatient(), notNullValue());
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens(), not(empty()));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().size(), equalTo(2));
		
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens(),
		    hasItem(hasProperty("valuesAsQueryTokens", hasItem(allOf(hasProperty("chain", equalTo(Patient.SP_IDENTIFIER)),
		        hasProperty("value", equalTo(PATIENT_IDENTIFIER)))))));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens(),
		    hasItem(hasProperty("valuesAsQueryTokens", hasItem(allOf(hasProperty("chain", equalTo(Patient.SP_GIVEN)),
		        hasProperty("value", equalTo(PATIENT_GIVEN_NAME)))))));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldSearchForMedicationDispensesByPatientUUID() throws Exception {
		verifyUri(String.format("/MedicationDispense?patient=%s", PATIENT_UUID));
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getPatient(), notNullValue());
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens(), not(empty()));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PATIENT_UUID));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    nullValue());
	}
	
	@Test
	public void searchForMedicationDispenses_shouldSearchForMedicationDispensesByPatientIdentifier() throws Exception {
		verifyUri(String.format("/MedicationDispense?patient.identifier=%s", PATIENT_IDENTIFIER));
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getPatient(), notNullValue());
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens(), not(empty()));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PATIENT_IDENTIFIER));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo(Patient.SP_IDENTIFIER));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldSearchForMedicationDispensesByPatientGivenName() throws Exception {
		verifyUri(String.format("/MedicationDispense?patient.given=%s", PATIENT_GIVEN_NAME));
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getPatient(), notNullValue());
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens(), not(empty()));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PATIENT_GIVEN_NAME));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo(Patient.SP_GIVEN));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldSearchForMedicationDispensesByPatientFamilyName() throws Exception {
		verifyUri(String.format("/MedicationDispense?patient.family=%s", PATIENT_FAMILY_NAME));
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getPatient(), notNullValue());
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens(), not(empty()));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PATIENT_FAMILY_NAME));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo(Patient.SP_FAMILY));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldSearchForMedicationDispensesByPatientName() throws Exception {
		verifyUri(String.format("/MedicationDispense?patient.name=%s", PATIENT_GIVEN_NAME));
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getPatient(), notNullValue());
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens(), not(empty()));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PATIENT_GIVEN_NAME));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo(Patient.SP_NAME));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldSearchForMedicationDispensesByPatientGivenNameAndIdentifier()
	        throws Exception {
		verifyUri(String.format("/MedicationDispense?patient.given=%s&patient.identifier=%s", PATIENT_GIVEN_NAME,
		    PATIENT_IDENTIFIER));
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getPatient(), notNullValue());
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens(), not(empty()));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens().size(), equalTo(2));
		
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens(),
		    hasItem(hasProperty("valuesAsQueryTokens", hasItem(allOf(hasProperty("chain", equalTo(Patient.SP_IDENTIFIER)),
		        hasProperty("value", equalTo(PATIENT_IDENTIFIER)))))));
		assertThat(argumentCaptor.getValue().getPatient().getValuesAsQueryTokens(),
		    hasItem(hasProperty("valuesAsQueryTokens", hasItem(allOf(hasProperty("chain", equalTo(Patient.SP_GIVEN)),
		        hasProperty("value", equalTo(PATIENT_GIVEN_NAME)))))));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldSearchForMedicationDispensesByEncounterUUID() throws Exception {
		verifyUri(String.format("/MedicationDispense?context=%s", ENCOUNTER_UUID));
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue(), notNullValue());
		assertThat(argumentCaptor.getValue().getEncounter().getValuesAsQueryTokens(), not(empty()));
		assertThat(argumentCaptor.getValue().getEncounter().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo(null));
		assertThat(argumentCaptor.getValue().getEncounter().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldSearchForMedicationDispensesByMedicationRequestUUID() throws Exception {
		verifyUri(String.format("/MedicationDispense?prescription=%s", MEDICATION_REQUEST_UUID));
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getMedicationRequest(), notNullValue());
		assertThat(argumentCaptor.getValue().getMedicationRequest().getValuesAsQueryTokens(), not(empty()));
		assertThat(argumentCaptor.getValue().getMedicationRequest().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(MEDICATION_REQUEST_UUID));
		assertThat(argumentCaptor.getValue().getMedicationRequest().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    nullValue());
	}
	
	@Test
	public void searchForMedicationDispenses_shouldAddRelatedPatientWhenIncluded() throws Exception {
		verifyUri("/MedicationDispense?_include=MedicationDispense:patient");
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getIncludes(), notNullValue());
		assertThat(argumentCaptor.getValue().getIncludes().size(), equalTo(1));
		assertThat(argumentCaptor.getValue().getIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(argumentCaptor.getValue().getIncludes().iterator().next().getParamType(),
		    equalTo(FhirConstants.MEDICATION_DISPENSE));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldAddRelatedEncounterWhenIncluded() throws Exception {
		verifyUri("/MedicationDispense?_include=MedicationDispense:context");
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getIncludes(), notNullValue());
		assertThat(argumentCaptor.getValue().getIncludes().size(), equalTo(1));
		assertThat(argumentCaptor.getValue().getIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_CONTEXT_PARAM));
		assertThat(argumentCaptor.getValue().getIncludes().iterator().next().getParamType(),
		    equalTo(FhirConstants.MEDICATION_DISPENSE));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldAddRelatedMedicationRequestWhenIncluded() throws Exception {
		verifyUri("/MedicationDispense?_include=MedicationDispense:prescription");
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getIncludes(), notNullValue());
		assertThat(argumentCaptor.getValue().getIncludes().size(), equalTo(1));
		assertThat(argumentCaptor.getValue().getIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PRESCRIPTION_PARAM));
		assertThat(argumentCaptor.getValue().getIncludes().iterator().next().getParamType(),
		    equalTo(FhirConstants.MEDICATION_DISPENSE));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldAddRelatedMedicationWhenIncluded() throws Exception {
		verifyUri("/MedicationDispense?_include=MedicationDispense:medication");
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getIncludes(), notNullValue());
		assertThat(argumentCaptor.getValue().getIncludes().size(), equalTo(1));
		assertThat(argumentCaptor.getValue().getIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_MEDICATION_PARAM));
		assertThat(argumentCaptor.getValue().getIncludes().iterator().next().getParamType(),
		    equalTo(FhirConstants.MEDICATION_DISPENSE));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldAddRelatedPerformerWhenIncluded() throws Exception {
		verifyUri("/MedicationDispense?_include=MedicationDispense:performer");
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getIncludes(), notNullValue());
		assertThat(argumentCaptor.getValue().getIncludes().size(), equalTo(1));
		assertThat(argumentCaptor.getValue().getIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PERFORMER_PARAM));
		assertThat(argumentCaptor.getValue().getIncludes().iterator().next().getParamType(),
		    equalTo(FhirConstants.MEDICATION_DISPENSE));
	}
	
	@Test
	public void searchForMedicationDispenses_shouldHandleMultipleIncludes() throws Exception {
		verifyUri("/MedicationDispense?_include=MedicationDispense:medication&_include=MedicationDispense:context");
		
		verify(fhirMedicationDispenseService).searchMedicationDispenses(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getIncludes(), notNullValue());
		assertThat(argumentCaptor.getValue().getIncludes().size(), equalTo(2));
		assertThat(argumentCaptor.getValue().getIncludes(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_MEDICATION_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.MEDICATION_DISPENSE)))));
		assertThat(argumentCaptor.getValue().getIncludes(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_CONTEXT_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.MEDICATION_DISPENSE)))));
	}
	
	private void verifyUri(String uri) throws Exception {
		when(fhirMedicationDispenseService.searchMedicationDispenses(argumentCaptor.capture()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(medicationDispense), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), containsString(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(MEDICATION_DISPENSE_UUID));
	}
}
