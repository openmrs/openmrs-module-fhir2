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
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.fail;
import static org.openmrs.util.PrivilegeConstants.GET_USERS;

import java.util.Arrays;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.api.dao.FhirUserDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirUserDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String USER_NAME = "firstaccount";
	
	private static final String USER_UUID = "c98a1558-e131-11de-babe-001e378eb67e";
	
	private static final String USER_INITIAL_DATA_XML = "org/openmrs/api/include/UserServiceTest.xml";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Autowired
	private ObjectFactory<FhirUserDao> daoFactory;
	
	private FhirUserDao dao;
	
	private FhirUserDaoImpl daoImpl;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(USER_INITIAL_DATA_XML);
		
		dao = daoFactory.getObject();
		daoImpl = new FhirUserDaoImpl();
		daoImpl.setSessionFactory(sessionFactory);
	}
	
	@Test
	public void shouldGetUserByUserName() {
		User user = daoImpl.getUserByUserName(USER_NAME);
		
		assertThat(user, notNullValue());
		assertThat(user.getUsername(), equalTo(USER_NAME));
	}
	
	@Test
	public void shouldRequireGetUsersPrivilegeForGet() {
		Context.logout();
		
		try {
			dao.get(USER_UUID);
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_USERS);
			assertThat(dao.get(USER_UUID), notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_USERS);
		}
	}
	
	@Test
	public void shouldRequireGetUsersPrivilegeForGetByCollection() {
		Context.logout();
		
		try {
			dao.get(Arrays.asList(USER_UUID));
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_USERS);
			List<User> users = dao.get(Arrays.asList(USER_UUID));
			assertThat(users, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_USERS);
		}
	}
	
	@Test
	public void shouldRequireGetUsersPrivilegeForGetSearchResults() {
		Context.logout();
		
		try {
			dao.getSearchResults(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_USERS);
			List<User> users = dao.getSearchResults(new SearchParameterMap());
			assertThat(users, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_USERS);
		}
	}
	
	@Test
	public void shouldRequireGetUsersPrivilegeForGetSearchResultsCount() {
		Context.logout();
		
		try {
			dao.getSearchResultsCount(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_USERS);
			int count = dao.getSearchResultsCount(new SearchParameterMap());
			assertThat(count, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_USERS);
		}
	}
}
