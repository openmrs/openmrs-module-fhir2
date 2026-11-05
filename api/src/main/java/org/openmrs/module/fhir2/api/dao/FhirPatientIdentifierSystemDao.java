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

import java.util.Optional;

import org.openmrs.PatientIdentifierType;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.model.FhirPatientIdentifierSystem;
import org.openmrs.util.PrivilegeConstants;

public interface FhirPatientIdentifierSystemDao extends FhirDaoAop {
	
	@Authorized(PrivilegeConstants.GET_IDENTIFIER_TYPES)
	String getUrlByPatientIdentifierType(PatientIdentifierType patientIdentifierType);
	
	@Authorized(PrivilegeConstants.GET_IDENTIFIER_TYPES)
	PatientIdentifierType getPatientIdentifierTypeByUrl(String url);
	
	/**
	 * Looks up a {@link PatientIdentifierType} by name or uuid. A non-retired type matching
	 * {@code name} or any type matching {@code uuid} qualifies; when {@code uuid} is supplied it wins
	 * over a name match.
	 *
	 * @param name the identifier type name to match, ignoring retired types
	 * @param uuid the identifier type uuid to match
	 * @return the matching identifier type, or {@code null} if none matches
	 */
	@Authorized(PrivilegeConstants.GET_PATIENT_IDENTIFIERS)
	PatientIdentifierType getPatientIdentifierTypeByNameOrUuid(String name, String uuid);
	
	@Authorized(PrivilegeConstants.GET_IDENTIFIER_TYPES)
	Optional<FhirPatientIdentifierSystem> getFhirPatientIdentifierSystem(@Nonnull PatientIdentifierType identifierType);
	
	@Authorized(PrivilegeConstants.MANAGE_IDENTIFIER_TYPES)
	FhirPatientIdentifierSystem saveFhirPatientIdentifierSystem(
	        @Nonnull FhirPatientIdentifierSystem fhirPatientIdentifierSystem);
}
