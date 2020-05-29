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

import java.math.BigDecimal;

import org.hl7.fhir.r4.model.Timing;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.DrugOrder;
import org.openmrs.OrderFrequency;

@RunWith(MockitoJUnitRunner.class)
public class MedicationRequestTimingComponentTranslatorImplTest {
	
	private static final int DURATION = 2;
	
	private static final double FREQUENCY_PER_DAY = 3.0;
	
	private MedicationRequestTimingComponentTranslatorImpl requestTimingComponentTranslator;
	
	private DrugOrder drugOrder;
	
	@Before
	public void setup() {
		requestTimingComponentTranslator = new MedicationRequestTimingComponentTranslatorImpl();
		
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
	
}
