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

import org.hl7.fhir.r4.model.MedicationDispense;

public interface MedicationDispenseTranslator<T> extends OpenmrsFhirUpdatableTranslator<T, MedicationDispense> {
	
	/**
	 * Maps <T> an OpenMRS representation of a dispensed medication to a {@link MedicationDispense}
	 *
	 * @param dispenseData the OpenMRS representation to translate
	 * @return the corresponding FHIR condition resource
	 */
	@Override
	MedicationDispense toFhirResource(@Nonnull T dispenseData);
	
	/**
	 * Maps a {@link MedicationDispense} to an OpenMRS representation
	 *
	 * @param medicationDispense the FHIR condition to translate
	 * @return the corresponding OpenMRS representation
	 */
	@Override
	T toOpenmrsType(@Nonnull MedicationDispense medicationDispense);
	
	/**
	 * Maps a {@link MedicationDispense} to an existing <T> OpenMRS representation
	 *
	 * @param dispenseData the existing OpenMRS representation to update
	 * @param medicationDispense the condition to map
	 * @return an updated version of the OpenMRS representation
	 */
	@Override
	T toOpenmrsType(@Nonnull T dispenseData, @Nonnull MedicationDispense medicationDispense);
}
