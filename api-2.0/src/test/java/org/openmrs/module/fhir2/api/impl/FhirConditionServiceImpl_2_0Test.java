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
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirConditionServiceImpl_2_0Test {
	
	private static final String CONDITION_UUID = "ca0dfd38-ee20-41a6-909e-7d84247ca192";
	
	private static final String WRONG_CONDITION_UUID = "tx0dfd38-ee20-41a6-909e-7d84247c8340";
	
	@Mock
	private FhirConditionDao<Condition> dao;
	
	@Mock
	private ConditionTranslator<Condition> conditionTranslator;
	
	private FhirConditionServiceImpl_2_0 conditionServiceImpl_2_0;
	
	private org.hl7.fhir.r4.model.Condition fhirCondition;
	
	@Before
	public void setup() {
		conditionServiceImpl_2_0 = new FhirConditionServiceImpl_2_0();
		conditionServiceImpl_2_0.setDao(dao);
		conditionServiceImpl_2_0.setConditionTranslator(conditionTranslator);
		
		fhirCondition = new org.hl7.fhir.r4.model.Condition();
		fhirCondition.setId(CONDITION_UUID);
	}
	
	@Test
	public void getConditionByUuid_shouldReturnCondition() {
		Condition condition = new Condition();
		condition.setUuid(CONDITION_UUID);
		when(dao.getConditionByUuid(CONDITION_UUID)).thenReturn(condition);
		when(conditionTranslator.toFhirResource(condition)).thenReturn(fhirCondition);
		org.hl7.fhir.r4.model.Condition result = conditionServiceImpl_2_0.getConditionByUuid(CONDITION_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void getConditionByWrongUuid_shouldReturnCondition() {
		assertThat(conditionServiceImpl_2_0.getConditionByUuid(WRONG_CONDITION_UUID), nullValue());
		
	}
}
