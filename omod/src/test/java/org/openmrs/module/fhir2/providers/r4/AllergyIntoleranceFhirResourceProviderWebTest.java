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
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirAllergyIntoleranceService;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class AllergyIntoleranceFhirResourceProviderWebTest extends BaseFhirR4ResourceProviderWebTest<AllergyIntoleranceFhirResourceProvider, AllergyIntolerance> {
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_ALLERGY_UUID = "2085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String PATIENT_UUID = "8d703ff2-c3e2-4070-9737-73e713d5a50d";
	
	private static final String JSON_CREATE_ALLERGY_PATH = "org/openmrs/module/fhir2/providers/AllergyIntoleranceWebTest_create.json";
	
	private static final String JSON_UPDATE_ALLERGY_PATH = "org/openmrs/module/fhir2/providers/AllergyIntoleranceWebTest_update.json";
	
	private static final String JSON_UPDATE_WITHOUTID_ALLERGY_PATH = "org/openmrs/module/fhir2/providers/AllergyIntoleranceWebTest_updateWithoutId.json";
	
	private static final String JSON_UPDATE_WITH_WRONGID_ALLERGY_PATH = "org/openmrs/module/fhir2/providers/AllergyIntoleranceWebTest_updateWithWrongId.json";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	@Mock
	private FhirAllergyIntoleranceService allergyService;
	
	@Getter(AccessLevel.PUBLIC)
	private AllergyIntoleranceFhirResourceProvider allergyProvider;
	
	@Captor
	private ArgumentCaptor<ReferenceAndListParam> patientCaptor;
	
	@Captor
	private ArgumentCaptor<TokenAndListParam> tokenAndListParamArgumentCaptor;
	
	@Captor
	private ArgumentCaptor<DateRangeParam> dateRangeParamArgumentCaptor;
	
	@Captor
	private ArgumentCaptor<HashSet<Include>> includeArgumentCaptor;
	
	private AllergyIntolerance allergyIntolerance;
	
	@Before
	@Override
	public void setup() throws ServletException {
		allergyProvider = new AllergyIntoleranceFhirResourceProvider();
		allergyProvider.setFhirAllergyIntoleranceService(allergyService);
		allergyIntolerance = new AllergyIntolerance();
		allergyIntolerance.setId(ALLERGY_UUID);
		super.setup();
	}
	
	@Override
	public AllergyIntoleranceFhirResourceProvider getResourceProvider() {
		return allergyProvider;
	}
	
	@Test
	public void getAllergyIntoleranceByUuid_shouldReturnAllergy() throws Exception {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.setId(ALLERGY_UUID);
		when(allergyService.get(ALLERGY_UUID)).thenReturn(allergy);
		
		MockHttpServletResponse response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientUUID() throws Exception {
		verifyUri(String.format("/AllergyIntolerance?patient=%s", PATIENT_UUID));
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo(null));
		assertThat(referenceParam.getValue(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientIdentifier() throws Exception {
		verifyUri("/AllergyIntolerance?patient.identifier=M4001-1");
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("identifier"));
		assertThat(referenceParam.getValue(), equalTo("M4001-1"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientIdentifierWithOr() throws Exception {
		verifyUri("/AllergyIntolerance?patient.identifier=M4001-1,MK89I");
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("identifier"));
		assertThat(referenceParam.getValue(), equalTo("M4001-1"));
		assertThat(orListParams.get(0).getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientIdentifierWithAnd() throws Exception {
		verifyUri("/AllergyIntolerance?patient.identifier=M4001-1&patient.identifier=MK89I");
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("identifier"));
		assertThat(referenceParam.getValue(), equalTo("M4001-1"));
		assertThat(patientCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientGivenName() throws Exception {
		verifyUri("/AllergyIntolerance?patient.given=John");
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("given"));
		assertThat(referenceParam.getValue(), equalTo("John"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientFamilyName() throws Exception {
		verifyUri("/AllergyIntolerance?patient.family=John");
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo("John"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientFamilyNameWithOr() throws Exception {
		verifyUri("/AllergyIntolerance?patient.family=John,Tim,Him");
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo("John"));
		assertThat(orListParams.get(0).getValuesAsQueryTokens().size(), equalTo(3));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientFamilyNameWithAnd() throws Exception {
		verifyUri("/AllergyIntolerance?patient.family=John&patient.family=Tim&patient.family=Him");
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo("John"));
		assertThat(patientCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(3));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientName() throws Exception {
		verifyUri("/AllergyIntolerance?patient.name=John");
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("name"));
		assertThat(referenceParam.getValue(), equalTo("John"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByCategory() throws Exception {
		verifyUri("/AllergyIntolerance?category=food");
		
		verify(allergyService).searchForAllergies(isNull(), tokenAndListParamArgumentCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo("food"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByAllergenCode() throws Exception {
		verifyUri("/AllergyIntolerance?code=d1b98543-10ff-4911-83a2-b7f5fafe2751");
		
		verify(allergyService).searchForAllergies(isNull(), isNull(), tokenAndListParamArgumentCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<TokenOrListParam> listParams = tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens();
		TokenParam tokenParam = listParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenParam.getValue(), equalTo("d1b98543-10ff-4911-83a2-b7f5fafe2751"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByAllergenCodeAndSystem() throws Exception {
		verifyUri("/AllergyIntolerance?code=d1b98543-10ff-4911-83a2-b7f5fafe2751");
		
		verify(allergyService).searchForAllergies(isNull(), isNull(), tokenAndListParamArgumentCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo("d1b98543-10ff-4911-83a2-b7f5fafe2751"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesBySeverity() throws Exception {
		verifyUri("/AllergyIntolerance?severity=severe");
		
		verify(allergyService).searchForAllergies(isNull(), isNull(), isNull(), tokenAndListParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo("severe"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByManifestation() throws Exception {
		verifyUri("/AllergyIntolerance?manifestation=c0b1f314-1691-11df-97a5-7038c432aabd");
		
		verify(allergyService).searchForAllergies(isNull(), isNull(), isNull(), isNull(),
		    tokenAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<TokenOrListParam> listParams = tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens();
		TokenParam tokenParam = listParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenParam.getValue(), equalTo("c0b1f314-1691-11df-97a5-7038c432aabd"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByStatus() throws Exception {
		verifyUri("/AllergyIntolerance?clinical-status=active");
		
		verify(allergyService).searchForAllergies(isNull(), isNull(), isNull(), isNull(), isNull(),
		    tokenAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo("active"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByUUID() throws Exception {
		verifyUri(String.format("/AllergyIntolerance?_id=%s", ALLERGY_UUID));
		
		verify(allergyService).searchForAllergies(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    tokenAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull());
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByLastUpdated() throws Exception {
		verifyUri(String.format("/AllergyIntolerance?_lastUpdated=eq%s", LAST_UPDATED_DATE));
		
		verify(allergyService).searchForAllergies(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    dateRangeParamArgumentCaptor.capture(), isNull(), isNull());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(dateRangeParamArgumentCaptor.getValue(), notNullValue());
		assertThat(dateRangeParamArgumentCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeParamArgumentCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void searchForAllergies_shouldIncludePatientsWithReturnedAllergies() throws Exception {
		verifyUri("/AllergyIntolerance?_include=AllergyIntolerance:patient");
		
		verify(allergyService).searchForAllergies(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(),
		    equalTo(FhirConstants.ALLERGY_INTOLERANCE));
	}
	
	@Test
	public void getAllergyIntoleranceByUuid_shouldReturn404() throws Exception {
		when(allergyService.get(WRONG_ALLERGY_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/AllergyIntolerance/" + WRONG_ALLERGY_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	private void verifyUri(String uri) throws Exception {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.setId(ALLERGY_UUID);
		
		when(allergyService.searchForAllergies(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void createAllergyIntolerance_shouldCreateAllergyIntolerance() throws Exception {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.setId(ALLERGY_UUID);
		String createAllergyJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_ALLERGY_PATH)) {
			Objects.requireNonNull(is);
			createAllergyJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		when(allergyService.create(any(AllergyIntolerance.class))).thenReturn(allergy);
		
		MockHttpServletResponse response = post("/AllergyIntolerance").jsonContent(createAllergyJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isCreated());
		assertThat(response.getStatus(), is(201));
	}
	
	@Test
	public void updateAllergyIntolerance_shouldUpdateRequestedAllergyIntolerance() throws Exception {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.setId(ALLERGY_UUID);
		String createAllergyJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_ALLERGY_PATH)) {
			Objects.requireNonNull(is);
			createAllergyJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		when(allergyService.update(any(String.class), any(AllergyIntolerance.class))).thenReturn(allergy);
		
		MockHttpServletResponse response = put("/AllergyIntolerance/" + ALLERGY_UUID).jsonContent(createAllergyJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
	}
	
	@Test
	public void updateAllergyIntolerance_shouldThrowErrorForIdMismatch() throws Exception {
		String createAllergyJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_ALLERGY_PATH)) {
			Objects.requireNonNull(is);
			createAllergyJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/AllergyIntolerance/" + WRONG_ALLERGY_UUID).jsonContent(createAllergyJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    containsStringIgnoringCase("body must contain an ID element which matches the request URL"));
	}
	
	@Test
	public void updateAllergyIntolerance_shouldThrowErrorForNoId() throws Exception {
		String createAllergyJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_WITHOUTID_ALLERGY_PATH)) {
			Objects.requireNonNull(is);
			createAllergyJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/AllergyIntolerance/" + ALLERGY_UUID).jsonContent(createAllergyJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	public void updateAllergyIntolerance_shouldThrowErrorForNonexistentMedication() throws Exception {
		String createAllergyJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_WITH_WRONGID_ALLERGY_PATH)) {
			Objects.requireNonNull(is);
			createAllergyJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		when(allergyService.update(eq(WRONG_ALLERGY_UUID), any(AllergyIntolerance.class)))
		        .thenThrow(new MethodNotAllowedException("AllergyIntolerance " + WRONG_ALLERGY_UUID + " does not exist"));
		
		MockHttpServletResponse response = put("/AllergyIntolerance/" + WRONG_ALLERGY_UUID).jsonContent(createAllergyJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isMethodNotAllowed());
	}
	
	@Test
	public void deleteAllergyIntolerance_shouldDeleteRequestedAllergyIntolerance() throws Exception {
		when(allergyService.delete(any(String.class))).thenReturn(allergyIntolerance);
		
		MockHttpServletResponse response = delete("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void deleteAllergyIntolerance_shouldReturn404ForNonExistingAllergyIntolerance() throws Exception {
		when(allergyService.delete(WRONG_ALLERGY_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = delete("/AllergyIntolerance/" + WRONG_ALLERGY_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isNotFound());
	}
	
}
