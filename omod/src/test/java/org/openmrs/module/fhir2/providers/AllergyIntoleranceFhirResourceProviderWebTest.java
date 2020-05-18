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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.FhirConstants.AUT;
import static org.openmrs.module.fhir2.FhirConstants.AUTHOR;

import javax.servlet.ServletException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Provenance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirAllergyIntoleranceService;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.openmrs.module.fhir2.web.servlet.BaseFhirResourceProviderTest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class AllergyIntoleranceFhirResourceProviderWebTest extends BaseFhirResourceProviderTest<AllergyIntoleranceFhirResourceProvider, AllergyIntolerance> {
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_ALLERGY_UUID = "2085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String PATIENT_UUID = "8d703ff2-c3e2-4070-9737-73e713d5a50d";
	
	@Mock
	private FhirAllergyIntoleranceService allergyService;
	
	@Getter(AccessLevel.PUBLIC)
	private AllergyIntoleranceFhirResourceProvider allergyProvider;
	
	@Captor
	private ArgumentCaptor<ReferenceAndListParam> patientCaptor;
	
	@Captor
	private ArgumentCaptor<TokenAndListParam> tokenAndListParamArgumentCaptor;
	
	AllergyIntolerance allergyIntolerance;
	
	@Before
	@Override
	public void setup() throws Exception {
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
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo(null));
		assertThat(referenceParam.getValue(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientIdentifier() throws Exception {
		verifyUri("/AllergyIntolerance?patient.identifier=M4001-1");
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("identifier"));
		assertThat(referenceParam.getValue(), equalTo("M4001-1"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientIdentifierWithOr() throws Exception {
		verifyUri("/AllergyIntolerance?patient.identifier=M4001-1,MK89I");
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("given"));
		assertThat(referenceParam.getValue(), equalTo("John"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientFamilyName() throws Exception {
		verifyUri("/AllergyIntolerance?patient.family=John");
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo("John"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientFamilyNameWithOr() throws Exception {
		verifyUri("/AllergyIntolerance?patient.family=John,Tim,Him");
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		
		verify(allergyService).searchForAllergies(patientCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		    isNull(), isNull());
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo("food"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByAllergenCode() throws Exception {
		verifyUri("/AllergyIntolerance?code=d1b98543-10ff-4911-83a2-b7f5fafe2751");
		
		verify(allergyService).searchForAllergies(isNull(), isNull(), tokenAndListParamArgumentCaptor.capture(), isNull(),
		    isNull(), isNull());
		
		List<TokenOrListParam> listParams = tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens();
		TokenParam tokenParam = listParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenParam.getValue(), equalTo("d1b98543-10ff-4911-83a2-b7f5fafe2751"));
	}
	
	public void searchForAllergies_shouldSearchForAllergiesByAllergenCodeAndSystem() throws Exception {
		verifyUri("/AllergyIntolerance?code=d1b98543-10ff-4911-83a2-b7f5fafe2751");
		
		verify(allergyService).searchForAllergies(isNull(), isNull(), tokenAndListParamArgumentCaptor.capture(), isNull(),
		    isNull(), isNull());
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo("d1b98543-10ff-4911-83a2-b7f5fafe2751"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesBySeverity() throws Exception {
		verifyUri("/AllergyIntolerance?severity=severe");
		
		verify(allergyService).searchForAllergies(isNull(), isNull(), isNull(), tokenAndListParamArgumentCaptor.capture(),
		    isNull(), isNull());
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo("severe"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByManifestation() throws Exception {
		verifyUri("/AllergyIntolerance?manifestation=c0b1f314-1691-11df-97a5-7038c432aabd");
		
		verify(allergyService).searchForAllergies(isNull(), isNull(), isNull(), isNull(),
		    tokenAndListParamArgumentCaptor.capture(), isNull());
		
		List<TokenOrListParam> listParams = tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens();
		TokenParam tokenParam = listParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenParam.getValue(), equalTo("c0b1f314-1691-11df-97a5-7038c432aabd"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByStatus() throws Exception {
		verifyUri("/AllergyIntolerance?clinical-status=active");
		
		verify(allergyService).searchForAllergies(isNull(), isNull(), isNull(), isNull(), isNull(),
		    tokenAndListParamArgumentCaptor.capture());
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo("active"));
	}
	
	@Test
	public void getAllergyIntoleranceByUuid_shouldReturn404() throws Exception {
		when(allergyService.get(WRONG_ALLERGY_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/AllergyIntolerance/" + WRONG_ALLERGY_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void shouldVerifyAllergyIntoleranceHistoryByIdUri() throws Exception {
		AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
		allergyIntolerance.setId(ALLERGY_UUID);
		when(allergyService.get(ALLERGY_UUID)).thenReturn(allergyIntolerance);
		
		MockHttpServletResponse response = getAllergyIntoleranceHistoryRequest();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(BaseFhirResourceProviderTest.FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void shouldGetAllergyIntoleranceHistoryById() throws IOException, ServletException {
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
		AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
		allergyIntolerance.setId(ALLERGY_UUID);
		allergyIntolerance.addContained(provenance);
		
		when(allergyService.get(ALLERGY_UUID)).thenReturn(allergyIntolerance);
		
		MockHttpServletResponse response = getAllergyIntoleranceHistoryRequest();
		
		Bundle results = readBundleResponse(response);
		assertThat(results, notNullValue());
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getResourceType().name(),
		    equalTo(Provenance.class.getSimpleName()));
		
	}
	
	@Test
	public void getAllergyIntoleranceHistoryById_shouldReturnBundleWithEmptyEntriesIfResourceContainedIsEmpty()
	        throws Exception {
		AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
		allergyIntolerance.setId(ALLERGY_UUID);
		allergyIntolerance.setContained(new ArrayList<>());
		when(allergyService.get(ALLERGY_UUID)).thenReturn(allergyIntolerance);
		
		MockHttpServletResponse response = getAllergyIntoleranceHistoryRequest();
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(false));
	}
	
	@Test
	public void getAllergyIntoleranceHistoryById_shouldReturn404IfAllergyIntoleranceIdIsWrong() throws Exception {
		MockHttpServletResponse response = get("/AllergyIntolerance/" + WRONG_ALLERGY_UUID + "/_history")
		        .accept(BaseFhirResourceProviderTest.FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	private MockHttpServletResponse getAllergyIntoleranceHistoryRequest() throws IOException, ServletException {
		return get("/AllergyIntolerance/" + ALLERGY_UUID + "/_history")
		        .accept(BaseFhirResourceProviderTest.FhirMediaTypes.JSON).go();
		
	}
	
	private void verifyUri(String uri) throws Exception {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.setId(ALLERGY_UUID);
		when(allergyService.searchForAllergies(any(), any(), any(), any(), any(), any()))
		        .thenReturn(Collections.singletonList(allergyIntolerance));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
	}
	
}
