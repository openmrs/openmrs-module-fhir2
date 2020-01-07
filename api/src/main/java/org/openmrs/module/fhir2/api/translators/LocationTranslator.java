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

import org.hl7.fhir.r4.model.Location;

public interface LocationTranslator extends OpenmrsFhirTranslator<org.openmrs.Location, org.hl7.fhir.r4.model.Location> {
	
	/**
	 * Maps an {@link org.openmrs.Location} to a {@link org.hl7.fhir.r4.model.Location}
	 * 
	 * @param openmrsLocation the location to translate
	 * @return the corresponding FHIR location resource
	 */
	@Override
	Location toFhirResource(org.openmrs.Location openmrsLocation);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Location} to an {@link org.openmrs.Location}
	 * 
	 * @param fhirLocation the FHIR location to translate
	 * @return the corresponding OpenMRS location
	 */
	@Override
	org.openmrs.Location toOpenmrsType(Location fhirLocation);
}
