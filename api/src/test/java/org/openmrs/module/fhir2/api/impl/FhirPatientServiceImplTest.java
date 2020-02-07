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
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.util.ArrayList;
import java.util.Collection;

import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import org.hl7.fhir.r4.model.HumanName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirPatientServiceImplTest {
	
	private static final String PATIENT_UUID = "3434gh32-34h3j4-34jk34-3422h";
	
	private static final String PATIENT_GIVEN_NAME = "Jeannette";
	
	private static final String PATIENT_GIVEN_NAME_NOT_MATCHED = "wafula";
	
	private static final String PATIENT_FAMILY_NAME_NOT_MATCHED = "your fam";
	
	private static final String PATIENT_PARTIAL_GIVEN_NAME = "Jean";
	
	private static final String PATIENT_FAMILY_NAME = "Ricky";
	
	private static final String PATIENT_PARTIAL_FAMILY_NAME = "Claud";
	
	@Mock
	private PatientTranslator patientTranslator;
	
	@Mock
	private FhirPatientDao dao;
	
	private FhirPatientServiceImpl patientService;
	
	private org.hl7.fhir.r4.model.Patient fhirPatient;
	
	private Patient patient;
	
	@Before
	public void setUp() {
		patientService = new FhirPatientServiceImpl();
		patientService.setDao(dao);
		patientService.setTranslator(patientTranslator);
		
		PersonName name = new PersonName();
		name.setFamilyName(PATIENT_FAMILY_NAME);
		name.setGivenName(PATIENT_GIVEN_NAME);
		
		patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		patient.addName(name);
		
		HumanName humanName = new HumanName();
		humanName.addGiven(PATIENT_GIVEN_NAME);
		humanName.setFamily(PATIENT_FAMILY_NAME);
		
		fhirPatient = new org.hl7.fhir.r4.model.Patient();
		fhirPatient.setId(PATIENT_UUID);
		fhirPatient.addName(humanName);
	}
	
	@Test
	public void getPatientByUuid_shouldRetrievePatientByUuid() {
		when(dao.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
		when(patientTranslator.toFhirResource(patient)).thenReturn(fhirPatient);
		
		org.hl7.fhir.r4.model.Patient result = patientService.getPatientByUuid(PATIENT_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByName() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(PATIENT_GIVEN_NAME));
		when(dao.searchForPatients(argThat(equalTo(stringOrListParam)), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull())).thenReturn(patients);
		
		Collection<org.hl7.fhir.r4.model.Patient> results = patientService.searchForPatients(stringOrListParam, null, null,
		    null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), equalTo(1));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByGivenName() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(PATIENT_GIVEN_NAME));
		when(dao.searchForPatients(isNull(), argThat(equalTo(stringOrListParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull())).thenReturn(patients);
		
		Collection<org.hl7.fhir.r4.model.Patient> results = patientService.searchForPatients(null, stringOrListParam, null,
		    null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), equalTo(1));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByFamilyName() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(PATIENT_FAMILY_NAME));
		when(dao.searchForPatients(isNull(), isNull(), argThat(equalTo(stringOrListParam)), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull())).thenReturn(patients);
		
		Collection<org.hl7.fhir.r4.model.Patient> results = patientService.searchForPatients(null, null, stringOrListParam,
		    null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), equalTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnCollectionOfPatientsForPartialMatchOnGivenName() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(PATIENT_PARTIAL_GIVEN_NAME));
		when(dao.searchForPatients(isNull(), argThat(equalTo(stringOrListParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull())).thenReturn(patients);
		
		Collection<org.hl7.fhir.r4.model.Patient> results = patientService.searchForPatients(null, stringOrListParam, null,
		    null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnCollectionOfPatientsForPartialMatchOnFamilyName() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(PATIENT_PARTIAL_FAMILY_NAME));
		when(dao.searchForPatients(isNull(), isNull(), argThat(equalTo(stringOrListParam)), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull())).thenReturn(patients);
		
		Collection<org.hl7.fhir.r4.model.Patient> results = patientService.searchForPatients(null, null, stringOrListParam,
		    null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientNameNotMatched() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(PATIENT_GIVEN_NAME_NOT_MATCHED));
		
		Collection<org.hl7.fhir.r4.model.Patient> results = patientService.searchForPatients(stringOrListParam, null, null,
		    null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, is(empty()));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientGivenNameNotMatched() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(PATIENT_GIVEN_NAME_NOT_MATCHED));
		
		Collection<org.hl7.fhir.r4.model.Patient> results = patientService.searchForPatients(null, stringOrListParam, null,
		    null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, is(empty()));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientFamilyNameNotMatched() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(PATIENT_FAMILY_NAME_NOT_MATCHED));
		
		Collection<org.hl7.fhir.r4.model.Patient> results = patientService.searchForPatients(null, null, stringOrListParam,
		    null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, is(empty()));
	}
}
