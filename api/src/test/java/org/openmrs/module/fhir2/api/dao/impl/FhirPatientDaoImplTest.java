/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirPatientDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String PATIENT_UUID = "256ccf6d-6b41-455c-9be2-51ff4386ae76";
	
	private static final String PATIENT_SEARCH_DATA_XML = "org/openmrs/api/include/PatientServiceTest-findPatients.xml";
	
	private static final String PATIENT_GIVEN_NAME = "Jeannette";
	
	private static final String PATIENT_PARTIAL_GIVEN_NAME = "Jean";
	
	private static final String PATIENT_FAMILY_NAME = "Claudent";
	
	private static final String PATIENT_PARTIAL_FAMILY_NAME = "Claud";
	
	private static final String PATIENT_NOT_FOUND_NAME = "Igor";
	
	private static final String PERSON_ATTRIBUTE_TYPE_UUID = "14d4f066-15f5-102d-96e4-000c29c2a5d7";
	
	private FhirPatientDaoImpl dao;
	
	@Inject
	@Named("patientService")
	private Provider<PatientService> patientServiceProvider;
	
	@Inject
	private Provider<SessionFactory> sessionFactoryProvider;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirPatientDaoImpl();
		dao.setPatientService(patientServiceProvider.get());
		dao.setSessionFactory(sessionFactoryProvider.get());
		executeDataSet(PATIENT_SEARCH_DATA_XML);
	}
	
	@Test
	public void shouldRetrievePatientByUuid() {
		Patient result = dao.getPatientByUuid(PATIENT_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void shouldSearchForPatientsByName() {
		List<Patient> results = dao.findPatientsByName(PATIENT_GIVEN_NAME);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.get(0).getGivenName(), equalTo(PATIENT_GIVEN_NAME));
	}
	
	@Test
	public void shouldReturnMultiplePatientsForPartialMatch() {
		List<Patient> results = dao.findPatientsByName(PATIENT_PARTIAL_GIVEN_NAME);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThan(1));
	}
	
	@Test
	public void shouldReturnEmptyListWhenPatientNameNotMatched() {
		List<Patient> results = dao.findPatientsByName(PATIENT_NOT_FOUND_NAME);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void shouldSearchForPatientsByGivenName() {
		List<Patient> results = dao.findPatientsByGivenName(PATIENT_GIVEN_NAME);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.get(0).getGivenName(), equalTo(PATIENT_GIVEN_NAME));
	}
	
	@Test
	public void shouldReturnMultiplePatientsForPartialMatchOnGivenName() {
		List<Patient> results = dao.findPatientsByGivenName(PATIENT_PARTIAL_GIVEN_NAME);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThan(1));
	}
	
	@Test
	public void shouldReturnEmptyListWhenPatientGivenNameNotMatched() {
		List<Patient> results = dao.findPatientsByGivenName(PATIENT_NOT_FOUND_NAME);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void shouldSearchForPatientsByFamilyName() {
		List<Patient> results = dao.findPatientsByFamilyName(PATIENT_FAMILY_NAME);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.get(0).getFamilyName(), equalTo(PATIENT_FAMILY_NAME));
	}
	
	@Test
	public void shouldReturnMultiplePatientsForPartialMatchOnFamilyName() {
		List<Patient> results = dao.findPatientsByFamilyName(PATIENT_PARTIAL_FAMILY_NAME);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThan(1));
	}
	
	@Test
	public void shouldReturnEmptyListWhenPatientFamilyNameNotMatched() {
		List<Patient> results = dao.findPatientsByFamilyName(PATIENT_NOT_FOUND_NAME);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void getActiveAttributesByPersonAndAttributeTypeUuid_shouldReturnPersonAttribute() {
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		
		List<PersonAttribute> attributeList = dao.getActiveAttributesByPatientAndAttributeTypeUuid(patient,
		    PERSON_ATTRIBUTE_TYPE_UUID);
		
		assertThat(attributeList, notNullValue());
	}
}
