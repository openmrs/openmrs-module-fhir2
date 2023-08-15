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

import static co.unruly.matchers.OptionalMatchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.hl7.fhir.r4.model.ContactPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.LocationAttributeType;
import org.openmrs.PersonAttributeType;
import org.openmrs.ProviderAttributeType;
import org.openmrs.module.fhir2.api.dao.FhirContactPointMapDao;
import org.openmrs.module.fhir2.model.FhirContactPointMap;

@RunWith(MockitoJUnitRunner.class)
public class FhirContactPointMapServiceImplTest {
	
	@Mock
	private FhirContactPointMapDao fhirContactPointMapDao;
	
	@Mock
	private FhirContactPointMapServiceImpl fhirContactPointMapService;
	
	private FhirContactPointMap fhirContactPointMap;
	
	private PersonAttributeType personAttributeType;
	
	private LocationAttributeType locationAttributeType;
	
	private ProviderAttributeType providerAttributeType;
	
	@Before
	public void setup() {
		fhirContactPointMapService = new FhirContactPointMapServiceImpl();
		fhirContactPointMapService.setDao(fhirContactPointMapDao);
		personAttributeType = new PersonAttributeType();
		locationAttributeType = new LocationAttributeType();
		providerAttributeType = new ProviderAttributeType();
		fhirContactPointMap = new FhirContactPointMap();
	}
	
	@Test
	public void getFhirContactPointMapForPersonAttributeType_shouldReturnEmptyFhirContactPointMapWhenNoPersonAttributeTypeIsFound() {
		when(fhirContactPointMapDao.getFhirContactPointMapForPersonAttributeType(null)).thenReturn(Optional.empty());
		
		Optional<FhirContactPointMap> result = fhirContactPointMapService.getFhirContactPointMapForPersonAttributeType(null);
		
		assertThat(result.isPresent(), is(false));
	}
	
	@Test
	public void getFhirContactPointMapForPersonAttributeType_shouldReturnFhirContactPointMapWhenPersonAttributeTypeIsFound() {
		when(fhirContactPointMapDao.getFhirContactPointMapForPersonAttributeType(personAttributeType))
		        .thenReturn(Optional.of(fhirContactPointMap));
		
		Optional<FhirContactPointMap> result = fhirContactPointMapService
		        .getFhirContactPointMapForPersonAttributeType(personAttributeType);
		
		assertThat(result.isPresent(), is(true));
		assertThat(result, contains(equalTo(fhirContactPointMap)));
	}
	
	@Test
	public void getFhirContactPointMapForAttributeType_shouldReturnEmptyFhirContactPointMapWhenAttributeTypeIsNotFound() {
		when(fhirContactPointMapDao.getFhirContactPointMapForAttributeType(null)).thenReturn(Optional.empty());
		
		Optional<FhirContactPointMap> result = fhirContactPointMapService.getFhirContactPointMapForAttributeType(null);
		
		assertThat(result.isPresent(), is(false));
	}
	
	@Test
	public void getFhirContactPointMapForAttributeType_shouldReturnFhirContactPointMapWhenLocationAttributeTypeIsFound() {
		when(fhirContactPointMapDao.getFhirContactPointMapForAttributeType(locationAttributeType))
		        .thenReturn(Optional.of(fhirContactPointMap));
		
		Optional<FhirContactPointMap> result = fhirContactPointMapService
		        .getFhirContactPointMapForAttributeType(locationAttributeType);
		
		assertThat(result.isPresent(), is(true));
		assertThat(result, contains(equalTo(fhirContactPointMap)));
	}
	
	@Test
	public void getFhirContactPointMapForAttributeType_shouldReturnFhirContactPointMapWhenProviderAttributeTypeIsFound() {
		when(fhirContactPointMapDao.getFhirContactPointMapForAttributeType(providerAttributeType))
		        .thenReturn(Optional.of(fhirContactPointMap));
		
		Optional<FhirContactPointMap> result = fhirContactPointMapService
		        .getFhirContactPointMapForAttributeType(providerAttributeType);
		
		assertThat(result.isPresent(), is(true));
		assertThat(result, contains(equalTo(fhirContactPointMap)));
	}
	
	@Test
	public void saveFhirContactPointMap_ShouldReturnFhirContactPointMap() {
		fhirContactPointMap.setSystem(ContactPoint.ContactPointSystem.EMAIL);
		fhirContactPointMap.setUse(ContactPoint.ContactPointUse.WORK);
		fhirContactPointMap.setRank(2);
		when(fhirContactPointMapDao.saveFhirContactPointMap(fhirContactPointMap)).thenReturn(fhirContactPointMap);
		
		FhirContactPointMap result = fhirContactPointMapService.saveFhirContactPointMap(fhirContactPointMap);
		
		assertThat(result.getSystem(), equalTo(ContactPoint.ContactPointSystem.EMAIL));
		assertThat(result.getUse(), equalTo(ContactPoint.ContactPointUse.WORK));
		assertThat(result.getRank(), equalTo(2));
	}
	
	@Test
	public void saveFhirContactPointMap_ShouldUpdateExistingFhirContactPointMap() {
		FhirContactPointMap existingFhirContactPointMap = new FhirContactPointMap();
		existingFhirContactPointMap.setId(1);
		existingFhirContactPointMap.setAttributeTypeDomain("person");
		existingFhirContactPointMap.setAttributeTypeId(10001);
		existingFhirContactPointMap.setSystem(ContactPoint.ContactPointSystem.PHONE);
		existingFhirContactPointMap.setUse(ContactPoint.ContactPointUse.WORK);
		existingFhirContactPointMap.setRank(1);
		when(fhirContactPointMapDao.saveFhirContactPointMap(existingFhirContactPointMap))
		        .thenReturn(existingFhirContactPointMap);
		
		FhirContactPointMap result = fhirContactPointMapService.saveFhirContactPointMap(existingFhirContactPointMap);
		assertThat(result.getAttributeTypeDomain(), equalTo("person"));
		assertThat(result.getAttributeTypeId(), equalTo(10001));
		assertThat(result.getSystem(), equalTo(ContactPoint.ContactPointSystem.PHONE));
		assertThat(result.getUse(), equalTo(ContactPoint.ContactPointUse.WORK));
		assertThat(result.getRank(), equalTo(1));
		
		fhirContactPointMap.setAttributeTypeDomain("person");
		fhirContactPointMap.setAttributeTypeId(10001);
		fhirContactPointMap.setSystem(ContactPoint.ContactPointSystem.EMAIL);
		fhirContactPointMap.setUse(ContactPoint.ContactPointUse.HOME);
		fhirContactPointMap.setRank(2);
		when(fhirContactPointMapDao.saveFhirContactPointMap(fhirContactPointMap)).thenReturn(fhirContactPointMap);
		
		result = fhirContactPointMapService.saveFhirContactPointMap(fhirContactPointMap);
		assertThat(result.getAttributeTypeDomain(), equalTo("person"));
		assertThat(result.getAttributeTypeId(), equalTo(10001));
		assertThat(result.getSystem(), equalTo(ContactPoint.ContactPointSystem.EMAIL));
		assertThat(result.getUse(), equalTo(ContactPoint.ContactPointUse.HOME));
		assertThat(result.getRank(), equalTo(2));
	}
}
