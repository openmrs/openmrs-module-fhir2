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
import org.hl7.fhir.r4.model.ContactPoint;
import org.openmrs.LocationAttribute;
import org.openmrs.api.LocationService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.translators.LocationTelecomTranslator;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static org.apache.commons.lang.Validate.notNull;

@Component
@Setter(AccessLevel.PACKAGE)
public class LocationTelecomTranslatorImpl implements LocationTelecomTranslator {
	
	@Inject
	LocationService locationService;
	
	@Inject
	FhirGlobalPropertyService globalPropertyService;
	
	@Override
	public ContactPoint toFhirResource(LocationAttribute locationAttribute) {
		ContactPoint contactPoint = new ContactPoint();
		if (locationAttribute != null) {
			contactPoint.setId(locationAttribute.getUuid());
			contactPoint.setValue(locationAttribute.getValue().toString());
		}
		return contactPoint;
	}
	
	@Override
	public LocationAttribute toOpenmrsType(ContactPoint contactPoint) {
		LocationAttribute locationAttribute = new LocationAttribute();
		if (contactPoint != null) {
			locationAttribute.setUuid(contactPoint.getId());
			locationAttribute.setValue(contactPoint.getValue());
			locationAttribute.setAttributeType(locationService.getLocationAttributeTypeByUuid(globalPropertyService
			        .getGlobalProperty(FhirConstants.LOCATION_ATTRIBUTE_TYPE_PROPERTY)));
		}
		return locationAttribute;
	}
	
	@Override
	public LocationAttribute toOpenmrsType(LocationAttribute locationAttribute, ContactPoint contactPoint) {
		notNull(locationAttribute, "location attribute to be updated cannot be null");
		if (contactPoint == null) {
			return locationAttribute;
		}
		locationAttribute.setUuid(contactPoint.getId());
		locationAttribute.setValue(contactPoint.getValue());
		return locationAttribute;
	}
}
