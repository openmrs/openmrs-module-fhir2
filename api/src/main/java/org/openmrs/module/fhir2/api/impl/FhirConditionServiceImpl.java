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

import java.util.HashSet;

import org.hl7.fhir.r4.model.Condition;
import org.openmrs.Obs;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;

@Component
@Getter(AccessLevel.PROTECTED)
@OpenmrsProfile(openmrsPlatformVersion = "2.0.5 - 2.1.*")
public class FhirConditionServiceImpl extends BaseFhirService<Condition, Obs> implements FhirConditionService {
	
	@Autowired
	private FhirConditionDao<org.openmrs.Obs> dao;
	
	@Autowired
	private ConditionTranslator<org.openmrs.Obs> translator;
	
	@Autowired
	private SearchQueryInclude searchQueryInclude;
	
	@Autowired
	private SearchQuery<org.openmrs.Obs, Condition, FhirConditionDao<org.openmrs.Obs>, ConditionTranslator<org.openmrs.Obs>, SearchQueryInclude<Condition>> searchQuery;
	
	@Override
	public IBundleProvider searchConditions(ReferenceAndListParam patientParam, TokenAndListParam code,
	        TokenAndListParam clinicalStatus, DateRangeParam onsetDate, QuantityAndListParam onsetAge,
	        DateRangeParam recordedDate, TokenAndListParam id, DateRangeParam lastUpdated, @Sort SortSpec sort,
	        HashSet<Include> includes) {
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientParam)
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, code)
		        .addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, onsetAge)
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "onsetDate", onsetDate)
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "dateCreated", recordedDate)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, id)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes).setSortSpec(sort);	
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
}
