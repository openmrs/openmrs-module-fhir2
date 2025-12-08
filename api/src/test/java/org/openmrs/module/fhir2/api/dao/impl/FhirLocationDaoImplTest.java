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

import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import static org.openmrs.util.PrivilegeConstants.GET_LOCATIONS;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.LocationTag;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;

public class FhirLocationDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432";
	
	private static final String UNKNOWN_LOCATION_UUID = "8516d594-9c31-4bd3-bfec-b42b2f8a8444";
	
	private static final String LOCATION_ATTRIBUTE_TYPE_UUID = "cb5703b1-0d1e-47e5-9d5b-d3ab77bccb9d";
	
	private static final String LOCATION_TAG_UUID = "cb5703b1-0d1e-47e5-9d5b-d3ab77baab9d";
	
	private static final String LOCATION_TAG_NAME = "SomeName";
	
	private static final String LOCATION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirLocationDaoImplTest_initial_data.xml";
	
	@Autowired
	private FhirLocationDao fhirLocationDao;
	
	@Autowired
	LocationService locationService;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(LOCATION_INITIAL_DATA_XML);
	}
	
	@Test
	public void get_shouldReturnMatchingLocation() {
		Location location = fhirLocationDao.get(LOCATION_UUID);
		
		assertThat(location, notNullValue());
		assertThat(location.getUuid(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void get_shouldReturnNullWithUnknownUuid() {
		Location location = fhirLocationDao.get(UNKNOWN_LOCATION_UUID);
		
		assertThat(location, nullValue());
	}
	
	@Test
	public void get_shouldRequireGetLocationsPrivilege() {
		Context.logout();
		
		try {
			fhirLocationDao.get(LOCATION_UUID);
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_LOCATIONS);
			assertThat(fhirLocationDao.get(LOCATION_UUID), notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_LOCATIONS);
		}
	}
	
	@Test
	public void get_shouldRequireGetLocationsPrivilegeWithCollection() {
		Context.logout();
		
		try {
			fhirLocationDao.get(Arrays.asList(LOCATION_UUID));
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_LOCATIONS);
			List<Location> locations = fhirLocationDao.get(Arrays.asList(LOCATION_UUID));
			assertThat(locations, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_LOCATIONS);
		}
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
	public void getActiveAttributesByLocationsAndAttributeTypeUuid_shouldReturnLocationAttribute() {
		Location location = new Location();
		location.setUuid(LOCATION_UUID);
		
		Map<Location, List<LocationAttribute>> attributes = fhirLocationDao
		        .getActiveAttributesByLocationsAndAttributeTypeUuid(singleton(location), LOCATION_ATTRIBUTE_TYPE_UUID);
		
		assertThat(attributes, notNullValue());
	}
	
	@Test
	public void getLocationAttributeTypeByUuid_shouldReturnAttributeType() {
		LocationAttributeType locationAttributeType = new LocationAttributeType();
		locationAttributeType.setUuid(LOCATION_ATTRIBUTE_TYPE_UUID);
		locationAttributeType.setName("Some Attribute");
		locationAttributeType.setDatatypeClassname(FreeTextDatatype.class.getName());
		locationService.saveLocationAttributeType(locationAttributeType);
		
		LocationAttributeType result = fhirLocationDao.getLocationAttributeTypeByUuid(LOCATION_ATTRIBUTE_TYPE_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(LOCATION_ATTRIBUTE_TYPE_UUID));
	}
	
	@Test
	public void getLocationTagByName_shouldGetTag() {
		LocationTag locationTag = new LocationTag();
		locationTag.setUuid(LOCATION_TAG_UUID);
		locationTag.setName(LOCATION_TAG_NAME);
		locationService.saveLocationTag(locationTag);
		
		LocationTag result = fhirLocationDao.getLocationTagByName(LOCATION_TAG_NAME);
		
		assertThat(result, notNullValue());
		assertThat(result.getName(), equalTo(LOCATION_TAG_NAME));
	}
	
	@Test
	public void getSearchResults_shouldRequireGetLocationsPrivilege() {
		Context.logout();
		
		try {
			fhirLocationDao.getSearchResults(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_LOCATIONS);
			List<Location> locations = fhirLocationDao.getSearchResults(new SearchParameterMap());
			assertThat(locations, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_LOCATIONS);
		}
	}
	
	@Test
	public void getSearchResultsCount_shouldRequireGetLocationsPrivilege() {
		Context.logout();
		
		try {
			fhirLocationDao.getSearchResultsCount(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_LOCATIONS);
			int count = fhirLocationDao.getSearchResultsCount(new SearchParameterMap());
			assertThat(count, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_LOCATIONS);
		}
	}
	
	@Test
	public void createLocationTag_shouldCreateTag() {
		LocationTag locationTag = new LocationTag();
		locationTag.setUuid(LOCATION_TAG_UUID);
		locationTag.setName(LOCATION_TAG_NAME);
		locationService.saveLocationTag(locationTag);
		
		LocationTag result = fhirLocationDao.createLocationTag(locationTag);
		
		assertThat(result, notNullValue());
	}
}
