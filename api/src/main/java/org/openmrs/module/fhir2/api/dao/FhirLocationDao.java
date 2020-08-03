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

import javax.validation.constraints.NotNull;

import java.util.List;

import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;

public interface FhirLocationDao extends FhirDao<Location> {
	
	@Override
	@Authorized(PrivilegeConstants.GET_LOCATIONS)
	Location get(String uuid);
	
	@Authorized(PrivilegeConstants.GET_LOCATIONS)
	List<LocationAttribute> getActiveAttributesByLocationAndAttributeTypeUuid(@NotNull Location location,
	        @NotNull String locationAttributeTypeUuid);
	
	@Override
	@Authorized(PrivilegeConstants.MANAGE_LOCATIONS)
	Location createOrUpdate(Location newEntry);
	
	@Override
	@Authorized(PrivilegeConstants.MANAGE_LOCATIONS)
	Location delete(String uuid);
	
	@Override
	@Authorized(PrivilegeConstants.GET_LOCATIONS)
	List<String> getSearchResultUuids(SearchParameterMap theParams);
	
	@Override
	@Authorized(PrivilegeConstants.GET_LOCATIONS)
	List<Location> getSearchResults(SearchParameterMap theParams, List<String> matchingResourceUuids, int firstResult,
	        int lastResult);
}
