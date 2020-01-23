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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.hl7.fhir.r4.model.Address;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
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
	
	@Mock
	FhirLocationDao locationDao;
	
	@Mock
	LocationTranslator locationTranslator;
	
	private FhirLocationServiceImpl fhirLocationService;
	
	private Location location;
	
	private Address address;
	
	private org.hl7.fhir.r4.model.Location fhirLocation;
	
	@Before
	public void setUp() {
		fhirLocationService = new FhirLocationServiceImpl();
		fhirLocationService.setLocationDao(locationDao);
		fhirLocationService.setLocationTranslator(locationTranslator);
		
		location = new Location();
		location.setUuid(LOCATION_UUID);
		location.setName(LOCATION_NAME);
		location.setDescription(LOCATION_DESCRIPTION);
		location.setDateCreated(new Date());
		location.setRetired(false);
		
		fhirLocation = new org.hl7.fhir.r4.model.Location();
		fhirLocation.setId(LOCATION_UUID);
		fhirLocation.setName(LOCATION_NAME);
		fhirLocation.setDescription(LOCATION_DESCRIPTION);
		
		address = new Address();
		address.setCity(LOCATION_CITY);
		address.setPostalCode(POSTAL_CODE);
		address.setCountry(LOCATION_COUNTRY);
		address.setState(LOCATION_STATE);
		fhirLocation.setAddress(address);
	}
	
	@Test
	public void getLocationByUuid_shouldGetLocationByUuid() {
		when(locationDao.getLocationByUuid(LOCATION_UUID)).thenReturn(location);
		when(locationTranslator.toFhirResource(location)).thenReturn(fhirLocation);
		
		org.hl7.fhir.r4.model.Location result = fhirLocationService.getLocationByUuid(LOCATION_UUID);
		assertNotNull(result);
		assertEquals(result.getId(), fhirLocation.getId());
		assertEquals(result.getName(), LOCATION_NAME);
		assertEquals(result.getDescription(), LOCATION_DESCRIPTION);
		
	}
	
	@Test
	public void findLocationByName_shouldFindLocationByName() {
		Collection<Location> locations = new ArrayList<>();
		locations.add(location);
		when(locationDao.findLocationByName(LOCATION_NAME)).thenReturn(locations);
		when(locationTranslator.toFhirResource(location)).thenReturn(fhirLocation);

		Collection<org.hl7.fhir.r4.model.Location> results = fhirLocationService.findLocationByName(LOCATION_NAME);
		assertNotNull(results);
		assertEquals(results.size(), 1);
	}
	
	@Test
	public void findLocationByCity_shouldFindLocationByCity() {
		Collection<Location> locations = new ArrayList<>();
		locations.add(location);
		when(locationDao.findLocationsByCity(LOCATION_CITY)).thenReturn(locations);
		when(locationTranslator.toFhirResource(location)).thenReturn(fhirLocation);

		Collection<org.hl7.fhir.r4.model.Location> results = fhirLocationService.findLocationsByCity(LOCATION_CITY);
		assertNotNull(results);
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findLocationsByCountry_shouldFindLocationsByCountry(){
		Collection<Location> locations = new ArrayList<>();
		locations.add(location);
		when(locationDao.findLocationsByCountry(LOCATION_COUNTRY)).thenReturn(locations);
		when(locationTranslator.toFhirResource(location)).thenReturn(fhirLocation);

		Collection<org.hl7.fhir.r4.model.Location> collection = fhirLocationService.findLocationsByCountry(LOCATION_COUNTRY);
		assertNotNull(collection);
		assertThat(collection.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findLocationsByPostalCode_shouldfindLocationsByPostalCode(){
		Collection<Location> locations = new ArrayList<>();
		locations.add(location);
		when(locationDao.findLocationsByPostalCode(POSTAL_CODE)).thenReturn(locations);
		when(locationTranslator.toFhirResource(location)).thenReturn(fhirLocation);

		Collection<org.hl7.fhir.r4.model.Location> collection = fhirLocationService.findLocationsByPostalCode(POSTAL_CODE);
		assertNotNull(collection);
		assertThat(collection.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findLocationsBySsetCityVillagetate_shouldfindLocationsByState(){
		Collection<Location> locations = new ArrayList<>();
		locations.add(location);
		when(locationDao.findLocationsByState(LOCATION_STATE)).thenReturn(locations);
		when(locationTranslator.toFhirResource(location)).thenReturn(fhirLocation);

		Collection<org.hl7.fhir.r4.model.Location> collection = fhirLocationService.findLocationsByState(LOCATION_STATE);
		assertNotNull(collection);
		assertThat(collection.size(), greaterThanOrEqualTo(1));
	}
}
