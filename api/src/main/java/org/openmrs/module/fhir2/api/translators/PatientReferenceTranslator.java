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

import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Patient;

public interface PatientReferenceTranslator extends OpenmrsFhirTranslator<Patient, Reference> {
	
	/**
	 * Maps an {@link Patient} to a FHIR reference
	 *
	 * @param patient the patient to translate
	 * @return the corresponding FHIR reference
	 */
	@Override
	Reference toFhirResource(Patient patient);
	
	/**
	 * Maps a FHIR reference to an {@link Patient}
	 *
	 * @param patient the FHIR reference to translate
	 * @return the corresponding OpenMRS patient
	 */
	@Override
	Patient toOpenmrsType(Reference patient);
}
