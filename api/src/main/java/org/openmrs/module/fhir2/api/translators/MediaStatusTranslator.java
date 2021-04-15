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

public interface MediaStatusTranslator extends OpenmrsFhirUpdatableTranslator<Obs, Media.MediaStatus> {
	
	/**
	 * Maps an {@link Obs} to an {@link org.hl7.fhir.r4.model.Media.MediaStatus}
	 *
	 * @param data the complex obs to translate
	 * @return the corresponding media resource
	 */
	@Override
	Media.MediaStatus toFhirResource(@Nonnull Obs data);
	
	/**
	 * Maps an {@link org.hl7.fhir.r4.model.Media.MediaStatus} to an {@link Obs}
	 *
	 * @param existingObject the observation to update
	 * @param resource the media status
	 * @return the corresponding media resource status
	 */
	@Override
	Obs toOpenmrsType(@Nonnull Obs existingObject, @Nonnull Media.MediaStatus resource);
}
