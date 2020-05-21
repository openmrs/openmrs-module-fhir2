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

import java.util.Collection;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Location;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.translators.LocationTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class FhirLocationServiceImpl extends BaseFhirService<Location, org.openmrs.Location> implements FhirLocationService {
	
	@Autowired
	private FhirLocationDao dao;
	
	@Autowired
	private LocationTranslator translator;
	
	@Override
	@Transactional(readOnly = true)
	public Collection<Location> searchForLocations(StringAndListParam name, StringAndListParam city,
	        StringAndListParam country, StringAndListParam postalCode, StringAndListParam state, TokenAndListParam tag,
	        ReferenceAndListParam parent, SortSpec sort) {
		return dao.searchForLocations(name, city, country, postalCode, state, tag, parent, sort).stream()
		        .map(translator::toFhirResource).collect(Collectors.toList());
	}
}
