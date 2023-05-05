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

import java.util.List;

import org.openmrs.Encounter;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;

public interface FhirEncounterDao extends FhirDao<Encounter> {
	
	@Override
	@Authorized(PrivilegeConstants.GET_ENCOUNTERS)
	Encounter get(@Nonnull String uuid);
	
	@Override
	@Authorized({ PrivilegeConstants.ADD_ENCOUNTERS, PrivilegeConstants.EDIT_ENCOUNTERS })
	Encounter createOrUpdate(@Nonnull Encounter newEntry);
	
	@Override
	@Authorized(PrivilegeConstants.DELETE_ENCOUNTERS)
	Encounter delete(@Nonnull String uuid);
	
	@Authorized(PrivilegeConstants.GET_ENCOUNTERS)
	List<String> getSearchResultUuids(@Nonnull SearchParameterMap theParams);
	
	@Override
	@Authorized(PrivilegeConstants.GET_ENCOUNTERS)
	List<Encounter> getSearchResults(@Nonnull SearchParameterMap theParams);
}
