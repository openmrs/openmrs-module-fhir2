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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import ca.uhn.fhir.rest.param.TokenParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.hamcrest.CoreMatchers;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirLocationService;
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
	
	@Mock
	private FhirLocationService locationService;
	
	@Getter(AccessLevel.PUBLIC)
	private LocationFhirResourceProvider locationProvider;
	
	@Captor
	ArgumentCaptor<TokenParam> tagCaptor;
	
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
		when(locationService.getLocationByUuid(LOCATION_UUID)).thenReturn(location);
		
		MockHttpServletResponse response = get("/Location/" + LOCATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Location resource = readResponse(response);
		assertThat(resource.getIdElement().getIdPart(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void findLocationByName_shouldReturnBundleOfLocationsWithMatchingName() throws Exception {
		Location location = new Location();
		location.setName(LOCATION_NAME);
		when(locationService.findLocationByName(LOCATION_NAME)).thenReturn(Collections.singletonList(location));
		
		MockHttpServletResponse response = get("/Location?name=" + LOCATION_NAME).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findLocationsByCity_shouldReturnBundleOfLocationsWithMatchingCity() throws Exception {
		Location location = new Location();
		location.setAddress(new Address().setCity(CITY));
		when(locationService.findLocationsByCity(CITY)).thenReturn(Collections.singleton(location));
		
		MockHttpServletResponse response = get("/Location?address-city=" + CITY).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findLocationsByCountry_shouldReturnBundleOfLocationsWithMatchingCountry() throws Exception {
		Location location = new Location();
		location.setAddress(new Address().setCountry(COUNTRY));
		when(locationService.findLocationsByCountry(COUNTRY)).thenReturn(Collections.singleton(location));
		
		MockHttpServletResponse response = get("/Location?address-country=" + COUNTRY).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findLocationsByPostalCode_shouldReturnBundleOfLocationsWithMatchingAddressCode() throws Exception {
		Location location = new Location();
		location.setAddress(new Address().setPostalCode(POSTAL_CODE));
		when(locationService.findLocationsByPostalCode(POSTAL_CODE)).thenReturn(Collections.singleton(location));
		
		MockHttpServletResponse response = get("/Location?address-postalcode=" + POSTAL_CODE).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findLocationsByState_shouldReturnBundleOfLocationsWithMatchingAddressState() throws Exception {
		Location location = new Location();
		location.setAddress(new Address().setCountry(STATE));
		when(locationService.findLocationsByState(STATE)).thenReturn(Collections.singleton(location));
		
		MockHttpServletResponse response = get("/Location?address-state=" + STATE).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findLocationsByTag_shouldReturnBundleOfLocationsWithMatchingTag() throws Exception {
		Location location = new Location();
		location.getMeta().setTag(Collections.singletonList(new Coding(FhirConstants.OPENMRS_FHIR_EXT_LOCATION_TAG,
		        LOGIN_LOCATION_TAG_NAME, LOGIN_LOCATION_TAG_DESCRIPTION)));
		
		when(locationService.findLocationsByTag(any(TokenParam.class))).thenReturn(Collections.singletonList(location));
		
		MockHttpServletResponse response = get("/Location?_tag=" + LOGIN_LOCATION_TAG_NAME).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry().size(), greaterThanOrEqualTo(1));
		
		verify(locationService).findLocationsByTag(tagCaptor.capture());
		assertThat(tagCaptor.getValue(), notNullValue());
		assertThat(tagCaptor.getValue().getValue(), CoreMatchers.equalTo(LOGIN_LOCATION_TAG_NAME));
	}
	
	@Test
	public void shouldReturn404IfLocationNotFound() throws Exception {
		when(locationService.getLocationByUuid(WRONG_LOCATION_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/Location/" + WRONG_LOCATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
}
