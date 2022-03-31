/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hl7.fhir.dstu3.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class TaskFhirResourceProviderWebTest extends BaseFhirR3ResourceProviderWebTest<TaskFhirResourceProvider, Task> {
	
	private static final String TASK_UUID = "55616228-dc6d-446f-ab50-4ec711ea9243";
	
	private static final String WRONG_TASK_UUID = "df34a1c1-f57b-4c33-bee5-e601b56b9d5b";
	
	private static final String JSON_TASK_PATH = "org/openmrs/module/fhir2/providers/Task_create.json";
	
	private static final String JSON_TASK_NO_ID_PATH = "org/openmrs/module/fhir2/providers/Task_create_no_id.json";
	
	private static final String JSON_TASK_WRONG_ID_PATH = "org/openmrs/module/fhir2/providers/Task_create_wrong_id.json";
	
	private static final String BASED_ON_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
	
	private static final String OWNER_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";
	
	private static final String LAST_UPDATED_DATE = "eq2020-09-03";
	
	private org.hl7.fhir.r4.model.Task task;
	
	@Mock
	private FhirTaskService service;
	
	@Getter(AccessLevel.PUBLIC)
	private TaskFhirResourceProvider resourceProvider;
	
	@Captor
	private ArgumentCaptor<ReferenceAndListParam> referenceAndListParamArgumentCaptor;
	
	@Captor
	private ArgumentCaptor<TokenAndListParam> tokenAndListParamArgumentCaptor;
	
	@Captor
	private ArgumentCaptor<DateRangeParam> dateRangeParamArgumentCaptor;
	
	@Before
	public void setup() throws ServletException {
		resourceProvider = new TaskFhirResourceProvider();
		resourceProvider.setFhirTaskService(service);
		
		super.setup();
		
		task = new org.hl7.fhir.r4.model.Task();
		task.setId(TASK_UUID);
		when(service.get(TASK_UUID)).thenReturn(task);
	}
	
	@Test
	public void getTaskByUuid_shouldReturnTask() throws Exception {
		MockHttpServletResponse response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(TASK_UUID));
	}
	
	@Test
	public void getTaskByWrongUuid_shouldReturn404() throws Exception {
		when(service.get(WRONG_TASK_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/Task/" + WRONG_TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void createTask_shouldCreateNewTask() throws Exception {
		String jsonTask;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_TASK_PATH)) {
			Objects.requireNonNull(is);
			jsonTask = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		when(service.create(any(org.hl7.fhir.r4.model.Task.class))).thenReturn(task);
		
		MockHttpServletResponse response = post("/Task").jsonContent(jsonTask).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isCreated());
	}
	
	@Test
	public void updateTask_shouldUpdateExistingTask() throws Exception {
		String jsonTask;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_TASK_PATH)) {
			Objects.requireNonNull(is);
			jsonTask = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		when(service.update(anyString(), any(org.hl7.fhir.r4.model.Task.class))).thenReturn(task);
		
		MockHttpServletResponse response = put("/Task/" + TASK_UUID).jsonContent(jsonTask).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
	}
	
	@Test
	public void updateTask_shouldErrorForIdMismatch() throws Exception {
		String jsonTask;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_TASK_WRONG_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonTask = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/Task/" + TASK_UUID).jsonContent(jsonTask).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    containsStringIgnoringCase("body must contain an ID element which matches the request URL"));
	}
	
	@Test
	public void updateTask_shouldErrorForNoId() throws Exception {
		String jsonTask;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_TASK_NO_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonTask = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/Task/" + TASK_UUID).jsonContent(jsonTask).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	public void updateTask_shouldErrorForNonexistentTask() throws Exception {
		String jsonTask;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_TASK_WRONG_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonTask = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		when(service.update(eq(WRONG_TASK_UUID), any(org.hl7.fhir.r4.model.Task.class)))
		        .thenThrow(new MethodNotAllowedException("Can't find Task"));
		
		MockHttpServletResponse response = put("/Task/" + WRONG_TASK_UUID).jsonContent(jsonTask).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isMethodNotAllowed());
	}
	
	@Test
	public void searchForTasks_shouldReturnBundleWithMatchingBasedOnResourceUUID() throws Exception {
		verifyURI(String.format("/Task?based-on:%s=%s", FhirConstants.SERVICE_REQUEST, BASED_ON_UUID));
		
		verify(service).searchForTasks(referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getResourceType(),
		    equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(null));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(BASED_ON_UUID));
	}
	
	@Test
	public void searchForTasks_shouldReturnBundleWithMatchingOwnerResourceUUID() throws Exception {
		verifyURI(String.format("/Task?owner:%s=%s", FhirConstants.PRACTITIONER, OWNER_UUID));
		
		verify(service).searchForTasks(isNull(), referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getResourceType(),
		    equalTo(FhirConstants.PRACTITIONER));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(null));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(OWNER_UUID));
	}
	
	@Test
	public void searchForTasks_shouldReturnBundleWithMatchingStatus() throws Exception {
		verifyURI(String.format("/Task?status=%s", Task.TaskStatus.ACCEPTED.toString()));
		
		verify(service).searchForTasks(isNull(), isNull(), tokenAndListParamArgumentCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull());
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(Task.TaskStatus.ACCEPTED.toString()));
	}
	
	@Test
	public void searchForTasks_shouldHandleComplexQuery() throws Exception {
		verifyURI(String.format("/Task?based-on:%s=%s&status=%s", FhirConstants.SERVICE_REQUEST, BASED_ON_UUID,
		    Task.TaskStatus.ACCEPTED.toString()));
		
		verify(service).searchForTasks(referenceAndListParamArgumentCaptor.capture(), isNull(),
		    tokenAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getResourceType(),
		    equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(null));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(BASED_ON_UUID));
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(Task.TaskStatus.ACCEPTED.toString()));
	}
	
	@Test
	public void searchForTasks_shouldReturnBundleWithMatchingUUID() throws Exception {
		verifyURI(String.format("/Task?_id=%s", TASK_UUID));
		
		verify(service).searchForTasks(isNull(), isNull(), isNull(), tokenAndListParamArgumentCaptor.capture(), isNull(),
		    isNull(), isNull());
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(TASK_UUID));
	}
	
	@Test
	public void searchForTasks_shouldReturnBundleWithMatchingLastUpdatedDate() throws Exception {
		verifyURI(String.format("/Task?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(service).searchForTasks(isNull(), isNull(), isNull(), isNull(), dateRangeParamArgumentCaptor.capture(),
		    isNull(), isNull());
		
		assertThat(dateRangeParamArgumentCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(dateRangeParamArgumentCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeParamArgumentCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	private void verifyURI(String uri) throws Exception {
		Task condition = new Task();
		condition.setId(TASK_UUID);
		when(service.searchForTasks(any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(condition), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void deleteTask_shouldDeleteTask() throws Exception {
		MockHttpServletResponse response = delete("/Task/" + TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void deleteTask_shouldReturn404ForNonExistingTask() throws Exception {
		doThrow(new ResourceNotFoundException("")).when(service).delete(WRONG_TASK_UUID);
		
		MockHttpServletResponse response = delete("/Task/" + WRONG_TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
}
