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

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Location;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
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
	
	@Autowired
	private SearchQuery<org.openmrs.Location, Location, FhirLocationDao, LocationTranslator> searchQuery;
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchForLocations(StringAndListParam name, StringAndListParam city, StringAndListParam country,
	        StringAndListParam postalCode, StringAndListParam state, TokenAndListParam tag, ReferenceAndListParam parent,
	        SortSpec sort) {
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, name)
		        .addParameter(FhirConstants.CITY_SEARCH_HANDLER, city)
		        .addParameter(FhirConstants.STATE_SEARCH_HANDLER, state)
		        .addParameter(FhirConstants.COUNTRY_SEARCH_HANDLER, country)
		        .addParameter(FhirConstants.POSTALCODE_SEARCH_HANDLER, postalCode)
		        .addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER, parent)
		        .addParameter(FhirConstants.TAG_SEARCH_HANDLER, tag).setSortSpec(sort);
		
		return searchQuery.getQueryResults(theParams, dao, translator);
	}
}
