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

import org.hl7.fhir.r4.model.Encounter;
import org.openmrs.Location;

public interface EncounterLocationTranslator extends ToFhirTranslator<Location, Encounter.EncounterLocationComponent>, ToOpenmrsTranslator<Location, Encounter.EncounterLocationComponent> {
	
	/**
	 * Maps an {@link org.openmrs.Location} to an
	 * {@link org.hl7.fhir.r4.model.Encounter.EncounterLocationComponent}
	 * 
	 * @param location the OpenMRS location to translate
	 * @return the corresponding FHIR Encounter.EncounterLocationComponent resource
	 */
	@Override
	Encounter.EncounterLocationComponent toFhirResource(@Nonnull Location location);
	
	/**
	 * Maps an {@link org.hl7.fhir.r4.model.Encounter.EncounterLocationComponent} to an existing
	 * {@link org.openmrs.Location}
	 * 
	 * @param encounterLocationComponent the encounterLocationComponent to map
	 * @return an updated version of the location
	 */
	@Override
	Location toOpenmrsType(@Nonnull Encounter.EncounterLocationComponent encounterLocationComponent);
}
