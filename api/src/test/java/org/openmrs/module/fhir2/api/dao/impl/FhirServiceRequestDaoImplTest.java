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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import static org.openmrs.util.PrivilegeConstants.GET_ORDERS;

import java.util.Arrays;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.TestOrder;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.api.dao.FhirServiceRequestDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirServiceRequestDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String TEST_ORDER_UUID = "7d96f25c-4949-4f72-9931-d808fbc226de";
	
	private static final String OTHER_ORDER_UUID = "02b9d1e4-7619-453e-bd6b-c32286f861df";
	
	private static final String WRONG_UUID = "7d96f25c-4949-4f72-9931-d808fbc226dd";
	
	private static final String TEST_ORDER_INITIAL_DATA = "org/openmrs/module/fhir2/api/dao/impl/FhirServiceRequestTest_initial_data.xml";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Autowired
	private ObjectFactory<FhirServiceRequestDao> daoFactory;
	
	private FhirServiceRequestDao dao;
	
	private FhirServiceRequestDaoImpl daoImpl;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(TEST_ORDER_INITIAL_DATA);
		
		dao = daoFactory.getObject();
		daoImpl = new FhirServiceRequestDaoImpl();
		daoImpl.setSessionFactory(sessionFactory);
	}
	
	@Test
	public void shouldRetrieveTestOrderByUuid() {
		TestOrder result = daoImpl.get(TEST_ORDER_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(TestOrder.class));
		assertThat(result.getOrderType().getUuid(), equalTo(OrderType.TEST_ORDER_TYPE_UUID));
		assertThat(result.getUuid(), equalTo(TEST_ORDER_UUID));
	}
	
	@Test
	public void shouldReturnNullIfUuidNotFound() {
		TestOrder result = daoImpl.get(WRONG_UUID);
		assertThat(result, nullValue());
	}
	
	@Test
	public void shouldReturnNullIfUuidIsNotValidTestOrder() {
		TestOrder result = daoImpl.get(OTHER_ORDER_UUID);
		assertThat(result, nullValue());
	}
	
	@Test
	public void shouldRequireGetOrdersPrivilegeForGet() {
		Context.logout();
		
		try {
			dao.get(TEST_ORDER_UUID);
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_ORDERS);
			assertThat(dao.get(TEST_ORDER_UUID), notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_ORDERS);
		}
	}
	
	@Test
	public void shouldRequireGetOrdersPrivilegeForGetByCollection() {
		Context.logout();
		
		try {
			dao.get(Arrays.asList(TEST_ORDER_UUID));
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_ORDERS);
			List<Order> orders = dao.get(Arrays.asList(TEST_ORDER_UUID));
			assertThat(orders, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_ORDERS);
		}
	}
	
	@Test
	public void shouldRequireGetOrdersPrivilegeForGetSearchResults() {
		Context.logout();
		
		try {
			dao.getSearchResults(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_ORDERS);
			List<Order> orders = dao.getSearchResults(new SearchParameterMap());
			assertThat(orders, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_ORDERS);
		}
	}
	
	@Test
	public void shouldRequireGetOrdersPrivilegeForGetSearchResultsCount() {
		Context.logout();
		
		try {
			dao.getSearchResultsCount(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_ORDERS);
			int count = dao.getSearchResultsCount(new SearchParameterMap());
			assertThat(count, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_ORDERS);
		}
	}
}
