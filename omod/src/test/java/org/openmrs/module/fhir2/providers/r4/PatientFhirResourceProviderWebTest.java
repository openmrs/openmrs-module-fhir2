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
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hamcrest.MatcherAssert;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Provenance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class PatientFhirResourceProviderWebTest extends BaseFhirR4ResourceProviderWebTest<PatientFhirResourceProvider, Patient> {
	
	private static final String PATIENT_UUID = "0b42f99b-776e-4388-8f6f-84357ae2a8fb";
	
	private static final String BAD_PATIENT_UUID = "bb2354c1-e9e4-4020-bda0-d4a9f3232c9c";
	
	private static final String AUTHOR = "author";
	
	private static final String AUT = "AUT";
	
	private static final String LAST_UPDATED_DATE = "eq2020-09-03";
	
	private static final String JSON_CREATE_PATIENT_PATH = "org/openmrs/module/fhir2/providers/PatientWebTest_create.json";
	
	private static final String JSON_UPDATE_PATIENT_PATH = "org/openmrs/module/fhir2/providers/PatientWebTest_update.json";
	
	private static final String JSON_UPDATE_PATIENT_NO_ID_PATH = "org/openmrs/module/fhir2/providers/PatientWebTest_updateWithoutId.json";
	
	private static final String JSON_UPDATE_PATIENT_WRONG_ID_PATH = "org/openmrs/module/fhir2/providers/PatientWebTest_updateWithWrongId.json";
	
	@Getter(AccessLevel.PUBLIC)
	private PatientFhirResourceProvider resourceProvider;
	
	@Mock
	private FhirPatientService patientService;
	
	@Captor
	private ArgumentCaptor<StringAndListParam> stringAndListCaptor;
	
	@Captor
	private ArgumentCaptor<TokenAndListParam> tokenAndListCaptor;
	
	@Captor
	private ArgumentCaptor<DateRangeParam> dateRangeCaptor;
	
	@Captor
	private ArgumentCaptor<HashSet<Include>> includeArgumentCaptor;
	
	@Before
	public void setup() throws ServletException {
		resourceProvider = new PatientFhirResourceProvider();
		resourceProvider.setPatientService(patientService);
		super.setup();
	}
	
	@Test
	public void shouldGetPatientByUuid() throws Exception {
		Patient patient = new Patient();
		patient.setId(PATIENT_UUID);
		when(patientService.get(PATIENT_UUID)).thenReturn(patient);
		
		MockHttpServletResponse response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Patient resource = readResponse(response);
		assertThat(resource.getIdElement().getIdPart(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void shouldReturn404IfPatientNotFound() throws Exception {
		MockHttpServletResponse response = get("/Patient/" + BAD_PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void shouldGetPatientByName() throws Exception {
		verifyUri("/Patient/?name=Hannibal Lector");
		
		verify(patientService).searchForPatients(stringAndListCaptor.capture(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Hannibal Lector"));
	}
	
	@Test
	public void shouldGetPatientByGivenName() throws Exception {
		verifyUri("/Patient/?given=Hannibal");
		
		verify(patientService).searchForPatients(isNull(), stringAndListCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Hannibal"));
	}
	
	@Test
	public void shouldGetPatientByFamilyName() throws Exception {
		verifyUri("/Patient/?family=Lector");
		
		verify(patientService).searchForPatients(isNull(), isNull(), stringAndListCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Lector"));
	}
	
	@Test
	public void shouldGetPatientByIdentifier() throws Exception {
		verifyUri("/Patient/?identifier=M10000");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), tokenAndListCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(tokenAndListCaptor.getValue(), notNullValue());
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("M10000"));
	}
	
	@Test
	public void shouldGetPatientByGender() throws Exception {
		verifyUri("/Patient/?gender=male");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), tokenAndListCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(tokenAndListCaptor.getValue(), notNullValue());
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("male"));
	}
	
	@Test
	public void shouldGetPatientByBirthDate() throws Exception {
		verifyUri("/Patient/?birthdate=eq1975-02-02");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), dateRangeCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPatientByBirthDateGreaterThanOrEqualTo() throws Exception {
		verifyUri("/Patient/?birthdate=ge1975-02-02");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), dateRangeCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetPatientByBirthDateGreaterThan() throws Exception {
		verifyUri("/Patient/?birthdate=gt1975-02-02");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), dateRangeCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetPatientByBirthDateLessThanOrEqualTo() throws Exception {
		verifyUri("/Patient/?birthdate=le1975-02-02");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), dateRangeCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound(), nullValue());
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPatientByBirthDateLessThan() throws Exception {
		verifyUri("/Patient/?birthdate=lt1975-02-02");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), dateRangeCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound(), nullValue());
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPatientByBirthDateBetween() throws Exception {
		verifyUri("/Patient/?birthdate=ge1975-02-02&birthdate=le1980-02-02");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), dateRangeCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar lowerBound = Calendar.getInstance();
		lowerBound.set(1975, Calendar.FEBRUARY, 2);
		
		Calendar upperBound = Calendar.getInstance();
		upperBound.set(1980, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(lowerBound.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(upperBound.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPatientByDeathDate() throws Exception {
		verifyUri("/Patient/?death-date=eq1975-02-02");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    dateRangeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPatientByDeathDateGreaterThanOrEqualTo() throws Exception {
		verifyUri("/Patient/?death-date=ge1975-02-02");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    dateRangeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetPatientByDeathDateGreaterThan() throws Exception {
		verifyUri("/Patient/?death-date=gt1975-02-02");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    dateRangeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetPatientByDeathDateLessThanOrEqualTo() throws Exception {
		verifyUri("/Patient/?death-date=le1975-02-02");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    dateRangeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound(), nullValue());
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPatientByDeathDateLessThan() throws Exception {
		verifyUri("/Patient/?death-date=lt1975-02-02");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    dateRangeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound(), nullValue());
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPatientByDeathDateBetween() throws Exception {
		verifyUri("/Patient/?death-date=ge1975-02-02&death-date=le1980-02-02");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    dateRangeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar lowerBound = Calendar.getInstance();
		lowerBound.set(1975, Calendar.FEBRUARY, 2);
		
		Calendar upperBound = Calendar.getInstance();
		upperBound.set(1980, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(lowerBound.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(upperBound.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPatientByDeceased() throws Exception {
		verifyUri("/Patient/?deceased=true");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    tokenAndListCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(tokenAndListCaptor.getValue(), notNullValue());
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("true"));
	}
	
	@Test
	public void shouldGetPatientByCity() throws Exception {
		verifyUri("/Patient/?address-city=Washington");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), stringAndListCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Washington"));
	}
	
	@Test
	public void shouldGetPatientByState() throws Exception {
		verifyUri("/Patient/?address-state=Washington");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), stringAndListCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Washington"));
	}
	
	@Test
	public void shouldGetPatientByCountry() throws Exception {
		verifyUri("/Patient/?address-country=Washington");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), stringAndListCaptor.capture(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Washington"));
	}
	
	@Test
	public void shouldGetPatientByPostalCode() throws Exception {
		verifyUri("/Patient/?address-postalcode=98136");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), stringAndListCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("98136"));
	}
	
	@Test
	public void shouldGetPatientByUUID() throws Exception {
		verifyUri(String.format("/Patient?_id=%s", PATIENT_UUID));
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), tokenAndListCaptor.capture(), isNull(), isNull(), isNull());
		
		assertThat(tokenAndListCaptor.getValue(), notNullValue());
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PATIENT_UUID));
	}
	
	@Test
	public void shouldGetPatientByLastUpdatedDate() throws Exception {
		verifyUri(String.format("/Patient?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull());
		
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldAddReverseIncludedObservationsToReturnedResults() throws Exception {
		verifyUri("/Patient?_revinclude=Observation:patient");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(), equalTo(FhirConstants.OBSERVATION));
	}
	
	@Test
	public void shouldAddReverseIncludedAllergiesToReturnedResults() throws Exception {
		verifyUri("/Patient?_revinclude=AllergyIntolerance:patient");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(),
		    equalTo(FhirConstants.ALLERGY_INTOLERANCE));
	}
	
	@Test
	public void shouldAddReverseIncludedDiagnosticReportsToReturnedResults() throws Exception {
		verifyUri("/Patient?_revinclude=DiagnosticReport:patient");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(),
		    equalTo(FhirConstants.DIAGNOSTIC_REPORT));
	}
	
	@Test
	public void shouldAddReverseIncludedEncountersToReturnedResults() throws Exception {
		verifyUri("/Patient?_revinclude=Encounter:patient");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(), equalTo(FhirConstants.ENCOUNTER));
	}
	
	@Test
	public void shouldAddReverseIncludedMedicationRequestsToReturnedResults() throws Exception {
		verifyUri("/Patient?_revinclude=MedicationRequest:patient");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(),
		    equalTo(FhirConstants.MEDICATION_REQUEST));
	}
	
	@Test
	public void shouldAddReverseIncludedServiceRequestsToReturnedResults() throws Exception {
		verifyUri("/Patient?_revinclude=ServiceRequest:patient");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(),
		    equalTo(FhirConstants.SERVICE_REQUEST));
	}
	
	@Test
	public void shouldHandleMultipleReverseIncludes() throws Exception {
		verifyUri("/Patient?_revinclude=Observation:patient&_revinclude=AllergyIntolerance:patient");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(2));
		
		assertThat(includeArgumentCaptor.getValue(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_PATIENT_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.OBSERVATION)))));
		assertThat(includeArgumentCaptor.getValue(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_PATIENT_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.ALLERGY_INTOLERANCE)))));
	}
	
	@Test
	public void shouldVerifyGetPatientResourceHistoryUri() throws Exception {
		Patient patient = new Patient();
		patient.setId(PATIENT_UUID);
		when(patientService.get(PATIENT_UUID)).thenReturn(patient);
		
		MockHttpServletResponse response = getPatientHistoryRequest();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void shouldGetPatientResourceHistory() throws IOException, ServletException {
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.newUuid()));
		provenance.setRecorded(new Date());
		provenance.setActivity(new CodeableConcept().addCoding(
		    new Coding().setCode("CREATE").setSystem(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION).setDisplay("create")));
		provenance.addAgent(new Provenance.ProvenanceAgentComponent()
		        .setType(new CodeableConcept().addCoding(new Coding().setCode(AUT).setDisplay(AUTHOR)
		                .setSystem(FhirConstants.FHIR_TERMINOLOGY_PROVENANCE_PARTICIPANT_TYPE)))
		        .addRole(new CodeableConcept().addCoding(
		            new Coding().setCode("").setDisplay("").setSystem(FhirConstants.FHIR_TERMINOLOGY_PARTICIPATION_TYPE))));
		Patient patient = new Patient();
		patient.setId(PATIENT_UUID);
		patient.addContained(provenance);
		
		when(patientService.get(PATIENT_UUID)).thenReturn(patient);
		
		MockHttpServletResponse response = getPatientHistoryRequest();
		
		Bundle results = readBundleResponse(response);
		assertThat(results, notNullValue());
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getResourceType().name(),
		    equalTo(Provenance.class.getSimpleName()));
		
	}
	
	@Test
	public void shouldReturnBundleWithEmptyEntriesIfContainedIsEmpty() throws Exception {
		Patient patient = new Patient();
		patient.setId(PATIENT_UUID);
		patient.setContained(new ArrayList<>());
		when(patientService.get(PATIENT_UUID)).thenReturn(patient);
		
		MockHttpServletResponse response = getPatientHistoryRequest();
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(false));
	}
	
	@Test
	public void getPatientHistory_shouldReturn404IfPatientIdIsWrong() throws Exception {
		MockHttpServletResponse response = get("/Patient/" + BAD_PATIENT_UUID + "/_history").accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isNotFound());
	}
	
	private MockHttpServletResponse getPatientHistoryRequest() throws IOException, ServletException {
		return get("/Patient/" + PATIENT_UUID + "/_history").accept(FhirMediaTypes.JSON).go();
	}
	
	private void verifyUri(String uri) throws Exception {
		Patient patient = new Patient();
		patient.setId(PATIENT_UUID);
		when(patientService.searchForPatients(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
		    any(), any(), any(), any(), any()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void createPatient_shouldCreatePatient() throws Exception {
		String jsonPatient;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_PATIENT_PATH)) {
			Objects.requireNonNull(is);
			jsonPatient = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		Patient patient = new Patient();
		patient.setId(PATIENT_UUID);
		
		when(patientService.create(any(Patient.class))).thenReturn(patient);
		
		MockHttpServletResponse response = post("/Patient").jsonContent(jsonPatient).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isCreated());
	}
	
	@Test
	public void updatePatient_shouldUpdateExistingPatient() throws Exception {
		String jsonPatient;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_PATIENT_PATH)) {
			Objects.requireNonNull(is);
			jsonPatient = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		Patient patient = new Patient();
		patient.setId(PATIENT_UUID);
		
		when(patientService.update(anyString(), any(Patient.class))).thenReturn(patient);
		
		MockHttpServletResponse response = put("/Patient/" + PATIENT_UUID).jsonContent(jsonPatient)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
	}
	
	@Test
	public void updatePatient_shouldRaiseExceptionForNoId() throws Exception {
		String jsonPatient;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_PATIENT_NO_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonPatient = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/Patient/" + PATIENT_UUID).jsonContent(jsonPatient)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	public void updatePatient_shouldRaiseExceptionOnIdMismatch() throws Exception {
		String jsonPatient;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_PATIENT_WRONG_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonPatient = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/Patient/" + BAD_PATIENT_UUID).jsonContent(jsonPatient)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    containsStringIgnoringCase("body must contain an ID element which matches the request URL"));
	}
	
	@Test
	public void deletePatient_shouldDeletePatient() throws Exception {
		OperationOutcome retVal = new OperationOutcome();
		retVal.setId(PATIENT_UUID);
		retVal.getText().setDivAsString("Deleted successfully");
		
		Patient patient = new Patient();
		patient.setId(PATIENT_UUID);
		
		when(patientService.delete(PATIENT_UUID)).thenReturn(patient);
		
		MockHttpServletResponse response = delete("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void getPatientEverything_shouldHandlePatientId() throws Exception {
		verifyEverythingOperation("/Patient/" + PATIENT_UUID + "/$everything?");
		
		verify(patientService).getPatientEverything(tokenAndListCaptor.capture());
		
		assertThat(tokenAndListCaptor.getValue(), notNullValue());
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PATIENT_UUID));
	}
	
	private void verifyEverythingOperation(String uri) throws Exception {
		Patient patient = new Patient();
		patient.setId(PATIENT_UUID);
		
		when(patientService.getPatientEverything(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isOk());
		MatcherAssert.assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(PATIENT_UUID));
	}
	
}
