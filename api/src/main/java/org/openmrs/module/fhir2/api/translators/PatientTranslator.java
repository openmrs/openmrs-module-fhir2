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

import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Provenance;

public interface PatientTranslator extends OpenmrsFhirUpdatableTranslator<org.openmrs.Patient, Patient>, ToFhirProvenance<org.openmrs.Patient> {
	
	/**
	 * Maps a {@link org.openmrs.Patient} to a {@link Patient}
	 *
	 * @param patient the patient to translate
	 * @return the corresponding FHIR patient
	 */
	@Override
	Patient toFhirResource(org.openmrs.Patient patient);
	
	/**
	 * Maps a {@link Patient} to a {@link org.openmrs.Patient}
	 *
	 * @param patient the FHIR patient to map
	 * @return the corresponding OpenMRS patient
	 */
	@Override
	org.openmrs.Patient toOpenmrsType(Patient patient);
	
	/**
	 * Maps a {@link Patient} to an existing {@link org.openmrs.Patient}
	 *
	 * @param currentPatient the patient to update
	 * @param patient the FHIR patient to map
	 * @return the updated OpenMRS patient
	 */
	@Override
	org.openmrs.Patient toOpenmrsType(org.openmrs.Patient currentPatient, Patient patient);
	
	/**
	 * Maps a {@link org.openmrs.Patient} to a {@link org.hl7.fhir.r4.model.Provenance} resource
	 *
	 * @param patient the OpenMRS object to translate
	 * @return the corresponding {@link org.hl7.fhir.r4.model.Provenance} resource
	 */
	@Override
	Provenance getUpdateProvenance(org.openmrs.Patient patient);
	
	/**
	 * Maps a {@link org.openmrs.Patient} to a {@link org.hl7.fhir.r4.model.Provenance} resource
	 *
	 * @param patient the OpenMRS object to translate
	 * @return the corresponding {@link org.hl7.fhir.r4.model.Provenance} resource
	 */
	@Override
	Provenance getCreateProvenance(org.openmrs.Patient patient);
}
