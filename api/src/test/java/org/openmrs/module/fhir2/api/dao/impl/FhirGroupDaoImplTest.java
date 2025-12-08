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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import static org.openmrs.util.PrivilegeConstants.GET_PATIENT_COHORTS;

import java.util.Arrays;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.api.dao.FhirGroupDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirGroupDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String COHORT_UUID = "1d64befb-3b2e-48e5-85f5-353d43e23e46";
	
	private static final String NEW_COHORT_UUID = "111ff1a2-c2ef-49fd-836f-8a1d936d9ef0";
	
	private static final String BAD_COHORT_UUID = "005ff1a0-c2ef-49fd-836f-8a1d936d9ef7";
	
	private static final String COHORT_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirCohortDaoImplTest_initial_data.xml";
	
	private static final String COHORT_NAME = "Covid19 patients";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Autowired
	private ObjectFactory<FhirGroupDao> daoFactory;
	
	private FhirGroupDao dao;
	
	private FhirGroupDaoImpl daoImpl;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(COHORT_INITIAL_DATA_XML);
		
		dao = daoFactory.getObject();
		daoImpl = new FhirGroupDaoImpl();
		daoImpl.setSessionFactory(sessionFactory);
	}
	
	@Test
	public void get_shouldReturnMatchingCohort() {
		Cohort cohort = dao.get(COHORT_UUID);
		assertThat(cohort, notNullValue());
		assertThat(cohort.getUuid(), equalTo(COHORT_UUID));
		assertThat(cohort.getName(), equalTo(COHORT_NAME));
	}
	
	@Test
	public void get_shouldReturnNullCohortWithWrongUuid() {
		Cohort cohort = dao.get(BAD_COHORT_UUID);
		assertThat(cohort, nullValue());
	}
	
	@Test
	public void get_shouldRequireGetPatientCohortsPrivilege() {
		Context.logout();
		
		try {
			dao.get(COHORT_UUID);
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_PATIENT_COHORTS);
			assertThat(dao.get(COHORT_UUID), notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_PATIENT_COHORTS);
		}
	}
	
	@Test
	public void get_shouldRequireGetPatientCohortsPrivilegeWithCollection() {
		Context.logout();
		
		try {
			dao.get(Arrays.asList(COHORT_UUID));
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_PATIENT_COHORTS);
			List<Cohort> cohorts = dao.get(Arrays.asList(COHORT_UUID));
			assertThat(cohorts, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_PATIENT_COHORTS);
		}
	}
	
	@Test
	public void getSearchResults_shouldRequireGetPatientCohortsPrivilege() {
		Context.logout();
		
		try {
			dao.getSearchResults(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_PATIENT_COHORTS);
			List<Cohort> cohorts = dao.getSearchResults(new SearchParameterMap());
			assertThat(cohorts, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_PATIENT_COHORTS);
		}
	}
	
	@Test
	public void getSearchResultsCount_shouldRequireGetPatientCohortsPrivilege() {
		Context.logout();
		
		try {
			dao.getSearchResultsCount(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_PATIENT_COHORTS);
			int count = dao.getSearchResultsCount(new SearchParameterMap());
			assertThat(count, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_PATIENT_COHORTS);
		}
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
		Cohort result = daoImpl.delete(BAD_COHORT_UUID);
		
		assertThat(result, nullValue());
	}
}
