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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.FhirConstants.AUT;
import static org.openmrs.module.fhir2.FhirConstants.AUTHOR;

import javax.servlet.ServletException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.openmrs.module.fhir2.web.servlet.BaseFhirResourceProviderTest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class TaskFhirResourceProviderWebTest extends BaseFhirResourceProviderTest<TaskFhirResourceProvider, Task> {
	
	private static final String TASK_UUID = "55616228-dc6d-446f-ab50-4ec711ea9243";
	
	private static final String WRONG_TASK_UUID = "df34a1c1-f57b-4c33-bee5-e601b56b9d5b";
	
	private static final String JSON_TASK_PATH = "org/openmrs/module/fhir2/providers/TestTask_CreateUpdate.json";
	
	private static final String JSON_TASK_NO_ID_PATH = "org/openmrs/module/fhir2/providers/TestTask_CreateUpdate_NoId.json";
	
	private static final String JSON_TASK_WRONG_ID_PATH = "org/openmrs/module/fhir2/providers/TestTask_CreateUpdate_WrongId.json";
	
	private Task task;
	
	@Mock
	private FhirTaskService service;
	
	@Getter(AccessLevel.PUBLIC)
	private TaskFhirResourceProvider resourceProvider;
	
	@Before
	public void setup() throws Exception {
		resourceProvider = new TaskFhirResourceProvider();
		resourceProvider.setService(service);
		
		super.setup();
		
		task = new Task();
		task.setId(TASK_UUID);
		when(service.getTask(TASK_UUID)).thenReturn(task);
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
		when(service.getTaskByUuid(WRONG_TASK_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/Task/" + WRONG_TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void getTaskHistoryByIdRequest_shouldVerifyGetTaskHistoryByIdUri() throws Exception {
		MockHttpServletResponse response = getTaskHistoryByIdRequest();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(BaseFhirResourceProviderTest.FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void getTaskHistoryByIdRequest_shouldGetTaskHistoryById() throws IOException, ServletException {
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.uniqueUuid()));
		provenance.setRecorded(new Date());
		provenance.setActivity(new CodeableConcept().addCoding(
		    new Coding().setCode("CREATE").setSystem(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION).setDisplay("create")));
		provenance.addAgent(new Provenance.ProvenanceAgentComponent()
		        .setType(new CodeableConcept().addCoding(new Coding().setCode(AUT).setDisplay(AUTHOR)
		                .setSystem(FhirConstants.FHIR_TERMINOLOGY_PROVENANCE_PARTICIPANT_TYPE)))
		        .addRole(new CodeableConcept().addCoding(
		            new Coding().setCode("").setDisplay("").setSystem(FhirConstants.FHIR_TERMINOLOGY_PARTICIPATION_TYPE))));
		
		task.addContained(provenance);
		
		MockHttpServletResponse response = getTaskHistoryByIdRequest();
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getResourceType().name(),
		    equalTo(Provenance.class.getSimpleName()));
	}
	
	@Test
	public void getTaskHistoryById_shouldReturnBundleWithEmptyEntriesIfPractitionerContainedIsEmpty() throws Exception {
		task.setContained(new ArrayList<>());
		
		MockHttpServletResponse response = getTaskHistoryByIdRequest();
		Bundle results = readBundleResponse(response);
		
		assertThat(results.hasEntry(), is(false));
	}
	
	@Test
	public void getTaskHistoryById_shouldReturn404IfPractitionerIdIsWrong() throws Exception {
		MockHttpServletResponse response = get("/Task/" + WRONG_TASK_UUID + "/_history")
		        .accept(BaseFhirResourceProviderTest.FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	private MockHttpServletResponse getTaskHistoryByIdRequest() throws IOException, ServletException {
		return get("/Task/" + TASK_UUID + "/_history").accept(BaseFhirResourceProviderTest.FhirMediaTypes.JSON).go();
	}
	
	@Test
	public void createTask_shouldCreateNewTask() throws Exception {
		String jsonTask;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_TASK_PATH)) {
			jsonTask = IOUtils.toString(is);
		}
		
		when(service.saveTask(any(Task.class))).thenReturn(task);
		
		MockHttpServletResponse response = post("/Task").jsonContent(jsonTask).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isCreated());
	}
	
	@Test
	public void updateTask_shouldUpdateExistingTask() throws Exception {
		String jsonTask;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_TASK_PATH)) {
			jsonTask = IOUtils.toString(is);
		}
		
		when(service.updateTask(anyString(), any(Task.class))).thenReturn(task);
		
		MockHttpServletResponse response = put("/Task/" + TASK_UUID).jsonContent(jsonTask).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
	}
	
	@Test
	public void updateTask_shouldErrorForIdMismatch() throws Exception {
		String jsonTask;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_TASK_WRONG_ID_PATH)) {
			jsonTask = IOUtils.toString(is);
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
			jsonTask = IOUtils.toString(is);
		}
		
		MockHttpServletResponse response = put("/Task/" + TASK_UUID).jsonContent(jsonTask).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	public void updateTask_shouldErrorForNonexistentTask() throws Exception {
		String jsonTask;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_TASK_WRONG_ID_PATH)) {
			jsonTask = IOUtils.toString(is);
		}
		
		when(service.updateTask(eq(WRONG_TASK_UUID), any(Task.class)))
		        .thenThrow(new MethodNotAllowedException("Can't find Task"));
		
		MockHttpServletResponse response = put("/Task/" + WRONG_TASK_UUID).jsonContent(jsonTask).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isMethodNotAllowed());
	}
}
