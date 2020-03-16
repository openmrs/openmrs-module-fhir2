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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import ca.uhn.fhir.rest.param.TokenOrListParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Medication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirMedicationService;
import org.openmrs.module.fhir2.web.servlet.BaseFhirResourceProviderTest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class MedicationFhirResourceProviderWebTest extends BaseFhirResourceProviderTest<MedicationFhirResourceProvider, Medication> {
	
	private static final String MEDICATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String WRONG_MEDICATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String CODE = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Mock
	private FhirMedicationService fhirMedicationService;
	
	@Getter(AccessLevel.PUBLIC)
	private MedicationFhirResourceProvider resourceProvider;
	
	@Captor
	private ArgumentCaptor<TokenOrListParam> tokenOrListParamArgumentCaptor;
	
	private Medication medication;
	
	@Before
	@Override
	public void setup() throws Exception {
		resourceProvider = new MedicationFhirResourceProvider();
		resourceProvider.setFhirMedicationService(fhirMedicationService);
		medication = new Medication();
		medication.setId(MEDICATION_UUID);
		super.setup();
	}
	
	@Override
	public MedicationFhirResourceProvider getResourceProvider() {
		return resourceProvider;
	}
	
	@Test
	public void getMedicationByUuid_shouldReturnMedication() throws Exception {
		Medication medication = new Medication();
		medication.setId(MEDICATION_UUID);
		when(fhirMedicationService.getMedicationByUuid(MEDICATION_UUID)).thenReturn(medication);
		
		MockHttpServletResponse response = get("/Medication/" + MEDICATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void searchForMedications_shouldSearchForMedicationsByCode() throws Exception {
		verifyUri("/Medication?code=5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		
		verify(fhirMedicationService).searchForMedications(tokenOrListParamArgumentCaptor.capture(), isNull(), isNull(),
		    isNull());
		assertThat(tokenOrListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenOrListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValue(), equalTo(CODE));
	}
	
	@Test
	public void searchForMedications_shouldSearchForMedicationsByDosageForm() throws Exception {
		verifyUri("/Medication?form=5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		
		verify(fhirMedicationService).searchForMedications(isNull(), tokenOrListParamArgumentCaptor.capture(), isNull(),
		    isNull());
		assertThat(tokenOrListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenOrListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValue(), equalTo(CODE));
	}
	
	@Test
	public void searchForMedications_shouldSearchForMedicationsByStatus() throws Exception {
		verifyUri("/Medication?status=active");
		
		verify(fhirMedicationService).searchForMedications(isNull(), isNull(), isNull(),
		    tokenOrListParamArgumentCaptor.capture());
		assertThat(tokenOrListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenOrListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValue(), equalTo("active"));
	}
	
	@Test
	public void getMedicationByUuid_shouldReturn404() throws Exception {
		when(fhirMedicationService.getMedicationByUuid(WRONG_MEDICATION_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/Medication/" + WRONG_MEDICATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	private void verifyUri(String uri) throws Exception {
		when(fhirMedicationService.searchForMedications(any(), any(), any(), any()))
		        .thenReturn(Collections.singletonList(medication));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
	}
}
