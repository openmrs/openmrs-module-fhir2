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
import static org.junit.Assert.assertThat;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirEncounterDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String ENCOUNTER_UUID = "430bbb70-6a9c-4e1e-badb-9d1034b1b5e9";
	
	private static final String UNKNOWN_ENCOUNTER_UUID = "xx923xx-3423kk-2323-232jk23";
	
	private static final String ENCOUNTER_INITIAL_DATA_XML = "org/openmrs/api/include/EncounterServiceTest-initialData.xml";
	
	@Inject
	@Named("sessionFactory")
	private Provider<SessionFactory> sessionFactoryProvider;
	
	private FhirEncounterDaoImpl dao;
	
	@Before
	public void setUp() throws Exception {
		dao = new FhirEncounterDaoImpl();
		dao.setSessionFactory(sessionFactoryProvider.get());
		executeDataSet(ENCOUNTER_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldReturnMatchingEncounter() {
		Encounter encounter = dao.getEncounterByUuid(ENCOUNTER_UUID);
		assertThat(encounter, notNullValue());
		assertThat(encounter.getUuid(), notNullValue());
		assertThat(encounter.getUuid(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void shouldReturnNullWithUnknownEncounterUuid() {
		Encounter encounter = dao.getEncounterByUuid(UNKNOWN_ENCOUNTER_UUID);
		assertThat(encounter, nullValue());
	}
	
}
