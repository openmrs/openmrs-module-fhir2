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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Optional;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.model.FhirPatientIdentifierSystem;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirPatientIdentifierSystemDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private FhirPatientIdentifierSystemDaoImpl dao;
	
	private static final String PATIENT_IDENTIFIER_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPatientIdentifierSystemDaoImplTest_initial_data.xml";
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	PatientService patientService;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirPatientIdentifierSystemDaoImpl();
		dao.setSessionFactory(sessionFactory);
		executeDataSet(PATIENT_IDENTIFIER_DATA_XML);
	}
	
	@Test
	public void shouldReturnUrlByPatientIdentifierType() {
		PatientIdentifierType patientIdentifierType = patientService.getPatientIdentifierType(1);
		String url = dao.getUrlByPatientIdentifierType(patientIdentifierType);
		assertThat(url, notNullValue());
		assertThat(url, equalTo("www.example.com"));
	}
	
	@Test
	public void shouldReturnNullIfPatientIdentifierTypeNotFound() {
		PatientIdentifierType patientIdentifierType = patientService.getPatientIdentifierType(2);
		String url = dao.getUrlByPatientIdentifierType(patientIdentifierType);
		assertThat(url, nullValue());
	}
	
	@Test
	public void shouldGetFhirPatientIdentifierSystemByPatientIdentifierType() {
		PatientIdentifierType identifierType = patientService.getPatientIdentifierType(1);
		Optional<FhirPatientIdentifierSystem> fhirSystem = dao.getFhirPatientIdentifierSystem(identifierType);
		assertThat(fhirSystem.isPresent(), equalTo(true));
		assertThat(fhirSystem.get().getUrl(), equalTo("www.example.com"));
	}
	
	@Test
	public void shouldGetNoPatientIdentifierSystemByPatientIdentifierTypeNotFound() {
		PatientIdentifierType identifierType = patientService.getPatientIdentifierType(2);
		Optional<FhirPatientIdentifierSystem> fhirSystem = dao.getFhirPatientIdentifierSystem(identifierType);
		assertThat(fhirSystem.isPresent(), equalTo(false));
	}
	
	@Test
	public void shouldSavePatientIdentifierSystem() {
		PatientIdentifierType identifierType = patientService.getPatientIdentifierType(2);
		FhirPatientIdentifierSystem newSystem = new FhirPatientIdentifierSystem();
		newSystem.setPatientIdentifierType(identifierType);
		newSystem.setName("New System Name");
		newSystem.setUrl("www.newsystem.com");
		newSystem = dao.saveFhirPatientIdentifierSystem(newSystem);
		assertThat(newSystem.getId(), notNullValue());
		assertThat(newSystem.getUuid(), notNullValue());
		Optional<FhirPatientIdentifierSystem> savedSystem = dao.getFhirPatientIdentifierSystem(identifierType);
		assertThat(savedSystem.isPresent(), equalTo(true));
		assertThat(savedSystem.get().getName(), equalTo("New System Name"));
		assertThat(savedSystem.get().getUrl(), equalTo("www.newsystem.com"));
		assertThat(savedSystem.get(), equalTo(newSystem));
	}
}
