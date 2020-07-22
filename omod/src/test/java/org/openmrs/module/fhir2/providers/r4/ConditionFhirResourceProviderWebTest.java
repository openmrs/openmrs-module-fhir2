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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.FhirConstants.AUT;
import static org.openmrs.module.fhir2.FhirConstants.AUTHOR;

import javax.servlet.ServletException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hamcrest.Matchers;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Provenance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.openmrs.module.fhir2.providers.BaseFhirResourceProviderWebTest.FhirMediaTypes;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class ConditionFhirResourceProviderWebTest extends BaseFhirR4ResourceProviderWebTest<ConditionFhirResourceProvider, Condition> {
	
	private static final String CONDITION_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";
	
	private static final String WRONG_CONDITION_UUID = "9bf0d1ac-62a8-4440-a5a1-eb1015a7cc65";
	
	private static final String JSON_CREATE_CONDITION_PATH = "org/openmrs/module/fhir2/providers/ConditionResourceWebTest_create.json";
	
	private static final String JSON_UPDATE_CONDITION_PATH = "org/openmrs/module/fhir2/providers/ConditionResourceWebTest_Update.json";
	
	private static final String JSON_UPDATE_CONDITION_NO_ID_PATH = "org/openmrs/module/fhir2/providers/ConditionResourceWebTest_updateWithoutId.json";
	
	private static final String JSON_CONDITION_WRONG_UUID_PATH = "org/openmrs/module/fhir2/providers/ConditionResourceWebTest_updateWithWrongId.json";
	
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
	
	@Mock
	private FhirConditionService conditionService;
	
	@Getter(AccessLevel.PUBLIC)
	private ConditionFhirResourceProvider resourceProvider;
	
	@Captor
	ArgumentCaptor<ReferenceAndListParam> referenceAndListParamArgumentCaptor;
	
	@Captor
	ArgumentCaptor<TokenAndListParam> tokenAndListParamArgumentCaptor;
	
	@Captor
	ArgumentCaptor<DateRangeParam> dateRangeParamArgumentCaptor;
	
	@Captor
	ArgumentCaptor<QuantityAndListParam> quantityAndListParamArgumentCaptor;
	
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
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
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
	public void shouldVerifyConditionHistoryByIdUri() throws Exception {
		Condition condition = new Condition();
		condition.setId(CONDITION_UUID);
		when(conditionService.get(CONDITION_UUID)).thenReturn(condition);
		
		MockHttpServletResponse response = getConditionHistoryRequest();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void shouldGetConditionHistoryById() throws IOException, ServletException {
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.uniqueUuid()));
		provenance.setRecorded(new Date());
		provenance.setActivity(new CodeableConcept().addCoding(
		    new Coding().setCode("CREATE").setSystem(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION).setDisplay("create")));
		provenance.addAgent(new Provenance.ProvenanceAgentComponent()
		        .setType(new CodeableConcept().addCoding(new Coding().setCode(AUT).setDisplay(AUTHOR)
		                .setSystem(FhirConstants.FHIR_TERMINOLOGY_PROVENANCE_PARTICIPANT_TYPE)))
		        .addRole(new CodeableConcept().addCoding(
		            new Coding().setCode("").setDisplay("").setSystem(FhirConstants.FHIR_TERMINOLOGY_PARTICIPATION_TYPE))));
		Condition condition = new Condition();
		condition.setId(CONDITION_UUID);
		condition.addContained(provenance);
		
		when(conditionService.get(CONDITION_UUID)).thenReturn(condition);
		
		MockHttpServletResponse response = getConditionHistoryRequest();
		
		Bundle results = readBundleResponse(response);
		assertThat(results, notNullValue());
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getResourceType().name(),
		    equalTo(Provenance.class.getSimpleName()));
		
	}
	
	@Test
	public void getConditionHistoryById_shouldReturnBundleWithEmptyEntriesIfConditionContainedIsEmpty() throws Exception {
		Condition condition = new Condition();
		condition.setId(CONDITION_UUID);
		condition.setContained(new ArrayList<>());
		when(conditionService.get(CONDITION_UUID)).thenReturn(condition);
		
		MockHttpServletResponse response = getConditionHistoryRequest();
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(false));
	}
	
	@Test
	public void getConditionHistoryById_shouldReturn404IfConditionIdIsWrong() throws Exception {
		MockHttpServletResponse response = get("/Condition/" + WRONG_CONDITION_UUID + "/_history")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	private MockHttpServletResponse getConditionHistoryRequest() throws IOException, ServletException {
		return get("/Condition/" + CONDITION_UUID + "/_history").accept(FhirMediaTypes.JSON).go();
	}
	
	@Test
	public void shouldCreateNewConditionGivenValidConditionResource() throws Exception {
		Condition condition = new Condition();
		condition.setId(CONDITION_UUID);
		String conditionJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_CONDITION_PATH)) {
			Objects.requireNonNull(is);
			conditionJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		when(conditionService.saveCondition(any(Condition.class))).thenReturn(condition);
		
		MockHttpServletResponse response = post("/Condition").jsonContent(conditionJson).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isCreated());
		assertThat(response.getStatus(), is(201));
	}
	
	@Test
	public void updateCondition_shouldUpdateConditionWithPath() throws Exception {
		String conditionJson;
		Condition condition = new Condition();
		condition.setId(CONDITION_UUID);
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_CONDITION_PATH)) {
			Objects.requireNonNull(is);
			conditionJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		when(conditionService.update(conditionJson, any(Condition.class))).thenReturn(condition);
		
		MockHttpServletResponse response = put("/Condition/" + CONDITION_UUID).jsonContent(conditionJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
	}
	
	@Test
	public void updateCondition_shouldThrowExceptionForNoId() throws Exception {
		String conditionJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_CONDITION_NO_ID_PATH)) {
			Objects.requireNonNull(is);
			conditionJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		MockHttpServletResponse response = put("/Condition/" + CONDITION_UUID).jsonContent(conditionJson)
		        .accept(FhirMediaTypes.JSON).go();
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    Matchers.containsStringIgnoringCase("Condition body must contain an ID element for update"));
	}
	
	@Test
	public void updateCondition_shouldThrowExceptionForIdMisMatch() throws Exception {
		String conditionJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_CONDITION_PATH)) {
			Objects.requireNonNull(is);
			conditionJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		MockHttpServletResponse response = put("/Condition/" + WRONG_CONDITION_UUID).jsonContent(conditionJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    Matchers.containsStringIgnoringCase("Condition body must contain an ID element which matches the request URL"));
	}
	
	@Test
	public void updateCondition_shouldThrowErrorForNonExistentCondition() throws Exception {
		String conditionJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CONDITION_WRONG_UUID_PATH)) {
			Objects.requireNonNull(is);
			conditionJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		when(conditionService.update(eq(WRONG_CONDITION_UUID), any(Condition.class)))
		        .thenThrow(new MethodNotAllowedException("Condition " + WRONG_CONDITION_UUID + " does not exist"));
		
		MockHttpServletResponse response = put("/Condition/" + WRONG_CONDITION_UUID).jsonContent(conditionJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isMethodNotAllowed());
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithMatchingPatientUUID() throws Exception {
		verifyURI(String.format("/Condition?patient=%s", PATIENT_UUID));
		
		verify(conditionService).searchConditions(referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PATIENT_UUID));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(null));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithMatchingPatientName() throws Exception {
		verifyURI(String.format("/Condition?patient.name=%s", PATIENT_NAME));
		
		verify(conditionService).searchConditions(referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PATIENT_NAME));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(Patient.SP_NAME));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithMatchingPatientGivenName() throws Exception {
		verifyURI(String.format("/Condition?patient.given=%s", PATIENT_GIVEN_NAME));
		
		verify(conditionService).searchConditions(referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PATIENT_GIVEN_NAME));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(Patient.SP_GIVEN));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithMatchingPatientFamilyName() throws Exception {
		verifyURI(String.format("/Condition?patient.family=%s", PATIENT_FAMILY_NAME));
		
		verify(conditionService).searchConditions(referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PATIENT_FAMILY_NAME));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(Patient.SP_FAMILY));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithMatchingPatientIdentifier() throws Exception {
		verifyURI(String.format("/Condition?patient.identifier=%s", PATIENT_IDENTIFIER));
		
		verify(conditionService).searchConditions(referenceAndListParamArgumentCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull());
		
		assertThat(referenceAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PATIENT_IDENTIFIER));
		assertThat(referenceAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getChain(),
		    equalTo(Patient.SP_IDENTIFIER));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithMatchingCode() throws Exception {
		verifyURI(String.format("/Condition?code=%s", CONDITION_CODE));
		
		verify(conditionService).searchConditions(isNull(), tokenAndListParamArgumentCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(CONDITION_CODE));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithMatchingClinicalStatus() throws Exception {
		verifyURI(String.format("/Condition?clinical-status=%s", CLINICAL_STATUS));
		
		verify(conditionService).searchConditions(isNull(), isNull(), tokenAndListParamArgumentCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull());
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(CLINICAL_STATUS));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithMatchingRecordedDate() throws Exception {
		verifyURI(String.format("/Condition?recorded-date=%s", RECORDED_DATE));
		
		verify(conditionService).searchConditions(isNull(), isNull(), isNull(), isNull(), isNull(),
		    dateRangeParamArgumentCaptor.capture(), isNull());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1978, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeParamArgumentCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeParamArgumentCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithOnsetDateGreaterThanOrEqualTo() throws Exception {
		verifyURI(String.format("/Condition?onset-date=%s", ONSET_DATE));
		
		verify(conditionService).searchConditions(isNull(), isNull(), isNull(), dateRangeParamArgumentCaptor.capture(),
		    isNull(), isNull(), isNull());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeParamArgumentCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeParamArgumentCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void searchForConditions_shouldReturnBundleWithOnsetAgeLessThanHour() throws Exception {
		verifyURI(String.format("/Condition?onset-age=%s", ONSET_AGE));
		
		verify(conditionService).searchConditions(isNull(), isNull(), isNull(), isNull(),
		    quantityAndListParamArgumentCaptor.capture(), isNull(), isNull());
		
		assertThat(quantityAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(quantityAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(quantityAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getPrefix(),
		    equalTo(ParamPrefixEnum.LESSTHAN));
		assertThat(quantityAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(BigDecimal.valueOf(2)));
		assertThat(quantityAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getUnits(),
		    equalTo("h"));
		assertThat(quantityAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getSystem(),
		    equalTo(null));
	}
	
	private void verifyURI(String uri) throws Exception {
		Condition condition = new Condition();
		condition.setId(CONDITION_UUID);
		when(conditionService.searchConditions(any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(condition), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void deleteCondition_shouldDeleteCondition() throws Exception {
		OperationOutcome outcome = new OperationOutcome();
		outcome.setId(CONDITION_UUID);
		outcome.getText().setDivAsString("Deleted successfully");
		
		org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
		condition.setId(CONDITION_UUID);
		
		when(conditionService.delete(CONDITION_UUID)).thenReturn(condition);
		
		MockHttpServletResponse response = delete("/Condition/" + CONDITION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void deleteCondition_shouldReturn404ForNonExistingCondition() throws Exception {
		when(conditionService.get(WRONG_CONDITION_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = delete("/Condition/" + WRONG_CONDITION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
}
