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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exparity.hamcrest.date.DateMatchers;
import org.hamcrest.CoreMatchers;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
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
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.FhirTestConstants;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceCriticalityTranslator;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceSeverityTranslator;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;

@RunWith(MockitoJUnitRunner.class)
public class AllergyIntoleranceTranslatorImplTest {
	
	private static final String ALLERGY_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String ALLERGY_REACTION_UUID = "c0938432-1691-11df-97a5-7038c432r679";
	
	private static final String ALLERGY_INTOLERANCE_CLINICAL_STATUC_ACTIVE = "active";
	
	private static final String ALLERGY_INTOLERANCE_CLINICAL_STATUC_INACTIVE = "inactive";
	
	private static final String PATIENT_UUID = "c0938432-1691-11df-9pa5-7038c432aaba";
	
	private static final String CREATOR_UUID = "c1038432-1691-11df-9pa5-7038c432aaba";
	
	private static final String CONCEPT_UUID = "162553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String NON_CODED_REACTION = "Test Reaction";
	
	private static final String GLOBAL_PROPERTY_MILD_VALUE = "102553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String GLOBAL_PROPERTY_SEVERE_VALUE = "202553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String GLOBAL_PROPERTY_MODERATE_VALUE = "302553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String GLOBAL_PROPERTY_OTHER_VALUE = "402553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Mock
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private ProvenanceTranslator<Allergy> provenanceTranslator;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private AllergyIntoleranceSeverityTranslator severityTranslator;
	
	@Mock
	private AllergyIntoleranceCriticalityTranslator criticalityTranslator;
	
	private AllergyIntoleranceTranslatorImpl allergyIntoleranceTranslator;
	
	private Allergy omrsAllergy;
	
	private Map<String, String> severityConceptUuids = new HashMap<>();
	
	@Before
	public void setUp() {
		allergyIntoleranceTranslator = new AllergyIntoleranceTranslatorImpl();
		allergyIntoleranceTranslator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
		allergyIntoleranceTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
		allergyIntoleranceTranslator.setProvenanceTranslator(provenanceTranslator);
		allergyIntoleranceTranslator.setConceptTranslator(conceptTranslator);
		allergyIntoleranceTranslator.setSeverityTranslator(severityTranslator);
		allergyIntoleranceTranslator.setCriticalityTranslator(criticalityTranslator);
		
		omrsAllergy = new Allergy();
		Allergen allergen = new Allergen(AllergenType.FOOD, null, "Test allergen");
		omrsAllergy.setAllergen(allergen);
		initSeverityData();
	}
	
	public void initSeverityData() {
		severityConceptUuids.put(FhirConstants.GLOBAL_PROPERTY_MILD, GLOBAL_PROPERTY_MILD_VALUE);
		severityConceptUuids.put(FhirConstants.GLOBAL_PROPERTY_MODERATE, GLOBAL_PROPERTY_MODERATE_VALUE);
		severityConceptUuids.put(FhirConstants.GLOBAL_PROPERTY_SEVERE, GLOBAL_PROPERTY_SEVERE_VALUE);
		severityConceptUuids.put(FhirConstants.GLOBAL_PROPERTY_OTHER, GLOBAL_PROPERTY_OTHER_VALUE);
	}
	
	@Test
	public void toFhirResource_shouldReturnNullWhenCalledWithANullObject() {
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(null);
		assertThat(allergyIntolerance, nullValue());
	}
	
	@Test
	public void toFhirResource_shouldTranslateUuidToId() {
		omrsAllergy.setUuid(ALLERGY_UUID);
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateNullUuidToNullId() {
		omrsAllergy.setUuid(null);
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getId(), nullValue());
	}
	
