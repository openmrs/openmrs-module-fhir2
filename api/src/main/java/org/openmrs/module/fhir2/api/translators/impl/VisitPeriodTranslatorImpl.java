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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Period;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.api.translators.EncounterPeriodTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class VisitPeriodTranslatorImpl implements EncounterPeriodTranslator<Visit> {

	@Override
	public Period toFhirResource(@Nonnull Visit visit) {
		Period result = new Period();
		result.setStart(visit.getStartDatetime());
		result.setEnd(visit.getStopDatetime());
		return result;
	}

	@Override
	public Visit toOpenmrsType(@Nonnull Visit visit, @Nonnull Period period) {
		if (period.hasStart()) {
			visit.setStartDatetime(period.getStart());
		} else if (visit.getStartDatetime() == null) {
			visit.setStartDatetime(visit.getDateCreated());
		}

		if (period.hasEnd()) {
			visit.setStopDatetime(period.getEnd());
		}

		return visit;
	}
}
