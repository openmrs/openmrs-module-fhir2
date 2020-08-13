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

import static org.apache.commons.lang3.Validate.notNull;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Encounter;
import org.openmrs.Location;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.api.translators.EncounterLocationTranslator;
import org.openmrs.module.fhir2.api.translators.LocationTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class EncounterLocationTranslatorImpl extends BaseReferenceHandlingTranslator implements EncounterLocationTranslator {
	
	@Autowired
	FhirLocationService locationService;
	
	@Autowired
	LocationTranslator locationTranslator;
	
	@Override
	public Encounter.EncounterLocationComponent toFhirResource(Location location) {
		if (location == null) {
			return null;
		}
		
		Encounter.EncounterLocationComponent locationComponent = new Encounter.EncounterLocationComponent();
		locationComponent.setLocation(createLocationReference(location));
		return locationComponent;
	}
	
	@Override
	public Location toOpenmrsType(Encounter.EncounterLocationComponent encounterLocationComponent) {
		notNull(encounterLocationComponent, "The EncounterLocationComponent object should not be null");
		
		String locationUuid = getReferenceId(encounterLocationComponent.getLocation());
		return locationTranslator.toOpenmrsType(locationService.get(locationUuid));
	}
}
