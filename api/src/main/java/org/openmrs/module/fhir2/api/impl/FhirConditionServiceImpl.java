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

import javax.transaction.Transactional;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Condition;
import org.openmrs.Obs;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.ConditionSearchParams;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Transactional
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PACKAGE)
@OpenmrsProfile(openmrsPlatformVersion = "2.0.5 - 2.1.*")
public class FhirConditionServiceImpl extends BaseFhirService<Condition, Obs> implements FhirConditionService {
	
	@Autowired
	private FhirConditionDao<org.openmrs.Obs> dao;
	
	@Autowired
	private ConditionTranslator<org.openmrs.Obs> translator;
	
	@Autowired
	private SearchQueryInclude<Condition> searchQueryInclude;
	
	@Autowired
	private SearchQuery<org.openmrs.Obs, Condition, FhirConditionDao<org.openmrs.Obs>, ConditionTranslator<org.openmrs.Obs>, SearchQueryInclude<Condition>> searchQuery;
	
	@Override
	public IBundleProvider searchConditions(ConditionSearchParams conditionSearchParams) {
		if (conditionSearchParams.getClinicalStatus() != null
		        && !conditionSearchParams.getClinicalStatus().getValuesAsQueryTokens().isEmpty()) {
			throw new IllegalArgumentException(
			        "The clinicalStatus parameter in Condition is not supported in OpenMRS versions lower than 2.2.0");
		}
		
		conditionSearchParams.toSearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "obsDatetime",
		    conditionSearchParams.getOnsetDate());
		
		IBundleProvider providerBundle = searchQuery.getQueryResults(conditionSearchParams.toSearchParameterMap(), dao,
		    translator, searchQueryInclude);
		
		return providerBundle;
	}
}
