/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;

public class TaskTranslatorImplTest {
	
	private static final String TASK_UUID = "d899333c-5bd4-45cc-b1e7-2f9542dbcbf6";
	
	private static final Task.TaskStatus FHIR_TASK_STATUS = Task.TaskStatus.REQUESTED;
	
	private static final Task.TaskStatus FHIR_NEW_TASK_STATUS = Task.TaskStatus.ACCEPTED;
	
	private static final org.openmrs.module.fhir2.Task.TaskStatus OPENMRS_TASK_STATUS = org.openmrs.module.fhir2.Task.TaskStatus.REQUESTED;
	
	private static final org.openmrs.module.fhir2.Task.TaskStatus OPENMRS_NEW_TASK_STATUS = org.openmrs.module.fhir2.Task.TaskStatus.ACCEPTED;
	
	private static final Task.TaskIntent FHIR_TASK_INTENT = Task.TaskIntent.ORDER;
	
	private static final Task.TaskIntent FHIR_NEW_TASK_INTENT = Task.TaskIntent.ORIGINALORDER;
	
	private static final org.openmrs.module.fhir2.Task.TaskIntent OPENMRS_TASK_INTENT = org.openmrs.module.fhir2.Task.TaskIntent.ORDER;
	
	private static final org.openmrs.module.fhir2.Task.TaskIntent OPENMRS_NEW_TASK_INTENT = org.openmrs.module.fhir2.Task.TaskIntent.ORDER;
	
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
	
	@Test
	public void shouldTranslateNewOpenmrsTask() {
		Task fhirTask = new Task();
		
		org.openmrs.module.fhir2.Task result = taskTranslator.toOpenmrsType(fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), notNullValue());
	}
	
	@Test
	public void shouldTranslateStatusIntentToOpenmrsTask() {
		Task fhirTask = new Task();
		fhirTask.setStatus(FHIR_NEW_TASK_STATUS);
		fhirTask.setIntent(FHIR_TASK_INTENT);
		
		org.openmrs.module.fhir2.Task result = taskTranslator.toOpenmrsType(fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(OPENMRS_NEW_TASK_STATUS));
		assertThat(result.getIntent(), equalTo(OPENMRS_TASK_INTENT));
		assertThat(result.getUuid(), notNullValue());
	}
	
	@Test
	public void shouldIgnoreUUIDForNewOpenmrsTask() {
		Task fhirTask = new Task();
		
		fhirTask.setId(TASK_UUID);
		
		org.openmrs.module.fhir2.Task result = taskTranslator.toOpenmrsType(fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), not(equalTo(TASK_UUID)));
	}
	
	@Test
	public void shouldUpdateExistingOpenmrsTask() {
		org.openmrs.module.fhir2.Task task = new org.openmrs.module.fhir2.Task();
		task.setUuid(TASK_UUID);
		
		Task fhirTask = taskTranslator.toFhirResource(task);
		fhirTask.setStatus(FHIR_NEW_TASK_STATUS);
		
		org.openmrs.module.fhir2.Task result = taskTranslator.toOpenmrsType(task, fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(OPENMRS_NEW_TASK_STATUS));
	}
	
	@Test
	public void shouldCreateOpenmrsTaskWhenNull() {
		Task fhirTask = new Task();
		fhirTask.setId(TASK_UUID);
		fhirTask.setStatus(FHIR_NEW_TASK_STATUS);
		
		org.openmrs.module.fhir2.Task result = taskTranslator.toOpenmrsType(null, fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(OPENMRS_NEW_TASK_STATUS));
	}
	
	@Test
	public void shouldSetOpenmrsTaskUUIDWhenNull() {
		org.openmrs.module.fhir2.Task task = new org.openmrs.module.fhir2.Task();
		task.setUuid(null);
		
		Task fhirTask = taskTranslator.toFhirResource(task);
		fhirTask.setId(TASK_UUID);
		
		org.openmrs.module.fhir2.Task result = taskTranslator.toOpenmrsType(task, fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(TASK_UUID));
	}
	
	@Test
	public void shouldUpdateStatusOnExistingTask() {
		org.openmrs.module.fhir2.Task task = new org.openmrs.module.fhir2.Task();
		task.setStatus(OPENMRS_TASK_STATUS);
		task.setIntent(OPENMRS_TASK_INTENT);
		task.setUuid(TASK_UUID);
		
		Task fhirTask = taskTranslator.toFhirResource(task);
		fhirTask.setStatus(FHIR_NEW_TASK_STATUS);
		
		org.openmrs.module.fhir2.Task result = taskTranslator.toOpenmrsType(task, fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(OPENMRS_NEW_TASK_STATUS));
		assertThat(result.getIntent(), equalTo(OPENMRS_TASK_INTENT));
	}
}
