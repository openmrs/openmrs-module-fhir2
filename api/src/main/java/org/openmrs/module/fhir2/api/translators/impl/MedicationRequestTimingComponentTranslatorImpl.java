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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Timing;
import org.openmrs.DrugOrder;
import org.openmrs.OrderFrequency;
import org.openmrs.module.fhir2.api.translators.DurationUnitTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTimingComponentTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
@Setter(AccessLevel.PACKAGE)
public class MedicationRequestTimingComponentTranslatorImpl implements MedicationRequestTimingComponentTranslator {
	
	@Autowired
	private DurationUnitTranslator durationUnitTranslator;
	
	@Override
	public Timing.TimingRepeatComponent toFhirResource(@Nonnull DrugOrder drugOrder) {

		Timing.TimingRepeatComponent repeatComponent = new Timing.TimingRepeatComponent();
		if (drugOrder.getDuration() != null) {
			repeatComponent.setDuration(drugOrder.getDuration());
		}
		
		if (drugOrder.getDurationUnits() != null) {
			repeatComponent.setDurationUnit(durationUnitTranslator.toFhirResource(drugOrder.getDurationUnits()));
		}

		OrderFrequency frequency = drugOrder.getFrequency();
		if (frequency != null) {
			Double frequencyPerDay = frequency.getFrequencyPerDay();
			if (frequencyPerDay != null) {
				// If the frequency per day translates into an even frequency per hour, default to once every X hours
				if (frequencyPerDay > 1 && 24 % frequencyPerDay == 0) {
					repeatComponent.setFrequency(1);
					repeatComponent.setPeriod(24/frequencyPerDay);
					repeatComponent.setPeriodUnit(Timing.UnitsOfTime.H);
				}
				// Otherwise, set to once every X days
				else {
					repeatComponent.setFrequency(1);
					repeatComponent.setPeriod(1/frequencyPerDay);
					repeatComponent.setPeriodUnit(Timing.UnitsOfTime.D);
				}
			}
		}

		return repeatComponent;
	}

	@Override
	public DrugOrder toOpenmrsType(@Nonnull DrugOrder drugOrder, @Nonnull Timing.TimingRepeatComponent repeatComponent) {
		if (repeatComponent.getDuration() != null) {
			drugOrder.setDuration(repeatComponent.getDuration().intValue());
		}
		if (repeatComponent.getDurationUnit() != null) {
			drugOrder.setDurationUnits(durationUnitTranslator.toOpenmrsType(repeatComponent.getDurationUnit()));
		}
		// TODO: This isn't really possible to implement as-is
		return drugOrder;
	}
}
