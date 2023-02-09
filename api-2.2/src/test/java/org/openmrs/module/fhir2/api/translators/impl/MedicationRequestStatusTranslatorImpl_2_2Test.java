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

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.DrugOrder;
import org.openmrs.Order;

@RunWith(MockitoJUnitRunner.class)
public class MedicationRequestStatusTranslatorImpl_2_2Test {
	
	private static final String DRUG_ORDER_UUID = "44fdc8ad-fe4d-499b-93a8-8a991c1d477e";
	
	private MedicationRequestStatusTranslatorImpl_2_2 statusTranslator;
	
	private DrugOrder drugOrder;
	
	@Before
	public void setup() {
		statusTranslator = new MedicationRequestStatusTranslatorImpl_2_2();
		
		drugOrder = new DrugOrder();
		drugOrder.setUuid(DRUG_ORDER_UUID);
		drugOrder.setDateActivated(new Date());
	}
	
	@Test
	public void toFhirResource_shouldTranslateToActiveStatus() {
		MedicationRequest.MedicationRequestStatus status = statusTranslator.toFhirResource(drugOrder);
		assertThat(status, notNullValue());
		assertThat(status, equalTo(MedicationRequest.MedicationRequestStatus.ACTIVE));
	}
	
	@Test
	public void toFhirResource_shouldTranslateExpiredOrderToStoppedStatus() throws ParseException {
		drugOrder.setAutoExpireDate(new SimpleDateFormat("YYYY-MM-DD").parse("2000-10-10"));
		
		MedicationRequest.MedicationRequestStatus status = statusTranslator.toFhirResource(drugOrder);
		assertThat(status, notNullValue());
		assertThat(status, equalTo(MedicationRequest.MedicationRequestStatus.STOPPED));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDiscontinueOrderToCancelledStatus()
	        throws NoSuchFieldException, ParseException, IllegalAccessException {
		Field dateStopped = Order.class.getDeclaredField("dateStopped");
		dateStopped.setAccessible(true);
		dateStopped.set(drugOrder, new SimpleDateFormat("YYYY-MM-DD").parse("2000-10-10"));
		
		MedicationRequest.MedicationRequestStatus status = statusTranslator.toFhirResource(drugOrder);
		assertThat(status, notNullValue());
		assertThat(status, equalTo(MedicationRequest.MedicationRequestStatus.CANCELLED));
	}
	
	@Test
	public void toFhirResource_shouldTranslateVoidedOrderToCancelled() {
		drugOrder.setVoided(true);
		MedicationRequest.MedicationRequestStatus status = statusTranslator.toFhirResource(drugOrder);
		assertThat(status, notNullValue());
		assertThat(status, equalTo(MedicationRequest.MedicationRequestStatus.CANCELLED));
	}
	
	@Test
	public void toFhirResource_shouldTranslatedOrderWithFulfillerStatusCompletedToCompleted() {
		drugOrder.setFulfillerStatus(Order.FulfillerStatus.COMPLETED);
		MedicationRequest.MedicationRequestStatus status = statusTranslator.toFhirResource(drugOrder);
		assertThat(status, notNullValue());
		assertThat(status, equalTo(MedicationRequest.MedicationRequestStatus.COMPLETED));
	}
	
}
