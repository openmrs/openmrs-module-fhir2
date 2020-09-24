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

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Obs;
import org.openmrs.annotation.OpenmrsProfile;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.1.* - 2.*")
public class ObservationStatusTranslatorImpl_2_1 extends ObservationStatusTranslatorImpl {
	
	@Override
	public Observation.ObservationStatus toFhirResource(@Nonnull Obs obs) {
		return Observation.ObservationStatus.valueOf(obs.getStatus().toString());
	}
	
	@Override
	public Obs toOpenmrsType(@Nonnull Obs observation, @Nonnull Observation.ObservationStatus observationStatus) {
		if (observationStatus.equals(Observation.ObservationStatus.PRELIMINARY)
		        || observationStatus.equals(Observation.ObservationStatus.FINAL)
		        || observationStatus.equals(Observation.ObservationStatus.AMENDED)) {
			observation.setStatus(Obs.Status.valueOf(observationStatus.toString()));
		}
		return observation;
	}
}
