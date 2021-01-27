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

import org.openmrs.Cohort;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;

public interface FhirGroupDao extends FhirDao<Cohort> {
	
	@Override
	@Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
	Cohort get(@Nonnull String uuid);
	
	@Override
	@Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
	List<Cohort> get(@Nonnull Collection<String> uuids);
	
	@Override
	@Authorized(PrivilegeConstants.ADD_COHORTS)
	Cohort createOrUpdate(@Nonnull Cohort newEntry);
	
	@Override
	@Authorized(PrivilegeConstants.DELETE_COHORTS)
	Cohort delete(@Nonnull String uuid);
	
	@Override
	@Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
	List<String> getSearchResultUuids(@Nonnull SearchParameterMap theParams);
	
	@Override
	@Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
	default List<Cohort> getSearchResults(@Nonnull SearchParameterMap theParams,
	        @Nonnull List<String> matchingResourceUuids) {
		return null;
	}
	
	@Override
	@Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
	List<Cohort> getSearchResults(@Nonnull SearchParameterMap theParams, @Nonnull List<String> matchingResourceUuids,
	        int firstResult, int lastResult);
}
