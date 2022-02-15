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

public interface MediaContentTranslator extends OpenmrsFhirUpdatableTranslator<Obs, Media> {
	
	/**
	 * Maps an {@link org.openmrs.Obs} to a corresponding {@link org.hl7.fhir.r4.model.Type}
	 *
	 * @param data the obs data to translate
	 * @return the corresponding FHIR base64 encoded version of the data
	 */
	@Override
	Media toFhirResource(@Nonnull Obs data);
	
	/**
	 * Maps an {@link org.hl7.fhir.r4.model.Type} to a existing {@link org.openmrs.Obs}
	 *
	 * @param existingObject the obs data to update
	 * @param resource the FHIR base64 encoded version of the data to map
	 * @return an updated version of the obs data
	 */
	@Override
	Obs toOpenmrsType(@Nonnull Obs existingObject, @Nonnull Media resource);
}
