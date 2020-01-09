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
import org.hl7.fhir.r4.model.Location;
import org.openmrs.module.fhir2.api.translators.LocationAddressTranslator;
import org.openmrs.module.fhir2.api.translators.LocationTranslator;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
@Setter(AccessLevel.PACKAGE)
public class LocationTranslatorImpl implements LocationTranslator {
	
	@Inject
	private LocationAddressTranslator locationAddressTranslator;
	
	/**
	 * @see org.openmrs.module.fhir2.api.translators.LocationTranslator#toFhirResource(org.openmrs.Location)
	 */
	@Override
	public Location toFhirResource(org.openmrs.Location openmrsLocation) {
		Location fhirLocation = new Location();
		if (openmrsLocation != null) {
			Location.LocationPositionComponent position = new Location.LocationPositionComponent();
			fhirLocation.setId(openmrsLocation.getUuid());
			fhirLocation.setName(openmrsLocation.getName());
			fhirLocation.setDescription(openmrsLocation.getDescription());
			fhirLocation.setAddress(locationAddressTranslator.toFhirResource(openmrsLocation));
			
			if (openmrsLocation.getLatitude() != null) {
				position.setLatitude(Double.parseDouble(openmrsLocation.getLatitude()));
			}
			if (openmrsLocation.getLongitude() != null) {
				position.setLongitude(Double.parseDouble(openmrsLocation.getLongitude()));
			}
			
			fhirLocation.setPosition(position);
			
			if (!openmrsLocation.getRetired()) {
				fhirLocation.setStatus(Location.LocationStatus.ACTIVE);
			}
			
			if (openmrsLocation.getRetired()) {
				fhirLocation.setStatus(Location.LocationStatus.INACTIVE);
			}
		}
		return fhirLocation;
	}
	
	/**
	 * @see org.openmrs.module.fhir2.api.translators.LocationTranslator#toOpenmrsType(org.hl7.fhir.r4.model.Location)
	 */
	@Override
	public org.openmrs.Location toOpenmrsType(Location fhirLocation) {
		org.openmrs.Location openmrsLocation = new org.openmrs.Location();
		
		if (fhirLocation != null) {
			openmrsLocation.setUuid(fhirLocation.getId());
			openmrsLocation.setName(fhirLocation.getName());
			openmrsLocation.setDescription(fhirLocation.getDescription());
			openmrsLocation.setCityVillage(fhirLocation.getAddress().getCity());
			openmrsLocation.setStateProvince(fhirLocation.getAddress().getState());
			openmrsLocation.setCountry(fhirLocation.getAddress().getCountry());
			openmrsLocation.setPostalCode(fhirLocation.getAddress().getPostalCode());
		}
		return openmrsLocation;
	}
}
