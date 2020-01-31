/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.IdType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirConditionService;

@RunWith(MockitoJUnitRunner.class)
public class ConditionFhirResourceProviderTest {
	
	private static final String CONDITION_UUID = "23f620c3-2ecb-4d80-aea8-44fa1c5ff978";
	
	private static final String WRONG_CONDITION_UUID = "ca0dfd38-ee20-41a6-909e-7d84247ca192";
	
	@Mock
	private FhirConditionService conditionService;
	
	private Condition condition;
	
	private ConditionFhirResourceProvider resourceProvider;
	
	@Before
	public void setUp() {
		resourceProvider = new ConditionFhirResourceProvider();
		resourceProvider.setConditionService(conditionService);
	}
	
	@Before
	public void initCondition() {
		condition = new Condition();
		condition.setId(CONDITION_UUID);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Condition.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Condition.class.getName()));
	}
	
	@Test
	public void getConditionByUuid_shouldReturnMatchingEncounter() {
		when(conditionService.getConditionByUuid(CONDITION_UUID)).thenReturn(condition);
		IdType id = new IdType();
		id.setValue(CONDITION_UUID);
		Condition condition = resourceProvider.getConditionByUuid(id);
		assertThat(condition, notNullValue());
		assertThat(condition.getId(), notNullValue());
		assertThat(condition.getId(), equalTo(CONDITION_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getConditionWithWrongUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_CONDITION_UUID);
		Condition result = resourceProvider.getConditionByUuid(id);
		assertThat(result, nullValue());
	}
	
}
