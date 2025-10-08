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
import static org.hamcrest.Matchers.containsString;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import javax.servlet.ServletException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.search.param.OpenmrsPatientSearchParams;
import org.openmrs.module.fhir2.api.search.param.PatientSearchParams;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class PatientFhirR3ResourceProviderWebTest extends BaseFhirR3ResourceProviderWebTest<PatientFhirResourceProvider, Patient> {
	
	private static final String PATIENT_UUID = "0b42f99b-776e-4388-8f6f-84357ae2a8fb";
	
	private static final String BAD_PATIENT_UUID = "bb2354c1-e9e4-4020-bda0-d4a9f3232c9c";
	
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
	private ArgumentCaptor<TokenParam> tokenCaptor;
	
	@Captor
	private ArgumentCaptor<PatientSearchParams> patientSearchParamsCaptor;
	
	@Captor
	private ArgumentCaptor<OpenmrsPatientSearchParams> openmrsPatientSearchParamsCaptor;
	
	@Override
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
		assertThat(response.getContentType(), containsString(FhirMediaTypes.JSON.toString()));
		
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
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		StringAndListParam nameParam = patientSearchParamsCaptor.getValue().getName();
		
		assertThat(nameParam, notNullValue());
		assertThat(nameParam.getValuesAsQueryTokens(), not(empty()));
		assertThat(nameParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Hannibal Lector"));
	}
	
	@Test
	public void shouldGetPatientByGivenName() throws Exception {
		verifyUri("/Patient/?given=Hannibal");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		StringAndListParam givenNameParam = patientSearchParamsCaptor.getValue().getGiven();
		
		assertThat(givenNameParam, notNullValue());
		assertThat(givenNameParam.getValuesAsQueryTokens(), not(empty()));
		assertThat(givenNameParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Hannibal"));
	}
	
	@Test
	public void shouldGetPatientByFamilyName() throws Exception {
		verifyUri("/Patient/?family=Lector");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		StringAndListParam familyNameParam = patientSearchParamsCaptor.getValue().getFamily();
		
		assertThat(familyNameParam, notNullValue());
		assertThat(familyNameParam.getValuesAsQueryTokens(), not(empty()));
		assertThat(familyNameParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Lector"));
	}
	
	@Test
	public void shouldGetPatientByIdentifier() throws Exception {
		verifyUri("/Patient/?identifier=M10000");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		TokenAndListParam identifierParam = patientSearchParamsCaptor.getValue().getIdentifier();
		
		assertThat(identifierParam, notNullValue());
		assertThat(identifierParam.getValuesAsQueryTokens(), not(empty()));
		assertThat(identifierParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("M10000"));
	}
	
	@Test
	public void shouldGetPatientByGender() throws Exception {
		verifyUri("/Patient/?gender=male");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		TokenAndListParam genderParam = patientSearchParamsCaptor.getValue().getGender();
		
		assertThat(genderParam, notNullValue());
		assertThat(genderParam.getValuesAsQueryTokens(), not(empty()));
		assertThat(genderParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(), equalTo("male"));
	}
	
	@Test
	public void shouldGetPatientByBirthDate() throws Exception {
		verifyUri("/Patient/?birthdate=eq1975-02-02");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		DateRangeParam birthDateParam = patientSearchParamsCaptor.getValue().getBirthDate();
		
		assertThat(birthDateParam, notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(birthDateParam.getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(birthDateParam.getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPatientByBirthDateGreaterThanOrEqualTo() throws Exception {
		verifyUri("/Patient/?birthdate=ge1975-02-02");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		DateRangeParam birthDateParam = patientSearchParamsCaptor.getValue().getBirthDate();
		
		assertThat(birthDateParam, notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(birthDateParam.getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(birthDateParam.getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetPatientByBirthDateGreaterThan() throws Exception {
		verifyUri("/Patient/?birthdate=gt1975-02-02");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		DateRangeParam birthDateParam = patientSearchParamsCaptor.getValue().getBirthDate();
		
		assertThat(birthDateParam, notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(birthDateParam.getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(birthDateParam.getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetPatientByBirthDateLessThanOrEqualTo() throws Exception {
		verifyUri("/Patient/?birthdate=le1975-02-02");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		DateRangeParam birthDateParam = patientSearchParamsCaptor.getValue().getBirthDate();
		
		assertThat(birthDateParam, notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(birthDateParam.getLowerBound(), nullValue());
		assertThat(birthDateParam.getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPatientByBirthDateLessThan() throws Exception {
		verifyUri("/Patient/?birthdate=lt1975-02-02");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		DateRangeParam birthDateParam = patientSearchParamsCaptor.getValue().getBirthDate();
		
		assertThat(birthDateParam, notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(birthDateParam.getLowerBound(), nullValue());
		assertThat(birthDateParam.getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPatientByBirthDateBetween() throws Exception {
		verifyUri("/Patient/?birthdate=ge1975-02-02&birthdate=le1980-02-02");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		DateRangeParam birthDateParam = patientSearchParamsCaptor.getValue().getBirthDate();
		
		assertThat(birthDateParam, notNullValue());
		
		Calendar lowerBound = Calendar.getInstance();
		lowerBound.set(1975, Calendar.FEBRUARY, 2);
		
		Calendar upperBound = Calendar.getInstance();
		upperBound.set(1980, Calendar.FEBRUARY, 2);
		
		assertThat(birthDateParam.getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(lowerBound.getTime(), Calendar.DATE)));
		assertThat(birthDateParam.getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(upperBound.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPatientByDeathDate() throws Exception {
		verifyUri("/Patient/?death-date=eq1975-02-02");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		DateRangeParam deathDateParam = patientSearchParamsCaptor.getValue().getDeathDate();
		
		assertThat(deathDateParam, notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(deathDateParam.getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(deathDateParam.getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPatientByDeathDateGreaterThanOrEqualTo() throws Exception {
		verifyUri("/Patient/?death-date=ge1975-02-02");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		DateRangeParam deathDateParam = patientSearchParamsCaptor.getValue().getDeathDate();
		
		assertThat(deathDateParam, notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(deathDateParam.getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(deathDateParam.getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetPatientByDeathDateGreaterThan() throws Exception {
		verifyUri("/Patient/?death-date=gt1975-02-02");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		DateRangeParam deathDateParam = patientSearchParamsCaptor.getValue().getDeathDate();
		
		assertThat(deathDateParam, notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(deathDateParam.getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(deathDateParam.getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetPatientByDeathDateLessThanOrEqualTo() throws Exception {
		verifyUri("/Patient/?death-date=le1975-02-02");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		DateRangeParam deathDateParam = patientSearchParamsCaptor.getValue().getDeathDate();
		
		assertThat(deathDateParam, notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(deathDateParam.getLowerBound(), nullValue());
		assertThat(deathDateParam.getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPatientByDeathDateLessThan() throws Exception {
		verifyUri("/Patient/?death-date=lt1975-02-02");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		DateRangeParam deathDateParam = patientSearchParamsCaptor.getValue().getDeathDate();
		
		assertThat(deathDateParam, notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(deathDateParam.getLowerBound(), nullValue());
		assertThat(deathDateParam.getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPatientByDeathDateBetween() throws Exception {
		verifyUri("/Patient/?death-date=ge1975-02-02&death-date=le1980-02-02");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		DateRangeParam deathDateParam = patientSearchParamsCaptor.getValue().getDeathDate();
		
		assertThat(deathDateParam, notNullValue());
		
		Calendar lowerBound = Calendar.getInstance();
		lowerBound.set(1975, Calendar.FEBRUARY, 2);
		
		Calendar upperBound = Calendar.getInstance();
		upperBound.set(1980, Calendar.FEBRUARY, 2);
		
		assertThat(deathDateParam.getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(lowerBound.getTime(), Calendar.DATE)));
		assertThat(deathDateParam.getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(upperBound.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPatientByDeceased() throws Exception {
		verifyUri("/Patient/?deceased=true");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		TokenAndListParam deceasedParam = patientSearchParamsCaptor.getValue().getDeceased();
		
		assertThat(deceasedParam, notNullValue());
		assertThat(deceasedParam.getValuesAsQueryTokens(), not(empty()));
		assertThat(deceasedParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("true"));
	}
	
	@Test
	public void shouldGetPatientByCity() throws Exception {
		verifyUri("/Patient/?address-city=Washington");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		StringAndListParam cityParam = patientSearchParamsCaptor.getValue().getCity();
		
		assertThat(cityParam, notNullValue());
		assertThat(cityParam.getValuesAsQueryTokens(), not(empty()));
		assertThat(cityParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Washington"));
	}
	
	@Test
	public void shouldGetPatientByState() throws Exception {
		verifyUri("/Patient/?address-state=Washington");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		StringAndListParam stateParam = patientSearchParamsCaptor.getValue().getState();
		
		assertThat(stateParam, notNullValue());
		assertThat(stateParam.getValuesAsQueryTokens(), not(empty()));
		assertThat(stateParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Washington"));
	}
	
	@Test
	public void shouldGetPatientByCountry() throws Exception {
		verifyUri("/Patient/?address-country=Washington");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		StringAndListParam countryParam = patientSearchParamsCaptor.getValue().getCountry();
		
		assertThat(countryParam, notNullValue());
		assertThat(countryParam.getValuesAsQueryTokens(), not(empty()));
		assertThat(countryParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Washington"));
	}
	
	@Test
	public void shouldGetPatientByPostalCode() throws Exception {
		verifyUri("/Patient/?address-postalcode=98136");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		StringAndListParam postalCodeParam = patientSearchParamsCaptor.getValue().getPostalCode();
		
		assertThat(postalCodeParam, notNullValue());
		assertThat(postalCodeParam.getValuesAsQueryTokens(), not(empty()));
		assertThat(postalCodeParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("98136"));
	}
	
	@Test
	public void shouldGetPatientByUUID() throws Exception {
		verifyUri(String.format("/Patient?_id=%s", PATIENT_UUID));
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		TokenAndListParam uuidParam = patientSearchParamsCaptor.getValue().getId();
		
		assertThat(uuidParam, notNullValue());
		assertThat(uuidParam.getValuesAsQueryTokens(), not(empty()));
		assertThat(uuidParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PATIENT_UUID));
	}
	
	@Test
	public void shouldGetPatientByLastUpdatedDate() throws Exception {
		verifyUri(String.format("/Patient?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		DateRangeParam lastUpdatedParam = patientSearchParamsCaptor.getValue().getLastUpdated();
		
		assertThat(lastUpdatedParam, notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(lastUpdatedParam.getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(lastUpdatedParam.getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldAddReverseIncludedObservationsToReturnedResults() throws Exception {
		verifyUri("/Patient?_revinclude=Observation:patient");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		Set<Include> revIncludesParam = patientSearchParamsCaptor.getValue().getRevIncludes();
		
		assertThat(revIncludesParam, notNullValue());
		assertThat(revIncludesParam.size(), equalTo(1));
		assertThat(revIncludesParam.iterator().next().getParamName(), equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(revIncludesParam.iterator().next().getParamType(), equalTo(FhirConstants.OBSERVATION));
	}
	
	@Test
	public void shouldAddReverseIncludedAllergiesToReturnedResults() throws Exception {
		verifyUri("/Patient?_revinclude=AllergyIntolerance:patient");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		Set<Include> revIncludesParam = patientSearchParamsCaptor.getValue().getRevIncludes();
		
		assertThat(revIncludesParam, notNullValue());
		assertThat(revIncludesParam.size(), equalTo(1));
		assertThat(revIncludesParam.iterator().next().getParamName(), equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(revIncludesParam.iterator().next().getParamType(), equalTo(FhirConstants.ALLERGY_INTOLERANCE));
	}
	
	@Test
	public void shouldAddReverseIncludedDiagnosticReportsToReturnedResults() throws Exception {
		verifyUri("/Patient?_revinclude=DiagnosticReport:patient");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		Set<Include> revIncludesParam = patientSearchParamsCaptor.getValue().getRevIncludes();
		
		assertThat(revIncludesParam, notNullValue());
		assertThat(revIncludesParam.size(), equalTo(1));
		assertThat(revIncludesParam.iterator().next().getParamName(), equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(revIncludesParam.iterator().next().getParamType(), equalTo(FhirConstants.DIAGNOSTIC_REPORT));
	}
	
	@Test
	public void shouldAddReverseIncludedEncountersToReturnedResults() throws Exception {
		verifyUri("/Patient?_revinclude=Encounter:patient");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		Set<Include> revIncludesParam = patientSearchParamsCaptor.getValue().getRevIncludes();
		
		assertThat(revIncludesParam, notNullValue());
		assertThat(revIncludesParam.size(), equalTo(1));
		assertThat(revIncludesParam.iterator().next().getParamName(), equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(revIncludesParam.iterator().next().getParamType(), equalTo(FhirConstants.ENCOUNTER));
	}
	
	@Test
	public void shouldAddReverseIncludedMedicationRequestsToReturnedResults() throws Exception {
		verifyUri("/Patient?_revinclude=MedicationRequest:patient");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		Set<Include> revIncludesParam = patientSearchParamsCaptor.getValue().getRevIncludes();
		
		assertThat(revIncludesParam, notNullValue());
		assertThat(revIncludesParam.size(), equalTo(1));
		assertThat(revIncludesParam.iterator().next().getParamName(), equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(revIncludesParam.iterator().next().getParamType(), equalTo(FhirConstants.MEDICATION_REQUEST));
	}
	
	@Test
	public void shouldAddReverseIncludedProcedureRequestsToReturnedResults() throws Exception {
		verifyUri("/Patient?_revinclude=ProcedureRequest:patient");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		Set<Include> revIncludesParam = patientSearchParamsCaptor.getValue().getRevIncludes();
		
		assertThat(revIncludesParam, notNullValue());
		assertThat(revIncludesParam.size(), equalTo(1));
		assertThat(revIncludesParam.iterator().next().getParamName(), equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(revIncludesParam.iterator().next().getParamType(), equalTo(FhirConstants.PROCEDURE_REQUEST));
	}
	
	@Test
	public void shouldHandleMultipleReverseIncludes() throws Exception {
		verifyUri("/Patient?_revinclude=Observation:patient&_revinclude=AllergyIntolerance:patient");
		
		verify(patientService).searchForPatients(patientSearchParamsCaptor.capture());
		Set<Include> revIncludesParam = patientSearchParamsCaptor.getValue().getRevIncludes();
		
		assertThat(revIncludesParam, notNullValue());
		assertThat(revIncludesParam.size(), equalTo(2));
		
		assertThat(revIncludesParam, hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_PATIENT_PARAM)),
		    hasProperty("paramType", equalTo(FhirConstants.OBSERVATION)))));
		assertThat(revIncludesParam, hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_PATIENT_PARAM)),
		    hasProperty("paramType", equalTo(FhirConstants.ALLERGY_INTOLERANCE)))));
	}
	
	@Test
	public void shouldSupportCustomOpenmrsQuery() throws Exception {
		verifyUri("/Patient/?_query=openmrsPatients&q=Hannibal Lector");
		
		verify(patientService).searchForPatients(openmrsPatientSearchParamsCaptor.capture());
		StringAndListParam queryParam = openmrsPatientSearchParamsCaptor.getValue().getQuery();
		
		assertThat(queryParam, notNullValue());
		assertThat(queryParam.getValuesAsQueryTokens(), not(empty()));
		assertThat(queryParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("Hannibal Lector"));
	}
	
	private void verifyUri(String uri) throws Exception {
		Patient patient = new Patient();
		patient.setId(PATIENT_UUID);
		when(patientService.searchForPatients(any(PatientSearchParams.class)))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		when(patientService.searchForPatients(any(OpenmrsPatientSearchParams.class)))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), containsString(FhirMediaTypes.JSON.toString()));
		
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
			jsonPatient = inputStreamToString(is, StandardCharsets.UTF_8);
		}
		
		org.hl7.fhir.r4.model.Patient patient = new org.hl7.fhir.r4.model.Patient();
		patient.setId(PATIENT_UUID);
		
		when(patientService.create(any(org.hl7.fhir.r4.model.Patient.class))).thenReturn(patient);
		
		MockHttpServletResponse response = post("/Patient").jsonContent(jsonPatient).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isCreated());
	}
	
	@Test
	public void updatePatient_shouldUpdateExistingPatient() throws Exception {
		String jsonPatient;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_PATIENT_PATH)) {
			Objects.requireNonNull(is);
			jsonPatient = inputStreamToString(is, StandardCharsets.UTF_8);
		}
		
		org.hl7.fhir.r4.model.Patient patient = new org.hl7.fhir.r4.model.Patient();
		patient.setId(PATIENT_UUID);
		
		when(patientService.update(anyString(), any(org.hl7.fhir.r4.model.Patient.class))).thenReturn(patient);
		
		MockHttpServletResponse response = put("/Patient/" + PATIENT_UUID).jsonContent(jsonPatient)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
	}
	
	@Test
	public void updatePatient_shouldErrorForNoId() throws Exception {
		String jsonPatient;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_PATIENT_NO_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonPatient = inputStreamToString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/Patient/" + PATIENT_UUID).jsonContent(jsonPatient)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	public void updatePatient_shouldErrorForIdMissMatch() throws Exception {
		String jsonPatient;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_PATIENT_WRONG_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonPatient = inputStreamToString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/Patient/" + BAD_PATIENT_UUID).jsonContent(jsonPatient)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    containsStringIgnoringCase("body must contain an ID element which matches the request URL"));
	}
	
	@Test
	public void deletePatient_shouldDeletePatient() throws Exception {
		MockHttpServletResponse response = delete("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), containsString(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void deletePatient_shouldReturn404WhenPatientNotFound() throws Exception {
		doThrow(new ResourceNotFoundException("")).when(patientService).delete(BAD_PATIENT_UUID);
		
		MockHttpServletResponse response = delete("/Patient/" + BAD_PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), containsString(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void getPatientEverything_shouldHandlePatientId() throws Exception {
		verifyEverythingOperation("/Patient/" + PATIENT_UUID + "/$everything?");
		
		verify(patientService).getPatientEverything(tokenCaptor.capture());
		
		assertThat(tokenCaptor.getValue(), notNullValue());
		assertThat(tokenCaptor.getValue().getValue(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void getPatientEverything_shouldHandleNoPatientId() throws Exception {
		verifyEverythingTypeOperation("/Patient/$everything?");
		
		verify(patientService).getPatientEverything();
	}
	
	private void verifyEverythingOperation(String uri) throws Exception {
		Patient patient = new Patient();
		patient.setId(PATIENT_UUID);
		
		when(patientService.getPatientEverything(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), containsString(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(PATIENT_UUID));
	}
	
	private void verifyEverythingTypeOperation(String uri) throws Exception {
		Patient patient = new Patient();
		patient.setId(PATIENT_UUID);
		
		when(patientService.getPatientEverything())
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), containsString(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(PATIENT_UUID));
	}
	
}
