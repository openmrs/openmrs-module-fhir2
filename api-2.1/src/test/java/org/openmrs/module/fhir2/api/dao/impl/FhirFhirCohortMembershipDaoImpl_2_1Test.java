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
import org.openmrs.CohortMembership;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirFhirCohortMembershipDaoImpl_2_1Test extends BaseModuleContextSensitiveTest {
	
	private static final String COHORT_MEMBER_UUID = "745ff1a2-c2ef-49fd-836f-8a1d936d9ef9";
	
	private static final String BAD_UUID = "111ff1a2-c2ef-49fd-836f-8a1d936d9ef0";
	
	private static final String COHORT_MEMBER_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirCohortMemberDaoImplTest_initial_data.xml";
	
	private FhirFhirCohortMembershipDaoImpl dao;
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirFhirCohortMembershipDaoImpl();
		dao.setSessionFactory(sessionFactory);
		executeDataSet(COHORT_MEMBER_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldGetCohortMembershipByUUID() {
		CohortMembership cohortMembership = dao.get(COHORT_MEMBER_UUID);
		assertThat(cohortMembership, notNullValue());
		assertThat(cohortMembership.getUuid(), equalTo(COHORT_MEMBER_UUID));
		assertThat(cohortMembership.getCohort().getCohortId(), equalTo(1));
	}
	
	@Test
	public void shouldReturnNullCohortMembershipWhenGetByBadUuid() {
		CohortMembership cohortMembership = dao.get(BAD_UUID);
		assertThat(cohortMembership, nullValue());
	}
}
