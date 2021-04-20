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
import org.hl7.fhir.r4.model.Period;
import org.openmrs.Encounter;
import org.openmrs.module.fhir2.api.translators.EncounterPeriodTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class EncounterPeriodTranslatorImpl implements EncounterPeriodTranslator<Encounter> {
	
	@Override
	public Period toFhirResource(@Nonnull Encounter encounter) {
		Period result = new Period();
		result.setStart(encounter.getEncounterDatetime());
		return result;
	}
	
	@Override
	public Encounter toOpenmrsType(@Nonnull Encounter encounter, @Nonnull Period period) {
		Date encounterDateTime;
		if (period.hasStart()) {
			encounterDateTime = period.getStart();
		} else if (period.hasEnd()) {
			encounterDateTime = period.getEnd();
		} else if (encounter.getEncounterDatetime() == null) {
			encounterDateTime = encounter.getDateCreated();
		} else {
			encounterDateTime = encounter.getEncounterDatetime();
		}

		if (encounterDateTime == null) {
			encounterDateTime = new Date();
		}

		encounter.setEncounterDatetime(encounterDateTime);
		return encounter;
	}
}
