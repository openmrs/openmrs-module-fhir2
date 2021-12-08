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

import org.hl7.fhir.r4.model.DateType;
import org.openmrs.Person;

public interface BirthDateTranslator extends ToFhirTranslator<Person, DateType>, UpdatableOpenmrsTranslator<Person, DateType> {
	
	/**
	 * Maps an OpenMRS person to a FHIR date representing the birthdate
	 *
	 * @param person the OpenMRS person to translate
	 * @return the corresponding FHIR date
	 */
	@Override
	DateType toFhirResource(@Nonnull Person person);
	
	/**
	 * Maps a FHIR date to the supplied OpenMRS person's birthdate
	 *
	 * @param person the person object to add the birthdate to
	 * @param date the FHIR date to translate
	 * @return the corresponding OpenMRS data element
	 */
	@Override
	Person toOpenmrsType(@Nonnull Person person, @Nonnull DateType date);
}
