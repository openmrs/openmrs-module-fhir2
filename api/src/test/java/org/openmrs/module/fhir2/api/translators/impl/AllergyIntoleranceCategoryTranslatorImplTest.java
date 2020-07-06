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
import static org.hamcrest.Matchers.nullValue;

import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.AllergenType;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceCategoryTranslator;

public class AllergyIntoleranceCategoryTranslatorImplTest {
	
	private AllergyIntoleranceCategoryTranslator categoryTranslator;
	
	@Before
	public void setup() {
		categoryTranslator = new AllergyIntoleranceCategoryTranslatorImpl();
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugAllergenTypeToMedicationCategory() {
		assertThat(categoryTranslator.toFhirResource(AllergenType.DRUG),
		    equalTo(AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION));
	}
	
	@Test
	public void toFhirResource_shouldTranslateFoodAllergenTypeToFoodCategory() {
		assertThat(categoryTranslator.toFhirResource(AllergenType.FOOD),
		    equalTo(AllergyIntolerance.AllergyIntoleranceCategory.FOOD));
	}
	
	@Test
	public void toFhirResource_shouldTranslateEnvironmentAllergenTypeToEnvironmentCategory() {
		assertThat(categoryTranslator.toFhirResource(AllergenType.ENVIRONMENT),
		    equalTo(AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOtherAllergenTypeToNullCategory() {
		assertThat(categoryTranslator.toFhirResource(AllergenType.OTHER), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateMedicationCategoryToDrugAllergenType() {
		assertThat(categoryTranslator.toOpenmrsType(AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION),
		    equalTo(AllergenType.DRUG));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFoodCategoryToFoodAllergenType() {
		assertThat(categoryTranslator.toOpenmrsType(AllergyIntolerance.AllergyIntoleranceCategory.FOOD),
		    equalTo(AllergenType.FOOD));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateEnvironmentCategoryToEnvironmentAllergenType() {
		assertThat(categoryTranslator.toOpenmrsType(AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT),
		    equalTo(AllergenType.ENVIRONMENT));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateBiologicCategoryToNullAllergenType() {
		assertThat(categoryTranslator.toOpenmrsType(AllergyIntolerance.AllergyIntoleranceCategory.BIOLOGIC), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateNullCategoryToNullAllergenType() {
		assertThat(categoryTranslator.toOpenmrsType(AllergyIntolerance.AllergyIntoleranceCategory.NULL), nullValue());
	}
}
