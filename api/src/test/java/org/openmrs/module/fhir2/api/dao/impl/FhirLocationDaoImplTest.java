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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirLocationDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432";
	
	private static final String UNKNOWN_LOCATION_UUID = "8516d594-9c31-4bd3-bfec-b42b2f8a8444";
	
	private static final String LOCATION_ATTRIBUTE_TYPE_UUID = "cb5703b1-0d1e-47e5-9d5b-d3ab77bccb9d";
	
	private static final String LOCATION_TAG_UUID = "cb5703b1-0d1e-47e5-9d5b-d3ab77baab9d";
	
	private static final String LOCATION_TAG_NAME = "SomeName";
	
	private static final String LOCATION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirLocationDaoImplTest_initial_data.xml";
	
	private FhirLocationDaoImpl fhirLocationDao;
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Mock
	private LocationService locationService;
	
	@Before
	public void setup() throws Exception {
		fhirLocationDao = new FhirLocationDaoImpl();
		fhirLocationDao.setLocationService(locationService);
		fhirLocationDao.setSessionFactory(sessionFactory);
		executeDataSet(LOCATION_INITIAL_DATA_XML);
	}
	
	@Test
	public void getLocationByUuid_shouldReturnMatchingLocation() {
		Location location = fhirLocationDao.get(LOCATION_UUID);
		
		assertThat(location, notNullValue());
		assertThat(location.getUuid(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void getLocationByUuid_shouldReturnNullWithUnknownUuid() {
		Location location = fhirLocationDao.get(UNKNOWN_LOCATION_UUID);
		
		assertThat(location, nullValue());
	}
	
	@Test
	public void getActiveAttributesByLocationAndAttributeTypeUuid_shouldReturnLocationAttribute() {
		Location location = new Location();
		location.setUuid(LOCATION_UUID);
		
		List<LocationAttribute> attributeList = fhirLocationDao.getActiveAttributesByLocationAndAttributeTypeUuid(location,
		    LOCATION_ATTRIBUTE_TYPE_UUID);
		
		assertThat(attributeList, notNullValue());
	}
	
	@Test
	public void getLocationAttributeTypeByUuid_shouldReturnAttributeType() {
		LocationAttributeType locationAttributeType = new LocationAttributeType();
		locationAttributeType.setUuid(LOCATION_ATTRIBUTE_TYPE_UUID);
		
		when(locationService.getLocationAttributeTypeByUuid(LOCATION_ATTRIBUTE_TYPE_UUID)).thenReturn(locationAttributeType);
		
		LocationAttributeType result = fhirLocationDao.getLocationAttributeTypeByUuid(LOCATION_ATTRIBUTE_TYPE_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(LOCATION_ATTRIBUTE_TYPE_UUID));
	}
	
	@Test
	public void saveLocationTag_shouldSaveTag() {
		LocationTag locationTag = new LocationTag();
		locationTag.setUuid(LOCATION_TAG_UUID);
		locationTag.setName(LOCATION_TAG_NAME);
		
		when(locationService.saveLocationTag(locationTag)).thenReturn(locationTag);
		
		LocationTag result = fhirLocationDao.saveLocationTag(locationTag);
		
		assertThat(result, notNullValue());
	}
	
	@Test
	public void getLocationTagByName_shouldGetTag() {
		LocationTag locationTag = new LocationTag();
		locationTag.setUuid(LOCATION_TAG_UUID);
		locationTag.setName(LOCATION_TAG_NAME);
		
		when(locationService.getLocationTagByName(LOCATION_TAG_NAME)).thenReturn(locationTag);
		
		LocationTag result = fhirLocationDao.getLocationTagByName(LOCATION_TAG_NAME);
		
		assertThat(result, notNullValue());
		assertThat(result.getName(), equalTo(LOCATION_TAG_NAME));
	}
}
