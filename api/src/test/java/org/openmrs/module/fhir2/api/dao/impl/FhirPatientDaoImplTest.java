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

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirPatientDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String PATIENT_UUID = "256ccf6d-6b41-455c-9be2-51ff4386ae76";
	
	private static final String BAD_PATIENT_UUID = "282390a6-3608-496d-9025-aecbc1235670";
	
	private static final String[] PATIENT_SEARCH_DATA_FILES = {
	        "org/openmrs/module/fhir2/api/dao/impl/FhirPatientDaoImplTest_initial_data.xml",
	        "org/openmrs/module/fhir2/api/dao/impl/FhirPatientDaoImplTest_address_data.xml" };
	
	private FhirPatientDaoImpl dao;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirPatientDaoImpl();
		dao.setSessionFactory(sessionFactory);
		for (String search_data : PATIENT_SEARCH_DATA_FILES) {
			executeDataSet(search_data);
		}
	}
	
	@Test
	public void getPatientById_shouldRetrievePatientById() {
		Patient result = dao.getPatientById(4);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(PATIENT_UUID));
		assertThat(result.getId(), equalTo(4));
	}
	
	@Test
	public void getPatientById_shouldReturnNullIfPatientNotFound() {
		assertThat(dao.getPatientById(0), nullValue());
	}
	
	@Test
	public void getPatientByUuid_shouldRetrievePatientByUuid() {
		Patient result = dao.get(PATIENT_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void getPatientByUuid_shouldReturnNullIfPatientNotFound() {
		Patient result = dao.get(BAD_PATIENT_UUID);
		
		assertThat(result, nullValue());
	}
}
