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
import org.openmrs.module.fhir2.FhirTask;
import org.openmrs.module.fhir2.api.translators.TaskTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class TaskTranslatorImpl implements TaskTranslator {
	
	@Override
	public Task toFhirResource(FhirTask openmrsTask) {
		Task fhirTask = new Task();
		
		if (openmrsTask != null) {
			setFhirTaskFields(openmrsTask, fhirTask);
		}
		
		return fhirTask;
	}
	
	@Override
	public FhirTask toOpenmrsType(Task fhirTask) {
		FhirTask openmrsTask = new FhirTask();
		
		if (fhirTask != null) {
			setOpenmrsTaskFields(openmrsTask, fhirTask);
		}
		
		return openmrsTask;
	}
	
	@Override
	public FhirTask toOpenmrsType(FhirTask openmrsTask, Task fhirTask) {
		if (fhirTask != null) {
			if (openmrsTask == null) {
				openmrsTask = new FhirTask();
			}
			setOpenmrsTaskFields(openmrsTask, fhirTask);
		}
		
		return openmrsTask;
	}
	
	private void setFhirTaskFields(FhirTask openmrsTask, Task fhirTask) {
		fhirTask.setId(openmrsTask.getUuid());
		if (openmrsTask.getStatus() != null) {
			fhirTask.setStatus(Task.TaskStatus.valueOf(openmrsTask.getStatus().name()));
		}
		if (openmrsTask.getIntent() != null) {
			fhirTask.setIntent(Task.TaskIntent.valueOf(openmrsTask.getIntent().name()));
		}
		fhirTask.getMeta().setLastUpdated(openmrsTask.getDateChanged());
	}
	
	private void setOpenmrsTaskFields(FhirTask openmrsTask, Task fhirTask) {
		if (openmrsTask.getUuid() == null) {
			openmrsTask.setUuid(fhirTask.getId());
		}
		if (fhirTask.getStatus() != null) {
			openmrsTask.setStatus(FhirTask.TaskStatus.valueOf(fhirTask.getStatus().name()));
		}
		if (fhirTask.getIntent() != null) {
			openmrsTask.setIntent(FhirTask.TaskIntent.valueOf(fhirTask.getIntent().name()));
		}
		openmrsTask.setDateChanged(fhirTask.getMeta().getLastUpdated());
	}
	
}
