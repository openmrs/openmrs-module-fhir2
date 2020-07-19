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

import static org.junit.Assert.assertThrows;

import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import org.hl7.fhir.r4.model.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.OpenmrsObject;

@RunWith(MockitoJUnitRunner.class)
public class FhirConditionServiceImplTest {
	
	private static final String BAD_CONDITION_UUID = "90378769-f1a4-46af-b08b-d9fe8a09034j";
	
	private FhirConditionServiceImpl<?> conditionService;
	
	private Condition condition;
	
	@Before
	@SuppressWarnings("rawtypes")
	public void setup() {
		conditionService = new FhirConditionServiceImpl() {
			
			@Override
			protected void validateObject(OpenmrsObject object) {
			}
		};
		
		condition = new Condition();
		condition.setId(BAD_CONDITION_UUID);
	}
	
	@Test
	public void get_shouldThrowNotImplementedOperationException() {
		assertThrows(NotImplementedOperationException.class, () -> conditionService.get(BAD_CONDITION_UUID));
	}
	
	@Test
	public void create_shouldThrowNotImplementedOperationException() {
		assertThrows(NotImplementedOperationException.class, () -> conditionService.create(condition));
	}
	
	@Test
	public void update_shouldThrowNotImplementedOperationException() {
		assertThrows(NotImplementedOperationException.class, () -> conditionService.update(BAD_CONDITION_UUID, condition));
	}
	
	@Test
	public void delete_shouldThrowNotImplementedOperationException() {
		assertThrows(NotImplementedOperationException.class, () -> conditionService.delete(BAD_CONDITION_UUID));
	}
	
	@Test
	public void searchConditions_shouldThrowNotImplementedOperationException() {
		assertThrows(NotImplementedOperationException.class,
		    () -> conditionService.searchConditions(null, null, null, null, null, null, null, null, null, null));
	}
}
