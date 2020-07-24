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

import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;

public interface FhirUserDao extends FhirDao<User> {
	
	@Authorized(PrivilegeConstants.GET_USERS)
	@Override
	User get(String uuid);
	
	@Authorized(PrivilegeConstants.GET_USERS)
	User getUserByUserName(String clientName);
	
	@Authorized({ PrivilegeConstants.ADD_USERS, PrivilegeConstants.EDIT_USERS })
	@Override
	User createOrUpdate(User newEntry);
	
	@Authorized(PrivilegeConstants.DELETE_USERS)
	@Override
	User delete(String uuid);
	
	@Authorized(PrivilegeConstants.GET_USERS)
	@Override
	List<String> getSearchResultUuids(SearchParameterMap theParams);
	
	@Authorized(PrivilegeConstants.GET_USERS)
	@Override
	List<User> getSearchResults(SearchParameterMap theParams, List<String> matchingResourceUuids, int firstResult,
	        int lastResult);
}
