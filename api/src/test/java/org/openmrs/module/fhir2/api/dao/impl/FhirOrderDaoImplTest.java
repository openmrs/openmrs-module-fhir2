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
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.TestOrder;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirOrderDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String ORDER_INITIAL_DATA = "org/openmrs/module/fhir2/api/dao/impl/FhirServiceRequestTest_initial_data.xml";
	
	private static final String ORDER_UUID = "7d96f25c-4949-4f72-9931-d808fbc226de";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	private FhirOrderDaoImpl dao;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(ORDER_INITIAL_DATA);
		
		dao = new FhirOrderDaoImpl();
		dao.setSessionFactory(sessionFactory);
	}
	
	@Test
	public void shouldRetrieveOrderByUuid() {
		Order result = dao.get(ORDER_UUID);
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(TestOrder.class));
		assertThat(result.getOrderType().getUuid(), equalTo(OrderType.TEST_ORDER_TYPE_UUID));
		assertThat(result.getUuid(), equalTo(ORDER_UUID));
	}
	
}
