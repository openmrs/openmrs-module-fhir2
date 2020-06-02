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

import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceCriticalityTranslator;
import org.springframework.stereotype.Component;

@Component
public class AllergyIntoleranceCriticalityTranslatorImpl implements AllergyIntoleranceCriticalityTranslator {
	
	@Override
	public AllergyIntolerance.AllergyIntoleranceCriticality toFhirResource(
	        AllergyIntolerance.AllergyIntoleranceSeverity allergyIntoleranceSeverity) {
		switch (allergyIntoleranceSeverity) {
			case SEVERE:
				return AllergyIntolerance.AllergyIntoleranceCriticality.HIGH;
			case MILD:
				return AllergyIntolerance.AllergyIntoleranceCriticality.LOW;
			case MODERATE:
				return AllergyIntolerance.AllergyIntoleranceCriticality.UNABLETOASSESS;
			default:
				return AllergyIntolerance.AllergyIntoleranceCriticality.NULL;
		}
	}
}
