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

import java.util.Collections;
import java.util.Date;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Timing;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.OrderFrequency;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.DurationUnitTranslator;

@RunWith(MockitoJUnitRunner.class)
public class MedicationRequestTimingTranslatorImplTest {
	
	private MedicationRequestTimingTranslatorImpl timingTranslator;
	
	private MedicationRequestTimingRepeatComponentTranslatorImpl timingRepeatComponentTranslator;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private OrderService orderService;
	
	@Mock
	private DurationUnitTranslator durationUnitTranslator;
	
	private DrugOrder drugOrder;
	
	private Concept daysConcept;
	
	private Concept oncePerDayConcept;
	
	private CodeableConcept oncePerDayFhirConcept;
	
	private OrderFrequency oncePerDayFrequency;
	
	@Before
	public void setup() {
		timingTranslator = new MedicationRequestTimingTranslatorImpl();
		timingRepeatComponentTranslator = new MedicationRequestTimingRepeatComponentTranslatorImpl();
		timingRepeatComponentTranslator.setDurationUnitTranslator(durationUnitTranslator);
		timingTranslator.setTimingRepeatComponentTranslator(timingRepeatComponentTranslator);
		timingTranslator.setConceptTranslator(conceptTranslator);
		timingTranslator.setOrderService(orderService);
		
		drugOrder = new DrugOrder();
		
		daysConcept = new Concept();
		when(durationUnitTranslator.toOpenmrsType(Timing.UnitsOfTime.D)).thenReturn(daysConcept);
		when(durationUnitTranslator.toFhirResource(daysConcept)).thenReturn(Timing.UnitsOfTime.D);
		
		oncePerDayConcept = new Concept();
		oncePerDayFhirConcept = new CodeableConcept();
		oncePerDayFhirConcept.addCoding(new Coding("system", "code", "display"));
		oncePerDayFrequency = new OrderFrequency();
		oncePerDayFrequency.setConcept(oncePerDayConcept);
		when(conceptTranslator.toFhirResource(oncePerDayConcept)).thenReturn(oncePerDayFhirConcept);
		when(conceptTranslator.toOpenmrsType(oncePerDayFhirConcept)).thenReturn(oncePerDayConcept);
		when(orderService.getOrderFrequencyByConcept(oncePerDayConcept)).thenReturn(oncePerDayFrequency);
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
	public void toOpenmrsType_shouldSetScheduledDate() {
		Date eventDate = new Date();
		Timing timing = new Timing();
		timing.setEvent(Collections.singletonList(new DateTimeType(eventDate)));
		assertThat(drugOrder.getScheduledDate(), nullValue());
		drugOrder = timingTranslator.toOpenmrsType(drugOrder, timing);
		assertThat(drugOrder.getScheduledDate(), equalTo(eventDate));
	}
	
	@Test
	public void toFhirResource_shouldSetDuration() {
		drugOrder.setDuration(10);
		drugOrder.setDurationUnits(daysConcept);
		Timing result = timingTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getRepeat(), notNullValue());
		assertThat(result.getRepeat().getDuration().intValue(), equalTo(10));
		assertThat(result.getRepeat().getDurationUnit(), equalTo(Timing.UnitsOfTime.D));
	}
	
	@Test
	public void toOpenmrsType_shouldSetDuration() {
		Timing timing = new Timing();
		Timing.TimingRepeatComponent timingRepeatComponent = new Timing.TimingRepeatComponent();
		timingRepeatComponent.setDuration(10);
		timingRepeatComponent.setDurationUnit(Timing.UnitsOfTime.D);
		timing.setRepeat(timingRepeatComponent);
		drugOrder = timingTranslator.toOpenmrsType(drugOrder, timing);
		assertThat(drugOrder.getDuration(), equalTo(10));
		assertThat(drugOrder.getDurationUnits(), equalTo(daysConcept));
	}
	
	@Test
	public void toFhirResource_shouldSetCodeValue() {
		drugOrder.setFrequency(oncePerDayFrequency);
		Timing result = timingTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getCode(), notNullValue());
		assertThat(result.getCode(), equalTo(oncePerDayFhirConcept));
	}
	
	@Test
	public void toOpenmrsType_shouldSetOrderFrequency() {
		Timing timing = new Timing();
		timing.setCode(oncePerDayFhirConcept);
		assertThat(drugOrder.getFrequency(), nullValue());
		drugOrder = timingTranslator.toOpenmrsType(drugOrder, timing);
		assertThat(drugOrder.getFrequency(), equalTo(oncePerDayFrequency));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfDrugOrderIsNull() {
		assertThat(timingTranslator.toFhirResource(null), nullValue());
	}
	
}
