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

import org.hl7.fhir.r4.model.Condition;
import org.openmrs.Diagnosis;

public interface DiagnosisTranslator extends OpenmrsFhirUpdatableTranslator<Diagnosis, Condition> {
	
	/**
	 * Maps <T> an OpenMRS {@link Diagnosis} to a {@link org.hl7.fhir.r4.model.Condition}
	 *
	 * @param diagnosis the OpenMRS Diagnosis to translate
	 * @return the corresponding FHIR condition resource
	 */
	@Override
	org.hl7.fhir.r4.model.Condition toFhirResource(@Nonnull Diagnosis diagnosis);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Condition} to an <T> an OpenMRS {@link Diagnosis}
	 *
	 * @param condition the FHIR condition to translate
	 * @return the corresponding OpenMRS Diagnosis
	 */
	@Override
	Diagnosis toOpenmrsType(@Nonnull org.hl7.fhir.r4.model.Condition condition);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Condition} to an existing OpenMRS Diagnosis
	 *
	 * @param existingDiagnosis the existing diagnosis to update
	 * @param condition the condition to map
	 * @return an updated version of the existingDiagnosis
	 */
	@Override
	Diagnosis toOpenmrsType(@Nonnull Diagnosis existingDiagnosis, @Nonnull org.hl7.fhir.r4.model.Condition condition);
}
