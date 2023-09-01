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

import static org.hl7.fhir.convertors.conv30_40.datatypes30_40.Reference30_40.convertReference;
import static org.hl7.fhir.convertors.conv30_40.datatypes30_40.complextypes30_40.Annotation30_40.convertAnnotation;
import static org.hl7.fhir.convertors.conv30_40.datatypes30_40.complextypes30_40.CodeableConcept30_40.convertCodeableConcept;
import static org.hl7.fhir.convertors.conv30_40.datatypes30_40.complextypes30_40.Identifier30_40.convertIdentifier;
import static org.hl7.fhir.convertors.conv30_40.datatypes30_40.complextypes30_40.Period30_40.convertPeriod;
import static org.hl7.fhir.convertors.conv30_40.datatypes30_40.primitivetypes30_40.DateTime30_40.convertDateTime;
import static org.hl7.fhir.convertors.conv30_40.datatypes30_40.primitivetypes30_40.PositiveInt30_40.convertPositiveInt;
import static org.hl7.fhir.convertors.conv30_40.datatypes30_40.primitivetypes30_40.String30_40.convertString;
import static org.hl7.fhir.convertors.conv30_40.datatypes30_40.primitivetypes30_40.Uri30_40.convertUri;
import static org.hl7.fhir.convertors.factory.VersionConvertorFactory_30_40.convertType;

import org.hl7.fhir.convertors.conv30_40.VersionConvertor_30_40;
import org.hl7.fhir.dstu3.model.Task;
import org.hl7.fhir.exceptions.FHIRException;

public class TaskVersionConverter {
	
	private static VersionConvertor_30_40 versionConvertor_30_40;
	
