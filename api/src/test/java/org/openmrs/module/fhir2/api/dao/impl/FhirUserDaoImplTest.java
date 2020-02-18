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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirUserDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String USER_UUID = "2eadf946-e53c-11de-8404-001e378eb67e";
	
	private static final String WRONG_USER_UUID = "5f07c6ff-c483-4e77-815e-44dd650470e7";
	
	private static final String USER_INITIAL_DATA_XML = "org/openmrs/api/include/UserServiceTest.xml";
	
	@Inject
	@Named("sessionFactory")
	private Provider<SessionFactory> sessionFactoryProvider;
	
	private FhirUserDaoImpl fhirUserDao;
	
	@Before
	public void setup() throws Exception {
		fhirUserDao = new FhirUserDaoImpl();
		fhirUserDao.setSessionFactory(sessionFactoryProvider.get());
		executeDataSet(USER_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldGetUserByUuid() {
		User user = fhirUserDao.getUserByUuid(USER_UUID);
		assertThat(user, notNullValue());
		assertThat(user.getUuid(), notNullValue());
		assertThat(user.getUuid(), equalTo(USER_UUID));
	}
	
	@Test
	public void shouldReturnNullWhenGetByWrongUuid() {
		assertThat(fhirUserDao.getUserByUuid(WRONG_USER_UUID), nullValue());
	}
	
}
