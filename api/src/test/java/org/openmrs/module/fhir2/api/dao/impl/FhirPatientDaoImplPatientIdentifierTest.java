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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.inject.Inject;
import javax.inject.Provider;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirPatientDaoImplPatientIdentifierTest extends BaseModuleContextSensitiveTest {
	
	private static final String PATIENT_IDENTIFIER_TYPE_NAME = "Test Identifier Type";
	
	private static final String PATIENT_IDENTIFIER_TYPE_UUID = "c5576187-9a67-43a7-9b7c-04db22851211";
	
	private static final String WRONG_IDENTIFIER_TYPE_NAME = "Wrong Identifier Type";
	
	private static final String WRONG_IDENTIFIER_TYPE_UUID = "123456-abcdef-123456";
	
	private static final String PATIENT_IDENTIFIER_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPatientDaoImplPatientIdentifierTest_initial_data.xml";
	
	private FhirPatientDaoImpl dao;
	
	@Inject
	private Provider<SessionFactory> sessionFactoryProvider;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirPatientDaoImpl();
		dao.setSessionFactory(sessionFactoryProvider.get());
		executeDataSet(PATIENT_IDENTIFIER_DATA_XML);
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
	public void shouldReturnPatientIdentifierResultIfRightNameWrongUuid() {
		PatientIdentifierType result = dao.getPatientIdentifierTypeByNameOrUuid(PATIENT_IDENTIFIER_TYPE_NAME,
		    WRONG_IDENTIFIER_TYPE_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getName(), equalTo(PATIENT_IDENTIFIER_TYPE_NAME));
		assertThat(result.getUuid(), equalTo(PATIENT_IDENTIFIER_TYPE_UUID));
	}
	
	@Test
	public void shouldReturnPatientIdentifierResultIfRightUuidWrongName() {
		PatientIdentifierType result = dao.getPatientIdentifierTypeByNameOrUuid(WRONG_IDENTIFIER_TYPE_NAME,
		    PATIENT_IDENTIFIER_TYPE_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getName(), equalTo(PATIENT_IDENTIFIER_TYPE_NAME));
		assertThat(result.getUuid(), equalTo(PATIENT_IDENTIFIER_TYPE_UUID));
	}
	
	@Test
	public void shouldNotReturnRetiredPatientIdentifierByName() {
		PatientIdentifierType result = dao.getPatientIdentifierTypeByNameOrUuid("Test Retired Identifier Type", null);
		assertThat(result, nullValue());
	}
	
	@Test
	public void shouldReturnRetiredPatientIdentifierByUuid() {
		PatientIdentifierType result = dao
		        .getPatientIdentifierTypeByNameOrUuid(null, "d62a8faa-c405-48f7-a2a7-f3d0f72b6d30");
		assertThat(result, notNullValue());
	}
	
	@Test
	public void shouldReturnFirstResultIfMultipleIdentifierTypesShareName() {
		PatientIdentifierType result = dao.getPatientIdentifierTypeByNameOrUuid("Test Identifier Type 2", null);
		assertThat(result.getUuid(), equalTo("06b022e8-df1e-4ce7-b89b-59e7a181d319"));
	}
	
	@Test
	public void shouldReturnNullIfIdentifierCannotBeFound() {
		PatientIdentifierType result = dao.getPatientIdentifierTypeByNameOrUuid(null, null);
		assertThat(result, nullValue());
	}
}
