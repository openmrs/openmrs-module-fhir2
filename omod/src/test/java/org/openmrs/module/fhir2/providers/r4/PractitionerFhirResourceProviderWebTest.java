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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.FhirConstants.AUT;
import static org.openmrs.module.fhir2.FhirConstants.AUTHOR;

import javax.servlet.ServletException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.hamcrest.MatcherAssert;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Provenance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class PractitionerFhirResourceProviderWebTest extends BaseFhirR4ResourceProviderWebTest<PractitionerFhirResourceProvider, Practitioner> {
	
	private static final String PRACTITIONER_UUID = "c51d0879-ed58-4655-a450-6527afba831f";
	
	private static final String WRONG_PRACTITIONER_UUID = "810abbe5-4eca-47de-8e00-3f334ec89036";
	
	private static final String NAME = "Ricky sanchez";
	
	private static final String PRACTITIONER_IDENTIFIER = "eu984ot-k";
	
	private static final String PRACTITIONER_GIVEN_NAME = "John";
	
	private static final String PRACTITIONER_FAMILY_NAME = "Doe";
	
	private static final String CITY = "Indianapolis";
	
	private static final String STATE = "IN";
	
	private static final String POSTAL_CODE = "46202";
	
	private static final String COUNTRY = "USA";
	
	@Getter(AccessLevel.PUBLIC)
	private PractitionerFhirResourceProvider resourceProvider;
	
	@Mock
	private FhirPractitionerService practitionerService;
	
	@Captor
	private ArgumentCaptor<StringAndListParam> stringAndListParamArgumentCaptor;
	
	@Captor
	private ArgumentCaptor<TokenAndListParam> tokenAndListParamArgumentCaptor;
	
	@Before
	@Override
	public void setup() throws ServletException {
		resourceProvider = new PractitionerFhirResourceProvider();
		resourceProvider.setPractitionerService(practitionerService);
		super.setup();
	}
	
	@Test
	public void getPractitionerById_shouldReturnPractitioner() throws Exception {
		Practitioner practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		when(practitionerService.get(PRACTITIONER_UUID)).thenReturn(practitioner);
		
		MockHttpServletResponse response = get("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isOk());
		MatcherAssert.assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		MatcherAssert.assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void getEncounterByWrongUuid_shouldReturn404() throws Exception {
		
		MockHttpServletResponse response = get("/Practitioner/" + WRONG_PRACTITIONER_UUID).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isNotFound());
	}
	
	@Test
	public void findPractitionersByName_shouldReturnBundleOfPractitioners() throws Exception {
		verifyUri(String.format("/Practitioner?name=%s", NAME));
		
		verify(practitionerService).searchForPractitioners(stringAndListParamArgumentCaptor.capture(), any(), any(), any(),
		    any(), any(), any(), any());
		
		assertThat(stringAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(stringAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(NAME));
	}
	
	@Test
	public void findPractitionersByIdentifier_shouldReturnBundleOfPractitioners() throws Exception {
		verifyUri(String.format("/Practitioner?identifier=%s", PRACTITIONER_IDENTIFIER));
		
		verify(practitionerService).searchForPractitioners(any(), tokenAndListParamArgumentCaptor.capture(), any(), any(),
		    any(), any(), any(), any());
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PRACTITIONER_IDENTIFIER));
	}
	
	@Test
	public void findPractitionersByGivenName_shouldReturnBundleOfPractitioners() throws Exception {
		verifyUri(String.format("/Practitioner?given=%s", PRACTITIONER_GIVEN_NAME));
		
		verify(practitionerService).searchForPractitioners(any(), any(), stringAndListParamArgumentCaptor.capture(), any(),
		    any(), any(), any(), any());
		
		assertThat(stringAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(stringAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PRACTITIONER_GIVEN_NAME));
	}
	
	@Test
	public void findPractitionersByFamilyName_shouldReturnBundleOfPractitioners() throws Exception {
		verifyUri(String.format("/Practitioner?family=%s", PRACTITIONER_FAMILY_NAME));
		
		verify(practitionerService).searchForPractitioners(any(), any(), any(), stringAndListParamArgumentCaptor.capture(),
		    any(), any(), any(), any());
		
		assertThat(stringAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(stringAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(PRACTITIONER_FAMILY_NAME));
	}
	
	@Test
	public void findPractitionersByAddressCity_shouldReturnBundleOfPractitioners() throws Exception {
		verifyUri(String.format("/Practitioner?address-city=%s", CITY));
		
		verify(practitionerService).searchForPractitioners(any(), any(), any(), any(),
		    stringAndListParamArgumentCaptor.capture(), any(), any(), any());
		
		assertThat(stringAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(stringAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(CITY));
	}
	
	@Test
	public void findPractitionersByAddressState_shouldReturnBundleOfPractitioners() throws Exception {
		verifyUri(String.format("/Practitioner?address-state=%s", STATE));
		
		verify(practitionerService).searchForPractitioners(any(), any(), any(), any(), any(),
		    stringAndListParamArgumentCaptor.capture(), any(), any());
		
		assertThat(stringAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(stringAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(STATE));
	}
	
	@Test
	public void findPractitionersByAddressPostalCode_shouldReturnBundleOfPractitioners() throws Exception {
		verifyUri(String.format("/Practitioner?address-postalcode=%s", POSTAL_CODE));
		
		verify(practitionerService).searchForPractitioners(any(), any(), any(), any(), any(), any(),
		    stringAndListParamArgumentCaptor.capture(), any());
		
		assertThat(stringAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(stringAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(POSTAL_CODE));
	}
	
	@Test
	public void findPractitionersByAddressCountry_shouldReturnBundleOfPractitioners() throws Exception {
		verifyUri(String.format("/Practitioner?address-country=%s", COUNTRY));
		
		verify(practitionerService).searchForPractitioners(any(), any(), any(), any(), any(), any(), any(),
		    stringAndListParamArgumentCaptor.capture());
		
		assertThat(stringAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(stringAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(COUNTRY));
	}
	
	@Test
	public void findPractitioners_shouldHandleComplexQuery() throws Exception {
		verifyUri(String.format("/Practitioner?identifier=%s&name=%s", PRACTITIONER_IDENTIFIER, NAME));
		
		verify(practitionerService).searchForPractitioners(stringAndListParamArgumentCaptor.capture(),
		    tokenAndListParamArgumentCaptor.capture(), any(), any(), any(), any(), any(), any());
		
		assertThat(stringAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(stringAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens()
		        .get(0).getValue(),
		    equalTo(NAME));
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PRACTITIONER_IDENTIFIER));
	}
	
	@Test
	public void shouldVerifyGetPractitionerHistoryByIdUri() throws Exception {
		Practitioner practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		when(practitionerService.get(PRACTITIONER_UUID)).thenReturn(practitioner);
		
		MockHttpServletResponse response = getPractitionerHistoryByIdRequest();
		
		MatcherAssert.assertThat(response, isOk());
		MatcherAssert.assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void shouldGetPractitionerHistoryById() throws IOException, ServletException {
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
		Practitioner practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		practitioner.addContained(provenance);
		
		when(practitionerService.get(PRACTITIONER_UUID)).thenReturn(practitioner);
		
		MockHttpServletResponse response = getPractitionerHistoryByIdRequest();
		
		Bundle results = readBundleResponse(response);
		assertThat(results, notNullValue());
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getResourceType().name(),
		    equalTo(Provenance.class.getSimpleName()));
		
	}
	
	@Test
	public void getPractitionerHistoryById_shouldReturnBundleWithEmptyEntriesIfPractitionerContainedIsEmpty()
	        throws Exception {
		Practitioner practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		practitioner.setContained(new ArrayList<>());
		when(practitionerService.get(PRACTITIONER_UUID)).thenReturn(practitioner);
		
		MockHttpServletResponse response = getPractitionerHistoryByIdRequest();
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(false));
	}
	
	@Test
	public void getPractitionerHistoryById_shouldReturn404IfPractitionerIdIsWrong() throws Exception {
		MockHttpServletResponse response = get("/Practitioner/" + WRONG_PRACTITIONER_UUID + "/_history")
		        .accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isNotFound());
	}
	
	private MockHttpServletResponse getPractitionerHistoryByIdRequest() throws IOException, ServletException {
		return get("/Practitioner/" + PRACTITIONER_UUID + "/_history").accept(FhirMediaTypes.JSON).go();
	}
	
	private void verifyUri(String uri) throws Exception {
		Practitioner practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		when(practitionerService.searchForPractitioners(any(), any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(practitioner), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isOk());
		MatcherAssert.assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
	}
	
}
