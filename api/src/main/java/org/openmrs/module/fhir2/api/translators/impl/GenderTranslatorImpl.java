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

import org.hl7.fhir.r4.model.Enumerations;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.springframework.stereotype.Component;

@Component
public class GenderTranslatorImpl implements GenderTranslator {
	
	@Override
	public Enumerations.AdministrativeGender toFhirResource(String gender) {
		if (gender == null) {
			return Enumerations.AdministrativeGender.NULL;
		}
		
		switch (gender.toUpperCase()) {
			case "M":
				return Enumerations.AdministrativeGender.MALE;
			case "F":
				return Enumerations.AdministrativeGender.FEMALE;
			case "U":
				return Enumerations.AdministrativeGender.UNKNOWN;
			case "O":
				return Enumerations.AdministrativeGender.OTHER;
			default:
				return Enumerations.AdministrativeGender.NULL;
		}
	}
	
	@Override
	public String toOpenmrsType(Enumerations.AdministrativeGender gender) {
		if (gender == null) {
			return null;
		}
		
		switch (gender) {
			case MALE:
				return "M";
			case FEMALE:
				return "F";
			case UNKNOWN:
				return "U";
			case OTHER:
				return "O";
			default:
				return null;
		}
	}
}
