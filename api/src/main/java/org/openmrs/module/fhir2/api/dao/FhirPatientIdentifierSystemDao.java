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

public interface FhirPatientIdentifierSystemDao {
	
	String getUrlByPatientIdentifierType(PatientIdentifierType patientIdentifierType);
	
	@Authorized(PrivilegeConstants.GET_IDENTIFIER_TYPES)
	Optional<FhirPatientIdentifierSystem> getFhirPatientIdentifierSystem(@Nonnull PatientIdentifierType identifierType);
	
	@Authorized(PrivilegeConstants.MANAGE_IDENTIFIER_TYPES)
	FhirPatientIdentifierSystem saveFhirPatientIdentifierSystem(
	        @Nonnull FhirPatientIdentifierSystem fhirPatientIdentifierSystem);
}
