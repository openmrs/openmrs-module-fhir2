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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.GlobalProperty;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.util.FhirGlobalPropertyHolder;

// we intentionally have unnecessary stubbings for this class when testing the X-Forwarded headers
@RunWith(MockitoJUnitRunner.Silent.class)
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
		// avoid trying to do an actual lookup by pre-caching an empty string
		new FhirGlobalPropertyHolder()
		        .globalPropertyChanged(new GlobalProperty(FhirConstants.GLOBAL_PROPERTY_URI_PREFIX, ""));
	}
	
	@After
	public void after() {
		FhirGlobalPropertyHolder.reset();
		;
	}
	
	@Test
	public void shouldDetermineServerBaseFromGlobalProperty() {
		new FhirGlobalPropertyHolder().globalPropertyChanged(
		    new GlobalProperty(FhirConstants.GLOBAL_PROPERTY_URI_PREFIX, "http://my.openmrs.org/ws/fhir2/"));
		when(httpServletRequest.getContextPath()).thenReturn("/");
		when(httpServletRequest.getRequestURI()).thenReturn("/ws/fhir2/R4");
		
		String serverBase = fhirAddressStrategy.determineServerBase(servletContext, httpServletRequest);
		
		assertThat(serverBase, startsWith("http://my.openmrs.org/ws/fhir2/"));
	}
	
	@Test
	public void shouldDetermineServerBaseFromGlobalPropertyWithoutTrailingSlash() {
		new FhirGlobalPropertyHolder().globalPropertyChanged(
		    new GlobalProperty(FhirConstants.GLOBAL_PROPERTY_URI_PREFIX, "http://my.openmrs.org/ws/fhir2"));
		when(httpServletRequest.getContextPath()).thenReturn("/");
		when(httpServletRequest.getRequestURI()).thenReturn("/ws/fhir2/R4");
		
		String serverBase = fhirAddressStrategy.determineServerBase(servletContext, httpServletRequest);
		
		assertThat(serverBase, startsWith("http://my.openmrs.org/ws/fhir2/"));
	}
	
	@Test
	public void shouldReturnAppropriateBaseUrlWithContextPath() {
		when(httpServletRequest.getScheme()).thenReturn("http");
		when(httpServletRequest.getServerName()).thenReturn("localhost");
		when(httpServletRequest.getServerPort()).thenReturn(80);
		when(httpServletRequest.getContextPath()).thenReturn("/openmrs");
		when(httpServletRequest.getRequestURI()).thenReturn("/openmrs/ws/fhir2/R4/Person");
		
		String serverBase = fhirAddressStrategy.determineServerBase(servletContext, httpServletRequest);
		
		assertThat(serverBase, equalTo("http://localhost/openmrs/ws/fhir2/R4"));
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
		
		assertThat(serverBase, not(containsString(":443/")));
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
	
	@Test
	public void shouldProperlyHandleStandardProxyHeaders() {
		when(httpServletRequest.getHeader("X-Forwarded-Proto")).thenReturn("https");
		when(httpServletRequest.getHeader("X-Forwarded-Host")).thenReturn("my.openmrs.org");
		when(httpServletRequest.getHeader("X-Forwarded-Port")).thenReturn("4443");
		when(httpServletRequest.getScheme()).thenReturn("http");
		when(httpServletRequest.getServerName()).thenReturn("localhost");
		when(httpServletRequest.getServerPort()).thenReturn(8080);
		when(httpServletRequest.getContextPath()).thenReturn("/openmrs");
		when(httpServletRequest.getRequestURI()).thenReturn("/openmrs/ws/fhir2/R4/Person");
		
		String serverBase = fhirAddressStrategy.determineServerBase(servletContext, httpServletRequest);
		
		assertThat(serverBase, equalTo("https://my.openmrs.org:4443/openmrs/ws/fhir2/R4"));
	}
}
