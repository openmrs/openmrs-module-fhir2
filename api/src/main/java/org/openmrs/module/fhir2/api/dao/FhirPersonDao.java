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

import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;

public interface FhirPersonDao extends FhirDao<Person> {
	
	@Override
	@Authorized(PrivilegeConstants.GET_PERSONS)
	Person get(String uuid);
	
	@Authorized(PrivilegeConstants.GET_PERSONS)
	List<PersonAttribute> getActiveAttributesByPersonAndAttributeTypeUuid(@NotNull Person person,
	        @NotNull String personAttributeTypeUuid);
	
	@Override
	@Authorized({ PrivilegeConstants.ADD_PERSONS, PrivilegeConstants.EDIT_PERSONS })
	Person createOrUpdate(Person newEntry);
	
	@Override
	@Authorized(PrivilegeConstants.EDIT_PERSONS)
	Person delete(String uuid);
	
	@Override
	@Authorized(PrivilegeConstants.GET_PERSONS)
	List<String> getSearchResultUuids(SearchParameterMap theParams);
	
	@Override
	@Authorized(PrivilegeConstants.GET_PERSONS)
	List<Person> getSearchResults(SearchParameterMap theParams, List<String> matchingResourceUuids, int firstResult,
	        int lastResult);
}
