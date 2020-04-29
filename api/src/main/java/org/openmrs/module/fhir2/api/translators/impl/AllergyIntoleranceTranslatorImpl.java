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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
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
	private FhirGlobalPropertyService globalPropertyService;
	
	@Autowired
	private FhirConceptService conceptService;
	
	@Autowired
	private ProvenanceTranslator<Allergy> provenanceTranslator;
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	private Map<String, String> severityConceptUuids;
	
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
		allergy.addContained(provenanceTranslator.getCreateProvenance(omrsAllergy));
		allergy.addContained(provenanceTranslator.getUpdateProvenance(omrsAllergy));
		
		return allergy;
	}
	
	@Override
	public Allergy toOpenmrsType(AllergyIntolerance fhirAllergy) {
		return toOpenmrsType(new Allergy(), fhirAllergy);
	}
	
	@Override
	public Allergy toOpenmrsType(Allergy allergy, AllergyIntolerance fhirAllergy) {
		if (fhirAllergy == null) {
			return allergy;
		}
		
		if (fhirAllergy.getId() != null) {
			allergy.setUuid(fhirAllergy.getId());
		}
		
		if (allergy.getAllergen() == null) {
			Allergen allergen = new Allergen();
			if (fhirAllergy.hasCode()) {
				allergen.setCodedAllergen(conceptTranslator.toOpenmrsType(fhirAllergy.getCode()));
				allergen.setNonCodedAllergen(fhirAllergy.getCode().getText());
			}
			allergy.setAllergen(allergen);
		}
		if (fhirAllergy.hasCategory()) {
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
		
		List<AllergyReaction> reactions = new ArrayList<>();
		
		if (fhirAllergy.hasReaction()) {
			allergy.setSeverity(getOpenmrsSeverity(fhirAllergy.getReaction().get(0).getSeverity()));
			
			for (AllergyIntolerance.AllergyIntoleranceReactionComponent reaction : fhirAllergy.getReaction()) {
				if (reaction.hasManifestation()) {
					reactions.add(
					    new AllergyReaction(allergy, conceptTranslator.toOpenmrsType(reaction.getManifestation().get(0)),
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
		if (allergen == null) {
			return null;
		}
		
		CodeableConcept allergySubstance = new CodeableConcept();
		
		if (allergen.getCodedAllergen() != null) {
			allergySubstance = conceptTranslator.toFhirResource(allergen.getCodedAllergen());
		}
		
		if (allergen.getNonCodedAllergen() != null) {
			allergySubstance.setText(allergen.getNonCodedAllergen());
		}
		
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
		
		Map<String, String> severityConceptUuids = getSeverityConceptUuids();
		
		if (severityConceptUuids.isEmpty()) {
			return null;
		}
		
		if (severityConcept.getUuid().equals(severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_MILD))) {
			return AllergyIntolerance.AllergyIntoleranceSeverity.MILD;
		} else if (severityConcept.getUuid().equals(severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_MODERATE))) {
			return AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE;
		} else if (severityConcept.getUuid().equals(severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_SEVERE))) {
			return AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE;
		} else {
			return AllergyIntolerance.AllergyIntoleranceSeverity.NULL;
		}
	}
	
	private Concept getOpenmrsSeverity(AllergyIntolerance.AllergyIntoleranceSeverity severity) {
		if (severity == null) {
			return null;
		}
		
		Map<String, String> severityConceptUuids = getSeverityConceptUuids();
		
		if (severityConceptUuids.isEmpty()) {
			return null;
		}
		
		switch (severity) {
			case MILD:
				return conceptService.getConceptByUuid(severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_MILD))
				        .orElse(null);
			case MODERATE:
				return conceptService.getConceptByUuid(severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_MODERATE))
				        .orElse(null);
			case SEVERE:
				return conceptService.getConceptByUuid(severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_SEVERE))
				        .orElse(null);
			case NULL:
			default:
				return conceptService.getConceptByUuid(severityConceptUuids.get(FhirConstants.GLOBAL_PROPERTY_OTHER))
				        .orElse(null);
		}
	}
	
	private Map<String, String> getSeverityConceptUuids() {
		return globalPropertyService.getGlobalProperties(FhirConstants.GLOBAL_PROPERTY_MILD,
		    FhirConstants.GLOBAL_PROPERTY_MODERATE, FhirConstants.GLOBAL_PROPERTY_SEVERE,
		    FhirConstants.GLOBAL_PROPERTY_OTHER);
	}
}
