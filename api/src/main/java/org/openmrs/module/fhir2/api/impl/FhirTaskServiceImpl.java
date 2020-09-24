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
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.TaskTranslator;
import org.openmrs.module.fhir2.model.FhirTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PACKAGE)
public class FhirTaskServiceImpl extends BaseFhirService<Task, FhirTask> implements FhirTaskService {
	
	@Autowired
	private FhirTaskDao dao;
	
	@Autowired
	private TaskTranslator translator;
	
	@Autowired
	private SearchQuery<FhirTask, Task, FhirTaskDao, TaskTranslator> searchQuery;
	
	/**
	 * Get collection of tasks corresponding to the provided search parameters
	 *
	 * @param basedOnReference A reference list to basedOn resources
	 * @param ownerReference A reference list to owner resources
	 * @param status A list of statuses
	 * @param id The UUID of the requested task
	 * @param lastUpdated A date range corresponding to when the Tasks were last updated
	 * @param sort The sort parameters for the search results
	 * @return the collection of Tasks that match the search parameters
	 */
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchForTasks(ReferenceAndListParam basedOnReference, ReferenceAndListParam ownerReference,
	        TokenAndListParam status, TokenAndListParam id, DateRangeParam lastUpdated, SortSpec sort) {
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.BASED_ON_REFERENCE_SEARCH_HANDLER, basedOnReference)
		        .addParameter(FhirConstants.OWNER_REFERENCE_SEARCH_HANDLER, ownerReference)
		        .addParameter(FhirConstants.STATUS_SEARCH_HANDLER, status)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, id)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)
		        .setSortSpec(sort);
		
		return searchQuery.getQueryResults(theParams, dao, translator);
	}
}
