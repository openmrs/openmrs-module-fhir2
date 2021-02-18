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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.openmrs.module.fhir2.model.FhirReference;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhir2.model.FhirTaskInput;
import org.openmrs.module.fhir2.model.FhirTaskOutput;

@RunWith(MockitoJUnitRunner.class)
public class FhirTaskTranslatorImplTest {
	
	private static final String TASK_UUID = "d899333c-5bd4-45cc-b1e7-2f9542dbcbf6";
	
	private static final String PATIENT_UUID = "123456-abcdef-123456";
	
	private static final String SERVICE_REQUEST_UUID = "4e4851c3-c265-400e-acc9-1f1b0ac7f9c4";
	
	private static final String DIAGNOSTIC_REPORT_UUID = "249b9094-b812-4b0c-a204-0052a05c657f";
	
	private static final String ENCOUNTER_UUID = "34h34hj-343jk32-34nl3kd-34jk34";
	
	private static final String CONCEPT_UUID = "aed0122d-7eed-47e9-89a6-3964c9886588";
	
	private static final Task.TaskStatus FHIR_TASK_STATUS = Task.TaskStatus.REQUESTED;
	
	private static final Task.TaskStatus FHIR_NEW_TASK_STATUS = Task.TaskStatus.ACCEPTED;
	
	private static final FhirTask.TaskStatus OPENMRS_TASK_STATUS = FhirTask.TaskStatus.REQUESTED;
	
	private static final FhirTask.TaskStatus OPENMRS_NEW_TASK_STATUS = FhirTask.TaskStatus.ACCEPTED;
	
	private static final Task.TaskIntent FHIR_TASK_INTENT = Task.TaskIntent.ORDER;
	
	private static final FhirTask.TaskIntent OPENMRS_TASK_INTENT = FhirTask.TaskIntent.ORDER;
	
	private static final String OPENELIS_ID = "openelis";
	
	@Mock
	private ProvenanceTranslator<FhirTask> provenanceTranslator;
	
	@Mock
	private ReferenceTranslatorImpl referenceTranslator;
	
	@Mock
	private ConceptTranslatorImpl conceptTranslator;
	
	private TaskTranslatorImpl taskTranslator;
	
