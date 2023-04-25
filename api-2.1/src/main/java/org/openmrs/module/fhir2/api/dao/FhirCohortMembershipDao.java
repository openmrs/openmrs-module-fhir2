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

import org.openmrs.CohortMembership;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.model.IdUuidTuple;
import org.openmrs.util.PrivilegeConstants;

public interface FhirCohortMembershipDao extends FhirDao<CohortMembership> {
	
	@Override
	@Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
	CohortMembership get(@Nonnull String uuid);
	
	@Override
	@Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
	List<CohortMembership> get(@Nonnull Collection<String> uuids);
	
	@Override
	@Authorized(PrivilegeConstants.ADD_COHORTS)
	CohortMembership createOrUpdate(@Nonnull CohortMembership newEntry);
	
	@Override
	@Authorized(PrivilegeConstants.DELETE_COHORTS)
	CohortMembership delete(@Nonnull String uuid);
	
	@Override
	@Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
	List<String> getSearchResultUuids(@Nonnull SearchParameterMap theParams);
	
	@Override
	@Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
	List<CohortMembership> getSearchResults(@Nonnull SearchParameterMap theParams, @Nonnull List<String> resourceUuids);
	
	@Authorized(PrivilegeConstants.GET_PATIENTS)
	List<IdUuidTuple> getIdsForUuids(List<String> uuid);
	
	@Authorized(PrivilegeConstants.GET_PATIENTS)
	List<IdUuidTuple> getUuidsForIds(List<Integer> ids);
}
