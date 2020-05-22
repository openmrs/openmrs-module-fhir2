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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
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

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.hamcrest.Matchers;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Provenance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.openmrs.module.fhir2.web.servlet.BaseFhirResourceProviderTest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class LocationFhirResourceProviderWebTest extends BaseFhirResourceProviderTest<LocationFhirResourceProvider, Location> {
	
	private static final String LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String WRONG_LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432aabd";
	
	private static final String LOCATION_NAME = "Ngeria";
	
	private static final String CITY = "Test City";
	
	private static final String COUNTRY = "Kenya";
	
	private static final String STATE = "Pan villa";
	
	private static final String POSTAL_CODE = "234-30100";
	
	private static final String SYSTEM = "https://fhir.openmrs.org/ext/location-tag";
	
	private static final String LOGIN_LOCATION_TAG_NAME = "login";
	
	private static final String LOGIN_LOCATION_TAG_DESCRIPTION = "Identify login locations";
	
	private static final String PARENT_LOCATION_NAME = "Test parent location";
	
	private static final String PARENT_LOCATION_ID = "c0938432-1691-11df-97a5-7038c432aabe";
	
	private static final String PARENT_LOCATION_CITY = "Test parent city";
	
	private static final String PARENT_LOCATION_STATE = "Test parent state";
	
	private static final String PARENT_LOCATION_COUNTRY = "Test parent country";
	
	private static final String PARENT_LOCATION_POSTAL_CODE = "Test parent postal code";
	
	@Mock
	private FhirLocationService locationService;
	
	@Getter(AccessLevel.PUBLIC)
	private LocationFhirResourceProvider locationProvider;
	
	@Captor
	ArgumentCaptor<TokenAndListParam> tagCaptor;
	
	@Captor
	ArgumentCaptor<StringAndListParam> stringAndListParamCaptor;
	
	@Captor
	ArgumentCaptor<ReferenceAndListParam> referenceAndListParamCaptor;
	
	@Before
	@Override
	public void setup() throws Exception {
		locationProvider = new LocationFhirResourceProvider();
		locationProvider.setFhirLocationService(locationService);
		super.setup();
	}
	
	@Override
	public LocationFhirResourceProvider getResourceProvider() {
		return locationProvider;
	}
	
	@Test
	public void getLocationById_shouldReturnLocationWithMatchingUuid() throws Exception {
		Location location = new Location();
		location.setId(LOCATION_UUID);
		when(locationService.get(LOCATION_UUID)).thenReturn(location);
		
		MockHttpServletResponse response = get("/Location/" + LOCATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Location resource = readResponse(response);
		assertThat(resource.getIdElement().getIdPart(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void findLocationByName_shouldReturnBundleOfLocationsWithMatchingName() throws Exception {
		verifyURI(String.format("/Location?name=%s", LOCATION_NAME));
		
		verify(locationService).searchForLocations(stringAndListParamCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull());
		assertThat(stringAndListParamCaptor.getValue(), notNullValue());
		assertThat(
		    stringAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(LOCATION_NAME));
	}
	
	@Test
	public void findLocationsByCity_shouldReturnBundleOfLocationsWithMatchingCity() throws Exception {
		verifyURI(String.format("/Location?address-city=%s", CITY));
		
		verify(locationService).searchForLocations(isNull(), stringAndListParamCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull());
		assertThat(stringAndListParamCaptor.getValue(), notNullValue());
		assertThat(
		    stringAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(CITY));
	}
	
	@Test
	public void findLocationsByCountry_shouldReturnBundleOfLocationsWithMatchingCountry() throws Exception {
		verifyURI(String.format("/Location?address-country=%s", COUNTRY));
		
		verify(locationService).searchForLocations(isNull(), isNull(), stringAndListParamCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull(), isNull());
		assertThat(stringAndListParamCaptor.getValue(), notNullValue());
		assertThat(
		    stringAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(COUNTRY));
	}
	
	@Test
	public void findLocationsByPostalCode_shouldReturnBundleOfLocationsWithMatchingAddressCode() throws Exception {
		verifyURI(String.format("/Location?address-postalcode=%s", POSTAL_CODE));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), stringAndListParamCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull());
		assertThat(stringAndListParamCaptor.getValue(), notNullValue());
		assertThat(
		    stringAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(POSTAL_CODE));
	}
	
	@Test
	public void findLocationsByState_shouldReturnBundleOfLocationsWithMatchingAddressState() throws Exception {
		verifyURI(String.format("/Location?address-state=%s", STATE));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(),
		    stringAndListParamCaptor.capture(), isNull(), isNull(), isNull());
		assertThat(stringAndListParamCaptor.getValue(), notNullValue());
		assertThat(
		    stringAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(STATE));
	}
	
	@Test
	public void findLocationsByTag_shouldReturnBundleOfLocationsWithMatchingTag() throws Exception {
		Location location = new Location();
		location.getMeta().setTag(Collections.singletonList(new Coding(FhirConstants.OPENMRS_FHIR_EXT_LOCATION_TAG,
		        LOGIN_LOCATION_TAG_NAME, LOGIN_LOCATION_TAG_DESCRIPTION)));
		
		when(locationService.searchForLocations(any(), any(), any(), any(), any(), any(TokenAndListParam.class), any(),
		    any())).thenReturn(Collections.singletonList(location));
		
		MockHttpServletResponse response = get("/Location?_tag=" + LOGIN_LOCATION_TAG_NAME).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry().size(), greaterThanOrEqualTo(1));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), tagCaptor.capture(),
		    isNull(), isNull());
		assertThat(tagCaptor.getValue(), notNullValue());
		assertThat(tagCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(LOGIN_LOCATION_TAG_NAME));
	}
	
	@Test
	public void findLocationsByParent_shouldReturnBundleOfLocationsWithMatchingParentId() throws Exception {
		verifyURI(String.format("/Location?partof=%s", PARENT_LOCATION_ID));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    referenceAndListParamCaptor.capture(), isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PARENT_LOCATION_ID));
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo(null));
	}
	
	@Test
	public void findLocationsByParent_shouldReturnBundleOfLocationsWithMatchingParentName() throws Exception {
		verifyURI(String.format("/Location?partof.name=%s", PARENT_LOCATION_NAME));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    referenceAndListParamCaptor.capture(), isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PARENT_LOCATION_NAME));
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo("name"));
	}
	
	@Test
	public void findLocationsByParent_shouldReturnBundleOfLocationsWithMatchingParentCity() throws Exception {
		verifyURI(String.format("/Location?partof.address-city=%s", PARENT_LOCATION_CITY));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    referenceAndListParamCaptor.capture(), isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PARENT_LOCATION_CITY));
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo("address-city"));
	}
	
	@Test
	public void findLocationsByParent_shouldReturnBundleOfLocationsWithMatchingParentCountry() throws Exception {
		verifyURI(String.format("/Location?partof.address-country=%s", PARENT_LOCATION_COUNTRY));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    referenceAndListParamCaptor.capture(), isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PARENT_LOCATION_COUNTRY));
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo("address-country"));
	}
	
	@Test
	public void findLocationsByParent_shouldReturnBundleOfLocationsWithMatchingParentPostalCode() throws Exception {
		verifyURI(String.format("/Location?partof.address-postalcode=%s", PARENT_LOCATION_POSTAL_CODE));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    referenceAndListParamCaptor.capture(), isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PARENT_LOCATION_POSTAL_CODE));
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo("address-postalcode"));
	}
	
	@Test
	public void findLocationsByParent_shouldReturnBundleOfLocationsWithMatchingParentState() throws Exception {
		verifyURI(String.format("/Location?partof.address-state=%s", PARENT_LOCATION_STATE));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    referenceAndListParamCaptor.capture(), isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PARENT_LOCATION_STATE));
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo("address-state"));
	}
	
	@Test
	public void shouldGetLocationByComplexQuery() throws Exception {
		verifyURI(String.format("/Location?name=%s&partof.address-city=%s", LOCATION_NAME, PARENT_LOCATION_CITY));
		
		verify(locationService).searchForLocations(stringAndListParamCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), referenceAndListParamCaptor.capture(), isNull());
		
		assertThat(stringAndListParamCaptor.getValue(), notNullValue());
		assertThat(
		    stringAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(LOCATION_NAME));
		
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PARENT_LOCATION_CITY));
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getChain(),
		    equalTo("address-city"));
	}
	
	@Test
	public void shouldReturn404IfLocationNotFound() throws Exception {
		when(locationService.get(WRONG_LOCATION_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/Location/" + WRONG_LOCATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void shouldVerifyGetLocationHistoryByIdUri() throws Exception {
		Location location = new Location();
		location.setId(LOCATION_UUID);
		when(locationService.get(LOCATION_UUID)).thenReturn(location);
		
		MockHttpServletResponse response = getLocationHistoryByIdRequest();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(BaseFhirResourceProviderTest.FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void shouldGetLocationHistoryById() throws IOException, ServletException {
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
		Location location = new Location();
		location.setId(LOCATION_UUID);
		location.addContained(provenance);
		
		when(locationService.get(LOCATION_UUID)).thenReturn(location);
		
		MockHttpServletResponse response = getLocationHistoryByIdRequest();
		
		Bundle results = readBundleResponse(response);
		assertThat(results, Matchers.notNullValue());
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), Matchers.notNullValue());
		assertThat(results.getEntry().get(0).getResource().getResourceType().name(),
		    equalTo(Provenance.class.getSimpleName()));
		
	}
	
	@Test
	public void getLocationHistoryById_shouldReturnBundleWithEmptyEntriesIfPractitionerContainedIsEmpty() throws Exception {
		Location location = new Location();
		location.setId(LOCATION_UUID);
		location.setContained(new ArrayList<>());
		when(locationService.get(LOCATION_UUID)).thenReturn(location);
		
		MockHttpServletResponse response = getLocationHistoryByIdRequest();
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(false));
	}
	
	@Test
	public void getLocationHistoryById_shouldReturn404IfPractitionerIdIsWrong() throws Exception {
		MockHttpServletResponse response = get("/Location/" + WRONG_LOCATION_UUID + "/_history")
		        .accept(BaseFhirResourceProviderTest.FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	private MockHttpServletResponse getLocationHistoryByIdRequest() throws IOException, ServletException {
		return get("/Location/" + LOCATION_UUID + "/_history").accept(BaseFhirResourceProviderTest.FhirMediaTypes.JSON).go();
	}
	
	private void verifyURI(String uri) throws Exception {
		Location location = new Location();
		location.setId(LOCATION_UUID);
		when(locationService.searchForLocations(any(), any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(Collections.singleton(location));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry().size(), greaterThanOrEqualTo(1));
	}
}
