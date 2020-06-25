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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
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
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class PatientFhirR3ResourceProviderWebTest extends BaseFhirR3ResourceProviderWebTest<PatientFhirResourceProvider, Patient> {
	
	private static final String PATIENT_UUID = "0b42f99b-776e-4388-8f6f-84357ae2a8fb";
	
	private static final String BAD_PATIENT_UUID = "bb2354c1-e9e4-4020-bda0-d4a9f3232c9c";
	
	private static final String AUTHOR = "author";
	
	private static final String AUT = "AUT";
	
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
	
	@Before
	public void setup() throws ServletException {
		resourceProvider = new PatientFhirResourceProvider();
		resourceProvider.setPatientService(patientService);
		super.setup();
	}
	
	@Test
	public void shouldGetPatientByUuid() throws Exception {
		org.hl7.fhir.r4.model.Patient patient = new org.hl7.fhir.r4.model.Patient();
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
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Hannibal Lector"));
	}
	
	@Test
	public void shouldGetPatientByGivenName() throws Exception {
		verifyUri("/Patient/?given=Hannibal");
		
		verify(patientService).searchForPatients(isNull(), stringAndListCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Hannibal"));
	}
	
	@Test
	public void shouldGetPatientByFamilyName() throws Exception {
		verifyUri("/Patient/?family=Lector");
		
		verify(patientService).searchForPatients(isNull(), isNull(), stringAndListCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Lector"));
	}
	
	@Test
	public void shouldGetPatientByIdentifier() throws Exception {
		verifyUri("/Patient/?identifier=M10000");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), tokenAndListCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(tokenAndListCaptor.getValue(), notNullValue());
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("M10000"));
	}
	
	@Test
	public void shouldGetPatientByGender() throws Exception {
		verifyUri("/Patient/?gender=male");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), tokenAndListCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(tokenAndListCaptor.getValue(), notNullValue());
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("male"));
	}
	
	@Test
	public void shouldGetPatientByBirthDate() throws Exception {
		verifyUri("/Patient/?birthdate=eq1975-02-02");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), dateRangeCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
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
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
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
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
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
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
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
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
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
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
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
		    dateRangeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
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
		    dateRangeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
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
		    dateRangeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
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
		    dateRangeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
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
		    dateRangeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
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
		    dateRangeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
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
		    tokenAndListCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(tokenAndListCaptor.getValue(), notNullValue());
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("true"));
	}
	
	@Test
	public void shouldGetPatientByCity() throws Exception {
		verifyUri("/Patient/?address-city=Washington");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), stringAndListCaptor.capture(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Washington"));
	}
	
	@Test
	public void shouldGetPatientByState() throws Exception {
		verifyUri("/Patient/?address-state=Washington");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), stringAndListCaptor.capture(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Washington"));
	}
	
	@Test
	public void shouldGetPatientByCountry() throws Exception {
		verifyUri("/Patient/?address-country=Washington");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), stringAndListCaptor.capture(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Washington"));
	}
	
	@Test
	public void shouldGetPatientByPostalCode() throws Exception {
		verifyUri("/Patient/?address-postalcode=98136");
		
		verify(patientService).searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), stringAndListCaptor.capture(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("98136"));
	}
	
	@Test
	public void shouldVerifyGetPatientResourceHistoryUri() throws Exception {
		org.hl7.fhir.r4.model.Patient patient = new org.hl7.fhir.r4.model.Patient();
		patient.setId(PATIENT_UUID);
		when(patientService.get(PATIENT_UUID)).thenReturn(patient);
		
		MockHttpServletResponse response = getPatientHistoryRequest();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void shouldGetPatientResourceHistory() throws IOException, ServletException {
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.uniqueUuid()));
		provenance.setRecorded(new Date());
		provenance.setActivity(new CodeableConcept().addCoding(
		    new Coding().setCode("CREATE").setSystem(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION).setDisplay("create")));
		provenance.addAgent(new Provenance.ProvenanceAgentComponent()
		        .setType(new CodeableConcept().addCoding(new Coding().setCode(AUT).setDisplay(AUTHOR)
		                .setSystem(FhirConstants.FHIR_TERMINOLOGY_PROVENANCE_PARTICIPANT_TYPE)))
		        .addRole(new CodeableConcept().addCoding(
		            new Coding().setCode("").setDisplay("").setSystem(FhirConstants.FHIR_TERMINOLOGY_PARTICIPATION_TYPE))));
		org.hl7.fhir.r4.model.Patient patient = new org.hl7.fhir.r4.model.Patient();
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
		org.hl7.fhir.r4.model.Patient patient = new org.hl7.fhir.r4.model.Patient();
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
		    any(), any())).thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(PATIENT_UUID));
	}
}
