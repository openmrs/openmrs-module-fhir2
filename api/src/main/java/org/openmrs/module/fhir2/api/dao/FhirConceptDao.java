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

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.openmrs.Concept;
import org.openmrs.ConceptSource;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

public interface FhirConceptDao extends FhirDao<Concept> {
	
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.GET_CONCEPTS)
	Concept get(@Nonnull Integer id);
	
	@Override
	@Authorized(PrivilegeConstants.GET_CONCEPTS)
	Concept get(@Nonnull String uuid);
	
	@Override
	@Authorized(PrivilegeConstants.GET_CONCEPTS)
	List<Concept> get(@Nonnull Collection<String> uuids);
	
	@Authorized(PrivilegeConstants.GET_CONCEPTS)
	Optional<Concept> getConceptWithSameAsMappingInSource(ConceptSource conceptSource, String mappingCode);
	
	@Authorized(PrivilegeConstants.GET_CONCEPTS)
	List<Concept> getConceptsWithAnyMappingInSource(ConceptSource conceptSource, String mappingCode);
	
	@Override
	@Authorized(PrivilegeConstants.GET_CONCEPTS)
	List<Concept> getSearchResults(@Nonnull SearchParameterMap theParams);
	
	@Override
	@Authorized(PrivilegeConstants.GET_CONCEPTS)
	int getSearchResultsCount(@Nonnull SearchParameterMap theParams);
	
	@Override
	@Authorized(PrivilegeConstants.MANAGE_CONCEPTS)
	Concept createOrUpdate(@Nonnull Concept newEntry);
	
	@Override
	@Authorized(PrivilegeConstants.MANAGE_CONCEPTS)
	Concept delete(@Nonnull String uuid);
	
}
