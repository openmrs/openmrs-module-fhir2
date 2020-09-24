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

import java.util.function.Supplier;

import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Obs;

public interface ObservationTranslator extends ToFhirTranslator<Obs, Observation>, OpenmrsFhirUpdatableTranslator<Obs, Observation> {
	
	/**
	 * Maps an {@link org.openmrs.Obs} to an {@link org.hl7.fhir.r4.model.Observation}
	 * 
	 * @param observation the observation to translate
	 * @return the corresponding FHIR observation
	 */
	@Override
	Observation toFhirResource(@Nonnull Obs observation);
	
	/**
	 * Maps an {@link org.hl7.fhir.r4.model.Observation} to an existing {@link org.openmrs.Obs}
	 * 
	 * @param existingObs the observation to update
	 * @param observation the observation to translate
	 * @return an updated version of the current obs
	 */
	@Override
	default Obs toOpenmrsType(@Nonnull Obs existingObs, @Nonnull Observation observation) {
		return toOpenmrsType(existingObs, observation, () -> {
			Obs obs = new Obs();
			obs.setEncounter(existingObs.getEncounter());
			obs.setPerson(existingObs.getPerson());
			obs.setLocation(existingObs.getLocation());
			return obs;
		});
	}
	
	/**
	 * Maps an {@link org.hl7.fhir.r4.model.Observation} to an existing {@link org.openmrs.Obs}, but
	 * provides a means to update an existing Obs in the Obs group
	 *
	 * @param existingObs the observation to update
	 * @param observation the observation to translate
	 * @param groupedObsFactory function to return a new Obs for the obs group
	 * @return an updated version of the current obs
	 */
	Obs toOpenmrsType(Obs existingObs, Observation observation, Supplier<Obs> groupedObsFactory);
	
	Obs toOpenmrsType(@Nonnull Observation observation);
}
