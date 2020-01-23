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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.OrderType;
import org.openmrs.TestOrder;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirServiceRequestDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String TEST_ORDER_UUID = "7d96f25c-4949-4f72-9931-d808fbc226de";
	
	private static final String OTHER_ORDER_UUID = "02b9d1e4-7619-453e-bd6b-c32286f861df";
	
	private static final String WRONG_UUID = "7d96f25c-4949-4f72-9931-d808fbc226dd";
	
	private static final String TEST_ORDER_INITIAL_DATA = "org/openmrs/module/fhir2/api/dao/impl/FhirServiceRequestTest_initial_data.xml";
	
	private FhirServiceRequestDaoImpl dao;
	
	@Inject
	@Named("orderService")
	private Provider<OrderService> orderServiceProvider;
	
	@Inject
	@Named("sessionFactory")
	private Provider<SessionFactory> sessionFactoryProvider;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(TEST_ORDER_INITIAL_DATA);
		
		dao = new FhirServiceRequestDaoImpl();
		dao.setSessionFactory(sessionFactoryProvider.get());
		dao.setOrderService(orderServiceProvider.get());
	}
	
	@Test
	public void shouldRetrieveTestOrderByUuid() {
		TestOrder result = dao.getTestOrderByUuid(TEST_ORDER_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(TestOrder.class));
		assertThat(result.getOrderType().getUuid(), equalTo(OrderType.TEST_ORDER_TYPE_UUID));
		assertThat(result.getUuid(), equalTo(TEST_ORDER_UUID));
	}
	
	@Test
	public void shouldReturnNullIfUuidNotFound() {
		TestOrder result = dao.getTestOrderByUuid(WRONG_UUID);
		assertThat(result, nullValue());
	}
	
	@Test
	public void shouldReturnNullIfUuidIsNotValidTestOrder() {
		TestOrder result = dao.getTestOrderByUuid(OTHER_ORDER_UUID);
		assertThat(result, nullValue());
	}
}
