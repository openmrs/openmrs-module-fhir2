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

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Timing;
import org.openmrs.Concept;
import org.openmrs.Duration;
import org.openmrs.module.fhir2.api.translators.DurationUnitTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class DurationUnitTranslatorImpl implements DurationUnitTranslator {
	
	private static Map<String, Timing.UnitsOfTime> codeMap;
	static {
		codeMap = new HashMap<>();
		codeMap.put(Duration.SNOMED_CT_SECONDS_CODE, Timing.UnitsOfTime.S);
		codeMap.put(Duration.SNOMED_CT_MINUTES_CODE, Timing.UnitsOfTime.MIN);
		codeMap.put(Duration.SNOMED_CT_HOURS_CODE, Timing.UnitsOfTime.H);
		codeMap.put(Duration.SNOMED_CT_DAYS_CODE, Timing.UnitsOfTime.D);
		codeMap.put(Duration.SNOMED_CT_WEEKS_CODE, Timing.UnitsOfTime.WK);
		codeMap.put(Duration.SNOMED_CT_MONTHS_CODE, Timing.UnitsOfTime.MO);
		codeMap.put(Duration.SNOMED_CT_YEARS_CODE, Timing.UnitsOfTime.A);
	}
	
	@Override
	public Timing.UnitsOfTime toFhirResource(@Nonnull Concept concept) {
		String durationCode = Duration.getCode(concept);
		Timing.UnitsOfTime unitsOfTime = codeMap.get(durationCode);
		if (unitsOfTime == null) {
			return Timing.UnitsOfTime.NULL;
		}
		return unitsOfTime;
	}
}
