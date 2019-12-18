/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators;

import org.hl7.fhir.r4.model.Identifier;
import org.openmrs.PatientIdentifier;

public interface PatientIdentifierTranslator extends OpenmrsFhirTranslator<PatientIdentifier, Identifier> {
	
	/**
	 * Maps a {@link PatientIdentifier} to a FHIR {@link Identifier}
	 * 
	 * @param identifier the patient identifier to translate
	 * @return the corresponding FHIR identifier
	 */
	@Override
	Identifier toFhirResource(PatientIdentifier identifier);
	
	/**
	 * Maps a {@link Identifier} to a {@link PatientIdentifier}
	 * 
	 * @param identifier the identifier to translate
	 * @return the corresponding OpenMRS patient identifier
	 */
	@Override
	PatientIdentifier toOpenmrsType(Identifier identifier);
}
