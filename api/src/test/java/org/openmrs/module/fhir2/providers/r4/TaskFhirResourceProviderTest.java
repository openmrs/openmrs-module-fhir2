/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import org.hamcrest.Matchers;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.openmrs.module.fhir2.api.search.param.TaskSearchParams;
import org.openmrs.module.fhir2.providers.BaseFhirProvenanceResourceTest;

@RunWith(MockitoJUnitRunner.class)
public class TaskFhirResourceProviderTest extends BaseFhirProvenanceResourceTest<Task> {
	
	private static final String TASK_UUID = "bdd7e368-3d1a-42a9-9538-395391b64adf";
	
	private static final String WRONG_TASK_UUID = "df34a1c1-f57b-4c33-bee5-e601b56b9d5b";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	private static final int PREFERRED_PAGE_SIZE = 10;
	
	private static final int COUNT = 1;
	
	@Mock
	private FhirTaskService taskService;
	
	@Mock
	private RequestDetails mockRequestDetails;
	
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
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
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
		when(taskService.get(TASK_UUID)).thenReturn(task);
		
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
	public void createTask_shouldCreateNewTask() {
		when(taskService.create(task)).thenReturn(task);
		
		MethodOutcome result = resourceProvider.createTask(task);
		assertThat(result.getResource(), equalTo(task));
	}
	
	@Test
	public void doUpsert_shouldUpdateTask() {
		when(taskService.update(TASK_UUID, task, mockRequestDetails, false)).thenReturn(task);
		
		IdType uuid = new IdType();
		uuid.setValue(TASK_UUID);
		
		MethodOutcome result = resourceProvider.doUpsert(uuid, task, mockRequestDetails, false);
		assertThat(result.getResource(), equalTo(task));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void doUpsert_shouldThrowInvalidRequestForTaskUuidMismatch() {
		when(taskService.update(WRONG_TASK_UUID, task, mockRequestDetails, false)).thenThrow(InvalidRequestException.class);
		
		resourceProvider.doUpsert(new IdType().setValue(WRONG_TASK_UUID), task, mockRequestDetails, false);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void doUpsert_shouldThrowInvalidRequestIfTaskHasNoUuid() {
		Task noIdTask = new Task();
		
		when(taskService.update(TASK_UUID, noIdTask, mockRequestDetails, false)).thenThrow(InvalidRequestException.class);
		
		resourceProvider.doUpsert(new IdType().setValue(TASK_UUID), noIdTask, mockRequestDetails, false);
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void doUpsert_shouldThrowMethodNotAllowedIfTaskDoesNotExist() {
		Task wrongTask = new Task();
		wrongTask.setId(WRONG_TASK_UUID);
		
		when(taskService.update(WRONG_TASK_UUID, wrongTask, mockRequestDetails, false))
		        .thenThrow(MethodNotAllowedException.class);
		
		resourceProvider.doUpsert(new IdType().setValue(WRONG_TASK_UUID), wrongTask, mockRequestDetails, false);
	}
	
	@Test
	public void searchTasks_shouldReturnMatchingTasks() {
		List<Task> tasks = new ArrayList<>();
		tasks.add(task);
		
		when(taskService.searchForTasks(any())).thenReturn(new MockIBundleProvider<>(tasks, 10, 1));
		
		TokenAndListParam status = new TokenAndListParam();
		TokenParam statusToken = new TokenParam();
		statusToken.setValue("ACCEPTED");
		status.addAnd(new TokenOrListParam().add(statusToken));
		
		IBundleProvider results = resourceProvider.searchTasks(null, null, null, null, status, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.iterator().next().fhirType(), equalTo(FhirConstants.TASK));
	}
	
	@Test
	public void deleteTask_shouldDeleteRequestedTask() {
		OperationOutcome result = resourceProvider.deleteTask(new IdType().setValue(TASK_UUID));
		
		assertThat(result, notNullValue());
		assertThat(result.getIssue(), notNullValue());
		assertThat(result.getIssueFirstRep().getSeverity(), equalTo(OperationOutcome.IssueSeverity.INFORMATION));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getCode(), equalTo("MSG_DELETED"));
	}
	
	@Test
	public void searchTasks_shouldAddRelatedPatientResourceToResultListWhenIncluded() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("Task:patient"));
		
		when(taskService.searchForTasks(new TaskSearchParams(null, null, null, null, null, null, null, null, includes)))
		        .thenReturn(new MockIBundleProvider<>(Arrays.asList(task, new Patient()), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchTasks(null, null, null, null, null, null, null, null, includes);
		
		List<IBaseResource> resources = getResources(results);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(Matchers.equalTo(2)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), Matchers.equalTo(FhirConstants.TASK));
		assertThat(resources.get(1).fhirType(), Matchers.equalTo(FhirConstants.PATIENT));
		assertThat(resources.get(0).getIdElement().getIdPart(), Matchers.equalTo(TASK_UUID));
	}
	
	@Test
	public void searchTasks_shouldAddRelatedPractionerResourceToResultListWhenIncluded() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("Task:owner"));
		
		when(taskService.searchForTasks(new TaskSearchParams(null, null, null, null, null, null, null, null, includes)))
		        .thenReturn(new MockIBundleProvider<>(Arrays.asList(task, new Practitioner()), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchTasks(null, null, null, null, null, null, null, null, includes);
		
		List<IBaseResource> resources = getResources(results);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(Matchers.equalTo(2)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), Matchers.equalTo(FhirConstants.TASK));
		assertThat(resources.get(1).fhirType(), Matchers.equalTo(FhirConstants.PRACTITIONER));
		assertThat(resources.get(0).getIdElement().getIdPart(), Matchers.equalTo(TASK_UUID));
	}
	
	@Test
	public void searchTasks_shouldAddRelatedEncounterResourceToResultListWhenIncluded() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("Task:encounter"));
		
		when(taskService.searchForTasks(new TaskSearchParams(null, null, null, null, null, null, null, null, includes)))
		        .thenReturn(new MockIBundleProvider<>(Arrays.asList(task, new Encounter()), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchTasks(null, null, null, null, null, null, null, null, includes);
		
		List<IBaseResource> resources = getResources(results);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(Matchers.equalTo(2)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), Matchers.equalTo(FhirConstants.TASK));
		assertThat(resources.get(1).fhirType(), Matchers.equalTo(FhirConstants.ENCOUNTER));
		assertThat(resources.get(0).getIdElement().getIdPart(), Matchers.equalTo(TASK_UUID));
	}
	
	@Test
	public void searchTasks_shouldAddRelatedServiceRequestResourceToResultListWhenIncluded() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("Task:based-on"));
		
		when(taskService.searchForTasks(new TaskSearchParams(null, null, null, null, null, null, null, null, includes)))
		        .thenReturn(
		            new MockIBundleProvider<>(Arrays.asList(task, new ServiceRequest()), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchTasks(null, null, null, null, null, null, null, null, includes);
		
		List<IBaseResource> resources = getResources(results);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(Matchers.equalTo(2)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), Matchers.equalTo(FhirConstants.TASK));
		assertThat(resources.get(1).fhirType(), Matchers.equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), Matchers.equalTo(TASK_UUID));
	}
	
	private List<IBaseResource> getResources(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
}
