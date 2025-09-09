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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PatientProgram;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirEpisodeOfCareDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String ENCOUNTER_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirEpisodeOfCareDaoImpl_2_2Test_initial_data.xml";
	
	private static final String EPISODE_OF_CARE_UUID = "9119b9f8-af3d-4ad8-9e2e-2317c3de91c6";
	
	private static final String UNKNOWN_EPISODE_OF_CARE_UUID = "911xx9f8-ax3d-4ad8-9e2e-2317cxde91c6";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	private FhirEpisodeOfCareDaoImpl dao;
	
	@Before
	public void setUp() throws Exception {
		dao = new FhirEpisodeOfCareDaoImpl();
		dao.setSessionFactory(sessionFactory);
		
		executeDataSet(ENCOUNTER_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldReturnMatchingPatientProgram() {
		PatientProgram patientProgram = dao.get(EPISODE_OF_CARE_UUID);
		
		assertThat(patientProgram, notNullValue());
		assertThat(patientProgram.getUuid(), notNullValue());
		assertThat(patientProgram.getUuid(), equalTo(EPISODE_OF_CARE_UUID));
	}
	
	@Test
	public void shouldReturnNullWithUnknownPatientProgram() {
		PatientProgram encounter = dao.get(UNKNOWN_EPISODE_OF_CARE_UUID);
		assertThat(encounter, nullValue());
	}
}
