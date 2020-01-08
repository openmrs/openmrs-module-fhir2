/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TaskTranslatorImplTest {
	
	private static final Task.TaskStatus FHIR_TASK_STATUS = Task.TaskStatus.REQUESTED;
	
	private static final org.openmrs.module.fhir2.Task.TaskStatus OPENMRS_TASK_STATUS = org.openmrs.module.fhir2.Task.TaskStatus.REQUESTED;
	
	private static final Task.TaskIntent FHIR_TASK_INTENT = Task.TaskIntent.ORDER;
	
	private static final org.openmrs.module.fhir2.Task.TaskIntent OPENMRS_TASK_INTENT = org.openmrs.module.fhir2.Task.TaskIntent.ORDER;
	
	private TaskTranslatorImpl taskTranslator;
	
	@Before
	public void setup() {
		taskTranslator = new TaskTranslatorImpl();
		
	}
	
	@Test
	public void shouldTranslateOpenmrsTaskToFhirTask() {
		org.openmrs.module.fhir2.Task task = new org.openmrs.module.fhir2.Task();
		Task result = taskTranslator.toFhirResource(task);
		assertThat(result, notNullValue());
	}
	
	@Test
	public void shouldTranslateFhirTaskToOpenmrsTask() {
		Task task = new Task();
		org.openmrs.module.fhir2.Task result = taskTranslator.toOpenmrsType(task);
		assertThat(result, notNullValue());
	}
	
	@Test
	public void shouldTranslateOpenmrsTaskStatusIntentToFhirTaskStatusIntent() {
		org.openmrs.module.fhir2.Task task = new org.openmrs.module.fhir2.Task();
		task.setStatus(OPENMRS_TASK_STATUS);
		task.setIntent(OPENMRS_TASK_INTENT);
		
		Task result = taskTranslator.toFhirResource(task);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(FHIR_TASK_STATUS));
		assertThat(result.getIntent(), equalTo(FHIR_TASK_INTENT));
	}
	
	@Test
	public void shouldTranslateFhirTaskStatusIntentToOpenmrsTaskStatusIntent() {
		Task task = new Task();
		task.setStatus(FHIR_TASK_STATUS);
		task.setIntent(FHIR_TASK_INTENT);
		
		org.openmrs.module.fhir2.Task result = taskTranslator.toOpenmrsType(task);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(OPENMRS_TASK_STATUS));
		assertThat(result.getIntent(), equalTo(OPENMRS_TASK_INTENT));
	}
}
