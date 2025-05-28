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

import static lombok.AccessLevel.PROTECTED;
import static org.apache.commons.lang3.Validate.notNull;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getLastUpdated;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getVersionId;
import static org.openmrs.module.fhir2.api.util.FhirUtils.getMetadataTranslation;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.math.NumberUtils;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationTag;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.translators.LocationAddressTranslator;
import org.openmrs.module.fhir2.api.translators.LocationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.LocationTagTranslator;
import org.openmrs.module.fhir2.api.translators.LocationTranslator;
import org.openmrs.module.fhir2.api.translators.LocationTypeTranslator;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocationTranslatorImpl implements LocationTranslator {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private LocationAddressTranslator locationAddressTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private LocationTagTranslator locationTagTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private LocationReferenceTranslator locationReferenceTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private TelecomTranslator<BaseOpenmrsData> telecomTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private LocationTypeTranslator locationTypeTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirGlobalPropertyService propertyService;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirLocationDao fhirLocationDao;
	
	/**
	 * @see org.openmrs.module.fhir2.api.translators.LocationTranslator#toFhirResource(org.openmrs.Location)
	 */
	@Override
	public Location toFhirResource(@Nonnull org.openmrs.Location openmrsLocation) {
		return toFhirResources(Collections.singletonList(openmrsLocation)).get(0);
	}
	
	@Override
	public List<Location> toFhirResources(Collection<org.openmrs.Location> openmrsLocations) {
		final LocationTranslatorContext context = new LocationTranslatorContext(getLocationContactDetails(openmrsLocations));
		
		return openmrsLocations.stream().map((location) -> toFhirResource(location, context)).collect(Collectors.toList());
	}
	
	/**
	 * @see org.openmrs.module.fhir2.api.translators.LocationTranslator#toOpenmrsType(org.hl7.fhir.r4.model.Location)
	 */
	@Override
	public org.openmrs.Location toOpenmrsType(@Nonnull Location fhirLocation) {
		if (fhirLocation == null) {
			return null;
		}
		
		return toOpenmrsType(new org.openmrs.Location(), fhirLocation);
	}
	
	/**
	 * @see org.openmrs.module.fhir2.api.translators.LocationTranslator#toOpenmrsType(org.openmrs.Location,
	 *      org.hl7.fhir.r4.model.Location)
	 */
	@Override
	public org.openmrs.Location toOpenmrsType(@Nonnull org.openmrs.Location openmrsLocation,
	        @Nonnull Location fhirLocation) {
		notNull(openmrsLocation, "The existing Openmrs location should not be null");
		notNull(fhirLocation, "The Location object should not be null");
		
		if (fhirLocation.hasId()) {
			openmrsLocation.setUuid(fhirLocation.getIdElement().getIdPart());
		}
		
		openmrsLocation.setName(fhirLocation.getName());
		openmrsLocation.setDescription(fhirLocation.getDescription());
		
		if (fhirLocation.getAddress() != null) {
			openmrsLocation.setCityVillage(fhirLocation.getAddress().getCity());
			openmrsLocation.setStateProvince(fhirLocation.getAddress().getState());
			openmrsLocation.setCountry(fhirLocation.getAddress().getCountry());
			openmrsLocation.setPostalCode(fhirLocation.getAddress().getPostalCode());
		}
		
		if (fhirLocation.getPosition().hasLatitude()) {
			openmrsLocation.setLatitude(fhirLocation.getPosition().getLatitude().toString());
		}
		if (fhirLocation.getPosition().hasLongitude()) {
			openmrsLocation.setLongitude(fhirLocation.getPosition().getLongitude().toString());
		}
		
		fhirLocation.getTelecom().stream().map(
		    contactPoint -> (LocationAttribute) telecomTranslator.toOpenmrsType(new LocationAttribute(), contactPoint))
		        .distinct().filter(Objects::nonNull).forEach(openmrsLocation::addAttribute);
		
		if (fhirLocation.hasType()) {
			openmrsLocation = locationTypeTranslator.toOpenmrsType(openmrsLocation, fhirLocation.getType());
		}
		
		if (fhirLocation.getMeta().hasTag()) {
			for (Coding tag : fhirLocation.getMeta().getTag()) {
				openmrsLocation.addTag(locationTagTranslator.toOpenmrsType(tag));
			}
		}
		
		openmrsLocation.setParentLocation(getOpenmrsParentLocation(fhirLocation.getPartOf()));
		
		return openmrsLocation;
	}
	
	public org.openmrs.Location getOpenmrsParentLocation(Reference location) {
		if (location == null) {
			return null;
		}
		
		if (location.hasType() && !location.getType().equals("Location")) {
			throw new IllegalArgumentException("Reference must be to a Location not a " + location.getType());
		}
		
		return locationReferenceTranslator.toOpenmrsType(location);
	}
	
	private Map<org.openmrs.Location, List<ContactPoint>> getLocationContactDetails(
	        Collection<org.openmrs.Location> locations) {
		final String locationContactPointAttributeType = propertyService
		        .getGlobalProperty(FhirConstants.LOCATION_CONTACT_POINT_ATTRIBUTE_TYPE);
		
		if (locationContactPointAttributeType == null || locationContactPointAttributeType.isEmpty()) {
			return Collections.emptyMap();
		}
		
		final Map<org.openmrs.Location, List<LocationAttribute>> contactPointAttributes = fhirLocationDao
		        .getActiveAttributesByLocationsAndAttributeTypeUuid(locations, locationContactPointAttributeType);
		
		return contactPointAttributes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
		    entry -> entry.getValue().stream().map(telecomTranslator::toFhirResource).collect(Collectors.toList())));
	}
	
	private Location toFhirResource(org.openmrs.Location openmrsLocation, LocationTranslatorContext context) {
		final Location fhirLocation = new Location();
		fhirLocation.setId(openmrsLocation.getUuid());
		fhirLocation.setName(getMetadataTranslation(openmrsLocation));
		fhirLocation.setDescription(openmrsLocation.getDescription());
		fhirLocation.setAddress(locationAddressTranslator.toFhirResource(openmrsLocation));
		
		Location.LocationPositionComponent position = null;
		if (openmrsLocation.getLatitude() != null && !openmrsLocation.getLatitude().isEmpty()) {
			double latitude = NumberUtils.toDouble(openmrsLocation.getLatitude(), -1.0d);
			if (latitude >= 0.0d) {
				position = new Location.LocationPositionComponent();
				position.setLatitude(latitude);
			}
		}
		
		if (openmrsLocation.getLongitude() != null && !openmrsLocation.getLongitude().isEmpty()) {
			double longitude = NumberUtils.toDouble(openmrsLocation.getLongitude(), -1.0d);
			if (longitude >= 0.0d) {
				if (position == null) {
					position = new Location.LocationPositionComponent();
				}
				position.setLongitude(longitude);
			}
		}
		
		if (position != null) {
			fhirLocation.setPosition(position);
		}
		
		if (!openmrsLocation.getRetired()) {
			fhirLocation.setStatus(Location.LocationStatus.ACTIVE);
		} else {
			fhirLocation.setStatus(Location.LocationStatus.INACTIVE);
		}
		
		fhirLocation.setTelecom(context.locationContactDetails.get(openmrsLocation));
		
		fhirLocation.setType(locationTypeTranslator.toFhirResource(openmrsLocation));
		
		if (openmrsLocation.getTags() != null) {
			for (LocationTag tag : openmrsLocation.getTags()) {
				fhirLocation.getMeta().addTag(FhirConstants.OPENMRS_FHIR_EXT_LOCATION_TAG, tag.getName(),
				    tag.getDescription());
			}
		}
		
		if (openmrsLocation.getParentLocation() != null) {
			fhirLocation.setPartOf(locationReferenceTranslator.toFhirResource(openmrsLocation.getParentLocation()));
		}
		
		fhirLocation.getMeta().setLastUpdated(getLastUpdated(openmrsLocation));
		fhirLocation.getMeta().setVersionId(getVersionId(openmrsLocation));
		
		return fhirLocation;
	}
	
	@AllArgsConstructor
	private static class LocationTranslatorContext {
		
		final Map<org.openmrs.Location, List<ContactPoint>> locationContactDetails;
	}
}
