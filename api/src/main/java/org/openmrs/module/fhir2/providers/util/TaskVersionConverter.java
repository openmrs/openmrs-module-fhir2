/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.util;

import org.hl7.fhir.convertors.VersionConvertor_30_40;
import org.hl7.fhir.dstu3.model.Task;
import org.hl7.fhir.exceptions.FHIRException;

public class TaskVersionConverter {
	
	public static org.hl7.fhir.dstu3.model.Task convertTask(org.hl7.fhir.r4.model.Task src) throws FHIRException {
		org.hl7.fhir.dstu3.model.Task tgt = new org.hl7.fhir.dstu3.model.Task();
		VersionConvertor_30_40.copyDomainResource(src, tgt);
		for (org.hl7.fhir.r4.model.Identifier t : src.getIdentifier()) {
			tgt.addIdentifier(VersionConvertor_30_40.convertIdentifier(t));
		}
		//TODO: check
		if (src.hasInstantiatesUri()) {
			tgt.setDefinition(VersionConvertor_30_40.convertType(src.getInstantiatesUriElement()));
		}
		//TODO: check
		if (src.hasInstantiatesCanonical()) {
			tgt.setDefinition(VersionConvertor_30_40.convertType(src.getInstantiatesCanonicalElement()));
		}
		if (src.hasBasedOn()) {
			for (org.hl7.fhir.r4.model.Reference t : src.getBasedOn()) {
				tgt.addBasedOn(VersionConvertor_30_40.convertReference(t));
			}
		}
		if (src.hasGroupIdentifier()) {
			tgt.setGroupIdentifier(VersionConvertor_30_40.convertIdentifier(src.getGroupIdentifier()));
		}
		if (src.hasPartOf()) {
			for (org.hl7.fhir.r4.model.Reference t : src.getPartOf()) {
				tgt.addPartOf(VersionConvertor_30_40.convertReference(t));
			}
		}
		if (src.hasStatus()) {
			tgt.setStatusElement(convertTaskStatus(src.getStatusElement()));
		}
		if (src.hasStatusReason()) {
			tgt.setStatusReason(VersionConvertor_30_40.convertCodeableConcept(src.getStatusReason()));
		}
		if (src.hasBusinessStatus()) {
			tgt.setBusinessStatus(VersionConvertor_30_40.convertCodeableConcept(src.getBusinessStatus()));
		}
		if (src.hasIntent()) {
			tgt.setIntentElement(convertTaskIntent(src.getIntentElement()));
		}
		if (src.hasPriority()) {
			tgt.setPriorityElement(convertProcedureRequestPriority(src.getPriorityElement()));
		}
		if (src.hasCode()) {
			tgt.setCode(VersionConvertor_30_40.convertCodeableConcept(src.getCode()));
		}
		if (src.hasDescription()) {
			tgt.setDescriptionElement(VersionConvertor_30_40.convertString(src.getDescriptionElement()));
		}
		if (src.hasFocus()) {
			tgt.setFocus(VersionConvertor_30_40.convertReference(src.getFocus()));
		}
		if (src.hasFor()) {
			tgt.setFor(VersionConvertor_30_40.convertReference(src.getFor()));
		}
		if (src.hasEncounter()) {
			tgt.setContext(VersionConvertor_30_40.convertReference(src.getEncounter()));
		}
		if (src.hasExecutionPeriod()) {
			tgt.setExecutionPeriod(VersionConvertor_30_40.convertPeriod(src.getExecutionPeriod()));
		}
		if (src.hasAuthoredOn()) {
			tgt.setAuthoredOnElement(VersionConvertor_30_40.convertDateTime(src.getAuthoredOnElement()));
		}
		if (src.hasLastModified()) {
			tgt.setLastModifiedElement(VersionConvertor_30_40.convertDateTime(src.getLastModifiedElement()));
		}
		if (src.hasRequester()) {
			tgt.getRequester().setAgent(VersionConvertor_30_40.convertReference(src.getRequester()));
		}
		if (src.hasPerformerType()) {
			for (org.hl7.fhir.r4.model.CodeableConcept t : src.getPerformerType())
				tgt.addPerformerType(VersionConvertor_30_40.convertCodeableConcept(t));
		}
		if (src.hasOwner()) {
			tgt.setOwner(VersionConvertor_30_40.convertReference(src.getOwner()));
		}
		if (src.hasReasonCode()) {
			tgt.setReason(VersionConvertor_30_40.convertCodeableConcept(src.getReasonCode()));
		}
		if (src.hasNote()) {
			for (org.hl7.fhir.r4.model.Annotation t : src.getNote())
				tgt.addNote(VersionConvertor_30_40.convertAnnotation(t));
		}
		if (src.hasRelevantHistory()) {
			for (org.hl7.fhir.r4.model.Reference t : src.getRelevantHistory())
				tgt.addRelevantHistory(VersionConvertor_30_40.convertReference(t));
		}
		if (src.hasRestriction()) {
			tgt.setRestriction(convertTaskRestriction(src.getRestriction(), tgt.getRestriction()));
		}
		if (src.hasInput()) {
			convertTaskInput(src, tgt);
		}
		if (src.hasOutput()) {
			convertTaskOutput(src, tgt);
		}
		
		return tgt;
	}
	
