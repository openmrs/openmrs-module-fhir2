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
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.Allergen;
import org.openmrs.AllergenType;
import org.openmrs.Allergy;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class AllergyIntoleranceTranslatorImpl extends AbstractReferenceHandlingTranslator implements AllergyIntoleranceTranslator {
	
	@Inject
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	@Inject
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Override
	public AllergyIntolerance toFhirResource(Allergy omrsAllergy) {
		if (omrsAllergy == null) {
			return null;
		}
		
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.setId(omrsAllergy.getUuid());
		if (omrsAllergy.getAllergen() != null) {
			switch (omrsAllergy.getAllergen().getAllergenType()) {
				case DRUG:
					allergy.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION);
					break;
				case FOOD:
					allergy.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.FOOD);
					break;
				case ENVIRONMENT:
					allergy.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT);
					break;
				case OTHER:
				default:
					return allergy.addCategory(null);
			}
		}
		allergy.setClinicalStatus(setClinicalStatus(omrsAllergy.getVoided()));
		allergy.setPatient(patientReferenceTranslator.toFhirResource(omrsAllergy.getPatient()));
		allergy.setRecorder(practitionerReferenceTranslator.toFhirResource(omrsAllergy.getCreator()));
		allergy.setRecordedDate(omrsAllergy.getDateCreated());
		allergy.getMeta().setLastUpdated(omrsAllergy.getDateChanged());
		
		return allergy;
	}
	
	@Override
	public Allergy toOpenmrsType(Allergy allergy, AllergyIntolerance fhirAllergy) {
		if (fhirAllergy == null) {
			return null;
		}
		
		allergy.setUuid(fhirAllergy.getId());
		if (allergy.getAllergen() == null) {
			Allergen allergen = new Allergen();
			allergy.setAllergen(allergen);
		}
		if (fhirAllergy.getCategory().size() > 0) {
			switch (fhirAllergy.getCategory().get(0).getValue()) {
				case MEDICATION:
					allergy.getAllergen().setAllergenType(AllergenType.DRUG);
					break;
				case FOOD:
					allergy.getAllergen().setAllergenType(AllergenType.FOOD);
					break;
				case ENVIRONMENT:
					allergy.getAllergen().setAllergenType(AllergenType.ENVIRONMENT);
					break;
				case BIOLOGIC:
				case NULL:
				default:
					allergy.getAllergen().setAllergenType(null);
			}
		}
		allergy.setVoided(isAllergyInactive(fhirAllergy.getClinicalStatus()));
		allergy.setPatient(patientReferenceTranslator.toOpenmrsType(fhirAllergy.getPatient()));
		allergy.setCreator(practitionerReferenceTranslator.toOpenmrsType(fhirAllergy.getRecorder()));
		allergy.setDateCreated(fhirAllergy.getRecordedDate());
		allergy.setDateChanged(fhirAllergy.getMeta().getLastUpdated());
		
		return allergy;
	}
	
	private CodeableConcept setClinicalStatus(boolean voided) {
		CodeableConcept status = new CodeableConcept();
		if (voided == true) {
			status.setText("Inactive");
			status.addCoding(
			    new Coding(FhirConstants.ALLERGY_INTOLERANCE_CLINICAL_STATUS_VALUE_SET, "inactive", "Inactive"));
		} else {
			status.setText("Active");
			status.addCoding(new Coding(FhirConstants.ALLERGY_INTOLERANCE_CLINICAL_STATUS_VALUE_SET, "active", "Active"));
		}
		
		return status;
	}
	
	private boolean isAllergyInactive(CodeableConcept status) {
		return status.getCoding().stream()
		        .filter(c -> FhirConstants.ALLERGY_INTOLERANCE_CLINICAL_STATUS_VALUE_SET.equals(c.getSystem()))
		        .anyMatch(c -> "inactive".equals(c.getCode()));
	}
	
}
