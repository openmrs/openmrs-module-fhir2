/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import javax.annotation.Nonnull;

import java.util.Date;

import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.hl7.fhir.r4.model.Period;
import org.openmrs.module.fhir2.api.translators.VisitPeriodTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class VisitPeriodTranslatorImpl implements VisitPeriodTranslator {
	
	@Override
	public Period toFhirResource(@Nonnull ImmutablePair<Date, Date> pair) {
		Period period = new Period();
		
		period.setStart(pair.getKey());
		period.setEnd(pair.getValue());
		
		return period;
	}
	
	@Override
	public ImmutablePair<Date, Date> toOpenmrsType(@Nonnull Period resource) {
		return new ImmutablePair(resource.getStart(), resource.getEnd());
	}
}
