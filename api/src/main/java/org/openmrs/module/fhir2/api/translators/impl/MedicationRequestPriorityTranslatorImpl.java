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

import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.api.translators.MedicationRequestPriorityTranslator;
import org.springframework.stereotype.Component;

@Component
public class MedicationRequestPriorityTranslatorImpl implements MedicationRequestPriorityTranslator {
	
	@Override
	public MedicationRequest.MedicationRequestPriority toFhirResource(DrugOrder.Urgency urgency) {
		switch (urgency) {
			case ROUTINE:
			case ON_SCHEDULED_DATE:
				return MedicationRequest.MedicationRequestPriority.ROUTINE;
			case STAT:
				return MedicationRequest.MedicationRequestPriority.STAT;
			default:
				return MedicationRequest.MedicationRequestPriority.NULL;
		}
	}
	
	@Override
	public DrugOrder.Urgency toOpenmrsType(MedicationRequest.MedicationRequestPriority medicationRequestPriority) {
		switch (medicationRequestPriority) {
			case ROUTINE:
				return DrugOrder.Urgency.ROUTINE;
			case STAT:
				return DrugOrder.Urgency.STAT;
			default:
				return null;
		}
	}
}
