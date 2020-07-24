/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ToFhirTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Generic search Interface
 *
 * @param <O> openMrs generic DAO Class Implementation
 * @param <T> FHIR generic translator Class
 */
@Component
public class SearchQuery<T extends OpenmrsObject & Auditable, U extends IBaseResource, O extends FhirDao<T>, V extends ToFhirTranslator<T, U>> {
	
	@Autowired
	private FhirGlobalPropertyService globalPropertyService;
	
	/**
	 * Gets query results
	 *
	 * @param theParams search params.
	 * @param dao generic dao
	 * @param translator generic translator
	 * @return IBundleProvider
	 */
	public IBundleProvider getQueryResults(SearchParameterMap theParams, O dao, V translator) {
		return new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService);
	}
}
