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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Allergen;
import org.openmrs.AllergenType;
import org.openmrs.Allergy;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirTestConstants;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class AllergyIntoleranceTranslatorImplTest {
	
	private static final String ALLERGY_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String ALLERGY_INTOLERANCE_CLINICAL_STATUC_ACTIVE = "active";
	
	private static final String ALLERGY_INTOLERANCE_CLINICAL_STATUC_INACTIVE = "inactive";
	
	private static final String PATIENT_UUID = "c0938432-1691-11df-9pa5-7038c432aaba";
	
	private static final String CREATOR_UUID = "c1038432-1691-11df-9pa5-7038c432aaba";
	
	@Mock
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	private AllergyIntoleranceTranslatorImpl allergyIntoleranceTranslator;
	
	private Allergy omrsAllergy;
	
	@Before
	public void setUp() {
		allergyIntoleranceTranslator = new AllergyIntoleranceTranslatorImpl();
		allergyIntoleranceTranslator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
		allergyIntoleranceTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
		omrsAllergy = new Allergy();
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
		assertThat(omrsAllergy.getDateChanged(), DateMatchers.sameDay(new Date()));
	}
}
