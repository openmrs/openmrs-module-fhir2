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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.junit.Before;
import org.junit.Test;

public class AllergyIntoleranceCriticalityTranslatorImplTest {
	
	private AllergyIntoleranceCriticalityTranslatorImpl criticalityTranslator;
	
	@Before
	public void setup() {
		criticalityTranslator = new AllergyIntoleranceCriticalityTranslatorImpl();
	}
	
	@Test
	public void shouldTranslateSEVERESeverityToHIGHCriticality() {
		assertThat(criticalityTranslator.toFhirResource(AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE),
		    equalTo(AllergyIntolerance.AllergyIntoleranceCriticality.HIGH));
	}
	
	@Test
	public void shouldTranslateMILDSeverityToLOWCriticality() {
		assertThat(criticalityTranslator.toFhirResource(AllergyIntolerance.AllergyIntoleranceSeverity.MILD),
		    equalTo(AllergyIntolerance.AllergyIntoleranceCriticality.LOW));
	}
	
	@Test
	public void shouldTranslateMODERATESeverityToUNABLETOACCESSCriticality() {
		assertThat(criticalityTranslator.toFhirResource(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE),
		    equalTo(AllergyIntolerance.AllergyIntoleranceCriticality.UNABLETOASSESS));
	}
	
	@Test
	public void shouldTranslateNULLSeverityToUNABLETOACCESSCriticality() {
		assertThat(criticalityTranslator.toFhirResource(AllergyIntolerance.AllergyIntoleranceSeverity.NULL),
		    equalTo(AllergyIntolerance.AllergyIntoleranceCriticality.UNABLETOASSESS));
	}
}
