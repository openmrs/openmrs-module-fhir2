/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;

@RunWith(MockitoJUnitRunner.class)
public class OpenmrsFhirAddressStrategyTest {
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private ServletContext servletContext;
	
	@Mock
	private HttpServletRequest httpServletRequest;
	
	private OpenmrsFhirAddressStrategy fhirAddressStrategy;
	
	@Before
	public void setup() {
		fhirAddressStrategy = new OpenmrsFhirAddressStrategy();
		fhirAddressStrategy.setGlobalPropertyService(globalPropertyService);
	}
	
	@Test
	public void shouldDetermineServerBaseFromGlobalProperty() {
		when(globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_URI_PREFIX))
		        .thenReturn("http://my.openmrs.org/ws/fhir2/");
		when(httpServletRequest.getContextPath()).thenReturn("");
		when(httpServletRequest.getRequestURI()).thenReturn("/ws/fhir2/R4");
		
		String serverBase = fhirAddressStrategy.determineServerBase(servletContext, httpServletRequest);
		
		assertThat(serverBase, startsWith("http://my.openmrs.org/ws/fhir2/"));
	}
	
	@Test
	public void shouldDetermineServerBaseFromGlobalPropertyWithoutTrailingSlash() {
		when(globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_URI_PREFIX))
		        .thenReturn("http://my.openmrs.org/ws/fhir2");
		when(httpServletRequest.getContextPath()).thenReturn("");
		when(httpServletRequest.getRequestURI()).thenReturn("/ws/fhir2/R4");
		
		String serverBase = fhirAddressStrategy.determineServerBase(servletContext, httpServletRequest);
		
		assertThat(serverBase, startsWith("http://my.openmrs.org/ws/fhir2/"));
	}
	
	@Test
	public void shouldDetermineServerBaseForR3() {
		when(httpServletRequest.getScheme()).thenReturn("http");
		when(httpServletRequest.getServerName()).thenReturn("localhost");
		when(httpServletRequest.getServerPort()).thenReturn(80);
		when(httpServletRequest.getContextPath()).thenReturn("/openmrs");
		when(httpServletRequest.getRequestURI()).thenReturn("/openmrs/ws/fhir2/R3/Person");
		
		String serverBase = fhirAddressStrategy.determineServerBase(servletContext, httpServletRequest);
		
		assertThat(serverBase, endsWith("/R3"));
	}
	
	@Test
	public void shouldDetermineServerBaseForR4() {
		when(httpServletRequest.getScheme()).thenReturn("http");
		when(httpServletRequest.getServerName()).thenReturn("localhost");
		when(httpServletRequest.getServerPort()).thenReturn(80);
		when(httpServletRequest.getContextPath()).thenReturn("/openmrs");
		when(httpServletRequest.getRequestURI()).thenReturn("/openmrs/ws/fhir2/R4/Person");
		
		String serverBase = fhirAddressStrategy.determineServerBase(servletContext, httpServletRequest);
		
		assertThat(serverBase, endsWith("/R4"));
	}
	
	@Test
	public void shouldOmitDefaultPortForHttp() {
		when(httpServletRequest.getScheme()).thenReturn("http");
		when(httpServletRequest.getServerName()).thenReturn("localhost");
		when(httpServletRequest.getServerPort()).thenReturn(80);
		when(httpServletRequest.getContextPath()).thenReturn("/openmrs");
		when(httpServletRequest.getRequestURI()).thenReturn("/openmrs/ws/fhir2/R4/Person");
		
		String serverBase = fhirAddressStrategy.determineServerBase(servletContext, httpServletRequest);
		
		assertThat(serverBase, not(containsString(":80/")));
	}
	
	@Test
	public void shouldHaveNonDefaultPortForHttp() {
		when(httpServletRequest.getScheme()).thenReturn("http");
		when(httpServletRequest.getServerName()).thenReturn("localhost");
		when(httpServletRequest.getServerPort()).thenReturn(8080);
		when(httpServletRequest.getContextPath()).thenReturn("/openmrs");
		when(httpServletRequest.getRequestURI()).thenReturn("/openmrs/ws/fhir2/R4/Person");
		
		String serverBase = fhirAddressStrategy.determineServerBase(servletContext, httpServletRequest);
		
		assertThat(serverBase, containsString(":8080/"));
	}
	
	@Test
	public void shouldOmitDefaultPortForHttps() {
		when(httpServletRequest.getScheme()).thenReturn("https");
		when(httpServletRequest.getServerName()).thenReturn("localhost");
		when(httpServletRequest.getServerPort()).thenReturn(443);
		when(httpServletRequest.getContextPath()).thenReturn("/openmrs");
		when(httpServletRequest.getRequestURI()).thenReturn("/openmrs/ws/fhir2/R4/Person");
		
		String serverBase = fhirAddressStrategy.determineServerBase(servletContext, httpServletRequest);
		
		assertThat(serverBase, not(containsString(":80/")));
	}
	
	@Test
	public void shouldHaveNonDefaultPortForHttps() {
		when(httpServletRequest.getScheme()).thenReturn("https");
		when(httpServletRequest.getServerName()).thenReturn("localhost");
		when(httpServletRequest.getServerPort()).thenReturn(8443);
		when(httpServletRequest.getContextPath()).thenReturn("/openmrs");
		when(httpServletRequest.getRequestURI()).thenReturn("/openmrs/ws/fhir2/R4/Person");
		
		String serverBase = fhirAddressStrategy.determineServerBase(servletContext, httpServletRequest);
		
		assertThat(serverBase, containsString(":8443/"));
	}
}
