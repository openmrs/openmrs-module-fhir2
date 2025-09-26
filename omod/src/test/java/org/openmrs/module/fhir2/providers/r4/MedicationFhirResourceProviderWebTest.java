/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import javax.servlet.ServletException;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Getter;
import org.apache.commons.lang.time.DateUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Medication;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.FhirMedicationService;
import org.openmrs.module.fhir2.api.search.param.MedicationSearchParams;
import org.powermock.reflect.Whitebox;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class MedicationFhirResourceProviderWebTest extends BaseFhirR4ResourceProviderWebTest<MedicationFhirResourceProvider, Medication> {
	
	private static final String MEDICATION_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_MEDICATION_UUID = "8eaa7db6-5da5-49d2-a63b-93abf4feb7d0";
	
	private static final String CODE = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String JSON_CREATE_MEDICATION_PATH = "org/openmrs/module/fhir2/providers/MedicationWebTest_create.json";
	
	private static final String JSON_UPDATE_MEDICATION_PATH = "org/openmrs/module/fhir2/providers/MedicationWebTest_update.json";
	
	private static final String JSON_UPDATE_WITHOUT_ID_MEDICATION_PATH = "org/openmrs/module/fhir2/providers/MedicationWebTest_updateWithoutId.json";
	
	private static final String JSON_UPDATE_WITH_WRONG_ID_MEDICATION_PATH = "org/openmrs/module/fhir2/providers/MedicationWebTest_updateWithWrongId.json";
	
	private static final String LAST_UPDATED_DATE = "eq2020-09-03";
	
	@Mock
	private FhirMedicationService fhirMedicationService;
	
	@Mock
	private FhirGlobalPropertyService fhirGpService;
	
	@Getter
	private MedicationFhirResourceProvider resourceProvider;
	
	@Captor
	private ArgumentCaptor<MedicationSearchParams> medicationSearchParamsArgumentCaptor;
	
	private Medication medication;
	
	@Before
	@Override
	public void setup() throws ServletException {
		resourceProvider = new MedicationFhirResourceProvider();
		resourceProvider.setFhirMedicationService(fhirMedicationService);
		Whitebox.setInternalState(BaseUpsertFhirResourceProvider.class, "globalPropsService", fhirGpService);
		medication = new Medication();
		medication.setId(MEDICATION_UUID);
		super.setup();
	}
	
	@After
	public void tearDown() {
		Whitebox.setInternalState(BaseUpsertFhirResourceProvider.class, "globalPropsService", (Object) null);
	}
	
	@Test
	public void getMedicationByUuid_shouldReturnMedication() throws Exception {
		Medication medication = new Medication();
		medication.setId(MEDICATION_UUID);
		when(fhirMedicationService.get(MEDICATION_UUID)).thenReturn(medication);
		
		MockHttpServletResponse response = get("/Medication/" + MEDICATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void searchForMedications_shouldSearchForMedicationsByCode() throws Exception {
		verifyUri(String.format("/Medication?code=%s", CODE));
		
		verify(fhirMedicationService).searchForMedications(medicationSearchParamsArgumentCaptor.capture());
		
		List<TokenOrListParam> listParams = medicationSearchParamsArgumentCaptor.getValue().getCode()
		        .getValuesAsQueryTokens();
		TokenParam tokenParam = listParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(medicationSearchParamsArgumentCaptor.getValue().getCode(), notNullValue());
		assertThat(tokenParam.getValue(), equalTo(CODE));
	}
	
	@Test
	public void searchForMedications_shouldSearchForMedicationsByDosageForm() throws Exception {
		verifyUri(String.format("/Medication?form=%s", CODE));
		
		verify(fhirMedicationService).searchForMedications(medicationSearchParamsArgumentCaptor.capture());
		
		List<TokenOrListParam> listParams = medicationSearchParamsArgumentCaptor.getValue().getDosageForm()
		        .getValuesAsQueryTokens();
		TokenParam tokenParam = listParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(medicationSearchParamsArgumentCaptor.getValue().getDosageForm(), notNullValue());
		assertThat(tokenParam.getValue(), equalTo(CODE));
	}
	
	@Test
	public void searchForMedications_shouldSearchForMedicationsByIngredientCode() throws Exception {
		verifyUri(String.format("/Medication?ingredient-code=%s", CODE));
		
		verify(fhirMedicationService).searchForMedications(medicationSearchParamsArgumentCaptor.capture());
		
		List<TokenOrListParam> listParams = medicationSearchParamsArgumentCaptor.getValue().getIngredientCode()
		        .getValuesAsQueryTokens();
		TokenParam tokenParam = listParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(medicationSearchParamsArgumentCaptor.getValue().getIngredientCode(), notNullValue());
		assertThat(tokenParam.getValue(), equalTo(CODE));
	}
	
	@Test
	public void searchForMedications_shouldSearchForMedicationsByUUID() throws Exception {
		verifyUri(String.format("/Medication?_id=%s", MEDICATION_UUID));
		
		verify(fhirMedicationService).searchForMedications(medicationSearchParamsArgumentCaptor.capture());
		
		assertThat(medicationSearchParamsArgumentCaptor.getValue().getId(), notNullValue());
		assertThat(medicationSearchParamsArgumentCaptor.getValue().getId().getValuesAsQueryTokens(), not(empty()));
		assertThat(medicationSearchParamsArgumentCaptor.getValue().getId().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void searchForMedications_shouldSearchForMedicationsByLastUpdatedDate() throws Exception {
		verifyUri(String.format("/Medication?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(fhirMedicationService).searchForMedications(medicationSearchParamsArgumentCaptor.capture());
		
		assertThat(medicationSearchParamsArgumentCaptor.getValue().getLastUpdated(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(medicationSearchParamsArgumentCaptor.getValue().getLastUpdated().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(medicationSearchParamsArgumentCaptor.getValue().getLastUpdated().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void searchForMedications_shouldAddMedicationRequestsWithReturnedMedications() throws Exception {
		verifyUri("/Medication?_revinclude=MedicationRequest:medication");
		
		verify(fhirMedicationService).searchForMedications(medicationSearchParamsArgumentCaptor.capture());
		
		assertThat(medicationSearchParamsArgumentCaptor.getValue().getRevIncludes(), notNullValue());
		assertThat(medicationSearchParamsArgumentCaptor.getValue().getRevIncludes().size(), equalTo(1));
		assertThat(medicationSearchParamsArgumentCaptor.getValue().getRevIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_MEDICATION_PARAM));
		assertThat(medicationSearchParamsArgumentCaptor.getValue().getRevIncludes().iterator().next().getParamType(),
		    equalTo(FhirConstants.MEDICATION_REQUEST));
	}
	
	@Test
	public void searchForMedications_shouldAddMedicationRequestsAndMedicationDispenseWithReturnedMedications()
	        throws Exception {
		verifyUri(
		    "/Medication?_revinclude=MedicationRequest:medication&_revinclude:iterate=MedicationDispense:prescription");
		
		verify(fhirMedicationService).searchForMedications(medicationSearchParamsArgumentCaptor.capture());
		
		assertThat(medicationSearchParamsArgumentCaptor.getValue().getRevIncludes(), notNullValue());
		assertThat(medicationSearchParamsArgumentCaptor.getValue().getRevIncludes().size(), equalTo(2));
		assertThat(medicationSearchParamsArgumentCaptor.getValue().getRevIncludes(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_MEDICATION_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.MEDICATION_REQUEST)))));
		assertThat(medicationSearchParamsArgumentCaptor.getValue().getRevIncludes(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_PRESCRIPTION_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.MEDICATION_DISPENSE)))));
		
	}
	
	@Test
	public void getMedicationByUuid_shouldReturn404() throws Exception {
		when(fhirMedicationService.get(WRONG_MEDICATION_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/Medication/" + WRONG_MEDICATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	private void verifyUri(String uri) throws Exception {
		when(fhirMedicationService.searchForMedications(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(medication), 10, 1));
		
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
	public void createMedication_shouldCreateNewMedication() throws Exception {
		Medication medication = new Medication();
		medication.setId(MEDICATION_UUID);
		String medicationJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_MEDICATION_PATH)) {
			Objects.requireNonNull(is);
			medicationJson = inputStreamToString(is, UTF_8);
		}
		
		when(fhirMedicationService.create(any(Medication.class))).thenReturn(medication);
		
		MockHttpServletResponse response = post("/Medication").jsonContent(medicationJson).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isCreated());
		assertThat(response.getStatus(), is(201));
	}
	
	@Test
	public void updateMedication_shouldUpdateRequestedMedication() throws Exception {
		Medication medication = new Medication();
		medication.setId(MEDICATION_UUID);
		medication.setStatus(Medication.MedicationStatus.INACTIVE);
		
		String medicationJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_MEDICATION_PATH)) {
			assert is != null;
			Objects.requireNonNull(is);
			medicationJson = inputStreamToString(is, UTF_8);
		}
		
		lenient().when(fhirMedicationService.update(any(String.class), any(Medication.class))).thenReturn(medication);
		
		MockHttpServletResponse response = put("/Medication/" + MEDICATION_UUID).jsonContent(medicationJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
	}
	
	@Test
	public void updateMedication_shouldErrorForIdMismatch() throws Exception {
		String medicationJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_MEDICATION_PATH)) {
			Objects.requireNonNull(is);
			medicationJson = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = put("/Medication/" + WRONG_MEDICATION_UUID).jsonContent(medicationJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    containsStringIgnoringCase("body must contain an ID element which matches the request URL"));
	}
	
	@Test
	public void updateMedication_shouldErrorForNoId() throws Exception {
		String medicationJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_WITHOUT_ID_MEDICATION_PATH)) {
			Objects.requireNonNull(is);
			medicationJson = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = put("/Medication/" + MEDICATION_UUID).jsonContent(medicationJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	public void updateMedication_shouldErrorForNonexistentMedication() throws Exception {
		String medicationJson;
		try (InputStream is = this.getClass().getClassLoader()
		        .getResourceAsStream(JSON_UPDATE_WITH_WRONG_ID_MEDICATION_PATH)) {
			Objects.requireNonNull(is);
			medicationJson = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = put("/Medication/" + WRONG_MEDICATION_UUID).jsonContent(medicationJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
	}
	
	@Test
	public void deleteMedication_shouldDeleteRequestedMedication() throws Exception {
		MockHttpServletResponse response = delete("/Medication/" + MEDICATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void deleteMedication_shouldReturn404ForNonExistingMedication() throws Exception {
		doThrow(new ResourceNotFoundException("")).when(fhirMedicationService).delete(WRONG_MEDICATION_UUID);
		
		MockHttpServletResponse response = delete("/Medication/" + WRONG_MEDICATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getStatus(), equalTo(404));
	}
}
