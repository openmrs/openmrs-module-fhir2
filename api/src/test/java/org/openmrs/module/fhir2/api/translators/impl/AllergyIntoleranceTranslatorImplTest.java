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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Allergen;
import org.openmrs.AllergenType;
import org.openmrs.Allergy;

@RunWith(MockitoJUnitRunner.class)
public class AllergyIntoleranceTranslatorImplTest {
	
	private static final String ALLERGY_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String ALLERGY_INTOLERANCE_CLINICAL_STATUC_ACTIVE = "active";
	
	private static final String ALLERGY_INTOLERANCE_CLINICAL_STATUC_INACTIVE = "inactive";
	
	private AllergyIntoleranceTranslatorImpl allergyIntoleranceTranslator;
	
	private Allergy omrsAllergy;
	
	@Before
	public void setUp() {
		allergyIntoleranceTranslator = new AllergyIntoleranceTranslatorImpl();
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
	public void toOpenmrsType_shouldReturnNullWhenCalledWithFhirNullCategory() {
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
	public void toOpenmrsType_shouldTranslateInactiveToVoidedFalse() {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.setClinicalStatus(
		    new CodeableConcept().addCoding(new Coding("", ALLERGY_INTOLERANCE_CLINICAL_STATUC_ACTIVE, "")));
		allergyIntoleranceTranslator.toOpenmrsType(omrsAllergy, allergy);
		assertThat(omrsAllergy, notNullValue());
		assertThat(omrsAllergy.getVoided(), equalTo(false));
	}
	
}
