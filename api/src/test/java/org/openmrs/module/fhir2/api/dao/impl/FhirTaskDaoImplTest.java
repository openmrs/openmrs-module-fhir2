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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Collections;

import org.hibernate.SessionFactory;
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
	
	private static final String CONCEPT_DATA_XML = "org/openmrs/api/include/ConceptServiceTest-initialConcepts.xml";
	
	private static final String TASK_UUID = "d899333c-5bd4-45cc-b1e7-2f9542dbcbf6";
	
	private static final String BASED_ON_ORDER_UUID = "7d96f25c-4949-4f72-9931-d808fbc226de";
	
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
		
		FhirReference forReference = new FhirReference();
		forReference.setType(FhirConstants.ENCOUNTER);
		forReference.setReference(ENCOUNTER_UUID);
		forReference.setName("TEMP");
		
		toUpdate.setEncounterReference(forReference);
		
		dao.createOrUpdate(toUpdate);
		
		FhirTask result = dao.get(TASK_UUID);
		
		assertThat(result.getEncounterReference(), notNullValue());
		assertThat(result.getEncounterReference().getId(), notNullValue());
		assertThat(result.getEncounterReference().getType(), equalTo(FhirConstants.ENCOUNTER));
		assertThat(result.getEncounterReference().getReference(), equalTo(ENCOUNTER_UUID));
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
		
		FhirTask result = dao.get(TASK_UUID);
		
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
		
		FhirTask toUpdate = dao.get(TASK_UUID);
		
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
		
		FhirTask result = dao.get(TASK_UUID);
		
		assertThat(result.getOutput(), notNullValue());
		assertThat(result.getOutput(), not(empty()));
		
		assertThat(result.getOutput(), hasItem(hasProperty("type", hasProperty("uuid", equalTo(CONCEPT_UUID)))));
		assertThat(result.getOutput(),
		    hasItem(hasProperty("valueReference", hasProperty("reference", equalTo(DIAGNOSTIC_REPORT_UUID)))));
	}
	
	@Test
	public void searchForTasks_shouldHandleNullBasedOnRefs() {
		FhirTask toUpdate = dao.get(TASK_UUID);
		
		FhirReference nullTypeRef = new FhirReference();
		nullTypeRef.setType(null);
		nullTypeRef.setReference(BASED_ON_ORDER_UUID);
		nullTypeRef.setName();
		
		toUpdate.setOwnerReference(nullTypeRef);
		toUpdate.setBasedOnReferences(Collections.singleton(nullTypeRef));
		
		dao.createOrUpdate(toUpdate);
		
		FhirTask result = dao.get(TASK_UUID);
		
		assertThat(result.getBasedOnReferences(), notNullValue());
		assertThat(result.getBasedOnReferences().size(), greaterThan(0));
	}
	
}
