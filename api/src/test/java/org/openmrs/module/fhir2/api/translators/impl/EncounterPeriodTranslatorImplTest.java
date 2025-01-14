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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Date;

import org.hl7.fhir.r4.model.Period;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Encounter;
import org.openmrs.module.fhir2.api.translators.EncounterPeriodTranslator;

@RunWith(MockitoJUnitRunner.class)
public class EncounterPeriodTranslatorImplTest {
	
	private EncounterPeriodTranslator<Encounter> encounterPeriodTranslator;
	
	@Before
	public void setup() {
		encounterPeriodTranslator = new EncounterPeriodTranslatorImpl();
	}
	
	@Test
	public void toFhirResource_shouldMapEncounterDatetimeToPeriod() {
		Date encounterDate = new Date();
		
		Encounter encounter = new Encounter();
		encounter.setEncounterDatetime(encounterDate);
		
		Period result = encounterPeriodTranslator.toFhirResource(encounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getStart(), notNullValue());
		assertThat(result.getEnd(), nullValue());
		assertThat(result.getStart(), equalTo(encounterDate));
	}
	
	@Test
	public void toOpenmrsObject_shouldMapPeriodStartToEncounterDatetime() {
		Date encounterDate = new java.util.Date();
		
		Encounter encounter = new Encounter();
		
		Period period = new Period();
		period.setStart(encounterDate);
		
		Encounter result = encounterPeriodTranslator.toOpenmrsType(encounter, period);
		
		assertThat(result, notNullValue());
		assertThat(result.getEncounterDatetime(), equalTo(encounterDate));
	}
	
	@Test
	public void toOpenmrsObject_shouldMapPeriodEndToEncounterDatetimeIsNoStartProvided() {
		Date encounterDate = new java.util.Date();
		
		Encounter encounter = new Encounter();
		
		Period period = new Period();
		period.setEnd(encounterDate);
		
		Encounter result = encounterPeriodTranslator.toOpenmrsType(encounter, period);
		
		assertThat(result, notNullValue());
		assertThat(result.getEncounterDatetime(), equalTo(encounterDate));
	}
	
	@Test
	public void toOpenmrsObject_shouldNotTouchEncounterDatetimeIfPeriodHasNoStartOrEnd() {
		Date encounterDate = new java.util.Date();
		
		Encounter encounter = new Encounter();
		encounter.setEncounterDatetime(encounterDate);
		
		Period period = new Period();
		
		Encounter result = encounterPeriodTranslator.toOpenmrsType(encounter, period);
		
		assertThat(result, notNullValue());
		assertThat(result.getEncounterDatetime(), equalTo(encounterDate));
	}
	
}
