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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskOutputComponent;
import org.hl7.fhir.r4.model.Type;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.TaskOutputTranslator;
import org.openmrs.module.fhir2.model.FhirReference;
import org.openmrs.module.fhir2.model.FhirTaskOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class TaskOutputTranslatorImpl implements TaskOutputTranslator {
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Autowired
	private ReferenceTranslator referenceTranslator;
	
	@Override
	public TaskOutputComponent toFhirResource(FhirTaskOutput openmrsTaskOutput) {
		CodeableConcept type = conceptTranslator.toFhirResource(openmrsTaskOutput.getType());
		Task.TaskOutputComponent output = new Task.TaskOutputComponent().setType(type);
		if (openmrsTaskOutput.getValueReference() != null) {
			Reference ref = referenceTranslator.toFhirResource(openmrsTaskOutput.getValueReference());
			return output.setValue(ref);
		} else if (openmrsTaskOutput.getValueText() != null) {
			return output.setValue(new StringType().setValue(openmrsTaskOutput.getValueText()));
		} else if (openmrsTaskOutput.getValueNumeric() != null) {
			DecimalType decimal = new DecimalType();
			decimal.setValue(openmrsTaskOutput.getValueNumeric());
			return output.setValue(decimal);
		} else if (openmrsTaskOutput.getValueDatetime() != null) {
			return output.setValue(new DateTimeType().setValue(openmrsTaskOutput.getValueDatetime()));
		} else {
			return null;
		}
	}
	
	@Override
	public FhirTaskOutput toOpenmrsType(TaskOutputComponent taskOutputComponent) {
		FhirTaskOutput output = new FhirTaskOutput();
		Concept type = conceptTranslator.toOpenmrsType(taskOutputComponent.getType());
		output.setType(type);
		Type value = taskOutputComponent.getValue();
		if (value instanceof Reference) {
			FhirReference ref = referenceTranslator.toOpenmrsType((Reference) value);
			output.setValueReference(ref);
		} else if (value instanceof StringType) {
			output.setValueText(value.toString());
		} else if (value instanceof DecimalType) {
			output.setValueNumeric(((DecimalType) value).getValueAsNumber().doubleValue());
		} else if (value instanceof DateTimeType) {
			output.setValueDatetime(((DateTimeType) value).getValue());
		} else {
			return null;
		}
		output.setName("TaskOutputComponent/" + output.getUuid());
		return output;
	}
}
