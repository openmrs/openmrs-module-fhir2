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

import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.Reference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.module.fhir2.api.dao.FhirGenericServiceRequestDao;
import org.openmrs.module.fhir2.api.dao.FhirMedicationRequestDao;
import org.openmrs.module.fhir2.api.translators.ServiceRequestReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRequestReferenceTranslatorImplTest {
	
	private static final String MEDICATION_REQUEST_TYPE = "MedicationRequest";
	
	private static final String SERVICE_REQUEST_TYPE = "ServiceRequest";
	
	@Mock
	private FhirMedicationRequestDao medicationRequestDao;
	
	@Mock
	private FhirGenericServiceRequestDao fhirOrderServiceRequestDao;
	
	ServiceRequestReferenceTranslator translator;
	
	@Before
	public void setUp() {
		ServiceRequestReferenceTranslatorImpl orderReferenceTranslator = new ServiceRequestReferenceTranslatorImpl();
		orderReferenceTranslator.setMedicationRequestDao(medicationRequestDao);
		orderReferenceTranslator.setServiceRequestDao(fhirOrderServiceRequestDao);
		translator = orderReferenceTranslator;
	}
	
	@Test
	public void shouldGetReferenceToGenericOrder() {
		Order order = new Order();
		Reference reference = translator.toFhirResource(order);
		Assert.assertEquals((SERVICE_REQUEST_TYPE + "/").concat(order.getUuid()), reference.getReference());
	}
	
	@Test
	public void shouldGetReferenceToMedicationRequest() {
		Order order = new DrugOrder();
		Reference reference = translator.toFhirResource(order);
		Assert.assertEquals((MEDICATION_REQUEST_TYPE).concat("/").concat(order.getUuid()), reference.getReference());
	}
	
	@Test
	public void shouldReturnMedicationOrderReference() {
		Reference reference = new Reference().setReference(MEDICATION_REQUEST_TYPE + "/123");
		when(medicationRequestDao.get("123")).thenReturn(new DrugOrder());
		Order order = translator.toOpenmrsType(reference);
		Assert.assertTrue(order instanceof DrugOrder);
	}
	
	@Test
	public void shouldReturnGenericOrderReference() {
		Reference reference = new Reference().setReference(SERVICE_REQUEST_TYPE + "/123");
		when(fhirOrderServiceRequestDao.get("123")).thenReturn(new Order());
		Order order = translator.toOpenmrsType(reference);
		Assert.assertTrue(order instanceof Order);
	}
}
