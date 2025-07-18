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

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirDiagnosisDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String DIAGNOSIS_UUID = "11111111-2222-3333-4444-555555555555";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private EncounterService encounterService;
	
	private FhirDiagnosisDaoImpl dao;
	
	@Before
	public void setUp() {
		dao = new FhirDiagnosisDaoImpl();
		dao.setSessionFactory(sessionFactory);
	}
	
	@Test
	public void createOrUpdate_shouldSaveDiagnosis() {
		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setUuid(DIAGNOSIS_UUID);
		Encounter encounter = encounterService.getEncounter(3);
		diagnosis.setEncounter(encounter);
		diagnosis.setPatient(encounter.getPatient());
		diagnosis.setRank(1);
		
		dao.createOrUpdate(diagnosis);
		
		Diagnosis result = dao.get(DIAGNOSIS_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(DIAGNOSIS_UUID));
	}
}
