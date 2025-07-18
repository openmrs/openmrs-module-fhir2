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

import org.openmrs.Diagnosis;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;

public interface FhirDiagnosisDao extends FhirDao<Diagnosis> {
	
	@Override
	@Authorized(PrivilegeConstants.GET_DIAGNOSES)
	Diagnosis get(@Nonnull String uuid);
	
	@Override
	@Authorized(PrivilegeConstants.GET_DIAGNOSES)
	List<Diagnosis> get(@Nonnull Collection<String> uuids);
	
	@Override
	@Authorized(PrivilegeConstants.GET_DIAGNOSES)
	List<Diagnosis> getSearchResults(@Nonnull SearchParameterMap theParams);
	
	@Override
	@Authorized(PrivilegeConstants.GET_DIAGNOSES)
	int getSearchResultsCount(@Nonnull SearchParameterMap theParams);
	
	@Override
	@Authorized({ PrivilegeConstants.EDIT_DIAGNOSES })
	Diagnosis createOrUpdate(@Nonnull Diagnosis newDiagnosis);
	
	@Override
	@Authorized(PrivilegeConstants.DELETE_DIAGNOSES)
	Diagnosis delete(@Nonnull String uuid);
}
