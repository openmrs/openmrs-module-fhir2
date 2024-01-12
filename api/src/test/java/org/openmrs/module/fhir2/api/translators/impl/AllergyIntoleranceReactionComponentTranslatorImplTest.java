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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Allergen;
import org.openmrs.AllergenType;
import org.openmrs.Allergy;
import org.openmrs.AllergyReaction;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceSeverityTranslator;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;

@RunWith(MockitoJUnitRunner.class)
public class AllergyIntoleranceReactionComponentTranslatorImplTest {
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private AllergyIntoleranceSeverityTranslator severityTranslator;
	
	private AllergyIntoleranceReactionComponentTranslatorImpl reactionComponentTranslator;
	
	private static final String GLOBAL_PROPERTY_MILD_VALUE = "102553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String GLOBAL_PROPERTY_SEVERE_VALUE = "202553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String GLOBAL_PROPERTY_MODERATE_VALUE = "302553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String GLOBAL_PROPERTY_OTHER_VALUE = "402553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String ALLERGY_REACTION_UUID = "f984ceea-fb7f-48fd-af98-532149f5d240";
	
	private static final String CONCEPT_UUID = "162553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_ALLERGEN = "Coded Allergen";
	
	private static final String NON_CODED_ALLERGEN = "Non coded Allergen";
	
	private static final String CODED_REACTION = "Coded Reaction";
	
	private static final String NON_CODED_REACTION = "Non coded Reaction";
	
	private static final String OTHER_CONCEPT_NAME = "Other";
	
	private Allergy omrsAllergy;
	
	@Before
	public void setUp() {
		reactionComponentTranslator = new AllergyIntoleranceReactionComponentTranslatorImpl();
		reactionComponentTranslator.setConceptTranslator(conceptTranslator);
		reactionComponentTranslator.setSeverityTranslator(severityTranslator);
		
		omrsAllergy = new Allergy();
		Allergen allergen = new Allergen(AllergenType.FOOD, null, "Test allergen");
		omrsAllergy.setAllergen(allergen);
		
	}
	
	@Test
	public void toFhirResource_shouldTranslateReactionToManifestation() {
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		concept.addName(new ConceptName(CODED_REACTION, null));
		
		AllergyReaction reaction = new AllergyReaction();
		reaction.setUuid(ALLERGY_REACTION_UUID);
		reaction.setReaction(concept);
		reaction.setAllergy(omrsAllergy);
		omrsAllergy.setReactions(Collections.singletonList(reaction));
		
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding(new Coding().setCode(CONCEPT_UUID));
		codeableConcept.setText(CODED_REACTION);
		when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
		
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = reactionComponentTranslator
		        .toFhirResource(omrsAllergy);
		
		assertThat(reactionComponent, notNullValue());
		assertThat(reactionComponent.getManifestation(), hasSize(equalTo(1)));
		assertThat(reactionComponent.getManifestation().get(0).getCoding(), hasSize(equalTo(1)));
		assertThat(reactionComponent.getManifestation().get(0).getCoding().get(0).getCode(), equalTo(CONCEPT_UUID));
		assertThat(reactionComponent.getManifestation().get(0).getText(), equalTo(CODED_REACTION));
	}
	
	@Test
	public void toFhirResource_shouldTranslateNonCodedReactionToManifestation() {
		
		Concept otherConcept = new Concept();
		otherConcept.setUuid(GLOBAL_PROPERTY_OTHER_VALUE);
		otherConcept.addName(new ConceptName(OTHER_CONCEPT_NAME, null));
		
		AllergyReaction reaction = new AllergyReaction();
		reaction.setUuid(ALLERGY_REACTION_UUID);
		reaction.setReaction(otherConcept);
		reaction.setAllergy(omrsAllergy);
		reaction.setReactionNonCoded(NON_CODED_REACTION);
		omrsAllergy.setReactions(Collections.singletonList(reaction));
		
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding(new Coding().setCode(CONCEPT_UUID));
		when(conceptTranslator.toFhirResource(otherConcept)).thenReturn(codeableConcept);
		
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = reactionComponentTranslator
		        .toFhirResource(omrsAllergy);
		
		assertThat(reactionComponent, notNullValue());
		assertThat(reactionComponent.getManifestation(), hasSize(equalTo(1)));
		assertThat(reactionComponent.getManifestation().get(0).getCoding(), hasSize(equalTo(1)));
		assertThat(reactionComponent.getManifestation().get(0).getCoding().get(0).getCode(), equalTo(CONCEPT_UUID));
		assertThat(reactionComponent.getManifestation().get(0).getText(), equalTo(NON_CODED_REACTION));
	}
	
