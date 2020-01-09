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
import org.openmrs.module.fhir2.api.dao.FhirGlobalPropertyDao;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FhirGlobalPropertyServiceImplTest {
	
	private static final String PERSON_ATTRIBUTE_TYPE_VALUE = "fhir2.personAttributeTypeUuid";
	
	private static final String PERSON_ATTRIBUTE_TYPE_VALUE_NOT_FOUND = "fhir2.non-existing property";
	
	private static final String PERSON_ATTRIBUTE_TYPE_UUID = "12323h324-32423n30-32n23-23j23";
	
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
		when(fhirGlobalPropertyDao.getGlobalProperty(PERSON_ATTRIBUTE_TYPE_VALUE_NOT_FOUND)).thenReturn("");
		String personAttributeTypeUuid = globalPropertyService.getGlobalProperty(PERSON_ATTRIBUTE_TYPE_VALUE);
		assertThat(personAttributeTypeUuid, nullValue());
	}
	
}
