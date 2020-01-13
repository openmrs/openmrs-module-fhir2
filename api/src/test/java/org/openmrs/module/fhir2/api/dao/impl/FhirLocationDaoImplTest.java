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

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.Collection;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirLocationDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String UNKNOWN_LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432aabz";
	
	private static final String LOCATION_NAME = "Test location 1";
	
	private static final String UNKNOWN_LOCATION_NAME = "Location2";
	
	private static final String LOCATION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirLocationDaoImplTest_initial_data.xml";
	
	private FhirLocationDaoImpl fhirLocationDao;
	
	@Inject
	@Named("locationService")
	private Provider<LocationService> locationServiceProvider;
	
	@Before
	public void setup() throws Exception {
		fhirLocationDao = new FhirLocationDaoImpl();
		fhirLocationDao.setLocationService(locationServiceProvider.get());
		executeDataSet(LOCATION_INITIAL_DATA_XML);
	}
	
	@Test
	public void getLocationByUuid_shouldReturnMatchingLocation() {
		Location location = fhirLocationDao.getLocationByUuid(LOCATION_UUID);
		assertNotNull(location);
		assertEquals(location.getUuid(), LOCATION_UUID);
	}
	
	@Test
	public void getLocationByUuid_shouldReturnNullWithUnknownUuid() {
		Location location = fhirLocationDao.getLocationByUuid(UNKNOWN_LOCATION_UUID);
		assertNull(location);
	}
	
	@Test
	public void findLocationByName_shouldReturnCorrectLocation() {
		Collection<Location> locations = fhirLocationDao.findLocationByName(LOCATION_NAME);
		assertNotNull(locations);
		assertEquals(locations.size(), 1);
		assertThat(locations.stream().findAny().isPresent(), is(true));
	}
	
	@Test
	public void findLocationByName_shouldReturnNullWhenCalledWithUnknownName() {
		Collection<Location> locations = fhirLocationDao.findLocationByName(UNKNOWN_LOCATION_NAME);
		assertNotNull(locations);
		assertEquals(locations.size(), 0);
		assertThat(locations.stream().findAny().isPresent(), is(false));
	}
}
