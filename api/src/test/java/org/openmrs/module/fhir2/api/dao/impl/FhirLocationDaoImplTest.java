/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import java.util.Collection;

import ca.uhn.fhir.rest.param.TokenParam;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirLocationDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String UNKNOWN_LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432aabz";
	
	private static final String LOCATION_NAME = "Test location 1";
	
	private static final String UNKNOWN_LOCATION_NAME = "Location2";
	
	private static final String LOCATION_CITY = "Artuor";
	
	private static final String UNKNOWN_LOCATION_CITY = "ArtuorA";
	
	private static final String LOCATION_COUNTRY = "Kenya";
	
	private static final String UNKNOWN_LOCATION_COUNTRY = "KenyaA";
	
	private static final String POSTAL_CODE = "4069-3100";
	
	private static final String UNKNOWN_POSTAL_CODE = "4015-3100";
	
	private static final String LOCATION_STATE = "province";
	
	private static final String UNKNOWN_LOCATION_STATE = "province state";
	
	private static final String LOGIN_LOCATION_TAG_NAME = "login";
	
	private static final String LOCATION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirLocationDaoImplTest_initial_data.xml";
	
	private FhirLocationDaoImpl fhirLocationDao;
	
	@Inject
	@Named("locationService")
	private Provider<LocationService> locationServiceProvider;
	
	@Inject
	@Named("sessionFactory")
	private Provider<SessionFactory> sessionFactoryProvider;
	
	@Before
	public void setup() throws Exception {
		fhirLocationDao = new FhirLocationDaoImpl();
		fhirLocationDao.setLocationService(locationServiceProvider.get());
		fhirLocationDao.setSessionFactory(sessionFactoryProvider.get());
		executeDataSet(LOCATION_INITIAL_DATA_XML);
	}
	
	@Test
	public void getLocationByUuid_shouldReturnMatchingLocation() {
		Location location = fhirLocationDao.getLocationByUuid(LOCATION_UUID);
		
		assertThat(location, notNullValue());
		assertThat(location.getUuid(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void getLocationByUuid_shouldReturnNullWithUnknownUuid() {
		Location location = fhirLocationDao.getLocationByUuid(UNKNOWN_LOCATION_UUID);
		
		assertThat(location, nullValue());
	}
	
	@Test
	public void findLocationByName_shouldReturnCorrectLocation() {
		Collection<Location> locations = fhirLocationDao.findLocationByName(LOCATION_NAME);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(1));
	}
	
	@Test
	public void findLocationByName_shouldReturnEmptyCollectionWhenCalledWithUnknownName() {
		Collection<Location> locations = fhirLocationDao.findLocationByName(UNKNOWN_LOCATION_NAME);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(0));
	}
	
	@Test
	public void findLocationByCity_shouldReturnCorrectLocation() {
		Collection<Location> locations = fhirLocationDao.findLocationsByCity(LOCATION_CITY);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findLocationByCity_shouldReturnEmptyCollectionWhenCalledWithUnknownCity() {
		Collection<Location> locations = fhirLocationDao.findLocationsByCity(UNKNOWN_LOCATION_CITY);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(0));
	}
	
	@Test
	public void findLocationByCountry_shouldReturnCorrectLocation() {
		Collection<Location> locations = fhirLocationDao.findLocationsByCountry(LOCATION_COUNTRY);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), greaterThanOrEqualTo(2));
	}
	
	@Test
	public void findLocationByCountry_shouldReturnEmptyCollectionWhenCalledWithUnknownCountry() {
		Collection<Location> locations = fhirLocationDao.findLocationsByCountry(UNKNOWN_LOCATION_COUNTRY);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(0));
	}
	
	@Test
	public void findLocationsByPostalCode_shouldReturnCorrectLocation() {
		Collection<Location> locations = fhirLocationDao.findLocationsByPostalCode(POSTAL_CODE);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), greaterThanOrEqualTo(2));
	}
	
	@Test
	public void findLocationsByPostalCode_shouldReturnEmptyCollectionWhenCalledWithUnknownCode() {
		Collection<Location> locations = fhirLocationDao.findLocationsByPostalCode(UNKNOWN_POSTAL_CODE);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(0));
	}
	
	@Test
	public void findLocationsByState_shouldReturnCorrectLocation() {
		Collection<Location> locations = fhirLocationDao.findLocationsByState(LOCATION_STATE);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findLocationsByState_shouldReturnEmptyCollectionWhenCalledWithUnknownState() {
		Collection<Location> locations = fhirLocationDao.findLocationsByState(UNKNOWN_LOCATION_STATE);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(0));
	}
	
	@Test
	public void findLocationsByTag_shouldReturnLocationsContainingGivenTag() {
		TokenParam locationTag = new TokenParam(FhirConstants.OPENMRS_FHIR_EXT_LOCATION_TAG, LOGIN_LOCATION_TAG_NAME);
		Collection<Location> locations = fhirLocationDao.findLocationsByTag(locationTag);
		assertThat(locations, notNullValue());
		assertThat(locations.size(), greaterThanOrEqualTo(2));
	}
}
