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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.translators.LocationTranslator;

import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FhirLocationServiceImplTest {
	
	private static final String LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String LOCATION_NAME = "Test location 1";
	
	private static final String LOCATION_DESCRIPTION = "Test description";
	
	@Mock
	FhirLocationDao locationDao;
	
	@Mock
	LocationTranslator locationTranslator;
	
	private FhirLocationServiceImpl fhirLocationService;
	
	@Before
	public void setUp() {
		fhirLocationService = new FhirLocationServiceImpl();
		fhirLocationService.setLocationDao(locationDao);
		fhirLocationService.setLocationTranslator(locationTranslator);
	}
	
	@Test
	public void shouldGetLocationByUuid() {
		Location location = new Location();
		location.setUuid(LOCATION_UUID);
		location.setName(LOCATION_NAME);
		location.setDescription(LOCATION_DESCRIPTION);
		location.setDateCreated(new Date());
		location.setRetired(false);
		
		when(locationDao.getLocationByUuid(LOCATION_UUID)).thenReturn(location);
		
		org.hl7.fhir.r4.model.Location result = fhirLocationService.getLocationByUuid(LOCATION_UUID);
		assertNotNull(location);
	}
}
