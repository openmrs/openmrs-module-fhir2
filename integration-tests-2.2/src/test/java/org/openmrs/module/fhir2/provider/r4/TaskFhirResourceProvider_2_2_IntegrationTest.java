/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.provider.r4;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import java.io.InputStream;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.providers.r4.BaseFhirR4IntegrationTest;
import org.openmrs.module.fhir2.providers.r4.TaskFhirResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class TaskFhirResourceProvider_2_2_IntegrationTest extends BaseFhirR4IntegrationTest<TaskFhirResourceProvider, Task> {
	
	private static final String TASK_UUID = "d899333c-5bd4-45cc-b1e7-2f9542dbcbf6";
	
	private static final String LOCATION_UUID = "58ab6cf9-ea12-43bc-98a6-40353423331e";
	
	private static final String TASK_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirTask_2_2_initial_data.xml";
	
	private static final String JSON_PATCH_TASK_PATH = "org/openmrs/module/fhir2/providers/Task_merge_json_patch.json";
	
	private static final String JSON_PATCH_TASK_TEXT = "[\n    { \"op\": \"replace\", \"path\": \"/status\", \"value\": \"requested\" }\n]";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private TaskFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(TASK_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingTaskAsJson() throws Exception {
		MockHttpServletResponse response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Task task = readResponse(response);
		
		assertThat(task, notNullValue());
		assertThat(task.getIdElement().getIdPart(), equalTo(TASK_UUID));
		assertThat(task.getStatus(), is(Task.TaskStatus.ACCEPTED));
		assertThat(task.getIntent(), is(Task.TaskIntent.ORDER));
		assertThat(task.getLocation().getReference(), is(LOCATION_UUID));
		assertThat(task, validResource());
	}
	
	@Test
	public void shouldPatchExistingTaskAsJsonUsingJsonMergePatch() throws Exception {
		String jsonTaskPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_TASK_PATH)) {
			Objects.requireNonNull(is);
			jsonTaskPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/Task/" + TASK_UUID).jsonMergePatch(jsonTaskPatch)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Task task = readResponse(response);
		
		assertThat(task, notNullValue());
		assertThat(task.getIdElement().getIdPart(), equalTo(TASK_UUID));
		assertThat(task, validResource());
		assertThat(task.getStatus(), is(Task.TaskStatus.REQUESTED));
	}
	
	@Test
	public void shouldPatchExistingTaskAsJsonUsingJsonPatch() throws Exception {
		MockHttpServletResponse response = patch("/Task/" + TASK_UUID).jsonPatch(JSON_PATCH_TASK_TEXT)
				.accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Task task = readResponse(response);
		
		assertThat(task, notNullValue());
		assertThat(task.getIdElement().getIdPart(), equalTo(TASK_UUID));
		assertThat(task, validResource());
		assertThat(task.getStatus(), is(Task.TaskStatus.REQUESTED));
	}
	
}
