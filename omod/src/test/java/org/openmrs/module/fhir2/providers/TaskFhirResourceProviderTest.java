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
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirTaskService;

@RunWith(MockitoJUnitRunner.class)
public class TaskFhirResourceProviderTest {

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

}
