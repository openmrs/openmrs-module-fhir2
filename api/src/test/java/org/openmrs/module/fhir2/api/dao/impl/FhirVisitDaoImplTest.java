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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import static org.openmrs.util.PrivilegeConstants.GET_VISITS;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Visit;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.api.dao.FhirVisitDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class FhirVisitDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String VISIT_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirVisitDaoImplTest_initial_data.xml";
	
	private static final String VISIT_UUID = "65aefd46-973d-4526-89de-93842c80ad11";
	
	private static final String BAD_VISIT_UUID = "65a7fd46-xx56-4526-89de-93842c8078d11";
	
	@Autowired
	private ObjectFactory<FhirVisitDao> daoFactory;
	
	private FhirVisitDao dao;
	
	@Before
	public void setup() throws Exception {
		dao = daoFactory.getObject();
		executeDataSet(VISIT_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldReturnVisitByUuidForGet() {
		Visit visit = dao.get(VISIT_UUID);
		
		assertThat(visit, notNullValue());
		assertThat(visit.getUuid(), equalTo(VISIT_UUID));
	}
	
	@Test
	public void shouldReturnNullIfVisitNotFoundByUuidForGet() {
		Visit visit = dao.get(BAD_VISIT_UUID);
		
		assertThat(visit, nullValue());
	}
	
	@Test
	public void shouldRequireGetVisitPrivilegeForGet() {
		Context.logout();
		
		try {
			dao.get(VISIT_UUID);
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_VISITS);
			assertThat(dao.get(VISIT_UUID), notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_VISITS);
		}
	}
	
	@Test
	public void shouldRequireGetVisitsPrivilegeForGetByCollection() {
		Context.logout();
		
		try {
			dao.get(Arrays.asList(VISIT_UUID));
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_VISITS);
			List<Visit> visits = dao.get(Arrays.asList(VISIT_UUID));
			assertThat(visits, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_VISITS);
		}
	}
	
	@Test
	public void shouldRequireGetVisitsPrivilegeForGetSearchResults() {
		Context.logout();
		
		try {
			dao.getSearchResults(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_VISITS);
			List<Visit> visits = dao.getSearchResults(new SearchParameterMap());
			assertThat(visits, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_VISITS);
		}
	}
	
	@Test
	public void shouldRequireGetVisitsPrivilegeForGetSearchResultsCount() {
		Context.logout();
		
		try {
			dao.getSearchResultsCount(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_VISITS);
			int count = dao.getSearchResultsCount(new SearchParameterMap());
			assertThat(count, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_VISITS);
		}
	}
}
