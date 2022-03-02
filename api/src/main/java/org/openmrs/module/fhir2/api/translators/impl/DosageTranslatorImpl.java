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
import javax.annotation.Nullable;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Dosage;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.DosageTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTimingTranslator;
import org.openmrs.module.fhir2.api.util.FhirCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class DosageTranslatorImpl implements DosageTranslator {
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Autowired
	private MedicationRequestTimingTranslator timingTranslator;
	
	@Override
	public Dosage toFhirResource(@Nonnull DrugOrder drugOrder) {
		return toFhirResourceInternal(drugOrder, null);
	}
	
	@Override
	public Dosage toFhirResource(@Nonnull DrugOrder drugOrder, @Nullable FhirCache cache) {
		return toFhirResourceInternal(drugOrder, cache);
	}
	
	protected Dosage toFhirResourceInternal(@Nonnull DrugOrder drugOrder, @Nullable FhirCache cache) {
		if (drugOrder == null) {
			return null;
		}
		
		Dosage dosage = new Dosage();
		dosage.setText(drugOrder.getDosingInstructions());
		dosage.setAsNeeded(new BooleanType(drugOrder.getAsNeeded()));
		dosage.setRoute(conceptTranslator.toFhirResource(drugOrder.getRoute(), cache));
		dosage.setTiming(timingTranslator.toFhirResource(drugOrder, cache));
		
		return dosage;
	}
}
