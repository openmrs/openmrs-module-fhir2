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
import org.openmrs.Cohort;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirGroupDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String COHORT_UUID = "1d64befb-3b2e-48e5-85f5-353d43e23e46";
	
	private static final String NEW_COHORT_UUID = "111ff1a2-c2ef-49fd-836f-8a1d936d9ef0";
	
	private static final String BAD_COHORT_UUID = "005ff1a0-c2ef-49fd-836f-8a1d936d9ef7";
	
	private static final String COHORT_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirCohortDaoImplTest_initial_data.xml";
	
	private static final String COHORT_NAME = "Covid19 patients";
	
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
	public void getByUuid_shouldReturnMatchingCohort() {
		Cohort cohort = dao.get(COHORT_UUID);
		assertThat(cohort, notNullValue());
		assertThat(cohort.getUuid(), equalTo(COHORT_UUID));
		assertThat(cohort.getName(), equalTo(COHORT_NAME));
	}
	
	@Test
	public void getByWithWrongUuid_shouldReturnNullCohort() {
		Cohort cohort = dao.get(BAD_COHORT_UUID);
		assertThat(cohort, nullValue());
	}
	
	@Test
	public void shouldSaveGroup() {
		Cohort cohort = new Cohort();
		cohort.setUuid(NEW_COHORT_UUID);
		cohort.setName(COHORT_NAME);
		cohort.setDescription("Test cohort");
		
		Cohort result = dao.createOrUpdate(cohort);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(NEW_COHORT_UUID));
		assertThat(result.getName(), equalTo(COHORT_NAME));
	}
	
	@Test
	public void shouldUpdateGroupCorrectly() {
		Cohort cohort = dao.get(COHORT_UUID);
		cohort.setName("Update cohort name");
		
		Cohort result = dao.createOrUpdate(cohort);
		assertThat(result, notNullValue());
		assertThat(result.getName(), equalTo("Update cohort name"));
	}
	
	@Test
	public void shouldDeleteGroup() {
		Cohort result = dao.delete(COHORT_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getVoided(), is(true));
		assertThat(result.getVoidReason(), equalTo("Voided via FHIR API"));
	}
	
	@Test
	public void shouldReturnNullIfGroupToDeleteDoesNotExist() {
		Cohort result = dao.delete(BAD_COHORT_UUID);
		
		assertThat(result, nullValue());
	}
}
