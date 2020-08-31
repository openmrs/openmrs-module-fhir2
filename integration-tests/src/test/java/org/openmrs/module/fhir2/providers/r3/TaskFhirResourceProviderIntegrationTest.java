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
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.BaseFhirIntegrationTest.FhirMediaTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class TaskFhirResourceProviderIntegrationTest extends BaseFhirR3IntegrationTest<TaskFhirResourceProvider, Task> {
	
	private static final String[] TASK_SEARCH_DATA_FILES = {
	        "org/openmrs/module/fhir2/api/dao/impl/FhirTaskDaoImplTest_initial_data.xml",
	        "org/openmrs/module/fhir2/api/dao/impl/FhirTaskDaoImplTest_owner_data.xml" };
	
	private static final String TASK_UUID = "c0a3af35-c0a9-4c2e-9cc0-8e0440e357e5";
	
	private static final String JSON_CREATE_TASK_DOCUMENT = "org/openmrs/module/fhir2/providers/TestTask_CreateUpdate.json";
	
	private static final String XML_CREATE_TASK_DOCUMENT = "org/openmrs/module/fhir2/providers/TestTask_create.xml";
	
	private static final String WRONG_TASK_UUID = "df34a1c1-f57b-4c33-bee5-e501b56b9d5b";

	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	private TaskFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		
		for (String search_data : TASK_SEARCH_DATA_FILES) {
			executeDataSet(search_data);
		}
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
		assertThat(task, validResource());
		
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
		assertThat(task, validResource());
		
	}
	
	@Test
	public void shouldThrow404ForNonExistingTaskAsJson() throws Exception {
		MockHttpServletResponse response = get("/Task/" + WRONG_TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldThrow404ForNonExistingTaskAsXML() throws Exception {
		MockHttpServletResponse response = get("/Task/" + WRONG_TASK_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));

		OperationOutcome operationOutcome = readOperationOutcome(response);
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
 public void shouldReturnNotFoundWhenUpdatingNonExistentTaskAsJSON() throws Exception {
  // get the existing record
  MockHttpServletResponse response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.JSON).go();
  Task task = readResponse(response);
  
  // update the existing record
  task.setId(WRONG_TASK_UUID);
  
  // send the update to the server
  response = put("/Task/" + WRONG_TASK_UUID).jsonContent(toJson(task)).go();
  
  assertThat(response, isNotFound());
  assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
  assertThat(response.getContentAsString(), notNullValue());
  
  OperationOutcome operationOutcome = readOperationOutcome(response);
  
  assertThat(operationOutcome, notNullValue());
  assertThat(operationOutcome.hasIssue(), is(true));
	}
	
 @Test
 public void shouldReturnNotFoundWhenUpdatingNonExistentTaskAsXML() throws Exception {
  // get the existing record
  MockHttpServletResponse response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.XML).go();
  Task task = readResponse(response);
  
  // update the existing record
  task.setId(WRONG_TASK_UUID);
  
  // send the update to the server
  response = put("/Task/" + TASK_UUID).xmlContext(toXML(task)).go();
  
  assertThat(response, isNotFound());
  assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
  assertThat(response.getContentAsString(), notNullValue());
  
  OperationOutcome operationOutcome = readOperationOutcome(response);
  
  assertThat(operationOutcome, notNullValue());
  assertThat(operationOutcome.hasIssue(), is(true));
 }
 
 @Test
 public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchTaskIdAsXML() throws Exception {
  // get the existing record
  MockHttpServletResponse response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.XML).go();
  Task task = readResponse(response);
  
  // update the existing record
  task.setId(WRONG_TASK_UUID);
  
  // send the update to the server
  response = put("/Task/" + TASK_UUID).xmlContext(toXML(task)).go();
  assertThat(response, isBadRequest());
  assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
  assertThat(response.getContentAsString(), notNullValue());
  
  OperationOutcome operationOutcome = readOperationOutcome(response);
  assertThat(operationOutcome, notNullValue());
  assertThat(operationOutcome.hasIssue(), is(true));
  
 }
 
 @Test
 public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchTaskIdAsJSON() throws Exception {
  // get the existing record
  MockHttpServletResponse response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.JSON).go();
  Task task = readResponse(response);
  
  // update the existing record
  task.setId(WRONG_TASK_UUID);
  // send the update to the server
  response = put("/Task/" + WRONG_TASK_UUID).jsonContent(toJson(task)).go();
  assertThat(response, isBadRequest());
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
			jsonTask = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		// create task
		MockHttpServletResponse response = post("/Task").accept(FhirMediaTypes.JSON).jsonContent(jsonTask).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Task task = readResponse(response);
		assertThat(task, notNullValue());
		assertThat(task.getIdElement().getIdPart(), notNullValue());
		assertThat(task.getAuthoredOn(), equalTo("2016-10-31T08:25:05+10:00"));
		assertThat(task.getStatus(), equalTo("ACCEPTED"));
		assertThat(task.getIntent(), equalTo("order"));
		assertThat(task, validResource());
		
		assertThat(task.getOwner(), equalTo("owner Requested Task"));
		assertThat(task, validResource());
		
		// try to get new task
		response = get("/Task/" + task.getIdElement().getIdPart()).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Task newTask = readResponse(response);
		
		assertThat(newTask.getId(), equalTo(task.getId()));
		assertThat(newTask.getIdElement().getIdPart(), notNullValue());
	}
	
	@Test
	public void shouldCreateNewTaskAsXML() throws Exception {
		String xmlTask;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_CREATE_TASK_DOCUMENT)) {
			Objects.requireNonNull(is);
			xmlTask = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		// create test
		MockHttpServletResponse response = post("/Task").accept(FhirMediaTypes.XML).xmlContext(xmlTask).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Task task = readResponse(response);
		
		assertThat(task, notNullValue());
		assertThat(task.getIdElement().getIdPart(), notNullValue());
		assertThat(task.getAuthoredOn(), equalTo("2016-10-31T08:25:05+10:00"));
		assertThat(task.getStatus(), equalTo("ACCEPTED"));
		assertThat(task.getIntent(), equalTo("order"));
		assertThat(task, validResource());
		
		assertThat(task.getOwner(), equalTo("owner Requested Task"));
		assertThat(task, validResource());
		
		// try to get new task
		response = get("/Task/" + task.getIdElement().getIdPart()).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		Task newTask = readResponse(response);
		
		assertThat(newTask.getId(), equalTo(task.getId()));
		assertThat(newTask.getIdElement().getIdPart(), notNullValue());
		
	}
	
	@Test
	public void shouldUpdateExistingTaskAsJson() throws Exception {
		MockHttpServletResponse response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.JSON).go();
		Task task = readResponse(response);
		
		// update the existing record
		Date authoredOn = DateUtils.truncate(new Date(), Calendar.DATE);
		task.setAuthoredOn(authoredOn);
		
		// send the update to the server
		response = put("/Task/" + TASK_UUID).jsonContent(toJson(task)).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read the updated record
		Task updatedTask = readResponse(response);
		
		assertThat(updatedTask, notNullValue());
		assertThat(updatedTask.getIdElement().getIdPart(), equalTo(TASK_UUID));
		assertThat(updatedTask.getAuthoredOn(), equalTo(authoredOn));
		assertThat(task, validResource());
		
		// double-check the record returned via get
		response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.JSON).go();
		Task reReadTask = readResponse(response);
		assertThat(reReadTask.getAuthoredOn(), equalTo(authoredOn));
		
	}
	
	@Test
	public void shouldUpdateExistingTaskAsXml() throws Exception {
		MockHttpServletResponse response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.XML).go();
		Task task = readResponse(response);
		
		// update the existing record
		Date authoredOn = DateUtils.truncate(new Date(), Calendar.DATE);
		task.setAuthoredOn(authoredOn);
		
		// send the update to the server
		response = put("/Task/" + TASK_UUID).jsonContent(toJson(task)).xmlContext(toXML(task)).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read the updated record
		Task updatedTask = readResponse(response);
		
		assertThat(updatedTask, notNullValue());
		assertThat(updatedTask.getIdElement().getIdPart(), equalTo(TASK_UUID));
		assertThat(updatedTask.getAuthoredOn(), equalTo(authoredOn));
		assertThat(task, validResource());
		
		// double-check the record returned via get
		response = get("/Task/" + TASK_UUID).accept(FhirMediaTypes.XML).go();
		Task reReadTask = readResponse(response);
		
		assertThat(reReadTask.getAuthoredOn(), equalTo(authoredOn));
		assertThat(updatedTask.getStatus(), is(Task.TaskStatus.ACCEPTED));
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
		MockHttpServletResponse response = delete("/Task/" + WRONG_TASK_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldSearchForExistingTasksAsJson() throws Exception {
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
	public void shouldSearchForExistingTaskAsXml() throws Exception {
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
 public void shouldReturnSortedAndFilteredSearchResultsForTaskAsJson() throws Exception {
	   MockHttpServletResponse response = get("/Task/?status=accepted&_sort=_lastUpdated").accept(FhirMediaTypes.JSON).go();
	   
	   assertThat(response, isOk());
	   assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
	   assertThat(response.getContentAsString(), notNullValue());
	   
	   Bundle results = readBundleResponse(response);
	   
	   assertThat(results, notNullValue());
	   assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
	   assertThat(results.hasEntry(), is(true));
	   
	   List<Bundle.BundleEntryComponent> entries = results.getEntry();
	   
	   assertThat(entries, everyItem(hasResource(hasProperty("lastUpdated", hasProperty("status", startsWith("accepted"))))));
	   assertThat(entries,
	         containsInRelativeOrder(
	             hasResource(hasProperty("lastUpdated", hasProperty("givenAsSingleString", containsString("accepted")))),
	             hasResource(hasProperty("lastUpdated", hasProperty("givenAsSingleString", containsString("rejected"))))));
	     assertThat(entries, everyItem(hasResource(validResource())));
	}
	
 @Test
 public void shouldReturnSortedAndFilteredSearchResultsForTaskAsXML() throws Exception {
  MockHttpServletResponse response = get("/Task/?status=accepted&_sort=_lastUpdated").accept(FhirMediaTypes.XML).go();
    
    assertThat(response, isOk());
    assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
    assertThat(response.getContentAsString(), notNullValue());
    
    Bundle results = readBundleResponse(response);
    
    assertThat(results, notNullValue());
    assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
    assertThat(results.hasEntry(), is(true));
    
    List<Bundle.BundleEntryComponent> entries = results.getEntry();
    
    assertThat(entries, everyItem(hasResource(hasProperty("lastUpdated", hasProperty("status", startsWith("accepted"))))));
    assertThat(entries,
          containsInRelativeOrder(
              hasResource(hasProperty("lastUpdated", hasProperty("givenAsSingleString", containsString("accepted")))),
              hasResource(hasProperty("lastUpdated", hasProperty("givenAsSingleString", containsString("rejected"))))));
      assertThat(entries, everyItem(hasResource(validResource())));
 }
	
}
