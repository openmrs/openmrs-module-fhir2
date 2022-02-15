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

import org.hl7.fhir.r4.model.Media;
import org.openmrs.Obs;

public interface MediaTranslator extends OpenmrsFhirTranslator<Obs, Media>, OpenmrsFhirUpdatableTranslator<Obs, Media> {
	
	/**
	 * Maps an {@link org.openmrs.Obs} to a {@link org.hl7.fhir.r4.model.Media}
	 *
	 * @param data the FHIR Media to translate
	 * @return the corresponding FHIR media resource
	 */
	@Override
	Media toFhirResource(@Nonnull Obs data);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Media} to an {@link org.openmrs.Obs}
	 *
	 * @param resource the FHIR Media resource to translate
	 * @return the corresponding OpenMRS observation resource
	 */
	@Override
	Obs toOpenmrsType(@Nonnull Media resource);
	
	/**
	 * Maps a {@link Media} to an existing {@link org.openmrs.Obs}
	 *
	 * @param existingObject the observation to update
	 * @param resource the FHIR complex object to map
	 * @return the updated OpenMRS observation
	 */
	@Override
	Obs toOpenmrsType(@Nonnull Obs existingObject, @Nonnull Media resource);
}
