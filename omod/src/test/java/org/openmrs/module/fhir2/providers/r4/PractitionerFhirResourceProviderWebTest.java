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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import javax.servlet.ServletException;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;

import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang.time.DateUtils;
import org.hamcrest.MatcherAssert;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Practitioner;
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
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.fhir2.api.search.param.PractitionerSearchParams;
import org.powermock.reflect.Whitebox;
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
	
	private static final String LAST_UPDATED_DATE = "eq2020-09-03";
	
	private static final String JSON_CREATE_PRACTITIONER_PATH = "org/openmrs/module/fhir2/providers/PractitionerWebTest_create.json";
	
	private static final String JSON_UPDATE_PRACTITIONER_PATH = "org/openmrs/module/fhir2/providers/PractitionerWebTest_update.json";
	
	private static final String JSON_UPDATE_PRACTITIONER_NO_ID_PATH = "org/openmrs/module/fhir2/providers/PractitionerWebTest_updateWithoutId.json";
	
	private static final String JSON_UPDATE_PRACTITIONER_WRONG_ID_PATH = "org/openmrs/module/fhir2/providers/PractitionerWebTest_updateWithWrongId.json";
	
	@Getter(AccessLevel.PUBLIC)
	private PractitionerFhirResourceProvider resourceProvider;
	
	@Mock
	private FhirPractitionerService practitionerService;
	
	@Mock
	private FhirGlobalPropertyService fhirGpService;
	
	@Captor
	private ArgumentCaptor<PractitionerSearchParams> practitionerSearchParamsArgumentCaptor;
	
	@Before
	@Override
	public void setup() throws ServletException {
		resourceProvider = new PractitionerFhirResourceProvider();
		resourceProvider.setPractitionerService(practitionerService);
		Whitebox.setInternalState(BaseUpsertFhirResourceProvider.class, "globalPropsService", fhirGpService);
		super.setup();
	}
	
	@After
	public void tearDown() {
		Whitebox.setInternalState(BaseUpsertFhirResourceProvider.class, "globalPropsService", (Object) null);
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
		
		verify(practitionerService).searchForPractitioners(practitionerSearchParamsArgumentCaptor.capture());
		StringAndListParam name = practitionerSearchParamsArgumentCaptor.getValue().getName();
		
		assertThat(name, notNullValue());
		assertThat(name.getValuesAsQueryTokens(), not(empty()));
		assertThat(name.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(), equalTo(NAME));
	}
	
	@Test
	public void findPractitionersByIdentifier_shouldReturnBundleOfPractitioners() throws Exception {
		verifyUri(String.format("/Practitioner?identifier=%s", PRACTITIONER_IDENTIFIER));
		
		verify(practitionerService).searchForPractitioners(practitionerSearchParamsArgumentCaptor.capture());
		
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getIdentifier(), notNullValue());
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getIdentifier().getValuesAsQueryTokens(), not(empty()));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getIdentifier().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PRACTITIONER_IDENTIFIER));
	}
	
	@Test
	public void findPractitionersByGivenName_shouldReturnBundleOfPractitioners() throws Exception {
		verifyUri(String.format("/Practitioner?given=%s", PRACTITIONER_GIVEN_NAME));
		
		verify(practitionerService).searchForPractitioners(practitionerSearchParamsArgumentCaptor.capture());
		
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getGiven(), notNullValue());
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getGiven().getValuesAsQueryTokens(), not(empty()));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getGiven().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PRACTITIONER_GIVEN_NAME));
	}
	
	@Test
	public void findPractitionersByFamilyName_shouldReturnBundleOfPractitioners() throws Exception {
		verifyUri(String.format("/Practitioner?family=%s", PRACTITIONER_FAMILY_NAME));
		
		verify(practitionerService).searchForPractitioners(practitionerSearchParamsArgumentCaptor.capture());
		
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getFamily(), notNullValue());
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getFamily().getValuesAsQueryTokens(), not(empty()));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getFamily().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PRACTITIONER_FAMILY_NAME));
	}
	
	@Test
	public void findPractitionersByAddressCity_shouldReturnBundleOfPractitioners() throws Exception {
		verifyUri(String.format("/Practitioner?address-city=%s", CITY));
		
		verify(practitionerService).searchForPractitioners(practitionerSearchParamsArgumentCaptor.capture());
		
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getCity(), notNullValue());
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getCity().getValuesAsQueryTokens(), not(empty()));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getCity().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(CITY));
	}
	
	@Test
	public void findPractitionersByAddressState_shouldReturnBundleOfPractitioners() throws Exception {
		verifyUri(String.format("/Practitioner?address-state=%s", STATE));
		
		verify(practitionerService).searchForPractitioners(practitionerSearchParamsArgumentCaptor.capture());
		
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getState(), notNullValue());
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getState().getValuesAsQueryTokens(), not(empty()));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getState().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(STATE));
	}
	
	@Test
	public void findPractitionersByAddressPostalCode_shouldReturnBundleOfPractitioners() throws Exception {
		verifyUri(String.format("/Practitioner?address-postalcode=%s", POSTAL_CODE));
		
		verify(practitionerService).searchForPractitioners(practitionerSearchParamsArgumentCaptor.capture());
		
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getPostalCode(), notNullValue());
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getPostalCode().getValuesAsQueryTokens(), not(empty()));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getPostalCode().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(POSTAL_CODE));
	}
	
	@Test
	public void findPractitionersByAddressCountry_shouldReturnBundleOfPractitioners() throws Exception {
		verifyUri(String.format("/Practitioner?address-country=%s", COUNTRY));
		
		verify(practitionerService).searchForPractitioners(practitionerSearchParamsArgumentCaptor.capture());
		
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getCountry(), notNullValue());
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getCountry().getValuesAsQueryTokens(), not(empty()));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getCountry().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(COUNTRY));
	}
	
	@Test
	public void findPractitionersByUUID_shouldReturnBundleOfPractitioners() throws Exception {
		verifyUri(String.format("/Practitioner?_id=%s", PRACTITIONER_UUID));
		
		verify(practitionerService).searchForPractitioners(practitionerSearchParamsArgumentCaptor.capture());
		
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getId(), notNullValue());
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getId().getValuesAsQueryTokens(), not(empty()));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getId().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void findPractitionersByLastUpdatedDate_shouldReturnBundleOfPractitioners() throws Exception {
		verifyUri(String.format("/Practitioner?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(practitionerService).searchForPractitioners(practitionerSearchParamsArgumentCaptor.capture());
		
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getLastUpdated(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getLastUpdated().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getLastUpdated().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void findPractitioners_shouldReverseIncludeEncounters() throws Exception {
		verifyUri("/Practitioner?_revinclude=Encounter:participant");
		
		verify(practitionerService).searchForPractitioners(practitionerSearchParamsArgumentCaptor.capture());
		
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes(), notNullValue());
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes().size(), equalTo(1));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PARTICIPANT_PARAM));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes().iterator().next().getParamType(),
		    equalTo(FhirConstants.ENCOUNTER));
	}
	
	@Test
	public void findPractitioners_shouldReverseIncludeMedicationRequests() throws Exception {
		verifyUri("/Practitioner?_revinclude=MedicationRequest:requester");
		
		verify(practitionerService).searchForPractitioners(practitionerSearchParamsArgumentCaptor.capture());
		
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes(), notNullValue());
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes().size(), equalTo(1));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_REQUESTER_PARAM));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes().iterator().next().getParamType(),
		    equalTo(FhirConstants.MEDICATION_REQUEST));
	}
	
	@Test
	public void findPractitioners_shouldReverseIncludeServiceRequests() throws Exception {
		verifyUri("/Practitioner?_revinclude=ServiceRequest:requester");
		
		verify(practitionerService).searchForPractitioners(practitionerSearchParamsArgumentCaptor.capture());
		
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes(), notNullValue());
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes().size(), equalTo(1));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_REQUESTER_PARAM));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes().iterator().next().getParamType(),
		    equalTo(FhirConstants.SERVICE_REQUEST));
	}
	
	@Test
	public void findPractitioners_shouldHandleMultipleReverseIncludes() throws Exception {
		verifyUri("/Practitioner?_revinclude=ServiceRequest:requester&_revinclude=Encounter:participant");
		
		verify(practitionerService).searchForPractitioners(practitionerSearchParamsArgumentCaptor.capture());
		
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes(), notNullValue());
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes().size(), equalTo(2));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_REQUESTER_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.SERVICE_REQUEST)))));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_PARTICIPANT_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.ENCOUNTER)))));
	}
	
	@Test
	public void findPractitioners_shouldAddReverseIncludedMedicationRequestsWithIterativeMedicationDispenseToReturnedResults()
	        throws Exception {
		verifyUri("/Practitioner?_revinclude=MedicationRequest:requester&_revinclude=MedicationDispense:prescription");
		
		verify(practitionerService).searchForPractitioners(practitionerSearchParamsArgumentCaptor.capture());
		
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes(), notNullValue());
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes().size(), equalTo(2));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_REQUESTER_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.MEDICATION_REQUEST)))));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getRevIncludes(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_PRESCRIPTION_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.MEDICATION_DISPENSE)))));
	}
	
	@Test
	public void findPractitioners_shouldHandleComplexQuery() throws Exception {
		verifyUri(String.format("/Practitioner?identifier=%s&name=%s", PRACTITIONER_IDENTIFIER, NAME));
		
		verify(practitionerService).searchForPractitioners(practitionerSearchParamsArgumentCaptor.capture());
		
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getName(), notNullValue());
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getName().getValuesAsQueryTokens(), not(empty()));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getName().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(NAME));
		
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getIdentifier(), notNullValue());
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getIdentifier().getValuesAsQueryTokens(), not(empty()));
		assertThat(practitionerSearchParamsArgumentCaptor.getValue().getIdentifier().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PRACTITIONER_IDENTIFIER));
	}
	
	private void verifyUri(String uri) throws Exception {
		Practitioner practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		when(practitionerService.searchForPractitioners(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(practitioner), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isOk());
		MatcherAssert.assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void createPractitioner_shouldCreatePractitioner() throws Exception {
		String jsonPractitioner;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_PRACTITIONER_PATH)) {
			Objects.requireNonNull(is);
			jsonPractitioner = inputStreamToString(is, UTF_8);
		}
		org.hl7.fhir.r4.model.Practitioner practitioner = new org.hl7.fhir.r4.model.Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		
		when(practitionerService.create(any(org.hl7.fhir.r4.model.Practitioner.class))).thenReturn(practitioner);
		
		MockHttpServletResponse response = post("/Practitioner").jsonContent(jsonPractitioner).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isCreated());
	}
	
	@Test
	public void updatePractitioner_shouldUpdateExistingPractitioner() throws Exception {
		String jsonPractitioner;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_PRACTITIONER_PATH)) {
			Objects.requireNonNull(is);
			jsonPractitioner = inputStreamToString(is, UTF_8);
		}
		
		org.hl7.fhir.r4.model.Practitioner practitioner = new org.hl7.fhir.r4.model.Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		
		lenient().when(practitionerService.update(anyString(), any(org.hl7.fhir.r4.model.Practitioner.class)))
		        .thenReturn(practitioner);
		
		MockHttpServletResponse response = put("/Practitioner/" + PRACTITIONER_UUID).jsonContent(jsonPractitioner)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
	}
	
	@Test
	public void updatePractitioner_shouldErrorForNoId() throws Exception {
		String jsonPractitioner;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_PRACTITIONER_NO_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonPractitioner = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = put("/Practitioner/" + PRACTITIONER_UUID).jsonContent(jsonPractitioner)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	public void updatePractitioner_shouldErrorForIdMissMatch() throws Exception {
		String jsonPractitioner;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_PRACTITIONER_WRONG_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonPractitioner = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = put("/Practitioner/" + WRONG_PRACTITIONER_UUID).jsonContent(jsonPractitioner)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    containsStringIgnoringCase("body must contain an ID element which matches the request URL"));
	}
	
	@Test
	public void deletePractitioner_shouldDeletePractitioner() throws Exception {
		MockHttpServletResponse response = delete("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void deletePractitioner_shouldReturn404IfPractitionerNotFound() throws Exception {
		doThrow(new ResourceNotFoundException("")).when(practitionerService).delete(WRONG_PRACTITIONER_UUID);
		
		MockHttpServletResponse response = delete("/Practitioner/" + WRONG_PRACTITIONER_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
}
