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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Collections;
import java.util.Date;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.FhirTask;

public class FhirTaskTranslatorImplTest {
	
	private static final String TASK_UUID = "d899333c-5bd4-45cc-b1e7-2f9542dbcbf6";
	
	private static final String PATIENT_UUID = "123456-abcdef-123456";
	
	private static final String SERVICE_REQUEST_UUID = "4e4851c3-c265-400e-acc9-1f1b0ac7f9c4";
	
	private static final String DIAGNOSTIC_REPORT_UUID = "249b9094-b812-4b0c-a204-0052a05c657f";
	
	private static final String ENCOUNTER_UUID = "34h34hj-343jk32-34nl3kd-34jk34";
	
	private static final Task.TaskStatus FHIR_TASK_STATUS = Task.TaskStatus.REQUESTED;
	
	private static final Task.TaskStatus FHIR_NEW_TASK_STATUS = Task.TaskStatus.ACCEPTED;
	
	private static final FhirTask.TaskStatus OPENMRS_TASK_STATUS = FhirTask.TaskStatus.REQUESTED;
	
	private static final FhirTask.TaskStatus OPENMRS_NEW_TASK_STATUS = FhirTask.TaskStatus.ACCEPTED;
	
	private static final Task.TaskIntent FHIR_TASK_INTENT = Task.TaskIntent.ORDER;
	
	private static final FhirTask.TaskIntent OPENMRS_TASK_INTENT = FhirTask.TaskIntent.ORDER;
	
	private static final String OPENELIS_ID = "openelis";
	
	private TaskTranslatorImpl taskTranslator;
	
