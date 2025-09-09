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

import static lombok.AccessLevel.PROTECTED;
import static org.apache.commons.lang3.Validate.notNull;
import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getLastUpdated;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getVersionId;

import javax.annotation.Nonnull;

import java.util.Collections;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
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
import org.openmrs.module.fhir2.api.translators.MedicationRequestReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestStatusTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;
import org.openmrs.module.fhir2.api.translators.OrderIdentifierTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MedicationRequestTranslatorImpl implements MedicationRequestTranslator {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private MedicationRequestStatusTranslator statusTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private PractitionerReferenceTranslator<Provider> practitionerReferenceTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private MedicationRequestPriorityTranslator medicationRequestPriorityTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private MedicationRequestReferenceTranslator medicationRequestReferenceTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private MedicationReferenceTranslator medicationReferenceTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ConceptTranslator conceptTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private DosageTranslator dosageTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private OrderIdentifierTranslator orderIdentifierTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
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
			medicationRequest.setPriorPrescription(
			    medicationRequestReferenceTranslator.toFhirResource((DrugOrder) drugOrder.getPreviousOrder())
			            .setIdentifier(orderIdentifierTranslator.toFhirResource(drugOrder.getPreviousOrder())));
		} else if (drugOrder.getPreviousOrder() != null && drugOrder.getAction() == Order.Action.RENEW) {
			medicationRequest.setBasedOn(Collections.singletonList(
			    medicationRequestReferenceTranslator.toFhirResource((DrugOrder) drugOrder.getPreviousOrder())
			            .setIdentifier(orderIdentifierTranslator.toFhirResource(drugOrder.getPreviousOrder()))));
		}
		
		if (drugOrder.getFulfillerStatus() != null) {
			Extension extension = new Extension();
			extension.setUrl(OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS);
			extension.setValue(new CodeType(drugOrder.getFulfillerStatus().toString()));
			medicationRequest.addExtension(extension);
		}
		
		medicationRequest.getMeta().setLastUpdated(getLastUpdated(drugOrder));
		medicationRequest.getMeta().setVersionId(getVersionId(drugOrder));
		
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
		
		if (medicationRequest.hasId()) {
			existingDrugOrder.setUuid(medicationRequest.getIdElement().getIdPart());
		}
		
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
		
		if (medicationRequest.getExtensionByUrl(OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS) != null) {
			if (!medicationRequest.getExtensionByUrl(OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS).getValue()
			        .isEmpty()) {
				existingDrugOrder.setFulfillerStatus(Order.FulfillerStatus
				        .valueOf(medicationRequest.getExtensionByUrl(OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS)
				                .getValue().toString().toUpperCase()));
			} else {
				existingDrugOrder.setFulfillerStatus(null);
			}
		}
		
		return existingDrugOrder;
		
	}
}
