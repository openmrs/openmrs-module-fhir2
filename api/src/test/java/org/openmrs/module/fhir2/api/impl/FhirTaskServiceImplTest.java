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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.Task;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.api.translators.TaskTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirTaskServiceImplTest {
	
	private static final String TASK_UUID = "dc9ce8be-3155-4adf-b28f-29436ec30a30";
	
	private static final org.hl7.fhir.r4.model.Task.TaskStatus FHIR_TASK_STATUS = org.hl7.fhir.r4.model.Task.TaskStatus.REQUESTED;
	
	private static final org.hl7.fhir.r4.model.Task.TaskStatus FHIR_NEW_TASK_STATUS = org.hl7.fhir.r4.model.Task.TaskStatus.ACCEPTED;
	
	private static final org.openmrs.module.fhir2.Task.TaskStatus OPENMRS_TASK_STATUS = org.openmrs.module.fhir2.Task.TaskStatus.REQUESTED;
	
	private static final org.openmrs.module.fhir2.Task.TaskStatus OPENMRS_NEW_TASK_STATUS = org.openmrs.module.fhir2.Task.TaskStatus.ACCEPTED;
	
	private static final org.hl7.fhir.r4.model.Task.TaskIntent FHIR_TASK_INTENT = org.hl7.fhir.r4.model.Task.TaskIntent.ORDER;
	
	private static final org.openmrs.module.fhir2.Task.TaskIntent OPENMRS_TASK_INTENT = org.openmrs.module.fhir2.Task.TaskIntent.ORDER;
	
	@Mock
	FhirTaskDao dao;
	
	@Mock
	TaskTranslator translator;
	
	private FhirTaskServiceImpl fhirTaskService;
	
	@Before
	public void setUp() {
		fhirTaskService = new FhirTaskServiceImpl();
		fhirTaskService.setDao(dao);
		fhirTaskService.setTranslator(translator);
	}
	
	@Test
	public void shouldRetrieveTaskByUuid() {
		Task task = new Task();
		org.hl7.fhir.r4.model.Task translatedTask = new org.hl7.fhir.r4.model.Task();
		
		task.setUuid(TASK_UUID);
		translatedTask.setId(TASK_UUID);
		
		when(dao.getTaskByUuid(TASK_UUID)).thenReturn(task);
		when(translator.toFhirResource(task)).thenReturn(translatedTask);
		
		org.hl7.fhir.r4.model.Task result = fhirTaskService.getTaskByUuid(TASK_UUID);
		assertNotNull(result);
		assertThat(result, equalTo(translatedTask));
	}
	
	@Test
	public void shouldSaveNewTask() {
		org.hl7.fhir.r4.model.Task fhirTask = new org.hl7.fhir.r4.model.Task();
		Task openmrsTask = new Task();
		
		fhirTask.setStatus(FHIR_TASK_STATUS);
		fhirTask.setIntent(FHIR_TASK_INTENT);
		
		openmrsTask.setUuid(TASK_UUID);
		openmrsTask.setStatus(OPENMRS_TASK_STATUS);
		openmrsTask.setIntent(OPENMRS_TASK_INTENT);
		
		when(translator.toOpenmrsType(fhirTask)).thenReturn(openmrsTask);
		when(dao.saveTask(openmrsTask)).thenReturn(openmrsTask);
		when(translator.toFhirResource(openmrsTask)).thenReturn(fhirTask);
		
		org.hl7.fhir.r4.model.Task result = fhirTaskService.saveTask(fhirTask);
		
		assertNotNull(result);
		assertThat(result, equalTo(fhirTask));
	}
	
	@Test
	public void shouldUpdateExistingTask() {
		org.hl7.fhir.r4.model.Task fhirTask = new org.hl7.fhir.r4.model.Task();
		Task openmrsTask = new Task();
		Task updatedOpenmrsTask = new Task();
		
		fhirTask.setId(TASK_UUID);
		fhirTask.setStatus(FHIR_NEW_TASK_STATUS);
		fhirTask.setIntent(FHIR_TASK_INTENT);
		
		openmrsTask.setUuid(TASK_UUID);
		openmrsTask.setStatus(OPENMRS_TASK_STATUS);
		openmrsTask.setIntent(OPENMRS_TASK_INTENT);
		
		updatedOpenmrsTask.setUuid(TASK_UUID);
		updatedOpenmrsTask.setStatus(OPENMRS_NEW_TASK_STATUS);
		openmrsTask.setIntent(OPENMRS_TASK_INTENT);
		
		when(translator.toOpenmrsType(openmrsTask, fhirTask)).thenReturn(updatedOpenmrsTask);
		when(dao.saveTask(updatedOpenmrsTask)).thenReturn(updatedOpenmrsTask);
		when(dao.getTaskByUuid(TASK_UUID)).thenReturn(openmrsTask);
		when(translator.toFhirResource(updatedOpenmrsTask)).thenReturn(fhirTask);
		
		org.hl7.fhir.r4.model.Task result = fhirTaskService.updateTask(TASK_UUID, fhirTask);
		
		assertNotNull(result);
		assertThat(result, equalTo(fhirTask));
		
	}
	
}
