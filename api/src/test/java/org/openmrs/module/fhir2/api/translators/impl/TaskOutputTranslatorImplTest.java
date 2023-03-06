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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;

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
import org.openmrs.module.fhir2.model.FhirTaskOutput;

@RunWith(MockitoJUnitRunner.class)
public class TaskOutputTranslatorImplTest {
	
	private static final String DIAGNOSTIC_REPORT_UUID = "249b9094-b812-4b0c-a204-0052a05c657f";
	
	private static final String CONCEPT_UUID = "aed0122d-7eed-47e9-89a6-3964c9886588";
	
	@Mock
	private ReferenceTranslatorImpl referenceTranslator;
	
	@Mock
	private ConceptTranslatorImpl conceptTranslator;
	
	private TaskOutputTranslatorImpl taskOutputTranslator;
	
	@Before
	public void setup() {
		taskOutputTranslator = new TaskOutputTranslatorImpl();
		taskOutputTranslator.setReferenceTranslator(referenceTranslator);
		taskOutputTranslator.setConceptTranslator(conceptTranslator);
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateInputReference() {
		CodeableConcept outputType = innitializeFhirType();
		
		FhirReference openmrsReference = new FhirReference();
		openmrsReference.setReference(DIAGNOSTIC_REPORT_UUID);
		openmrsReference.setType(FhirConstants.DIAGNOSTIC_REPORT);
		
		when(referenceTranslator.toOpenmrsType(any(Reference.class))).thenReturn(openmrsReference);
		
		Task.TaskOutputComponent refOutput = new Task.TaskOutputComponent();
		Reference outputReference = new Reference().setReference(DIAGNOSTIC_REPORT_UUID)
		        .setType(FhirConstants.DIAGNOSTIC_REPORT);
		refOutput.setType(outputType).setValue(outputReference);
		
		FhirTaskOutput output = taskOutputTranslator.toOpenmrsType(refOutput);
		assertThat(output.getType().getUuid(), equalTo(CONCEPT_UUID));
		assertThat(output.getValueReference().getReference(), equalTo(DIAGNOSTIC_REPORT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateInputNumeric() {
		CodeableConcept outputType = innitializeFhirType();
		
		Task.TaskOutputComponent numericInput = new Task.TaskOutputComponent();
		DecimalType decimal = new DecimalType();
		Double numericValue = 12.0;
		decimal.setValue(numericValue);
		numericInput.setType(outputType);
		numericInput.setValue(decimal);
		
		FhirTaskOutput output = taskOutputTranslator.toOpenmrsType(numericInput);
		assertThat(output.getType().getUuid(), equalTo(CONCEPT_UUID));
		assertThat(output.getValueNumeric(), equalTo(numericValue));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateInputDate() throws ParseException {
		CodeableConcept outputType = innitializeFhirType();
		
		Task.TaskOutputComponent dateOutput = new Task.TaskOutputComponent();
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = "2014-02-11";
		dateOutput.setType(outputType);
		dateOutput.setValue(new DateTimeType().setValue(sdf.parse(dateString)));
		
		FhirTaskOutput output = taskOutputTranslator.toOpenmrsType(dateOutput);
		assertThat(output.getType().getUuid(), equalTo(CONCEPT_UUID));
		assertThat(sdf.format(output.getValueDatetime()), equalTo(dateString));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateInputText() {
		CodeableConcept outputType = innitializeFhirType();
		
		Task.TaskOutputComponent textOutput = new Task.TaskOutputComponent();
		String textValue = "sample output";
		textOutput.setType(outputType);
		textOutput.setValue(new StringType().setValue(textValue));
		
		FhirTaskOutput output = taskOutputTranslator.toOpenmrsType(textOutput);
		assertThat(output.getValueText(), equalTo(textValue));
	}
	
	@Test
	public void toFhirResource_shouldTranslateInputReference() {
		Concept outputType = innitializeOpenmrsType();
		
		Reference fhirReference = new Reference().setReference(DIAGNOSTIC_REPORT_UUID)
		        .setType(FhirConstants.DIAGNOSTIC_REPORT);
		when(referenceTranslator.toFhirResource(any(FhirReference.class))).thenReturn(fhirReference);
		
		FhirTaskOutput refOutput = new FhirTaskOutput();
		FhirReference inputReference = new FhirReference();
		inputReference.setType(FhirConstants.DIAGNOSTIC_REPORT);
		inputReference.setReference(DIAGNOSTIC_REPORT_UUID);
		refOutput.setType(outputType);
		refOutput.setValueReference(inputReference);
		
		Task.TaskOutputComponent taskOutput = taskOutputTranslator.toFhirResource(refOutput);
		assertThat(taskOutput.getType().getCoding().iterator().next().getCode(), equalTo(CONCEPT_UUID));
		assertTrue(taskOutput.getValue() instanceof Reference);
		assertThat(((Reference) taskOutput.getValue()).getReference(), equalTo(DIAGNOSTIC_REPORT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateInputDate() throws ParseException {
		Concept outputType = innitializeOpenmrsType();
		
		FhirTaskOutput dateOutput = new FhirTaskOutput();
		dateOutput.setType(outputType);
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = "2014-02-11";
		dateOutput.setValueDatetime(sdf.parse(dateString));
		
		Task.TaskOutputComponent taskOutput = taskOutputTranslator.toFhirResource(dateOutput);
		assertThat(taskOutput.getType().getCoding().iterator().next().getCode(), equalTo(CONCEPT_UUID));
		assertTrue(taskOutput.getValue() instanceof DateTimeType);
		String dateResult = sdf.format(((DateTimeType) taskOutput.getValue()).getValue());
		assertThat(dateResult, equalTo(dateString));
	}
	
	@Test
	public void toFhirResource_shouldTranslateInputNumeric() {
		Concept outputType = innitializeOpenmrsType();
		
		FhirTaskOutput numericOutput = new FhirTaskOutput();
		numericOutput.setType(outputType);
		Double numericValue = 12.0;
		numericOutput.setValueNumeric(numericValue);
		
		Task.TaskOutputComponent taskOutput = taskOutputTranslator.toFhirResource(numericOutput);
		assertThat(taskOutput.getType().getCoding().iterator().next().getCode(), equalTo(CONCEPT_UUID));
		assertTrue(taskOutput.getValue() instanceof DecimalType);
		assertThat(((DecimalType) taskOutput.getValue()).getValueAsNumber().doubleValue(), equalTo(numericValue));
	}
	
	@Test
	public void toFhirResource_shouldTranslateInputText() {
		Concept outputType = innitializeOpenmrsType();
		
		FhirTaskOutput textOutput = new FhirTaskOutput();
		textOutput.setType(outputType);
		String textValue = "sample output";
		textOutput.setValueText(textValue);
		
		Task.TaskOutputComponent taskOutput = taskOutputTranslator.toFhirResource(textOutput);
		assertThat(taskOutput.getType().getCoding().iterator().next().getCode(), equalTo(CONCEPT_UUID));
		assertTrue(taskOutput.getValue() instanceof StringType);
		assertThat(taskOutput.getValue().toString(), equalTo(textValue));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfOpenmrsInputHasNoValue() {
		Concept outputType = innitializeOpenmrsType();
		
		FhirTaskOutput output = new FhirTaskOutput();
		output.setType(outputType);
		
		Task.TaskOutputComponent taskInput = taskOutputTranslator.toFhirResource(output);
		assertThat(taskInput, equalTo(null));
	}
	
	private CodeableConcept innitializeFhirType() {
		CodeableConcept outputType = new CodeableConcept().setText("some text");
		Concept openmrsOutputType = new Concept();
		openmrsOutputType.setUuid(CONCEPT_UUID);
		when(conceptTranslator.toOpenmrsType(outputType)).thenReturn(openmrsOutputType);
		return outputType;
	}
	
	private Concept innitializeOpenmrsType() {
		Concept outputType = new Concept();
		outputType.setUuid(CONCEPT_UUID);
		
		when(conceptTranslator.toFhirResource(outputType))
		        .thenReturn(new CodeableConcept().setCoding(Collections.singletonList(new Coding().setCode(CONCEPT_UUID))));
		return outputType;
	}
}
