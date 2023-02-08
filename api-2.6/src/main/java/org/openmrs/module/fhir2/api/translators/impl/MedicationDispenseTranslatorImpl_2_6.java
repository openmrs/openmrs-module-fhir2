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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.MedicationDispense.MedicationDispensePerformerComponent;
import org.hl7.fhir.r4.model.MedicationDispense.MedicationDispenseSubstitutionComponent;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.MedicationDispense;
import org.openmrs.Provider;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.LocationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationDispenseStatusTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationDispenseTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Setter(AccessLevel.PACKAGE)
@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.6.* - 2.*")
public class MedicationDispenseTranslatorImpl_2_6 implements MedicationDispenseTranslator<MedicationDispense> {
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;
	
	@Autowired
	private MedicationRequestReferenceTranslator medicationRequestReferenceTranslator;
	
	@Autowired
	private MedicationRequestTranslator medicationRequestTranslator;
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Autowired
	private LocationReferenceTranslator locationReferenceTranslator;
	
	@Autowired
	private PractitionerReferenceTranslator<Provider> practitionerReferenceTranslator;
	
	@Autowired
	private MedicationDispenseStatusTranslator medicationDispenseStatusTranslator;
	
	@Override
	public org.hl7.fhir.r4.model.MedicationDispense toFhirResource(@Nonnull MedicationDispense openmrsObject) {
		notNull(openmrsObject, "The Openmrs MedicationDispense object should not be null");
		
		org.hl7.fhir.r4.model.MedicationDispense fhirObject = new org.hl7.fhir.r4.model.MedicationDispense();
		fhirObject.setId(openmrsObject.getUuid());
		fhirObject.setSubject(patientReferenceTranslator.toFhirResource(openmrsObject.getPatient()));
		fhirObject.setContext(encounterReferenceTranslator.toFhirResource(openmrsObject.getEncounter()));
		fhirObject.addAuthorizingPrescription(
		    medicationRequestReferenceTranslator.toFhirResource(openmrsObject.getDrugOrder()));
		fhirObject.setStatus(medicationDispenseStatusTranslator.toFhirResource(openmrsObject.getStatus()));
		fhirObject.setStatusReason(conceptTranslator.toFhirResource(openmrsObject.getStatusReason()));
		fhirObject.setLocation(locationReferenceTranslator.toFhirResource(openmrsObject.getLocation()));
		fhirObject.setType(conceptTranslator.toFhirResource(openmrsObject.getType()));
		fhirObject.setWhenPrepared(openmrsObject.getDatePrepared());
		fhirObject.setWhenHandedOver(openmrsObject.getDateHandedOver());
		
		// There is significant overlap in translating between MedicationRequests and Drug Orders.
		// Create a temp drug order and use the translated fields to set Medication, Dosing, and Quantity
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setConcept(openmrsObject.getConcept());
		drugOrder.setDrug(openmrsObject.getDrug());
		drugOrder.setDose(openmrsObject.getDose());
		drugOrder.setDoseUnits(openmrsObject.getDoseUnits());
		drugOrder.setRoute(openmrsObject.getRoute());
		drugOrder.setFrequency(openmrsObject.getFrequency());
		drugOrder.setAsNeeded(openmrsObject.getAsNeeded());
		drugOrder.setDosingInstructions(openmrsObject.getDosingInstructions());
		drugOrder.setQuantity(openmrsObject.getQuantity());
		drugOrder.setQuantityUnits(openmrsObject.getQuantityUnits());
		
		MedicationRequest medDoseAndQuantity = medicationRequestTranslator.toFhirResource(drugOrder);
		if (medDoseAndQuantity != null) {
			fhirObject.setMedication(medDoseAndQuantity.getMedication());
			fhirObject.setDosageInstruction(medDoseAndQuantity.getDosageInstruction());
			if (medDoseAndQuantity.getDispenseRequest() != null) {
				fhirObject.setQuantity(medDoseAndQuantity.getDispenseRequest().getQuantity());
			}
		}
		
		if (openmrsObject.getDispenser() != null) {
			fhirObject.addPerformer().setActor(practitionerReferenceTranslator.toFhirResource(openmrsObject.getDispenser()));
		}
		
		if (openmrsObject.getWasSubstituted() != null || openmrsObject.getSubstitutionType() != null
		        || openmrsObject.getSubstitutionReason() != null) {
			MedicationDispenseSubstitutionComponent substitution = new MedicationDispenseSubstitutionComponent();
			if (openmrsObject.getWasSubstituted() != null) {
				substitution.setWasSubstituted(openmrsObject.getWasSubstituted());
			}
			substitution.setType(conceptTranslator.toFhirResource(openmrsObject.getSubstitutionType()));
			substitution.addReason(conceptTranslator.toFhirResource(openmrsObject.getSubstitutionReason()));
			fhirObject.setSubstitution(substitution);
		}
		
		fhirObject.getMeta().setLastUpdated(getLastUpdated(openmrsObject));
		
		return fhirObject;
	}
	
	@Override
	public MedicationDispense toOpenmrsType(@Nonnull org.hl7.fhir.r4.model.MedicationDispense fhirObject) {
		notNull(fhirObject, "The MedicationDispense object should not be null");
		return this.toOpenmrsType(new MedicationDispense(), fhirObject);
	}
	