	public static org.hl7.fhir.r4.model.Task convertTask(org.hl7.fhir.dstu3.model.Task src) throws FHIRException {
		org.hl7.fhir.r4.model.Task tgt = new org.hl7.fhir.r4.model.Task();
		VersionConvertor_30_40.copyDomainResource(src, tgt);
		
		for (org.hl7.fhir.dstu3.model.Identifier t : src.getIdentifier()) {
			tgt.addIdentifier(VersionConvertor_30_40.convertIdentifier(t));
		}
		//TODO: check
		if (src.hasDefinition()) {
			tgt.setInstantiatesUri(String.valueOf(VersionConvertor_30_40.convertUri(src.getDefinitionUriType())));
		}
		//TODO: check
		if (src.hasDefinition()) {
			tgt.setInstantiatesCanonical(
			    String.valueOf(VersionConvertor_30_40.convertReference(src.getDefinitionReference())));
		}
		if (src.hasBasedOn()) {
			for (org.hl7.fhir.dstu3.model.Reference t : src.getBasedOn()) {
				tgt.addBasedOn(VersionConvertor_30_40.convertReference(t));
			}
		}
		if (src.hasGroupIdentifier()) {
			tgt.setGroupIdentifier(VersionConvertor_30_40.convertIdentifier(src.getGroupIdentifier()));
		}
		if (src.hasPartOf()) {
			for (org.hl7.fhir.dstu3.model.Reference t : src.getPartOf()) {
				tgt.addPartOf(VersionConvertor_30_40.convertReference(t));
			}
		}
		if (src.hasStatus()) {
			tgt.setStatusElement(convertTaskStatus(src.getStatusElement()));
		}
		if (src.hasStatusReason()) {
			tgt.setStatusReason(VersionConvertor_30_40.convertCodeableConcept(src.getStatusReason()));
		}
		if (src.hasBusinessStatus()) {
			tgt.setBusinessStatus(VersionConvertor_30_40.convertCodeableConcept(src.getBusinessStatus()));
		}
		if (src.hasIntent()) {
			tgt.setIntentElement(convertTaskIntent(src.getIntentElement()));
		}
		if (src.hasPriority()) {
			tgt.setPriorityElement(convertProcedureRequestPriority(src.getPriorityElement()));
		}
		if (src.hasCode()) {
			tgt.setCode(VersionConvertor_30_40.convertCodeableConcept(src.getCode()));
		}
		if (src.hasDescription()) {
			tgt.setDescriptionElement(VersionConvertor_30_40.convertString(src.getDescriptionElement()));
		}
		if (src.hasFocus()) {
			tgt.setFocus(VersionConvertor_30_40.convertReference(src.getFocus()));
		}
		if (src.hasFor()) {
			tgt.setFor(VersionConvertor_30_40.convertReference(src.getFor()));
		}
		if (src.hasContext()) {
			tgt.setFocus(VersionConvertor_30_40.convertReference(src.getContext()));
		}
		if (src.hasExecutionPeriod()) {
			tgt.setExecutionPeriod(VersionConvertor_30_40.convertPeriod(src.getExecutionPeriod()));
		}
		if (src.hasAuthoredOn()) {
			tgt.setAuthoredOnElement(VersionConvertor_30_40.convertDateTime(src.getAuthoredOnElement()));
		}
		if (src.hasLastModified()) {
			tgt.setLastModifiedElement(VersionConvertor_30_40.convertDateTime(src.getLastModifiedElement()));
		}
		if (src.hasRequester()) {
			tgt.setRequester(VersionConvertor_30_40.convertReference(src.getRequester().getAgent()));
		}
		if (src.hasPerformerType()) {
			for (org.hl7.fhir.dstu3.model.CodeableConcept t : src.getPerformerType())
				tgt.addPerformerType(VersionConvertor_30_40.convertCodeableConcept(t));
		}
		if (src.hasOwner()) {
			tgt.setOwner(VersionConvertor_30_40.convertReference(src.getOwner()));
		}
		if (src.hasReason()) {
			tgt.setReasonCode(VersionConvertor_30_40.convertCodeableConcept(src.getReason()));
		}
		if (src.hasNote()) {
			for (org.hl7.fhir.dstu3.model.Annotation t : src.getNote())
				tgt.addNote(VersionConvertor_30_40.convertAnnotation(t));
		}
		if (src.hasRelevantHistory()) {
			for (org.hl7.fhir.dstu3.model.Reference t : src.getRelevantHistory())
				tgt.addRelevantHistory(VersionConvertor_30_40.convertReference(t));
		}
		if (src.hasRestriction()) {
			tgt.setRestriction(convertTaskRestriction(src.getRestriction(), tgt.getRestriction()));
		}
		if (src.hasInput()) {
			convertTaskInput(src, tgt);
		}
		if (src.hasOutput()) {
			convertTaskOutput(src, tgt);
		}
		
		return tgt;
	}
	
