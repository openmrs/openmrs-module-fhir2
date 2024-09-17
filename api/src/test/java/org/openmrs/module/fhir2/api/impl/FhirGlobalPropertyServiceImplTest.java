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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirGlobalPropertyDao;

@RunWith(MockitoJUnitRunner.class)
public class FhirGlobalPropertyServiceImplTest {
	
	private static final String PERSON_ATTRIBUTE_TYPE_VALUE = "fhir2.personAttributeTypeUuid";
	
	private static final String PERSON_ATTRIBUTE_TYPE_UUID = "12323h324-32423n30-32n23-23j23";
	
	private static final String GLOBAL_PROPERTY_MODERATE = "1499AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String DEFAULT_PAGE_SIZE = "default.page.size";
	
	private static final String DEFAULT_PAGE_SIZE_STRING_VALUE = "10";
	
	private static final Integer DEFAULT_PAGE_SIZE_INTEGER_VALUE = 10;
	
	@Mock
	private FhirGlobalPropertyDao fhirGlobalPropertyDao;
	
	private FhirGlobalPropertyServiceImpl globalPropertyService;
	
	@Before
	public void setUp() {
		globalPropertyService = new FhirGlobalPropertyServiceImpl();
		globalPropertyService.setDao(fhirGlobalPropertyDao);
	}
	
	@Test
	public void shouldReturnStringGlobalPropertyWhenPropertyMatched() {
		when(fhirGlobalPropertyDao.getGlobalProperty(PERSON_ATTRIBUTE_TYPE_VALUE)).thenReturn(PERSON_ATTRIBUTE_TYPE_UUID);
		
		String personAttributeTypeUuid = globalPropertyService.getGlobalProperty(PERSON_ATTRIBUTE_TYPE_VALUE);
		
		assertThat(personAttributeTypeUuid, notNullValue());
		assertThat(personAttributeTypeUuid, equalTo(PERSON_ATTRIBUTE_TYPE_UUID));
	}
	
	@Test
	public void shouldReturnNullWhenGlobalPropertyNotFound() {
		String personAttributeTypeUuid = globalPropertyService.getGlobalProperty(PERSON_ATTRIBUTE_TYPE_VALUE);
		
		assertThat(personAttributeTypeUuid, nullValue());
	}
	
	@Test
	public void shouldReturnListOfGlobalPropertyValues() {
		Map<String, String> uuids = new HashMap<>();
		uuids.put(FhirConstants.GLOBAL_PROPERTY_MODERATE, GLOBAL_PROPERTY_MODERATE);
		
		when(fhirGlobalPropertyDao.getGlobalProperties(FhirConstants.GLOBAL_PROPERTY_MODERATE,
		    FhirConstants.GLOBAL_PROPERTY_SEVERE)).thenReturn(uuids);
		
		Map<String, String> values = globalPropertyService.getGlobalProperties(FhirConstants.GLOBAL_PROPERTY_MODERATE,
		    FhirConstants.GLOBAL_PROPERTY_SEVERE);
		
		assertThat(values, CoreMatchers.notNullValue());
		assertThat(values.size(), greaterThanOrEqualTo(1));
		assertThat(values.get(FhirConstants.GLOBAL_PROPERTY_MODERATE), CoreMatchers.equalTo(GLOBAL_PROPERTY_MODERATE));
	}
	
	@Test
	public void shouldReturnIntegerGlobalPropertyValueWhenPropertyMatched() {
		FhirGlobalPropertyService globalPropertyService = mock(FhirGlobalPropertyService.class);
		when(globalPropertyService.getGlobalProperty(DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE_INTEGER_VALUE)).thenReturn(100);
		
		int result = globalPropertyService.getGlobalProperty(DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE_INTEGER_VALUE);
		assertThat(result, notNullValue());
		assertThat(result, is(100));
	}
	
	@Test
	public void shouldReturnDefaultIntegerGlobalPropertyValueWhenPropertyNotMatched() {
		int result = globalPropertyService.getGlobalProperty(DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE_INTEGER_VALUE);
		assertThat(result, notNullValue());
		assertThat(result, is(DEFAULT_PAGE_SIZE_INTEGER_VALUE));
	}
	
	@Test
	public void shouldReturnStringGlobalPropertyValueWhenPropertyMatched() {
		FhirGlobalPropertyService globalPropertyService = mock(FhirGlobalPropertyService.class);
		when(globalPropertyService.getGlobalProperty(DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE_STRING_VALUE)).thenReturn("100");
		
		String result = globalPropertyService.getGlobalProperty(DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE_STRING_VALUE);
		assertThat(result, notNullValue());
		assertThat(result, is("100"));
	}
	
	@Test
	public void shouldReturnDefaultStringGlobalPropertyValueWhenPropertyNotMatched() {
		String result = globalPropertyService.getGlobalProperty(DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE_STRING_VALUE);
		assertThat(result, notNullValue());
		assertThat(result, is(DEFAULT_PAGE_SIZE_STRING_VALUE));
	}
	
	@Test
	public void shouldThrowNumberFormatException() {
		when(globalPropertyService.getGlobalProperty(DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE_STRING_VALUE))
		        .thenReturn("Invalid");
		Integer result = globalPropertyService.getGlobalProperty(DEFAULT_PAGE_SIZE, 0);
		assertThat(result, notNullValue());
		assertThat(result, is(0));
	}
}
