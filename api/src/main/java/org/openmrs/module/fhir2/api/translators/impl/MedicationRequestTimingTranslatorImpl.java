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

import static lombok.AccessLevel.PROTECTED;

import javax.annotation.Nonnull;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Timing;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.OrderFrequency;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTimingRepeatComponentTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTimingTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MedicationRequestTimingTranslatorImpl implements MedicationRequestTimingTranslator {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private MedicationRequestTimingRepeatComponentTranslator timingRepeatComponentTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ConceptTranslator conceptTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private OrderService orderService;
	
	@Override
	public Timing toFhirResource(@Nonnull DrugOrder drugOrder) {
		if (drugOrder == null) {
			return null;
		}
		Timing timing = new Timing();
		timing.addEvent(drugOrder.getScheduledDate());
		if (drugOrder.getFrequency() != null) {
			timing.setCode(conceptTranslator.toFhirResource(drugOrder.getFrequency().getConcept()));
		}
		timing.setRepeat(timingRepeatComponentTranslator.toFhirResource(drugOrder));
		return timing;
	}
	
	@Override
	public DrugOrder toOpenmrsType(@Nonnull DrugOrder drugOrder, @Nonnull Timing timing) {
		if (timing.getEvent() != null && !timing.getEvent().isEmpty()) {
			drugOrder.setScheduledDate(timing.getEvent().get(0).getValue());
		}
		if (timing.hasCode()) {
			OrderFrequency frequency = null;
			for (Coding coding : timing.getCode().getCoding()) {
				if (coding.getCode() != null && frequency == null) {
					frequency = orderService.getOrderFrequencyByUuid(coding.getCode());
				}
			}
			if (frequency == null) {
				Concept frequencyConcept = conceptTranslator.toOpenmrsType(timing.getCode());
				if (frequencyConcept != null) {
					frequency = orderService.getOrderFrequencyByConcept(frequencyConcept);
				}
			}
			drugOrder.setFrequency(frequency);
		}
		timingRepeatComponentTranslator.toOpenmrsType(drugOrder, timing.getRepeat());
		return drugOrder;
	}
}
