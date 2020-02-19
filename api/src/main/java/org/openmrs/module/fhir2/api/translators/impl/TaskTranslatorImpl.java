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

import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
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
		
		if (openmrsTask.getBasedOnReferences() != null) {
			fhirTask.setBasedOn(openmrsTask.getBasedOnReferences().stream().map(this::translateFromStringReference)
			        .collect(Collectors.toList()));
		}
		
		if (openmrsTask.getEncounterReference() != null) {
			fhirTask.setEncounter(translateFromStringReference(openmrsTask.getEncounterReference()));
		}
		
		if (openmrsTask.getForReference() != null) {
			fhirTask.setFor(translateFromStringReference(openmrsTask.getForReference()));
		}
		
		if (openmrsTask.getOwnerReference() != null) {
			fhirTask.setOwner(translateFromStringReference(openmrsTask.getOwnerReference()));
		}
		
		if (openmrsTask.getOutputReferences() != null) {
			fhirTask.setOutput(openmrsTask.getOutputReferences().stream().map(this::translateFromOutputReferences)
			        .collect(Collectors.toList()));
		}

		fhirTask.setAuthoredOn(openmrsTask.getDateCreated());

		fhirTask.setLastModified(openmrsTask.getDateChanged());
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
		
		if (fhirTask.getBasedOn() != null) {
			openmrsTask.setBasedOnReferences(
			    fhirTask.getBasedOn().stream().map(this::translateToStringReference).collect(Collectors.toList()));
		}
		
		if (fhirTask.getEncounter() != null) {
			openmrsTask.setEncounterReference(translateToStringReference(fhirTask.getEncounter()));
		}
		
		if (fhirTask.getFor() != null) {
			openmrsTask.setForReference(translateToStringReference(fhirTask.getFor()));
		}
		
		if (fhirTask.getOwner() != null) {
			openmrsTask.setOwnerReference(translateToStringReference(fhirTask.getOwner()));
		}
		
		if (fhirTask.getOutput() != null) {
			openmrsTask.setOutputReferences(
			    fhirTask.getOutput().stream().map(this::translateToOutputReferences).collect(Collectors.toList()));
		}
	}
	
	private Reference translateFromStringReference(String stringRef) {
		String[] splitRef = stringRef.split("/");
		
		return new Reference().setReference(splitRef[0] + "/" + splitRef[1]).setType(splitRef[0])
		        .setIdentifier(new Identifier().setValue(splitRef[1]));
	}
	
	private String translateToStringReference(Reference reference) {
		if (reference.getReference() != null) {
			return reference.getReference();
		}
		
		if (reference.getType() != null && reference.getIdentifier().getValue() != null) {
			return reference.getType() + "/" + reference.getIdentifier();
		}
		
		return null;
	}
	
	private Task.TaskOutputComponent translateFromOutputReferences(String refString) {
		Reference ref = translateFromStringReference(refString);
		return new Task.TaskOutputComponent().setType(new CodeableConcept().setText(ref.getType() + " generated"))
		        .setValue(ref);
	}
	
	private String translateToOutputReferences(Task.TaskOutputComponent taskOutputComponent) {
		return translateToStringReference((Reference) taskOutputComponent.getValue());
	}
	
}
