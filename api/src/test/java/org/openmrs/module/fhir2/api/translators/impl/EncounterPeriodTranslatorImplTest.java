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
import static org.hamcrest.Matchers.*;

import java.util.Date;

import junit.framework.TestCase;
import org.hl7.fhir.r4.model.Period;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.translators.EncounterPeriodTranslator;

@RunWith(MockitoJUnitRunner.class)
public class EncounterPeriodTranslatorImplTest extends TestCase {
	
	EncounterPeriodTranslator encounterPeriodTranslator;
	
	@Before
	public void setup() {
		encounterPeriodTranslator = new EncounterPeriodTranslatorImpl();
	}
	
	@Test
	public void toFhirResource_shouldMapEncounterDatetimeToPeriod() {
		Date encounterDate = new java.util.Date();
		
		Period result = encounterPeriodTranslator.toFhirResource(encounterDate);
		
		assertThat(result, notNullValue());
		assertThat(result.getStart(), notNullValue());
		assertThat(result.getEnd(), nullValue());
		assertThat(result.getStart(), equalTo(encounterDate));
	}
	
	@Test
	public void toOpenmrsObject_shouldMapPeriodToEncounterDatetime() {
		Date encounterDate = new java.util.Date();
		
		Period period = new Period();
		period.setStart(encounterDate);
		
		Date result = encounterPeriodTranslator.toOpenmrsType(period);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(encounterDate));
	}
	
}
