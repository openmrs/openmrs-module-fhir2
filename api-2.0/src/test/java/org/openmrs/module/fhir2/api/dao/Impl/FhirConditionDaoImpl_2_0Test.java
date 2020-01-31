/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.Impl;

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
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.emrapi.conditionslist.ConditionService;

@RunWith(MockitoJUnitRunner.class)
public class FhirConditionDaoImpl_2_0Test {
	
	private static final String CONDITION_UUID = "a6867095-e2b1-4a68-9aaa-0d161a37ce9c";
	
	private static final String WRONG_CONDITION_UUID = "e348934-e2b1-4a68-9aaa-0d161a37ce9d";
	
	@Mock
	private ConditionService conditionService;
	
	private FhirConditionDaoImpl_2_0 dao;
	
	@Before
	public void setUp() {
		dao = new FhirConditionDaoImpl_2_0();
		dao.setConditionService(conditionService);
		
	}
	
	@Test
	public void shouldReturnCondition() {
		Condition condition = new Condition();
		condition.setUuid(CONDITION_UUID);
		when(conditionService.getConditionByUuid(CONDITION_UUID)).thenReturn(condition);
		Condition result = dao.getConditionByUuid(CONDITION_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), notNullValue());
		assertThat(result.getUuid(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void shouldReturnNullWhenConditionUuidIsWrong() {
		Condition condition = dao.getConditionByUuid(WRONG_CONDITION_UUID);
		assertThat(condition, nullValue());
	}
	
}
