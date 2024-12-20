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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.OrderType;
import org.openmrs.TestOrder;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirServiceRequestDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String TEST_ORDER_UUID = "7d96f25c-4949-4f72-9931-d808fbc226de";
	
	private static final String OTHER_ORDER_UUID = "02b9d1e4-7619-453e-bd6b-c32286f861df";
	
	private static final String WRONG_UUID = "7d96f25c-4949-4f72-9931-d808fbc226dd";
	
	private static final String TEST_ORDER_INITIAL_DATA = "org/openmrs/module/fhir2/api/dao/impl/FhirServiceRequestTest_initial_data.xml";
	
	private FhirServiceRequestDaoImpl dao;
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(TEST_ORDER_INITIAL_DATA);
		
		dao = new FhirServiceRequestDaoImpl();
		dao.setSessionFactory(sessionFactory);
	}
	
	@Test
	public void shouldRetrieveTestOrderByUuid() {
		TestOrder result = dao.get(TEST_ORDER_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(TestOrder.class));
		assertThat(result.getOrderType().getUuid(), equalTo(OrderType.TEST_ORDER_TYPE_UUID));
		assertThat(result.getUuid(), equalTo(TEST_ORDER_UUID));
	}
	
	@Test
	public void shouldReturnNullIfUuidNotFound() {
		TestOrder result = dao.get(WRONG_UUID);
		assertThat(result, nullValue());
	}
	
	@Test
	public void shouldReturnNullIfUuidIsNotValidTestOrder() {
		TestOrder result = dao.get(OTHER_ORDER_UUID);
		assertThat(result, nullValue());
	}
}
