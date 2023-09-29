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
import org.hl7.fhir.r4.model.Condition;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.ConditionSearchParams;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class FhirConditionServiceImpl extends BaseFhirService<Condition, org.openmrs.Condition> implements FhirConditionService {
	
	@Autowired
	private FhirConditionDao<org.openmrs.Condition> dao;
	
	@Autowired
	private ConditionTranslator<org.openmrs.Condition> translator;
	
	@Autowired
	private SearchQueryInclude searchQueryInclude;
	
	@Autowired
	private SearchQuery<org.openmrs.Condition, Condition, FhirConditionDao<org.openmrs.Condition>, ConditionTranslator<org.openmrs.Condition>, SearchQueryInclude<Condition>> searchQuery;
	
	@Override
	public IBundleProvider searchConditions(ConditionSearchParams conditionSearchParams) {
		return searchQuery.getQueryResults(conditionSearchParams.toSearchParameterMap(), dao, translator,
		    searchQueryInclude);
	}
}
