/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
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
import org.openmrs.module.fhir2.api.translators.TaskTranslator;
import org.openmrs.module.fhir2.model.FhirTask;

/**
 * Tests the CRUD/search wiring and dispatch predicates of the default Task handler.
 * Orchestrator-level concerns (fan-out, the typed {@code searchForTasks} entry point) live in
 * {@code FhirTaskServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FhirTaskBackedTaskHandlerTest {
	
	private static final String TASK_UUID = "dc9ce8be-3155-4adf-b28f-29436ec30a30";
	
	private static final String WRONG_TASK_UUID = "df34a1c1-f57b-4c33-bee5-e601b56b9d5b";
	
	private static final Task.TaskStatus FHIR_TASK_STATUS = Task.TaskStatus.REQUESTED;
	
	private static final Task.TaskStatus FHIR_NEW_TASK_STATUS = Task.TaskStatus.ACCEPTED;
	
	private static final FhirTask.TaskStatus OPENMRS_TASK_STATUS = FhirTask.TaskStatus.REQUESTED;
	
	private static final FhirTask.TaskStatus OPENMRS_NEW_TASK_STATUS = FhirTask.TaskStatus.ACCEPTED;
	
	private static final Task.TaskIntent FHIR_TASK_INTENT = Task.TaskIntent.ORDER;
	
	private static final FhirTask.TaskIntent OPENMRS_TASK_INTENT = FhirTask.TaskIntent.ORDER;
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirTaskDao dao;
	
	@Mock
	private TaskTranslator translator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<Task> searchQueryInclude;
	
	@Mock
	private SearchQuery<FhirTask, Task, FhirTaskDao, TaskTranslator, SearchQueryInclude<Task>> searchQuery;
	
	private FhirTaskBackedTaskHandler handler;
	
	@Before
	public void setUp() {
		handler = new FhirTaskBackedTaskHandler() {
			
			@Override
			protected void validateObject(FhirTask object) {
			}
		};
		handler.setDao(dao);
		handler.setTranslator(translator);
		handler.setSearchQuery(searchQuery);
		handler.setSearchQueryInclude(searchQueryInclude);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeTaskImplicitProfile() {
		assertThat(handler.getImplicitProfile(), equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-task"));
	}
	
	@Test
	public void canHandle_shouldAlwaysReturnTrue() {
		assertTrue(handler.canHandle(new Task()));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptAnySearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	// ---- get ----
	
	@Test
	public void get_shouldRetrieveTaskByUuid() {
		FhirTask task = new FhirTask();
		Task translatedTask = new Task();
		
		task.setUuid(TASK_UUID);
		translatedTask.setId(TASK_UUID);
		
		when(dao.get(TASK_UUID)).thenReturn(task);
		when(translator.toFhirResource(task)).thenReturn(translatedTask);
		
		Task result = handler.get(TASK_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(translatedTask));
	}
	
	// ---- create ----
	
	@Test
	public void create_shouldCreateNewTask() {
		Task fhirTask = new Task();
		FhirTask openmrsTask = new FhirTask();
		
		fhirTask.setStatus(FHIR_TASK_STATUS);
		fhirTask.setIntent(FHIR_TASK_INTENT);
		
		openmrsTask.setUuid(TASK_UUID);
		openmrsTask.setStatus(OPENMRS_TASK_STATUS);
		openmrsTask.setIntent(OPENMRS_TASK_INTENT);
		
		when(translator.toOpenmrsType(fhirTask)).thenReturn(openmrsTask);
		when(dao.createOrUpdate(openmrsTask)).thenReturn(openmrsTask);
		when(translator.toFhirResource(openmrsTask)).thenReturn(fhirTask);
		
		Task result = handler.create(fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(fhirTask));
	}
	
	// ---- update ----
	
	@Test
	public void update_shouldUpdateExistingTask() {
		Task fhirTask = new Task();
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
		
		Task result = handler.update(TASK_UUID, fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(fhirTask));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void update_shouldThrowInvalidRequestForUuidMismatch() {
		Task fhirTask = new Task();
		fhirTask.setId(TASK_UUID);
		
		handler.update(WRONG_TASK_UUID, fhirTask);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void update_shouldThrowInvalidRequestForMissingUuid() {
		Task fhirTask = new Task();
		
		handler.update(TASK_UUID, fhirTask);
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void update_shouldThrowResourceNotFoundIfTaskDoesNotExist() {
		Task fhirTask = new Task();
		fhirTask.setId(WRONG_TASK_UUID);
		
		when(dao.get(WRONG_TASK_UUID)).thenReturn(null);
		
		handler.update(WRONG_TASK_UUID, fhirTask);
	}
	
	// ---- search ----
	
	@Test
	public void search_shouldReturnTasksByParameters() {
		List<FhirTask> openmrsTasks = new ArrayList<>();
		FhirTask openmrsTask = new FhirTask();
		
		openmrsTask.setUuid(TASK_UUID);
		openmrsTasks.add(openmrsTask);
		
		Task task = new Task();
		task.setId(TASK_UUID);
		
		SearchParameterMap theParams = new SearchParameterMap();
		
		when(dao.getSearchResults(any())).thenReturn(openmrsTasks);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(translator.toFhirResource(openmrsTask)).thenReturn(task);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		
		List<IBaseResource> resultList = get(handler.search(theParams));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(TASK_UUID))));
	}
}
