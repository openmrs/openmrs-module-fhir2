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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Address;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.LocationTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirLocationServiceImplTest {
	
	private static final String LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
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
	FhirLocationDao locationDao;
	
	@Mock
	LocationTranslator locationTranslator;
	
	@Mock
	SearchQueryInclude<org.hl7.fhir.r4.model.Location> searchQueryInclude;
	
	@Mock
	SearchQuery<Location, org.hl7.fhir.r4.model.Location, FhirLocationDao, LocationTranslator, SearchQueryInclude<org.hl7.fhir.r4.model.Location>> searchQuery;
	
	private FhirLocationServiceImpl fhirLocationService;
	
	private Location location;
	
	private org.hl7.fhir.r4.model.Location fhirLocation;
	
	@Before
	public void setUp() {
		fhirLocationService = new FhirLocationServiceImpl();
		fhirLocationService.setDao(locationDao);
		fhirLocationService.setTranslator(locationTranslator);
		fhirLocationService.setSearchQuery(searchQuery);
		fhirLocationService.setSearchQueryInclude(searchQueryInclude);
		
		location = new Location();
		location.setUuid(LOCATION_UUID);
		location.setName(LOCATION_NAME);
		location.setDescription(LOCATION_DESCRIPTION);
		location.setDateCreated(new Date());
		location.setRetired(false);
		Set<LocationTag> locationTags = new HashSet<>();
		locationTags.add(new LocationTag(LOGIN_LOCATION_TAG_NAME, LOGIN_LOCATION_TAG_DESCRIPTION));
		location.setTags(locationTags);
		
		fhirLocation = new org.hl7.fhir.r4.model.Location();
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
		List<Location> locations = new ArrayList<>();
		locations.add(location);
		
		SearchParameterMap theParams = new SearchParameterMap();
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(locationDao.getResultUuids(any())).thenReturn(Collections.singletonList(LOCATION_UUID));
		when(searchQuery.getQueryResults(any(), any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, locationDao, locationTranslator, searchQueryInclude));
		when(locationTranslator.toFhirResource(location)).thenReturn(fhirLocation);
		when(locationDao.search(any(), any(), anyInt(), anyInt())).thenReturn(locations);
		
		IBundleProvider results = fhirLocationService.searchForLocations(null, null, null, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(LOCATION_UUID))));
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
}
