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
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.search.param.TaskSearchParams;
import org.openmrs.module.fhir2.api.translators.TaskTranslator;
import org.openmrs.module.fhir2.model.FhirTask;

@RunWith(MockitoJUnitRunner.class)
public class FhirTaskServiceImplTest {
	
	private static final Integer TASK_ID = 123;
	
	private static final String TASK_UUID = "dc9ce8be-3155-4adf-b28f-29436ec30a30";
	
	private static final String WRONG_TASK_UUID = "df34a1c1-f57b-4c33-bee5-e601b56b9d5b";
	
	private static final org.hl7.fhir.r4.model.Task.TaskStatus FHIR_TASK_STATUS = org.hl7.fhir.r4.model.Task.TaskStatus.REQUESTED;
	
	private static final org.hl7.fhir.r4.model.Task.TaskStatus FHIR_NEW_TASK_STATUS = org.hl7.fhir.r4.model.Task.TaskStatus.ACCEPTED;
	
	private static final FhirTask.TaskStatus OPENMRS_TASK_STATUS = FhirTask.TaskStatus.REQUESTED;
	
	private static final FhirTask.TaskStatus OPENMRS_NEW_TASK_STATUS = FhirTask.TaskStatus.ACCEPTED;
	
	private static final org.hl7.fhir.r4.model.Task.TaskIntent FHIR_TASK_INTENT = org.hl7.fhir.r4.model.Task.TaskIntent.ORDER;
	
	private static final FhirTask.TaskIntent OPENMRS_TASK_INTENT = FhirTask.TaskIntent.ORDER;
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirTaskDao dao;
	
	@Mock
	private TaskTranslator translator;
	
	@Mock
	private FhirGlobalPropertyService fhirGlobalPropertyService;
	
	@Mock
	private SearchQueryInclude<Task> searchQueryInclude;
	
	@Mock
	SearchQuery<FhirTask, Task, FhirTaskDao, TaskTranslator, SearchQueryInclude<Task>> searchQuery;
	
	private FhirTaskServiceImpl fhirTaskService;
	
	@Before
	public void setUp() {
		fhirTaskService = new FhirTaskServiceImpl() {
			
			@Override
			protected void validateObject(FhirTask object) {
			}
		};
		fhirTaskService.setDao(dao);
		fhirTaskService.setTranslator(translator);
		fhirTaskService.setSearchQuery(searchQuery);
		fhirTaskService.setSearchQueryInclude(searchQueryInclude);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void getTask_shouldRetrieveTaskByUuid() {
		FhirTask task = new FhirTask();
		org.hl7.fhir.r4.model.Task translatedTask = new org.hl7.fhir.r4.model.Task();
		
		task.setUuid(TASK_UUID);
		translatedTask.setId(TASK_UUID);
		
		when(dao.get(TASK_UUID)).thenReturn(task);
		when(translator.toFhirResource(task)).thenReturn(translatedTask);
		
		org.hl7.fhir.r4.model.Task result = fhirTaskService.get(TASK_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(translatedTask));
	}
	
	@Test
	public void saveTask_shouldCreateNewTask() {
		org.hl7.fhir.r4.model.Task fhirTask = new org.hl7.fhir.r4.model.Task();
		FhirTask openmrsTask = new FhirTask();
		
		fhirTask.setStatus(FHIR_TASK_STATUS);
		fhirTask.setIntent(FHIR_TASK_INTENT);
		
		openmrsTask.setUuid(TASK_UUID);
		openmrsTask.setStatus(OPENMRS_TASK_STATUS);
		openmrsTask.setIntent(OPENMRS_TASK_INTENT);
		
		when(translator.toOpenmrsType(fhirTask)).thenReturn(openmrsTask);
		when(dao.createOrUpdate(openmrsTask)).thenReturn(openmrsTask);
		when(translator.toFhirResource(openmrsTask)).thenReturn(fhirTask);
		
		org.hl7.fhir.r4.model.Task result = fhirTaskService.create(fhirTask);
		
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
		when(dao.createOrUpdate(updatedOpenmrsTask)).thenReturn(updatedOpenmrsTask);
		when(dao.get(TASK_UUID)).thenReturn(openmrsTask);
		when(translator.toFhirResource(updatedOpenmrsTask)).thenReturn(fhirTask);
		
		org.hl7.fhir.r4.model.Task result = fhirTaskService.update(TASK_UUID, fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(fhirTask));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateTask_shouldThrowInvalidRequestForUuidMismatch() {
		org.hl7.fhir.r4.model.Task fhirTask = new org.hl7.fhir.r4.model.Task();
		fhirTask.setId(TASK_UUID);
		
		fhirTaskService.update(WRONG_TASK_UUID, fhirTask);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateTask_shouldThrowInvalidRequestForMissingUuid() {
		org.hl7.fhir.r4.model.Task fhirTask = new org.hl7.fhir.r4.model.Task();
		
		fhirTaskService.update(TASK_UUID, fhirTask);
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void updateTask_shouldThrowResourceNotFoundIfTaskDoesNotExist() {
		org.hl7.fhir.r4.model.Task fhirTask = new org.hl7.fhir.r4.model.Task();
		fhirTask.setId(WRONG_TASK_UUID);
		
		when(dao.get(WRONG_TASK_UUID)).thenReturn(null);
		
		fhirTaskService.update(WRONG_TASK_UUID, fhirTask);
	}
	
	@Test
	public void searchForTasks_shouldReturnTasksByParameters() {
		List<FhirTask> openmrsTasks = new ArrayList<>();
		FhirTask openmrsTask = new FhirTask();
		
		openmrsTask.setUuid(TASK_UUID);
		openmrsTasks.add(openmrsTask);
		
		Task task = new Task();
		task.setId(TASK_UUID);
		
		SearchParameterMap theParams = new SearchParameterMap();
		
		when(dao.getSearchResults(any())).thenReturn(openmrsTasks);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, fhirGlobalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(translator.toFhirResource(openmrsTask)).thenReturn(task);
		
		IBundleProvider results = fhirTaskService
		        .searchForTasks(new TaskSearchParams(null, null, null, null, null, null, null, null, null));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(TASK_UUID))));
	}
}
