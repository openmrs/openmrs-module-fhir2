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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirTask;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.api.translators.TaskTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirTaskServiceImplTest {
	
	private static final String TASK_UUID = "dc9ce8be-3155-4adf-b28f-29436ec30a30";
	
	private static final String WRONG_TASK_UUID = "df34a1c1-f57b-4c33-bee5-e601b56b9d5b";
	
	private static final String SERVICE_REQUEST_UUID = "9bf48663-be75-49d1-91a8-16b71287db1a";
	
	private static final org.hl7.fhir.r4.model.Task.TaskStatus FHIR_TASK_STATUS = org.hl7.fhir.r4.model.Task.TaskStatus.REQUESTED;
	
	private static final org.hl7.fhir.r4.model.Task.TaskStatus FHIR_NEW_TASK_STATUS = org.hl7.fhir.r4.model.Task.TaskStatus.ACCEPTED;
	
	private static final FhirTask.TaskStatus OPENMRS_TASK_STATUS = FhirTask.TaskStatus.REQUESTED;
	
	private static final FhirTask.TaskStatus OPENMRS_NEW_TASK_STATUS = FhirTask.TaskStatus.ACCEPTED;
	
	private static final org.hl7.fhir.r4.model.Task.TaskIntent FHIR_TASK_INTENT = org.hl7.fhir.r4.model.Task.TaskIntent.ORDER;
	
	private static final FhirTask.TaskIntent OPENMRS_TASK_INTENT = FhirTask.TaskIntent.ORDER;
	
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
	public void getTaskByUuid_shouldRetrieveTaskByUuid() {
		FhirTask task = new FhirTask();
		org.hl7.fhir.r4.model.Task translatedTask = new org.hl7.fhir.r4.model.Task();
		
		task.setUuid(TASK_UUID);
		translatedTask.setId(TASK_UUID);
		
		when(dao.getTaskByUuid(TASK_UUID)).thenReturn(task);
		when(translator.toFhirResource(task)).thenReturn(translatedTask);
		
		org.hl7.fhir.r4.model.Task result = fhirTaskService.getTaskByUuid(TASK_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(translatedTask));
	}
	
	@Test
	public void saveTask_shouldSaveNewTask() {
		org.hl7.fhir.r4.model.Task fhirTask = new org.hl7.fhir.r4.model.Task();
		FhirTask openmrsTask = new FhirTask();
		
		fhirTask.setStatus(FHIR_TASK_STATUS);
		fhirTask.setIntent(FHIR_TASK_INTENT);
		
		openmrsTask.setUuid(TASK_UUID);
		openmrsTask.setStatus(OPENMRS_TASK_STATUS);
		openmrsTask.setIntent(OPENMRS_TASK_INTENT);
		
		when(translator.toOpenmrsType(fhirTask)).thenReturn(openmrsTask);
		when(dao.saveTask(openmrsTask)).thenReturn(openmrsTask);
		when(translator.toFhirResource(openmrsTask)).thenReturn(fhirTask);
		
		org.hl7.fhir.r4.model.Task result = fhirTaskService.saveTask(fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(fhirTask));
	}
	
	@Test
	public void updateTask_shouldUpdateExistingTask() {
		org.hl7.fhir.r4.model.Task fhirTask = new org.hl7.fhir.r4.model.Task();
		FhirTask openmrsTask = new FhirTask();
		FhirTask updatedOpenmrsTask = new FhirTask();
		
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
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(fhirTask));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateTask_shouldThrowInvalidRequestForUuidMismatch() {
		org.hl7.fhir.r4.model.Task fhirTask = new org.hl7.fhir.r4.model.Task();
		fhirTask.setId(TASK_UUID);
		
		fhirTaskService.updateTask(WRONG_TASK_UUID, fhirTask);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateTask_shouldThrowInvalidRequestForMissingUuid() {
		org.hl7.fhir.r4.model.Task fhirTask = new org.hl7.fhir.r4.model.Task();
		
		fhirTaskService.updateTask(TASK_UUID, fhirTask);
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void updateTask_shouldThrowMethodNotAllowedIfTaskDoesNotExist() {
		org.hl7.fhir.r4.model.Task fhirTask = new org.hl7.fhir.r4.model.Task();
		fhirTask.setId(WRONG_TASK_UUID);
		
		when(dao.getTaskByUuid(WRONG_TASK_UUID)).thenReturn(null);
		
		fhirTaskService.updateTask(WRONG_TASK_UUID, fhirTask);
	}
	
	@Test
	public void getTaskByBasedOn_shouldGetTasksByBasedOnServiceRequest() {
		Collection<FhirTask> basedOnTasks;
		Collection<org.hl7.fhir.r4.model.Task> basedOnFhirTasks;
		
		FhirTask t1 = new FhirTask();
		FhirTask t2 = new FhirTask();
		
		org.hl7.fhir.r4.model.Task f1 = new org.hl7.fhir.r4.model.Task();
		org.hl7.fhir.r4.model.Task f2 = new org.hl7.fhir.r4.model.Task();
		
		basedOnTasks = Arrays.asList(t1, t2);
		basedOnFhirTasks = Arrays.asList(f1, f2);
		
		when(dao.getTasksByBasedOnUuid(ServiceRequest.class, SERVICE_REQUEST_UUID)).thenReturn(basedOnTasks);
		when(translator.toFhirResource(t1)).thenReturn(f1);
		when(translator.toFhirResource(t2)).thenReturn(f2);
		
		Collection<org.hl7.fhir.r4.model.Task> result = fhirTaskService.getTasksByBasedOn(ServiceRequest.class,
		    SERVICE_REQUEST_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(basedOnFhirTasks));
	}
	
	@Test
	public void shouldReturnEmptyListForTaskByBasedOnForUnsupportedResources() {
		Collection<org.hl7.fhir.r4.model.Task> result = fhirTaskService.getTasksByBasedOn(Patient.class,
		    SERVICE_REQUEST_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result, empty());
	}
	
	@Test
	public void shouldReturnEmptyListForNoAssociatedTasks() {
		when(dao.getTasksByBasedOnUuid(ServiceRequest.class, SERVICE_REQUEST_UUID)).thenReturn(Collections.emptyList());
		
		Collection<org.hl7.fhir.r4.model.Task> result = fhirTaskService.getTasksByBasedOn(ServiceRequest.class,
		    SERVICE_REQUEST_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result, empty());
	}
	
	@Test
	public void searchForTasks_shouldReturnTasksByParameters() {
		Collection<FhirTask> openmrsTasks = new ArrayList<>();
		FhirTask openmrsTask = new FhirTask();
		
		openmrsTask.setUuid(TASK_UUID);
		openmrsTasks.add(openmrsTask);
		
		Task task = new Task();
		task.setId(TASK_UUID);
		
		when(dao.searchForTasks(any(), any(), any(), any())).thenReturn(openmrsTasks);
		when(translator.toFhirResource(openmrsTask)).thenReturn(task);
		
		Collection<Task> results = fhirTaskService.searchForTasks(null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("id", equalTo(TASK_UUID))));
	}
}
