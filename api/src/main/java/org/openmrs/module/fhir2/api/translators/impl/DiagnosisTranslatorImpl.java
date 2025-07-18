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
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getLastUpdated;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getVersionId;

import javax.annotation.Nonnull;

import java.util.Optional;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.DiagnosisTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisTranslatorImpl implements DiagnosisTranslator {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ConceptTranslator conceptTranslator;
	
	// FHIR Extension URLs for OpenMRS-specific diagnosis fields
	private static final String DIAGNOSIS_RANK_EXTENSION = "http://openmrs.org/fhir/StructureDefinition/diagnosis-rank";
	
	private static final String DIAGNOSIS_CERTAINTY_EXTENSION = "http://openmrs.org/fhir/StructureDefinition/diagnosis-certainty";
	
	@Override
	public org.hl7.fhir.r4.model.Condition toFhirResource(@Nonnull Diagnosis diagnosis) {
		notNull(diagnosis, "The OpenMRS Diagnosis object should not be null");
		
		org.hl7.fhir.r4.model.Condition fhirCondition = new org.hl7.fhir.r4.model.Condition();
		fhirCondition.setId(diagnosis.getUuid());
		
		CodeableConcept category = new CodeableConcept();
		category.addCoding(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/condition-category")
		        .setCode("encounter-diagnosis").setDisplay("Encounter Diagnosis"));
		fhirCondition.addCategory(category);
		
		// Set patient reference
		if (diagnosis.getPatient() != null) {
			fhirCondition.setSubject(patientReferenceTranslator.toFhirResource(diagnosis.getPatient()));
		}
		
		// Set encounter reference - specific to diagnosis
		if (diagnosis.getEncounter() != null) {
			fhirCondition.setEncounter(encounterReferenceTranslator.toFhirResource(diagnosis.getEncounter()));
		}
		
		// Set diagnosis concept/code
		if (diagnosis.getDiagnosis() != null) {
			if (diagnosis.getDiagnosis().getCoded() != null) {
				fhirCondition.setCode(conceptTranslator.toFhirResource(diagnosis.getDiagnosis().getCoded()));
			}
			
			// Handle non-coded diagnosis
			if (diagnosis.getDiagnosis().getNonCoded() != null) {
				Extension extension = new Extension();
				extension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_NON_CODED_CONDITION);
				extension.setValue(new StringType(diagnosis.getDiagnosis().getNonCoded()));
				fhirCondition.addExtension(extension);
			}
		}
		
		// Set clinical status based on diagnosis voided status
		CodeableConcept clinicalStatus = new CodeableConcept();
		if (diagnosis.getVoided()) {
			clinicalStatus.addCoding(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical")
			        .setCode("inactive").setDisplay("Inactive"));
		} else {
			clinicalStatus.addCoding(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical")
			        .setCode("active").setDisplay("Active"));
		}
		fhirCondition.setClinicalStatus(clinicalStatus);
		
		// Set verification status based on certainty
		CodeableConcept verificationStatus = new CodeableConcept();
		if (diagnosis.getCertainty() != null) {
			switch (diagnosis.getCertainty()) {
				case CONFIRMED:
					verificationStatus
					        .addCoding(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/condition-ver-status")
					                .setCode("confirmed").setDisplay("Confirmed"));
					break;
				case PROVISIONAL:
					verificationStatus
					        .addCoding(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/condition-ver-status")
					                .setCode("provisional").setDisplay("Provisional"));
					break;
				default:
					verificationStatus
					        .addCoding(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/condition-ver-status")
					                .setCode("unconfirmed").setDisplay("Unconfirmed"));
					break;
			}
		} else {
			verificationStatus.addCoding(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/condition-ver-status")
			        .setCode("unconfirmed").setDisplay("Unconfirmed"));
		}
		fhirCondition.setVerificationStatus(verificationStatus);
		
		// Add diagnosis-specific extensions
		if (diagnosis.getRank() != null) {
			Extension rankExtension = new Extension();
			rankExtension.setUrl(DIAGNOSIS_RANK_EXTENSION);
			rankExtension.setValue(new IntegerType(diagnosis.getRank()));
			fhirCondition.addExtension(rankExtension);
		}
		
		if (diagnosis.getCertainty() != null) {
			Extension certaintyExtension = new Extension();
			certaintyExtension.setUrl(DIAGNOSIS_CERTAINTY_EXTENSION);
			certaintyExtension.setValue(new StringType(diagnosis.getCertainty().toString()));
			fhirCondition.addExtension(certaintyExtension);
		}
		
		// Set onset date from encounter date if available
		if (diagnosis.getEncounter() != null && diagnosis.getEncounter().getEncounterDatetime() != null) {
			fhirCondition.setOnset(new DateTimeType().setValue(diagnosis.getEncounter().getEncounterDatetime()));
		}
		
		// Set recorder and recorded date
		fhirCondition.setRecorder(practitionerReferenceTranslator.toFhirResource(diagnosis.getCreator()));
		fhirCondition.setRecordedDate(diagnosis.getDateCreated());
		
		// Set metadata
		fhirCondition.getMeta().addTag(FhirConstants.OPENMRS_FHIR_EXT_CONDITION_TAG, "diagnosis", "Diagnosis");
		fhirCondition.getMeta().setLastUpdated(getLastUpdated(diagnosis));
		fhirCondition.getMeta().setVersionId(getVersionId(diagnosis));
		
		return fhirCondition;
	}
	
	@Override
	public Diagnosis toOpenmrsType(@Nonnull org.hl7.fhir.r4.model.Condition condition) {
		notNull(condition, "The FHIR Condition object should not be null");
		return this.toOpenmrsType(new Diagnosis(), condition);
	}
	
	@Override
	public Diagnosis toOpenmrsType(@Nonnull Diagnosis existingDiagnosis,
	        @Nonnull org.hl7.fhir.r4.model.Condition condition) {
		notNull(existingDiagnosis, "The existing OpenMRS Diagnosis object should not be null");
		notNull(condition, "The FHIR Condition object should not be null");
		
		if (condition.hasId()) {
			existingDiagnosis.setUuid(condition.getIdElement().getIdPart());
		}
		
		// Set patient
		if (condition.hasSubject()) {
			Patient patient = patientReferenceTranslator.toOpenmrsType((condition.getSubject()));
			if (patient != null) {
				existingDiagnosis.setPatient(patient);
			}
		}
		
		// Set encounter
		if (condition.hasEncounter()) {
			Encounter encounter = (Encounter) encounterReferenceTranslator.toOpenmrsType(condition.getEncounter());
			if (encounter != null) {
				existingDiagnosis.setEncounter(encounter);
			}
		}
		
		// Set diagnosis concept/code
		if (condition.hasCode()) {
			org.openmrs.CodedOrFreeText diagnosisCodedOrText = new org.openmrs.CodedOrFreeText();
			diagnosisCodedOrText.setCoded(conceptTranslator.toOpenmrsType(condition.getCode()));
			
			// Handle non-coded diagnosis extension
			Optional<Extension> nonCodedExtension = Optional
			        .ofNullable(condition.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_NON_CODED_CONDITION));
			nonCodedExtension.ifPresent(ext -> diagnosisCodedOrText.setNonCoded(String.valueOf(ext.getValue())));
			
			existingDiagnosis.setDiagnosis(diagnosisCodedOrText);
		}
		
		// Set certainty from verification status or extension
		Optional<Extension> certaintyExtension = Optional
		        .ofNullable(condition.getExtensionByUrl(DIAGNOSIS_CERTAINTY_EXTENSION));
		if (certaintyExtension.isPresent()) {
			String certaintyValue = String.valueOf(certaintyExtension.get().getValue());
			try {
				existingDiagnosis.setCertainty(ConditionVerificationStatus.valueOf(certaintyValue));
			}
			catch (IllegalArgumentException e) {
				// Default to PROVISIONAL if invalid value
				existingDiagnosis.setCertainty(ConditionVerificationStatus.PROVISIONAL);
			}
		} else if (condition.hasVerificationStatus()) {
			// Map from FHIR verification status to OpenMRS certainty
			String verificationCode = condition.getVerificationStatus().getCodingFirstRep().getCode();
			switch (verificationCode) {
				case "confirmed":
					existingDiagnosis.setCertainty(ConditionVerificationStatus.CONFIRMED);
					break;
				case "provisional":
					existingDiagnosis.setCertainty(ConditionVerificationStatus.PROVISIONAL);
					break;
				default:
					existingDiagnosis.setCertainty(ConditionVerificationStatus.PROVISIONAL);
					break;
			}
		}
		
		// Set rank from extension
		Optional<Extension> rankExtension = Optional.ofNullable(condition.getExtensionByUrl(DIAGNOSIS_RANK_EXTENSION));
		rankExtension.ifPresent(ext -> {
			if (ext.getValue() instanceof IntegerType) {
				existingDiagnosis.setRank(((IntegerType) ext.getValue()).getValue());
			}
		});
		
		// Set voided status based on clinical status
		if (condition.hasClinicalStatus()) {
			String clinicalCode = condition.getClinicalStatus().getCodingFirstRep().getCode();
			existingDiagnosis.setVoided("inactive".equals(clinicalCode));
		}
		
		// Set creator from recorder
		if (condition.hasRecorder()) {
			existingDiagnosis.setCreator(practitionerReferenceTranslator.toOpenmrsType(condition.getRecorder()));
		}
		
		return existingDiagnosis;
	}
}
