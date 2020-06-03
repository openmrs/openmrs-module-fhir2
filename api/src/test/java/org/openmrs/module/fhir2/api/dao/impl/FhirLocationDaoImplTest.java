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

import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirLocationDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String UNKNOWN_LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432aabz";
	
	private static final String LOCATION_ATTRIBUTE_TYPE_UUID = "abcde432-1691-11df-97a5-7038c432abcd";
	
	private static final String LOCATION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirLocationDaoImplTest_initial_data.xml";
	
	private FhirLocationDaoImpl fhirLocationDao;
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Before
	public void setup() throws Exception {
		fhirLocationDao = new FhirLocationDaoImpl();
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
}
