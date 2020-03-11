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

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
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
	public Collection<Location> searchForLocations(StringOrListParam name, StringOrListParam city, StringOrListParam country,
	        StringOrListParam postalCode, StringOrListParam state, TokenOrListParam tag, ReferenceOrListParam parent,
	        SortSpec sort) {
		return locationDao.searchForLocations(name, city, country, postalCode, state, tag, parent, sort).stream()
		        .map(locationTranslator::toFhirResource).collect(Collectors.toList());
	}
}
