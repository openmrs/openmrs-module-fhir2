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
import java.util.Optional;

import org.openmrs.Concept;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;

public interface FhirConceptDao extends FhirDao<Concept> {
	
	@Override
	@Authorized(PrivilegeConstants.GET_CONCEPTS)
	Concept get(@NotNull String uuid);
	
	@Authorized(PrivilegeConstants.GET_CONCEPTS)
	Optional<Concept> getConceptBySourceNameAndCode(String sourceName, String code);
	
	@Override
	@Authorized(PrivilegeConstants.MANAGE_CONCEPTS)
	Concept createOrUpdate(Concept newEntry);
	
	@Override
	@Authorized(PrivilegeConstants.MANAGE_CONCEPTS)
	Concept delete(String uuid);
	
	@Override
	@Authorized(PrivilegeConstants.GET_CONCEPTS)
	List<Concept> getSearchResults(SearchParameterMap theParams, List<String> matchingResourceUuids, int firstResult,
	        int lastResult);
}
