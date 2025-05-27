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

import static lombok.AccessLevel.PROTECTED;

import javax.annotation.Nonnull;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.hl7.fhir.r4.model.Type;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.TaskInputTranslator;
import org.openmrs.module.fhir2.model.FhirReference;
import org.openmrs.module.fhir2.model.FhirTaskInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskInputTranslatorImpl implements TaskInputTranslator {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ConceptTranslator conceptTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ReferenceTranslator referenceTranslator;
	
	@Override
	public ParameterComponent toFhirResource(@Nonnull FhirTaskInput openmrsTaskInput) {
		CodeableConcept type = conceptTranslator.toFhirResource(openmrsTaskInput.getType());
		
		Task.ParameterComponent input = new Task.ParameterComponent().setType(type);
		if (openmrsTaskInput.getValueReference() != null) {
			Reference ref = referenceTranslator.toFhirResource(openmrsTaskInput.getValueReference());
			return input.setValue(ref);
		} else if (openmrsTaskInput.getValueText() != null) {
			return input.setValue(new StringType().setValue(openmrsTaskInput.getValueText()));
		} else if (openmrsTaskInput.getValueNumeric() != null) {
			DecimalType decimal = new DecimalType();
			decimal.setValue(openmrsTaskInput.getValueNumeric());
			return input.setValue(decimal);
		} else if (openmrsTaskInput.getValueDatetime() != null) {
			return input.setValue(new DateTimeType().setValue(openmrsTaskInput.getValueDatetime()));
		} else {
			return null;
		}
	}
	
	@Override
	public FhirTaskInput toOpenmrsType(@Nonnull ParameterComponent parameterComponent) {
		FhirTaskInput input = new FhirTaskInput();
		Concept type = conceptTranslator.toOpenmrsType(parameterComponent.getType());
		input.setType(type);
		
		Type value = parameterComponent.getValue();
		if (value instanceof Reference) {
			FhirReference ref = referenceTranslator.toOpenmrsType((Reference) value);
			input.setValueReference(ref);
		} else if (value instanceof StringType) {
			input.setValueText(value.toString());
		} else if (value instanceof DecimalType) {
			input.setValueNumeric(((DecimalType) value).getValueAsNumber().doubleValue());
		} else if (value instanceof DateTimeType) {
			input.setValueDatetime(((DateTimeType) value).getValue());
		} else {
			return null;
		}
		
		input.setName("ParameterComponent/" + input.getUuid());
		return input;
	}
}
