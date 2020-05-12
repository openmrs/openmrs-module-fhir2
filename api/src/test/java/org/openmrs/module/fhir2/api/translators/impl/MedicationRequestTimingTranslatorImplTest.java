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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.Timing;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTimingComponentTranslator;

@RunWith(MockitoJUnitRunner.class)
public class MedicationRequestTimingTranslatorImplTest {
	
	private static final int PERIOD = 1;
	
	@Mock
	private MedicationRequestTimingComponentTranslator timingComponentTranslator;
	
	private MedicationRequestTimingTranslatorImpl timingTranslator;
	
	private DrugOrder drugOrder;
	
	@Before
	public void setup() {
		timingTranslator = new MedicationRequestTimingTranslatorImpl();
		timingTranslator.setTimingComponentTranslator(timingComponentTranslator);
		
		drugOrder = new DrugOrder();
	}
	
	@Test
	public void toFhirResource_shouldAddEvent() {
		drugOrder.setScheduledDate(new Date());
		Timing result = timingTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getEvent(), not(empty()));
		assertThat(result.getEvent().get(0).getValue(), notNullValue());
		assertThat(result.getEvent().get(0).getValue(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toFhirResource_shouldSetRepeatValue() {
		Timing.TimingRepeatComponent repeatComponent = new Timing.TimingRepeatComponent();
		repeatComponent.setPeriod(PERIOD);
		repeatComponent.setPeriodUnit(Timing.UnitsOfTime.D);
		when(timingComponentTranslator.toFhirResource(drugOrder)).thenReturn(repeatComponent);
		
		Timing result = timingTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getRepeat(), notNullValue());
		assertThat(result.getRepeat().getPeriod(), equalTo(new BigDecimal(PERIOD)));
		assertThat(result.getRepeat().getPeriodUnit(), equalTo(Timing.UnitsOfTime.D));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfDrugOrderIsNull() {
		assertThat(timingTranslator.toFhirResource(null), nullValue());
	}
	
}
