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

import org.hl7.fhir.r4.model.Enumerations;

public interface GenderTranslator extends OpenmrsFhirTranslator<String, Enumerations.AdministrativeGender> {
	
	/**
	 * Maps an OpenMRS gender code to an {@link org.hl7.fhir.r4.model.Enumerations.AdministrativeGender}
	 * 
	 * @param gender the gender to translate
	 * @return the corresponding FHIR gender
	 */
	@Override
	Enumerations.AdministrativeGender toFhirResource(@Nonnull String gender);
	
	/**
	 * Maps an {@link org.hl7.fhir.r4.model.Enumerations.AdministrativeGender} to an OpenMRS gender code
	 * 
	 * @param gender the gender to translate
	 * @return the corresponding OpenMRS gender code
	 */
	@Override
	String toOpenmrsType(@Nonnull Enumerations.AdministrativeGender gender);
}