	@Test
	public void toFhirResource_shouldTranslateReactionMildCorrectly() {
		Concept mildConcept = new Concept();
		mildConcept.setUuid(GLOBAL_PROPERTY_MILD_VALUE);
		omrsAllergy.setSeverity(mildConcept);
		
		when(severityTranslator.toFhirResource(mildConcept)).thenReturn(AllergyIntolerance.AllergyIntoleranceSeverity.MILD);
		
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = reactionComponentTranslator
		        .toFhirResource(omrsAllergy);
		assertThat(reactionComponent, notNullValue());
		assertThat(reactionComponent.getSeverity(), equalTo(AllergyIntolerance.AllergyIntoleranceSeverity.MILD));
	}
	
	@Test
	public void toFhirResource_shouldTranslateReactionModerateCorrectly() {
		Concept moderateConcept = new Concept();
		moderateConcept.setUuid(GLOBAL_PROPERTY_MODERATE_VALUE);
		omrsAllergy.setSeverity(moderateConcept);
		
		when(severityTranslator.toFhirResource(moderateConcept))
		        .thenReturn(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE);
		
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = reactionComponentTranslator
		        .toFhirResource(omrsAllergy);
		assertThat(reactionComponent, notNullValue());
		assertThat(reactionComponent.getSeverity(), equalTo(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE));
	}
	
	@Test
	public void toFhirResource_shouldTranslateReactionSevereCorrectly() {
		Concept severeConcept = new Concept();
		severeConcept.setUuid(GLOBAL_PROPERTY_SEVERE_VALUE);
		omrsAllergy.setSeverity(severeConcept);
		
		when(severityTranslator.toFhirResource(severeConcept))
		        .thenReturn(AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE);
		
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = reactionComponentTranslator
		        .toFhirResource(omrsAllergy);
		assertThat(reactionComponent, notNullValue());
		assertThat(reactionComponent.getSeverity(), equalTo(AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE));
	}
	
	@Test
	public void toFhirResource_shouldTranslateReactionOtherToNull() {
		Concept otherConcept = new Concept();
		otherConcept.setUuid(GLOBAL_PROPERTY_OTHER_VALUE);
		omrsAllergy.setSeverity(otherConcept);
		
		when(severityTranslator.toFhirResource(otherConcept)).thenReturn(AllergyIntolerance.AllergyIntoleranceSeverity.NULL);
		
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = reactionComponentTranslator
		        .toFhirResource(omrsAllergy);
		assertThat(reactionComponent, notNullValue());
		assertThat(reactionComponent.getSeverity(), equalTo(AllergyIntolerance.AllergyIntoleranceSeverity.NULL));
	}
	
	@Test
	public void toFhirResource_shouldTranslateAllergenToAllergySubstance() {
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		concept.addName(new ConceptName(CODED_ALLERGEN, null));
		
		Allergen allergen = new Allergen(AllergenType.FOOD, concept, null);
		omrsAllergy.setAllergen(allergen);
		
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding(new Coding().setCode(CONCEPT_UUID).setDisplay(CODED_ALLERGEN));
		codeableConcept.setText(CODED_ALLERGEN);
		when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
		
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = reactionComponentTranslator
		        .toFhirResource(omrsAllergy);
		assertThat(reactionComponent, notNullValue());
		assertThat(reactionComponent.getSubstance().getCodingFirstRep().getCode(), equalTo(CONCEPT_UUID));
		assertThat(reactionComponent.getSubstance().getCodingFirstRep().getDisplay(), equalTo(CODED_ALLERGEN));
		assertThat(reactionComponent.getSubstance().getText(), equalTo(CODED_ALLERGEN));
	}
	
