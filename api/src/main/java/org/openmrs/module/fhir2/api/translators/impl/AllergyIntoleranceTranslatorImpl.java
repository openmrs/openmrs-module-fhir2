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

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.Allergen;
import org.openmrs.AllergenType;
import org.openmrs.Allergy;
import org.openmrs.AllergyReaction;
import org.openmrs.Concept;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
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
	
	@Inject
	private FhirGlobalPropertyService globalPropertyService;
	
	@Inject
	private FhirConceptService conceptService;
	
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
		allergy.setType(AllergyIntolerance.AllergyIntoleranceType.ALLERGY);
		allergy.setCode(getAllergySubstance(omrsAllergy.getAllergen()));
		allergy.addNote(new Annotation().setText(omrsAllergy.getComment()));
		
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSubstance(getAllergySubstance(omrsAllergy.getAllergen()));
		reactionComponent.addManifestation(getManifestation(omrsAllergy.getReactions()));
		reactionComponent.setDescription(omrsAllergy.getReactionNonCoded());
		reactionComponent.setSeverity(getFhirSeverity(omrsAllergy.getSeverity()));
		allergy.addReaction(reactionComponent);
		
		return allergy;
	}
	
	@Override
	public Allergy toOpenmrsType(Allergy allergy, AllergyIntolerance fhirAllergy) {
		if (fhirAllergy == null) {
			return allergy;
		}
		
		allergy.setUuid(fhirAllergy.getId());
		if (allergy.getAllergen() == null) {
			Allergen allergen = new Allergen();
			if (!fhirAllergy.getCode().getCoding().isEmpty()) {
				allergen.setCodedAllergen(
				    conceptService.getConceptByUuid(fhirAllergy.getCode().getCoding().get(0).getCode()).orElse(null));
				allergen.setNonCodedAllergen(fhirAllergy.getCode().getCoding().get(0).getDisplay());
			}
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
		
		List<AllergyReaction> reactions = new ArrayList<>();
		
		if (!fhirAllergy.getReaction().isEmpty()) {
			allergy.setSeverity(getOpenmrsSeverity(fhirAllergy.getReaction().get(0).getSeverity()));
			
			for (AllergyIntolerance.AllergyIntoleranceReactionComponent reaction : fhirAllergy.getReaction()) {
				if (!reaction.getManifestation().isEmpty()) {
					reactions.add(new AllergyReaction(allergy, conceptService
					        .getConceptByUuid(reaction.getManifestation().get(0).getCoding().get(0).getCode()).orElse(null),
					        reaction.getManifestation().get(0).getText()));
				}
			}
		}
		if (!fhirAllergy.getNote().isEmpty()) {
			allergy.setComment(fhirAllergy.getNote().get(0).getText());
		}
		allergy.setReactions(reactions);
		
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
	
	private CodeableConcept getAllergySubstance(Allergen allergen) {
		if (allergen.getCodedAllergen() == null) {
			return null;
		}
		CodeableConcept allergySubstance = new CodeableConcept();
		Coding code = new Coding(FhirConstants.ALLERGY_SUBSTANCE_VALUE_SET_URI, allergen.getCodedAllergen().getUuid(),
		        allergen.getNonCodedAllergen());
		allergySubstance.addCoding(code);
		allergySubstance.setText(allergen.getNonCodedAllergen());
		
		return allergySubstance;
	}
	
	private CodeableConcept getManifestation(List<AllergyReaction> reactions) {
		CodeableConcept manifestations = new CodeableConcept();
		for (AllergyReaction reaction : reactions) {
			Coding code = new Coding(FhirConstants.CLINICAL_FINDINGS_VALUE_SET_URI, reaction.getUuid(),
			        reaction.getReactionNonCoded());
			manifestations.addCoding(code);
			manifestations.setText(reaction.getReactionNonCoded());
		}
		
		return manifestations;
	}
	
	private AllergyIntolerance.AllergyIntoleranceSeverity getFhirSeverity(Concept severityConcept) {
		if (severityConcept == null) {
			return null;
		}
		
		if (globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_MILD, "")
		        .equals(severityConcept.getUuid())) {
			return AllergyIntolerance.AllergyIntoleranceSeverity.MILD;
		} else if (globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_MODERATE, "")
		        .equals(severityConcept.getUuid())) {
			return AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE;
		} else if (globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_SEVERE, "")
		        .equals(severityConcept.getUuid())) {
			return AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE;
		} else {
			return AllergyIntolerance.AllergyIntoleranceSeverity.NULL;
		}
	}
	
	private Concept getOpenmrsSeverity(AllergyIntolerance.AllergyIntoleranceSeverity severity) {
		Concept concept;
		switch (severity) {
			case MILD:
				concept = conceptService
				        .getConceptByUuid(globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_MILD))
				        .orElse(null);
				break;
			case MODERATE:
				concept = conceptService
				        .getConceptByUuid(globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_MODERATE))
				        .orElse(null);
				break;
			case SEVERE:
				concept = conceptService
				        .getConceptByUuid(globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_SEVERE))
				        .orElse(null);
				break;
			case NULL:
			default:
				concept = conceptService
				        .getConceptByUuid(globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_OTHER))
				        .orElse(null);
		}
		return concept;
	}
	
}
