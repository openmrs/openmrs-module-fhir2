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

import org.hl7.fhir.r4.model.CodeableConcept;
import org.openmrs.Obs;

public interface ObservationInterpretationTranslator extends ToFhirTranslator<Obs, CodeableConcept>, UpdatableOpenmrsTranslator<Obs, CodeableConcept> {
	
	/**
	 * Maps an {@link Obs} to an {@link org.hl7.fhir.r4.model.CodeableConcept}
	 *
	 * @param obs the OpenMRS obs element to translate
	 * @return a FHIR CodeableConcept representing the interpretation of this Observation if any
	 */
	@Override
	CodeableConcept toFhirResource(@Nonnull Obs obs);
	
	/**
	 * Maps an {@link org.hl7.fhir.r4.model.CodeableConcept} to an {@link Obs}
	 *
	 * @param existingObs the existingObs to update
	 * @param resource the resource to map
	 * @return the OpenMRS observation with the interpretation updated
	 */
	@Override
	Obs toOpenmrsType(@Nonnull Obs existingObs, @Nonnull CodeableConcept resource);
}
