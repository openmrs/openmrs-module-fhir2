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

import org.hl7.fhir.r4.model.Flag;
import org.openmrs.module.fhir2.model.FhirFlag;

public interface FlagTranslator extends OpenmrsFhirUpdatableTranslator<FhirFlag, Flag> {
	
	/**
	 * Maps an OpenMRS data element to a FHIR resource
	 *
	 * @param data the OpenMRS data element to translate
	 * @return the corresponding FHIR resource
	 */
	@Override
	Flag toFhirResource(@Nonnull FhirFlag data);
	
	/**
	 * Maps a FHIR resource to an OpenMRS data element
	 *
	 * @param resource the FHIR resource to translate
	 * @return the corresponding OpenMRS data element
	 */
	@Override
	FhirFlag toOpenmrsType(@Nonnull Flag resource);
	
	/**
	 * Maps a FHIR resource to an existing OpenMRS data element
	 *
	 * @param existingObject the existingObject to update
	 * @param resource the resource to map
	 * @return an updated version of the existingObject
	 */
	@Override
	FhirFlag toOpenmrsType(@Nonnull FhirFlag existingObject, @Nonnull Flag resource);
}
