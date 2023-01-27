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

import javax.annotation.Nonnull;

import java.util.Collections;

import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.DosageTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestDispenseRequestComponentTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestPriorityTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestStatusTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;
import org.openmrs.module.fhir2.api.translators.OrderIdentifierTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class MedicationRequestTranslatorImpl extends BaseReferenceHandlingTranslator implements MedicationRequestTranslator {
	
	@Autowired
	private MedicationRequestStatusTranslator statusTranslator;
	
	@Autowired
	private PractitionerReferenceTranslator<Provider> practitionerReferenceTranslator;
	
	@Autowired
	private MedicationRequestPriorityTranslator medicationRequestPriorityTranslator;
	
	@Autowired
	private MedicationReferenceTranslator medicationReferenceTranslator;
	
	@Autowired
	private EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Autowired
	private DosageTranslator dosageTranslator;
	
	@Autowired
	private OrderIdentifierTranslator orderIdentifierTranslator;
	
	@Autowired
	private MedicationRequestDispenseRequestComponentTranslator medicationRequestDispenseRequestComponentTranslator;
	
	@Override
	public MedicationRequest toFhirResource(@Nonnull DrugOrder drugOrder) {
		notNull(drugOrder, "The DrugOrder object should not be null");
		
		MedicationRequest medicationRequest = new MedicationRequest();
		medicationRequest.setId(drugOrder.getUuid());
		medicationRequest.setAuthoredOn(drugOrder.getDateCreated());
		medicationRequest.setStatus(statusTranslator.toFhirResource(drugOrder));
		
		if (drugOrder.getDrug() != null) {
			medicationRequest.setMedication(medicationReferenceTranslator.toFhirResource(drugOrder.getDrug()));
		} else {
			CodeableConcept medicationConcept = conceptTranslator.toFhirResource(drugOrder.getConcept());
			if (StringUtils.isNotBlank(drugOrder.getDrugNonCoded())) {
				medicationConcept.setText(drugOrder.getDrugNonCoded());
			}
			medicationRequest.setMedication(medicationConcept);
		}
		
		if (drugOrder.getUrgency() != null) {
			medicationRequest.setPriority(medicationRequestPriorityTranslator.toFhirResource(drugOrder.getUrgency()));
		}
		medicationRequest.setRequester(practitionerReferenceTranslator.toFhirResource(drugOrder.getOrderer()));
		medicationRequest.setEncounter(encounterReferenceTranslator.toFhirResource(drugOrder.getEncounter()));
		medicationRequest.setSubject(patientReferenceTranslator.toFhirResource(drugOrder.getPatient()));
		
		medicationRequest.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
		medicationRequest.addNote(new Annotation().setText(drugOrder.getCommentToFulfiller()));
		medicationRequest.addReasonCode(conceptTranslator.toFhirResource(drugOrder.getOrderReason()));
		medicationRequest.addDosageInstruction(dosageTranslator.toFhirResource(drugOrder));
		
		medicationRequest.setDispenseRequest(medicationRequestDispenseRequestComponentTranslator.toFhirResource(drugOrder));
		
		if (drugOrder.getPreviousOrder() != null
		        && (drugOrder.getAction() == Order.Action.DISCONTINUE || drugOrder.getAction() == Order.Action.REVISE)) {
			medicationRequest.setPriorPrescription(createOrderReference(drugOrder.getPreviousOrder())
			        .setIdentifier(orderIdentifierTranslator.toFhirResource(drugOrder.getPreviousOrder())));
		} else if (drugOrder.getPreviousOrder() != null && drugOrder.getAction() == Order.Action.RENEW) {
			medicationRequest.setBasedOn(Collections.singletonList(createOrderReference(drugOrder.getPreviousOrder())
			        .setIdentifier(orderIdentifierTranslator.toFhirResource(drugOrder.getPreviousOrder()))));
		}
		
		medicationRequest.getMeta().setLastUpdated(getLastUpdated(drugOrder));
		
		return medicationRequest;
	}
	
	@Override
	public DrugOrder toOpenmrsType(@Nonnull MedicationRequest medicationRequest) {
		notNull(medicationRequest, "The MedicationRequest object should not be null");
		return toOpenmrsType(new DrugOrder(), medicationRequest);
	}
	
	@Override
	public DrugOrder toOpenmrsType(@Nonnull DrugOrder existingDrugOrder, @Nonnull MedicationRequest medicationRequest) {
		notNull(existingDrugOrder, "The existing DrugOrder object should not be null");
		notNull(medicationRequest, "The MedicationRequest object should not be null");
		
		existingDrugOrder.setUuid(medicationRequest.getId());
		existingDrugOrder.setDateCreated(medicationRequest.getAuthoredOn());
		
		if (medicationRequest.hasMedicationReference()) {
			Drug drug = medicationReferenceTranslator.toOpenmrsType(medicationRequest.getMedicationReference());
			existingDrugOrder.setDrug(drug);
		} else {
			CodeableConcept codeableConcept = medicationRequest.getMedicationCodeableConcept();
			Concept concept = conceptTranslator.toOpenmrsType(codeableConcept);
			existingDrugOrder.setConcept(concept);
			if (codeableConcept.getText() != null) {
				CodeableConcept referenceConcept = conceptTranslator.toFhirResource(concept);
				if (!codeableConcept.getText().equals(referenceConcept.getText())) {
					existingDrugOrder.setDrugNonCoded(codeableConcept.getText());
				}
			}
		}
		
		if (medicationRequest.getPriority() != null) {
			existingDrugOrder.setUrgency(medicationRequestPriorityTranslator.toOpenmrsType(medicationRequest.getPriority()));
		}
		existingDrugOrder.setOrderer(practitionerReferenceTranslator.toOpenmrsType(medicationRequest.getRequester()));
		existingDrugOrder.setEncounter(encounterReferenceTranslator.toOpenmrsType(medicationRequest.getEncounter()));
		existingDrugOrder.setPatient(patientReferenceTranslator.toOpenmrsType(medicationRequest.getSubject()));
		
		existingDrugOrder.setCommentToFulfiller(medicationRequest.getNoteFirstRep().getText());
		existingDrugOrder.setOrderReason(conceptTranslator.toOpenmrsType(medicationRequest.getReasonCodeFirstRep()));
		dosageTranslator.toOpenmrsType(existingDrugOrder, medicationRequest.getDosageInstructionFirstRep());
		
		medicationRequestDispenseRequestComponentTranslator.toOpenmrsType(existingDrugOrder,
		    medicationRequest.getDispenseRequest());
		
		return existingDrugOrder;
		
	}
}
