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
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getVersionId;

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.Allergen;
import org.openmrs.Allergy;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceCategoryTranslator;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceCriticalityTranslator;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceReactionComponentTranslator;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceSeverityTranslator;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceTranslator;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class AllergyIntoleranceTranslatorImpl extends BaseReferenceHandlingTranslator implements AllergyIntoleranceTranslator {
	
	@Autowired
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Autowired
	private AllergyIntoleranceSeverityTranslator severityTranslator;
	
	@Autowired
	private AllergyIntoleranceCriticalityTranslator criticalityTranslator;
	
	@Autowired
	private AllergyIntoleranceCategoryTranslator categoryTranslator;
	
	@Autowired
	private AllergyIntoleranceReactionComponentTranslator reactionComponentTranslator;
	
	@Override
	public AllergyIntolerance toFhirResource(@Nonnull Allergy omrsAllergy) {
		notNull(omrsAllergy, "The Allergy object should not be null");
		
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.setId(omrsAllergy.getUuid());
		if (omrsAllergy.getAllergen() != null) {
			allergy.addCategory(categoryTranslator.toFhirResource(omrsAllergy.getAllergen().getAllergenType()));
		}
		allergy.setClinicalStatus(setClinicalStatus(omrsAllergy.getVoided()));
		allergy.setVerificationStatus(new CodeableConcept().setText("Confirmed").addCoding(
		    new Coding(FhirConstants.ALLERGY_INTOLERANCE_VERIFICATION_STATUS_SYSTEM_URI, "confirmed", "Confirmed")));
		allergy.setPatient(patientReferenceTranslator.toFhirResource(omrsAllergy.getPatient()));
		allergy.setRecorder(practitionerReferenceTranslator.toFhirResource(omrsAllergy.getCreator()));
		allergy.setRecordedDate(omrsAllergy.getDateCreated());
		allergy.setType(AllergyIntolerance.AllergyIntoleranceType.ALLERGY);
		allergy.addNote(new Annotation().setText(omrsAllergy.getComment()));
		allergy.setCriticality(
		    criticalityTranslator.toFhirResource(severityTranslator.toFhirResource(omrsAllergy.getSeverity())));
		allergy.addReaction(reactionComponentTranslator.toFhirResource(omrsAllergy));
		allergy.setCode(allergy.getReactionFirstRep().getSubstance());
		
		allergy.getMeta().setLastUpdated(getLastUpdated(omrsAllergy));
		allergy.getMeta().setVersionId(getVersionId(omrsAllergy));
		
		return allergy;
	}
	
	@Override
	public Allergy toOpenmrsType(@Nonnull AllergyIntolerance fhirAllergy) {
		notNull(fhirAllergy, "The AllergyIntolerance object should not be null");
		return toOpenmrsType(new Allergy(), fhirAllergy);
	}
	
	@Override
	public Allergy toOpenmrsType(@Nonnull Allergy allergy, @Nonnull AllergyIntolerance fhirAllergy) {
		notNull(allergy, "The existing Allergy should not be null");
		notNull(fhirAllergy, "The AllergyIntolerance object should not be null");
		
		if (fhirAllergy.hasId()) {
			allergy.setUuid(fhirAllergy.getIdElement().getIdPart());
		}
		
		if (fhirAllergy.hasCode()) {
			if (allergy.getAllergen() == null) {
				Allergen allergen = new Allergen();
				allergen.setCodedAllergen(conceptTranslator.toOpenmrsType(fhirAllergy.getCode()));
				
				allergy.setAllergen(allergen);
			}
		}
		if (fhirAllergy.hasCategory() && allergy.getAllergen() != null) {
			allergy.getAllergen()
			        .setAllergenType(categoryTranslator.toOpenmrsType(fhirAllergy.getCategory().get(0).getValue()));
		}
		allergy.setVoided(isAllergyInactive(fhirAllergy.getClinicalStatus()));
		allergy.setPatient(patientReferenceTranslator.toOpenmrsType(fhirAllergy.getPatient()));
		allergy.setCreator(practitionerReferenceTranslator.toOpenmrsType(fhirAllergy.getRecorder()));
		
		if (fhirAllergy.hasReaction()) {
			for (AllergyIntolerance.AllergyIntoleranceReactionComponent reaction : fhirAllergy.getReaction()) {
				reactionComponentTranslator.toOpenmrsType(allergy, reaction);
			}
		}
		if (!CollectionUtils.isEmpty(fhirAllergy.getNote())) {
			allergy.setComment(fhirAllergy.getNote().get(0).getText());
		}
		
		return allergy;
	}
	
	private CodeableConcept setClinicalStatus(boolean voided) {
		CodeableConcept status = new CodeableConcept();
		if (voided) {
			status.setText("Inactive");
			status.addCoding(
			    new Coding(FhirConstants.ALLERGY_INTOLERANCE_CLINICAL_STATUS_SYSTEM_URI, "inactive", "Inactive"));
		} else {
			status.setText("Active");
			status.addCoding(new Coding(FhirConstants.ALLERGY_INTOLERANCE_CLINICAL_STATUS_SYSTEM_URI, "active", "Active"));
		}
		
		return status;
	}
	
	private boolean isAllergyInactive(CodeableConcept status) {
		if (status == null || CollectionUtils.isEmpty(status.getCoding())) {
			return false;
		}
		
		return status.getCoding().stream()
		        .filter(c -> FhirConstants.ALLERGY_INTOLERANCE_CLINICAL_STATUS_SYSTEM_URI.equals(c.getSystem()))
		        .anyMatch(c -> "inactive".equals(c.getCode()));
	}
}
