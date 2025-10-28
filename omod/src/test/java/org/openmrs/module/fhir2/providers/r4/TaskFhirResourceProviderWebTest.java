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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import javax.servlet.ServletException;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang.time.DateUtils;
import org.hamcrest.MatcherAssert;
import org.hl7.fhir.r4.model.Task;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.openmrs.module.fhir2.api.search.param.TaskSearchParams;
import org.powermock.reflect.Whitebox;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class TaskFhirResourceProviderWebTest extends BaseFhirR4ResourceProviderWebTest<TaskFhirResourceProvider, Task> {
	
	private static final String TASK_UUID = "55616228-dc6d-446f-ab50-4ec711ea9243";
	
	private static final String WRONG_TASK_UUID = "df34a1c1-f57b-4c33-bee5-e601b56b9d5b";
	
	private static final String JSON_TASK_PATH = "org/openmrs/module/fhir2/providers/Task_create.json";
	
	private static final String JSON_TASK_NO_ID_PATH = "org/openmrs/module/fhir2/providers/Task_create_no_id.json";
	
	private static final String JSON_TASK_WRONG_ID_PATH = "org/openmrs/module/fhir2/providers/Task_create_wrong_id.json";
	
	private static final String BASED_ON_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
	
	private static final String OWNER_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";
	
	private static final String LAST_UPDATED_DATE = "eq2020-09-03";
	
	private Task task;
	
	@Mock
	private FhirTaskService service;
	
	@Mock
	private FhirGlobalPropertyService fhirGpService;
	
	@Getter(AccessLevel.PUBLIC)
	private TaskFhirResourceProvider resourceProvider;
	
	@Captor
	private ArgumentCaptor<TaskSearchParams> taskSearchParamsArgumentCaptor;
	
	@Before
	public void setup() throws ServletException {
		resourceProvider = new TaskFhirResourceProvider();
		resourceProvider.setService(service);
		Whitebox.setInternalState(BaseUpsertFhirResourceProvider.class, "globalPropsService", fhirGpService);
		
		super.setup();
		
		task = new Task();
		task.setId(TASK_UUID);
		when(service.get(TASK_UUID)).thenReturn(task);
	}
	
	@After
	public void tearDown() {
		Whitebox.setInternalState(BaseUpsertFhirResourceProvider.class, "globalPropsService", (Object) null);
	}
	
	@Test
	public void getTaskByUuid_shouldReturnTask() throws Exception {
		MockHttpServletResponse response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isOk());
		MatcherAssert.assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		MatcherAssert.assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(TASK_UUID));
	}
	
	@Test
	public void getTaskByWrongUuid_shouldReturn404() throws Exception {
		when(service.get(WRONG_TASK_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/Task/" + WRONG_TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isNotFound());
	}
	
	@Test
	public void createTask_shouldCreateNewTask() throws Exception {
		String jsonTask;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_TASK_PATH)) {
			Objects.requireNonNull(is);
			jsonTask = inputStreamToString(is, UTF_8);
		}
		
		when(service.create(any(Task.class))).thenReturn(task);
		
		MockHttpServletResponse response = post("/Task").jsonContent(jsonTask).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isCreated());
	}
	
	@Test
	public void updateTask_shouldUpdateExistingTask() throws Exception {
		String jsonTask;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_TASK_PATH)) {
			Objects.requireNonNull(is);
			jsonTask = inputStreamToString(is, UTF_8);
		}
		
		lenient().when(service.update(anyString(), any(Task.class))).thenReturn(task);
		
		MockHttpServletResponse response = put("/Task/" + TASK_UUID).jsonContent(jsonTask).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isOk());
	}
	
	@Test
	public void updateTask_shouldErrorForIdMismatch() throws Exception {
		String jsonTask;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_TASK_WRONG_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonTask = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = put("/Task/" + TASK_UUID).jsonContent(jsonTask).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    containsStringIgnoringCase("body must contain an ID element which matches the request URL"));
	}
	
	@Test
	public void updateTask_shouldErrorForNoId() throws Exception {
		String jsonTask;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_TASK_NO_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonTask = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = put("/Task/" + TASK_UUID).jsonContent(jsonTask).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	public void updateTask_shouldErrorForNonexistentTask() throws Exception {
		String jsonTask;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_TASK_WRONG_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonTask = inputStreamToString(is, UTF_8);
		}
		
		lenient().when(service.update(eq(WRONG_TASK_UUID), any(Task.class), any(RequestDetails.class), eq(false)))
		        .thenThrow(new MethodNotAllowedException("Can't find Task"));
		
		MockHttpServletResponse response = put("/Task/" + WRONG_TASK_UUID).jsonContent(jsonTask).accept(FhirMediaTypes.JSON)
		        .go();
		
		MatcherAssert.assertThat(response, isMethodNotAllowed());
	}
	
	@Test
	public void searchForTasks_shouldReturnBundleWithMatchingBasedOnResourceUUID() throws Exception {
		verifyURI(String.format("/Task?based-on:%s=%s", FhirConstants.SERVICE_REQUEST, BASED_ON_UUID));
		
		verify(service).searchForTasks(taskSearchParamsArgumentCaptor.capture());
		ReferenceAndListParam resource = taskSearchParamsArgumentCaptor.getValue().getBasedOnReference();
		
		assertThat(resource, notNullValue());
		assertThat(resource.getValuesAsQueryTokens(), not(empty()));
		assertThat(resource.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getResourceType(),
		    equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(resource.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getChain(), equalTo(null));
		assertThat(resource.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(BASED_ON_UUID));
	}
	
	@Test
	public void searchForTasks_shouldReturnBundleWithMatchingOwnerResourceUUID() throws Exception {
		verifyURI(String.format("/Task?owner:%s=%s", FhirConstants.PRACTITIONER, OWNER_UUID));
		
		verify(service).searchForTasks(taskSearchParamsArgumentCaptor.capture());
		ReferenceAndListParam owner = taskSearchParamsArgumentCaptor.getValue().getOwnerReference();
		
		assertThat(owner, notNullValue());
		assertThat(owner.getValuesAsQueryTokens(), not(empty()));
		assertThat(owner.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getResourceType(),
		    equalTo(FhirConstants.PRACTITIONER));
		assertThat(owner.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getChain(), equalTo(null));
		assertThat(owner.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(), equalTo(OWNER_UUID));
	}
	
	@Test
	public void searchForTasks_shouldReturnBundleWithMatchingStatus() throws Exception {
		verifyURI(String.format("/Task?status=%s", Task.TaskStatus.ACCEPTED.toString()));
		
		verify(service).searchForTasks(taskSearchParamsArgumentCaptor.capture());
		TokenAndListParam status = taskSearchParamsArgumentCaptor.getValue().getStatus();
		
		assertThat(status, notNullValue());
		assertThat(status.getValuesAsQueryTokens(), not(empty()));
		assertThat(status.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(Task.TaskStatus.ACCEPTED.toString()));
	}
	
	@Test
	public void searchForTasks_shouldHandleComplexQuery() throws Exception {
		verifyURI(String.format("/Task?based-on:%s=%s&status=%s", FhirConstants.SERVICE_REQUEST, BASED_ON_UUID,
		    Task.TaskStatus.ACCEPTED.toString()));
		
		verify(service).searchForTasks(taskSearchParamsArgumentCaptor.capture());
		
		TokenAndListParam status = taskSearchParamsArgumentCaptor.getValue().getStatus();
		ReferenceAndListParam serviceRequest = taskSearchParamsArgumentCaptor.getValue().getBasedOnReference();
		
		assertThat(serviceRequest, notNullValue());
		assertThat(serviceRequest.getValuesAsQueryTokens(), not(empty()));
		assertThat(serviceRequest.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getResourceType(),
		    equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(serviceRequest.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getChain(), equalTo(null));
		assertThat(serviceRequest.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(BASED_ON_UUID));
		
		assertThat(status, notNullValue());
		assertThat(status.getValuesAsQueryTokens(), not(empty()));
		assertThat(status.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(Task.TaskStatus.ACCEPTED.toString()));
	}
	
	@Test
	public void searchForTasks_shouldReturnBundleWithMatchingUUID() throws Exception {
		verifyURI(String.format("/Task?_id=%s", TASK_UUID));
		
		verify(service).searchForTasks(taskSearchParamsArgumentCaptor.capture());
		TokenAndListParam taskUuid = taskSearchParamsArgumentCaptor.getValue().getId();
		
		assertThat(taskUuid, notNullValue());
		assertThat(taskUuid.getValuesAsQueryTokens(), not(empty()));
		assertThat(taskUuid.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(), equalTo(TASK_UUID));
	}
	
	@Test
	public void searchForTasks_shouldReturnBundleWithMatchingLastUpdatedDate() throws Exception {
		verifyURI(String.format("/Task?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(service).searchForTasks(taskSearchParamsArgumentCaptor.capture());
		DateRangeParam lastUpdatedDate = taskSearchParamsArgumentCaptor.getValue().getLastUpdated();
		
		assertThat(lastUpdatedDate, notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(lastUpdatedDate.getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(lastUpdatedDate.getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	private void verifyURI(String uri) throws Exception {
		Task condition = new Task();
		condition.setId(TASK_UUID);
		when(service.searchForTasks(any()))
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
