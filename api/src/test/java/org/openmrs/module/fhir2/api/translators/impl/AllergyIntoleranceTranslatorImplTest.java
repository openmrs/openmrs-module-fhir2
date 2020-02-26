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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.hamcrest.MockitoHamcrest;
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
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class AllergyIntoleranceTranslatorImplTest {
	
	private static final String ALLERGY_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
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
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private FhirConceptService conceptService;
	
	private AllergyIntoleranceTranslatorImpl allergyIntoleranceTranslator;
	
	private Allergy omrsAllergy;
	
	@Before
	public void setUp() {
		allergyIntoleranceTranslator = new AllergyIntoleranceTranslatorImpl();
		allergyIntoleranceTranslator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
		allergyIntoleranceTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
		allergyIntoleranceTranslator.setGlobalPropertyService(globalPropertyService);
		allergyIntoleranceTranslator.setConceptService(conceptService);
		omrsAllergy = new Allergy();
		Allergen allergen = new Allergen(AllergenType.FOOD, null, "Test allergen");
		omrsAllergy.setAllergen(allergen);
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
	public void toFhirResource_shouldTranslateAllrgenToAllergySubstance() {
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		
		Allergen allergen = new Allergen();
		allergen.setCodedAllergen(concept);
		allergen.setAllergenType(AllergenType.FOOD);
		omrsAllergy.setAllergen(allergen);
		
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getCode().getCoding().get(0).getCode(), equalTo(CONCEPT_UUID));
		assertThat(allergyIntolerance.getCode().getCoding().get(0).getSystem(),
		    equalTo(FhirConstants.ALLERGY_SUBSTANCE_VALUE_SET_URI));
	}
	
	@Test
	public void toFhirResource_shouldTranslateReactionToManifestation() {
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		
		AllergyReaction reaction = new AllergyReaction();
		reaction.setReaction(concept);
		reaction.setAllergy(omrsAllergy);
		reaction.setReactionNonCoded(NON_CODED_REACTION);
		
		omrsAllergy.setReactions(Collections.singletonList(reaction));
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getReaction().get(0).getManifestation().get(0).getCoding().get(0).getDisplay(),
		    equalTo(NON_CODED_REACTION));
		assertThat(allergyIntolerance.getReaction().get(0).getManifestation().get(0).getCoding().get(0).getSystem(),
		    equalTo(FhirConstants.CLINICAL_FINDINGS_VALUE_SET_URI));
		assertThat(allergyIntolerance.getReaction().get(0).getManifestation().get(0).getText(), equalTo(NON_CODED_REACTION));
		assertThat(allergyIntolerance.getReaction().get(0).getDescription(), equalTo(NON_CODED_REACTION));
	}
	
	@Test
	public void toFhirResource_shouldTranslateReactionMildCorrectly() {
		Concept mildConcept = new Concept();
		mildConcept.setUuid(GLOBAL_PROPERTY_MILD_VALUE);
		omrsAllergy.setSeverity(mildConcept);
		
		when(globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_MILD, "")).thenReturn(GLOBAL_PROPERTY_MILD_VALUE);
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
		
		when(globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_MODERATE, ""))
		        .thenReturn(GLOBAL_PROPERTY_MODERATE_VALUE);
		when(globalPropertyService.getGlobalProperty(MockitoHamcrest.argThat(not(equalTo(FhirConstants.GLOBAL_PROPERTY_MODERATE))),
		    anyString())).thenReturn("");
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
		
		when(globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_SEVERE, "")).thenReturn(GLOBAL_PROPERTY_SEVERE_VALUE);
		when(globalPropertyService.getGlobalProperty(MockitoHamcrest.argThat(not(equalTo(FhirConstants.GLOBAL_PROPERTY_SEVERE))),
		    anyString())).thenReturn("");
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
		
		when(globalPropertyService.getGlobalProperty(MockitoHamcrest.argThat(not(equalTo(FhirConstants.GLOBAL_PROPERTY_OTHER))),
		    anyString())).thenReturn("");
		AllergyIntolerance allergyIntolerance = allergyIntoleranceTranslator.toFhirResource(omrsAllergy);
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getReaction().get(0).getSeverity(),
		    equalTo(AllergyIntolerance.AllergyIntoleranceSeverity.NULL));
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
	public void toOpenmrsType_shouldTranslateRecordedDateToOpenmrsDateCreated() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.setRecordedDate(new Date());
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getDateCreated(), DateMatchers.sameDay(new Date()));
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
	public void toOpenmrsType_shouldTranslateLastUpdatedDateToDateChanged() {
		AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
		allergyIntolerance.getMeta().setLastUpdated(new Date());
		
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergyIntolerance);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getDateChanged(), DateMatchers.sameDay(new Date()));
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
	public void toOpenmrsType_shouldTranslateAllergySubstanceToAllergen() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.setCode(new CodeableConcept().addCoding(new Coding("", CONCEPT_UUID, "Coded Allergen")));
		
		Concept allergen = new Concept();
		allergen.setUuid(CONCEPT_UUID);
		when(conceptService.getConceptByUuid(CONCEPT_UUID)).thenReturn(Optional.of(allergen));
		omrsAllergy.setAllergen(null);
		
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(allergen.getUuid(), equalTo(CONCEPT_UUID));
		assertThat(omrsAllergy.getAllergen().getNonCodedAllergen(), equalTo("Coded Allergen"));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSeverityMildCorrectly() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.MILD);
		allergy.setReaction(Collections.singletonList(reactionComponent));
		
		Concept mildConcept = new Concept();
		mildConcept.setUuid(GLOBAL_PROPERTY_MILD_VALUE);
		when(globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_MILD)).thenReturn(GLOBAL_PROPERTY_MILD_VALUE);
		when(conceptService.getConceptByUuid(GLOBAL_PROPERTY_MILD_VALUE)).thenReturn(Optional.of(mildConcept));
		
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getSeverity(), notNullValue());
		assertThat(omrsAllergy.getSeverity(), equalTo(mildConcept));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSeverityModerateCorrectly() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
		reactionComponent.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE);
		allergy.setReaction(Collections.singletonList(reactionComponent));
		
		Concept moderateConcept = new Concept();
		moderateConcept.setUuid(GLOBAL_PROPERTY_MODERATE_VALUE);
		when(globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_MODERATE)).thenReturn(GLOBAL_PROPERTY_MODERATE_VALUE);
		when(conceptService.getConceptByUuid(GLOBAL_PROPERTY_MODERATE_VALUE)).thenReturn(Optional.of(moderateConcept));
		
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
		when(globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_SEVERE)).thenReturn(GLOBAL_PROPERTY_SEVERE_VALUE);
		when(conceptService.getConceptByUuid(GLOBAL_PROPERTY_SEVERE_VALUE)).thenReturn(Optional.of(severeConcept));
		
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
		when(globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_OTHER)).thenReturn(GLOBAL_PROPERTY_OTHER_VALUE);
		when(conceptService.getConceptByUuid(GLOBAL_PROPERTY_OTHER_VALUE)).thenReturn(Optional.of(otherConcept));
		
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
		reactionComponent.addManifestation(new CodeableConcept()
		        .addCoding(new Coding(FhirConstants.CLINICAL_FINDINGS_VALUE_SET_URI, CONCEPT_UUID, "Test Reaction")));
		allergy.addReaction(reactionComponent);
		
		Concept codedReaction = new Concept();
		codedReaction.setUuid(CONCEPT_UUID);
		
		when(conceptService.getConceptByUuid(CONCEPT_UUID)).thenReturn(Optional.of(codedReaction));
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getReactions().size(), greaterThanOrEqualTo(1));
		assertThat(omrsAllergy.getReactions().get(0).getReaction().getUuid(), equalTo(CONCEPT_UUID));
	}
}
