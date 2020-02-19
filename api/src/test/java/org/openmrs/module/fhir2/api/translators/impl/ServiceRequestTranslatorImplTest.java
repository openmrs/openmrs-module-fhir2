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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.TestOrder;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.impl.ObsServiceImpl;
import org.openmrs.api.impl.OrderServiceImpl;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.FhirTestConstants;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRequestTranslatorImplTest {
	
	private static final String SERVICE_REQUEST_UUID = "4e4851c3-c265-400e-acc9-1f1b0ac7f9c4";
	
	private static final TestOrder.Action ORDER_ACTION = TestOrder.Action.NEW;
	
	private static final String LOINC_CODE = "1000-1";

	private static final String PATIENT_UUID = "14d4f066-15f5-102d-96e4-000c29c2a5d7";

	private static final String PRACTITIONER_UUID = "b156e76e-b87a-4458-964c-a48e64a20fbb";

	private static final String ORGANIZATION_UUID = "44f7a79e-1de6-4b0b-9daf-bbcb7ed18b7e";

	private ServiceRequestTranslatorImpl translator;
	
	@Mock
	private FhirTaskService taskService;
	
	@Mock
	private ConceptTranslator conceptTranslator;

	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;

	@Mock
	private PractitionerReferenceTranslator practitionerReferenceTranslator;
	
	@Before
	public void setup() {
		translator = new ServiceRequestTranslatorImpl();
		translator.setConceptTranslator(conceptTranslator);
		translator.setTaskService(taskService);
		translator.setPatientReferenceTranslator(patientReferenceTranslator);
		translator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenmrsTestOrderToFhirServiceRequest() {
		TestOrder order = new TestOrder();
		ServiceRequest result = translator.toFhirResource(order);
		
		assertThat(result, notNullValue());
		assertThat(result.getIntent(), equalTo(ServiceRequest.ServiceRequestIntent.ORDER));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOrderFromRequestedTaskToActiveServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setUuid(SERVICE_REQUEST_UUID);
		
		Collection<Task> tasks = setUpBasedOnScenario(Task.TaskStatus.REQUESTED);
		
		when(taskService.getTasksByBasedOn(ServiceRequest.class, SERVICE_REQUEST_UUID)).thenReturn(tasks);
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.ACTIVE));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOrderFromRejectedTaskToRevokedServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setUuid(SERVICE_REQUEST_UUID);
		
		Collection<Task> tasks = setUpBasedOnScenario(Task.TaskStatus.REJECTED);
		
		when(taskService.getTasksByBasedOn(ServiceRequest.class, SERVICE_REQUEST_UUID)).thenReturn(tasks);
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.REVOKED));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOrderFromAcceptedTaskToActiveServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setUuid(SERVICE_REQUEST_UUID);
		
		Collection<Task> tasks = setUpBasedOnScenario(Task.TaskStatus.ACCEPTED);
		
		when(taskService.getTasksByBasedOn(ServiceRequest.class, SERVICE_REQUEST_UUID)).thenReturn(tasks);
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.ACTIVE));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOrderFromCompletedTaskToCompletedServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setUuid(SERVICE_REQUEST_UUID);
		
		Collection<Task> tasks = setUpBasedOnScenario(Task.TaskStatus.COMPLETED);
		
		when(taskService.getTasksByBasedOn(ServiceRequest.class, SERVICE_REQUEST_UUID)).thenReturn(tasks);
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.COMPLETED));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOrderFromOtherTaskToUnknownServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setUuid(SERVICE_REQUEST_UUID);
		
		Collection<Task> tasks = setUpBasedOnScenario(Task.TaskStatus.DRAFT);
		
		when(taskService.getTasksByBasedOn(ServiceRequest.class, SERVICE_REQUEST_UUID)).thenReturn(tasks);
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.UNKNOWN));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOrderWithoutTaskToUnknownServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setUuid(SERVICE_REQUEST_UUID);
		
		when(taskService.getTasksByBasedOn(ServiceRequest.class, SERVICE_REQUEST_UUID))
		        .thenReturn(Collections.<Task> emptyList());
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.UNKNOWN));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOrderWithMultipleTasksToUnknownServiceRequest() {
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
	public void toFhirResource_shouldTranslateCode() {
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

	@Test
	public void toFhirResource_shouldTranslateOccurrence() {
		TestOrder testOrder = new TestOrder();
		Date fromDate = new Date();
		Date toDate = new Date();

		testOrder.setDateActivated(fromDate);
		testOrder.setAutoExpireDate(toDate);

		Period result = translator.toFhirResource(testOrder).getOccurrencePeriod();

		assertThat(result, notNullValue());
		assertThat(result.getStart(), equalTo(fromDate));
		assertThat(result.getEnd(), equalTo(toDate));
	}

	@Test
	public void toFhirResource_shouldTranslateOccurrenceFromScheduled() {
		TestOrder testOrder = new TestOrder();
		Date fromDate = new Date();
		Date toDate = new Date();

		testOrder.setUrgency(TestOrder.Urgency.ON_SCHEDULED_DATE);
		testOrder.setScheduledDate(fromDate);
		testOrder.setAutoExpireDate(toDate);

		Period result = translator.toFhirResource(testOrder).getOccurrencePeriod();

		assertThat(result, notNullValue());
		assertThat(result.getStart(), equalTo(fromDate));
		assertThat(result.getEnd(), equalTo(toDate));
	}

	@Test
	public void toFhirResource_shouldTranslateSubject() {
		TestOrder order = new TestOrder();
		Patient subject = new Patient();
		Reference subjectReference = new Reference();
		
		subject.setUuid(PATIENT_UUID);
		order.setUuid(SERVICE_REQUEST_UUID);
		order.setPatient(subject);
		subjectReference.setType(FhirConstants.PATIENT).setReference(FhirConstants.PATIENT+"/"+PATIENT_UUID);

		when(patientReferenceTranslator.toFhirResource(subject)).thenReturn(subjectReference);

		Reference result = translator.toFhirResource(order).getSubject();

		assertThat(result, notNullValue());
		assertThat(result.getReference(), containsString(PATIENT_UUID));
	}

	@Test
	public void toFhirResource_shouldInheritPerformerFromTaskOwner() {
		TestOrder order = new TestOrder();
		order.setUuid(SERVICE_REQUEST_UUID);

		when(taskService.getTasksByBasedOn(ServiceRequest.class, SERVICE_REQUEST_UUID)).thenReturn(setUpPerformerScenario(ORGANIZATION_UUID));

		Collection<Reference> result = translator.toFhirResource(order).getPerformer();

		assertThat(result, notNullValue());
		assertThat(result, hasSize(1));
		assertThat(result.iterator().next().getReference(), containsString(ORGANIZATION_UUID));
	}

	@Test
	public void toFhirResource_shouldTranslateRequester() {

		TestOrder order = new TestOrder();
		Provider requester = new Provider();
		Reference requesterReference = new Reference();

		requester.setUuid(PRACTITIONER_UUID);
		order.setUuid(SERVICE_REQUEST_UUID);
		order.setOrderer(requester);
		requesterReference.setType(FhirConstants.PRACTITIONER).setReference(FhirConstants.PRACTITIONER+"/"+PRACTITIONER_UUID);

		when(practitionerReferenceTranslator.toFhirResource(requester)).thenReturn(requesterReference);

		Reference result = translator.toFhirResource(order).getRequester();

		assertThat(result, notNullValue());
		assertThat(result.getReference(), containsString(PRACTITIONER_UUID));
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

	private Collection<Task> setUpPerformerScenario(String performerUUID) {
		Reference performerRef = new Reference();
		Task task = new Task();

		performerRef.setReference(FhirConstants.ORGANIZATION + "/" + performerUUID);
		performerRef.setType(FhirConstants.ORGANIZATION);

		task.setOwner(performerRef);

		return Collections.singletonList(task);
	}


}
