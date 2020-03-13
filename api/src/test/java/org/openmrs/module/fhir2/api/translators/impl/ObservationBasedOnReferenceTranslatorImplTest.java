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

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.TestOrder;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirMedicationRequestDao;
import org.openmrs.module.fhir2.api.dao.FhirServiceRequestDao;

@RunWith(MockitoJUnitRunner.class)
public class ObservationBasedOnReferenceTranslatorImplTest {
	
	private static final String ORDER_UUID = "12344-edcba-12345";
	
	public static final String DRUG_ORDER_TYPE_UUID = "131168f4-15f5-102d-96e4-000c29c2a5d7";
	
	public static final String TEST_ORDER_TYPE_UUID = "52a447d3-a64a-11e3-9aeb-50e549534c5e";
	
	@Mock
	private FhirServiceRequestDao<TestOrder> orderDao;
	
	@Mock
	private FhirMedicationRequestDao medicationRequestDao;
	
	private ObservationBasedOnReferenceTranslatorImpl translator;
	
	@Before
	public void setup() {
		translator = new ObservationBasedOnReferenceTranslatorImpl();
		translator.setServiceRequestDao(orderDao);
		translator.setMedicationRequestDao(medicationRequestDao);
	}
	
	@Test
	public void toFhirResource_shouldReturnNullWhenCalledWithNullObject() {
		Reference reference = translator.toFhirResource(null);
		assertThat(reference, nullValue());
	}
	
	@Test
	public void toFhirResource_shouldConvertTestOrderToReference() {
		Order order = new Order();
		order.setUuid(ORDER_UUID);
		OrderType orderType = new OrderType();
		orderType.setUuid(TEST_ORDER_TYPE_UUID);
		order.setOrderType(orderType);
		
		Reference result = translator.toFhirResource(order);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(FhirConstants.SERVICE_REQUEST));
	}
	
	@Test
	public void toFhirResource_shouldConvertDrugOrderToReference() {
		Order order = new Order();
		order.setUuid(ORDER_UUID);
		OrderType orderType = new OrderType();
		orderType.setUuid(DRUG_ORDER_TYPE_UUID);
		order.setOrderType(orderType);
		
		Reference result = translator.toFhirResource(order);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(FhirConstants.MEDICATION));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfOrderTypeIsNull() {
		Order order = new Order();
		order.setUuid(ORDER_UUID);
		
		Reference result = translator.toFhirResource(order);
		
		assertThat(result, nullValue());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void toFhirType_shouldThrowIllegalArgumentExceptionException() {
		Order order = new DrugOrder();
		order.setUuid(ORDER_UUID);
		
		OrderType orderType = new OrderType();
		orderType.setUuid("");
		order.setOrderType(orderType);
		
		Reference result = translator.toFhirResource(order);
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldConvertServiceRequestReferenceToOrder() {
		Reference orderReference = new Reference().setReference(FhirConstants.SERVICE_REQUEST + "/" + ORDER_UUID)
		        .setType(FhirConstants.SERVICE_REQUEST).setIdentifier(new Identifier().setValue(ORDER_UUID));
		
		TestOrder order = new TestOrder();
		order.setUuid(ORDER_UUID);
		
		when(orderDao.getServiceRequestByUuid(ORDER_UUID)).thenReturn(order);
		
		Order result = translator.toOpenmrsType(orderReference);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(ORDER_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldConvertMedicationReferenceToOrder() {
		Reference orderReference = new Reference().setReference(FhirConstants.MEDICATION + "/" + ORDER_UUID)
		        .setType(FhirConstants.MEDICATION).setIdentifier(new Identifier().setValue(ORDER_UUID));
		
		DrugOrder order = new DrugOrder();
		order.setUuid(ORDER_UUID);
		
		when(medicationRequestDao.getMedicationRequestByUuid(ORDER_UUID)).thenReturn(order);
		
		Order result = translator.toOpenmrsType(orderReference);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(ORDER_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfReferenceIsNull() {
		Order result = translator.toOpenmrsType(null);
		
		assertThat(result, nullValue());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void toOpenmrsType_shouldErrorIfReferenceNotOrderType() {
		Reference otherReference = new Reference().setType(FhirConstants.PATIENT)
		        .setIdentifier(new Identifier().setValue(ORDER_UUID));
		
		translator.toOpenmrsType(otherReference);
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfReferenceMissingUuid() {
		Reference orderReference = new Reference().setType(FhirConstants.SERVICE_REQUEST);
		
		Order result = translator.toOpenmrsType(orderReference);
		
		assertThat(result, nullValue());
	}
	
}
