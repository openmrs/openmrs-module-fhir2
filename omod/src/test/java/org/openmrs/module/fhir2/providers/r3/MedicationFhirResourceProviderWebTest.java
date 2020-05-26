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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirMedicationService;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class MedicationFhirResourceProviderWebTest extends BaseFhirR3ResourceProviderWebTest<MedicationFhirResourceProvider, Medication> {
	
	private static final String MEDICATION_UUID = "a2749656-1bc0-4d22-9c11-1d60026b672b";
	
	private static final String WRONG_MEDICATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String CODE = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String JSON_CREATE_MEDICATION_PATH = "org/openmrs/module/fhir2/providers/MedicationResourceWebTest_create.json";
	
	private static final String JSON_UPDATE_MEDICATION_PATH = "org/openmrs/module/fhir2/providers/MedicationResourceWebTest_update.json";
	
	private static final String JSON_UPDATE_WITHOUTID_MEDICATION_PATH = "org/openmrs/module/fhir2/providers/MedicationResourceWebTest_UdateWithoutId.json";
	
	private static final String JSON_UPDATE_WITHWRONGID_MEDICATION_PATH = "org/openmrs/module/fhir2/providers/MedicationResourceWebTest_UdateWithWrongId.json";
	
	@Mock
	private FhirMedicationService fhirMedicationService;
	
	@Getter
	private MedicationFhirResourceProvider resourceProvider;
	
	@Captor
	private ArgumentCaptor<TokenOrListParam> tokenOrListParamArgumentCaptor;
	
	@Captor
	private ArgumentCaptor<TokenAndListParam> tokenAndListParamArgumentCaptor;
	
	private org.hl7.fhir.r4.model.Medication medication;
	
	@Before
	@Override
	public void setup() throws ServletException {
		resourceProvider = new MedicationFhirResourceProvider();
		resourceProvider.setMedicationService(fhirMedicationService);
		medication = new org.hl7.fhir.r4.model.Medication();
		medication.setId(MEDICATION_UUID);
		super.setup();
	}
	
	@Test
	public void getMedicationByUuid_shouldReturnMedication() throws Exception {
		org.hl7.fhir.r4.model.Medication medication = new org.hl7.fhir.r4.model.Medication();
		medication.setId(MEDICATION_UUID);
		when(fhirMedicationService.get(MEDICATION_UUID)).thenReturn(medication);
		
		MockHttpServletResponse response = get("/Medication/" + MEDICATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void searchForMedications_shouldSearchForMedicationsByCode() throws Exception {
		verifyUri("/Medication?code=5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		
		verify(fhirMedicationService).searchForMedications(tokenAndListParamArgumentCaptor.capture(), isNull(), isNull(),
		    isNull());
		
		List<TokenOrListParam> listParams = tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens();
		TokenParam tokenParam = listParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenParam.getValue(), equalTo(CODE));
	}
	
	@Test
	public void searchForMedications_shouldSearchForMedicationsByDosageForm() throws Exception {
		verifyUri("/Medication?form=5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		
		verify(fhirMedicationService).searchForMedications(isNull(), tokenAndListParamArgumentCaptor.capture(), isNull(),
		    isNull());
		
		List<TokenOrListParam> listParams = tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens();
		TokenParam tokenParam = listParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenParam.getValue(), equalTo(CODE));
	}
	
	@Test
	public void searchForMedications_shouldSearchForMedicationsByStatus() throws Exception {
		verifyUri("/Medication?status=active");
		
		verify(fhirMedicationService).searchForMedications(isNull(), isNull(), isNull(),
		    tokenAndListParamArgumentCaptor.capture());
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo("active"));
	}
	
	@Test
	public void getMedicationByUuid_shouldReturn404() throws Exception {
		when(fhirMedicationService.get(WRONG_MEDICATION_UUID)).thenReturn(null);
		
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
	
	@Test
	public void shouldCreateNewMedication() throws Exception {
		org.hl7.fhir.r4.model.Medication medication = new org.hl7.fhir.r4.model.Medication();
		medication.setId(MEDICATION_UUID);
		String medicationJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_MEDICATION_PATH)) {
			assert is != null;
			medicationJson = IOUtils.toString(is);
		}
		
		when(fhirMedicationService.create(any(org.hl7.fhir.r4.model.Medication.class))).thenReturn(medication);
		
		MockHttpServletResponse response = post("/Medication").jsonContent(medicationJson).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isCreated());
		assertThat(response.getStatus(), is(201));
	}
	
	@Test
	public void shouldUpdateMedication() throws Exception {
		org.hl7.fhir.r4.model.Medication medication = new org.hl7.fhir.r4.model.Medication();
		medication.setId(MEDICATION_UUID);
		medication.setStatus(org.hl7.fhir.r4.model.Medication.MedicationStatus.INACTIVE);
		String medicationJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_MEDICATION_PATH)) {
			assert is != null;
			medicationJson = IOUtils.toString(is);
		}
		
		when(fhirMedicationService.update(any(String.class), any(org.hl7.fhir.r4.model.Medication.class)))
		        .thenReturn(medication);
		
		MockHttpServletResponse response = put("/Medication/" + MEDICATION_UUID).jsonContent(medicationJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
	}
	
	@Test
	public void updateMedicationShouldErrorForIdMismatch() throws Exception {
		String medicationJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_MEDICATION_PATH)) {
			medicationJson = IOUtils.toString(is);
		}
		
		MockHttpServletResponse response = put("/Medication/" + WRONG_MEDICATION_UUID).jsonContent(medicationJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    containsStringIgnoringCase("body must contain an ID element which matches the request URL"));
	}
	
	@Test
	public void updateMedicationShouldErrorForNoId() throws Exception {
		String medicationJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_WITHOUTID_MEDICATION_PATH)) {
			medicationJson = IOUtils.toString(is);
		}
		
		MockHttpServletResponse response = put("/Medication/" + MEDICATION_UUID).jsonContent(medicationJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	public void updateMedicationShouldErrorForNonexistentMedication() throws Exception {
		String medicationJson;
		try (InputStream is = this.getClass().getClassLoader()
		        .getResourceAsStream(JSON_UPDATE_WITHWRONGID_MEDICATION_PATH)) {
			medicationJson = IOUtils.toString(is);
		}
		
		when(fhirMedicationService.update(eq(WRONG_MEDICATION_UUID), any(org.hl7.fhir.r4.model.Medication.class)))
		        .thenThrow(new MethodNotAllowedException("Medication " + WRONG_MEDICATION_UUID + " does not exist"));
		
		MockHttpServletResponse response = put("/Medication/" + WRONG_MEDICATION_UUID).jsonContent(medicationJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isMethodNotAllowed());
	}
	
	@Test
	public void shouldDeleteMedication() throws Exception {
		OperationOutcome retVal = new OperationOutcome();
		retVal.setId(MEDICATION_UUID);
		retVal.getText().setDivAsString("Deleted successfully");
		
		org.hl7.fhir.r4.model.Medication medication = new org.hl7.fhir.r4.model.Medication();
		medication.setId(MEDICATION_UUID);
		medication.setStatus(org.hl7.fhir.r4.model.Medication.MedicationStatus.INACTIVE);
		
		when(fhirMedicationService.delete(any(String.class))).thenReturn(medication);
		
		MockHttpServletResponse response = delete("/Medication/" + MEDICATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
}