	@Test
	public void toFhirResource_shouldReturnNullCategoryWhenAllergenTypeIsNull() {
		omrsAllergy.setAllergen(null);
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance.getCategory().size(), equalTo(0));
	}
	
	@Test
	public void toFhirResource_shouldTranslateAllergyIntoleranceCategoryFoodCorrectly() {
		Allergen allergen = new Allergen();
		allergen.setAllergenType(AllergenType.FOOD);
		omrsAllergy.setAllergen(allergen);
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getCategory().get(0).getValue(),
		    equalTo(AllergyIntolerance.AllergyIntoleranceCategory.FOOD));
	}
	
	@Test
	public void toFhirResource_shouldTranslateAllergyIntoleranceCategoryMedicationCorrectly() {
		Allergen allergen = new Allergen();
		allergen.setAllergenType(AllergenType.DRUG);
		omrsAllergy.setAllergen(allergen);
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getCategory().get(0).getValue(),
		    equalTo(AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION));
	}
	
	@Test
	public void toFhirResource_shouldTranslateAllergyIntoleranceCategoryEnvironmentCorrectly() {
		Allergen allergen = new Allergen();
		allergen.setAllergenType(AllergenType.ENVIRONMENT);
		omrsAllergy.setAllergen(allergen);
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getCategory().get(0).getValue(),
		    equalTo(AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullCategoryWhenCalledWithOpenMrsOtherCategory() {
		Allergen allergen = new Allergen();
		allergen.setAllergenType(AllergenType.OTHER);
		omrsAllergy.setAllergen(allergen);
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance.getCategory().get(0).getValue(), nullValue());
	}
	
	@Test
	public void toFhirResource_shouldTranslateVoidedTrueToInactive() {
		omrsAllergy.setVoided(true);
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance.getClinicalStatus().getCoding().get(0).getCode(), equalTo("inactive"));
		assertThat(allergyIntolerance.getClinicalStatus().getCoding().get(0).getDisplay(), equalTo("Inactive"));
		assertThat(allergyIntolerance.getClinicalStatus().getText(), equalTo("Inactive"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateVoidedFalseToActive() {
		omrsAllergy.setVoided(false);
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance.getClinicalStatus().getCoding().get(0).getCode(), equalTo("active"));
		assertThat(allergyIntolerance.getClinicalStatus().getCoding().get(0).getDisplay(), equalTo("Active"));
		assertThat(allergyIntolerance.getClinicalStatus().getText(), equalTo("Active"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOmrsPatientToFhirPatientReference() {
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		omrsAllergy.setPatient(patient);
		
		Reference patientReference = new Reference().setReference(FhirTestConstants.PATIENT + "/" + PATIENT_UUID)
		        .setType(FhirTestConstants.PATIENT).setIdentifier(new Identifier().setValue(PATIENT_UUID));
		
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientReference);
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance.getPatient(), notNullValue());
		assertThat(allergyIntolerance.getPatient().getType(), is(FhirTestConstants.PATIENT));
		assertThat(allergyIntoleranceTranslator.getReferenceId(allergyIntolerance.getPatient()), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOmrsAllergyCreatorToFhirPractitionerReference() {
		User user = new User();
		user.setUuid(CREATOR_UUID);
		omrsAllergy.setCreator(user);
		
		Reference practionerReference = new Reference().setReference(FhirTestConstants.PRACTITIONER + "/" + CREATOR_UUID)
		        .setType(FhirTestConstants.PRACTITIONER).setIdentifier(new Identifier().setValue(CREATOR_UUID));
		
		when(practitionerReferenceTranslator.toFhirResource(user)).thenReturn(practionerReference);
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance.getRecorder(), notNullValue());
		assertThat(allergyIntolerance.getRecorder().getType(), is(FhirTestConstants.PRACTITIONER));
		assertThat(allergyIntoleranceTranslator.getReferenceId(allergyIntolerance.getRecorder()), equalTo(CREATOR_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDateCreatedToRecordedDate() {
		omrsAllergy.setDateCreated(new Date());
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getRecordedDate(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenMrsDateChangedToLastUpdatedDate() {
		omrsAllergy.setDateChanged(new Date());
		
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getMeta().getLastUpdated(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToAllergyType() {
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getType(), equalTo(AllergyIntolerance.AllergyIntoleranceType.ALLERGY));
	}
	
	@Test
	public void toFhirResource_shouldTranslateAllergenToAllergySubstance() {
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		
		Allergen allergen = new Allergen();
		allergen.setCodedAllergen(concept);
		allergen.setAllergenType(AllergenType.FOOD);
		omrsAllergy.setAllergen(allergen);
		
		when(conceptTranslator.toFhirResource(concept))
		        .thenReturn(new CodeableConcept().addCoding(new Coding("", CONCEPT_UUID, "")));
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getCode().getCoding().get(0).getCode(), equalTo(CONCEPT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateReactionToManifestation() {
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		
		AllergyReaction reaction = new AllergyReaction();
		reaction.setUuid(ALLERGY_REACTION_UUID);
		reaction.setReaction(concept);
		reaction.setAllergy(omrsAllergy);
		reaction.setReactionNonCoded(NON_CODED_REACTION);
		omrsAllergy.setReactions(Collections.singletonList(reaction));
		
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding(new Coding().setCode(CONCEPT_UUID));
		codeableConcept.setText(NON_CODED_REACTION);
		when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getReaction(), hasSize(greaterThanOrEqualTo(1)));
		assertThat(allergyIntolerance.getReaction().get(0).getManifestation(), hasSize(greaterThanOrEqualTo(1)));
		assertThat(allergyIntolerance.getReaction().get(0).getManifestation().get(0).getCoding(),
		    hasSize(greaterThanOrEqualTo(1)));
		assertThat(allergyIntolerance.getReaction().get(0).getManifestation().size(), greaterThanOrEqualTo(1));
		assertThat(allergyIntolerance.getReaction().get(0).getManifestation().get(0).getCoding().get(0).getCode(),
		    equalTo(CONCEPT_UUID));
		assertThat(allergyIntolerance.getReaction().get(0).getManifestation().get(0).getText(), equalTo(NON_CODED_REACTION));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullSeverityIfSeverityConceptIsNull() {
		omrsAllergy.setSeverity(null);
		
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance.getReaction().get(0).getSeverity(), nullValue());
	}
	
	@Test
	public void toFhirResource_shouldReturnNullSeverityIfGlobalPropertyValueIsNull() {
		severityConceptUuids = null;
		
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance.getReaction().get(0).getSeverity(), nullValue());
	}
	
	@Test
	public void toFhirResource_shouldReturnNullSeverityIfGlobalPropertyUuidsContainerIsEmpty() {
		severityConceptUuids = new HashMap<>();
		
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance.getReaction().get(0).getSeverity(), nullValue());
	}
	
	@Test
	public void toFhirResource_shouldTranslateReactionMildCorrectly() {
		Concept mildConcept = new Concept();
		mildConcept.setUuid(GLOBAL_PROPERTY_MILD_VALUE);
		omrsAllergy.setSeverity(mildConcept);
		
		when(severityTranslator.toFhirResource(mildConcept)).thenReturn(AllergyIntolerance.AllergyIntoleranceSeverity.MILD);
		
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getReaction().get(0).getSeverity(),
		    equalTo(AllergyIntolerance.AllergyIntoleranceSeverity.MILD));
	}
	
	@Test
	public void toFhirResource_shouldTranslateReactionModerateCorrectly() {
		Concept moderateConcept = new Concept();
		moderateConcept.setUuid(GLOBAL_PROPERTY_MODERATE_VALUE);
		omrsAllergy.setSeverity(moderateConcept);
		
		when(severityTranslator.toFhirResource(moderateConcept))
		        .thenReturn(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE);
		
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getReaction().get(0).getSeverity(),
		    equalTo(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE));
	}
	
	@Test
	public void toFhirResource_shouldTranslateReactionSevereCorrectly() {
		Concept severeConcept = new Concept();
		severeConcept.setUuid(GLOBAL_PROPERTY_SEVERE_VALUE);
		omrsAllergy.setSeverity(severeConcept);
		
		when(severityTranslator.toFhirResource(severeConcept))
		        .thenReturn(AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE);
		
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getReaction().get(0).getSeverity(),
		    equalTo(AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE));
	}
	
	@Test
	public void toFhirResource_shouldTranslateReactionOtherToNull() {
		Concept otherConcept = new Concept();
		otherConcept.setUuid(GLOBAL_PROPERTY_OTHER_VALUE);
		omrsAllergy.setSeverity(otherConcept);
		
		when(severityTranslator.toFhirResource(otherConcept)).thenReturn(AllergyIntolerance.AllergyIntoleranceSeverity.NULL);
		
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getReaction().get(0).getSeverity(),
		    equalTo(AllergyIntolerance.AllergyIntoleranceSeverity.NULL));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToHighCriticality() {
		Concept severeConcept = new Concept();
		severeConcept.setUuid(GLOBAL_PROPERTY_SEVERE_VALUE);
		omrsAllergy.setSeverity(severeConcept);
		
		when(severityTranslator.toFhirResource(severeConcept))
		        .thenReturn(AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE);
		when(criticalityTranslator.toFhirResource(AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE))
		        .thenReturn(AllergyIntolerance.AllergyIntoleranceCriticality.HIGH);
		
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getCriticality(), equalTo(AllergyIntolerance.AllergyIntoleranceCriticality.HIGH));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToLowCriticality() {
		Concept mildConcept = new Concept();
		mildConcept.setUuid(GLOBAL_PROPERTY_MILD_VALUE);
		omrsAllergy.setSeverity(mildConcept);
		when(severityTranslator.toFhirResource(mildConcept)).thenReturn(AllergyIntolerance.AllergyIntoleranceSeverity.MILD);
		when(criticalityTranslator.toFhirResource(AllergyIntolerance.AllergyIntoleranceSeverity.MILD))
		        .thenReturn(AllergyIntolerance.AllergyIntoleranceCriticality.LOW);
		
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getCriticality(), equalTo(AllergyIntolerance.AllergyIntoleranceCriticality.LOW));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToNullCriticality() {
		Concept otherConcept = new Concept();
		otherConcept.setUuid(GLOBAL_PROPERTY_OTHER_VALUE);
		omrsAllergy.setSeverity(otherConcept);
		when(severityTranslator.toFhirResource(otherConcept)).thenReturn(AllergyIntolerance.AllergyIntoleranceSeverity.NULL);
		when(criticalityTranslator.toFhirResource(AllergyIntolerance.AllergyIntoleranceSeverity.NULL))
		        .thenReturn(AllergyIntolerance.AllergyIntoleranceCriticality.NULL);
		
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getCriticality(), equalTo(AllergyIntolerance.AllergyIntoleranceCriticality.NULL));
	}
	
	@Test
	public void toFhirResource_shouldTranslateCommentToNote() {
		omrsAllergy.setComment("");
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getNote().get(0).getText(), equalTo(""));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnAllergyAsIsIfAllergyIntoleranceIsNull() {
		Allergy result = allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, null);
		assertThat(result, equalTo(omrsAllergy));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateAllergenTypeFoodCorrectly() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.FOOD);
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getAllergen().getAllergenType(), equalTo(AllergenType.FOOD));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateAllergenTypeDrugCorrectly() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION);
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getAllergen().getAllergenType(), equalTo(AllergenType.DRUG));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateAllergenTypeEnvironmentCorrectly() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT);
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getAllergen().getAllergenType(), equalTo(AllergenType.ENVIRONMENT));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullWhenCalledWithFhirBiologicCategory() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.BIOLOGIC);
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getAllergen().getAllergenType(), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullCategoryWhenCalledWithFhirNullCategory() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.NULL);
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getAllergen().getAllergenType(), nullValue());
	}
	
	@Test
	public void toFhirResource_shouldReturnNullCodedAllergenIfCodeIsNull() {
		omrsAllergy.setAllergen(new Allergen(AllergenType.FOOD, null, null));
		
		AllergyIntolerance allergy = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergy.getCode(), notNullValue());
		assertThat(allergy.getCode().getCoding().size(), equalTo(0));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullNonCodedAllergenIfCodeTextIsNull() {
		omrsAllergy.setAllergen(new Allergen(AllergenType.FOOD, null, null));
		AllergyIntolerance allergy = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		
		assertThat(allergy.getCode(), notNullValue());
		assertThat(allergy.getCode().getText(), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateInactiveToVoidedTrue() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.setClinicalStatus(
		    new CodeableConcept().addCoding(new Coding("", ALLERGY_INTOLERANCE_CLINICAL_STATUC_INACTIVE, "")));
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateActiveToVoidedFalse() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.setClinicalStatus(
		    new CodeableConcept().addCoding(new Coding("", ALLERGY_INTOLERANCE_CLINICAL_STATUC_ACTIVE, "")));
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getVoided(), equalTo(false));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslatePatientReferenceToPatient() {
		Reference patientReference = new Reference().setReference(FhirTestConstants.PATIENT + "/" + PATIENT_UUID)
		        .setType(FhirTestConstants.PATIENT).setIdentifier(new Identifier().setValue(PATIENT_UUID));
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.setPatient(patientReference);
		
		Patient omrsPatient = new Patient();
		omrsPatient.setUuid(PATIENT_UUID);
		when(patientReferenceTranslator.toOpenmrsType(patientReference)).thenReturn(omrsPatient);
		
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getPatient(), is(omrsPatient));
		assertThat(omrsAllergy.getPatient().getUuid(), is(PATIENT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslatePractitionerReferenceToCreator() {
		Reference practitionerReference = new Reference().setReference(FhirTestConstants.PRACTITIONER + "/" + CREATOR_UUID)
		        .setType(FhirTestConstants.PRACTITIONER).setIdentifier(new Identifier().setValue(CREATOR_UUID));
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.setRecorder(practitionerReference);
		
		User user = new User();
		user.setUuid(CREATOR_UUID);
		omrsAllergy.setCreator(user);
		when(practitionerReferenceTranslator.toOpenmrsType(practitionerReference)).thenReturn(user);
		
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getCreator(), is(user));
		assertThat(omrsAllergy.getCreator().getUuid(), is(CREATOR_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfNotIsEmpty() {
		AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
		allergyIntolerance.setNote(null);
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergyIntolerance);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getComment(), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateNoteToComment() {
		AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
		allergyIntolerance.addNote(new Annotation().setText("Test Allergy"));
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergyIntolerance);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getComment(), equalTo("Test Allergy"));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullAllergenIfAllergySubstanceIsNull() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.setCode(null);
		omrsAllergy.setAllergen(null);
		
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getAllergen(), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateAllergySubstanceToCodedAllergen() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		
		CodeableConcept code = new CodeableConcept().addCoding(new Coding("", CONCEPT_UUID, ""));
		allergy.setCode(code);
		
		Concept allergen = new Concept();
		allergen.setUuid(CONCEPT_UUID);
		omrsAllergy.setAllergen(null);
		
		when(conceptTranslator.toOpenmrsType(allergy.getCode())).thenReturn(allergen);
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getAllergen().getCodedAllergen().getUuid(), equalTo(CONCEPT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateAllergySubstanceTextToNonCodedAllergen() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		
		CodeableConcept code = new CodeableConcept().addCoding(new Coding("", "", ""));
		code.setText("Non coded allergen");
		allergy.setCode(code);
		omrsAllergy.setAllergen(null);
		
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getAllergen().getNonCodedAllergen(), equalTo("Non coded allergen"));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSeverityMildCorrectly() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.MILD);
		allergy.setReaction(Collections.singletonList(reactionComponent));
		
		Concept mildConcept = new Concept();
		mildConcept.setUuid(GLOBAL_PROPERTY_MILD_VALUE);
		
		when(severityTranslator.toOpenmrsType(AllergyIntolerance.AllergyIntoleranceSeverity.MILD)).thenReturn(mildConcept);
		
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getSeverity(), notNullValue());
		assertThat(omrsAllergy.getSeverity(), equalTo(mildConcept));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfSeverityIsNull() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getSeverity(), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfSeverityConceptUuidsIsNull() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		severityConceptUuids = null;
		
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE);
		allergy.setReaction(Collections.singletonList(reactionComponent));
		
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getSeverity(), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfSeverityConceptUuidsContainerIsEmpty() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		severityConceptUuids = new HashMap<>();
		
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE);
		allergy.setReaction(Collections.singletonList(reactionComponent));
		
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getSeverity(), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSeverityModerateCorrectly() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE);
		allergy.setReaction(Collections.singletonList(reactionComponent));
		
		Concept moderateConcept = new Concept();
		moderateConcept.setUuid(GLOBAL_PROPERTY_MODERATE_VALUE);
		
		when(severityTranslator.toOpenmrsType(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE))
		        .thenReturn(moderateConcept);
		
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getSeverity(), notNullValue());
		assertThat(omrsAllergy.getSeverity(), equalTo(moderateConcept));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSeveritySevereCorrectly() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE);
		allergy.setReaction(Collections.singletonList(reactionComponent));
		
		Concept severeConcept = new Concept();
		severeConcept.setUuid(GLOBAL_PROPERTY_SEVERE_VALUE);
		when(severityTranslator.toOpenmrsType(AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE))
		        .thenReturn(severeConcept);
		
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getSeverity(), notNullValue());
		assertThat(omrsAllergy.getSeverity(), equalTo(severeConcept));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSeverityOtherCorrectly() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.NULL);
		allergy.setReaction(Collections.singletonList(reactionComponent));
		
		Concept otherConcept = new Concept();
		otherConcept.setUuid(GLOBAL_PROPERTY_OTHER_VALUE);
		
		when(severityTranslator.toOpenmrsType(AllergyIntolerance.AllergyIntoleranceSeverity.NULL)).thenReturn(otherConcept);
		
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getSeverity(), notNullValue());
		assertThat(omrsAllergy.getSeverity(), equalTo(otherConcept));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullReactionIfFhirReactionIsNull() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.setReaction(null);
		
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy.getReactions().size(), equalTo(0));
		assertThat(omrsAllergy.getSeverity(), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullReactionIfManifestationIsNull() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE);
		reactionComponent.setManifestation(null);
		allergy.addReaction(reactionComponent);
		
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy.getReactions().size(), equalTo(0));
	}
	
	@Test
	public void toOpenmrsType_shouldTransLateManifestationToReaction() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE);
		
		CodeableConcept manifestation = new CodeableConcept()
		        .addCoding(new Coding(FhirConstants.CLINICAL_FINDINGS_VALUE_SET_URI, CONCEPT_UUID, "Test Reaction"));
		reactionComponent.addManifestation(manifestation);
		allergy.addReaction(reactionComponent);
		
		Concept codedReaction = new Concept();
		codedReaction.setUuid(CONCEPT_UUID);
		
		when(conceptTranslator.toOpenmrsType(manifestation)).thenReturn(codedReaction);
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getReactions().size(), greaterThanOrEqualTo(1));
		assertThat(omrsAllergy.getReactions().get(0).getReaction().getUuid(), equalTo(CONCEPT_UUID));
	}
	
	@Test
	public void shouldAddProvenances() {
		org.openmrs.Allergy allergy = new org.openmrs.Allergy();
		allergy.setUuid(ALLERGY_UUID);
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		Allergen allergen = new Allergen();
		allergen.setCodedAllergen(concept);
		allergen.setNonCodedAllergen("Test Allergen");
		allergen.setAllergenType(AllergenType.FOOD);
		
		allergy.setAllergen(allergen);
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.uniqueUuid()));
		provenance.setRecorded(new Date());
		when(provenanceTranslator.getCreateProvenance(allergy)).thenReturn(provenance);
		when(provenanceTranslator.getUpdateProvenance(allergy)).thenReturn(provenance);
		
		AllergyIntolerance result = allergyIntoleranceTranslator.toFhirResource(allergy);
		List<Resource> resources = result.getContained();
		assertThat(resources, notNullValue());
		assertThat(resources, not(empty()));
		assertThat(resources.stream().findAny().isPresent(), CoreMatchers.is(true));
		assertThat(resources.stream().findAny().get().isResource(), CoreMatchers.is(true));
		assertThat(resources.stream().findAny().get().getResourceType().name(), equalTo(Provenance.class.getSimpleName()));
	}
}
