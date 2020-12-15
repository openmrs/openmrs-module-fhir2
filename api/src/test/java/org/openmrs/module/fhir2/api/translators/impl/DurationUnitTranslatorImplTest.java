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

import org.hl7.fhir.r4.model.Timing;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;

public class DurationUnitTranslatorImplTest {
	
	private static final String SECONDS_UUID = "162583AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String MINUTES_UUID = "1733AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String HOUR_UUID = "1822AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String DAYS_UUID = "1072AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WEEKS_UUID = "1073AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String MONTHS_UUID = "1074AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String YEARS_UUID = "1734AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_UUID = "2909AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private Concept concept;
	
	private Timing.UnitsOfTime result;
	
	private DurationUnitTranslatorImpl durationUnitTranslator;
	
	@Before
	public void setup() {
		
		durationUnitTranslator = new DurationUnitTranslatorImpl();
		concept = new Concept();
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitTimeIsNull() {
		
		concept.setUuid(WRONG_UUID);
		
		result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(Timing.UnitsOfTime.NULL));
		
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsSeconds() {
		
		concept.setUuid(SECONDS_UUID);
		
		result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(Timing.UnitsOfTime.S));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsMinutes() {
		
		concept.setUuid(MINUTES_UUID);
		
		result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(Timing.UnitsOfTime.MIN));
		
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsHours() {
		
		concept.setUuid(HOUR_UUID);
		
		result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(Timing.UnitsOfTime.H));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsDays() {
		
		concept.setUuid(DAYS_UUID);
		
		result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(Timing.UnitsOfTime.D));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsWeeks() {
		
		concept.setUuid(WEEKS_UUID);
		
		result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(Timing.UnitsOfTime.WK));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsMonths() {
		
		concept.setUuid(MONTHS_UUID);
		
		result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(Timing.UnitsOfTime.MO));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsYears() {
		
		concept.setUuid(YEARS_UUID);
		
		result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(Timing.UnitsOfTime.A));
	}
}
