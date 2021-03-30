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

import static org.exparity.hamcrest.date.DateMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class TaskFhirResourceIntegrationTest extends BaseFhirR3IntegrationTest<TaskFhirResourceProvider, Task> {
	
	private static final String TASK_DATA_FILES = "org/openmrs/module/fhir2/api/dao/impl/FhirTaskDaoImplTest_initial_data.xml";
	
	private static final String TASK_UUID = "d899333c-5bd4-45cc-b1e7-2f9542dbcbf6";
	
	private static final String WRONG_TASK_UUID = "097c8573-b26d-4893-a3e3-ea5c21c3cc94";
	
	private static final String JSON_CREATE_TASK_DOCUMENT = "org/openmrs/module/fhir2/providers/Task_create.json";
	
	private static final String XML_CREATE_TASK_DOCUMENT = "org/openmrs/module/fhir2/providers/Task_create.xml";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private TaskFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		
		executeDataSet(TASK_DATA_FILES);
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
		
		assertThat(task, validResource());
	}
	
	@Test
	public void shouldReturnNotFoundWhenTaskNotFoundAsJson() throws Exception {
		MockHttpServletResponse response = get("/Task/" + WRONG_TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnExistingTaskAsXML() throws Exception {
		MockHttpServletResponse response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Task task = readResponse(response);
		
		assertThat(task, notNullValue());
		assertThat(task.getIdElement().getIdPart(), equalTo(TASK_UUID));
		assertThat(task.getStatus(), is(Task.TaskStatus.ACCEPTED));
		assertThat(task.getIntent(), is(Task.TaskIntent.ORDER));
		
		assertThat(task, validResource());
	}
	
	@Test
	public void shouldReturnNotFoundWhenTaskNotFoundAsXML() throws Exception {
		MockHttpServletResponse response = get("/Task/" + WRONG_TASK_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldCreateNewTaskAsJson() throws Exception {
		String jsonTask;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_TASK_DOCUMENT)) {
			assertThat(is, notNullValue());
			jsonTask = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = post("/Task").accept(FhirMediaTypes.JSON).jsonContent(jsonTask).go();
		
		assertThat(response, isCreated());
		assertThat(response.getHeader("Location"), containsString("/Task/"));
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Task task = readResponse(response);
		
		assertThat(task, notNullValue());
		assertThat(task.getIdElement().getIdPart(), notNullValue());
		assertThat(task.getStatus(), is(Task.TaskStatus.ACCEPTED));
		assertThat(task.getIntent(), is(Task.TaskIntent.ORDER));
		assertThat(task.getAuthoredOn(), within(1, ChronoUnit.MINUTES, new Date()));
		assertThat(task.getLastModified(), within(1, ChronoUnit.MINUTES, new Date()));
		assertThat(task, validResource());
		
		response = get("/Task/" + task.getIdElement().getIdPart()).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Task newTask = readResponse(response);
		
		assertThat(newTask, notNullValue());
		assertThat(newTask.getIdElement().getIdPart(), equalTo(task.getIdElement().getIdPart()));
		assertThat(newTask.getStatus(), equalTo(task.getStatus()));
		assertThat(newTask.getIntent(), equalTo(task.getIntent()));
		assertThat(task.getAuthoredOn(), equalTo(task.getAuthoredOn()));
		assertThat(task.getLastModified(), equalTo(task.getLastModified()));
	}
	
	@Test
	public void shouldCreateNewTaskAsXML() throws Exception {
		String xmlTask;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_CREATE_TASK_DOCUMENT)) {
			assertThat(is, notNullValue());
			xmlTask = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = post("/Task").accept(FhirMediaTypes.XML).xmlContext(xmlTask).go();
		
		assertThat(response, isCreated());
		assertThat(response.getHeader("Location"), containsString("/Task/"));
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Task task = readResponse(response);
		
		assertThat(task, notNullValue());
		assertThat(task.getIdElement().getIdPart(), notNullValue());
		assertThat(task.getStatus(), is(Task.TaskStatus.ACCEPTED));
		assertThat(task.getIntent(), is(Task.TaskIntent.ORDER));
		assertThat(task.getAuthoredOn(), within(1, ChronoUnit.MINUTES, new Date()));
		assertThat(task.getLastModified(), within(1, ChronoUnit.MINUTES, new Date()));
		assertThat(task, validResource());
		
		response = get("/Task/" + task.getIdElement().getIdPart()).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		Task newTask = readResponse(response);
		
		assertThat(newTask, notNullValue());
		assertThat(newTask.getIdElement().getIdPart(), equalTo(task.getIdElement().getIdPart()));
		assertThat(newTask.getStatus(), equalTo(task.getStatus()));
		assertThat(newTask.getIntent(), equalTo(task.getIntent()));
		assertThat(task.getAuthoredOn(), equalTo(task.getAuthoredOn()));
		assertThat(task.getLastModified(), equalTo(task.getLastModified()));
	}
	
	@Test
	public void shouldUpdateExistingTaskAsJson() throws Exception {
		MockHttpServletResponse response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Task task = readResponse(response);
		
		assertThat(task.getStatus(), is(Task.TaskStatus.ACCEPTED));
		
		task.setStatus(Task.TaskStatus.COMPLETED);
		
		response = put("/Task/" + TASK_UUID).jsonContent(toJson(task)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Task updatedTask = readResponse(response);
		
		assertThat(updatedTask, notNullValue());
		assertThat(updatedTask.getIdElement().getIdPart(), equalTo(TASK_UUID));
		assertThat(updatedTask.getStatus(), is(Task.TaskStatus.COMPLETED));
		assertThat(updatedTask, validResource());
		
		response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Task reReadTask = readResponse(response);
		
		assertThat(reReadTask.getStatus(), is(Task.TaskStatus.COMPLETED));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchTaskIdAsJson() throws Exception {
		MockHttpServletResponse response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Task task = readResponse(response);
		
		task.setId(WRONG_TASK_UUID);
		
		response = put("/Task/" + TASK_UUID).jsonContent(toJson(task)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentTaskAsJson() throws Exception {
		MockHttpServletResponse response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Task patient = readResponse(response);
		
		patient.setId(WRONG_TASK_UUID);
		
		response = put("/Task/" + WRONG_TASK_UUID).jsonContent(toJson(patient)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldUpdateExistingTaskAsXML() throws Exception {
		MockHttpServletResponse response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		Task task = readResponse(response);
		
		assertThat(task.getStatus(), is(Task.TaskStatus.ACCEPTED));
		
		task.setStatus(Task.TaskStatus.COMPLETED);
		
		response = put("/Task/" + TASK_UUID).xmlContext(toXML(task)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Task updatedTask = readResponse(response);
		
		assertThat(updatedTask, notNullValue());
		assertThat(updatedTask.getIdElement().getIdPart(), equalTo(TASK_UUID));
		assertThat(updatedTask.getStatus(), is(Task.TaskStatus.COMPLETED));
		assertThat(updatedTask, validResource());
		
		response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		Task reReadTask = readResponse(response);
		
		assertThat(reReadTask.getStatus(), is(Task.TaskStatus.COMPLETED));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchTaskIdAsXML() throws Exception {
		MockHttpServletResponse response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		Task task = readResponse(response);
		
		task.setId(WRONG_TASK_UUID);
		
		response = put("/Task/" + TASK_UUID).xmlContext(toXML(task)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentTaskAsXML() throws Exception {
		MockHttpServletResponse response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		Task patient = readResponse(response);
		
		patient.setId(WRONG_TASK_UUID);
		
		response = put("/Task/" + WRONG_TASK_UUID).xmlContext(toXML(patient)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldDeleteExistingTask() throws Exception {
		MockHttpServletResponse response = delete("/Task/" + TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, statusEquals(HttpStatus.GONE));
	}
	
	@Test
	public void shouldReturnNotFoundWhenDeletingNonExistentTask() throws Exception {
		MockHttpServletResponse response = delete("/Patient/" + WRONG_TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldSearchForAllTasksAsJson() throws Exception {
		MockHttpServletResponse response = get("/Task").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Task/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Task.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldSearchForFilteredAndSortedTasksAsJson() throws Exception {
		MockHttpServletResponse response = get("/Task?status=requested&_sort=_lastUpdated").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), is(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, hasSize(2));
		assertThat(entries, everyItem(hasResource(hasProperty("status", is(Task.TaskStatus.REQUESTED)))));
		assertThat(entries,
		    containsInRelativeOrder(
		        hasResource(hasProperty(
		            "meta",
		            hasProperty(
		                "lastUpdated",
		                equalTo(
		                    Date.from(LocalDateTime.of(2012, 5, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant()))))),
		        hasResource(hasProperty("meta", hasProperty("lastUpdated", equalTo(
		            Date.from(LocalDateTime.of(2012, 7, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant())))))));
	}
	
	@Test
	public void shouldSearchForAllTasksAsXML() throws Exception {
		MockHttpServletResponse response = get("/Task").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Task/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Task.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldSearchForFilteredAndSortedTasksAsXML() throws Exception {
		MockHttpServletResponse response = get("/Task?status=requested&_sort=_lastUpdated").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), is(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, hasSize(2));
		assertThat(entries, everyItem(hasResource(hasProperty("status", is(Task.TaskStatus.REQUESTED)))));
		assertThat(entries,
		    containsInRelativeOrder(
		        hasResource(hasProperty(
		            "meta",
		            hasProperty(
		                "lastUpdated",
		                equalTo(
		                    Date.from(LocalDateTime.of(2012, 5, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant()))))),
		        hasResource(hasProperty("meta", hasProperty("lastUpdated", equalTo(
		            Date.from(LocalDateTime.of(2012, 7, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant())))))));
	}
	
	@Test
	public void shouldReturnCountForTaskAsJson() throws Exception {
		MockHttpServletResponse response = get("/Task?status=requested&_summary=count").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(2)));
	}
	
	@Test
	public void shouldReturnCountForTaskAsXml() throws Exception {
		MockHttpServletResponse response = get("/Task?status=requested&_summary=count").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(2)));
	}
}
