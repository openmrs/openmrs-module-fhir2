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

import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;

public interface FhirPractitionerDao extends FhirDao<Provider> {
	
	@Override
	Provider get(String uuid);
	
	@Authorized(PrivilegeConstants.GET_PROVIDERS)
	List<ProviderAttribute> getActiveAttributesByPractitionerAndAttributeTypeUuid(@NotNull Provider provider,
	        @NotNull String providerAttributeTypeUuid);
	
	@Override
	@Authorized({ PrivilegeConstants.MANAGE_PROVIDERS })
	Provider createOrUpdate(Provider newEntry);
	
	@Override
	@Authorized(PrivilegeConstants.MANAGE_PROVIDERS)
	Provider delete(String uuid);
	
	@Override
	@Authorized(PrivilegeConstants.GET_PROVIDERS)
	List<String> getSearchResultUuids(SearchParameterMap theParams);
	
	@Override
	@Authorized(PrivilegeConstants.GET_PROVIDERS)
	List<Provider> getSearchResults(SearchParameterMap theParams, List<String> matchingResourceUuids, int firstResult,
	        int lastResult);
}
