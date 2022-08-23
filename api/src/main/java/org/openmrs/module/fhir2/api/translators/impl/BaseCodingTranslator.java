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
import org.openmrs.module.fhir2.api.translators.CodingTranslator;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;

@Setter(AccessLevel.PACKAGE)
public abstract class BaseCodingTranslator implements CodingTranslator {
	
	@Autowired
	protected ConceptTranslator conceptTranslator;
	
	/**
	 * Base implementation of conversion between a FHIR Coding interface and OpenMRS Concept
	 * representation.
	 */
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
	public static Coding getCodingForSystem(CodeableConcept codeableConcept, String system) {
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
