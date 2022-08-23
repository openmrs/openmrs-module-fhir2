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
import org.hl7.fhir.r4.model.Encounter;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.api.FhirVisitService;
import org.openmrs.module.fhir2.api.dao.FhirVisitDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.EncounterTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class FhirVisitServiceImpl extends BaseFhirService<Encounter, Visit> implements FhirVisitService {
	
	@Autowired
	private FhirVisitDao dao;
	
	@Autowired
	private EncounterTranslator<Visit> translator;
	
	@Autowired
	private SearchQueryInclude<Encounter> searchQueryInclude;
	
	@Autowired
	private SearchQuery<Visit, Encounter, FhirVisitDao, EncounterTranslator<Visit>, SearchQueryInclude<Encounter>> searchQuery;
	
	@Override
	public IBundleProvider searchForVisits(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
}
