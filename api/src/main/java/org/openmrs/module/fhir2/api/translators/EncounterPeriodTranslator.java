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

import java.util.Date;

import org.hl7.fhir.r4.model.Period;
import org.openmrs.OpenmrsData;

public interface EncounterPeriodTranslator<T extends OpenmrsData> extends ToFhirTranslator<T, Period>, UpdatableOpenmrsTranslator<T, Period> {
	
	/**
	 * Maps an {@link Date} to a corresponding {@link org.hl7.fhir.r4.model.Period}
	 *
	 * @param encounter the encounter to translate
	 * @return the corresponding FHIR period
	 */
	@Override
	Period toFhirResource(@Nonnull T encounter);
	
	/**
	 * Maps an {@link org.hl7.fhir.r4.model} to a {@link Date}
	 *
	 * @param encounter the encounter to update
	 * @param period the period to map
	 * @return an updated version of the visit
	 */
	@Override
	T toOpenmrsType(@Nonnull T encounter, @Nonnull Period period);
}
