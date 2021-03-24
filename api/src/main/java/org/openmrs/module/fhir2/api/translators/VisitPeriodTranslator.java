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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.hl7.fhir.r4.model.Period;

public interface VisitPeriodTranslator extends ToFhirTranslator<ImmutablePair<Date, Date>, Period>, OpenmrsFhirTranslator<ImmutablePair<Date, Date>, Period> {
	
	/**
	 * Maps an {@link ImmutablePair<Date, Date>} to a corresponding {@link org.hl7.fhir.r4.model.Period}
	 *
	 * @param period the pair of dates to translate
	 * @return the corresponding FHIR period
	 */
	@Override
	Period toFhirResource(@Nonnull ImmutablePair<Date, Date> period);
	
	/**
	 * Maps an {@link Period} to a {@link ImmutablePair<Date, Date>}
	 *
	 * @param resource the period to map
	 * @return the mapped pair of dates
	 */
	@Override
	ImmutablePair<Date, Date> toOpenmrsType(@Nonnull Period resource);
}
