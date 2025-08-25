/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
/*
 * Rights Reserved, Unlicensed
 */
package org.openmrs.module.fhir2.api.util;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class FhirNormalizationUtils {
	
	/**
	 * Normalize a CodeableConcept by trimming code/system/display and decoding HL7 v2 escape sequences.
	 */
	public static void normalize(CodeableConcept concept) {
		if (concept == null) {
			return;
		}
		for (Coding coding : concept.getCoding()) {
			if (coding.hasCode()) {
				coding.setCode(clean(coding.getCode()));
			}
			if (coding.hasSystem()) {
				coding.setSystem(clean(coding.getSystem()));
			}
			if (coding.hasDisplay()) {
				coding.setDisplay(clean(coding.getDisplay()));
			}
		}
	}
	
	private static String clean(String input) {
		if (input == null) {
			return null;
		}
		String trimmed = input.trim();
		return decodeHl7Escapes(trimmed);
	}
	
	/**
	 * Simplified HL7 v2 escape decoding (handles \F\ | \S\ | \T\ | \R\ | \E\).
	 */
	private static String decodeHl7Escapes(String value) {
		return value.replace("\\F\\", "|").replace("\\S\\", "^").replace("\\T\\", "&").replace("\\R\\", "~").replace("\\E\\",
		    "\\");
	}
}
