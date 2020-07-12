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
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.module.fhir2.api.translators.LocationReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class LocationReferenceTranslatorImpl extends BaseReferenceHandlingTranslator implements LocationReferenceTranslator {
	
	@Autowired
	private LocationService locationService;
	
	@Override
	public Reference toFhirResource(Location location) {
		if (location == null) {
			return null;
		}
		
		return createLocationReference(location);
	}
	
	@Override
	public Location toOpenmrsType(Reference location) {
		if (location == null) {
			return null;
		}
		
		if (!getReferenceType(location).equals("Location")) {
			throw new IllegalArgumentException("Reference must be to an Location not a " + location.getType());
		}
		
		return getReferenceId(location).map(uuid -> locationService.getLocationByUuid(uuid)).orElse(null);
	}
	
}
