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

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirGroupDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String COHORT_UUID = "985ff1a2-c2ef-49fd-836f-8a1d936d9ef9";
	
	private static final String BAD_COHORT_UUID = "005ff1a0-c2ef-49fd-836f-8a1d936d9ef7";
	
	private static final String COHORT_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirCohortDaoImplTest_initial_data.xml";
	
	private static final String COHORT_NAME = "John's patientList";
	
	private FhirGroupDaoImpl dao;
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirGroupDaoImpl();
		dao.setSessionFactory(sessionFactory);
		executeDataSet(COHORT_INITIAL_DATA_XML);
	}
	
	@Test
	public void getCohortByUuid_shouldReturnMatchingCohort() {
		Cohort cohort = dao.get(COHORT_UUID);
		assertThat(cohort, notNullValue());
		assertThat(cohort.getUuid(), equalTo(COHORT_UUID));
		assertThat(cohort.getName(), equalTo(COHORT_NAME));
	}
	
	@Test
	public void getCohortByWithWrongUuid_shouldReturnNullCohort() {
		Cohort cohort = dao.get(BAD_COHORT_UUID);
		assertThat(cohort, nullValue());
	}
	
}
