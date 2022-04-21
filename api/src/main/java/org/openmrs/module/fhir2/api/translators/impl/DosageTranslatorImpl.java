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
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.SimpleQuantity;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.DosageTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTimingTranslator;
import org.openmrs.util.OpenmrsUtil;
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
		if (drugOrder == null) {
			return null;
		}
		Dosage dosage = new Dosage();
		dosage.setText(drugOrder.getDosingInstructions());
		dosage.setAsNeeded(new BooleanType(drugOrder.getAsNeeded()));
		dosage.setRoute(conceptTranslator.toFhirResource(drugOrder.getRoute()));
		dosage.setTiming(timingTranslator.toFhirResource(drugOrder));
		
		if (drugOrder.getDose() != null || drugOrder.getDoseUnits() != null) {
			Dosage.DosageDoseAndRateComponent doseAndRate = new Dosage.DosageDoseAndRateComponent();
			Quantity dose = new SimpleQuantity();
			dose.setValue(drugOrder.getDose());
			if (drugOrder.getDoseUnits() != null) {
				CodeableConcept doseUnits = conceptTranslator.toFhirResource(drugOrder.getDoseUnits());
				if (doseUnits != null) {
					Coding coding = getCodingForSystem(doseUnits, FhirConstants.RX_NORM_SYSTEM_URI);
					if (coding == null) {
						coding = getCodingForSystem(doseUnits, FhirConstants.SNOMED_SYSTEM_URI);
					}
					if (coding == null) {
						coding = getCodingForSystem(doseUnits, null);
					}
					if (coding == null) {
						coding = doseUnits.getCodingFirstRep();
					}
					dose.setSystem(coding.getSystem());
					dose.setCode(coding.getCode());
					dose.setUnit(coding.getDisplay());
				}
			}
			doseAndRate.setDose(dose);
			dosage.addDoseAndRate(doseAndRate);
		}
		
		return dosage;
	}
	
	@Override
	public DrugOrder toOpenmrsType(@Nonnull DrugOrder drugOrder, @Nonnull Dosage dosage) {
		drugOrder.setDosingInstructions(dosage.getText());
		if (dosage.getAsNeededBooleanType() != null) {
			drugOrder.setAsNeeded(dosage.getAsNeededBooleanType().getValue());
		}
		drugOrder.setRoute(conceptTranslator.toOpenmrsType(dosage.getRoute()));
		timingTranslator.toOpenmrsType(drugOrder, dosage.getTiming());
		Dosage.DosageDoseAndRateComponent doseAndRate = dosage.getDoseAndRateFirstRep();
		Quantity dose = doseAndRate.getDoseQuantity();
		if (dose != null) {
			if (dose.getValue() != null) {
				drugOrder.setDose(dose.getValue().doubleValue());
			}
			CodeableConcept doseUnits = new CodeableConcept();
			doseUnits.addCoding(new Coding(dose.getSystem(), dose.getCode(), dose.getDisplay()));
			Concept doseUnitsConcept = conceptTranslator.toOpenmrsType(doseUnits);
			drugOrder.setDoseUnits(doseUnitsConcept);
		}
		return drugOrder;
	}
	
	/**
	 * @return the coding on the CodeableConcept with the given system, or null if none found.
	 */
	private Coding getCodingForSystem(CodeableConcept codeableConcept, String system) {
		if (codeableConcept != null && codeableConcept.getCoding() != null) {
			for (Coding coding : codeableConcept.getCoding()) {
				if (OpenmrsUtil.nullSafeEqualsIgnoreCase(system, coding.getSystem())) {
					return coding;
				}
			}
		}
		return null;
	}
}
