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

import org.hl7.fhir.r4.model.Timing;
import org.openmrs.DrugOrder;
import org.openmrs.OrderFrequency;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTimingComponentTranslator;
import org.springframework.stereotype.Component;

@Component
public class MedicationRequestTimingComponentTranslatorImpl implements MedicationRequestTimingComponentTranslator {
	
	@Override
	public Timing.TimingRepeatComponent toFhirResource(DrugOrder drugOrder) {
		if (drugOrder == null) {
			return null;
		}
		Timing.TimingRepeatComponent repeatComponent = new Timing.TimingRepeatComponent();
		if (drugOrder.getDuration() != null)
			repeatComponent.setDuration(drugOrder.getDuration());
		/*
		 * TODO
		 * Figure out how to map DurationUnit to UnitsOfTime since openMrs duration units is concept
		 * which differs across implementation. Make use of concept mappings
		 */
		//repeatComponent.setDurationUnit(drugOrder.getDurationUnits());
		OrderFrequency frequency = drugOrder.getFrequency();
		if (frequency != null) {
			if (frequency.getFrequencyPerDay() != null) {
				repeatComponent.setFrequency(frequency.getFrequencyPerDay().intValue());
				repeatComponent.setPeriod(1);
				repeatComponent.setPeriodUnit(Timing.UnitsOfTime.D);
			}
		}
		
		return repeatComponent;
	}
}
