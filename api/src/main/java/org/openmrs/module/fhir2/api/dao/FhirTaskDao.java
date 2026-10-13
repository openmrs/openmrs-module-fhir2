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

import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.model.FhirTask;

/**
 * Guarded by the Task privileges this module declares in {@code config.xml}, since core defines
 * none.
 */
public interface FhirTaskDao extends FhirDao<FhirTask> {
	
	@Override
	@Authorized(FhirConstants.GET_TASKS_PRIVILEGE)
	FhirTask get(@Nonnull String uuid);
	
	@Override
	@Authorized(FhirConstants.GET_TASKS_PRIVILEGE)
	boolean exists(@Nonnull String uuid);
	
	@Override
	@Authorized(FhirConstants.GET_TASKS_PRIVILEGE)
	List<FhirTask> get(@Nonnull Collection<String> uuids);
	
	@Override
	@Authorized(FhirConstants.GET_TASKS_PRIVILEGE)
	List<FhirTask> getSearchResults(@Nonnull SearchParameterMap theParams);
	
	@Override
	@Authorized(FhirConstants.GET_TASKS_PRIVILEGE)
	int getSearchResultsCount(@Nonnull SearchParameterMap theParams);
	
	@Override
	@Authorized({ FhirConstants.ADD_TASKS_PRIVILEGE, FhirConstants.EDIT_TASKS_PRIVILEGE })
	FhirTask createOrUpdate(@Nonnull FhirTask newEntry);
	
	@Override
	@Authorized(FhirConstants.DELETE_TASKS_PRIVILEGE)
	FhirTask delete(@Nonnull String uuid);
}
