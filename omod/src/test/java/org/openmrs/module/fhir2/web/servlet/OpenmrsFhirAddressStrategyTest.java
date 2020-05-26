/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.servlet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;

@RunWith(MockitoJUnitRunner.class)
public class OpenmrsFhirAddressStrategyTest {
	
	private static final String FHIR_SERVER_BASE_URL = "https://localhost:8080/openmrs/ws/fhir2/";
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private OpenmrsFhirAddressStrategy fhirAddressStrategy;
	
	@Before
	public void setup() {
		fhirAddressStrategy = new OpenmrsFhirAddressStrategy();
		fhirAddressStrategy.setGlobalPropertyService(globalPropertyService);
	}
	
	@Test
	public void should_determineServerBaseR3() {
		ServletContext servletContext = Mockito.mock(ServletContext.class);
		HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
		
		when(httpServletRequest.getScheme()).thenReturn("https");
		when(httpServletRequest.getServerPort()).thenReturn(8080);
		when(httpServletRequest.getContextPath()).thenReturn("/openmrs");
		when(httpServletRequest.getServerName()).thenReturn("localhost");
		when(httpServletRequest.getRequestURI()).thenReturn("/openmrs/ws/fhir2/R3/Person");
		
		String serverBase = fhirAddressStrategy.determineServerBase(servletContext, httpServletRequest);
		
		assertThat(serverBase, notNullValue());
		assertThat(serverBase, equalTo(FHIR_SERVER_BASE_URL + "R3"));
	}
	
	@Test
	public void should_determineServerBaseR4() {
		ServletContext servletContext = Mockito.mock(ServletContext.class);
		HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
		
		when(httpServletRequest.getScheme()).thenReturn("https");
		when(httpServletRequest.getServerPort()).thenReturn(8080);
		when(httpServletRequest.getContextPath()).thenReturn("/openmrs");
		when(httpServletRequest.getServerName()).thenReturn("localhost");
		when(httpServletRequest.getRequestURI()).thenReturn("/openmrs/ws/fhir2/R4/Person");
		
		String serverBase = fhirAddressStrategy.determineServerBase(servletContext, httpServletRequest);
		
		assertThat(serverBase, notNullValue());
		assertThat(serverBase, equalTo(FHIR_SERVER_BASE_URL + "R4"));
	}
}
