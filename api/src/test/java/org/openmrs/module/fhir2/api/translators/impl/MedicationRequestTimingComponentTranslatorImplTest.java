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
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.hl7.fhir.r4.model.Timing;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.OrderFrequency;
import org.openmrs.module.fhir2.api.translators.DurationUnitTranslator;

@RunWith(MockitoJUnitRunner.class)
public class MedicationRequestTimingComponentTranslatorImplTest {
	
	private static final String SECONDS_UUID = "162583AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String MINUTES_UUID = "1733AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String HOUR_UUID = "1822AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String DAYS_UUID = "1072AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WEEKS_UUID = "1073AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String MONTHS_UUID = "1074AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String YEARS_UUID = "1734AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_UUID = "2909AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Mock
	private DurationUnitTranslator durationUnitTranslator;
	
	private static final int DURATION = 2;
	
	private static final double FREQUENCY_PER_DAY = 3.0;
	
	private MedicationRequestTimingComponentTranslatorImpl requestTimingComponentTranslator;
	
	private DrugOrder drugOrder;
	
	private Concept concept;
	
	@Before
	public void setup() {
		requestTimingComponentTranslator = new MedicationRequestTimingComponentTranslatorImpl();
		requestTimingComponentTranslator.setDurationUnitTranslator(durationUnitTranslator);
		
		concept = new Concept();
		drugOrder = new DrugOrder();
		drugOrder.setDuration(DURATION);
		
		OrderFrequency frequency = new OrderFrequency();
		frequency.setFrequencyPerDay(FREQUENCY_PER_DAY);
		drugOrder.setFrequency(frequency);
	}
	
	@Test
	public void toFhirResource_shouldTranslateDurationToFhirType() {
		Timing.TimingRepeatComponent result = requestTimingComponentTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getDuration(), notNullValue());
		assertThat(result.getDuration(), equalTo(new BigDecimal(2)));
	}
	
	@Test
	public void toFhirResource_shouldTranslateFrequencyToFhirType() {
		Timing.TimingRepeatComponent result = requestTimingComponentTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getFrequency(), notNullValue());
		assertThat(result.getFrequency(), equalTo(3));
	}
	
	@Test
	public void toFhirResource_shouldSetPeriodAndItsUnitsIfFrequencyPerDayIsNotNull() {
		Timing.TimingRepeatComponent result = requestTimingComponentTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getPeriod(), notNullValue());
		assertThat(result.getPeriod(), equalTo(new BigDecimal(1)));
		assertThat(result.getPeriodUnit(), equalTo(Timing.UnitsOfTime.D));
	}
	
	@Test
	public void toFhirResource_shouldSetNullDurationIfDrugOrderDurationIsNull() {
		drugOrder.setDuration(null);
		Timing.TimingRepeatComponent result = requestTimingComponentTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getDuration(), nullValue());
	}
	
	@Test
	public void toFhirResource_shouldNotSetPeriodAndItsUnitsIfFrequencyIsNull() {
		drugOrder.setFrequency(null);
		Timing.TimingRepeatComponent result = requestTimingComponentTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getPeriod(), nullValue());
		assertThat(result.getPeriodUnit(), nullValue());
	}
	
	@Test
	public void toFhirResource_shouldNotSetPeriodAndItsUnitsIfFrequencyPerDayIsNull() {
		drugOrder.setFrequency(new OrderFrequency());
		Timing.TimingRepeatComponent result = requestTimingComponentTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getPeriod(), nullValue());
		assertThat(result.getPeriodUnit(), nullValue());
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfDrugOrderIsNull() {
		assertThat(requestTimingComponentTranslator.toFhirResource(null), nullValue());
	}
	
	@Test
	public void toFhirResource_shouldTranslateConceptToUnitOfTimeSeconds() {
		concept.setUuid(SECONDS_UUID);
		drugOrder.setConcept(concept);
		
		when(durationUnitTranslator.toFhirResource(concept)).thenReturn(Timing.UnitsOfTime.S);
		Timing.TimingRepeatComponent result = requestTimingComponentTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getDurationUnit(), equalTo(Timing.UnitsOfTime.S));
	}
	
	@Test
	public void toFhirResource_shouldTranslateConceptToUnitOfTimeMinutes() {
		concept.setUuid(MINUTES_UUID);
		drugOrder.setConcept(concept);
		
		when(durationUnitTranslator.toFhirResource(concept)).thenReturn(Timing.UnitsOfTime.MIN);
		Timing.TimingRepeatComponent result = requestTimingComponentTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getDurationUnit(), equalTo(Timing.UnitsOfTime.MIN));
	}
	
	@Test
	public void toFhirResource_shouldTranslateConceptToUnitOfTimeHours() {
		concept.setUuid(HOUR_UUID);
		drugOrder.setConcept(concept);
		
		when(durationUnitTranslator.toFhirResource(concept)).thenReturn(Timing.UnitsOfTime.H);
		Timing.TimingRepeatComponent result = requestTimingComponentTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getDurationUnit(), equalTo(Timing.UnitsOfTime.H));
	}
	
	@Test
	public void toFhirResource_shouldTranslateConceptToUnitOfTimeDays() {
		concept.setUuid(DAYS_UUID);
		drugOrder.setConcept(concept);
		
		when(durationUnitTranslator.toFhirResource(concept)).thenReturn(Timing.UnitsOfTime.D);
		Timing.TimingRepeatComponent result = requestTimingComponentTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getDurationUnit(), equalTo(Timing.UnitsOfTime.D));
	}
	
	@Test
	public void toFhirResource_shouldTranslateConceptToUnitOfTimeWeeks() {
		concept.setUuid(WEEKS_UUID);
		drugOrder.setConcept(concept);
		
		when(durationUnitTranslator.toFhirResource(concept)).thenReturn(Timing.UnitsOfTime.WK);
		Timing.TimingRepeatComponent result = requestTimingComponentTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getDurationUnit(), equalTo(Timing.UnitsOfTime.WK));
	}
	
	@Test
	public void toFhirResource_shouldTranslateConceptToUnitOfTimeMonths() {
		concept.setUuid(MONTHS_UUID);
		drugOrder.setConcept(concept);
		
		when(durationUnitTranslator.toFhirResource(concept)).thenReturn(Timing.UnitsOfTime.MO);
		Timing.TimingRepeatComponent result = requestTimingComponentTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getDurationUnit(), equalTo(Timing.UnitsOfTime.MO));
	}
	
	@Test
	public void toFhirResource_shouldTranslateConceptToUnitOfTimeYears() {
		concept.setUuid(YEARS_UUID);
		drugOrder.setConcept(concept);
		
		when(durationUnitTranslator.toFhirResource(concept)).thenReturn(Timing.UnitsOfTime.A);
		Timing.TimingRepeatComponent result = requestTimingComponentTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getDurationUnit(), equalTo(Timing.UnitsOfTime.A));
	}
	
	@Test
	public void toFhirResource_shouldTranslateConceptToUnitOfTimeSecondNull() {
		concept.setUuid(WRONG_UUID);
		drugOrder.setConcept(concept);
		
		when(durationUnitTranslator.toFhirResource(concept)).thenReturn(Timing.UnitsOfTime.NULL);
		Timing.TimingRepeatComponent result = requestTimingComponentTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getDurationUnit(), equalTo(Timing.UnitsOfTime.NULL));
	}
}
