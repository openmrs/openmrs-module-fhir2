/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirPatientDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String PATIENT_UUID = "256ccf6d-6b41-455c-9be2-51ff4386ae76";
	
	private static final String PATIENT_IDENTIFIER_TYPE_NAME = "Test Identifier Type";
	
	private static final String PATIENT_IDENTIFIER_TYPE_UUID = "c5576187-9a67-43a7-9b7c-04db22851211";
	
	private static final String WRONG_IDENTIFIER_TYPE_NAME = "Wrong Identifier Type";
	
	private static final String WRONG_IDENTIFIER_TYPE_UUID = "123456-abcdef-123456";
	
	private static final String PATIENT_SEARCH_DATA_XML = "org/openmrs/api/include/PatientServiceTest-findPatients.xml";
	
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
	public void shouldRetrievePatientIdentifierTypeByName() {
		PatientIdentifierType result = dao.getPatientIdentifierTypeByNameOrUuid(PATIENT_IDENTIFIER_TYPE_NAME, null);
		assertThat(result, notNullValue());
		assertThat(result.getName(), equalTo(PATIENT_IDENTIFIER_TYPE_NAME));
		assertThat(result.getUuid(), equalTo(PATIENT_IDENTIFIER_TYPE_UUID));
	}
	
	@Test
	public void shouldRetrievePatientIdentifierTypeByUuid() {
		PatientIdentifierType result = dao.getPatientIdentifierTypeByNameOrUuid(null, PATIENT_IDENTIFIER_TYPE_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getName(), equalTo(PATIENT_IDENTIFIER_TYPE_NAME));
		assertThat(result.getUuid(), equalTo(PATIENT_IDENTIFIER_TYPE_UUID));
	}
	
	@Test
	public void shouldReturnResultIfRightNameWrongUuid() {
		PatientIdentifierType result = dao.getPatientIdentifierTypeByNameOrUuid(PATIENT_IDENTIFIER_TYPE_NAME,
		    WRONG_IDENTIFIER_TYPE_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getName(), equalTo(PATIENT_IDENTIFIER_TYPE_NAME));
		assertThat(result.getUuid(), equalTo(PATIENT_IDENTIFIER_TYPE_UUID));
	}
	
	@Test
	public void shouldReturnResultIfRightUuidWrongName() {
		PatientIdentifierType result = dao.getPatientIdentifierTypeByNameOrUuid(WRONG_IDENTIFIER_TYPE_NAME,
		    PATIENT_IDENTIFIER_TYPE_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getName(), equalTo(PATIENT_IDENTIFIER_TYPE_NAME));
		assertThat(result.getUuid(), equalTo(PATIENT_IDENTIFIER_TYPE_UUID));
	}
	
	@Test
	public void shouldReturnNullIfIdentifierCannotBeFound() {
		PatientIdentifierType result = dao.getPatientIdentifierTypeByNameOrUuid(null, null);
		assertThat(result, nullValue());
	}
	
}
