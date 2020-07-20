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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.openmrs.Allergen;
import org.openmrs.Allergy;
import org.openmrs.AllergyReaction;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceReactionComponentTranslator;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceSeverityTranslator;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class AllergyIntoleranceReactionComponentTranslatorImpl implements AllergyIntoleranceReactionComponentTranslator {
	
	@Autowired
	private AllergyIntoleranceSeverityTranslator severityTranslator;
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Override
	public AllergyIntolerance.AllergyIntoleranceReactionComponent toFhirResource(Allergy allergy) {
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSubstance(getAllergySubstance(allergy.getAllergen()));
		reactionComponent.setManifestation(getManifestation(allergy.getReactions()));
		reactionComponent.setSeverity(severityTranslator.toFhirResource(allergy.getSeverity()));
		return reactionComponent;
	}
	
	@Override
	public Allergy toOpenmrsType(Allergy allergy, AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent) {
		if (allergy.getReactions() == null) {
			allergy.setReactions(new ArrayList<>());
		}
		
		if (reactionComponent.hasSeverity()) {
			allergy.setSeverity(severityTranslator.toOpenmrsType(reactionComponent.getSeverity()));
		}
		if (reactionComponent.hasManifestation()) {
			reactionComponent.getManifestation().forEach(manifestation -> allergy.getReactions().add(
			    new AllergyReaction(allergy, conceptTranslator.toOpenmrsType(manifestation), manifestation.getText())));
		}
		
		return allergy;
	}
	
	@Override
	public Allergy toOpenmrsType(AllergyIntolerance.AllergyIntoleranceReactionComponent resource) {
		return toOpenmrsType(new Allergy(), resource);
	}
	
	private CodeableConcept getAllergySubstance(Allergen allergen) {
		if (allergen == null) {
			return null;
		}
		
		CodeableConcept allergySubstance = new CodeableConcept();
		
		if (allergen.getCodedAllergen() != null) {
			allergySubstance = conceptTranslator.toFhirResource(allergen.getCodedAllergen());
			allergySubstance.setText(allergen.getNonCodedAllergen());
		}
		
		return allergySubstance;
	}
	
	private List<CodeableConcept> getManifestation(List<AllergyReaction> reactions) {
		List<CodeableConcept> manifestations = new ArrayList<>();
		
		if (reactions != null) {
			for (AllergyReaction reaction : reactions) {
				
				if (reaction.getReaction() != null) {
					manifestations.add(
					    conceptTranslator.toFhirResource(reaction.getReaction()).setText(reaction.getReactionNonCoded()));
				}
				
			}
		}
		
		return manifestations;
	}
}
