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

import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.DrugOrder;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.api.translators.MedicationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestPriorityTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestStatusTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class MedicationRequestTranslatorImpl implements MedicationRequestTranslator {
	
	@Inject
	private MedicationRequestStatusTranslator statusTranslator;
	
	@Inject
	private PractitionerReferenceTranslator<Provider> practitionerReferenceTranslator;
	
	@Inject
	private MedicationRequestPriorityTranslator medicationRequestPriorityTranslator;
	
	@Inject
	private MedicationReferenceTranslator medicationReferenceTranslator;
	
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
		
		return medicationRequest;
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
		
		return existingDrugOrder;
		
	}
}
