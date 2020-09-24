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

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.openmrs.AllergenType;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceCategoryTranslator;
import org.springframework.stereotype.Component;

@Component
public class AllergyIntoleranceCategoryTranslatorImpl implements AllergyIntoleranceCategoryTranslator {
	
	@Override
	public AllergyIntolerance.AllergyIntoleranceCategory toFhirResource(@Nonnull AllergenType allergenType) {
		switch (allergenType) {
			case DRUG:
				return AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION;
			case FOOD:
				return AllergyIntolerance.AllergyIntoleranceCategory.FOOD;
			case ENVIRONMENT:
				return AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT;
			case OTHER:
			default:
				return null;
		}
	}
	
	@Override
	public AllergenType toOpenmrsType(@Nonnull AllergyIntolerance.AllergyIntoleranceCategory allergyIntoleranceCategory) {
		switch (allergyIntoleranceCategory) {
			case MEDICATION:
				return AllergenType.DRUG;
			case FOOD:
				return AllergenType.FOOD;
			case ENVIRONMENT:
				return AllergenType.ENVIRONMENT;
			case BIOLOGIC:
			case NULL:
			default:
				return null;
		}
	}
}
