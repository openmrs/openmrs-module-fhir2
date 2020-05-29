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

import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.openmrs.Concept;

public interface AllergyIntoleranceSeverityTranslator extends ToFhirTranslator<Concept, AllergyIntolerance.AllergyIntoleranceSeverity>, ToOpenmrsTranslator<Concept, AllergyIntolerance.AllergyIntoleranceSeverity> {
	
	/**
	 * Maps {@link org.openmrs.Concept} element to a
	 * {@link org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceSeverity}
	 *
	 * @param concept the OpenMRS concept to translate
	 * @return the corresponding
	 *         {@link org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceSeverity}
	 */
	@Override
	AllergyIntolerance.AllergyIntoleranceSeverity toFhirResource(Concept concept);
	
	/**
	 * Maps an {@link org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceSeverity} to a
	 * {@link org.openmrs.Concept}
	 *
	 * @param allergyIntoleranceSeverity the FHIR allergyIntoleranceSeverity to translate
	 * @return the corresponding {@link org.openmrs.Concept}
	 */
	@Override
	Concept toOpenmrsType(AllergyIntolerance.AllergyIntoleranceSeverity allergyIntoleranceSeverity);
}
