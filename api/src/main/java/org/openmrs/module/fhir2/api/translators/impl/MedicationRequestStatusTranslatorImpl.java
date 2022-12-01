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

import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.api.translators.MedicationRequestStatusTranslator;
import org.springframework.stereotype.Component;

@Component
public class MedicationRequestStatusTranslatorImpl implements MedicationRequestStatusTranslator {
	
	@Override
	public MedicationRequest.MedicationRequestStatus toFhirResource(@Nonnull DrugOrder drugOrder) {
		if (drugOrder == null) {
			return null;
		}
		
		if (drugOrder.isActive()) {
			return MedicationRequest.MedicationRequestStatus.ACTIVE;
		} else if (drugOrder.isDiscontinuedRightNow()) {
			return MedicationRequest.MedicationRequestStatus.CANCELLED;
		} else if (drugOrder.isExpired()) {
			return MedicationRequest.MedicationRequestStatus.STOPPED;
		}
		return MedicationRequest.MedicationRequestStatus.UNKNOWN;
	}
}
