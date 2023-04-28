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

import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS;

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;
import org.springframework.stereotype.Component;

@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.2.* - 2.*")
public class MedicationRequestTranslatorImpl_2_2 extends MedicationRequestTranslatorImpl implements MedicationRequestTranslator {
	
	@Override
	public MedicationRequest toFhirResource(@Nonnull DrugOrder drugOrder) {
		MedicationRequest medicationRequest = super.toFhirResource(drugOrder);
		
		if (drugOrder.getFulfillerStatus() != null) {
			Extension extension = new Extension();
			extension.setUrl(OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS);
			extension.setValue(new CodeType(drugOrder.getFulfillerStatus().toString()));
			medicationRequest.addExtension(extension);
		}
		
		return medicationRequest;
	}
	
	@Override
	public DrugOrder toOpenmrsType(@Nonnull MedicationRequest medicationRequest) {
		return toOpenmrsType(new DrugOrder(), medicationRequest);
		
	}
	
	@Override
	public DrugOrder toOpenmrsType(@Nonnull DrugOrder existingDrugOrder, @Nonnull MedicationRequest medicationRequest) {
		DrugOrder drugOrder = super.toOpenmrsType(existingDrugOrder, medicationRequest);
		
		if (medicationRequest.getExtensionByUrl(OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS) != null) {
			drugOrder.setFulfillerStatus(Order.FulfillerStatus
			        .valueOf(medicationRequest.getExtensionByUrl(OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS)
			                .getValue().toString().toUpperCase()));
		}
		
		return drugOrder;
	}
	
}
