/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.db.ContextDAO;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationFilterTest {
	
	private static final String USERNAME = "admin";
	
	private static final String PASSWORD = "Admin123";
	
	private AuthenticationFilter authenticationFilter;
	
	private MockFilterChain filterChain;
	
	@Mock
	private ContextDAO contextDAO;
	
	@Mock
	private User user;
	
	@Before
	public void setup() {
		Context.setDAO(contextDAO);
		Context.openSession();
		
		authenticationFilter = new AuthenticationFilter();
		filterChain = new MockFilterChain();
	}
	
	@After
	public void tearDown() {
		Context.closeSession();
	}
	
	@Test
	public void shouldLoginWithBasicAuthentication() throws Exception {
		when(contextDAO.authenticate(USERNAME, PASSWORD)).thenReturn(user);
		
		MockHttpServletRequest servletRequest = new MockHttpServletRequest();
		MockHttpServletResponse servletResponse = new MockHttpServletResponse();
		
		servletRequest.setRequestURI("/openmrs/ws/fhir2/Patient?_id=aa1c7cf0-6a54-4a06-9d77-b26107ad9144");
		servletRequest.addHeader(HttpHeaders.AUTHORIZATION,
		    "Basic " + Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes(StandardCharsets.UTF_8)));
		
		authenticationFilter.doFilter(servletRequest, servletResponse, filterChain);
		
		assertThat(servletResponse.getStatus(), equalTo(200));
	}
	
	@Test
	public void shouldReturn401WhenAuthenticationFails() throws Exception {
		when(contextDAO.authenticate(USERNAME, PASSWORD)).thenThrow(new ContextAuthenticationException());
		
		MockHttpServletRequest servletRequest = new MockHttpServletRequest();
		MockHttpServletResponse servletResponse = new MockHttpServletResponse();
		
		servletRequest.setRequestURI("/openmrs/ws/fhir2/Patient?_id=aa1c7cf0-6a54-4a06-9d77-b26107ad9144");
		servletRequest.addHeader(HttpHeaders.AUTHORIZATION,
		    "Basic " + Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes(StandardCharsets.UTF_8)));
		
		authenticationFilter.doFilter(servletRequest, servletResponse, filterChain);
		
		assertThat(servletResponse.getStatus(), equalTo(401));
	}
	
	@Test
	public void shouldReturn401WhenNotUsingBasicAuth() throws Exception {
		MockHttpServletRequest servletRequest = new MockHttpServletRequest();
		MockHttpServletResponse servletResponse = new MockHttpServletResponse();
		
		servletRequest.setRequestURI("/openmrs/ws/fhir2/Patient?_id=aa1c7cf0-6a54-4a06-9d77-b26107ad9144");
		servletRequest.addHeader(HttpHeaders.AUTHORIZATION,
		    "Bearer " + Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes(StandardCharsets.UTF_8)));
		
		authenticationFilter.doFilter(servletRequest, servletResponse, filterChain);
		
		assertThat(servletResponse.getStatus(), equalTo(401));
	}
	
	@Test
	public void shouldBypassAuthenticationForConformanceStatement() throws Exception {
		MockHttpServletRequest servletRequest = new MockHttpServletRequest();
		MockHttpServletResponse servletResponse = new MockHttpServletResponse();
		
		servletRequest.setRequestURI("/openmrs/ws/fhir2/metadata");
		servletRequest.addHeader(HttpHeaders.AUTHORIZATION,
		    "Basic " + Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes(StandardCharsets.UTF_8)));
		
		authenticationFilter.doFilter(servletRequest, servletResponse, filterChain);
		
		assertThat(servletResponse.getStatus(), equalTo(200));
	}
	
	@Test
	public void shouldBypassAuthenticationForWellKnownDirectory() throws Exception {
		MockHttpServletRequest servletRequest = new MockHttpServletRequest();
		MockHttpServletResponse servletResponse = new MockHttpServletResponse();
		
		servletRequest.setRequestURI("/openmrs/ws/fhir2/.well-known/config.json");
		servletRequest.addHeader(HttpHeaders.AUTHORIZATION,
		    "Basic " + Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes(StandardCharsets.UTF_8)));
		
		authenticationFilter.doFilter(servletRequest, servletResponse, filterChain);
		
		assertThat(servletResponse.getStatus(), equalTo(200));
	}
}
