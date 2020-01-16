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
import org.hl7.fhir.r4.model.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.LocationService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.translators.LocationAddressTranslator;
import org.openmrs.module.fhir2.api.translators.LocationTranslator;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Setter(AccessLevel.PACKAGE)
public class LocationTranslatorImpl implements LocationTranslator {
	
	@Inject
	private LocationAddressTranslator locationAddressTranslator;
	
	@Inject
	private TelecomTranslator telecomTranslator;
	
	@Inject
	private FhirGlobalPropertyService propertyService;
	
	@Inject
	private LocationService locationService;
	
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
			
			fhirLocation.setTelecom(getLocationContactDetails(openmrsLocation));
		}
		return fhirLocation;
	}
	
	public List<ContactPoint> getLocationContactDetails(@NotNull org.openmrs.Location location){
		List<ContactPoint> contactPoints = new ArrayList<>();
		LocationAttributeType locationAttributeType = locationService.getLocationAttributeTypeByUuid(
				propertyService.getGlobalProperty(FhirConstants.LOCATION_ATTRIBUTE_TYPE_PROPERTY));
		for (LocationAttribute attribute : location.getActiveAttributes()){
			if (attribute.getAttributeType().equals(locationAttributeType)){
				contactPoints.add(telecomTranslator.toFhirResource(attribute));
			}
		}
		return contactPoints;
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

			Set<LocationAttribute> attributes = fhirLocation.getTelecom()
					.stream()
					.map(contactPoint -> (LocationAttribute)telecomTranslator.toOpenmrsType(new LocationAttribute(),contactPoint))
					.collect(Collectors.toSet());
			openmrsLocation.setAttributes(attributes);
		}
		return openmrsLocation;
	}
}
