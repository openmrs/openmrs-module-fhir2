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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.model.FhirFlag;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirFlagDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String FHIR_FLAG_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirFlagDaoImplTest_initial_data.xml";
	
	private static final String FLAG_UUID = "d899333c-5bd4-45cc-b1e7-2f9542dbcbf6";
	
	private static final String NEW_FLAG_UUID = "h899343c-5bd4-45cc-b1e7-2f9542db5bf0";
	
	private static final String BAD_FLAG_UUID = "c699333c-5bd4-45cc-b1e7-2f9542d2cb9y";
	
	private FhirFlagDaoImpl dao;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirFlagDaoImpl();
		dao.setSessionFactory(sessionFactory);
		executeDataSet(FHIR_FLAG_INITIAL_DATA_XML);
		
	}
	
	@Test
	public void get_shouldRetrieveFlagByUuid() {
		FhirFlag result = dao.get(FLAG_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(FLAG_UUID));
	}
	
	@Test
	public void createOrUpdate_shouldCreateNewFlag() {
		Patient patient = new Patient();
		patient.setPatientId(7);
		FhirFlag flag = new FhirFlag();
		flag.setUuid(NEW_FLAG_UUID);
		flag.setFlag("Blood pressure is critically high");
		flag.setName("Test Flag");
		flag.setPatient(patient);
		flag.setPriority(FhirFlag.FlagPriority.HIGH);
		flag.setStatus(FhirFlag.FlagStatus.ACTIVE);
		
		FhirFlag result = dao.createOrUpdate(flag);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), is(NEW_FLAG_UUID));
		assertThat(result.getStatus(), is(FhirFlag.FlagStatus.ACTIVE));
	}
	
	@Test
	public void createOrUpdate_shouldUpdateExistingFlag() {
		FhirFlag flag = dao.get(FLAG_UUID);
		assertThat(flag, notNullValue());
		assertThat(flag.getStatus(), is(FhirFlag.FlagStatus.ACTIVE));
		
		// Let's update existing flag
		flag.setStatus(FhirFlag.FlagStatus.INACTIVE);
		flag.setPriority(FhirFlag.FlagPriority.LOW);
		FhirFlag updatedFlag = dao.createOrUpdate(flag);
		
		assertThat(updatedFlag, notNullValue());
		assertThat(updatedFlag.getUuid(), is(FLAG_UUID));
		assertThat(updatedFlag.getStatus(), is(FhirFlag.FlagStatus.INACTIVE));
		assertThat(updatedFlag.getPriority(), is(FhirFlag.FlagPriority.LOW));
	}
	
	@Test
	public void delete_shouldDeleteFlagWithPassInUuid() {
		// deleting
		dao.delete(FLAG_UUID);
		FhirFlag flag = dao.get(FLAG_UUID);
		
		assertThat(flag, notNullValue());
		assertThat(flag.getRetired(), is(true));
		assertThat(flag.getRetireReason(), is("Retired via FHIR API"));
	}
	
	@Test
	public void delete_shouldReturnNullIfFlagToDeleteDoesNotExist() {
		FhirFlag flag = dao.delete(BAD_FLAG_UUID);
		assertThat(flag, nullValue());
	}
	
}
