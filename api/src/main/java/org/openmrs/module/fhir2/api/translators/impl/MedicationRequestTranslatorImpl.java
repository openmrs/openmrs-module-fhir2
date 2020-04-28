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

import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.DrugOrder;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.DosageTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestPriorityTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestStatusTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Setter;

@Component
@Setter(AccessLevel.PACKAGE)
public class MedicationRequestTranslatorImpl implements MedicationRequestTranslator {
	
	@Autowired
	private MedicationRequestStatusTranslator statusTranslator;
	
	@Autowired
	private PractitionerReferenceTranslator<Provider> practitionerReferenceTranslator;
	
	@Autowired
	private MedicationRequestPriorityTranslator medicationRequestPriorityTranslator;
	
	@Autowired
	private MedicationReferenceTranslator medicationReferenceTranslator;
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Autowired
	private DosageTranslator dosageTranslator;
	
	@Override
	public MedicationRequest toFhirResource(DrugOrder drugOrder) {
		MedicationRequest medicationRequest = new MedicationRequest();
		if (drugOrder == null) {
			return medicationRequest;
		}
		medicationRequest.setId(drugOrder.getUuid());
		medicationRequest.setStatus(statusTranslator.toFhirResource(drugOrder));
		
		medicationRequest.setMedication(medicationReferenceTranslator.toFhirResource(drugOrder.getDrug()));
		medicationRequest.setPriority(medicationRequestPriorityTranslator.toFhirResource(drugOrder.getUrgency()));
		medicationRequest.setRequester(practitionerReferenceTranslator.toFhirResource(drugOrder.getOrderer()));
		
		medicationRequest.addNote(new Annotation().setText(drugOrder.getCommentToFulfiller()));
		medicationRequest.addReasonCode(conceptTranslator.toFhirResource(drugOrder.getOrderReason()));
		medicationRequest.addDosageInstruction(dosageTranslator.toFhirResource(drugOrder));
		
		return medicationRequest;
	}
	
	@Override
	public DrugOrder toOpenmrsType(MedicationRequest medicationRequest) {
		return toOpenmrsType(new DrugOrder(), medicationRequest);
	}

	@Override
	public DrugOrder toOpenmrsType(DrugOrder existingDrugOrder, MedicationRequest medicationRequest) {
		if (medicationRequest == null) {
			return existingDrugOrder;
		}
		existingDrugOrder.setUuid(medicationRequest.getId());
		
		existingDrugOrder.setDrug(medicationReferenceTranslator.toOpenmrsType(medicationRequest.getMedicationReference()));
		existingDrugOrder.setUrgency(medicationRequestPriorityTranslator.toOpenmrsType(medicationRequest.getPriority()));
		existingDrugOrder.setOrderer(practitionerReferenceTranslator.toOpenmrsType(medicationRequest.getRequester()));
		
		existingDrugOrder.setCommentToFulfiller(medicationRequest.getNoteFirstRep().getText());
		existingDrugOrder.setOrderReason(conceptTranslator.toOpenmrsType(medicationRequest.getReasonCodeFirstRep()));
		
		return existingDrugOrder;
		
	}
}
