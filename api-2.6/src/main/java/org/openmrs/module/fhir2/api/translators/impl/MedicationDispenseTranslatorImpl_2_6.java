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
			MedicationDispensePerformerComponent performerComponent = new MedicationDispensePerformerComponent();
			performerComponent.setActor(practitionerReferenceTranslator.toFhirResource(openmrsObject.getDispenser()));
			performerComponent.setFunction(null);
			fhirObject.addPerformer(performerComponent);
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
		
		openmrsObject.setUuid(fhirObject.getIdElement().getIdPart());
		openmrsObject.setPatient(patientReferenceTranslator.toOpenmrsType(fhirObject.getSubject()));
		
		return openmrsObject;
	}
}
