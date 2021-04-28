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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Media;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.translators.MediaStatusTranslator;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static org.apache.commons.lang3.Validate.notNull;

@Component
@Setter(AccessLevel.PACKAGE)
public class MediaStatusTranslatorImpl implements MediaStatusTranslator {
	
	@Override
	public Media.MediaStatus toFhirResource(@Nonnull Obs data) {
		return Media.MediaStatus.UNKNOWN;
	}
	
	@Override
	public Obs toOpenmrsType(@Nonnull Obs existingObject, @Nonnull Media.MediaStatus resource) {
		notNull(existingObject, "The existing Obs object should not be null");
		notNull(resource, "The Media object should not be null");

		return existingObject;
	}
	
	@Override
	public Obs toOpenmrsType(@Nonnull Media.MediaStatus resource) {
		notNull(resource, "The Media object should not be null");
		return null;
	}
}
