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
import static org.hamcrest.MatcherAssert.assertThat;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirConditionDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String NEW_OBS_CONDITION_UUID = "NEW6880e-2c46-15e4-9038-a6c5e4d22gh8";
	
	private static final String EXISTING_OBS_CONDITION_UUID = "942ec003-a55d-43c4-ac7a-bd6d1ba63382";
	
	private static final String WRONG_OBS_CONDITION_UUID = "430bbb70-6a9c-4e1e-badb-9d1034b1b5e9";
	
	private static final String OBS_CONDITION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirObsConditionDaoImplTest_initial_data.xml";
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private ConceptService conceptService;
	
	@Autowired
	private FhirConditionDao<Obs> dao;
	
	@Qualifier("sessionFactory")
	@Autowired
	private SessionFactory sessionFactory;
	
	@Before
	public void setUp() throws Exception {
		executeDataSet(OBS_CONDITION_INITIAL_DATA_XML);
	}
	
	@Test
	public void get_shouldGetObsConditionByUuid() {
		Obs result = dao.get(EXISTING_OBS_CONDITION_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(EXISTING_OBS_CONDITION_UUID));
	}
	
}
