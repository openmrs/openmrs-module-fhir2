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

import java.util.Collection;
import java.util.Map;

import org.hl7.fhir.r4.model.Location;

public interface LocationTranslator extends OpenmrsFhirUpdatableTranslator<org.openmrs.Location, Location> {
	
	/**
	 * Maps an {@link org.openmrs.Location} to a {@link org.hl7.fhir.r4.model.Location}
	 * 
	 * @param openmrsLocation the location to translate
	 * @return the corresponding FHIR location resource
	 */
	@Override
	Location toFhirResource(@Nonnull org.openmrs.Location openmrsLocation);
	
	/**
	 * Maps a collection of {@link org.openmrs.Location}s to a {@link org.hl7.fhir.r4.model.Location}
	 *
	 * @param openmrsLocations the collection of locations to translate
	 * @return the mapping of OpenMRS location to corresponding FHIR location resource
	 */
	@Override
	Map<org.openmrs.Location, Location> toFhirResources(Collection<org.openmrs.Location> openmrsLocations);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Location} to an {@link org.openmrs.Location}
	 * 
	 * @param fhirLocation the FHIR location to translate
	 * @return the corresponding OpenMRS location
	 */
	@Override
	org.openmrs.Location toOpenmrsType(@Nonnull Location fhirLocation);
	
	/**
	 * Maps a {@link Location} to an existing {@link org.openmrs.Location}
	 *
	 * @param existingLocation the location to update
	 * @param fhirLocation the FHIR location to map
	 * @return the updated OpenMRS location
	 */
	@Override
	org.openmrs.Location toOpenmrsType(@Nonnull org.openmrs.Location existingLocation, @Nonnull Location fhirLocation);
}
