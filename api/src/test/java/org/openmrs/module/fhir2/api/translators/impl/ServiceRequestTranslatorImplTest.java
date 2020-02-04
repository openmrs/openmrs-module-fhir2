/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.TestOrder;
import org.openmrs.module.fhir2.FhirTestConstants;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRequestTranslatorImplTest {
	
	private static final String SERVICE_REQUEST_UUID = "4e4851c3-c265-400e-acc9-1f1b0ac7f9c4";
	
	private static final TestOrder.Action ORDER_ACTION = TestOrder.Action.NEW;
	
	private static final String LOINC_CODE = "1000-1";
	
	private ServiceRequestTranslatorImpl translator;
	
	@Mock
	private FhirTaskService taskService;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Before
	public void setup() {
		translator = new ServiceRequestTranslatorImpl();
		translator.setConceptTranslator(conceptTranslator);
		translator.setTaskService(taskService);
	}
	
	@Test
	public void shouldTranslateOpenmrsTestOrderToFhirServiceRequest() {
		TestOrder order = new TestOrder();
		ServiceRequest result = translator.toFhirResource(order);
		
		assertThat(result, notNullValue());
		assertThat(result.getIntent(), equalTo(ServiceRequest.ServiceRequestIntent.ORDER));
	}
	
	@Test
	public void shouldTranslateOrderFromRequestedTaskToActiveServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setUuid(SERVICE_REQUEST_UUID);
		
		Collection<Task> tasks = setUpBasedOnScenario(Task.TaskStatus.REQUESTED);
		
		when(taskService.getTasksByBasedOn(ServiceRequest.class, SERVICE_REQUEST_UUID)).thenReturn(tasks);
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.ACTIVE));
	}
	
	@Test
	public void shouldTranslateOrderFromRejectedTaskToRevokedServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setUuid(SERVICE_REQUEST_UUID);
		
		Collection<Task> tasks = setUpBasedOnScenario(Task.TaskStatus.REJECTED);
		
		when(taskService.getTasksByBasedOn(ServiceRequest.class, SERVICE_REQUEST_UUID)).thenReturn(tasks);
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.REVOKED));
	}
	
	@Test
	public void shouldTranslateOrderFromAcceptedTaskToActiveServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setUuid(SERVICE_REQUEST_UUID);
		
		Collection<Task> tasks = setUpBasedOnScenario(Task.TaskStatus.ACCEPTED);
		
		when(taskService.getTasksByBasedOn(ServiceRequest.class, SERVICE_REQUEST_UUID)).thenReturn(tasks);
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.ACTIVE));
	}
	
	@Test
	public void shouldTranslateOrderFromCompletedTaskToCompletedServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setUuid(SERVICE_REQUEST_UUID);
		
		Collection<Task> tasks = setUpBasedOnScenario(Task.TaskStatus.COMPLETED);
		
		when(taskService.getTasksByBasedOn(ServiceRequest.class, SERVICE_REQUEST_UUID)).thenReturn(tasks);
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.COMPLETED));
	}
	
	@Test
	public void shouldTranslateOrderFromOtherTaskToUnknownServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setUuid(SERVICE_REQUEST_UUID);
		
		Collection<Task> tasks = setUpBasedOnScenario(Task.TaskStatus.DRAFT);
		
		when(taskService.getTasksByBasedOn(ServiceRequest.class, SERVICE_REQUEST_UUID)).thenReturn(tasks);
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.UNKNOWN));
	}
	
	@Test
	public void shouldTranslateOrderWithoutTaskToUnknownServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setUuid(SERVICE_REQUEST_UUID);
		
		when(taskService.getTasksByBasedOn(ServiceRequest.class, SERVICE_REQUEST_UUID))
		        .thenReturn(Collections.<Task> emptyList());
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.UNKNOWN));
	}
	
	@Test
	public void shouldTranslateOrderWithMultipleTasksToUnknownServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setUuid(SERVICE_REQUEST_UUID);
		
		Task firstTask = new Task();
		Task secondTask = new Task();
		
		Reference basedOnRef = new Reference();
		basedOnRef.setReference("ServiceRequest/" + SERVICE_REQUEST_UUID);
		basedOnRef.setType("ServiceRequest");
		
		firstTask.addBasedOn(basedOnRef);
		secondTask.addBasedOn(basedOnRef);
		
		Collection<Task> tasks = Arrays.asList(firstTask, secondTask);
		
		when(taskService.getTasksByBasedOn(ServiceRequest.class, SERVICE_REQUEST_UUID)).thenReturn(tasks);
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.UNKNOWN));
	}
	
	@Test
	public void shouldTranslateOrderConcept() {
		Concept openmrsConcept = new Concept();
		TestOrder testOrder = new TestOrder();
		
		testOrder.setConcept(openmrsConcept);
		
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding loincCoding = codeableConcept.addCoding();
		loincCoding.setSystem(FhirTestConstants.LOINC_SYSTEM_URL);
		loincCoding.setCode(LOINC_CODE);
		
		when(conceptTranslator.toFhirResource(openmrsConcept)).thenReturn(codeableConcept);
		
		CodeableConcept result = translator.toFhirResource(testOrder).getCode();
		
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), notNullValue());
		assertThat(result.getCoding(), hasItem(hasProperty("system", equalTo(FhirTestConstants.LOINC_SYSTEM_URL))));
		assertThat(result.getCoding(), hasItem(hasProperty("code", equalTo(LOINC_CODE))));
	}
	
	private Collection<Task> setUpBasedOnScenario(Task.TaskStatus status) {
		Reference basedOnRef = new Reference();
		Task task = new Task();
		task.setStatus(status);
		basedOnRef.setReference("ServiceRequest/" + SERVICE_REQUEST_UUID);
		basedOnRef.setType("ServiceRequest");
		task.addBasedOn(basedOnRef);
		
		return Collections.singletonList(task);
	}
}
