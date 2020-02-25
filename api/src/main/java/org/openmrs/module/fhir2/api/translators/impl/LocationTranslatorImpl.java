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

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationTag;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.translators.LocationAddressTranslator;
import org.openmrs.module.fhir2.api.translators.LocationTranslator;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class LocationTranslatorImpl extends AbstractReferenceHandlingTranslator implements LocationTranslator {
	
	@Inject
	private LocationAddressTranslator locationAddressTranslator;
	
	@Inject
	private TelecomTranslator<Object> telecomTranslator;
	
	@Inject
	private FhirGlobalPropertyService propertyService;
	
	@Inject
	private FhirLocationDao fhirLocationDao;
	
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
			
			if (openmrsLocation.getTags() != null) {
				for (LocationTag tag : openmrsLocation.getTags()) {
					fhirLocation.getMeta().addTag(FhirConstants.OPENMRS_FHIR_EXT_LOCATION_TAG, tag.getName(),
					    tag.getDescription());
				}
			}
			if (openmrsLocation.getParentLocation() != null) {
				fhirLocation.setPartOf(createLocationReference(openmrsLocation.getParentLocation()));
			}
			
			fhirLocation.getMeta().setLastUpdated(openmrsLocation.getDateChanged());
		}
		return fhirLocation;
	}
	
	protected List<ContactPoint> getLocationContactDetails(@NotNull org.openmrs.Location location) {
		return fhirLocationDao
		        .getActiveAttributesByLocationAndAttributeTypeUuid(location,
		            propertyService.getGlobalProperty(FhirConstants.LOCATION_ATTRIBUTE_TYPE_PROPERTY))
		        .stream().map(telecomTranslator::toFhirResource).collect(Collectors.toList());
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
			
			if (fhirLocation.getStatus() != null && fhirLocation.getStatus().equals(Location.LocationStatus.INACTIVE)) {
				openmrsLocation.setRetired(true);
				openmrsLocation.setRetireReason("Retired by FHIR module");
			}
			
			if (fhirLocation.getPosition().getLatitude() != null) {
				openmrsLocation.setLatitude(fhirLocation.getPosition().getLatitude().toString());
			}
			if (fhirLocation.getPosition().getLongitude() != null) {
				openmrsLocation.setLongitude(fhirLocation.getPosition().getLongitude().toString());
			}
			
			Set<LocationAttribute> attributes = fhirLocation.getTelecom().stream().map(
			    contactPoint -> (LocationAttribute) telecomTranslator.toOpenmrsType(new LocationAttribute(), contactPoint))
			        .collect(Collectors.toSet());
			openmrsLocation.setAttributes(attributes);
			
			if (fhirLocation.getMeta().getTag() != null) {
				for (Coding tag : fhirLocation.getMeta().getTag()) {
					openmrsLocation.addTag(new LocationTag(tag.getCode(), tag.getDisplay()));
				}
			}
			
			openmrsLocation.setParentLocation(getOpenmrsParentLocation(fhirLocation.getPartOf()));
			openmrsLocation.setDateChanged(fhirLocation.getMeta().getLastUpdated());
		}
		return openmrsLocation;
	}
	
	public org.openmrs.Location getOpenmrsParentLocation(Reference location) {
		if (location == null) {
			return null;
		}
		
		if (location.getType() != null && !location.getType().equals("Location")) {
			throw new IllegalArgumentException("Reference must be to a Location not a " + location.getType());
		}
		
		String uuid = getReferenceId(location);
		if (uuid == null) {
			return null;
		}
		
		return fhirLocationDao.getLocationByUuid(uuid);
	}
}
