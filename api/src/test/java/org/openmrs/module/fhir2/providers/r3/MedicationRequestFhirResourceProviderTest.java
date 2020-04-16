/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirMedicationRequestService;

@RunWith(MockitoJUnitRunner.class)
public class MedicationRequestFhirResourceProviderTest {
	
	private static final String MEDICATION_REQUEST_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String WRONG_MEDICATION_REQUEST_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	@Mock
	private FhirMedicationRequestService fhirMedicationRequestService;
	
	private MedicationRequestFhirResourceProvider resourceProvider;
	
	private org.hl7.fhir.r4.model.MedicationRequest medicationRequest;
	
	@Before
	public void setup() {
		resourceProvider = new MedicationRequestFhirResourceProvider();
		resourceProvider.setMedicationRequestService(fhirMedicationRequestService);
		
		medicationRequest = new org.hl7.fhir.r4.model.MedicationRequest();
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(MedicationRequest.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(MedicationRequest.class.getName()));
	}
	
	@Test
	public void getMedicationRequestByUuid_shouldReturnMatchingMedicationRequest() {
		when(fhirMedicationRequestService.get(MEDICATION_REQUEST_UUID)).thenReturn(medicationRequest);
		
		IdType id = new IdType();
		id.setValue(MEDICATION_REQUEST_UUID);
		MedicationRequest medicationRequest = resourceProvider.getMedicationRequestById(id);
		assertThat(medicationRequest, notNullValue());
		assertThat(medicationRequest.getId(), notNullValue());
		assertThat(medicationRequest.getId(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getMedicationRequestByUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_MEDICATION_REQUEST_UUID);
		MedicationRequest medicationRequest = resourceProvider.getMedicationRequestById(id);
		assertThat(medicationRequest, nullValue());
	}
}
