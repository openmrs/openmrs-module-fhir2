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

import org.hl7.fhir.r4.model.Timing;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.api.mappings.DurationUnitMap;
import org.openmrs.module.fhir2.api.translators.DurationUnitTranslator;
import org.springframework.beans.factory.annotation.Autowired;

public class DurationUnitTranslatorImpl implements DurationUnitTranslator {
	
	@Autowired
	private DurationUnitMap durationUnitMap;
	
	@Override
	public Timing.UnitsOfTime toFhirResource(@Nonnull DrugOrder drugOrder) {
		
		if (drugOrder.getDurationUnits().getUuid() == null) {
			return null;
		}
		
		return durationUnitMap.getDurationUnit(drugOrder.getDurationUnits().getUuid());
		
	}
}
