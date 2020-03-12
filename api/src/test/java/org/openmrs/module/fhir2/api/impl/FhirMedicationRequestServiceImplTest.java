/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.api.dao.FhirMedicationRequestDao;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirMedicationRequestServiceImplTest {
	
	private static final String MEDICATION_REQUEST_UUID = "d102c80f-1yz9-4da3-0099-8902ce886891";
	
	private static final String BAD_MEDICATION_REQUEST_UUID = "d102c80f-1yz9-4da3-0099-8902ce886891";
	
	@Mock
	private MedicationRequestTranslator medicationRequestTranslator;
	
	@Mock
	private FhirMedicationRequestDao dao;
	
	private FhirMedicationRequestServiceImpl medicationRequestService;
	
	private MedicationRequest medicationRequest;
	
	private DrugOrder drugOrder;
	
	@Before
	public void setup() {
		medicationRequestService = new FhirMedicationRequestServiceImpl();
		medicationRequestService.setDao(dao);
		medicationRequestService.setMedicationRequestTranslator(medicationRequestTranslator);
		
		medicationRequest = new MedicationRequest();
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		
		drugOrder = new DrugOrder();
		drugOrder.setUuid(MEDICATION_REQUEST_UUID);
	}
	
	@Test
	public void shouldGetMedicationRequestByUuid() {
		when(dao.getMedicationRequestByUuid(MEDICATION_REQUEST_UUID)).thenReturn(drugOrder);
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		
		MedicationRequest result = medicationRequestService.getMedicationRequestByUuid(MEDICATION_REQUEST_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void shouldReturnNullForBadMedicationRequestUuid() {
		MedicationRequest result = medicationRequestService.getMedicationRequestByUuid(BAD_MEDICATION_REQUEST_UUID);
		assertThat(result, nullValue());
	}
	
}
