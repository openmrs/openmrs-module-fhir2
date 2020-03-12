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

import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.DrugOrder;
import org.openmrs.Order;

@RunWith(MockitoJUnitRunner.class)
public class MedicationRequestPriorityTranslatorImplTest {
	
	private MedicationRequestPriorityTranslatorImpl medicationRequestPriorityTranslator;
	
	@Before
	public void setup() {
		medicationRequestPriorityTranslator = new MedicationRequestPriorityTranslatorImpl();
	}
	
	@Test
	public void shouldTranslateToFhirRoutinePriority() {
		MedicationRequest.MedicationRequestPriority priority = medicationRequestPriorityTranslator
		        .toFhirResource(Order.Urgency.ROUTINE);
		assertThat(priority, notNullValue());
		assertThat(priority, equalTo(MedicationRequest.MedicationRequestPriority.ROUTINE));
	}
	
	@Test
	public void shouldTranslateToFhirStatPriority() {
		MedicationRequest.MedicationRequestPriority priority = medicationRequestPriorityTranslator
		        .toFhirResource(Order.Urgency.STAT);
		assertThat(priority, notNullValue());
		assertThat(priority, equalTo(MedicationRequest.MedicationRequestPriority.STAT));
	}
	
	@Test
	public void shouldTranslateOnScheduledDateToFhirRoutinePriority() {
		MedicationRequest.MedicationRequestPriority priority = medicationRequestPriorityTranslator
		        .toFhirResource(Order.Urgency.ON_SCHEDULED_DATE);
		assertThat(priority, notNullValue());
		assertThat(priority, equalTo(MedicationRequest.MedicationRequestPriority.ROUTINE));
	}
	
	@Test
	public void shouldTranslateToRoutineUrgency() {
		DrugOrder.Urgency urgency = medicationRequestPriorityTranslator
		        .toOpenmrsType(MedicationRequest.MedicationRequestPriority.ROUTINE);
		assertThat(urgency, notNullValue());
		assertThat(urgency, equalTo(DrugOrder.Urgency.ROUTINE));
	}
	
	@Test
	public void shouldTranslateToOnStatUrgency() {
		DrugOrder.Urgency urgency = medicationRequestPriorityTranslator
		        .toOpenmrsType(MedicationRequest.MedicationRequestPriority.STAT);
		assertThat(urgency, notNullValue());
		assertThat(urgency, equalTo(DrugOrder.Urgency.STAT));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateAsapToNull() {
		DrugOrder.Urgency urgency = medicationRequestPriorityTranslator
		        .toOpenmrsType(MedicationRequest.MedicationRequestPriority.ASAP);
		assertThat(urgency, nullValue());
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateToNull() {
		DrugOrder.Urgency urgency = medicationRequestPriorityTranslator
		        .toOpenmrsType(MedicationRequest.MedicationRequestPriority.NULL);
		assertThat(urgency, nullValue());
	}
	
	@Test(expected = NullPointerException.class)
	public void toFhirResource_shouldThrowNullPointerException() {
		MedicationRequest.MedicationRequestPriority priority = medicationRequestPriorityTranslator.toFhirResource(null);
		assertThat(priority, nullValue());
	}
	
}
