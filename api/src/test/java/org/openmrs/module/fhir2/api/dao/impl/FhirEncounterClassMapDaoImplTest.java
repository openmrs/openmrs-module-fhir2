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
import static org.hamcrest.Matchers.nullValue;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class FhirEncounterClassMapDaoImplTest extends BaseFhirContextSensitiveTest {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private FhirEncounterClassMapDaoImpl dao;
	
	private static final String FHIR_ENCOUNTER_CLASS_MAP_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirEncounterClassMapDaoImplTest_initial_data.xml";
	
	private static final String LOCATION_UUID_WITH_SINGLE_MAPPING = "9356400c-a5a2-4532-8f2b-2361b3446eb8";
	
	private static final String LOCATION_UUID_WITH_MULTIPLE_MAPPINGS = "167ce20c-4785-4285-9119-d197268f7f4a";
	
	private static final String LOCATION_UUID_WITH_NO_MAPPING = "non-existent-uuid";
	
	private static final String EXPECTED_ENCOUNTER_CLASS = "AMB";
	
	@Before
	public void setup() throws Exception {
		dao = new FhirEncounterClassMapDaoImpl();
		dao.setSessionFactory(sessionFactory);
		executeDataSet(FHIR_ENCOUNTER_CLASS_MAP_INITIAL_DATA_XML);
	}
	
	@Test
	public void getFhirClass_shouldReturnEncounterClassForLocationWithSingleMapping() {
		String result = dao.getFhirClass(LOCATION_UUID_WITH_SINGLE_MAPPING);
		assertThat(result, is(equalTo(EXPECTED_ENCOUNTER_CLASS)));
	}
	
	@Test
	public void getFhirClass_shouldReturnNullForLocationWithNoMapping() {
		String result = dao.getFhirClass(LOCATION_UUID_WITH_NO_MAPPING);
		assertThat(result, is(nullValue()));
	}
	
	@Test
	public void getFhirClass_shouldReturnNullForLocationWithMultipleMappings() {
		String result = dao.getFhirClass(LOCATION_UUID_WITH_MULTIPLE_MAPPINGS);
		assertThat(result, is(nullValue()));
	}
}