	public static org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.Task.TaskStatus> convertTaskStatus(
	        org.hl7.fhir.dstu3.model.Enumeration<org.hl7.fhir.dstu3.model.Task.TaskStatus> src) throws FHIRException {
		if (src == null || src.isEmpty())
			return null;
		org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.Task.TaskStatus> tgt = new org.hl7.fhir.r4.model.Enumeration<>(
		        new org.hl7.fhir.r4.model.Task.TaskStatusEnumFactory());
		switch (src.getValue()) {
			case DRAFT:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskStatus.DRAFT);
				break;
			case REQUESTED:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskStatus.REQUESTED);
				break;
			case RECEIVED:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskStatus.RECEIVED);
				break;
			case ACCEPTED:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskStatus.ACCEPTED);
				break;
			case REJECTED:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskStatus.REJECTED);
				break;
			case READY:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskStatus.READY);
				break;
			case CANCELLED:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskStatus.CANCELLED);
				break;
			case INPROGRESS:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskStatus.INPROGRESS);
				break;
			case ONHOLD:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskStatus.ONHOLD);
				break;
			case FAILED:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskStatus.FAILED);
				break;
			case COMPLETED:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskStatus.COMPLETED);
				break;
			case ENTEREDINERROR:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskStatus.ENTEREDINERROR);
				break;
			case NULL:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskStatus.NULL);
				break;
			default:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskStatus.NULL);
				break;
		}
		return tgt;
	}
	
	public static org.hl7.fhir.dstu3.model.Enumeration<Task.TaskStatus> convertTaskStatus(
	        org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.Task.TaskStatus> src) throws FHIRException {
		if (src == null || src.isEmpty())
			return null;
		org.hl7.fhir.dstu3.model.Enumeration<Task.TaskStatus> tgt = new org.hl7.fhir.dstu3.model.Enumeration<>(
		        new Task.TaskStatusEnumFactory());
		switch (src.getValue()) {
			case DRAFT:
				tgt.setValue(Task.TaskStatus.DRAFT);
				break;
			case REQUESTED:
				tgt.setValue(Task.TaskStatus.REQUESTED);
				break;
			case RECEIVED:
				tgt.setValue(Task.TaskStatus.RECEIVED);
				break;
			case ACCEPTED:
				tgt.setValue(Task.TaskStatus.ACCEPTED);
				break;
			case REJECTED:
				tgt.setValue(Task.TaskStatus.REJECTED);
				break;
			case READY:
				tgt.setValue(Task.TaskStatus.READY);
				break;
			case CANCELLED:
				tgt.setValue(Task.TaskStatus.CANCELLED);
				break;
			case INPROGRESS:
				tgt.setValue(Task.TaskStatus.INPROGRESS);
				break;
			case ONHOLD:
				tgt.setValue(Task.TaskStatus.ONHOLD);
				break;
			case FAILED:
				tgt.setValue(Task.TaskStatus.FAILED);
				break;
			case COMPLETED:
				tgt.setValue(Task.TaskStatus.COMPLETED);
				break;
			case ENTEREDINERROR:
				tgt.setValue(Task.TaskStatus.ENTEREDINERROR);
				break;
			case NULL:
				tgt.setValue(Task.TaskStatus.NULL);
				break;
			default:
				tgt.setValue(Task.TaskStatus.NULL);
				break;
		}
		return tgt;
	}
	
	public static org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.Task.TaskIntent> convertTaskIntent(
	        org.hl7.fhir.dstu3.model.Enumeration<org.hl7.fhir.dstu3.model.Task.TaskIntent> src) throws FHIRException {
		if (src == null || src.isEmpty())
			return null;
		org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.Task.TaskIntent> tgt = new org.hl7.fhir.r4.model.Enumeration<>(
		        new org.hl7.fhir.r4.model.Task.TaskIntentEnumFactory());
		switch (src.getValue()) {
			case PROPOSAL:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskIntent.PROPOSAL);
				break;
			case PLAN:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskIntent.PLAN);
				break;
			case ORDER:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskIntent.ORDER);
				break;
			case ORIGINALORDER:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskIntent.ORIGINALORDER);
				break;
			case REFLEXORDER:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskIntent.REFLEXORDER);
				break;
			case FILLERORDER:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskIntent.FILLERORDER);
				break;
			case INSTANCEORDER:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskIntent.INSTANCEORDER);
				break;
			case OPTION:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskIntent.OPTION);
				break;
			case NULL:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskIntent.NULL);
				break;
			default:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskIntent.NULL);
				break;
		}
		return tgt;
	}
	
	public static org.hl7.fhir.dstu3.model.Enumeration<Task.TaskIntent> convertTaskIntent(
	        org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.Task.TaskIntent> src) throws FHIRException {
		if (src == null || src.isEmpty())
			return null;
		org.hl7.fhir.dstu3.model.Enumeration<org.hl7.fhir.dstu3.model.Task.TaskIntent> tgt = new org.hl7.fhir.dstu3.model.Enumeration<>(
		        new Task.TaskIntentEnumFactory());
		switch (src.getValue()) {
			case PROPOSAL:
				tgt.setValue(Task.TaskIntent.PROPOSAL);
				break;
			case PLAN:
				tgt.setValue(Task.TaskIntent.PLAN);
				break;
			case ORDER:
				tgt.setValue(Task.TaskIntent.ORDER);
				break;
			case ORIGINALORDER:
				tgt.setValue(Task.TaskIntent.ORIGINALORDER);
				break;
			case REFLEXORDER:
				tgt.setValue(Task.TaskIntent.REFLEXORDER);
				break;
			case FILLERORDER:
				tgt.setValue(Task.TaskIntent.FILLERORDER);
				break;
			case INSTANCEORDER:
				tgt.setValue(Task.TaskIntent.INSTANCEORDER);
				break;
			case OPTION:
				tgt.setValue(Task.TaskIntent.OPTION);
				break;
			case NULL:
				tgt.setValue(Task.TaskIntent.NULL);
				break;
			default:
				tgt.setValue(Task.TaskIntent.NULL);
				break;
		}
		return tgt;
	}
	
	public static org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.Task.TaskPriority> convertProcedureRequestPriority(
	        org.hl7.fhir.dstu3.model.Enumeration<org.hl7.fhir.dstu3.model.Task.TaskPriority> src) throws FHIRException {
		if (src == null || src.isEmpty())
			return null;
		org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.Task.TaskPriority> tgt = new org.hl7.fhir.r4.model.Enumeration<>(
		        new org.hl7.fhir.r4.model.Task.TaskPriorityEnumFactory());
		switch (src.getValue()) {
			case ROUTINE:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskPriority.ROUTINE);
				break;
			case URGENT:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskPriority.URGENT);
				break;
			case ASAP:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskPriority.ASAP);
				break;
			case STAT:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskPriority.STAT);
				break;
			case NULL:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskPriority.NULL);
				break;
			default:
				tgt.setValue(org.hl7.fhir.r4.model.Task.TaskPriority.NULL);
				break;
		}
		return tgt;
	}
	
	public static org.hl7.fhir.dstu3.model.Enumeration<Task.TaskPriority> convertProcedureRequestPriority(
	        org.hl7.fhir.r4.model.Enumeration<org.hl7.fhir.r4.model.Task.TaskPriority> src) throws FHIRException {
		if (src == null || src.isEmpty())
			return null;
		org.hl7.fhir.dstu3.model.Enumeration<org.hl7.fhir.dstu3.model.Task.TaskPriority> tgt = new org.hl7.fhir.dstu3.model.Enumeration<>(
		        new Task.TaskPriorityEnumFactory());
		switch (src.getValue()) {
			case ROUTINE:
				tgt.setValue(org.hl7.fhir.dstu3.model.Task.TaskPriority.ROUTINE);
				break;
			case URGENT:
				tgt.setValue(org.hl7.fhir.dstu3.model.Task.TaskPriority.URGENT);
				break;
			case ASAP:
				tgt.setValue(org.hl7.fhir.dstu3.model.Task.TaskPriority.ASAP);
				break;
			case STAT:
				tgt.setValue(org.hl7.fhir.dstu3.model.Task.TaskPriority.STAT);
				break;
			case NULL:
				tgt.setValue(org.hl7.fhir.dstu3.model.Task.TaskPriority.NULL);
				break;
			default:
				tgt.setValue(org.hl7.fhir.dstu3.model.Task.TaskPriority.NULL);
				break;
		}
		return tgt;
	}
	
	public static Task.TaskRestrictionComponent convertTaskRestriction(
	        org.hl7.fhir.r4.model.Task.TaskRestrictionComponent src,
	        org.hl7.fhir.dstu3.model.Task.TaskRestrictionComponent tgt) {
		
		if (src.hasRepetitions()) {
			tgt.setRepetitionsElement(VersionConvertor_30_40.convertPositiveInt(src.getRepetitionsElement()));
		}
		if (src.hasPeriod()) {
			tgt.setPeriod(VersionConvertor_30_40.convertPeriod(src.getPeriod()));
		}
		if (src.hasRecipient()) {
			for (org.hl7.fhir.r4.model.Reference t : src.getRecipient())
				tgt.addRecipient(VersionConvertor_30_40.convertReference(t));
		}
		return tgt;
	}
	
	public static org.hl7.fhir.r4.model.Task.TaskRestrictionComponent convertTaskRestriction(
	        org.hl7.fhir.dstu3.model.Task.TaskRestrictionComponent src,
	        org.hl7.fhir.r4.model.Task.TaskRestrictionComponent tgt) {
		
		if (src.hasRepetitions()) {
			tgt.setRepetitionsElement(VersionConvertor_30_40.convertPositiveInt(src.getRepetitionsElement()));
		}
		if (src.hasPeriod()) {
			tgt.setPeriod(VersionConvertor_30_40.convertPeriod(src.getPeriod()));
		}
		if (src.hasRecipient()) {
			for (org.hl7.fhir.dstu3.model.Reference t : src.getRecipient())
				tgt.addRecipient(VersionConvertor_30_40.convertReference(t));
		}
		return tgt;
	}
	
	public static void convertTaskInput(org.hl7.fhir.r4.model.Task src, org.hl7.fhir.dstu3.model.Task tgt) {
		src.getInput().forEach(ti -> tgt.addInput().setType(VersionConvertor_30_40.convertCodeableConcept(ti.getType()))
		        .setValue(VersionConvertor_30_40.convertType(ti.getValue())));
	}
	
	public static void convertTaskInput(org.hl7.fhir.dstu3.model.Task src, org.hl7.fhir.r4.model.Task tgt) {
		src.getOutput().forEach(ti -> tgt.addOutput().setType(VersionConvertor_30_40.convertCodeableConcept(ti.getType()))
		        .setValue(VersionConvertor_30_40.convertType(ti.getValue())));
	}
	
	public static void convertTaskOutput(org.hl7.fhir.r4.model.Task src, org.hl7.fhir.dstu3.model.Task tgt) {
		tgt.addInput().setType(VersionConvertor_30_40.convertCodeableConcept(src.getInput().iterator().next().getType()));
		tgt.addInput().setValue(VersionConvertor_30_40.convertType(src.getInput().iterator().next().getValue()));
	}
	
	public static void convertTaskOutput(org.hl7.fhir.dstu3.model.Task src, org.hl7.fhir.r4.model.Task tgt) {
		tgt.addOutput().setType(VersionConvertor_30_40.convertCodeableConcept(src.getInput().iterator().next().getType()));
		tgt.addOutput().setValue(VersionConvertor_30_40.convertType(src.getInput().iterator().next().getValue()));
	}
}
