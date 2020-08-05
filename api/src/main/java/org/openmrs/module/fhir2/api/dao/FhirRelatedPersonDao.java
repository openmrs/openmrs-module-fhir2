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

import org.openmrs.Relationship;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;

public interface FhirRelatedPersonDao extends FhirDao<Relationship> {
	
	@Override
	@Authorized({ PrivilegeConstants.GET_PERSONS, PrivilegeConstants.GET_RELATIONSHIPS })
	Relationship get(String uuid);
	
	@Override
	@Authorized({ PrivilegeConstants.ADD_PERSONS, PrivilegeConstants.EDIT_PERSONS, PrivilegeConstants.ADD_RELATIONSHIPS,
	        PrivilegeConstants.EDIT_RELATIONSHIPS })
	Relationship createOrUpdate(Relationship newEntry);
	
	@Override
	@Authorized({ PrivilegeConstants.DELETE_PERSONS, PrivilegeConstants.DELETE_RELATIONSHIPS })
	Relationship delete(String uuid);
	
	@Override
	@Authorized({ PrivilegeConstants.GET_PERSONS, PrivilegeConstants.GET_RELATIONSHIPS })
	List<String> getSearchResultUuids(SearchParameterMap theParams);
	
	@Override
	@Authorized({ PrivilegeConstants.GET_PERSONS, PrivilegeConstants.GET_RELATIONSHIPS })
	List<Relationship> getSearchResults(SearchParameterMap theParams, List<String> matchingResourceUuids, int firstResult,
	        int lastResult);
}
