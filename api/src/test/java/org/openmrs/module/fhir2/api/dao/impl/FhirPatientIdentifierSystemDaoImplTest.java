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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirPatientIdentifierSystemDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String PATIENT_IDENTIFIER_TYPE_NAME = "Test Identifier System";
	
	private static final int PATIENT_IDENTIFIER_TYPE_UUID = 1;
	
	private static final String WRONG_PATIENT_IDENTIFIER_TYPE_NAME = "Wrong Identifier System";
	
	private static final int WRONG_PATIENT_IDENTIFIER_TYPE_UUID = 5;
	
	private FhirPatientIdentifierSystemDaoImpl dao;
	
	private static final String PATIENT_IDENTIFIER_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPatientIdentifierSystemDaoImplTest_initial_data.xml";
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirPatientIdentifierSystemDaoImpl();
		dao.setSessionFactory(sessionFactory);
		executeDataSet(PATIENT_IDENTIFIER_DATA_XML);
	}
	
	@Test
	public void shouldReturnUrlByPatientIdentifierType() {
		PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
		patientIdentifierType.setId(PATIENT_IDENTIFIER_TYPE_UUID);
		patientIdentifierType.setName(PATIENT_IDENTIFIER_TYPE_NAME);
		
		String url = dao.getUrlByPatientIdentifierType(patientIdentifierType);
		
		assertThat(url, notNullValue());
	}
	
	@Test
	public void shouldReturnNullIfPatientIdentifierTypeNotFound() {
		PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
		patientIdentifierType.setId(WRONG_PATIENT_IDENTIFIER_TYPE_UUID);
		patientIdentifierType.setName(WRONG_PATIENT_IDENTIFIER_TYPE_NAME);
		
		String url = dao.getUrlByPatientIdentifierType(patientIdentifierType);
		
		assertThat(url, nullValue());
	}
	
}
