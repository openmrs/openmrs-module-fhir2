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

import static org.apache.commons.lang3.Validate.notNull;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getLastUpdated;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getVersionId;

import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.TaskInputTranslator;
import org.openmrs.module.fhir2.api.translators.TaskOutputTranslator;
import org.openmrs.module.fhir2.api.translators.TaskTranslator;
import org.openmrs.module.fhir2.model.FhirTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class TaskTranslatorImpl implements TaskTranslator {
	
	@Autowired
	private ReferenceTranslator referenceTranslator;
	
	@Autowired
	private TaskInputTranslator taskInputTranslator;
	
	@Autowired
	private TaskOutputTranslator taskOutputTranslator;
	
	@Override
	public Task toFhirResource(@Nonnull FhirTask openmrsTask) {
		notNull(openmrsTask, "The openmrsTask object should not be null");
		
		Task fhirTask = new Task();
		setFhirTaskFields(openmrsTask, fhirTask);
		
		return fhirTask;
	}
	
	@Override
	public FhirTask toOpenmrsType(@Nonnull Task fhirTask) {
		notNull(fhirTask, "The Task object should not be null");
		
		FhirTask openmrsTask = new FhirTask();
		setOpenmrsTaskFields(openmrsTask, fhirTask);
		
		return openmrsTask;
	}
	
	@Override
	public FhirTask toOpenmrsType(@Nonnull FhirTask openmrsTask, @Nonnull Task fhirTask) {
		notNull(openmrsTask, "The existing openmrsTask object should not be null");
		notNull(fhirTask, "The Task object should not be null");
		
		setOpenmrsTaskFields(openmrsTask, fhirTask);
		
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
		
		if (openmrsTask.getBasedOnReferences() != null && !openmrsTask.getBasedOnReferences().isEmpty()) {
			fhirTask.setBasedOn(openmrsTask.getBasedOnReferences().stream().map(referenceTranslator::toFhirResource)
			        .collect(Collectors.toList()));
		}
		
		if (openmrsTask.getEncounterReference() != null) {
			fhirTask.setEncounter(referenceTranslator.toFhirResource(openmrsTask.getEncounterReference()));
		}
		
		if (openmrsTask.getForReference() != null) {
			fhirTask.setFor(referenceTranslator.toFhirResource(openmrsTask.getForReference()));
		}
		
		if (openmrsTask.getOwnerReference() != null) {
			fhirTask.setOwner(referenceTranslator.toFhirResource(openmrsTask.getOwnerReference()));
		}
		
		if (openmrsTask.getLocationReference() != null) {
			fhirTask.setLocation(referenceTranslator.toFhirResource(openmrsTask.getLocationReference()));
		}
		
		if (openmrsTask.getInput() != null && !openmrsTask.getInput().isEmpty()) {
			fhirTask.setInput(openmrsTask.getInput().stream().map(taskInputTranslator::toFhirResource)
			        .filter(Objects::nonNull).collect(Collectors.toList()));
		}
		
		if (openmrsTask.getOutput() != null && !openmrsTask.getOutput().isEmpty()) {
			fhirTask.setOutput(openmrsTask.getOutput().stream().map(taskOutputTranslator::toFhirResource)
			        .filter(Objects::nonNull).collect(Collectors.toList()));
		}
		
		fhirTask.setAuthoredOn(openmrsTask.getDateCreated());
		
		if (openmrsTask.getDateChanged() != null) {
			fhirTask.setLastModified(openmrsTask.getDateChanged());
		} else {
			fhirTask.setLastModified(openmrsTask.getDateCreated());
		}
		
		fhirTask.setIdentifier(Collections.singletonList(
		    new Identifier().setSystem(FhirConstants.OPENMRS_FHIR_EXT_TASK_IDENTIFIER).setValue(openmrsTask.getUuid())));
		
		fhirTask.getMeta().setLastUpdated(getLastUpdated(openmrsTask));
		fhirTask.getMeta().setVersionId(getVersionId(openmrsTask));
	}
	
	private void setOpenmrsTaskFields(FhirTask openmrsTask, Task fhirTask) {
		if (openmrsTask.getUuid() == null) {
			openmrsTask.setUuid(fhirTask.getId());
		}
		if (fhirTask.hasStatus()) {
			try {
				openmrsTask.setStatus(FhirTask.TaskStatus.valueOf(fhirTask.getStatus().name()));
			}
			catch (IllegalArgumentException ex) {
				openmrsTask.setStatus(FhirTask.TaskStatus.UNKNOWN);
			}
		}
		if (fhirTask.hasIntent()) {
			try {
				openmrsTask.setIntent(FhirTask.TaskIntent.valueOf(fhirTask.getIntent().name()));
			}
			catch (IllegalArgumentException ex) {
				openmrsTask.setIntent(FhirTask.TaskIntent.ORDER);
			}
		}
		
		if (!fhirTask.getBasedOn().isEmpty()) {
			openmrsTask.setBasedOnReferences(
			    fhirTask.getBasedOn().stream().map(referenceTranslator::toOpenmrsType).collect(Collectors.toSet()));
		}
		
		if (!fhirTask.getEncounter().isEmpty()) {
			openmrsTask.setEncounterReference(referenceTranslator.toOpenmrsType(fhirTask.getEncounter()));
		}
		
		if (!fhirTask.getFor().isEmpty()) {
			openmrsTask.setForReference(referenceTranslator.toOpenmrsType(fhirTask.getFor()));
		}
		
		if (!fhirTask.getOwner().isEmpty()) {
			openmrsTask.setOwnerReference(referenceTranslator.toOpenmrsType(fhirTask.getOwner()));
		}
		
		if (!fhirTask.getLocation().isEmpty()) {
			openmrsTask.setLocationReference(referenceTranslator.toOpenmrsType(fhirTask.getLocation()));
		}
		
		if (!fhirTask.getInput().isEmpty()) {
			openmrsTask.setInput(fhirTask.getInput().stream().map(taskInputTranslator::toOpenmrsType)
			        .filter(Objects::nonNull).collect(Collectors.toSet()));
		}
		
		if (!fhirTask.getOutput().isEmpty()) {
			openmrsTask.setOutput(fhirTask.getOutput().stream().map(taskOutputTranslator::toOpenmrsType)
			        .filter(Objects::nonNull).collect(Collectors.toSet()));
		}
		
		openmrsTask.setName(FhirConstants.TASK + "/" + fhirTask.getId());
		
	}
	
}