	@Test
	public void toFhirResource_shouldTranslateNonCodedAllergenToAllergySubstance() {
		
		Concept otherConcept = new Concept();
		otherConcept.setUuid(GLOBAL_PROPERTY_OTHER_VALUE);
		otherConcept.addName(new ConceptName(OTHER_CONCEPT_NAME, null));
		
		Allergen allergen = new Allergen(AllergenType.OTHER, otherConcept, NON_CODED_ALLERGEN);
		omrsAllergy.setAllergen(allergen);
		
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding(new Coding().setCode(GLOBAL_PROPERTY_OTHER_VALUE).setDisplay(OTHER_CONCEPT_NAME));
		codeableConcept.setText(OTHER_CONCEPT_NAME);
		when(conceptTranslator.toFhirResource(otherConcept)).thenReturn(codeableConcept);
		
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = reactionComponentTranslator
		        .toFhirResource(omrsAllergy);
		
		assertThat(reactionComponent, notNullValue());
		assertThat(reactionComponent.getSubstance().getCodingFirstRep().getCode(), equalTo(GLOBAL_PROPERTY_OTHER_VALUE));
		assertThat(reactionComponent.getSubstance().getCodingFirstRep().getDisplay(), equalTo(OTHER_CONCEPT_NAME));
		assertThat(reactionComponent.getSubstance().getText(), equalTo(NON_CODED_ALLERGEN));
		
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSeverityMildCorrectly() {
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.MILD);
		
		Concept mildConcept = new Concept();
		mildConcept.setUuid(GLOBAL_PROPERTY_MILD_VALUE);
		
		when(severityTranslator.toOpenmrsType(AllergyIntolerance.AllergyIntoleranceSeverity.MILD)).thenReturn(mildConcept);
		
		reactionComponentTranslator.toOpenmrsType(omrsAllergy, reactionComponent);
		
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getSeverity(), notNullValue());
		assertThat(omrsAllergy.getSeverity(), equalTo(mildConcept));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfSeverityIsNull() {
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSeverity(null);
		
		reactionComponentTranslator.toOpenmrsType(omrsAllergy, reactionComponent);
		
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getSeverity(), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSeverityModerateCorrectly() {
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE);
		
		Concept moderateConcept = new Concept();
		moderateConcept.setUuid(GLOBAL_PROPERTY_MODERATE_VALUE);
		
		when(severityTranslator.toOpenmrsType(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE))
		        .thenReturn(moderateConcept);
		
		reactionComponentTranslator.toOpenmrsType(omrsAllergy, reactionComponent);
		
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getSeverity(), notNullValue());
		assertThat(omrsAllergy.getSeverity(), equalTo(moderateConcept));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSeveritySevereCorrectly() {
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE);
		
		Concept severeConcept = new Concept();
		severeConcept.setUuid(GLOBAL_PROPERTY_SEVERE_VALUE);
		
		when(severityTranslator.toOpenmrsType(AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE))
		        .thenReturn(severeConcept);
		
		reactionComponentTranslator.toOpenmrsType(omrsAllergy, reactionComponent);
		
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getSeverity(), notNullValue());
		assertThat(omrsAllergy.getSeverity(), equalTo(severeConcept));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSeverityOtherCorrectly() {
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.NULL);
		
		Concept otherConcept = new Concept();
		otherConcept.setUuid(GLOBAL_PROPERTY_OTHER_VALUE);
		
		when(severityTranslator.toOpenmrsType(AllergyIntolerance.AllergyIntoleranceSeverity.NULL)).thenReturn(otherConcept);
		
		reactionComponentTranslator.toOpenmrsType(omrsAllergy, reactionComponent);
		
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getSeverity(), notNullValue());
		assertThat(omrsAllergy.getSeverity(), equalTo(otherConcept));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullReactionIfManifestationIsNull() {
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE);
		reactionComponent.setManifestation(null);
		
		reactionComponentTranslator.toOpenmrsType(omrsAllergy, reactionComponent);
		
		assertThat(omrsAllergy.getReactions().size(), equalTo(0));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateManifestationToReaction() {
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE);
		
		CodeableConcept manifestation = new CodeableConcept()
		        .addCoding(new Coding(FhirConstants.CLINICAL_FINDINGS_SYSTEM_URI, CONCEPT_UUID, CODED_REACTION));
		reactionComponent.addManifestation(manifestation);
		
		Concept codedReaction = new Concept();
		codedReaction.setUuid(CONCEPT_UUID);
		
		when(conceptTranslator.toOpenmrsType(manifestation)).thenReturn(codedReaction);
		
		reactionComponentTranslator.toOpenmrsType(omrsAllergy, reactionComponent);
		
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getReactions().size(), greaterThanOrEqualTo(1));
		assertThat(omrsAllergy.getReactions().get(0).getReaction().getUuid(), equalTo(CONCEPT_UUID));
	}
	
}
