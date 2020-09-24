/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators;

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.openmrs.Allergy;

public interface AllergyIntoleranceReactionComponentTranslator extends ToFhirTranslator<Allergy, AllergyIntolerance.AllergyIntoleranceReactionComponent>, OpenmrsFhirUpdatableTranslator<Allergy, AllergyIntolerance.AllergyIntoleranceReactionComponent> {
	
	/**
	 * Maps {@link org.openmrs.Allergy} element to a
	 * {@link org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceReactionComponent}
	 *
	 * @param allergy the OpenMRS allergy type to translate
	 * @return the corresponding
	 *         {@link org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceReactionComponent}
	 */
	@Override
	AllergyIntolerance.AllergyIntoleranceReactionComponent toFhirResource(@Nonnull Allergy allergy);
	
	/**
	 * Maps an {@link org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceReactionComponent} to a
	 * {@link org.openmrs.Allergy}
	 *
	 * @param reactionComponent the FHIR allergyIntoleranceReactionComponent to translate
	 * @return the updated {@link org.openmrs.Allergy} object
	 */
	@Override
	Allergy toOpenmrsType(@Nonnull Allergy allergy,
	        @Nonnull AllergyIntolerance.AllergyIntoleranceReactionComponent reactionComponent);
}
