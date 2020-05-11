/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import static org.exparity.hamcrest.date.DateMatchers.sameOrAfter;
import static org.exparity.hamcrest.date.DateMatchers.sameOrBefore;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import org.hibernate.SessionFactory;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.api.db.hibernate.HibernateConceptDAO;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.FhirReference;
import org.openmrs.module.fhir2.FhirTask;
import org.openmrs.module.fhir2.FhirTaskInput;
import org.openmrs.module.fhir2.FhirTaskOutput;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirTaskDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String TASK_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirTaskDaoImplTest_initial_data.xml";
	
	private static final String TASK_DATA_OWNER_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirTaskDaoImplTest_owner_data.xml";
	
	private static final String CONCEPT_DATA_XML = "org/openmrs/api/include/ConceptServiceTest-initialConcepts.xml";
	
	private static final String TASK_UUID = "d899333c-5bd4-45cc-b1e7-2f9542dbcbf6";
	
	private static final String BASED_ON_TASK_UUID = "3dc9f4a7-44dc-4b29-adfd-a8b297a41f33";
	
	private static final String BASED_ON_ORDER_UUID = "7d96f25c-4949-4f72-9931-d808fbc226de";
	
	private static final String OTHER_ORDER_UUID = "cbcb84f3-4576-452f-ba74-7cdeaa9aa602";
	
	private static final String OWNER_TASK_UUID = "c1a3af38-c0a9-4c2e-9cc0-8e0440e357e5";
	
	private static final String OWNER_USER_UUID = "7f8aec9d-8269-4bb4-8bc5-1820bb31092c";
	
	private static final FhirTask.TaskStatus TASK_STATUS = FhirTask.TaskStatus.REQUESTED;
	
	private static final FhirTask.TaskStatus NEW_STATUS = FhirTask.TaskStatus.ACCEPTED;
	
	private static final FhirTask.TaskIntent TASK_INTENT = FhirTask.TaskIntent.ORDER;
	
	private static final String USER_UUID = "6f3077c4-d4c5-46a7-8731-3cec6a0eb6bd";
	
	private static final String PATIENT_ID = "23ec6c43-416e-48fd-9464-18c67a164df6";
	
	private static final String ENCOUNTER_UUID = "d9d22ce2-36f9-4c39-865b-48a12cf08ba5";
	
	private static final String CONCEPT_UUID = "957eba27-2b38-43e8-91a9-4dfe3956a32d";
	
	private static final String DIAGNOSTIC_REPORT_UUID = "584ebe74-eb7e-4dc2-ae0d-af941a163279";
	
	private FhirTaskDaoImpl dao;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirTaskDaoImpl();
		dao.setSessionFactory(sessionFactory);
		executeDataSet(TASK_DATA_XML);
	}
	
	@Test
	public void getTaskByUuid_shouldRetrieveTaskByUuid() {
		FhirTask result = dao.getTaskByUuid(TASK_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(TASK_UUID));
	}
	
	@Test
	public void saveTask_shouldCreateNewTask() {
		FhirTask newTask = new FhirTask();
		newTask.setStatus(NEW_STATUS);
		newTask.setName(TASK_INTENT.toString());
		newTask.setIntent(TASK_INTENT);
		
		dao.saveTask(newTask);
		
		FhirTask result = dao.getTaskByUuid(TASK_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(NEW_STATUS));
	}
	
	@Test
	public void saveTask_shouldUpdateTaskStatus() {
		FhirTask toUpdate = dao.getTaskByUuid(TASK_UUID);
		toUpdate.setStatus(NEW_STATUS);
		
		dao.saveTask(toUpdate);
		
		assertThat(dao.getTaskByUuid(TASK_UUID).getStatus(), equalTo(NEW_STATUS));
	}
	
	@Test
	public void saveTask_shouldUpdateOwnerReference() {
		FhirTask toUpdate = dao.getTaskByUuid(TASK_UUID);
		
		FhirReference ownerReference = new FhirReference();
		ownerReference.setType(FhirConstants.PRACTITIONER);
		ownerReference.setReference(USER_UUID);
		ownerReference.setName("TEMP");
		
		toUpdate.setOwnerReference(ownerReference);
		
		dao.saveTask(toUpdate);
		
		FhirTask result = dao.getTaskByUuid(TASK_UUID);
		
		assertThat(result.getOwnerReference(), notNullValue());
		assertThat(result.getOwnerReference().getId(), notNullValue());
		assertThat(result.getOwnerReference().getType(), equalTo(FhirConstants.PRACTITIONER));
		assertThat(result.getOwnerReference().getReference(), equalTo(USER_UUID));
	}
	
	@Test
	public void saveTask_shouldUpdateForReference() {
		FhirTask toUpdate = dao.getTaskByUuid(TASK_UUID);
		
		FhirReference forReference = new FhirReference();
		forReference.setType(FhirConstants.PATIENT);
		forReference.setReference(PATIENT_ID);
		forReference.setName("TEMP");
		
		toUpdate.setForReference(forReference);
		
		dao.saveTask(toUpdate);
		
		FhirTask result = dao.getTaskByUuid(TASK_UUID);
		
		assertThat(result.getForReference(), notNullValue());
		assertThat(result.getForReference().getId(), notNullValue());
		assertThat(result.getForReference().getType(), equalTo(FhirConstants.PATIENT));
		assertThat(result.getForReference().getReference(), equalTo(PATIENT_ID));
	}
	
	@Test
	public void saveTask_shouldUpdateEncounterReference() {
		FhirTask toUpdate = dao.getTaskByUuid(TASK_UUID);
		
		FhirReference forReference = new FhirReference();
		forReference.setType(FhirConstants.ENCOUNTER);
		forReference.setReference(ENCOUNTER_UUID);
		forReference.setName("TEMP");
		
		toUpdate.setEncounterReference(forReference);
		
		dao.saveTask(toUpdate);
		
		FhirTask result = dao.getTaskByUuid(TASK_UUID);
		
		assertThat(result.getEncounterReference(), notNullValue());
		assertThat(result.getEncounterReference().getId(), notNullValue());
		assertThat(result.getEncounterReference().getType(), equalTo(FhirConstants.ENCOUNTER));
		assertThat(result.getEncounterReference().getReference(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void saveTask_shouldUpdateBasedOnReferences() {
		FhirTask toUpdate = dao.getTaskByUuid(TASK_UUID);
		
		FhirReference basedOnReference = new FhirReference();
		basedOnReference.setType(FhirConstants.SERVICE_REQUEST);
		basedOnReference.setReference(BASED_ON_ORDER_UUID);
		basedOnReference.setName("TEMP");
		
		toUpdate.setBasedOnReferences(Collections.singleton(basedOnReference));
		
		dao.saveTask(toUpdate);
		
		FhirTask result = dao.getTaskByUuid(TASK_UUID);
		
		assertThat(result.getBasedOnReferences(), notNullValue());
		assertThat(result.getBasedOnReferences().size(), greaterThan(0));
		assertThat(result.getBasedOnReferences(), hasItem(hasProperty("type", equalTo(FhirConstants.SERVICE_REQUEST))));
		assertThat(result.getBasedOnReferences(), hasItem(hasProperty("reference", equalTo(BASED_ON_ORDER_UUID))));
	}
	
	@Test
	public void saveTask_shouldUpdateInput() throws Exception {
		executeDataSet(CONCEPT_DATA_XML);
		
		HibernateConceptDAO cd = new HibernateConceptDAO();
		cd.setSessionFactory(sessionFactory);
		
		FhirTask toUpdate = dao.getTaskByUuid(TASK_UUID);
		Double someNumericVal = 123123.11;
		
		Concept type = cd.getConceptByUuid(CONCEPT_UUID);
		
		assertThat(type, notNullValue());
		
		FhirTaskInput input = new FhirTaskInput();
		input.setValueNumeric(someNumericVal);
		input.setType(type);
		input.setName("TEMP");
		
		// TODO: why is this not autogenerated?
		input.setId(23423);
		
		toUpdate.setInput(Collections.singleton(input));
		
		FhirTask result = dao.getTaskByUuid(TASK_UUID);
		
		assertThat(result.getInput(), notNullValue());
		assertThat(result.getInput(), not(empty()));
		
		assertThat(result.getInput(), hasItem(hasProperty("type", hasProperty("uuid", equalTo(CONCEPT_UUID)))));
		assertThat(result.getInput(), hasItem(hasProperty("valueNumeric", equalTo(someNumericVal))));
	}
	
	@Test
	
	public void saveTask_shouldUpdateOutput() throws Exception {
		executeDataSet(CONCEPT_DATA_XML);
		
		HibernateConceptDAO cd = new HibernateConceptDAO();
		cd.setSessionFactory(sessionFactory);
		
		FhirTask toUpdate = dao.getTaskByUuid(TASK_UUID);
		
		FhirReference outputReference = new FhirReference();
		outputReference.setType(FhirConstants.DIAGNOSTIC_REPORT);
		outputReference.setReference(DIAGNOSTIC_REPORT_UUID);
		outputReference.setName("TEMP");
		
		Concept type = cd.getConceptByUuid(CONCEPT_UUID);
		
		assertThat(type, notNullValue());
		
		FhirTaskOutput output = new FhirTaskOutput();
		output.setValueReference(outputReference);
		output.setType(type);
		output.setName("TEMP");
		
		// TODO: why is this not autogenerated?
		output.setId(23423);
		
		toUpdate.setOutput(Collections.singleton(output));
		
		FhirTask result = dao.getTaskByUuid(TASK_UUID);
		
		assertThat(result.getOutput(), notNullValue());
		assertThat(result.getOutput(), not(empty()));
		
		assertThat(result.getOutput(), hasItem(hasProperty("type", hasProperty("uuid", equalTo(CONCEPT_UUID)))));
		assertThat(result.getOutput(),
		    hasItem(hasProperty("valueReference", hasProperty("reference", equalTo(DIAGNOSTIC_REPORT_UUID)))));
	}
	
	@Test
	public void getTasksByBasedOnUuid_shouldRetrieveTasksByBasedOn() {
		Collection<FhirTask> results = dao.getTasksByBasedOnUuid(ServiceRequest.class, BASED_ON_ORDER_UUID);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(BASED_ON_TASK_UUID));
	}
	
	@Test
	public void getTasksByBasedOnUuid_shouldReturnEmptyTaskListForOrderWithNoTask() {
		Collection<FhirTask> results = dao.getTasksByBasedOnUuid(ServiceRequest.class, OTHER_ORDER_UUID);
		
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForTasks_shouldReturnTasksByBasedOn() {
		ReferenceParam basedOnReference = new ReferenceParam();
		basedOnReference.setValue(FhirConstants.SERVICE_REQUEST + "/" + BASED_ON_ORDER_UUID);
		
		Collection<FhirTask> results = dao.searchForTasks(basedOnReference, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(BASED_ON_TASK_UUID))));
		assertThat(results, not(hasItem(hasProperty("uuid", equalTo(TASK_UUID)))));
		
	}
	
	@Test
	public void searchForTasks_shouldReturnTasksByOwner() throws Exception {
		ReferenceParam ownerReference = new ReferenceParam();
		ownerReference.setValue(FhirConstants.PRACTITIONER + "/" + OWNER_USER_UUID);
		
		executeDataSet(TASK_DATA_OWNER_XML);
		
		Collection<FhirTask> results = dao.searchForTasks(null, ownerReference, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OWNER_TASK_UUID))));
		assertThat(results, not(hasItem(hasProperty("uuid", equalTo(TASK_UUID)))));
	}
	
	@Test
	public void searchForTasks_shouldReturnTasksByStatus() {
		TokenAndListParam status = new TokenAndListParam().addAnd(
		    new TokenOrListParam().add(FhirConstants.TASK_STATUS_VALUE_SET_URI, Task.TaskStatus.ACCEPTED.toString()));
		
		Collection<FhirTask> results = dao.searchForTasks(null, null, status, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(TASK_UUID))));
	}
	
	@Test
	public void searchForTasks_shouldSortTasksAsRequested() {
		SortSpec sort = new SortSpec();
		sort.setParamName("date");
		sort.setOrder(SortOrderEnum.ASC);
		
		Collection<FhirTask> results = dao.searchForTasks(null, null, null, sort);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThan(1));
		
		List<FhirTask> resultsList = new ArrayList<>(results);
		// pair-wise compare of all obs by date
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getDateChanged(), sameOrBefore(resultsList.get(i).getDateChanged()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		results = dao.searchForTasks(null, null, null, sort);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThan(1));
		
		resultsList = new ArrayList<>(results);
		// pair-wise compare of all obs by date
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getDateChanged(), sameOrAfter(resultsList.get(i).getDateChanged()));
		}
	}
	
	@Test
	public void searchForTasks_shouldIgnoreSearchByUnknownProperty() {
		SortSpec sort = new SortSpec();
		sort.setParamName("date");
		sort.setOrder(SortOrderEnum.DESC);
		
		Collection<FhirTask> baseline = dao.searchForTasks(null, null, null, sort);
		
		assertThat(baseline, notNullValue());
		assertThat(baseline, not(empty()));
		
		SortSpec subSort = new SortSpec();
		sort.setChain(subSort);
		subSort.setParamName("dummy");
		subSort.setOrder(SortOrderEnum.ASC);
		
		Collection<FhirTask> results = dao.searchForTasks(null, null, null, sort);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, equalTo(baseline));
	}
	
	@Test
	public void searchForTasks_shouldHandleAComplexQuery() throws Exception {
		executeDataSet(TASK_DATA_OWNER_XML);
		
		TokenAndListParam status = new TokenAndListParam();
		
		status.addAnd(
		    new TokenOrListParam().add(FhirConstants.TASK_STATUS_VALUE_SET_URI, Task.TaskStatus.ACCEPTED.toString())
		            .add(FhirConstants.TASK_STATUS_VALUE_SET_URI, Task.TaskStatus.REQUESTED.toString()));
		
		ReferenceParam ownerReference = new ReferenceParam();
		ownerReference.setValue(FhirConstants.PRACTITIONER + "/" + OWNER_USER_UUID);
		ownerReference.setChain("");
		
		SortSpec sort = new SortSpec();
		sort.setParamName("date");
		sort.setOrder(SortOrderEnum.DESC);
		
		// TODO: figure out fk integrity issue with setting owner_reference_id in the initial_data.xml file
		Collection<FhirTask> results = dao.searchForTasks(null, ownerReference, status, sort);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasSize(greaterThan(1)));
		
		assertThat(results, hasItem(hasProperty("status", equalTo(FhirTask.TaskStatus.ACCEPTED))));
		assertThat(results, hasItem(hasProperty("status", equalTo(FhirTask.TaskStatus.REQUESTED))));
		assertThat(results, not(hasItem(hasProperty("status", equalTo(FhirTask.TaskStatus.REJECTED)))));
		
		assertThat(results, not(hasItem(hasProperty("uuid", equalTo(TASK_UUID)))));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OWNER_TASK_UUID))));
		
		List<FhirTask> resultsList = new ArrayList<>(results);
		// pair-wise compare of all obs by date
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getDateChanged(), sameOrAfter(resultsList.get(i).getDateChanged()));
		}
	}
	
	@Test
	public void searchForTasks_shouldHandleNullBasedOnRefs() {
		FhirTask toUpdate = dao.getTaskByUuid(TASK_UUID);
		
		FhirReference nullTypeRef = new FhirReference();
		nullTypeRef.setType(null);
		nullTypeRef.setReference(BASED_ON_ORDER_UUID);
		nullTypeRef.setName();
		
		toUpdate.setOwnerReference(nullTypeRef);
		toUpdate.setBasedOnReferences(Collections.singleton(nullTypeRef));
		
		dao.saveTask(toUpdate);
		
		FhirTask result = dao.getTaskByUuid(TASK_UUID);
		
		assertThat(result.getBasedOnReferences(), notNullValue());
		assertThat(result.getBasedOnReferences().size(), greaterThan(0));
	}
}
