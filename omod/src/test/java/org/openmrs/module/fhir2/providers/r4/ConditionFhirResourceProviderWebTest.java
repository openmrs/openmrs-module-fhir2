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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import javax.servlet.ServletException;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;

import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.search.param.ConditionSearchParams;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class ConditionFhirResourceProviderWebTest extends BaseFhirR4ResourceProviderWebTest<ConditionFhirResourceProvider, Condition> {
	
	private static final String CONDITION_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";
	
	private static final String WRONG_CONDITION_UUID = "9bf0d1ac-62a8-4440-a5a1-eb1015a7cc65";
	
	private static final String JSON_CREATE_CONDITION_PATH = "org/openmrs/module/fhir2/providers/ConditionWebTest_create.json";
	
	private static final String PATIENT_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
	
	private static final String PATIENT_GIVEN_NAME = "Horatio";
	
	private static final String PATIENT_FAMILY_NAME = "Hornblower";
	
	private static final String PATIENT_NAME = "Horatio Hornblower";
	
	private static final String PATIENT_IDENTIFIER = "6TS-104";
	
	private static final String CONDITION_CODE = "5085";
	
	private static final String CLINICAL_STATUS = "ACTIVE";
	
	private static final String ONSET_DATE = "ge1975-02-02";
	
	private static final String RECORDED_DATE = "eq1978-02-02";
	
	private static final String ONSET_AGE = "lt2||h";
	
	private static final String LAST_UPDATED_DATE = "eq2020-09-03";
	
	@Mock
	private FhirConditionService conditionService;
	
	@Getter(AccessLevel.PUBLIC)
	private ConditionFhirResourceProvider resourceProvider;
	
	@Captor
	private ArgumentCaptor<ConditionSearchParams> conditionSearchParamsArgumentCaptor;
	
	@Before
	@Override
	public void setup() throws ServletException {
		resourceProvider = new ConditionFhirResourceProvider();
		resourceProvider.setConditionService(conditionService);
		super.setup();
	}
	
	@Test
	public void shouldReturnConditionByUuid() throws Exception {
		Condition condition = new Condition();
		condition.setId(CONDITION_UUID);
		when(conditionService.get(CONDITION_UUID)).thenReturn(condition);
		
		MockHttpServletResponse response = get("/Condition/" + CONDITION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), containsString(FhirMediaTypes.JSON.toString()));
		
		Condition resource = readResponse(response);
		assertThat(resource.getIdElement().getIdPart(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void shouldReturn404IfConditionNotFound() throws Exception {
		when(conditionService.get(WRONG_CONDITION_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/Condition/" + WRONG_CONDITION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void shouldCreateNewConditionGivenValidConditionResource() throws Exception {
		Condition condition = new Condition();
		condition.setId(CONDITION_UUID);
		String conditionJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_CONDITION_PATH)) {
			Objects.requireNonNull(is);
			conditionJson = inputStreamToString(is, UTF_8);
		}
		
		when(conditionService.create(any(Condition.class))).thenReturn(condition);
		
		MockHttpServletResponse response = post("/Condition").jsonContent(conditionJson).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isCreated());
		assertThat(response.getStatus(), is(201));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithMatchingPatientUUID() throws Exception {
		verifyURI(String.format("/Condition?patient=%s", PATIENT_UUID));
		
		verify(conditionService).searchConditions(conditionSearchParamsArgumentCaptor.capture());
		
		assertThat(conditionSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getPatientParam().getValuesAsQueryTokens(), not(empty()));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getPatientParam().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PATIENT_UUID));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getPatientParam().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getChain(),
		    equalTo(null));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithMatchingPatientName() throws Exception {
		verifyURI(String.format("/Condition?patient.name=%s", PATIENT_NAME));
		
		verify(conditionService).searchConditions(conditionSearchParamsArgumentCaptor.capture());
		
		assertThat(conditionSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getPatientParam().getValuesAsQueryTokens(), not(empty()));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getPatientParam().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PATIENT_NAME));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getPatientParam().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getChain(),
		    equalTo(Patient.SP_NAME));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithMatchingPatientGivenName() throws Exception {
		verifyURI(String.format("/Condition?patient.given=%s", PATIENT_GIVEN_NAME));
		
		verify(conditionService).searchConditions(conditionSearchParamsArgumentCaptor.capture());
		
		assertThat(conditionSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getPatientParam().getValuesAsQueryTokens(), not(empty()));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getPatientParam().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PATIENT_GIVEN_NAME));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getPatientParam().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getChain(),
		    equalTo(Patient.SP_GIVEN));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithMatchingPatientFamilyName() throws Exception {
		verifyURI(String.format("/Condition?patient.family=%s", PATIENT_FAMILY_NAME));
		
		verify(conditionService).searchConditions(conditionSearchParamsArgumentCaptor.capture());
		
		assertThat(conditionSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getPatientParam().getValuesAsQueryTokens(), not(empty()));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getPatientParam().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PATIENT_FAMILY_NAME));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getPatientParam().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getChain(),
		    equalTo(Patient.SP_FAMILY));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithMatchingPatientIdentifier() throws Exception {
		verifyURI(String.format("/Condition?patient.identifier=%s", PATIENT_IDENTIFIER));
		
		verify(conditionService).searchConditions(conditionSearchParamsArgumentCaptor.capture());
		
		assertThat(conditionSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getPatientParam().getValuesAsQueryTokens(), not(empty()));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getPatientParam().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PATIENT_IDENTIFIER));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getPatientParam().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getChain(),
		    equalTo(Patient.SP_IDENTIFIER));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithMatchingCode() throws Exception {
		verifyURI(String.format("/Condition?code=%s", CONDITION_CODE));
		
		verify(conditionService).searchConditions(conditionSearchParamsArgumentCaptor.capture());
		
		assertThat(conditionSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getCode().getValuesAsQueryTokens(), not(empty()));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getCode().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(CONDITION_CODE));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithMatchingClinicalStatus() throws Exception {
		verifyURI(String.format("/Condition?clinical-status=%s", CLINICAL_STATUS));
		
		verify(conditionService).searchConditions(conditionSearchParamsArgumentCaptor.capture());
		
		assertThat(conditionSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getClinicalStatus().getValuesAsQueryTokens(),
		    not(empty()));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getClinicalStatus().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(CLINICAL_STATUS));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithMatchingRecordedDate() throws Exception {
		verifyURI(String.format("/Condition?recorded-date=%s", RECORDED_DATE));
		
		verify(conditionService).searchConditions(conditionSearchParamsArgumentCaptor.capture());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1978, Calendar.FEBRUARY, 2);
		
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getRecordedDate().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getRecordedDate().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithOnsetDateGreaterThanOrEqualTo() throws Exception {
		verifyURI(String.format("/Condition?onset-date=%s", ONSET_DATE));
		
		verify(conditionService).searchConditions(conditionSearchParamsArgumentCaptor.capture());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getOnsetDate().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getOnsetDate().getUpperBound(), nullValue());
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithMatchingUUID() throws Exception {
		verifyURI(String.format("/Condition?_id=%s", CONDITION_UUID));
		
		verify(conditionService).searchConditions(conditionSearchParamsArgumentCaptor.capture());
		
		assertThat(conditionSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getId().getValuesAsQueryTokens(), not(empty()));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getId().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(CONDITION_UUID));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithLastUpdatedDateEqualTo() throws Exception {
		verifyURI(String.format("/Condition?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(conditionService).searchConditions(conditionSearchParamsArgumentCaptor.capture());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getLastUpdated().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getLastUpdated().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithOnsetAgeLessThanHour() throws Exception {
		verifyURI(String.format("/Condition?onset-age=%s", ONSET_AGE));
		
		verify(conditionService).searchConditions(conditionSearchParamsArgumentCaptor.capture());
		
		assertThat(conditionSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getOnsetAge().getValuesAsQueryTokens(), not(empty()));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getOnsetAge().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getPrefix(),
		    equalTo(ParamPrefixEnum.LESSTHAN));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getOnsetAge().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(BigDecimal.valueOf(2)));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getOnsetAge().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getUnits(),
		    equalTo("h"));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getOnsetAge().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getSystem(),
		    equalTo(null));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleOfWithIncludedResources() throws Exception {
		verifyURI("/Condition?_include=Condition:patient");
		
		verify(conditionService).searchConditions(conditionSearchParamsArgumentCaptor.capture());
		
		assertThat(conditionSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getIncludes().size(), equalTo(1));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(conditionSearchParamsArgumentCaptor.getValue().getIncludes().iterator().next().getParamType(),
		    equalTo(FhirConstants.CONDITION));
	}
	
	private void verifyURI(String uri) throws Exception {
		Condition condition = new Condition();
		condition.setId(CONDITION_UUID);
		when(conditionService.searchConditions(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(condition), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), containsString(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry().size(), greaterThanOrEqualTo(1));
	}
}
