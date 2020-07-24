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

import org.openmrs.Drug;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;

public interface FhirMedicationDao extends FhirDao<Drug> {
	
	@Override
	@Authorized(PrivilegeConstants.GET_CONCEPTS)
	Drug get(String uuid);
	
	@Override
	@Authorized(PrivilegeConstants.MANAGE_CONCEPTS)
	Drug createOrUpdate(Drug newEntry);
	
	@Override
	@Authorized(PrivilegeConstants.MANAGE_CONCEPTS)
	Drug delete(String uuid);
	
	@Override
	@Authorized(PrivilegeConstants.GET_CONCEPTS)
	List<String> getSearchResultUuids(SearchParameterMap theParams);
	
	@Override
	@Authorized(PrivilegeConstants.GET_CONCEPTS)
	List<Drug> getSearchResults(SearchParameterMap theParams, List<String> matchingResourceUuids, int firstResult,
	        int lastResult);
}
