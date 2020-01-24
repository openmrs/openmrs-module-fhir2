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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

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
	public void shouldRetrievePatientByUuid() {
		when(dao.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
		when(patientTranslator.toFhirResource(patient)).thenReturn(fhirPatient);
		
		org.hl7.fhir.r4.model.Patient result = patientService.getPatientByUuid(PATIENT_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void shouldSearchForPatientsByName() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		when(dao.findPatientsByName(PATIENT_GIVEN_NAME)).thenReturn(patients);
		Collection<org.hl7.fhir.r4.model.Patient> results = patientService.findPatientsByName(PATIENT_GIVEN_NAME);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(),equalTo(1));
	}
	
	@Test
	public void shouldSearchForPatientsByGivenName() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		when(dao.findPatientsByGivenName(PATIENT_GIVEN_NAME)).thenReturn(patients);
		Collection<org.hl7.fhir.r4.model.Patient> results = patientService.findPatientsByGivenName(PATIENT_GIVEN_NAME);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(),equalTo(1));
	}
	
	@Test
	public void shouldSearchForPatientsByFamilyName() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		when(dao.findPatientsByFamilyName(PATIENT_FAMILY_NAME)).thenReturn(patients);
		Collection<org.hl7.fhir.r4.model.Patient> results = patientService.findPatientsByFamilyName(PATIENT_FAMILY_NAME);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(),equalTo(1));
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsForPartialMatchOnGivenName() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		when(dao.findPatientsByGivenName(PATIENT_PARTIAL_GIVEN_NAME)).thenReturn(patients);
		Collection<org.hl7.fhir.r4.model.Patient> results = patientService.findPatientsByGivenName(PATIENT_PARTIAL_GIVEN_NAME);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsForPartialMatchOnFamilyName() {
		Collection<Patient> patients = new ArrayList<>();
		patients.add(patient);
		when(dao.findPatientsByGivenName(PATIENT_PARTIAL_FAMILY_NAME)).thenReturn(patients);
		Collection<org.hl7.fhir.r4.model.Patient> results = patientService.findPatientsByGivenName(PATIENT_PARTIAL_FAMILY_NAME);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnEmptyCollectionWhenPatientGivenNameNotMatched() {
		Collection<org.hl7.fhir.r4.model.Patient> results = patientService
		        .findPatientsByGivenName(PATIENT_GIVEN_NAME_NOT_MATCHED);
		assertThat(results, notNullValue());
		assertThat(results, is(empty()));
	}
	
	@Test
	public void shouldReturnEmptyCollectionWhenPatientFamilyNameNotMatched() {
		Collection<org.hl7.fhir.r4.model.Patient> results = patientService
		        .findPatientsByFamilyName(PATIENT_FAMILY_NAME_NOT_MATCHED);
		assertThat(results, notNullValue());
		assertThat(results, is(empty()));
	}
}
