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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.search.param.TaskSearchParams;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

@RunWith(MockitoJUnitRunner.class)
public class FhirTaskServiceImplTest {
	
	private static final String TASK_UUID = "dc9ce8be-3155-4adf-b28f-29436ec30a30";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirResourceHandler<Task> handler;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirTaskServiceImpl service;
	
	@Before
	public void setUp() {
		lenient().when(handler.getImplicitProfile()).thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-task");
		lenient().when(handler.acceptsSearch(any())).thenReturn(true);
		
		service = new FhirTaskServiceImpl();
		service.setHandlers(Collections.singletonList(handler));
		service.setGlobalPropertyService(globalPropertyService);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void searchForTasks_shouldFanOutAndReturnHandlerResults() {
		when(handler.search(any())).thenReturn(bundleOf(1));
		
		TokenAndListParam id = new TokenAndListParam().addAnd(new TokenParam().setValue(TASK_UUID));
		
		IBundleProvider results = service.searchForTasks(TaskSearchParams.builder().id(id).build());
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(1));
		verify(handler).search(any());
	}
	
	@Test
	public void create_shouldDispatchToHandler() {
		Task input = new Task();
		Task created = new Task();
		created.setId(TASK_UUID);
		when(handler.canHandle(input)).thenReturn(true);
		when(handler.create(input)).thenReturn(created);
		
		Task result = service.create(input);
		
		assertThat(result, notNullValue());
		verify(handler).create(input);
	}
	
	@Test
	public void updateTask_shouldDispatchToHandler() {
		Task input = new Task();
		input.setId(TASK_UUID);
		Task updated = new Task();
		updated.setId(TASK_UUID);
		when(handler.exists(TASK_UUID)).thenReturn(true);
		when(handler.update(TASK_UUID, input, null, false)).thenReturn(updated);
		
		Task result = service.update(TASK_UUID, input, null, false);
		
		assertThat(result, notNullValue());
		verify(handler).update(TASK_UUID, input, null, false);
	}
	
	private static IBundleProvider bundleOf(int n) {
		List<Task> rows = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			rows.add(new Task());
		}
		return new MockIBundleProvider<>(rows, 10, 1);
	}
}
