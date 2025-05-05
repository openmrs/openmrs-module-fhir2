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
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Group;
import org.openmrs.Cohort;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGroupService;
import org.openmrs.module.fhir2.api.dao.FhirGroupDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.GroupTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirGroupServiceImpl extends BaseFhirService<Group, Cohort> implements FhirGroupService {
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private FhirGroupDao dao;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private GroupTranslator translator;
	
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private SearchQueryInclude<Group> searchQueryInclude;
	
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private SearchQuery<org.openmrs.Cohort, Group, FhirGroupDao, GroupTranslator, SearchQueryInclude<Group>> searchQuery;
	
	@Override
	public IBundleProvider searchForGroups(ReferenceAndListParam participant) {
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participant);
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
}
