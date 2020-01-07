package org.openmrs.module.fhir2.api.translators.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.api.translators.TaskTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class TaskTranslatorImpl implements TaskTranslator {
	
	@Override
	public Task toFhirResource(org.openmrs.module.fhir2.Task openmrsTask) {
		Task fhirTask = new Task();
		
		if (openmrsTask != null && openmrsTask.getStatus() != null && openmrsTask.getIntent() != null) {
			fhirTask.setId(openmrsTask.getUuid());
			fhirTask.setStatus(Task.TaskStatus.valueOf(openmrsTask.getStatus().name()));
			fhirTask.setIntent(Task.TaskIntent.valueOf(openmrsTask.getIntent().name()));
		}
		
		return fhirTask;
	}
	
	@Override
	public org.openmrs.module.fhir2.Task toOpenmrsType(Task fhirTask) {
		org.openmrs.module.fhir2.Task openmrsTask = new org.openmrs.module.fhir2.Task();
		
		if (fhirTask != null && fhirTask.getStatus() != null && fhirTask.getIntent() != null) {
			openmrsTask.setUuid(fhirTask.getId());
			openmrsTask.setStatus(org.openmrs.module.fhir2.Task.TaskStatus.valueOf(fhirTask.getStatus().name()));
			openmrsTask.setIntent(org.openmrs.module.fhir2.Task.TaskIntent.valueOf(fhirTask.getIntent().name()));
		}
		
		return openmrsTask;
	}
	
	@Override
	public org.openmrs.module.fhir2.Task toOpenmrsType(org.openmrs.module.fhir2.Task existingObject, Task resource) {
		return null;
	}
}
