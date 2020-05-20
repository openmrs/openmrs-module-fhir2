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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;

@RunWith(MockitoJUnitRunner.class)
public class FhirUserDefaultPropertiesImplTest {
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirUserDefaultPropertiesImpl userDefaultProperties;
	
	@Before
	public void setup() {
		userDefaultProperties = new FhirUserDefaultPropertiesImpl();
		userDefaultProperties.setGlobalPropertyService(globalPropertyService);
	}
	
	@Test
	public void getDefaultLocale_shouldReturnLocale() {
		when(globalPropertyService.getGlobalProperty(anyString(), anyString())).thenReturn("en_GB");
		Locale result = userDefaultProperties.getDefaultLocale();
		
		assertThat(result, notNullValue());
		assertThat(result, is(Locale.UK));
	}
	
	@Test
	public void getDefaultLocale_shouldReturnUSLocale() {
		when(globalPropertyService.getGlobalProperty(anyString(), anyString())).thenReturn("en_US");
		Locale result = userDefaultProperties.getDefaultLocale();
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(Locale.US));
	}
}