	@Before
	public void setup() {
		taskTranslator = new TaskTranslatorImpl();
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenmrsTaskToFhirTask() {
		FhirTask task = new FhirTask();
		Task result = taskTranslator.toFhirResource(task);
		assertThat(result, notNullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirTaskToOpenmrsTask() {
		Task task = new Task();
		
		FhirTask result = taskTranslator.toOpenmrsType(task);
		
		assertThat(result, notNullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateNewOpenmrsTask() {
		Task fhirTask = new Task();
		
		FhirTask result = taskTranslator.toOpenmrsType(fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), notNullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldIgnoreUUIDForNewOpenmrsTask() {
		Task fhirTask = new Task();
		
		fhirTask.setId(TASK_UUID);
		
		FhirTask result = taskTranslator.toOpenmrsType(fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), not(equalTo(TASK_UUID)));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateExistingOpenmrsTask() {
		FhirTask task = new FhirTask();
		task.setUuid(TASK_UUID);
		
		Task fhirTask = taskTranslator.toFhirResource(task);
		fhirTask.setStatus(FHIR_NEW_TASK_STATUS);
		
		FhirTask result = taskTranslator.toOpenmrsType(task, fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(OPENMRS_NEW_TASK_STATUS));
	}
	
	@Test
	public void toOpenmrsType_shouldCreateOpenmrsTaskWhenNull() {
		Task fhirTask = new Task();
		fhirTask.setId(TASK_UUID);
		fhirTask.setStatus(FHIR_NEW_TASK_STATUS);
		
		FhirTask result = taskTranslator.toOpenmrsType(null, fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(OPENMRS_NEW_TASK_STATUS));
	}
	
	@Test
	public void toOpenmrsType_shouldSetOpenmrsTaskUUIDWhenNull() {
		FhirTask task = new FhirTask();
		task.setUuid(null);
		
		Task fhirTask = taskTranslator.toFhirResource(task);
		fhirTask.setId(TASK_UUID);
		
		FhirTask result = taskTranslator.toOpenmrsType(task, fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(TASK_UUID));
	}
	
	// Task.status
	@Test
	public void toFhirResource_shouldTranslateStatus() {
		FhirTask task = new FhirTask();
		task.setStatus(OPENMRS_TASK_STATUS);
		
		Task result = taskTranslator.toFhirResource(task);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(FHIR_TASK_STATUS));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateStatus() {
		Task task = new Task();
		task.setStatus(FHIR_TASK_STATUS);
		
		FhirTask result = taskTranslator.toOpenmrsType(task);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(OPENMRS_TASK_STATUS));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateStatusOnExistingTask() {
		FhirTask task = new FhirTask();
		task.setStatus(OPENMRS_TASK_STATUS);
		task.setUuid(TASK_UUID);
		
		Task fhirTask = taskTranslator.toFhirResource(task);
		fhirTask.setStatus(FHIR_NEW_TASK_STATUS);
		
		FhirTask result = taskTranslator.toOpenmrsType(task, fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(OPENMRS_NEW_TASK_STATUS));
	}
	
	// Task.intent
	@Test
	public void toFhirResource_shouldTranslateIntent() {
		FhirTask task = new FhirTask();
		task.setIntent(OPENMRS_TASK_INTENT);
		
		Task result = taskTranslator.toFhirResource(task);
		
		assertThat(result, notNullValue());
		assertThat(result.getIntent(), equalTo(FHIR_TASK_INTENT));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateIntent() {
		Task task = new Task();
		task.setIntent(FHIR_TASK_INTENT);
		
		FhirTask result = taskTranslator.toOpenmrsType(task);
		
		assertThat(result, notNullValue());
		assertThat(result.getIntent(), equalTo(OPENMRS_TASK_INTENT));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateIntentOnExistingTask() {
		FhirTask task = new FhirTask();
		task.setIntent(null);
		task.setUuid(TASK_UUID);
		
		Task fhirTask = taskTranslator.toFhirResource(task);
		fhirTask.setIntent(FHIR_TASK_INTENT);
		
		FhirTask result = taskTranslator.toOpenmrsType(task, fhirTask);
		
		assertThat(result, notNullValue());
		assertThat(result.getIntent(), equalTo(OPENMRS_TASK_INTENT));
	}
	
	// Task.basedOn
	@Test
	public void toFhirResource_shouldTranslateBasedOn() {
		FhirTask task = new FhirTask();
		
		task.setBasedOnReferences(Collections.singletonList(FhirConstants.SERVICE_REQUEST + "/" + SERVICE_REQUEST_UUID));
		
		Task result = taskTranslator.toFhirResource(task);
		
		assertThat(result, notNullValue());
		assertThat(result.getBasedOn(), not(empty()));
		assertThat(result.getBasedOn().iterator().next().getType(), equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(result.getBasedOn().iterator().next().getIdentifier().getValue(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateBasedOn() {
		Task task = new Task();
		String refPath = FhirConstants.SERVICE_REQUEST + "/" + SERVICE_REQUEST_UUID;
		task.setBasedOn(Collections.singletonList(new Reference().setReference(refPath)
		        .setType(FhirConstants.SERVICE_REQUEST).setIdentifier(new Identifier().setValue(SERVICE_REQUEST_UUID))));
		
		FhirTask result = taskTranslator.toOpenmrsType(task);
		
		assertThat(result.getBasedOnReferences(), notNullValue());
		assertThat(result.getBasedOnReferences(), hasSize(1));
		assertThat(result.getBasedOnReferences().iterator().next(), equalTo(refPath));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateBasedOn() {
		Task task = new Task();
		String refPath = FhirConstants.SERVICE_REQUEST + "/" + SERVICE_REQUEST_UUID;
		task.setBasedOn(Collections.singletonList(new Reference().setReference(refPath)
		        .setType(FhirConstants.SERVICE_REQUEST).setIdentifier(new Identifier().setValue(SERVICE_REQUEST_UUID))));
		
		FhirTask openmrsTask = new FhirTask();
		openmrsTask.setUuid(TASK_UUID);
		openmrsTask.setBasedOnReferences(Collections.singletonList("<Some Reference String>"));
		
		FhirTask result = taskTranslator.toOpenmrsType(openmrsTask, task);
		
		assertThat(result.getUuid(), equalTo(TASK_UUID));
		assertThat(result.getBasedOnReferences(), notNullValue());
		assertThat(result.getBasedOnReferences(), hasSize(1));
		assertThat(result.getBasedOnReferences().iterator().next(), equalTo(refPath));
	}
	
	// Task.encounter
	@Test
	public void toFhirResource_shouldTranslateEncounter() {
		FhirTask task = new FhirTask();
		task.setEncounterReference(FhirConstants.ENCOUNTER + "/" + ENCOUNTER_UUID);
		
		Task result = taskTranslator.toFhirResource(task);
		
		assertThat(result, notNullValue());
		assertThat(result.getEncounter(), notNullValue());
		assertThat(result.getEncounter().getType(), equalTo(FhirConstants.ENCOUNTER));
		assertThat(result.getEncounter().getIdentifier().getValue(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateEncounter() {
		Task task = new Task();
		String refPath = FhirConstants.ENCOUNTER + "/" + ENCOUNTER_UUID;
		task.setEncounter(new Reference().setReference(refPath).setType(FhirConstants.ENCOUNTER)
		        .setIdentifier(new Identifier().setValue(ENCOUNTER_UUID)));
		
		FhirTask result = taskTranslator.toOpenmrsType(task);
		
		assertThat(result.getEncounterReference(), notNullValue());
		assertThat(result.getEncounterReference(), equalTo(refPath));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateEncounter() {
		Task task = new Task();
		String refPath = FhirConstants.ENCOUNTER + "/" + ENCOUNTER_UUID;
		task.setEncounter(new Reference().setReference(refPath).setType(FhirConstants.ENCOUNTER)
		        .setIdentifier(new Identifier().setValue(ENCOUNTER_UUID)));
		
		FhirTask openmrsTask = new FhirTask();
		openmrsTask.setUuid(TASK_UUID);
		openmrsTask.setEncounterReference("<Some Reference String>");
		
		FhirTask result = taskTranslator.toOpenmrsType(openmrsTask, task);
		
		assertThat(result.getUuid(), equalTo(TASK_UUID));
		assertThat(result.getEncounterReference(), notNullValue());
		assertThat(result.getEncounterReference(), equalTo(refPath));
	}
	
	// Task.owner
	@Test
	public void toFhirResource_shouldTranslateOwner() {
		FhirTask task = new FhirTask();
		task.setOwnerReference(FhirConstants.ORGANIZATION + "/" + OPENELIS_ID);
		
		Task result = taskTranslator.toFhirResource(task);
		
		assertThat(result, notNullValue());
		assertThat(result.getOwner(), notNullValue());
		assertThat(result.getOwner().getType(), equalTo(FhirConstants.ORGANIZATION));
		assertThat(result.getOwner().getIdentifier().getValue(), equalTo(OPENELIS_ID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateOwner() {
		Task task = new Task();
		String refPath = FhirConstants.ORGANIZATION + "/" + OPENELIS_ID;
		task.setOwner(new Reference().setReference(refPath).setType(FhirConstants.ORGANIZATION)
		        .setIdentifier(new Identifier().setValue(OPENELIS_ID)));
		
		FhirTask result = taskTranslator.toOpenmrsType(task);
		
		assertThat(result.getOwnerReference(), notNullValue());
		assertThat(result.getOwnerReference(), equalTo(refPath));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateOwner() {
		Task task = new Task();
		String refPath = FhirConstants.ORGANIZATION + "/" + OPENELIS_ID;
		task.setOwner(new Reference().setReference(refPath).setType(FhirConstants.ORGANIZATION)
		        .setIdentifier(new Identifier().setValue(OPENELIS_ID)));
		
		FhirTask openmrsTask = new FhirTask();
		openmrsTask.setUuid(TASK_UUID);
		openmrsTask.setOwnerReference("<Some Reference String>");
		
		FhirTask result = taskTranslator.toOpenmrsType(openmrsTask, task);
		
		assertThat(result.getUuid(), equalTo(TASK_UUID));
		assertThat(result.getOwnerReference(), notNullValue());
		assertThat(result.getOwnerReference(), equalTo(refPath));
	}
	
	// Task.for
	@Test
	public void toFhirResource_shouldTranslateFor() {
		FhirTask task = new FhirTask();
		task.setForReference(FhirConstants.PATIENT + "/" + PATIENT_UUID);
		
		Task result = taskTranslator.toFhirResource(task);
		
		assertThat(result, notNullValue());
		assertThat(result.getFor(), notNullValue());
		assertThat(result.getFor().getType(), equalTo(FhirConstants.PATIENT));
		assertThat(result.getFor().getIdentifier().getValue(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFor() {
		Task task = new Task();
		String refPath = FhirConstants.PATIENT + "/" + PATIENT_UUID;
		task.setFor(new Reference().setReference(refPath).setType(FhirConstants.PATIENT)
		        .setIdentifier(new Identifier().setValue(PATIENT_UUID)));
		
		FhirTask result = taskTranslator.toOpenmrsType(task);
		
		assertThat(result.getForReference(), notNullValue());
		assertThat(result.getForReference(), equalTo(refPath));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateFor() {
		Task task = new Task();
		String refPath = FhirConstants.PATIENT + "/" + PATIENT_UUID;
		task.setFor(new Reference().setReference(refPath).setType(FhirConstants.PATIENT)
		        .setIdentifier(new Identifier().setValue(PATIENT_UUID)));
		
		FhirTask openmrsTask = new FhirTask();
		openmrsTask.setUuid(TASK_UUID);
		openmrsTask.setForReference("<Some Reference String>");
		
		FhirTask result = taskTranslator.toOpenmrsType(openmrsTask, task);
		
		assertThat(result.getUuid(), equalTo(TASK_UUID));
		assertThat(result.getForReference(), notNullValue());
		assertThat(result.getForReference(), equalTo(refPath));
	}
	
	// Task.output
	@Test
	public void toFhirResource_shouldTranslateOutput() {
		FhirTask task = new FhirTask();
		
		task.setOutputReferences(Collections.singletonList(FhirConstants.DIAGNOSTIC_REPORT + "/" + DIAGNOSTIC_REPORT_UUID));
		
		Task result = taskTranslator.toFhirResource(task);
		
		assertThat(result, notNullValue());
		assertThat(result.getOutput(), not(empty()));
		assertThat(result.getOutput().iterator().next().getType().getText(),
		    containsString(FhirConstants.DIAGNOSTIC_REPORT));
		
		Reference outputVal = (Reference) result.getOutput().iterator().next().getValue();
		assertThat(outputVal.getType(), equalTo(FhirConstants.DIAGNOSTIC_REPORT));
		assertThat(outputVal.getIdentifier().getValue(), equalTo(DIAGNOSTIC_REPORT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateOutput() {
		Task task = new Task();
		String refPath = FhirConstants.DIAGNOSTIC_REPORT + "/" + DIAGNOSTIC_REPORT_UUID;
		Reference ref = new Reference().setReference(refPath).setType(FhirConstants.DIAGNOSTIC_REPORT)
		        .setIdentifier(new Identifier().setValue(DIAGNOSTIC_REPORT_UUID));
		
		task.setOutput(Collections.singletonList(new Task.TaskOutputComponent().setValue(ref)
		        .setType(new CodeableConcept().setText(FhirConstants.DIAGNOSTIC_REPORT + " generated"))));
		
		FhirTask result = taskTranslator.toOpenmrsType(task);
		
		assertThat(result.getOutputReferences(), notNullValue());
		assertThat(result.getOutputReferences(), hasSize(1));
		assertThat(result.getOutputReferences().iterator().next(), equalTo(refPath));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateOutput() {
		Task task = new Task();
		String refPath = FhirConstants.DIAGNOSTIC_REPORT + "/" + DIAGNOSTIC_REPORT_UUID;
		Reference ref = new Reference().setReference(refPath).setType(FhirConstants.DIAGNOSTIC_REPORT)
		        .setIdentifier(new Identifier().setValue(DIAGNOSTIC_REPORT_UUID));
		task.setOutput(Collections.singletonList(new Task.TaskOutputComponent().setValue(ref)
		        .setType(new CodeableConcept().setText(FhirConstants.DIAGNOSTIC_REPORT + " generated"))));
		
		FhirTask openmrsTask = new FhirTask();
		openmrsTask.setUuid(TASK_UUID);
		openmrsTask.setOutputReferences(Collections.singletonList("<Some Reference String>"));
		
		FhirTask result = taskTranslator.toOpenmrsType(openmrsTask, task);
		
		assertThat(result.getUuid(), equalTo(TASK_UUID));
		assertThat(result.getOutputReferences(), notNullValue());
		assertThat(result.getOutputReferences(), hasSize(1));
		assertThat(result.getOutputReferences().iterator().next(), equalTo(refPath));
	}

	// Task.authoredOn
	@Test
	public void toFhirResource_shouldTranslateAuthoredOn() {
		FhirTask task = new FhirTask();
		Date createdDate = new Date();

		task.setDateCreated(createdDate);

		Task result = taskTranslator.toFhirResource(task);

		assertThat(result, notNullValue());
		assertThat(result.getAuthoredOn(), equalTo(createdDate));
	}

	// Task.lastModified
	@Test
	public void toFhirResource_shouldTranslateLastModified() {
		FhirTask task = new FhirTask();
		Date dateModified = new Date();

		task.setDateChanged(dateModified);

		Task result = taskTranslator.toFhirResource(task);

		assertThat(result, notNullValue());
		assertThat(result.getLastModified(), equalTo(dateModified));
	}
}

