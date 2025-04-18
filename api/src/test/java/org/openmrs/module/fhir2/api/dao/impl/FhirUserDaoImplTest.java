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

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirUserDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String USER_NAME = "firstaccount";
	
	private static final String USER_INITIAL_DATA_XML = "org/openmrs/api/include/UserServiceTest.xml";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	private FhirUserDaoImpl fhirUserDao;
	
	@Before
	public void setup() throws Exception {
		fhirUserDao = new FhirUserDaoImpl();
		fhirUserDao.setSessionFactory(sessionFactory);
		executeDataSet(USER_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldGetUserByUserName() {
		User user = fhirUserDao.getUserByUserName(USER_NAME);
		
		assertThat(user, notNullValue());
		assertThat(user.getUsername(), equalTo(USER_NAME));
	}
	
}
