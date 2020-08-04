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

import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.fhir2.api.FhirUserService;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class FhirPractitionerServiceImpl extends BaseFhirService<Practitioner, Provider> implements FhirPractitionerService {
	
	@Autowired
	private FhirPractitionerDao dao;
	
	@Autowired
	private PractitionerTranslator<Provider> translator;
	
	@Autowired
	private SearchQuery<Provider, Practitioner, FhirPractitionerDao, PractitionerTranslator<Provider>> searchQuery;
	
	@Autowired
	private FhirUserService userService;
	
	@Override
	public Practitioner get(String uuid) {
		Practitioner practitioner = translator.toFhirResource(getDao().get(uuid));
		if (practitioner == null) {
			practitioner = userService.get(uuid);
		}
		return practitioner;
	}
	
	@Override
	public IBundleProvider searchForPractitioners(StringAndListParam name, TokenAndListParam identifier,
	        StringAndListParam given, StringAndListParam family, StringAndListParam city, StringAndListParam state,
	        StringAndListParam postalCode, StringAndListParam country, TokenAndListParam id, DateRangeParam lastUpdated) {
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PRACTITIONER_NAME_SEARCH_HANDLER, name)
		        .addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER, identifier)
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.GIVEN_PROPERTY, given)
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.FAMILY_PROPERTY, family)
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.CITY_PROPERTY, city)
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.STATE_PROPERTY, state)
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.POSTAL_CODE_PROPERTY, postalCode)
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.COUNTRY_PROPERTY, country)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, id)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider providerBundle = searchQuery.getQueryResults(theParams, dao, translator);
		IBundleProvider userBundle = userService.searchForUsers(name, identifier, given, family, city, state, postalCode,
		    country, id, lastUpdated);
		
		if (!providerBundle.isEmpty() && !userBundle.isEmpty()) {
			List<IBaseResource> theResource = providerBundle.getResources(0, providerBundle.size());
			theResource.addAll(userBundle.getResources(0, userBundle.size()));
			return new SimpleBundleProvider(theResource);
		} else if (providerBundle.isEmpty() && !userBundle.isEmpty()) {
			return userBundle;
		}
		return providerBundle;
	}
}
