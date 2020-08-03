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

import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;

public interface FhirPatientDao extends FhirDao<Patient> {
	
	@Authorized(PrivilegeConstants.GET_PATIENTS)
	Patient getPatientById(@NotNull Integer id);
	
	@Override
	@Authorized(PrivilegeConstants.GET_PATIENTS)
	Patient get(String uuid);
	
	@Authorized(PrivilegeConstants.GET_PATIENT_IDENTIFIERS)
	PatientIdentifierType getPatientIdentifierTypeByNameOrUuid(String name, String uuid);
	
	@Override
	@Authorized({ PrivilegeConstants.ADD_PATIENTS, PrivilegeConstants.EDIT_PATIENTS })
	Patient createOrUpdate(Patient newEntry);
	
	@Override
	@Authorized(PrivilegeConstants.DELETE_PATIENTS)
	Patient delete(String uuid);
	
	@Override
	@Authorized(PrivilegeConstants.GET_PATIENTS)
	List<String> getSearchResultUuids(SearchParameterMap theParams);
	
	@Override
	@Authorized(PrivilegeConstants.GET_PATIENTS)
	List<Patient> getSearchResults(SearchParameterMap theParams, List<String> matchingResourceUuids, int firstResult,
	        int lastResult);
}
