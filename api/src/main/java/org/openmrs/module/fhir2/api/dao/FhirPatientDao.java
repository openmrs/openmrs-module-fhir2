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

import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;

public interface FhirPatientDao extends FhirDao<Patient> {
	
	@Override
	@Authorized(PrivilegeConstants.GET_PATIENTS)
	Patient get(@Nonnull String uuid);
	
	@Override
	@Authorized(PrivilegeConstants.GET_PATIENTS)
	List<Patient> get(@Nonnull Collection<String> uuids);
	
	@Authorized(PrivilegeConstants.GET_PATIENTS)
	Patient getPatientById(@Nonnull Integer id);
	
	@Authorized(PrivilegeConstants.GET_PATIENTS)
	List<Patient> getPatientsByIds(@Nonnull Collection<Integer> ids);
	
	@Authorized(PrivilegeConstants.GET_PATIENT_IDENTIFIERS)
	PatientIdentifierType getPatientIdentifierTypeByNameOrUuid(String name, String uuid);
	
	@Override
	@Authorized(PrivilegeConstants.GET_PATIENTS)
	List<Patient> getSearchResults(@Nonnull SearchParameterMap theParams);
	
	@Override
	@Authorized(PrivilegeConstants.GET_PATIENTS)
	int getSearchResultsCount(@Nonnull SearchParameterMap theParams);
	
	@Override
	@Authorized({ PrivilegeConstants.ADD_PATIENTS, PrivilegeConstants.EDIT_PATIENTS })
	Patient createOrUpdate(@Nonnull Patient newEntry);
	
	@Override
	@Authorized(PrivilegeConstants.DELETE_PATIENTS)
	Patient delete(@Nonnull String uuid);
}
