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
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.module.fhir2.api.translators.LocationTagTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class LocationTagTranslatorImpl implements LocationTagTranslator {
	
	@Autowired
	LocationService locationService;
	
	@Override
	public LocationTag toOpenmrsType(Coding tag) {
		LocationTag existingTag = locationService.getLocationTagByName(tag.getCode());
		if (existingTag != null) {
			return existingTag;
		} else {
			return locationService.saveLocationTag(new LocationTag(tag.getCode(), tag.getDisplay()));
		}
	}
}
