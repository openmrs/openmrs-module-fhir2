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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DecimalType;
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
import org.openmrs.module.fhir2.model.FhirReference;
import org.openmrs.module.fhir2.model.FhirTaskInput;

@RunWith(MockitoJUnitRunner.class)
public class TaskInputTranslatorImplTest {
	
	private static final String DIAGNOSTIC_REPORT_UUID = "249b9094-b812-4b0c-a204-0052a05c657f";
	
	private static final String CONCEPT_UUID = "aed0122d-7eed-47e9-89a6-3964c9886588";
	
	@Mock
	private ReferenceTranslatorImpl referenceTranslator;
	
	@Mock
	private ConceptTranslatorImpl conceptTranslator;
	
	private TaskInputTranslatorImpl taskInputTranslator;
	
	@Before
	public void setup() {
		taskInputTranslator = new TaskInputTranslatorImpl();
		taskInputTranslator.setReferenceTranslator(referenceTranslator);
		taskInputTranslator.setConceptTranslator(conceptTranslator);
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateInputReference() {
		CodeableConcept inputType = innitializeFhirType();
		
		FhirReference openmrsReference = new FhirReference();
		openmrsReference.setReference(DIAGNOSTIC_REPORT_UUID);
		openmrsReference.setType(FhirConstants.DIAGNOSTIC_REPORT);
		
		when(referenceTranslator.toOpenmrsType(any(Reference.class))).thenReturn(openmrsReference);
		
		Task.ParameterComponent refInput = new Task.ParameterComponent();
		Reference inputReference = new Reference().setReference(DIAGNOSTIC_REPORT_UUID)
		        .setType(FhirConstants.DIAGNOSTIC_REPORT);
		refInput.setType(inputType).setValue(inputReference);
		
		FhirTaskInput input = taskInputTranslator.toOpenmrsType(refInput);
		assertThat(input.getType().getUuid(), equalTo(CONCEPT_UUID));
		assertThat(input.getValueReference().getReference(), equalTo(DIAGNOSTIC_REPORT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateInputNumeric() {
		CodeableConcept inputType = innitializeFhirType();
		
		Task.ParameterComponent numericInput = new Task.ParameterComponent();
		DecimalType decimal = new DecimalType();
		Double numericValue = 12.0;
		decimal.setValue(numericValue);
		numericInput.setType(inputType);
		numericInput.setValue(decimal);
		
		FhirTaskInput input = taskInputTranslator.toOpenmrsType(numericInput);
		assertThat(input.getType().getUuid(), equalTo(CONCEPT_UUID));
		assertThat(input.getValueNumeric(), equalTo(numericValue));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateInputDate() throws ParseException {
		CodeableConcept inputType = innitializeFhirType();
		
		Task.ParameterComponent dateInput = new Task.ParameterComponent();
		dateInput.setType(inputType);
		dateInput.setValue(new DateTimeType().setValue(new Date()));
		
		FhirTaskInput input = taskInputTranslator.toOpenmrsType(dateInput);
		assertThat(input.getType().getUuid(), equalTo(CONCEPT_UUID));
		assertThat(input.getValueDatetime(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateInputText() {
		CodeableConcept inputType = innitializeFhirType();
		
		Task.ParameterComponent textInput = new Task.ParameterComponent();
		String textValue = "sample output";
		textInput.setType(inputType);
		textInput.setValue(new StringType().setValue(textValue));
		
		FhirTaskInput input = taskInputTranslator.toOpenmrsType(textInput);
		assertThat(input.getValueText(), equalTo(textValue));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfFhirInputhasValueTypeNotSupported() {
		CodeableConcept inputType = innitializeFhirType();
		
		Task.ParameterComponent input = new Task.ParameterComponent();
		input.setType(inputType);
		input.setValue(new BooleanType(true));
		
		FhirTaskInput openmrsInput = taskInputTranslator.toOpenmrsType(input);
		assertThat(openmrsInput, equalTo(null));
	}
	
	@Test
	public void toFhirResource_shouldTranslateInputReference() {
		Concept inputType = innitializeOpenmrsType();
		
		Reference fhirReference = new Reference().setReference(DIAGNOSTIC_REPORT_UUID)
		        .setType(FhirConstants.DIAGNOSTIC_REPORT);
		when(referenceTranslator.toFhirResource(any(FhirReference.class))).thenReturn(fhirReference);
		
		FhirTaskInput refInput = new FhirTaskInput();
		FhirReference inputReference = new FhirReference();
		inputReference.setType(FhirConstants.DIAGNOSTIC_REPORT);
		inputReference.setReference(DIAGNOSTIC_REPORT_UUID);
		refInput.setType(inputType);
		refInput.setValueReference(inputReference);
		
		Task.ParameterComponent taskInput = taskInputTranslator.toFhirResource(refInput);
		assertThat(taskInput.getType().getCoding().iterator().next().getCode(), equalTo(CONCEPT_UUID));
		assertTrue(taskInput.getValue() instanceof Reference);
		assertThat(((Reference) taskInput.getValue()).getReference(), equalTo(DIAGNOSTIC_REPORT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateInputDate() throws ParseException {
		Concept inputType = innitializeOpenmrsType();
		
		FhirTaskInput dateInput = new FhirTaskInput();
		dateInput.setType(inputType);
		dateInput.setValueDatetime(new Date());
		
		Task.ParameterComponent taskInput = taskInputTranslator.toFhirResource(dateInput);
		assertThat(taskInput.getType().getCoding().iterator().next().getCode(), equalTo(CONCEPT_UUID));
		assertTrue(taskInput.getValue() instanceof DateTimeType);
		assertThat(((DateTimeType) taskInput.getValue()).getValue(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toFhirResource_shouldTranslateInputNumeric() {
		Concept inputType = innitializeOpenmrsType();
		
		FhirTaskInput numericInput = new FhirTaskInput();
		numericInput.setType(inputType);
		Double numericValue = 12.0;
		numericInput.setValueNumeric(numericValue);
		
		Task.ParameterComponent taskInput = taskInputTranslator.toFhirResource(numericInput);
		assertThat(taskInput.getType().getCoding().iterator().next().getCode(), equalTo(CONCEPT_UUID));
		assertTrue(taskInput.getValue() instanceof DecimalType);
		assertThat(((DecimalType) taskInput.getValue()).getValueAsNumber().doubleValue(), equalTo(numericValue));
	}
	
	@Test
	public void toFhirResource_shouldTranslateInputText() {
		Concept inputType = innitializeOpenmrsType();
		
		FhirTaskInput textInput = new FhirTaskInput();
		textInput.setType(inputType);
		String textValue = "sample output";
		textInput.setValueText(textValue);
		
		Task.ParameterComponent taskInput = taskInputTranslator.toFhirResource(textInput);
		assertThat(taskInput.getType().getCoding().iterator().next().getCode(), equalTo(CONCEPT_UUID));
		assertTrue(taskInput.getValue() instanceof StringType);
		assertThat(taskInput.getValue().toString(), equalTo(textValue));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfOpenmrsInputHasNoValue() {
		Concept inputType = innitializeOpenmrsType();
		
		FhirTaskInput input = new FhirTaskInput();
		input.setType(inputType);
		
		Task.ParameterComponent taskInput = taskInputTranslator.toFhirResource(input);
		assertThat(taskInput, equalTo(null));
	}
	
	private CodeableConcept innitializeFhirType() {
		CodeableConcept inputType = new CodeableConcept().setText("some text");
		Concept openmrsInputType = new Concept();
		openmrsInputType.setUuid(CONCEPT_UUID);
		when(conceptTranslator.toOpenmrsType(inputType)).thenReturn(openmrsInputType);
		return inputType;
	}
	
	private Concept innitializeOpenmrsType() {
		Concept inputType = new Concept();
		inputType.setUuid(CONCEPT_UUID);
		
		when(conceptTranslator.toFhirResource(inputType))
		        .thenReturn(new CodeableConcept().setCoding(Collections.singletonList(new Coding().setCode(CONCEPT_UUID))));
		return inputType;
	}
}
