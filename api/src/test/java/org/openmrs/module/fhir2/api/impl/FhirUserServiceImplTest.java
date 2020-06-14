/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.User;
import org.openmrs.module.fhir2.api.dao.FhirUserDao;

@RunWith(MockitoJUnitRunner.class)
public class FhirUserServiceImplTest {
	
	private static final String USER_UUID = "5f07c6ff-c483-4e77-815e-44dd650470e7";
	
	private static final String WRONG_USER_UUID = "1a1d2623-2f67-47de-8fb0-b02f51e378b7";
	
	@Mock
	private FhirUserDao dao;
	
	private FhirUserServiceImpl userService;
	
	@Before
	public void setup() {
		userService = new FhirUserServiceImpl();
		userService.setDao(dao);
	}
	
	@Test
	public void shouldGetUserByUuid() {
		User user = new User();
		user.setUuid(USER_UUID);
		when(dao.getUserByUuid(USER_UUID)).thenReturn(user);
		User result = userService.getUserByUuid(USER_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), notNullValue());
		assertThat(result.getUuid(), equalTo(USER_UUID));
	}
	
	@Test
	public void shouldReturnNullWhenGetByWrongUuid() {
		when(dao.getUserByUuid(WRONG_USER_UUID)).thenReturn(null);
		assertThat(userService.getUserByUuid(WRONG_USER_UUID), nullValue());
	}
	
}
