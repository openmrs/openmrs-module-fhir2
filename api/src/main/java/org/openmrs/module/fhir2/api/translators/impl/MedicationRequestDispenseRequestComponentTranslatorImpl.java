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
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Quantity;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.api.translators.MedicationRequestDispenseRequestComponentTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class MedicationRequestDispenseRequestComponentTranslatorImpl implements MedicationRequestDispenseRequestComponentTranslator {
	
	@Autowired
	private MedicationQuantityCodingTranslatorImpl quantityCodingTranslator;
	
	@Override
	public MedicationRequest.MedicationRequestDispenseRequestComponent toFhirResource(@Nonnull DrugOrder drugOrder) {
		MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequestComponent = new MedicationRequest.MedicationRequestDispenseRequestComponent();
		if (drugOrder.getQuantity() != null) {
			Quantity quantity = new Quantity();
			quantity.setValue(drugOrder.getQuantity());
			if (drugOrder.getQuantityUnits() != null) {
				Coding coding = quantityCodingTranslator.toFhirResource(drugOrder.getQuantityUnits());
				quantity.setSystem(coding.getSystem());
				quantity.setCode(coding.getCode());
				quantity.setUnit(coding.getDisplay());
			}
			dispenseRequestComponent.setQuantity(quantity);
		}
		if (drugOrder.getNumRefills() != null) {
			dispenseRequestComponent.setNumberOfRepeatsAllowed(drugOrder.getNumRefills());
		}
		if (drugOrder.getDateActivated() != null) {
			Period validityPeriod = new Period();
			validityPeriod.setStart(drugOrder.getDateActivated());
			dispenseRequestComponent.setValidityPeriod(validityPeriod);
		}
		return dispenseRequestComponent;
	}
	
	@Override
	public DrugOrder toOpenmrsType(@Nonnull DrugOrder drugOrder,
	        @Nonnull MedicationRequest.MedicationRequestDispenseRequestComponent resource) {
		if (resource.hasQuantity()) {
			Quantity quantity = resource.getQuantity();
			if (quantity.hasValue()) {
				drugOrder.setQuantity(quantity.getValue().doubleValue());
				Concept units = quantityCodingTranslator.toOpenmrsType(quantity);
				drugOrder.setQuantityUnits(units);
			}
		}
		drugOrder.setNumRefills(resource.getNumberOfRepeatsAllowed());
		if (resource.getValidityPeriod() != null && resource.getValidityPeriod().getStart() != null) {
			drugOrder.setDateActivated(resource.getValidityPeriod().getStart());
		}
		return drugOrder;
	}
}
