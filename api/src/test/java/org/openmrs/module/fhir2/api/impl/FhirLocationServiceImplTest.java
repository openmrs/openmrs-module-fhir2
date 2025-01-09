/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.LocationTag;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.LocationSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.LocationTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirLocationServiceImplTest {
	
	private static final Integer LOCATION_ID = 123;
	
	private static final String LOCATION_UUID = "a1758922-b132-4ead-8ebe-5e2b4eaf43a1";
	
	private static final String LOCATION_NAME = "Test location 1";
	
	private static final String LOCATION_DESCRIPTION = "Test description";
	
	private static final String LOCATION_CITY = "Artuor";
	
	private static final String LOCATION_COUNTRY = "Kenya";
	
	private static final String POSTAL_CODE = "4015-3100";
	
	private static final String LOCATION_STATE = "province";
	
	private static final String LOGIN_LOCATION_TAG_NAME = "login";
	
	private static final String LOGIN_LOCATION_TAG_DESCRIPTION = "Identify login locations";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirLocationDao locationDao;
	
	@Mock
	private LocationTranslator locationTranslator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	SearchQueryInclude<Location> searchQueryInclude;
	
	@Mock
	SearchQuery<org.openmrs.Location, Location, FhirLocationDao, LocationTranslator, SearchQueryInclude<Location>> searchQuery;
	
	private FhirLocationServiceImpl fhirLocationService;
	
	private org.openmrs.Location location;
	
	private Location fhirLocation;
	
	@Before
	public void setUp() {
		fhirLocationService = new FhirLocationServiceImpl() {
			
			@Override
			protected void validateObject(org.openmrs.Location object) {
			}
		};
		
		fhirLocationService.setDao(locationDao);
		fhirLocationService.setTranslator(locationTranslator);
		fhirLocationService.setSearchQuery(searchQuery);
		fhirLocationService.setSearchQueryInclude(searchQueryInclude);
		
		location = new org.openmrs.Location();
		location.setUuid(LOCATION_UUID);
		location.setName(LOCATION_NAME);
		location.setDescription(LOCATION_DESCRIPTION);
		location.setDateCreated(new Date());
		location.setRetired(false);
		Set<LocationTag> locationTags = new HashSet<>();
		locationTags.add(new LocationTag(LOGIN_LOCATION_TAG_NAME, LOGIN_LOCATION_TAG_DESCRIPTION));
		location.setTags(locationTags);
		
		fhirLocation = new Location();
		fhirLocation.setId(LOCATION_UUID);
		fhirLocation.setName(LOCATION_NAME);
		fhirLocation.setDescription(LOCATION_DESCRIPTION);
		
		Address address = new Address();
		address.setCity(LOCATION_CITY);
		address.setPostalCode(POSTAL_CODE);
		address.setCountry(LOCATION_COUNTRY);
		address.setState(LOCATION_STATE);
		fhirLocation.setAddress(address);
	}
	
	@Test
	public void getLocationByUuid_shouldGetLocationByUuid() {
		when(locationDao.get(LOCATION_UUID)).thenReturn(location);
		when(locationTranslator.toFhirResource(location)).thenReturn(fhirLocation);
		
		org.hl7.fhir.r4.model.Location result = fhirLocationService.get(LOCATION_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(LOCATION_UUID));
		assertThat(result.getName(), equalTo(LOCATION_NAME));
		assertThat(result.getDescription(), equalTo(LOCATION_DESCRIPTION));
	}
	
	@Test
	public void searchForLocations_shouldReturnLocationsByParameters() {
		List<org.openmrs.Location> locations = new ArrayList<>();
		locations.add(location);
		
		SearchParameterMap theParams = new SearchParameterMap();
		
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        locationDao, locationTranslator, globalPropertyService, searchQueryInclude));
		
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(locationTranslator.toFhirResource(location)).thenReturn(fhirLocation);
		when(locationDao.getSearchResults(any())).thenReturn(locations);
		
		IBundleProvider results = fhirLocationService.searchForLocations(
		    new LocationSearchParams(null, null, null, null, null, null, null, null, null, null, null, null, null));
		
		assertThat(results, notNullValue());
		
		List<Location> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(LOCATION_UUID))));
	}
	
	private List<Location> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX).stream().filter(it -> it instanceof Location)
		        .map(it -> (Location) it).collect(Collectors.toList());
	}
}
