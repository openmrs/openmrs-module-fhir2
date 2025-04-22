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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.Coding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.LocationTag;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;

@RunWith(MockitoJUnitRunner.class)
public class LocationTagTranslatorImplTest {
	
	private static final String LAB_TAG_NAME = "Lab location";
	
	private static final String LAB_TAG_DESCRIPTION = "Used to identify lab locations";
	
	@Mock
	FhirLocationDao fhirLocationDao;
	
	private LocationTagTranslatorImpl locationTagTranslatorImpl;
	
	@Before
	public void setup() {
		locationTagTranslatorImpl = new LocationTagTranslatorImpl();
		locationTagTranslatorImpl.setFhirLocationDao(fhirLocationDao);
	}
	
	@Test
	public void toOpenmrsType_shouldCreateNewTagIfNonExists() {
		LocationTag omrsTag = new LocationTag(LAB_TAG_NAME, LAB_TAG_DESCRIPTION);
		
		Coding tag = new Coding();
		tag.setCode(LAB_TAG_NAME);
		tag.setDisplay(LAB_TAG_DESCRIPTION);
		
		when(fhirLocationDao.getLocationTagByName(tag.getCode())).thenReturn(null);
		when(fhirLocationDao.createLocationTag(any(LocationTag.class))).thenReturn(omrsTag);
		LocationTag newLocationTag = locationTagTranslatorImpl.toOpenmrsType(tag);
		assertThat(newLocationTag, notNullValue());
		assertThat(newLocationTag.getName(), is(LAB_TAG_NAME));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnExistingTagIfTagExists() {
		LocationTag omrsTag = new LocationTag(LAB_TAG_NAME, LAB_TAG_DESCRIPTION);
		
		Coding tag = new Coding();
		tag.setCode(LAB_TAG_NAME);
		tag.setDisplay(LAB_TAG_DESCRIPTION);
		
		when(fhirLocationDao.getLocationTagByName(tag.getCode())).thenReturn(omrsTag);
		LocationTag existingLocationTag = locationTagTranslatorImpl.toOpenmrsType(tag);
		assertThat(existingLocationTag, notNullValue());
		assertThat(existingLocationTag.getName(), is(LAB_TAG_NAME));
	}
}
