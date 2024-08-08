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
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getLastUpdated;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getVersionId;
import static org.openmrs.module.fhir2.api.util.FhirUtils.getMetadataTranslation;

import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.AccessLevel;
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
@Setter(AccessLevel.PACKAGE)
public class LocationTranslatorImpl implements LocationTranslator {
	
	@Autowired
	private LocationAddressTranslator locationAddressTranslator;
	
	@Autowired
	private LocationTagTranslator locationTagTranslator;
	
	@Autowired
	private LocationReferenceTranslator locationReferenceTranslator;
	
	@Autowired
	private TelecomTranslator<BaseOpenmrsData> telecomTranslator;
	
	@Autowired
	private LocationTypeTranslator locationTypeTranslator;
	
	@Autowired
	private FhirGlobalPropertyService propertyService;
	
	@Autowired
	private FhirLocationDao fhirLocationDao;
	
	/**
	 * @see org.openmrs.module.fhir2.api.translators.LocationTranslator#toFhirResource(org.openmrs.Location)
	 */
	@Override
	public Location toFhirResource(@Nonnull org.openmrs.Location openmrsLocation) {
		if (openmrsLocation == null) {
			return null;
		}
		
		Location fhirLocation = new Location();
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
		
		fhirLocation.setTelecom(getLocationContactDetails(openmrsLocation));
		
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
	
	protected List<ContactPoint> getLocationContactDetails(@Nonnull org.openmrs.Location location) {
		String locationContactPointAttributeType = propertyService
		        .getGlobalProperty(FhirConstants.LOCATION_CONTACT_POINT_ATTRIBUTE_TYPE);
		
		if (locationContactPointAttributeType == null || locationContactPointAttributeType.isEmpty()) {
			return Collections.emptyList();
		}
		
		return fhirLocationDao.getActiveAttributesByLocationAndAttributeTypeUuid(location, locationContactPointAttributeType)
		        .stream().map(telecomTranslator::toFhirResource).collect(Collectors.toList());
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
}
