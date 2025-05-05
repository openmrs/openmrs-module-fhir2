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

import static org.openmrs.module.fhir2.FhirConstants.TITLE_SEARCH_HANDLER;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.api.FhirValueSetService;
import org.openmrs.module.fhir2.api.dao.FhirConceptDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ValueSetTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirValueSetServiceImpl extends BaseFhirService<ValueSet, Concept> implements FhirValueSetService {
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private FhirConceptDao dao;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private ValueSetTranslator translator;
	
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private SearchQueryInclude<ValueSet> searchQueryInclude;
	
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private SearchQuery<Concept, ValueSet, FhirConceptDao, ValueSetTranslator, SearchQueryInclude<ValueSet>> searchQuery;
	
	@Override
	public IBundleProvider searchForValueSets(StringAndListParam title) {
		SearchParameterMap theParams = new SearchParameterMap();
		if (title != null && title.size() > 0) {
			theParams.addParameter(TITLE_SEARCH_HANDLER, title);
		}
		
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
}
