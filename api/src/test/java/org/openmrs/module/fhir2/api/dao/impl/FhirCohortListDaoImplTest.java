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
public class FhirCohortListDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String LIST_UUID = "huuffg0i6-15e6-467c-9d4b-mbi7teu9lf0f";
	
	private static final String UNKNOWN_UUID = "c0b1f314-1691-11df-97a5-7038c432aab99";
	
	private static final String LIST_COHORT_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirCohortListDaoImplTest_initial_data.xml";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	private FhirCohortListDaoImpl fhirCohortListDao;
	
	@Before
	public void setup() throws Exception {
		fhirCohortListDao = new FhirCohortListDaoImpl();
		fhirCohortListDao.setSessionFactory(sessionFactory);
		executeDataSet(LIST_COHORT_INITIAL_DATA_XML);
	}
	
	@Test
	public void getListByUuid_shouldReturnMatchingCohort() {
		Cohort result = fhirCohortListDao.get(LIST_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(LIST_UUID));
	}
	
	@Test
	public void getListByUuid_shouldReturnNullWhenCalledWithUnknownUuid() {
		Cohort result = fhirCohortListDao.get(UNKNOWN_UUID);
		
		assertThat(result, nullValue());
	}
}
