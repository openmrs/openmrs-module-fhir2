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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.TestOrder;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRequestTranslatorImplTest {
	
	private static final String SERVICE_REQUEST_UUID = "4e4851c3-c265-400e-acc9-1f1b0ac7f9c4";
	
	private static final TestOrder.Action ORDER_ACTION = TestOrder.Action.NEW;
	
	private ServiceRequestTranslatorImpl translator;
	
	@Before
	public void setup() {
		translator = new ServiceRequestTranslatorImpl();
	}
	
	@Test
	public void shouldTranslateOpenmrsTestOrderToFhirServiceRequest() {
		TestOrder order = new TestOrder();
		ServiceRequest result = translator.toFhirResource(order);
		
		assertThat(result, notNullValue());
	}
	
	@Test
	public void shouldTranslateNewOrderToActiveServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setAction(ORDER_ACTION);
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.ACTIVE));
		assertThat(result.getIntent(), equalTo(ServiceRequest.ServiceRequestIntent.ORDER));
	}
	
	@Ignore
	@Test
	public void shouldTranslateRevisedOrderToActiveServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setAction(TestOrder.Action.REVISE);
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.ACTIVE));
		assertThat(result.getIntent(), equalTo(ServiceRequest.ServiceRequestIntent.ORDER));
	}
	
	@Test
	public void shouldTranslateDiscontinuedOrderToRevokedServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setAction(TestOrder.Action.DISCONTINUE);
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.REVOKED));
		assertThat(result.getIntent(), equalTo(ServiceRequest.ServiceRequestIntent.ORDER));
	}
	
	@Test
	public void shouldReturnNullForCreatingNewOrder() {
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);
		
		TestOrder result = translator.toOpenmrsType(serviceRequest);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void shouldReturnNullForUpdatingExistingOrder() {
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);
		TestOrder existingOrder = new TestOrder();
		existingOrder.setUuid(SERVICE_REQUEST_UUID);
		
		TestOrder result = translator.toOpenmrsType(existingOrder, serviceRequest);
		
		assertThat(result, nullValue());
	}
}
