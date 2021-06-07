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

import org.hl7.fhir.r4.model.CodeableConcept;
import org.openmrs.Obs;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.translators.ObservationInterpretationTranslator;
import org.springframework.stereotype.Component;

@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.0.* - 2.0.*")
public class ObservationInterpretationTranslatorImpl implements ObservationInterpretationTranslator {
	
	@Override
	public CodeableConcept toFhirResource(@Nonnull Obs obs) {
		return null;
	}
	
	@Override
	public Obs toOpenmrsType(@Nonnull Obs existingObs, @Nonnull CodeableConcept resource) {
		return existingObs;
	}
}
