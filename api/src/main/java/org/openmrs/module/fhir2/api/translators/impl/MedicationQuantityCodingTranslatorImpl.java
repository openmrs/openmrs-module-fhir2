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
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ICoding;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.CodingTranslator;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
/**
 * This is an implementation of Coding Translator that maps a Medication Quantity Concept to/from a
 * Coding in FHIR with a preferred set of systems and codes to prioritize. This will first favor
 * using RxNorm as the coding system. If this is not present on the OpenMRS concept, it will next
 * favor SNOMED-CT. Finally, if neither are present, it will favor the Concept UUID with a null
 * system.
 */
public class MedicationQuantityCodingTranslatorImpl implements CodingTranslator {
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Override
	public Coding toFhirResource(@Nonnull Concept concept) {
		CodeableConcept codeableConcept = conceptTranslator.toFhirResource(concept);
		if (codeableConcept == null) {
			return null;
		}
		Coding coding = getCodingForSystem(codeableConcept, FhirConstants.RX_NORM_SYSTEM_URI);
		if (coding == null) {
			coding = getCodingForSystem(codeableConcept, FhirConstants.SNOMED_SYSTEM_URI);
		}
		if (coding == null) {
			coding = getCodingForSystem(codeableConcept, null);
		}
		if (coding == null) {
			coding = codeableConcept.getCodingFirstRep();
		}
		return coding;
	}
	
	@Override
	public Concept toOpenmrsType(@Nonnull ICoding coding) {
		if (coding.getCode() != null) {
			CodeableConcept codeableConcept = new CodeableConcept();
			codeableConcept.addCoding(new Coding(coding.getSystem(), coding.getCode(), coding.getDisplay()));
			return conceptTranslator.toOpenmrsType(codeableConcept);
		}
		return null;
	}
	
	/**
	 * @return the coding on the CodeableConcept with the given system, or null if none found.
	 */
	Coding getCodingForSystem(CodeableConcept codeableConcept, String system) {
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
