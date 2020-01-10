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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.fhir2.Task;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.api.translators.TaskTranslator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FhirTaskServiceImplTest {
	
	private static final int TASK_ID = 1;
	
	private static final String TASK_STATUS_REASON = "task status reason";
	
	private static final String TASK_DESCRIPTION = "task description";
	
	private static final String TASK_UUID = "1232334-2323fg0-2der343-23nj34sd";
	
	@Mock
	private FhirTaskDao dao;
	
	@Mock
	private TaskTranslator taskTranslator;
	
	private FhirTaskServiceImpl fhirTaskService;
	
	private Task task;
	
	@Before
	public void setUp() {
		fhirTaskService = new FhirTaskServiceImpl();
		fhirTaskService.setDao(dao);
		fhirTaskService.setTranslator(taskTranslator);
		
		task = new Task();
		task.setId(TASK_ID);
		task.setUuid(TASK_UUID);
		task.setStatus(Task.TaskStatus.ACCEPTED);
		task.setDescription(TASK_DESCRIPTION);
		task.setStatusReason(TASK_STATUS_REASON);
	}
	
	@Test
	public void shouldRetrieveTaskByUuid() {
		org.hl7.fhir.r4.model.Task fhirTask = new org.hl7.fhir.r4.model.Task();
		fhirTask.setId(TASK_UUID);
		fhirTask.setStatus(org.hl7.fhir.r4.model.Task.TaskStatus.ACCEPTED);
		when(dao.getTaskByUuid(TASK_UUID)).thenReturn(task);
		when(taskTranslator.toFhirResource(task)).thenReturn(fhirTask);
		
		org.hl7.fhir.r4.model.Task result = fhirTaskService.getTaskByUuid(TASK_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(TASK_UUID));
		assertThat(result.getStatus(), equalTo(org.hl7.fhir.r4.model.Task.TaskStatus.ACCEPTED));
	}
	
	@Test
	public void shouldSaveTask() {
		org.hl7.fhir.r4.model.Task fhirTask = new org.hl7.fhir.r4.model.Task();
		fhirTask.setId(TASK_UUID);
		fhirTask.setStatus(org.hl7.fhir.r4.model.Task.TaskStatus.ACCEPTED);
		
		when(taskTranslator.toFhirResource(task)).thenReturn(fhirTask);
		when(taskTranslator.toOpenmrsType(fhirTask)).thenReturn(task);
		when(dao.saveTask(task)).thenReturn(task);

		org.hl7.fhir.r4.model.Task result = fhirTaskService.saveTask(fhirTask);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(TASK_UUID));
		assertThat(result.getStatus(), equalTo(org.hl7.fhir.r4.model.Task.TaskStatus.ACCEPTED));
		
	}
}
