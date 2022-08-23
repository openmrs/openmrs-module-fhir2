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

import org.hl7.fhir.r4.model.Timing;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.translators.DurationUnitTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class DurationUnitTranslatorImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String DURATION_UNIT_CONCEPT_DATA = "org/openmrs/module/fhir2/mapping/FhirDurationUnitTranslatorTest_initial_data.xml";
	
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
	
	@Autowired
	private DurationUnitTranslator durationUnitTranslator;
	
	@Autowired
	ConceptService conceptService;
	
	@Before
	public void setup() throws Exception {
		concept = new Concept();
		executeDataSet(DURATION_UNIT_CONCEPT_DATA);
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
		concept = conceptService.getConceptByUuid(SECONDS_UUID);
		
		result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(Timing.UnitsOfTime.S));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsMinutes() {
		concept = conceptService.getConceptByUuid(MINUTES_UUID);
		
		result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(Timing.UnitsOfTime.MIN));
		
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsHours() {
		concept = conceptService.getConceptByUuid(HOUR_UUID);
		
		result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(Timing.UnitsOfTime.H));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsDays() {
		concept = conceptService.getConceptByUuid(DAYS_UUID);
		
		result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(Timing.UnitsOfTime.D));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsWeeks() {
		concept = conceptService.getConceptByUuid(WEEKS_UUID);
		
		result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(Timing.UnitsOfTime.WK));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsMonths() {
		concept = conceptService.getConceptByUuid(MONTHS_UUID);
		
		result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(Timing.UnitsOfTime.MO));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderToUnitsOfTimeIsYears() {
		concept = conceptService.getConceptByUuid(YEARS_UUID);
		
		result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(Timing.UnitsOfTime.A));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateNullDuration() {
		Concept result = durationUnitTranslator.toOpenmrsType(Timing.UnitsOfTime.NULL);
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSeconds() {
		Concept result = durationUnitTranslator.toOpenmrsType(Timing.UnitsOfTime.S);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(SECONDS_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateMinutes() {
		Concept result = durationUnitTranslator.toOpenmrsType(Timing.UnitsOfTime.MIN);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(MINUTES_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateHours() {
		Concept result = durationUnitTranslator.toOpenmrsType(Timing.UnitsOfTime.H);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(HOUR_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateDays() {
		Concept result = durationUnitTranslator.toOpenmrsType(Timing.UnitsOfTime.D);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(DAYS_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateWeeks() {
		Concept result = durationUnitTranslator.toOpenmrsType(Timing.UnitsOfTime.WK);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(WEEKS_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateMonths() {
		Concept result = durationUnitTranslator.toOpenmrsType(Timing.UnitsOfTime.MO);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(MONTHS_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateYears() {
		Concept result = durationUnitTranslator.toOpenmrsType(Timing.UnitsOfTime.A);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(YEARS_UUID));
	}
}
