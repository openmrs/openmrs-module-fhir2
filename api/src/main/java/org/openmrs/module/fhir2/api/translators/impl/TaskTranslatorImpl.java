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
import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.api.translators.TaskTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class TaskTranslatorImpl implements TaskTranslator {
	
	@Override
	public Task toFhirResource(org.openmrs.module.fhir2.Task openmrsTask) {
		Task fhirTask = new Task();
		
		if (openmrsTask != null) {
			setFhirTaskFields(openmrsTask, fhirTask);
		}
		
		return fhirTask;
	}
	
	@Override
	public org.openmrs.module.fhir2.Task toOpenmrsType(Task fhirTask) {
		org.openmrs.module.fhir2.Task openmrsTask = new org.openmrs.module.fhir2.Task();
		
		if (fhirTask != null) {
			setOpenmrsTaskFields(openmrsTask, fhirTask);
		}
		
		return openmrsTask;
	}
	
	@Override
	public org.openmrs.module.fhir2.Task toOpenmrsType(org.openmrs.module.fhir2.Task openmrsTask, Task fhirTask) {
		if (fhirTask != null) {
			if (openmrsTask == null) {
				openmrsTask = new org.openmrs.module.fhir2.Task();
			}
			setOpenmrsTaskFields(openmrsTask, fhirTask);
		}
		
		return openmrsTask;
	}
	
	private void setFhirTaskFields(org.openmrs.module.fhir2.Task openmrsTask, Task fhirTask) {
		fhirTask.setId(openmrsTask.getUuid());
		if (openmrsTask.getStatus() != null) {
			fhirTask.setStatus(Task.TaskStatus.valueOf(openmrsTask.getStatus().name()));
		}
		if (openmrsTask.getIntent() != null) {
			fhirTask.setIntent(Task.TaskIntent.valueOf(openmrsTask.getIntent().name()));
		}
	}
	
	private void setOpenmrsTaskFields(org.openmrs.module.fhir2.Task openmrsTask, Task fhirTask) {
		if (openmrsTask.getUuid() == null) {
			openmrsTask.setUuid(fhirTask.getId());
		}
		if (fhirTask.getStatus() != null) {
			openmrsTask.setStatus(org.openmrs.module.fhir2.Task.TaskStatus.valueOf(fhirTask.getStatus().name()));
		}
		if (fhirTask.getIntent() != null) {
			openmrsTask.setIntent(org.openmrs.module.fhir2.Task.TaskIntent.valueOf(fhirTask.getIntent().name()));
		}
	}
	
}
