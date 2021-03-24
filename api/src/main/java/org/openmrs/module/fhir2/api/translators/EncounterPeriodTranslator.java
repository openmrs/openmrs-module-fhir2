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

public interface EncounterPeriodTranslator extends ToFhirTranslator<Date, Period>, OpenmrsFhirTranslator<Date, Period> {
	
	/**
	 * Maps an {@link Date} to a corresponding {@link org.hl7.fhir.r4.model.Period}
	 *
	 * @param encounterDatetime the encounter datetime to translate
	 * @return the corresponding FHIR period
	 */
	@Override
	Period toFhirResource(@Nonnull Date encounterDatetime);
	
	/**
	 * Maps an {@link org.hl7.fhir.r4.model} to a {@link Date}
	 *
	 * @param resource the period to map
	 * @return an updated version of the visit
	 */
	@Override
	Date toOpenmrsType(@Nonnull Period resource);
}
