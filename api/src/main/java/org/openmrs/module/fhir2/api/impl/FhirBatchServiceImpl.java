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
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Bundle;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirBatchService;
import org.openmrs.module.fhir2.api.dao.FhirBatchDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.BatchTranslator;
import org.openmrs.module.fhir2.model.FhirBatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class FhirBatchServiceImpl extends BaseFhirService<Bundle, FhirBatch> implements FhirBatchService {

	@Autowired
	private FhirBatchDao dao;

	@Autowired
	private SearchQueryInclude<Bundle> searchQueryInclude;

	@Autowired
	private BatchTranslator translator;

	@Autowired
	private SearchQuery<FhirBatch, Bundle, FhirBatchDao, BatchTranslator, SearchQueryInclude<Bundle>> searchQuery;


	@Override
	public IBundleProvider searchBatches(TokenAndListParam identifier, TokenAndListParam batchType) {
		SearchParameterMap theParams = new SearchParameterMap()
				.addParameter(FhirConstants.OPENMRS_FHIR_EXT_BATCH_IDENTIFIER, identifier)
				.addParameter(FhirConstants.ENCOUNTER_TYPE_REFERENCE_SEARCH_HANDLER, batchType);

		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
}