	public static org.hl7.fhir.dstu3.model.Task convertTask(org.hl7.fhir.r4.model.Task src) throws FHIRException {
		org.hl7.fhir.dstu3.model.Task tgt = new org.hl7.fhir.dstu3.model.Task();
		versionConvertor_30_40.copyDomainResource(src, tgt);
		for (org.hl7.fhir.r4.model.Identifier t : src.getIdentifier()) {
			tgt.addIdentifier(convertIdentifier(t));
		}
		//TODO: check
		if (src.hasInstantiatesUri()) {
			tgt.setDefinition(convertType(src.getInstantiatesUriElement()));
		}
		//TODO: check
		if (src.hasInstantiatesCanonical()) {
			tgt.setDefinition(convertType(src.getInstantiatesCanonicalElement()));
		}
		if (src.hasBasedOn()) {
			for (org.hl7.fhir.r4.model.Reference t : src.getBasedOn()) {
				tgt.addBasedOn(convertReference(t));
			}
		}
		if (src.hasGroupIdentifier()) {
			tgt.setGroupIdentifier(convertIdentifier(src.getGroupIdentifier()));
		}
		if (src.hasPartOf()) {
			for (org.hl7.fhir.r4.model.Reference t : src.getPartOf()) {
				tgt.addPartOf(convertReference(t));
			}
		}
		if (src.hasStatus()) {
			tgt.setStatusElement(convertTaskStatus(src.getStatusElement()));
		}
		if (src.hasStatusReason()) {
			tgt.setStatusReason(convertCodeableConcept(src.getStatusReason()));
		}
		if (src.hasBusinessStatus()) {
			tgt.setBusinessStatus(convertCodeableConcept(src.getBusinessStatus()));
		}
		if (src.hasIntent()) {
			tgt.setIntentElement(convertTaskIntent(src.getIntentElement()));
		}
		if (src.hasPriority()) {
			tgt.setPriorityElement(convertProcedureRequestPriority(src.getPriorityElement()));
		}
		if (src.hasCode()) {
			tgt.setCode(convertCodeableConcept(src.getCode()));
		}
		if (src.hasDescription()) {
			tgt.setDescriptionElement(convertString(src.getDescriptionElement()));
		}
		if (src.hasFocus()) {
			tgt.setFocus(convertReference(src.getFocus()));
		}
		if (src.hasFor()) {
			tgt.setFor(convertReference(src.getFor()));
		}
		if (src.hasEncounter()) {
			tgt.setContext(convertReference(src.getEncounter()));
		}
		if (src.hasExecutionPeriod()) {
			tgt.setExecutionPeriod(convertPeriod(src.getExecutionPeriod()));
		}
		if (src.hasAuthoredOn()) {
			tgt.setAuthoredOnElement(convertDateTime(src.getAuthoredOnElement()));
		}
		if (src.hasLastModified()) {
			tgt.setLastModifiedElement(convertDateTime(src.getLastModifiedElement()));
		}
		if (src.hasRequester()) {
			tgt.getRequester().setAgent(convertReference(src.getRequester()));
		}
		if (src.hasPerformerType()) {
			for (org.hl7.fhir.r4.model.CodeableConcept t : src.getPerformerType())
				tgt.addPerformerType(convertCodeableConcept(t));
		}
		if (src.hasOwner()) {
			tgt.setOwner(convertReference(src.getOwner()));
		}
		if (src.hasReasonCode()) {
			tgt.setReason(convertCodeableConcept(src.getReasonCode()));
		}
		if (src.hasNote()) {
			for (org.hl7.fhir.r4.model.Annotation t : src.getNote())
				tgt.addNote(convertAnnotation(t));
		}
		if (src.hasRelevantHistory()) {
			for (org.hl7.fhir.r4.model.Reference t : src.getRelevantHistory())
				tgt.addRelevantHistory(convertReference(t));
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
		versionConvertor_30_40.copyDomainResource(src, tgt);
		
		for (org.hl7.fhir.dstu3.model.Identifier t : src.getIdentifier()) {
			tgt.addIdentifier(convertIdentifier(t));
		}
		//TODO: check
		if (src.hasDefinition()) {
			tgt.setInstantiatesUri(String.valueOf(convertUri(src.getDefinitionUriType())));
		}
		//TODO: check
		if (src.hasDefinition()) {
			tgt.setInstantiatesCanonical(String.valueOf(convertReference(src.getDefinitionReference())));
		}
		if (src.hasBasedOn()) {
			for (org.hl7.fhir.dstu3.model.Reference t : src.getBasedOn()) {
				tgt.addBasedOn(convertReference(t));
			}
		}
		if (src.hasGroupIdentifier()) {
			tgt.setGroupIdentifier(convertIdentifier(src.getGroupIdentifier()));
		}
		if (src.hasPartOf()) {
			for (org.hl7.fhir.dstu3.model.Reference t : src.getPartOf()) {
				tgt.addPartOf(convertReference(t));
			}
		}
		if (src.hasStatus()) {
			tgt.setStatusElement(convertTaskStatus(src.getStatusElement()));
		}
		if (src.hasStatusReason()) {
			tgt.setStatusReason(convertCodeableConcept(src.getStatusReason()));
		}
		if (src.hasBusinessStatus()) {
			tgt.setBusinessStatus(convertCodeableConcept(src.getBusinessStatus()));
		}
		if (src.hasIntent()) {
			tgt.setIntentElement(convertTaskIntent(src.getIntentElement()));
		}
		if (src.hasPriority()) {
			tgt.setPriorityElement(convertProcedureRequestPriority(src.getPriorityElement()));
		}
		if (src.hasCode()) {
			tgt.setCode(convertCodeableConcept(src.getCode()));
		}
		if (src.hasDescription()) {
			tgt.setDescriptionElement(convertString(src.getDescriptionElement()));
		}
		if (src.hasFocus()) {
			tgt.setFocus(convertReference(src.getFocus()));
		}
		if (src.hasFor()) {
			tgt.setFor(convertReference(src.getFor()));
		}
		if (src.hasContext()) {
			tgt.setFocus(convertReference(src.getContext()));
		}
		if (src.hasExecutionPeriod()) {
			tgt.setExecutionPeriod(convertPeriod(src.getExecutionPeriod()));
		}
		if (src.hasAuthoredOn()) {
			tgt.setAuthoredOnElement(convertDateTime(src.getAuthoredOnElement()));
		}
		if (src.hasLastModified()) {
			tgt.setLastModifiedElement(convertDateTime(src.getLastModifiedElement()));
		}
		if (src.hasRequester()) {
			tgt.setRequester(convertReference(src.getRequester().getAgent()));
		}
		if (src.hasPerformerType()) {
			for (org.hl7.fhir.dstu3.model.CodeableConcept t : src.getPerformerType())
				tgt.addPerformerType(convertCodeableConcept(t));
		}
		if (src.hasOwner()) {
			tgt.setOwner(convertReference(src.getOwner()));
		}
		if (src.hasReason()) {
			tgt.setReasonCode(convertCodeableConcept(src.getReason()));
		}
		if (src.hasNote()) {
			for (org.hl7.fhir.dstu3.model.Annotation t : src.getNote())
				tgt.addNote(convertAnnotation(t));
		}
		if (src.hasRelevantHistory()) {
			for (org.hl7.fhir.dstu3.model.Reference t : src.getRelevantHistory())
				tgt.addRelevantHistory(convertReference(t));
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
			tgt.setRepetitionsElement(convertPositiveInt(src.getRepetitionsElement()));
		}
		if (src.hasPeriod()) {
			tgt.setPeriod(convertPeriod(src.getPeriod()));
		}
		if (src.hasRecipient()) {
			for (org.hl7.fhir.r4.model.Reference t : src.getRecipient())
				tgt.addRecipient(convertReference(t));
		}
		return tgt;
	}
	
	public static org.hl7.fhir.r4.model.Task.TaskRestrictionComponent convertTaskRestriction(
	        org.hl7.fhir.dstu3.model.Task.TaskRestrictionComponent src,
	        org.hl7.fhir.r4.model.Task.TaskRestrictionComponent tgt) {
		
		if (src.hasRepetitions()) {
			tgt.setRepetitionsElement(convertPositiveInt(src.getRepetitionsElement()));
		}
		if (src.hasPeriod()) {
			tgt.setPeriod(convertPeriod(src.getPeriod()));
		}
		if (src.hasRecipient()) {
			for (org.hl7.fhir.dstu3.model.Reference t : src.getRecipient())
				tgt.addRecipient(convertReference(t));
		}
		return tgt;
	}
	
	public static void convertTaskInput(org.hl7.fhir.r4.model.Task src, org.hl7.fhir.dstu3.model.Task tgt) {
		src.getInput().forEach(
		    ti -> tgt.addInput().setType(convertCodeableConcept(ti.getType())).setValue(convertType(ti.getValue())));
	}
	
	public static void convertTaskInput(org.hl7.fhir.dstu3.model.Task src, org.hl7.fhir.r4.model.Task tgt) {
		src.getInput().forEach(
		    ti -> tgt.addInput().setType(convertCodeableConcept(ti.getType())).setValue(convertType(ti.getValue())));
	}
	
	public static void convertTaskOutput(org.hl7.fhir.r4.model.Task src, org.hl7.fhir.dstu3.model.Task tgt) {
		src.getOutput().forEach(
		    to -> tgt.addOutput().setType(convertCodeableConcept(to.getType())).setValue(convertType(to.getValue())));
	}
	
	public static void convertTaskOutput(org.hl7.fhir.dstu3.model.Task src, org.hl7.fhir.r4.model.Task tgt) {
		src.getOutput().forEach(
		    to -> tgt.addOutput().setType(convertCodeableConcept(to.getType())).setValue(convertType(to.getValue())));
	}
}
