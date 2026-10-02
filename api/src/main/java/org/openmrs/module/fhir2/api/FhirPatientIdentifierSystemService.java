/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api;

import javax.annotation.Nonnull;

import java.util.Optional;

import org.hl7.fhir.r4.model.Identifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.model.FhirPatientIdentifierSystem;

public interface FhirPatientIdentifierSystemService extends FhirHelperService {
	
	String getUrlByPatientIdentifierType(PatientIdentifierType patientIdentifierType);
	
	/**
	 * Resolves the OpenMRS {@link PatientIdentifierType} a FHIR {@link Identifier} refers to. An
	 * identifier carrying a {@code system} is resolved against the registered identifier-system URLs;
	 * otherwise the identifier's {@code type.text} is treated as an identifier type name.
	 *
	 * @param identifier the FHIR identifier to resolve
	 * @return the matching identifier type, or {@code null} if it cannot be resolved
	 */
	PatientIdentifierType getPatientIdentifierTypeByIdentifier(Identifier identifier);
	
	Optional<FhirPatientIdentifierSystem> getFhirPatientIdentifierSystem(@Nonnull PatientIdentifierType identifierType);
	
	FhirPatientIdentifierSystem saveFhirPatientIdentifierSystem(
	        @Nonnull FhirPatientIdentifierSystem fhirPatientIdentifierSystem);
}
