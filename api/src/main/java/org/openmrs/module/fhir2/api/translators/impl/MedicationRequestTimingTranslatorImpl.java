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
import org.hl7.fhir.r4.model.Timing;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTimingComponentTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTimingTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class MedicationRequestTimingTranslatorImpl implements MedicationRequestTimingTranslator {
	
	@Autowired
	private MedicationRequestTimingComponentTranslator timingComponentTranslator;
	
	@Override
	public Timing toFhirResource(@Nonnull DrugOrder drugOrder) {
		if (drugOrder == null) {
			return null;
		}
		
		Timing timing = new Timing();
		timing.addEvent(drugOrder.getScheduledDate());
		timing.setRepeat(timingComponentTranslator.toFhirResource(drugOrder));
		
		return timing;
	}
}
