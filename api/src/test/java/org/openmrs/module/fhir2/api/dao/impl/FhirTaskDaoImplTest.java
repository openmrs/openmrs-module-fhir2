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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.db.hibernate.HibernateConceptDAO;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.model.FhirReference;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhir2.model.FhirTaskInput;
import org.openmrs.module.fhir2.model.FhirTaskOutput;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirTaskDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String TASK_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirTaskDaoImplTest_initial_data.xml";
	
	private static final String CONCEPT_DATA_XML = "org/openmrs/api/include/ConceptServiceTest-initialConcepts.xml";
	
	private static final String TASK_UUID = "d899333c-5bd4-45cc-b1e7-2f9542dbcbf6";
	
	private static final String BASED_ON_ORDER_UUID = "7d96f25c-4949-4f72-9931-d808fbc226de";
	
	private static final String PARENT_TASK_UUID = "c0a3af38-c0a9-4c2e-9cc0-8e0440e357e5";
	
	private static final String TASK_CODE_CONCEPT_UUID = "efc232b8-e591-4e93-b135-1cf8b4e30b95";
	
	private static final FhirTask.TaskStatus NEW_STATUS = FhirTask.TaskStatus.ACCEPTED;
	
	private static final FhirTask.TaskIntent TASK_INTENT = FhirTask.TaskIntent.ORDER;
	
	private static final String USER_UUID = "6f3077c4-d4c5-46a7-8731-3cec6a0eb6bd";
	
	private static final String PATIENT_ID = "23ec6c43-416e-48fd-9464-18c67a164df6";
	
	private static final String ENCOUNTER_UUID = "d9d22ce2-36f9-4c39-865b-48a12cf08ba5";
	
	private static final String LOCATION_UUID = "58ab6cf9-ea12-43bc-98a6-40353423331e";
	
	private static final String CONCEPT_UUID = "957eba27-2b38-43e8-91a9-4dfe3956a32d";
	
	private static final String DIAGNOSTIC_REPORT_UUID = "584ebe74-eb7e-4dc2-ae0d-af941a163279";
	
	private FhirTaskDaoImpl dao;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private ConceptService conceptService;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirTaskDaoImpl();
		dao.setSessionFactory(sessionFactory);
		executeDataSet(TASK_DATA_XML);
	}
	
	@Test
	public void getTaskByUuid_shouldRetrieveTaskByUuid() {
		FhirTask result = dao.get(TASK_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(TASK_UUID));
	}
	
	@Test
	public void saveTask_shouldCreateNewTask() {
		FhirTask newTask = new FhirTask();
		newTask.setStatus(NEW_STATUS);
		newTask.setName(TASK_INTENT.toString());
		newTask.setIntent(TASK_INTENT);
		
		dao.createOrUpdate(newTask);
		
		FhirTask result = dao.get(TASK_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(NEW_STATUS));
	}
	
	@Test
	public void saveTask_shouldUpdateTaskStatus() {
		FhirTask toUpdate = dao.get(TASK_UUID);
		toUpdate.setStatus(NEW_STATUS);
		
		dao.createOrUpdate(toUpdate);
		
		assertThat(dao.get(TASK_UUID).getStatus(), equalTo(NEW_STATUS));
	}
	
	@Test
	public void saveTask_shouldUpdateOwnerReference() {
		FhirTask toUpdate = dao.get(TASK_UUID);
		
		FhirReference ownerReference = new FhirReference();
		ownerReference.setType(FhirConstants.PRACTITIONER);
		ownerReference.setReference(USER_UUID);
		ownerReference.setName("TEMP");
		
		toUpdate.setOwnerReference(ownerReference);
		
		dao.createOrUpdate(toUpdate);
		
		FhirTask result = dao.get(TASK_UUID);
		
		assertThat(result.getOwnerReference(), notNullValue());
		assertThat(result.getOwnerReference().getId(), notNullValue());
		assertThat(result.getOwnerReference().getType(), equalTo(FhirConstants.PRACTITIONER));
		assertThat(result.getOwnerReference().getReference(), equalTo(USER_UUID));
	}
	
	@Test
	public void saveTask_shouldUpdateForReference() {
		FhirTask toUpdate = dao.get(TASK_UUID);
		
		FhirReference forReference = new FhirReference();
		forReference.setType(FhirConstants.PATIENT);
		forReference.setReference(PATIENT_ID);
		forReference.setName("TEMP");
		
		toUpdate.setForReference(forReference);
		
		dao.createOrUpdate(toUpdate);
		
		FhirTask result = dao.get(TASK_UUID);
		
		assertThat(result.getForReference(), notNullValue());
		assertThat(result.getForReference().getId(), notNullValue());
		assertThat(result.getForReference().getType(), equalTo(FhirConstants.PATIENT));
		assertThat(result.getForReference().getReference(), equalTo(PATIENT_ID));
	}
	
	@Test
	public void saveTask_shouldUpdateEncounterReference() {
		FhirTask toUpdate = dao.get(TASK_UUID);
		
		FhirReference encounterReference = new FhirReference();
		encounterReference.setType(FhirConstants.ENCOUNTER);
		encounterReference.setReference(ENCOUNTER_UUID);
		encounterReference.setName("TEMP");
		
		toUpdate.setEncounterReference(encounterReference);
		
		dao.createOrUpdate(toUpdate);
		
		FhirTask result = dao.get(TASK_UUID);
		
		assertThat(result.getEncounterReference(), notNullValue());
		assertThat(result.getEncounterReference().getId(), notNullValue());
		assertThat(result.getEncounterReference().getType(), equalTo(FhirConstants.ENCOUNTER));
		assertThat(result.getEncounterReference().getReference(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void saveTask_shouldUpdateLocationReference() {
		FhirTask toUpdate = dao.get(TASK_UUID);
		
		FhirReference locationReference = new FhirReference();
		locationReference.setType(FhirConstants.LOCATION);
		locationReference.setReference(LOCATION_UUID);
		locationReference.setName("TEMP");
		
		toUpdate.setLocationReference(locationReference);
		
		dao.createOrUpdate(toUpdate);
		
		FhirTask result = dao.get(TASK_UUID);
		
		assertThat(result.getLocationReference(), notNullValue());
		assertThat(result.getLocationReference().getId(), notNullValue());
		assertThat(result.getLocationReference().getType(), equalTo(FhirConstants.LOCATION));
		assertThat(result.getLocationReference().getReference(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void saveTask_shouldUpdateBasedOnReferences() {
		FhirTask toUpdate = dao.get(TASK_UUID);
		
		FhirReference basedOnReference = new FhirReference();
		basedOnReference.setType(FhirConstants.SERVICE_REQUEST);
		basedOnReference.setReference(BASED_ON_ORDER_UUID);
		basedOnReference.setName("TEMP");
		
		toUpdate.setBasedOnReferences(Collections.singleton(basedOnReference));
		
		dao.createOrUpdate(toUpdate);
		
		FhirTask result = dao.get(TASK_UUID);
		
		assertThat(result.getBasedOnReferences(), notNullValue());
		assertThat(result.getBasedOnReferences().size(), greaterThan(0));
		assertThat(result.getBasedOnReferences(), hasItem(hasProperty("type", equalTo(FhirConstants.SERVICE_REQUEST))));
		assertThat(result.getBasedOnReferences(), hasItem(hasProperty("reference", equalTo(BASED_ON_ORDER_UUID))));
	}
	
	@Test
	public void saveTask_shouldUpdateTaskCode() {
		FhirTask toUpdate = dao.get(TASK_UUID);
		
		Concept taskCode = conceptService.getConceptByUuid(TASK_CODE_CONCEPT_UUID);
		
		toUpdate.setTaskCode(taskCode);
		
		dao.createOrUpdate(toUpdate);
		
		FhirTask result = dao.get(TASK_UUID);
		
		assertThat(result.getTaskCode(), notNullValue());
		assertThat(result.getTaskCode(), equalTo(taskCode));
	}
	
	@Test
	public void saveTask_shouldUpdatePartOfReferences() {
		FhirTask toUpdate = dao.get(TASK_UUID);
		
		FhirReference partOfReference = new FhirReference();
		partOfReference.setType(FhirConstants.TASK);
		partOfReference.setReference(PARENT_TASK_UUID);
		partOfReference.setName("TEMP");
		
		toUpdate.setPartOfReferences(Collections.singleton(partOfReference));
		
		dao.createOrUpdate(toUpdate);
		
		FhirTask result = dao.get(TASK_UUID);
		
		assertThat(result.getPartOfReferences(), notNullValue());
		assertThat(result.getPartOfReferences().size(), greaterThan(0));
		assertThat(result.getPartOfReferences(), hasItem(hasProperty("type", equalTo(FhirConstants.TASK))));
		assertThat(result.getPartOfReferences(), hasItem(hasProperty("reference", equalTo(PARENT_TASK_UUID))));
	}
	
	@Test
	public void saveTask_shouldUpdateExecutionStartTime() {
		FhirTask toUpdate = dao.get(TASK_UUID);
		
		LocalDateTime localDateTime = LocalDateTime.of(2024, Month.APRIL, 12, 10, 0);
		Date executionStartTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
		
		toUpdate.setExecutionStartTime(executionStartTime);
		
		dao.createOrUpdate(toUpdate);
		
		FhirTask result = dao.get(TASK_UUID);
		
		assertThat(result.getExecutionStartTime(), notNullValue());
		assertThat(result.getExecutionStartTime(), equalTo(executionStartTime));
	}
	
	@Test
	public void saveTask_shouldUpdateExecutionEndTime() {
		FhirTask toUpdate = dao.get(TASK_UUID);
		
		LocalDateTime localDateTime = LocalDateTime.of(2024, Month.APRIL, 12, 17, 0);
		Date executionEndTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
		
		toUpdate.setExecutionEndTime(executionEndTime);
		
		dao.createOrUpdate(toUpdate);
		
		FhirTask result = dao.get(TASK_UUID);
		
		assertThat(result.getExecutionEndTime(), notNullValue());
		assertThat(result.getExecutionEndTime(), equalTo(executionEndTime));
	}
	
	@Test
	public void saveTask_shouldUpdateComment() {
		FhirTask toUpdate = dao.get(TASK_UUID);
		
		String comment = "Test task comment";
		toUpdate.setComment(comment);
		
		dao.createOrUpdate(toUpdate);
		
		FhirTask result = dao.get(TASK_UUID);
		
		assertThat(result.getComment(), notNullValue());
		assertThat(result.getComment(), equalTo(comment));
	}
	
	@Test
	public void saveTask_shouldUpdateInput() throws Exception {
		executeDataSet(CONCEPT_DATA_XML);
		
		HibernateConceptDAO cd = new HibernateConceptDAO();
		cd.setSessionFactory(sessionFactory);
		
		FhirTask toUpdate = dao.get(TASK_UUID);
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
		
		FhirTask updatedInput = dao.createOrUpdate(toUpdate);
		
		assertThat(updatedInput.getInput(), notNullValue());
		assertThat(updatedInput.getInput(), not(empty()));
		
		assertThat(updatedInput.getInput(), hasItem(hasProperty("type", hasProperty("uuid", equalTo(CONCEPT_UUID)))));
		assertThat(updatedInput.getInput(), hasItem(hasProperty("valueNumeric", equalTo(someNumericVal))));
	}
	
	@Test
	
	public void saveTask_shouldUpdateOutput() throws Exception {
		executeDataSet(CONCEPT_DATA_XML);
		
		HibernateConceptDAO cd = new HibernateConceptDAO();
		cd.setSessionFactory(sessionFactory);
		
		FhirTask toUpdate = dao.get(TASK_UUID);
		
		assertThat(toUpdate, notNullValue());
		
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
		
		FhirTask updatedOutput = dao.createOrUpdate(toUpdate);
		
		assertThat(updatedOutput.getOutput(), notNullValue());
		assertThat(updatedOutput.getOutput(), not(empty()));
		
		assertThat(updatedOutput.getOutput(), hasItem(hasProperty("type", hasProperty("uuid", equalTo(CONCEPT_UUID)))));
		assertThat(updatedOutput.getOutput(),
		    hasItem(hasProperty("valueReference", hasProperty("reference", equalTo(DIAGNOSTIC_REPORT_UUID)))));
	}
	
	@Test
	public void searchForTasks_shouldHandleNullBasedOnRefs() {
		FhirTask toUpdate = dao.get(TASK_UUID);
		
		FhirReference nullTypeRef = new FhirReference();
		nullTypeRef.setType(null);
		nullTypeRef.setReference(BASED_ON_ORDER_UUID);
		
		toUpdate.setOwnerReference(nullTypeRef);
		toUpdate.setBasedOnReferences(Collections.singleton(nullTypeRef));
		
		dao.createOrUpdate(toUpdate);
		
		FhirTask result = dao.get(TASK_UUID);
		
		assertThat(result.getBasedOnReferences(), notNullValue());
		assertThat(result.getBasedOnReferences().size(), greaterThan(0));
	}
	
}
