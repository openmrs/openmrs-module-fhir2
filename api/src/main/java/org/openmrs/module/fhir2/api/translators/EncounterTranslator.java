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

public interface EncounterTranslator<T> extends OpenmrsFhirUpdatableTranslator<T, org.hl7.fhir.r4.model.Encounter> {
	
	/**
	 * Maps {@link org.openmrs.Encounter} to a {@link org.hl7.fhir.r4.model.Encounter} resource
	 * 
	 * @param encounter the OpenMRS encounter to translate
	 * @return the corresponding FHIR Encounter resource
	 */
	@Override
	org.hl7.fhir.r4.model.Encounter toFhirResource(@Nonnull T encounter);
	
	/**
	 * Maps {@link org.hl7.fhir.r4.model.Encounter} to {@link org.openmrs.Encounter}
	 * 
	 * @param encounter the FHIR encounter to translate
	 * @return the corresponding OpenMRS Encounter
	 */
	@Override
	T toOpenmrsType(@Nonnull org.hl7.fhir.r4.model.Encounter encounter);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Encounter} to an existing {@link org.openmrs.Encounter}
	 * 
	 * @param existingEncounter the existingEncounter to update
	 * @param encounter the encounter to map
	 * @return an updated version of the existingEncounter
	 */
	@Override
	T toOpenmrsType(@Nonnull T existingEncounter, @Nonnull org.hl7.fhir.r4.model.Encounter encounter);
}
