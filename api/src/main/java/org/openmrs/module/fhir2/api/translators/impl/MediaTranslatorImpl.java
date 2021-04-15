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
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.translators.MediaTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationStatusTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static org.apache.commons.lang.Validate.notNull;

@Component
@Setter(AccessLevel.PACKAGE)
public class MediaTranslatorImpl implements MediaTranslator {
	
	@Autowired
	private ObservationStatusTranslator observationStatusTranslator;
	
	@Override
	public Media toFhirResource(@Nonnull Obs data) {
		notNull(data, "The Openmrs Complex obs object should not be null");
		
		Observation obs = new Observation();
		obs.setId(data.getUuid());
		obs.setStatus(observationStatusTranslator.toFhirResource(data));
		Media media = new Media();
		return media;
	}
	
	@Override
	public Obs toOpenmrsType(@Nonnull Media resource) {
		notNull(resource, "The media resource should not be null");
		return toOpenmrsType(new Obs(), resource);
	}
	
	@Override
	public Obs toOpenmrsType(@Nonnull Obs existingObject, @Nonnull Media resource) {
		notNull(existingObject, "The existing object should not be null");
		notNull(resource, "The observation object should not be null");
		return existingObject;
	}
}
