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

import org.hl7.fhir.r4.model.Practitioner;

public interface PractitionerTranslator<T> extends OpenmrsFhirUpdatableTranslator<T, Practitioner> {
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Practitioner} to an existing generic type <T> it can be
	 * {@link org.openmrs.Provider} or {@link org.openmrs.User}
	 * 
	 * @param existingUserOrProvider the existingUserOrProvider to update
	 * @param practitioner the FHIR practitioner to map
	 * @return an updated version of the OpenMrs existingUserOrProvider
	 */
	@Override
	T toOpenmrsType(@Nonnull T existingUserOrProvider, @Nonnull Practitioner practitioner);
	
	/**
	 * Maps {@link org.openmrs.Provider} or {@link org.openmrs.User} to a
	 * {@link org.hl7.fhir.r4.model.Practitioner} resource
	 * 
	 * @param userOrProvider the OpenMRS userOrProvider to translate
	 * @return the corresponding FHIR practitioner resource
	 */
	@Override
	Practitioner toFhirResource(@Nonnull T userOrProvider);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Practitioner} to {@link org.openmrs.Provider} or
	 * {@link org.openmrs.User}
	 * 
	 * @param practitioner the FHIR practitioner to translate
	 * @return the corresponding OpenMrs provider
	 */
	@Override
	T toOpenmrsType(@Nonnull Practitioner practitioner);
}
