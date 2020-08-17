/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;

@RunWith(MockitoJUnitRunner.class)
public class EncounterLocationTranslatorImplTest {
	
	private static final String LOCATION_UUID = "122344-234xx23-2323kk-232k2h2";
	
	private static final String LOCATION_URI = FhirConstants.LOCATION + "/" + LOCATION_UUID;
	
	private static final String TEST_LOCATION_NAME = "test location name";
	
	@Mock
	FhirLocationService locationService;
	
	@Mock
	FhirLocationDao locationDao;
	
	private EncounterLocationTranslatorImpl encounterLocationTranslator;
	
	private Encounter.EncounterLocationComponent encounterLocationComponent;
	
	private Location location;
	
	@Before
	public void setUp() {
		encounterLocationTranslator = new EncounterLocationTranslatorImpl();
		encounterLocationTranslator.setLocationService(locationService);
		encounterLocationTranslator.setLocationDao(locationDao);
		
		location = new Location();
		location.setUuid(LOCATION_UUID);
		location.setName(TEST_LOCATION_NAME);
		
		encounterLocationComponent = new Encounter.EncounterLocationComponent();
		Reference reference = new Reference(LOCATION_URI);
		encounterLocationComponent.setLocation(reference);
	}
	
	@Test
	public void shouldTranslateEncounterLocationToFhirType() {
		Encounter.EncounterLocationComponent result = encounterLocationTranslator.toFhirResource(location);
		assertThat(result, notNullValue());
		assertThat(result.getLocation(), notNullValue());
	}
	
	@Test
	public void shouldTranslateEncounterLocationToFhirTypeWithCorrectLocationReference() {
		Encounter.EncounterLocationComponent result = encounterLocationTranslator.toFhirResource(location);
		
		assertThat(result, notNullValue());
		assertThat(result.getLocation(), notNullValue());
		assertThat(result.getLocation().getReference(), equalTo(LOCATION_URI));
	}
	
	@Test
	public void shouldTranslateEncounterLocationToOpenMrsType() {
		when(locationDao.get(LOCATION_UUID)).thenReturn(location);
		
		assertThat(encounterLocationTranslator.toOpenmrsType(encounterLocationComponent), notNullValue());
	}
	
	@Test
	public void shouldTranslateEncounterLocationToFhirTypeWithCorrectLocationDetails() {
		when(locationDao.get(LOCATION_UUID)).thenReturn(location);
		
		Location location = encounterLocationTranslator.toOpenmrsType(encounterLocationComponent);
		
		assertThat(location, notNullValue());
		assertThat(location.getUuid(), notNullValue());
		assertThat(location.getUuid(), equalTo(LOCATION_UUID));
		assertThat(location.getName(), equalTo(TEST_LOCATION_NAME));
	}
	
}
