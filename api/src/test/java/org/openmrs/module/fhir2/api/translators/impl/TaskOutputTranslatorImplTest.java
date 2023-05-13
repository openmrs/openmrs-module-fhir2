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
	public void toOpenmrsType_shouldTranslateOutputReference() {
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
	public void toOpenmrsType_shouldTranslateOutputNumeric() {
		CodeableConcept outputType = innitializeFhirType();
		
		Task.TaskOutputComponent numericOutput = new Task.TaskOutputComponent();
		DecimalType decimal = new DecimalType();
		Double numericValue = 12.0;
		decimal.setValue(numericValue);
		numericOutput.setType(outputType);
		numericOutput.setValue(decimal);
		
		FhirTaskOutput output = taskOutputTranslator.toOpenmrsType(numericOutput);
		assertThat(output.getType().getUuid(), equalTo(CONCEPT_UUID));
		assertThat(output.getValueNumeric(), equalTo(numericValue));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateOutputDate() {
		CodeableConcept outputType = innitializeFhirType();
		
		Task.TaskOutputComponent dateOutput = new Task.TaskOutputComponent();
		dateOutput.setType(outputType);
		dateOutput.setValue(new DateTimeType().setValue(new Date()));
		
		FhirTaskOutput output = taskOutputTranslator.toOpenmrsType(dateOutput);
		assertThat(output.getType().getUuid(), equalTo(CONCEPT_UUID));
		assertThat(output.getValueDatetime(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateOutputText() {
		CodeableConcept outputType = innitializeFhirType();
		
		Task.TaskOutputComponent textOutput = new Task.TaskOutputComponent();
		String textValue = "sample output";
		textOutput.setType(outputType);
		textOutput.setValue(new StringType().setValue(textValue));
		
		FhirTaskOutput output = taskOutputTranslator.toOpenmrsType(textOutput);
		assertThat(output.getValueText(), equalTo(textValue));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfFhirOutputHasValueTypeNotSupported() {
		CodeableConcept outputType = innitializeFhirType();
		
		Task.TaskOutputComponent output = new Task.TaskOutputComponent();
		output.setType(outputType);
		output.setValue(new BooleanType(true));
		
		FhirTaskOutput openmrsOutput = taskOutputTranslator.toOpenmrsType(output);
		assertThat(openmrsOutput, equalTo(null));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOutputReference() {
		Concept outputType = innitializeOpenmrsType();
		
		Reference fhirReference = new Reference().setReference(DIAGNOSTIC_REPORT_UUID)
		        .setType(FhirConstants.DIAGNOSTIC_REPORT);
		when(referenceTranslator.toFhirResource(any(FhirReference.class))).thenReturn(fhirReference);
		
		FhirTaskOutput refOutput = new FhirTaskOutput();
		FhirReference outputReference = new FhirReference();
		outputReference.setType(FhirConstants.DIAGNOSTIC_REPORT);
		outputReference.setReference(DIAGNOSTIC_REPORT_UUID);
		refOutput.setType(outputType);
		refOutput.setValueReference(outputReference);
		
		Task.TaskOutputComponent taskOutput = taskOutputTranslator.toFhirResource(refOutput);
		assertThat(taskOutput.getType().getCoding().iterator().next().getCode(), equalTo(CONCEPT_UUID));
		assertTrue(taskOutput.getValue() instanceof Reference);
		assertThat(((Reference) taskOutput.getValue()).getReference(), equalTo(DIAGNOSTIC_REPORT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOutputDate() {
		Concept outputType = innitializeOpenmrsType();
		
		FhirTaskOutput dateOutput = new FhirTaskOutput();
		dateOutput.setType(outputType);
		dateOutput.setValueDatetime(new Date());
		
		Task.TaskOutputComponent taskOutput = taskOutputTranslator.toFhirResource(dateOutput);
		assertThat(taskOutput.getType().getCoding().iterator().next().getCode(), equalTo(CONCEPT_UUID));
		assertTrue(taskOutput.getValue() instanceof DateTimeType);
		assertThat(((DateTimeType) taskOutput.getValue()).getValue(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOutputNumeric() {
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
	public void toFhirResource_shouldTranslateOutputText() {
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
	public void toFhirResource_shouldReturnNullIfOpenmrsOutputHasNoValue() {
		Concept outputType = innitializeOpenmrsType();
		
		FhirTaskOutput output = new FhirTaskOutput();
		output.setType(outputType);
		
		Task.TaskOutputComponent taskOutput = taskOutputTranslator.toFhirResource(output);
		assertThat(taskOutput, equalTo(null));
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
