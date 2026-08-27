/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.handler;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;
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
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.LocationTranslator;

/**
 * Tests the CRUD/search wiring and dispatch predicates of the default Location handler.
 * Orchestrator-level concerns (fan-out, the typed {@code searchForLocations} entry point) live in
 * {@code FhirLocationServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LocationBackedLocationHandlerTest {
	
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
	private FhirLocationDao dao;
	
	@Mock
	private LocationTranslator translator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<Location> searchQueryInclude;
	
	@Mock
	private SearchQuery<org.openmrs.Location, Location, FhirLocationDao, LocationTranslator, SearchQueryInclude<Location>> searchQuery;
	
	private LocationBackedLocationHandler handler;
	
	private org.openmrs.Location location;
	
	private Location fhirLocation;
	
	@Before
	public void setUp() {
		handler = new LocationBackedLocationHandler() {
			
			@Override
			protected void validateObject(org.openmrs.Location object) {
			}
		};
		
		handler.setDao(dao);
		handler.setTranslator(translator);
		handler.setSearchQuery(searchQuery);
		handler.setSearchQueryInclude(searchQueryInclude);
		
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
	
	private List<Location> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX).stream().filter(it -> it instanceof Location)
		        .map(it -> (Location) it).collect(Collectors.toList());
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeLocationImplicitProfile() {
		assertThat(handler.getImplicitProfile(), equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-location"));
	}
	
	@Test
	public void canHandle_shouldAlwaysReturnTrue() {
		assertTrue(handler.canHandle(new Location()));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptAnySearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	// ---- get ----
	
	@Test
	public void get_shouldGetLocationByUuid() {
		when(dao.get(LOCATION_UUID)).thenReturn(location);
		when(translator.toFhirResource(location)).thenReturn(fhirLocation);
		
		Location result = handler.get(LOCATION_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(LOCATION_UUID));
		assertThat(result.getName(), equalTo(LOCATION_NAME));
		assertThat(result.getDescription(), equalTo(LOCATION_DESCRIPTION));
	}
	
	// ---- search ----
	
	@Test
	public void search_shouldReturnLocationsByParameters() {
		List<org.openmrs.Location> locations = new ArrayList<>();
		locations.add(location);
		
		SearchParameterMap theParams = new SearchParameterMap();
		
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(translator.toFhirResources(singletonList(location))).thenReturn(singletonList(fhirLocation));
		when(dao.getSearchResults(any())).thenReturn(locations);
		
		IBundleProvider results = handler.search(theParams);
		
		assertThat(results, notNullValue());
		
		List<Location> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(LOCATION_UUID))));
	}
}
