/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao;

import java.util.List;

import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

public interface FhirConditionDao<T extends OpenmrsObject & Auditable> extends FhirDao<T> {
	
	@Override
	T get(String uuid);
	
	@Override
	T createOrUpdate(T newEntry);
	
	@Override
	T delete(String uuid);
	
	@Override
	List<String> getSearchResultUuids(SearchParameterMap theParams);
	
	@Override
	List<T> getSearchResults(SearchParameterMap theParams, List<String> matchingResourceUuids, int firstResult,
	        int lastResult);
}