	@Override
	public MedicationDispense toOpenmrsType(@Nonnull MedicationDispense openmrsObject,
	        @Nonnull org.hl7.fhir.r4.model.MedicationDispense fhirObject) {
		notNull(openmrsObject, "The existing Openmrs MedicationDispense object should not be null");
		notNull(fhirObject, "The FHIR MedicationDispense object should not be null");
		
		if (fhirObject.hasId()) {
			openmrsObject.setUuid(fhirObject.getIdElement().getIdPart());
		}
		
		openmrsObject.setPatient(patientReferenceTranslator.toOpenmrsType(fhirObject.getSubject()));
		if (fhirObject.hasContext()) {
			openmrsObject.setEncounter(encounterReferenceTranslator.toOpenmrsType(fhirObject.getContext()));
		} else {
			openmrsObject.setEncounter(null);
		}
		if (fhirObject.hasAuthorizingPrescription()) {
			Reference prescription = fhirObject.getAuthorizingPrescriptionFirstRep();
			openmrsObject.setDrugOrder(medicationRequestReferenceTranslator.toOpenmrsType(prescription));
		} else {
			openmrsObject.setDrugOrder(null);
		}
		if (fhirObject.hasStatus()) {
			openmrsObject.setStatus(medicationDispenseStatusTranslator.toOpenmrsType(fhirObject.getStatus()));
		} // status mandatory, so we don't reset it, even if null
		
		if (fhirObject.hasStatusReasonCodeableConcept()) {
			openmrsObject.setStatusReason(conceptTranslator.toOpenmrsType(fhirObject.getStatusReasonCodeableConcept()));
		} else {
			openmrsObject.setStatusReason(null);
		}
		
		if (fhirObject.hasLocation()) {
			openmrsObject.setLocation(locationReferenceTranslator.toOpenmrsType(fhirObject.getLocation()));
		} else {
			openmrsObject.setLocation(null);
		}
		
		if (fhirObject.hasType()) {
			openmrsObject.setType(conceptTranslator.toOpenmrsType(fhirObject.getType()));
		} else {
			openmrsObject.setType(null);
		}
		
		openmrsObject.setDatePrepared(fhirObject.getWhenPrepared());
		openmrsObject.setDateHandedOver(fhirObject.getWhenHandedOver());
		
		// There is significant overlap in translating between MedicationRequests and Drug Orders.
		// Use the logic in the translator for this object to translate these properties
		MedicationRequest medicationRequest = new MedicationRequest();
		if (fhirObject.hasMedication()) {
			medicationRequest.setMedication(fhirObject.getMedication());
		}
		if (fhirObject.hasDosageInstruction()) {
			medicationRequest.setDosageInstruction(fhirObject.getDosageInstruction());
		}
		if (fhirObject.hasQuantity()) {
			MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = new MedicationRequest.MedicationRequestDispenseRequestComponent();
			dispenseRequest.setQuantity(fhirObject.getQuantity());
			medicationRequest.setDispenseRequest(dispenseRequest);
		}
		
		DrugOrder drugOrder = medicationRequestTranslator.toOpenmrsType(medicationRequest);
		openmrsObject.setConcept(drugOrder.getConcept());
		openmrsObject.setDrug(drugOrder.getDrug());
		openmrsObject.setDose(drugOrder.getDose());
		openmrsObject.setDoseUnits(drugOrder.getDoseUnits());
		openmrsObject.setRoute(drugOrder.getRoute());
		openmrsObject.setFrequency(drugOrder.getFrequency());
		openmrsObject.setAsNeeded(drugOrder.getAsNeeded());
		openmrsObject.setDosingInstructions(drugOrder.getDosingInstructions());
		openmrsObject.setQuantity(drugOrder.getQuantity());
		openmrsObject.setQuantityUnits(drugOrder.getQuantityUnits());
		
		if (fhirObject.hasPerformer()) {
			MedicationDispensePerformerComponent performerComponent = fhirObject.getPerformerFirstRep();
			if (performerComponent != null && performerComponent.hasActor()) {
				openmrsObject.setDispenser(practitionerReferenceTranslator.toOpenmrsType(performerComponent.getActor()));
			} else {
				openmrsObject.setDispenser(null);
			}
		} else {
			openmrsObject.setDispenser(null);
		}
		
		if (fhirObject.hasSubstitution()) {
			MedicationDispenseSubstitutionComponent substitution = fhirObject.getSubstitution();
			openmrsObject.setWasSubstituted(substitution.getWasSubstituted());
			if (substitution.hasType()) {
				openmrsObject.setSubstitutionType(conceptTranslator.toOpenmrsType(substitution.getType()));
			} else {
				openmrsObject.setSubstitutionType(null);
			}
			if (substitution.hasReason()) {
				openmrsObject.setSubstitutionReason(conceptTranslator.toOpenmrsType(substitution.getReasonFirstRep()));
			} else {
				openmrsObject.setSubstitutionReason(null);
			}
		} else {
			openmrsObject.setWasSubstituted(null);
			openmrsObject.setSubstitutionType(null);
			openmrsObject.setSubstitutionReason(null);
		}
		
		if (fhirObject.getMeta() != null && fhirObject.getMeta().getLastUpdated() != null) {
			if (openmrsObject.getDateCreated() == null) {
				openmrsObject.setDateCreated(fhirObject.getMeta().getLastUpdated());
			} else {
				openmrsObject.setDateChanged(fhirObject.getMeta().getLastUpdated());
			}
		}
		
		return openmrsObject;
	}
}