	@Before
	public void setup() {
		taskTranslator = new TaskTranslatorImpl();
		taskTranslator.setProvenanceTranslator(provenanceTranslator);
		taskTranslator.setReferenceTranslator(referenceTranslator);
		taskTranslator.setConceptTranslator(conceptTranslator);
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenmrsTaskToFhirTask() {
		FhirTask task = new FhirTask();
		Task result = taskTranslator.toFhirResource(task);
		assertThat(result, notNullValue());
	}
	
	@Test(expected = NullPointerException.class)
	public void toFhirResource_shouldThrowExceptionForNullOpenmrsTask() {
		taskTranslator.toFhirResource(null);
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirTaskToOpenmrsTask() {
		Task task = new Task();
		
		FhirTask result = taskTranslator.toOpenmrsType(task);
		
		assertThat(result, notNullValue());
	}
	
	@Test(expected = NullPointerException.class)
	public void toOpenmrsType_shouldThrowExceptionForNullTask() {
		taskTranslator.toOpenmrsType(null);
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
	
	@Test(expected = NullPointerException.class)
	public void toOpenmrsType_shouldThrowExceptionWhenNullProvided() {
		Task fhirTask = new Task();
		fhirTask.setId(TASK_UUID);
		fhirTask.setStatus(FHIR_NEW_TASK_STATUS);
		taskTranslator.toOpenmrsType(null, fhirTask);
	}
	
	@Test(expected = NullPointerException.class)
	public void toOpenmrsType_shouldThrowExceptionWhenFhirTaskNull() {
		FhirTask task = new FhirTask();
		task.setUuid(TASK_UUID);
		
		taskTranslator.toOpenmrsType(task, null);
	}
	
	@Test(expected = NullPointerException.class)
	public void toOpenmrsType_shouldThrowExceptionWhenAllNull() {
		taskTranslator.toOpenmrsType(null, null);
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
	
	@Test
	public void toOpenmrsType_shouldTranslateNullElements() {
		Task task = new Task().setBasedOn(null).setEncounter(null).setFor(null).setOwner(null).setInput(null)
		        .setOutput(null);
		
		FhirTask result = taskTranslator.toOpenmrsType(task);
		
		assertThat(result, notNullValue());
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
	public void toOpenmrsType_shouldTranslateUnsupportedStatusToUnknown() {
		Task task = new Task();
		task.setStatus(Task.TaskStatus.ENTEREDINERROR);
		
		FhirTask result = taskTranslator.toOpenmrsType(task);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(FhirTask.TaskStatus.UNKNOWN));
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
	public void toOpenmrsType_shouldTranslateUnsupportedIntent() {
		Task task = new Task();
		task.setIntent(Task.TaskIntent.PLAN);
		
		FhirTask result = taskTranslator.toOpenmrsType(task);
		
		assertThat(result, notNullValue());
		assertThat(result.getIntent(), equalTo(FhirTask.TaskIntent.ORDER));
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
		
		shouldTranslateReferenceListToFhir(task, FhirConstants.SERVICE_REQUEST, SERVICE_REQUEST_UUID,
		    task::setBasedOnReferences, t -> new ArrayList<>(t.getBasedOn()));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateBasedOn() {
		Task task = new Task();
		
		shouldTranslateReferenceListToOpenmrs(task, FhirConstants.SERVICE_REQUEST, SERVICE_REQUEST_UUID, task::setBasedOn,
		    FhirTask::getBasedOnReferences);
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateBasedOn() {
		Task task = new Task();
		
		shouldUpdateReferenceListInOpenmrs(task, FhirConstants.SERVICE_REQUEST, SERVICE_REQUEST_UUID, task::setBasedOn,
		    FhirTask::getBasedOnReferences);
	}
	
	// Task.encounter
	@Test
	public void toFhirResource_shouldTranslateEncounter() {
		FhirTask task = new FhirTask();
		
		shouldTranslateReferenceToFhir(task, FhirConstants.ENCOUNTER, ENCOUNTER_UUID, task::setEncounterReference,
		    Task::getEncounter);
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateEncounter() {
		Task task = new Task();
		
		shouldTranslateReferenceToOpenmrs(task, FhirConstants.ENCOUNTER, ENCOUNTER_UUID, task::setEncounter,
		    FhirTask::getEncounterReference);
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateEncounter() {
		Task task = new Task();
		
		shouldUpdateReferenceInOpenmrs(task, FhirConstants.ENCOUNTER, ENCOUNTER_UUID, task::setEncounter,
		    FhirTask::getEncounterReference);
	}
	
	// Task.owner
	@Test
	public void toFhirResource_shouldTranslateOwner() {
		FhirTask task = new FhirTask();
		
		shouldTranslateReferenceToFhir(task, FhirConstants.ORGANIZATION, OPENELIS_ID, task::setOwnerReference,
		    Task::getOwner);
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateOwner() {
		Task task = new Task();
		
		shouldTranslateReferenceToOpenmrs(task, FhirConstants.ORGANIZATION, OPENELIS_ID, task::setOwner,
		    FhirTask::getOwnerReference);
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateOwner() {
		Task task = new Task();
		
		shouldUpdateReferenceInOpenmrs(task, FhirConstants.ORGANIZATION, OPENELIS_ID, task::setOwner,
		    FhirTask::getOwnerReference);
	}
	
	// Task.for
	@Test
	public void toFhirResource_shouldTranslateFor() {
		FhirTask task = new FhirTask();
		
		shouldTranslateReferenceToFhir(task, FhirConstants.PATIENT, PATIENT_UUID, task::setForReference, Task::getFor);
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFor() {
		Task task = new Task();
		
		shouldTranslateReferenceToOpenmrs(task, FhirConstants.PATIENT, PATIENT_UUID, task::setFor,
		    FhirTask::getForReference);
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateFor() {
		Task task = new Task();
		
		shouldUpdateReferenceInOpenmrs(task, FhirConstants.PATIENT, PATIENT_UUID, task::setFor, FhirTask::getForReference);
	}
	
	// Task.output
	@Test
	public void toFhirResource_shouldTranslateOutputReference() {
		FhirTask task = new FhirTask();
		FhirTaskOutput output = new FhirTaskOutput();
		FhirReference outputReference = new FhirReference();
		Concept outputType = new Concept();
		
		outputReference.setType(FhirConstants.DIAGNOSTIC_REPORT);
		outputReference.setReference(DIAGNOSTIC_REPORT_UUID);
		outputType.setUuid(CONCEPT_UUID);
		
		output.setType(outputType);
		output.setValueReference(outputReference);
		task.setOutput(Collections.singleton(output));
		
		when(conceptTranslator.toFhirResource(outputType))
		        .thenReturn(new CodeableConcept().setCoding(Collections.singletonList(new Coding().setCode(CONCEPT_UUID))));
		
		Task result = shouldTranslateReferenceToFhir(task, FhirConstants.DIAGNOSTIC_REPORT, DIAGNOSTIC_REPORT_UUID,
		    output::setValueReference, t -> (Reference) t.getOutput().iterator().next().getValue());
		
		assertThat(result.getOutput(), hasSize(1));
		assertThat(result.getOutput().iterator().next().getType().getCoding().iterator().next().getCode(),
		    equalTo(CONCEPT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateOutputReference() {
		Task task = new Task();
		Task.TaskOutputComponent output = new Task.TaskOutputComponent();
		Reference outputReference = new Reference().setReference(DIAGNOSTIC_REPORT_UUID)
		        .setType(FhirConstants.DIAGNOSTIC_REPORT);
		CodeableConcept outputType = new CodeableConcept().setText("some text");
		Concept openmrsOutputType = new Concept();
		openmrsOutputType.setUuid(CONCEPT_UUID);
		
		output.setType(outputType).setValue(outputReference);
		
		task.setOutput(Collections.singletonList(output));
		
		when(conceptTranslator.toOpenmrsType(outputType)).thenReturn(openmrsOutputType);
		
		FhirTask result = shouldTranslateReferenceToOpenmrs(task, FhirConstants.DIAGNOSTIC_REPORT, DIAGNOSTIC_REPORT_UUID,
		    output::setValue, t -> t.getOutput().iterator().next().getValueReference());
		
		assertThat(result.getOutput(), hasSize(1));
		assertThat(result.getOutput().iterator().next().getType().getUuid(), equalTo(CONCEPT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateOutputReference() {
		Task task = new Task();
		Task.TaskOutputComponent output = new Task.TaskOutputComponent();
		Reference outputReference = new Reference().setReference(DIAGNOSTIC_REPORT_UUID)
		        .setType(FhirConstants.DIAGNOSTIC_REPORT);
		CodeableConcept outputType = new CodeableConcept().setText("some text");
		output.setType(outputType).setValue(outputReference);
		Concept openmrsOutputType = new Concept();
		openmrsOutputType.setUuid(CONCEPT_UUID);
		
		task.setOutput(Collections.singletonList(output));
		
		FhirTask openmrsTask = new FhirTask();
		openmrsTask.setUuid(TASK_UUID);
		openmrsTask.setOutput(Collections.singleton(new FhirTaskOutput()));
		
		when(conceptTranslator.toOpenmrsType(outputType)).thenReturn(openmrsOutputType);
		
		FhirTask result = shouldUpdateReferenceInOpenmrs(task, FhirConstants.DIAGNOSTIC_REPORT, DIAGNOSTIC_REPORT_UUID,
		    output::setValue, t -> t.getOutput().iterator().next().getValueReference());
		
		assertThat(result.getOutput(), hasSize(1));
		assertThat(result.getOutput().iterator().next().getType().getUuid(), equalTo(CONCEPT_UUID));
		
	}
	
	// Task.input
	@Test
	public void toFhirResource_shouldTranslateInputTextValue() {
		FhirTask task = new FhirTask();
		FhirTaskInput input = new FhirTaskInput();
		String inputVal = "some input value";
		Concept inputType = new Concept();
		
		inputType.setUuid(CONCEPT_UUID);
		
		input.setType(inputType);
		input.setValueText(inputVal);
		task.setInput(Collections.singleton(input));
		
		when(conceptTranslator.toFhirResource(inputType))
		        .thenReturn(new CodeableConcept().setCoding(Collections.singletonList(new Coding().setCode(CONCEPT_UUID))));
		
		Task result = taskTranslator.toFhirResource(task);
		
		assertThat(result, notNullValue());
		assertThat(result.getInput(), hasSize(1));
		assertThat(result.getInput().iterator().next().getType().getCoding().iterator().next().getCode(),
		    equalTo(CONCEPT_UUID));
		assertThat(result.getInput().iterator().next().getValue().toString(), equalTo(inputVal));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateInputTextValue() {
		Task task = new Task();
		Task.ParameterComponent input = new Task.ParameterComponent();
		CodeableConcept inputType = new CodeableConcept().setText("some text");
		String inputVal = "some input value";
		
		input.setType(inputType).setValue(new StringType(inputVal));
		
		Concept openmrsInputType = new Concept();
		openmrsInputType.setUuid(CONCEPT_UUID);
		
		task.setInput(Collections.singletonList(input));
		
		when(conceptTranslator.toOpenmrsType(inputType)).thenReturn(openmrsInputType);
		
		FhirTask result = taskTranslator.toOpenmrsType(task);
		
		assertThat(result.getInput(), not(empty()));
		assertThat(result.getInput(), hasItem(hasProperty("type", hasProperty("uuid", equalTo(CONCEPT_UUID)))));
		assertThat(result.getInput(), hasItem(hasProperty("valueText", equalTo(inputVal))));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateInputTextValue() {
		Task task = new Task();
		Task.ParameterComponent input = new Task.ParameterComponent();
		CodeableConcept inputType = new CodeableConcept().setText("some text");
		String inputVal = "some input value";
		
		input.setType(inputType).setValue(new StringType(inputVal));
		
		Concept openmrsInputType = new Concept();
		openmrsInputType.setUuid(CONCEPT_UUID);
		
		task.setInput(Collections.singletonList(input));
		
		FhirTask openmrsTask = new FhirTask();
		openmrsTask.setUuid(TASK_UUID);
		openmrsTask.setInput(Collections.singleton(new FhirTaskInput()));
		
		when(conceptTranslator.toOpenmrsType(inputType)).thenReturn(openmrsInputType);
		
		FhirTask result = taskTranslator.toOpenmrsType(openmrsTask, task);
		
		assertThat(result.getInput(), not(empty()));
		assertThat(result.getInput(), hasItem(hasProperty("type", hasProperty("uuid", equalTo(CONCEPT_UUID)))));
		assertThat(result.getInput(), hasItem(hasProperty("valueText", equalTo(inputVal))));
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
	
	// Task.Identifier
	@Test
	public void toFhirResource_shouldSetBusinessIdentifier() {
		// https://www.hl7.org/fhir/resource.html#identifiers
		FhirTask task = new FhirTask();
		
		Task result = taskTranslator.toFhirResource(task);
		
		assertThat(result, notNullValue());
		assertThat(result.getIdentifier(), hasSize(1));
		
		Identifier identifier = result.getIdentifier().iterator().next();
		
		assertThat(identifier.getValue(), equalTo(task.getUuid()));
		assertThat(identifier.getSystem(), equalTo(FhirConstants.OPENMRS_FHIR_EXT_TASK_IDENTIFIER));
	}
	
	/**
	 * Helpers for reference associations
	 */
	private Task shouldTranslateReferenceToFhir(FhirTask task, String refType, String refUuid,
	        Consumer<FhirReference> setOpenmrsReference, Function<Task, Reference> getFhirReference) {
		FhirReference openmrsReference = new FhirReference();
		openmrsReference.setType(refType);
		openmrsReference.setReference(refUuid);
		setOpenmrsReference.accept(openmrsReference);
		
		Reference fhirReference = new Reference().setReference(refUuid).setType(refType);
		
		when(referenceTranslator.toFhirResource(any(FhirReference.class))).thenReturn(fhirReference);
		
		Task result = taskTranslator.toFhirResource(task);
		Reference resultReference = getFhirReference.apply(result);
		
		assertThat(resultReference, notNullValue());
		assertThat(resultReference.getType(), equalTo(refType));
		assertThat(resultReference.getReference(), equalTo(refUuid));
		
		return result;
	}
	
	private FhirTask shouldTranslateReferenceToOpenmrs(Task task, String refType, String refUuid,
	        Consumer<Reference> setFhirReference, Function<FhirTask, FhirReference> getOpenmrsReference) {
		Reference fhirReference = new Reference().setReference(refUuid).setType(refType);
		setFhirReference.accept(fhirReference);
		
		FhirReference openmrsReference = new FhirReference();
		openmrsReference.setReference(refUuid);
		openmrsReference.setType(refType);
		
		when(referenceTranslator.toOpenmrsType(any(Reference.class))).thenReturn(openmrsReference);
		
		FhirTask result = taskTranslator.toOpenmrsType(task);
		FhirReference resultReference = getOpenmrsReference.apply(result);
		
		assertThat(resultReference, notNullValue());
		assertThat(resultReference.getReference(), equalTo(refUuid));
		assertThat(resultReference.getType(), equalTo(refType));
		
		return result;
	}
	
	private FhirTask shouldUpdateReferenceInOpenmrs(Task task, String refType, String refUuid,
	        Consumer<Reference> setFhirReference, Function<FhirTask, FhirReference> getOpenmrsReference) {
		Reference fhirReference = new Reference().setReference(refUuid).setType(refType);
		setFhirReference.accept(fhirReference);
		
		FhirReference openmrsReference = new FhirReference();
		openmrsReference.setReference(refUuid);
		openmrsReference.setType(refType);
		
		FhirTask openmrsTask = new FhirTask();
		openmrsTask.setUuid(TASK_UUID);
		openmrsTask.setEncounterReference(new FhirReference());
		
		when(referenceTranslator.toOpenmrsType(any(Reference.class))).thenReturn(openmrsReference);
		
		FhirTask result = taskTranslator.toOpenmrsType(openmrsTask, task);
		FhirReference resultReference = getOpenmrsReference.apply(result);
		
		assertThat(resultReference, notNullValue());
		assertThat(resultReference.getReference(), equalTo(refUuid));
		assertThat(resultReference.getType(), equalTo(refType));
		
		return result;
	}
	
	private void shouldTranslateReferenceListToFhir(FhirTask task, String refType, String refUuid,
	        Consumer<Set<FhirReference>> setOpenmrsReference, Function<Task, List<Reference>> getFhirReference) {
		FhirReference openmrsReference = new FhirReference();
		openmrsReference.setType(refType);
		openmrsReference.setReference(refUuid);
		setOpenmrsReference.accept(Collections.singleton(openmrsReference));
		
		Reference fhirReference = new Reference().setReference(refUuid).setType(refType);
		
		when(referenceTranslator.toFhirResource(any(FhirReference.class))).thenReturn(fhirReference);
		
		Task result = taskTranslator.toFhirResource(task);
		Collection<Reference> resultReference = getFhirReference.apply(result);
		
		assertThat(resultReference, notNullValue());
		assertThat(resultReference, hasSize(1));
		assertThat(resultReference.iterator().next(), notNullValue());
		assertThat(resultReference.iterator().next().getType(), equalTo(refType));
		assertThat(resultReference.iterator().next().getReference(), equalTo(refUuid));
		
	}
	
	private void shouldTranslateReferenceListToOpenmrs(Task task, String refType, String refUuid,
	        Consumer<List<Reference>> setFhirReference, Function<FhirTask, Set<FhirReference>> getOpenmrsReference) {
		
		Reference fhirReference = new Reference().setReference(refUuid).setType(refType);
		setFhirReference.accept(Collections.singletonList(fhirReference));
		
		FhirReference openmrsReference = new FhirReference();
		openmrsReference.setReference(refUuid);
		openmrsReference.setType(refType);
		
		when(referenceTranslator.toOpenmrsType(any(Reference.class))).thenReturn(openmrsReference);
		
		FhirTask result = taskTranslator.toOpenmrsType(task);
		Collection<FhirReference> resultReference = getOpenmrsReference.apply(result);
		
		assertThat(resultReference, notNullValue());
		assertThat(resultReference, hasSize(1));
		assertThat(resultReference.iterator().next().getReference(), equalTo(refUuid));
		assertThat(resultReference.iterator().next().getType(), equalTo(refType));
		
	}
	
	private void shouldUpdateReferenceListInOpenmrs(Task task, String refType, String refUuid,
	        Consumer<List<Reference>> setFhirReference, Function<FhirTask, Set<FhirReference>> getOpenmrsReference) {
		
		Reference fhirReference = new Reference().setReference(refUuid).setType(refType);
		setFhirReference.accept(Collections.singletonList(fhirReference));
		
		FhirReference openmrsReference = new FhirReference();
		openmrsReference.setReference(refUuid);
		openmrsReference.setType(refType);
		
		FhirTask openmrsTask = new FhirTask();
		openmrsTask.setUuid(TASK_UUID);
		openmrsTask.setEncounterReference(new FhirReference());
		
		when(referenceTranslator.toOpenmrsType(any(Reference.class))).thenReturn(openmrsReference);
		
		FhirTask result = taskTranslator.toOpenmrsType(openmrsTask, task);
		Collection<FhirReference> resultReference = getOpenmrsReference.apply(result);
		
		assertThat(resultReference, notNullValue());
		assertThat(resultReference, hasSize(1));
		assertThat(resultReference.iterator().next().getReference(), equalTo(refUuid));
		assertThat(resultReference.iterator().next().getType(), equalTo(refType));
		
	}
	
	@Test
	public void toFhirResource_shouldAddProvenanceResources() {
		FhirTask task = new FhirTask();
		task.setUuid(TASK_UUID);
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.newUuid()));
		when(provenanceTranslator.getCreateProvenance(task)).thenReturn(provenance);
		when(provenanceTranslator.getUpdateProvenance(task)).thenReturn(provenance);
		org.hl7.fhir.r4.model.Task result = taskTranslator.toFhirResource(task);
		assertThat(result, notNullValue());
		assertThat(result.getContained(), not(empty()));
		assertThat(result.getContained().size(), greaterThanOrEqualTo(2));
		assertThat(result.getContained().stream()
		        .anyMatch(resource -> resource.getResourceType().name().equals(Provenance.class.getSimpleName())),
		    is(true));
	}
}
