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

import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Obs;

public interface ObservationComponentTranslator extends ToFhirTranslator<Obs, Observation.ObservationComponentComponent>, UpdatableOpenmrsTranslator<Obs, Observation.ObservationComponentComponent> {
	
	/**
	 * Maps an {@link Obs} to an {@link org.hl7.fhir.r4.model.Observation.ObservationComponentComponent}
	 * 
	 * @param obs the observation to translate
	 * @return the corresponding FHIR observation component
	 */
	@Override
	Observation.ObservationComponentComponent toFhirResource(Obs obs);
	
	/**
	 * Maps an {@link org.hl7.fhir.r4.model.Observation.ObservationComponentComponent} to an {@link Obs}
	 * 
	 * @param obs the existing observation
	 * @param observationComponent the observation to translate
	 * @return the corresponding OpenMRS observation
	 */
	@Override
	Obs toOpenmrsType(Obs obs, Observation.ObservationComponentComponent observationComponent);
}
