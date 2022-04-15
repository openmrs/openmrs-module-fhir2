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
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Dosage;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.DosageTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTimingTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
@Setter(AccessLevel.PACKAGE)
public class DosageTranslatorImpl implements DosageTranslator {
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Autowired
	private MedicationRequestTimingTranslator timingTranslator;
	
	@Override
	public Dosage toFhirResource(@Nonnull DrugOrder drugOrder) {
		Dosage dosage = new Dosage();
		dosage.setText(drugOrder.getDosingInstructions());
		dosage.setAsNeeded(new BooleanType(drugOrder.getAsNeeded()));
		dosage.setRoute(conceptTranslator.toFhirResource(drugOrder.getRoute()));
		dosage.setTiming(timingTranslator.toFhirResource(drugOrder));
		return dosage;
	}

	@Override
	public DrugOrder toOpenmrsType(@Nonnull DrugOrder drugOrder, @Nonnull Dosage dosage) {
		drugOrder.setDosingInstructions(dosage.getText());
		drugOrder.setAsNeeded(drugOrder.getAsNeeded());
		drugOrder.setRoute(conceptTranslator.toOpenmrsType(dosage.getRoute()));
		timingTranslator.toOpenmrsType(drugOrder, dosage.getTiming());
		return drugOrder;
	}
}
