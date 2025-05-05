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

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.User;
import org.openmrs.module.fhir2.api.FhirUserService;
import org.openmrs.module.fhir2.api.dao.FhirUserDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirUserServiceImpl extends BaseFhirService<Practitioner, User> implements FhirUserService {
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private FhirUserDao dao;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private PractitionerTranslator<User> translator;
	
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private SearchQueryInclude<Practitioner> searchQueryInclude;
	
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private SearchQuery<User, Practitioner, FhirUserDao, PractitionerTranslator<User>, SearchQueryInclude<Practitioner>> searchQuery;
	
	@Override
	public IBundleProvider searchForUsers(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
}
