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

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.openmrs.PatientProgram;

public interface EpisodeOfCareTranslator extends OpenmrsFhirUpdatableTranslator<PatientProgram, EpisodeOfCare> {
	
	/**
	 * Maps a {@link org.openmrs.PatientProgram} to a {@link EpisodeOfCare}
	 *
	 * @param patientProgram the PatientProgram to translate
	 * @return the corresponding FHIR EpisodeOfCare
	 */
	@Override
	EpisodeOfCare toFhirResource(@Nonnull PatientProgram patientProgram);
	
	/**
	 * Maps a {@link EpisodeOfCare} to a {@link org.openmrs.PatientProgram}
	 *
	 * @param episodeOfCare the FHIR EpisodeOfCare to map
	 * @return the corresponding OpenMRS PatientProgram
	 */
	@Override
	PatientProgram toOpenmrsType(@Nonnull EpisodeOfCare episodeOfCare);
	
	/**
	 * Maps a {@link EpisodeOfCare} to an existing {@link org.openmrs.PatientProgram}
	 *
	 * @param patientProgram the PatientProgram to update
	 * @param episodeOfCare the FHIR EpisodeOfCare to map
	 * @return the updated OpenMRS PatientProgram
	 */
	@Override
	PatientProgram toOpenmrsType(@Nonnull PatientProgram patientProgram, @Nonnull EpisodeOfCare episodeOfCare);
}
