/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirMedicationRequestService;
import org.openmrs.module.fhir2.web.servlet.BaseFhirResourceProviderTest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class MedicationRequestFhirResourceProviderWebTest extends BaseFhirResourceProviderTest<MedicationRequestFhirResourceProvider, MedicationRequest> {
	
	private static final String MEDICATION_REQUEST_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String WRONG_MEDICATION_REQUEST_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	@Mock
	private FhirMedicationRequestService fhirMedicationRequestService;
	
	@Getter(AccessLevel.PUBLIC)
	private MedicationRequestFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		resourceProvider = new MedicationRequestFhirResourceProvider();
		resourceProvider.setFhirMedicationRequestService(fhirMedicationRequestService);
		super.setup();
	}
	
	@Test
	public void getMedicationRequestByUuid_shouldReturnMedication() throws Exception {
		MedicationRequest medicationRequest = new MedicationRequest();
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		when(fhirMedicationRequestService.getMedicationRequestByUuid(MEDICATION_REQUEST_UUID)).thenReturn(medicationRequest);
		
		MockHttpServletResponse response = get("/MedicationRequest/" + MEDICATION_REQUEST_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void getMedicationRequestByUuid_shouldReturn404() throws Exception {
		when(fhirMedicationRequestService.getMedicationRequestByUuid(WRONG_MEDICATION_REQUEST_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/MedicationRequest/" + WRONG_MEDICATION_REQUEST_UUID)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
}
