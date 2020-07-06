/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.HumanName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirPatientServiceImplTest {
	
	private static final String PATIENT_UUID = "3434gh32-34h3j4-34jk34-3422h";
	
	private static final String WRONG_PATIENT_UUID = "Wrong uuid";
	
	private static final String PATIENT_GIVEN_NAME = "Jeannette";
	
	private static final String PATIENT_GIVEN_NAME_NOT_MATCHED = "wafula";
	
	private static final String PATIENT_FAMILY_NAME_NOT_MATCHED = "your fam";
	
	private static final String PATIENT_PARTIAL_GIVEN_NAME = "Jean";
	
	private static final String PATIENT_FAMILY_NAME = "Ricky";
	
	private static final String PATIENT_PARTIAL_FAMILY_NAME = "Claud";
	
	private static final String GENDER = "M";
	
	private static final String WRONG_GENDER = "wrong-gender";
	
	private static final String DATE = "1996-12-12";
	
	private static final String UNKNOWN_DATE = "0001-10-10";
	
	private static final String CITY = "Washington";
	
	private static final String STATE = "Washington";
	
	private static final String POSTAL_CODE = "98136";
	
	private static final String COUNTRY = "Washington";
	
	private static final String UNKNOWN_ADDRESS = "unknown address";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final String WRONG_LAST_UPDATED_DATE = "2020-09-09";
	
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	
	@Mock
	private PatientTranslator patientTranslator;
	
	@Mock
	private FhirPatientDao dao;
	
	@Mock
	private SearchQuery<Patient, org.hl7.fhir.r4.model.Patient, FhirPatientDao, PatientTranslator> searchQuery;
	
	private FhirPatientServiceImpl patientService;
	
	private org.hl7.fhir.r4.model.Patient fhirPatient;
	
	private Patient patient;
	
	@Before
	public void setUp() {
		patientService = new FhirPatientServiceImpl();
		patientService.setDao(dao);
		patientService.setTranslator(patientTranslator);
		patientService.setSearchQuery(searchQuery);
		
		PersonName name = new PersonName();
		name.setFamilyName(PATIENT_FAMILY_NAME);
		name.setGivenName(PATIENT_GIVEN_NAME);
		
		patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		patient.setGender("M");
		patient.addName(name);
		
		PersonAddress address = new PersonAddress();
		address.setCityVillage(CITY);
		address.setStateProvince(STATE);
		address.setPostalCode(POSTAL_CODE);
		address.setCountry(COUNTRY);
		
		HumanName humanName = new HumanName();
		humanName.addGiven(PATIENT_GIVEN_NAME);
		humanName.setFamily(PATIENT_FAMILY_NAME);
		
		fhirPatient = new org.hl7.fhir.r4.model.Patient();
		fhirPatient.setId(PATIENT_UUID);
		fhirPatient.addName(humanName);
	}
	
	@Test
	public void getPatientByUuid_shouldRetrievePatientByUuid() {
		when(dao.get(PATIENT_UUID)).thenReturn(patient);
		when(patientTranslator.toFhirResource(patient)).thenReturn(fhirPatient);
		
		org.hl7.fhir.r4.model.Patient result = patientService.get(PATIENT_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByName() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		StringAndListParam stringAndListParam = new StringAndListParam().addAnd(new StringParam(PATIENT_GIVEN_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    stringAndListParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(PATIENT_UUID));
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(patients);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		when(patientTranslator.toFhirResource(patient)).thenReturn(fhirPatient);
		
		IBundleProvider results = patientService.searchForPatients(stringAndListParam, null, null, null, null, null, null,
		    null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		assertThat(get(results), hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByGivenName() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		StringAndListParam stringAndListParam = new StringAndListParam().addAnd(new StringParam(PATIENT_GIVEN_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    stringAndListParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(PATIENT_UUID));
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(patients);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		when(patientTranslator.toFhirResource(patient)).thenReturn(fhirPatient);
		
		IBundleProvider results = patientService.searchForPatients(null, stringAndListParam, null, null, null, null, null,
		    null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByFamilyName() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		StringAndListParam stringAndListParam = new StringAndListParam().addAnd(new StringParam(PATIENT_FAMILY_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    stringAndListParam);
		
		when(dao.getPreferredPageSize()).thenReturn(10);
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(PATIENT_UUID));
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(patients);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		when(patientTranslator.toFhirResource(patient)).thenReturn(fhirPatient);
		
		IBundleProvider results = patientService.searchForPatients(null, null, stringAndListParam, null, null, null, null,
		    null, null, null, null, null, null, null, null);
		
		assertThat(results.getUuid(), notNullValue());
		assertThat(results.size(), equalTo(1));
		assertThat(results.preferredPageSize(), equalTo(10));
		assertThat(get(results), not(empty()));
	}
	
	@Test
	public void searchForPatients_shouldReturnCollectionOfPatientsForPartialMatchOnGivenName() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		StringAndListParam stringAndListParam = new StringAndListParam().addAnd(new StringParam(PATIENT_PARTIAL_GIVEN_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    stringAndListParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(PATIENT_UUID));
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(patients);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		when(patientTranslator.toFhirResource(patient)).thenReturn(fhirPatient);
		
		IBundleProvider results = patientService.searchForPatients(null, stringAndListParam, null, null, null, null, null,
		    null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnCollectionOfPatientsForPartialMatchOnFamilyName() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringParam(PATIENT_PARTIAL_FAMILY_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    stringAndListParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(PATIENT_UUID));
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(patients);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		when(patientTranslator.toFhirResource(patient)).thenReturn(fhirPatient);
		
		IBundleProvider results = patientService.searchForPatients(null, null, stringAndListParam, null, null, null, null,
		    null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientNameNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringParam(PATIENT_GIVEN_NAME_NOT_MATCHED));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    stringAndListParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.emptyList());
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		
		IBundleProvider results = patientService.searchForPatients(stringAndListParam, null, null, null, null, null, null,
		    null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), is(empty()));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientGivenNameNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringParam(PATIENT_GIVEN_NAME_NOT_MATCHED));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    stringAndListParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.emptyList());
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		
		IBundleProvider results = patientService.searchForPatients(null, stringAndListParam, null, null, null, null, null,
		    null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), is(empty()));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientFamilyNameNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringParam(PATIENT_FAMILY_NAME_NOT_MATCHED));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    stringAndListParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.emptyList());
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		
		IBundleProvider results = patientService.searchForPatients(null, null, stringAndListParam, null, null, null, null,
		    null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), is(empty()));
	}
	
	@Test
	public void searchForPatients_shouldReturnCollectionOfPatientWhenPatientGenderMatched() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		TokenAndListParam tokenAndListParam = new TokenAndListParam().addAnd(new TokenParam(GENDER));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    tokenAndListParam);
		
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(patients);
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(PATIENT_UUID));
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		when(patientTranslator.toFhirResource(patient)).thenReturn(fhirPatient);
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, tokenAndListParam, null, null,
		    null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientGenderNotMatched() {
		TokenAndListParam tokenAndListParam = new TokenAndListParam().addAnd(new TokenParam(WRONG_GENDER));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    tokenAndListParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.emptyList());
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, tokenAndListParam, null, null,
		    null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), is(empty()));
	}
	
	@Test
	public void searchForPatients_shouldReturnCollectionOfPatientWhenPatientBirthDateMatched() throws ParseException {
		Date birthDate = dateFormatter.parse(DATE);
		patient.setBirthdate(birthDate);
		
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(DATE).setUpperBound(DATE);
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    dateRangeParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(PATIENT_UUID));
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(patients);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		when(patientTranslator.toFhirResource(patient)).thenReturn(fhirPatient);
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, null, dateRangeParam, null, null,
		    null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientBirthDateNotMatched() {
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(UNKNOWN_DATE).setUpperBound(UNKNOWN_DATE);
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    dateRangeParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.emptyList());
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, null, dateRangeParam, null, null,
		    null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), empty());
	}
	
	@Test
	public void searchForPatients_shouldReturnCollectionOfPatientWhenPatientDeathDateMatched() throws ParseException {
		Date deathDate = dateFormatter.parse(DATE);
		patient.setDeathDate(deathDate);
		
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(DATE).setUpperBound(DATE);
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    dateRangeParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(PATIENT_UUID));
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(patients);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		when(patientTranslator.toFhirResource(patient)).thenReturn(fhirPatient);
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, null, null, dateRangeParam, null,
		    null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientDeathDateNotMatched() {
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(UNKNOWN_DATE).setUpperBound(UNKNOWN_DATE);
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    dateRangeParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.emptyList());
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, null, null, dateRangeParam, null,
		    null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), empty());
	}
	
	@Test
	public void searchForPatients_shouldReturnCollectionOfPatientWhenPatientCityMatched() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		StringAndListParam stringAndListParam = new StringAndListParam().addAnd(new StringParam(CITY));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, "city",
		    stringAndListParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(PATIENT_UUID));
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(patients);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		when(patientTranslator.toFhirResource(patient)).thenReturn(fhirPatient);
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, null, null, null, null,
		    stringAndListParam, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientCityNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam().addAnd(new StringParam(UNKNOWN_ADDRESS));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, "city",
		    stringAndListParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.emptyList());
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, null, null, null, null,
		    stringAndListParam, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), empty());
	}
	
	@Test
	public void searchForPatients_shouldReturnCollectionOfPatientWhenPatientStateMatched() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		StringAndListParam stringAndListParam = new StringAndListParam().addAnd(new StringParam(STATE));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, "state",
		    stringAndListParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(PATIENT_UUID));
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(patients);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		when(patientTranslator.toFhirResource(patient)).thenReturn(fhirPatient);
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, null, null, null, null, null,
		    stringAndListParam, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientStateNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam().addAnd(new StringParam(UNKNOWN_ADDRESS));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, "state",
		    stringAndListParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.emptyList());
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, null, null, null, null, null,
		    stringAndListParam, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), empty());
	}
	
	@Test
	public void searchForPatients_shouldReturnCollectionOfPatientWhenPatientPostalCodeMatched() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		StringAndListParam stringAndListParam = new StringAndListParam().addAnd(new StringParam(POSTAL_CODE));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, "postalCode",
		    stringAndListParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(PATIENT_UUID));
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(patients);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		when(patientTranslator.toFhirResource(patient)).thenReturn(fhirPatient);
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, null, null, null, null, null,
		    null, stringAndListParam, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientPostalCodeNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam().addAnd(new StringParam(UNKNOWN_ADDRESS));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, "postalCode",
		    stringAndListParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.emptyList());
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, null, null, null, null, null,
		    null, stringAndListParam, null, null, null, null);
		assertThat(results, notNullValue());
		assertThat(get(results), empty());
	}
	
	@Test
	public void searchForPatients_shouldReturnCollectionOfPatientWhenPatientCountryMatched() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		StringAndListParam stringAndListParam = new StringAndListParam().addAnd(new StringParam(COUNTRY));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, "country",
		    stringAndListParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(PATIENT_UUID));
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(patients);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		when(patientTranslator.toFhirResource(patient)).thenReturn(fhirPatient);
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, null, null, null, null, null,
		    null, null, stringAndListParam, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientCountryNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam().addAnd(new StringParam(UNKNOWN_ADDRESS));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    stringAndListParam);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.emptyList());
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, null, null, null, null, null,
		    null, null, stringAndListParam, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), empty());
	}
	
	@Test
	public void searchForPatients_shouldReturnCollectionOfPatientWhenUUIDMatched() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PATIENT_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(PATIENT_UUID));
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(patient));
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		when(patientTranslator.toFhirResource(patient)).thenReturn(fhirPatient);
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, null, null, null, null, null,
		    null, null, null, uuid, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenUUIDNotMatched() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(WRONG_PATIENT_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.emptyList());
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, null, null, null, null, null,
		    null, null, null, uuid, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), empty());
	}
	
	@Test
	public void searchForPatients_shouldReturnCollectionOfPatientWhenLastUpdatedMatched() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(PATIENT_UUID));
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(patient));
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		when(patientTranslator.toFhirResource(patient)).thenReturn(fhirPatient);
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, null, null, null, null, null,
		    null, null, null, null, lastUpdated, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenLastUpdatedNotMatched() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(WRONG_LAST_UPDATED_DATE)
		        .setLowerBound(WRONG_LAST_UPDATED_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		when(dao.getResultUuids(any())).thenReturn(Collections.emptyList());
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, patientTranslator));
		
		IBundleProvider results = patientService.searchForPatients(null, null, null, null, null, null, null, null, null,
		    null, null, null, null, lastUpdated, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), empty());
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(0, 10);
	}
}
