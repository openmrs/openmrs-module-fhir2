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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import org.hamcrest.Matchers;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.openmrs.module.fhir2.web.servlet.BaseFhirProvenanceResourceTest;

@RunWith(MockitoJUnitRunner.class)
public class TaskFhirResourceProviderTest extends BaseFhirProvenanceResourceTest<Task> {
	
	private static final String TASK_UUID = "bdd7e368-3d1a-42a9-9538-395391b64adf";
	
	private static final String WRONG_TASK_UUID = "df34a1c1-f57b-4c33-bee5-e601b56b9d5b";
	
	@Mock
	private FhirTaskService taskService;
	
	@Getter(AccessLevel.PUBLIC)
	private TaskFhirResourceProvider resourceProvider;
	
	private Task task;
	
	@Before
	public void setup() {
		resourceProvider = new TaskFhirResourceProvider();
		resourceProvider.setService(taskService);
	}
	
	@Before
	public void initTask() {
		task = new Task();
		task.setId(TASK_UUID);
		setProvenanceResources(task);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Task.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Task.class.getName()));
	}
	
	@Test
	public void getTaskById_shouldReturnMatchingTask() {
		IdType id = new IdType();
		id.setValue(TASK_UUID);
		when(taskService.getTaskByUuid(TASK_UUID)).thenReturn(task);
		
		Task result = resourceProvider.getTaskById(id);
		
		assertThat(result.isResource(), is(true));
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(TASK_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getTaskByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_TASK_UUID);
		
		assertThat(resourceProvider.getTaskById(idType).isResource(), is(true));
		assertThat(resourceProvider.getTaskById(idType), nullValue());
	}
	
	@Test
	public void getTaskHistoryById_shouldReturnListOfResource() {
		IdType id = new IdType();
		id.setValue(TASK_UUID);
		when(taskService.getTaskByUuid(TASK_UUID)).thenReturn(task);
		
		List<Resource> resources = resourceProvider.getTaskHistoryById(id);
		assertThat(resources, Matchers.notNullValue());
		assertThat(resources, not(empty()));
		assertThat(resources.size(), Matchers.equalTo(2));
	}
	
	@Test
	public void getTaskHistoryById_shouldReturnProvenanceResources() {
		IdType id = new IdType();
		id.setValue(TASK_UUID);
		when(taskService.getTaskByUuid(TASK_UUID)).thenReturn(task);
		
		List<Resource> resources = resourceProvider.getTaskHistoryById(id);
		assertThat(resources, not(empty()));
		assertThat(resources.stream().findAny().isPresent(), Matchers.is(true));
		assertThat(resources.stream().findAny().get().getResourceType().name(),
		    Matchers.equalTo(Provenance.class.getSimpleName()));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getTaskHistoryByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_TASK_UUID);
		assertThat(resourceProvider.getTaskHistoryById(idType).isEmpty(), Matchers.is(true));
		assertThat(resourceProvider.getTaskHistoryById(idType).size(), Matchers.equalTo(0));
	}
	
	@Test
	public void createTask_shouldCreateNewTask() {
		when(taskService.saveTask(task)).thenReturn(task);
		
		MethodOutcome result = resourceProvider.createTask(task);
		assertThat(result.getResource(), equalTo(task));
	}
	
	@Test
	public void updateTask_shouldUpdateTask() {
		when(taskService.updateTask(TASK_UUID, task)).thenReturn(task);
		
		IdType uuid = new IdType();
		uuid.setValue(TASK_UUID);
		
		MethodOutcome result = resourceProvider.updateTask(uuid, task);
		assertThat(result.getResource(), equalTo(task));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateTask_shouldThrowInvalidRequestForTaskUuidMismatch() {
		when(taskService.updateTask(WRONG_TASK_UUID, task)).thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateTask(new IdType().setValue(WRONG_TASK_UUID), task);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateTask_shouldThrowInvalidRequestIfTaskHasNoUuid() {
		Task noIdTask = new Task();
		
		when(taskService.updateTask(TASK_UUID, noIdTask)).thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateTask(new IdType().setValue(TASK_UUID), noIdTask);
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void updateTask_shouldThrowMethodNotAllowedIfTaskDoesNotExist() {
		Task wrongTask = new Task();
		wrongTask.setId(WRONG_TASK_UUID);
		
		when(taskService.updateTask(WRONG_TASK_UUID, wrongTask)).thenThrow(MethodNotAllowedException.class);
		
		resourceProvider.updateTask(new IdType().setValue(WRONG_TASK_UUID), wrongTask);
	}
	
	@Test
	public void searchTasks_shouldReturnMatchingTasks() {
		List<Task> tasks = new ArrayList<>();
		tasks.add(task);
		
		when(taskService.searchForTasks(any(), any(), any(), any())).thenReturn(tasks);
		
		TokenAndListParam status = new TokenAndListParam();
		TokenParam statusToken = new TokenParam();
		statusToken.setValue("ACCEPTED");
		status.addAnd(new TokenOrListParam().add(statusToken));
		
		Bundle results = resourceProvider.searchTasks(null, null, status, null);
		
		assertThat(results, notNullValue());
		assertThat(results.getTotal(), equalTo(1));
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().fhirType(), equalTo("Task"));
		assertThat(results.getEntry().get(0).getResource().getId(), equalTo(TASK_UUID));
	}
}
