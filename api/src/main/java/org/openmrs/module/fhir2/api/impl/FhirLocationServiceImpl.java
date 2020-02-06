/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import javax.inject.Inject;

import java.util.Collection;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.param.TokenParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Location;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.translators.LocationTranslator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirLocationServiceImpl implements FhirLocationService {
	
	@Inject
	FhirLocationDao locationDao;
	
	@Inject
	LocationTranslator locationTranslator;
	
	@Override
	@Transactional(readOnly = true)
	public Location getLocationByUuid(String uuid) {
		return locationTranslator.toFhirResource(locationDao.getLocationByUuid(uuid));
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<Location> findLocationByName(String name) {
		return locationDao.findLocationByName(name).stream().map(locationTranslator::toFhirResource)
		        .collect(Collectors.toList());
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<Location> findLocationsByCity(String city) {
		return locationDao.findLocationsByCity(city).stream().map(locationTranslator::toFhirResource)
		        .collect(Collectors.toList());
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<Location> findLocationsByCountry(String country) {
		return locationDao.findLocationsByCountry(country).stream().map(locationTranslator::toFhirResource)
		        .collect(Collectors.toList());
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<Location> findLocationsByPostalCode(String postalCode) {
		return locationDao.findLocationsByPostalCode(postalCode).stream().map(locationTranslator::toFhirResource)
		        .collect(Collectors.toList());
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<Location> findLocationsByState(String state) {
		return locationDao.findLocationsByState(state).stream().map(locationTranslator::toFhirResource)
		        .collect(Collectors.toList());
	}
	
	@Override
	public Collection<Location> findLocationsByTag(TokenParam tag) {
		return locationDao.findLocationsByTag(tag).stream().map(locationTranslator::toFhirResource)
		        .collect(Collectors.toList());
	}
}
