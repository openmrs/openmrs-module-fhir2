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
	public TaskOutputComponent toFhirResource(FhirTaskOutput openmrsOutput) {
		CodeableConcept type = conceptTranslator.toFhirResource(openmrsOutput.getType());
		Task.TaskOutputComponent output = new Task.TaskOutputComponent().setType(type);
		if (openmrsOutput.getValueReference() != null) {
			Reference ref = referenceTranslator.toFhirResource(openmrsOutput.getValueReference());
			return output.setValue(ref);
		} else if (openmrsOutput.getValueText() != null) {
			return output.setValue(new StringType().setValue(openmrsOutput.getValueText()));
		} else if (openmrsOutput.getValueNumeric() != null) {
			DecimalType decimal = new DecimalType();
			decimal.setValue(openmrsOutput.getValueNumeric());
			return output.setValue(decimal);
		} else if (openmrsOutput.getValueDatetime() != null) {
			return output.setValue(new DateTimeType().setValue(openmrsOutput.getValueDatetime()));
		} else {
			return null;
		}
	}
	
	@Override
	public FhirTaskOutput toOpenmrsType(TaskOutputComponent taskOutputComponent) {
		FhirTaskOutput output = new FhirTaskOutput();
		Concept type = conceptTranslator.toOpenmrsType(taskOutputComponent.getType());
		output.setType(type);
		
		if (taskOutputComponent.getValue() instanceof Reference) {
			FhirReference ref = referenceTranslator.toOpenmrsType((Reference) taskOutputComponent.getValue());
			output.setValueReference(ref);
		}
		if (taskOutputComponent.getValue() instanceof StringType) {
			output.setValueText(taskOutputComponent.getValue().toString());
		}
		if (taskOutputComponent.getValue() instanceof DecimalType) {
			output.setValueNumeric(((DecimalType) taskOutputComponent.getValue()).getValueAsNumber().doubleValue());
		}
		if (taskOutputComponent.getValue() instanceof DateTimeType) {
			output.setValueDatetime(((DateTimeType) taskOutputComponent.getValue()).getValue());
		}
		
		output.setName("TaskOutputComponent/" + output.getUuid());
		return output;
	}
}
