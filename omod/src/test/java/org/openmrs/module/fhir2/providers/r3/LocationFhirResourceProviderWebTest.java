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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hamcrest.Matchers;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Location;
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
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class LocationFhirResourceProviderWebTest extends BaseFhirR3ResourceProviderWebTest<LocationFhirResourceProvider, Location> {
	
	private static final String LOCATION_UUID = "9e0d2e27-541f-435d-aebd-81eed8acc56b";
	
	private static final String WRONG_LOCATION_UUID = "bac45a1f-a4c6-4d56-9743-f407c8ef27b7";
	
	private static final String LOCATION_NAME = "Ngeria";
	
	private static final String CITY = "Test City";
	
	private static final String COUNTRY = "Kenya";
	
	private static final String STATE = "Pan villa";
	
	private static final String POSTAL_CODE = "234-30100";
	
	private static final String LOGIN_LOCATION_TAG_NAME = "login";
	
	private static final String PARENT_LOCATION_NAME = "Test parent location";
	
	private static final String PARENT_LOCATION_ID = "6bdfdc53-f828-4c7d-b127-e9b49574ef24";
	
	private static final String PARENT_LOCATION_CITY = "Test parent city";
	
	private static final String PARENT_LOCATION_STATE = "Test parent state";
	
	private static final String PARENT_LOCATION_COUNTRY = "Test parent country";
	
	private static final String PARENT_LOCATION_POSTAL_CODE = "Test parent postal code";
	
	private static final String LAST_UPDATED_DATE = "eq2020-09-03";
	
	private static final String JSON_CREATE_LOCATION_PATH = "org/openmrs/module/fhir2/providers/LocationWebTest_create.json";
	
	private static final String JSON_UPDATE_LOCATION_PATH = "org/openmrs/module/fhir2/providers/LocationWebTest_update.json";
	
	private static final String JSON_UPDATE_LOCATION_NO_ID_PATH = "org/openmrs/module/fhir2/providers/LocationWebTest_updateWithoutId.json";
	
	private static final String JSON_UPDATE_LOCATION_WRONG_ID_PATH = "org/openmrs/module/fhir2/providers/LocationWebTest_updateWithWrongId.json";
	
	@Mock
	private FhirLocationService locationService;
	
	@Getter(AccessLevel.PUBLIC)
	private LocationFhirResourceProvider locationProvider;
	
	@Captor
	private ArgumentCaptor<TokenAndListParam> tokenAndListParamArgumentCaptor;
	
	@Captor
	private ArgumentCaptor<StringAndListParam> stringAndListParamCaptor;
	
	@Captor
	private ArgumentCaptor<ReferenceAndListParam> referenceAndListParamCaptor;
	
	@Captor
	private ArgumentCaptor<DateRangeParam> dateRangeParamArgumentCaptor;
	
	@Captor
	private ArgumentCaptor<HashSet<Include>> includeArgumentCaptor;
	
	@Before
	@Override
	public void setup() throws ServletException {
		locationProvider = new LocationFhirResourceProvider();
		locationProvider.setLocationService(locationService);
		super.setup();
	}
	
	@Override
	public LocationFhirResourceProvider getResourceProvider() {
		return locationProvider;
	}
	
	@Test
	public void getLocationById_shouldReturnLocationWithMatchingUuid() throws Exception {
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
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
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(stringAndListParamCaptor.getValue(), notNullValue());
		assertThat(
		    stringAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(LOCATION_NAME));
	}
	
	@Test
	public void findLocationsByCity_shouldReturnBundleOfLocationsWithMatchingCity() throws Exception {
		verifyURI(String.format("/Location?address-city=%s", CITY));
		
		verify(locationService).searchForLocations(isNull(), stringAndListParamCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(stringAndListParamCaptor.getValue(), notNullValue());
		assertThat(
		    stringAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(CITY));
	}
	
	@Test
	public void findLocationsByCountry_shouldReturnBundleOfLocationsWithMatchingCountry() throws Exception {
		verifyURI(String.format("/Location?address-country=%s", COUNTRY));
		
		verify(locationService).searchForLocations(isNull(), isNull(), stringAndListParamCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(stringAndListParamCaptor.getValue(), notNullValue());
		assertThat(
		    stringAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(COUNTRY));
	}
	
	@Test
	public void findLocationsByPostalCode_shouldReturnBundleOfLocationsWithMatchingAddressCode() throws Exception {
		verifyURI(String.format("/Location?address-postalcode=%s", POSTAL_CODE));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), stringAndListParamCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(stringAndListParamCaptor.getValue(), notNullValue());
		assertThat(
		    stringAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(POSTAL_CODE));
	}
	
	@Test
	public void findLocationsByState_shouldReturnBundleOfLocationsWithMatchingAddressState() throws Exception {
		verifyURI(String.format("/Location?address-state=%s", STATE));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(),
		    stringAndListParamCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(stringAndListParamCaptor.getValue(), notNullValue());
		assertThat(
		    stringAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(STATE));
	}
	
	@Test
	public void findLocationsByTag_shouldReturnBundleOfLocationsWithMatchingTag() throws Exception {
		verifyURI(String.format("/Location?_tag=%s", LOGIN_LOCATION_TAG_NAME));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(),
		    tokenAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(LOGIN_LOCATION_TAG_NAME));
	}
	
	@Test
	public void findLocationsByParent_shouldReturnBundleOfLocationsWithMatchingParentId() throws Exception {
		verifyURI(String.format("/Location?partof=%s", PARENT_LOCATION_ID));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    referenceAndListParamCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PARENT_LOCATION_ID));
	}
	
	@Test
	public void findLocationsByParent_shouldReturnBundleOfLocationsWithMatchingParentName() throws Exception {
		verifyURI(String.format("/Location?partof.name=%s", PARENT_LOCATION_NAME));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    referenceAndListParamCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PARENT_LOCATION_NAME));
	}
	
	@Test
	public void findLocationsByParent_shouldReturnBundleOfLocationsWithMatchingParentCity() throws Exception {
		verifyURI(String.format("/Location?partof.address-city=%s", PARENT_LOCATION_CITY));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    referenceAndListParamCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PARENT_LOCATION_CITY));
	}
	
	@Test
	public void findLocationsByParent_shouldReturnBundleOfLocationsWithMatchingParentCountry() throws Exception {
		verifyURI(String.format("/Location?partof.address-country=%s", PARENT_LOCATION_COUNTRY));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    referenceAndListParamCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PARENT_LOCATION_COUNTRY));
	}
	
	@Test
	public void findLocationsByParent_shouldReturnBundleOfLocationsWithMatchingParentPostalCode() throws Exception {
		verifyURI(String.format("/Location?partof.address-postalcode=%s", PARENT_LOCATION_POSTAL_CODE));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    referenceAndListParamCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PARENT_LOCATION_POSTAL_CODE));
	}
	
	@Test
	public void findLocationsByParent_shouldReturnBundleOfLocationsWithMatchingParentState() throws Exception {
		verifyURI(String.format("/Location?partof.address-state=%s", PARENT_LOCATION_STATE));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    referenceAndListParamCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PARENT_LOCATION_STATE));
	}
	
	@Test
	public void findLocationsByUUID_shouldReturnBundleOfLocationsWithMatchingUUID() throws Exception {
		verifyURI(String.format("/Location?_id=%s", LOCATION_UUID));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    tokenAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(LOCATION_UUID));
	}
	
	@Test
	public void findLocationsByLastUpdatedDate_shouldReturnBundleOfLocationsWithMatchingLastUpdatedDate() throws Exception {
		verifyURI(String.format("/Location?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), dateRangeParamArgumentCaptor.capture(), isNull(), isNull(), isNull());
		
		assertThat(dateRangeParamArgumentCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(dateRangeParamArgumentCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeParamArgumentCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void findLocationsByInclude_shouldReturnBundleOfLocationsWithIncludedResources() throws Exception {
		verifyURI("/Location?_include=Location:partof");
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), includeArgumentCaptor.capture(), isNull(), isNull());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PART_OF_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(), equalTo(FhirConstants.LOCATION));
	}
	
	@Test
	public void findLocationsByReverseInclude_shouldReturnBundleOfLocationsWithReverseIncludedLocations() throws Exception {
		verifyURI("/Location?_revinclude=Location:partof");
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), includeArgumentCaptor.capture(), isNull());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PART_OF_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(), equalTo(FhirConstants.LOCATION));
	}
	
	@Test
	public void findLocationsByReverseInclude_shouldReturnBundleOfLocationsWithReverseIncludedEncounters() throws Exception {
		verifyURI("/Location?_revinclude=Encounter:location");
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), includeArgumentCaptor.capture(), isNull());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_LOCATION_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(), equalTo(FhirConstants.ENCOUNTER));
	}
	
	@Test
	public void findLocationsByReverseInclude_shouldHandleMultipleReverseIncludes() throws Exception {
		verifyURI("/Location?_revinclude=Encounter:location&_revinclude=Location:partof");
		
		verify(locationService).searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), includeArgumentCaptor.capture(), isNull());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(2));
		
		assertThat(includeArgumentCaptor.getValue(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_LOCATION_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.ENCOUNTER)))));
		assertThat(includeArgumentCaptor.getValue(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_PART_OF_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.LOCATION)))));
	}
	
	@Test
	public void shouldGetLocationByComplexQuery() throws Exception {
		verifyURI(String.format("/Location?name=%s&partof.address-city=%s", LOCATION_NAME, PARENT_LOCATION_CITY));
		
		verify(locationService).searchForLocations(stringAndListParamCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), referenceAndListParamCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListParamCaptor.getValue(), notNullValue());
		assertThat(
		    stringAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(LOCATION_NAME));
		
		assertThat(referenceAndListParamCaptor.getValue(), notNullValue());
		assertThat(referenceAndListParamCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(PARENT_LOCATION_CITY));
	}
	
	@Test
	public void shouldReturn404IfLocationNotFound() throws Exception {
		when(locationService.get(WRONG_LOCATION_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/Location/" + WRONG_LOCATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void shouldVerifyGetLocationHistoryByIdUri() throws Exception {
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
		location.setId(LOCATION_UUID);
		when(locationService.get(LOCATION_UUID)).thenReturn(location);
		
		MockHttpServletResponse response = getLocationHistoryByIdRequest();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void shouldGetLocationHistoryById() throws IOException, ServletException {
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.newUuid()));
		provenance.setRecorded(new Date());
		provenance.setActivity(new CodeableConcept().addCoding(
		    new Coding().setCode("CREATE").setSystem(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION).setDisplay("create")));
		provenance.addAgent(new Provenance.ProvenanceAgentComponent()
		        .setType(
		            new CodeableConcept().addCoding(new Coding().setCode(FhirConstants.AUT).setDisplay(FhirConstants.AUTHOR)
		                    .setSystem(FhirConstants.FHIR_TERMINOLOGY_PROVENANCE_PARTICIPANT_TYPE)))
		        .addRole(new CodeableConcept().addCoding(
		            new Coding().setCode("").setDisplay("").setSystem(FhirConstants.FHIR_TERMINOLOGY_PARTICIPATION_TYPE))));
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
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
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
		location.setId(LOCATION_UUID);
		location.setContained(new ArrayList<>());
		when(locationService.get(LOCATION_UUID)).thenReturn(location);
		
		MockHttpServletResponse response = getLocationHistoryByIdRequest();
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(false));
	}
	
	@Test
	public void getLocationHistoryById_shouldReturn404IfPractitionerIdIsWrong() throws Exception {
		MockHttpServletResponse response = get("/Location/" + WRONG_LOCATION_UUID + "/_history").accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isNotFound());
	}
	
	private MockHttpServletResponse getLocationHistoryByIdRequest() throws IOException, ServletException {
		return get("/Location/" + LOCATION_UUID + "/_history").accept(FhirMediaTypes.JSON).go();
	}
	
	private void verifyURI(String uri) throws Exception {
		Location location = new Location();
		location.setId(LOCATION_UUID);
		when(locationService.searchForLocations(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
		    any())).thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void createLocation_shouldCreateLocation() throws Exception {
		String jsonLocation;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_LOCATION_PATH)) {
			Objects.requireNonNull(is);
			jsonLocation = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
		location.setId(LOCATION_UUID);
		
		when(locationService.create(any(org.hl7.fhir.r4.model.Location.class))).thenReturn(location);
		
		MockHttpServletResponse response = post("/Location").jsonContent(jsonLocation).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isCreated());
	}
	
	@Test
	public void updateLocation_shouldUpdateExistingLocation() throws Exception {
		String jsonLocation;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_LOCATION_PATH)) {
			Objects.requireNonNull(is);
			jsonLocation = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
		location.setId(LOCATION_UUID);
		
		when(locationService.update(anyString(), any(org.hl7.fhir.r4.model.Location.class))).thenReturn(location);
		
		MockHttpServletResponse response = put("/Location/" + LOCATION_UUID).jsonContent(jsonLocation)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
	}
	
	@Test
	public void updateLocation_shouldThrowErrorForNoId() throws Exception {
		String jsonLocation;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_LOCATION_NO_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonLocation = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		MockHttpServletResponse response = put("/Location/" + LOCATION_UUID).jsonContent(jsonLocation)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	public void updateLocation_shouldThrowErrorForIdMissMatch() throws Exception {
		String jsonLocation;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_LOCATION_WRONG_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonLocation = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		MockHttpServletResponse response = put("/Location/" + WRONG_LOCATION_UUID).jsonContent(jsonLocation)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    containsStringIgnoringCase("body must contain an ID element which matches the request URL"));
	}
	
	@Test
	public void deleteLocation_shouldDeleteLocation() throws Exception {
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
		location.setId(LOCATION_UUID);
		
		when(locationService.delete(LOCATION_UUID)).thenReturn(location);
		
		MockHttpServletResponse response = delete("/Location/" + LOCATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
}
