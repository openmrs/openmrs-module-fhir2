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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.junit.jupiter.api.Test;

public class FhirNormalizationUtilsTest {
	
	@Test
	public void shouldTrimAndDecodeCodings() {
		CodeableConcept concept = new CodeableConcept();
		concept.addCoding(
		    new Coding().setSystem("  http://loinc.org  ").setCode(" 1234-5 ").setDisplay("Blood Test \\F\\ Panel"));
		
		FhirNormalizationUtils.normalize(concept);
		
		Coding coding = concept.getCodingFirstRep();
		assertEquals("http://loinc.org", coding.getSystem());
		assertEquals("1234-5", coding.getCode());
		assertEquals("Blood Test | Panel", coding.getDisplay());
	}
}
