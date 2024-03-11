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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.User;
import org.openmrs.api.context.Authenticated;
import org.openmrs.api.context.AuthenticationScheme;
import org.openmrs.api.context.BasicAuthenticated;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.Credentials;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.api.context.UsernamePasswordCredentials;
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
	
	static class InMemoryAuthenticationScheme implements AuthenticationScheme {
		
		@Override
		public Authenticated authenticate(Credentials credentials) throws ContextAuthenticationException {
			if (!(credentials instanceof UsernamePasswordCredentials)) {
				throw new ContextAuthenticationException(
				        "The provided credentials could not be used to authenticated with the specified authentication scheme.");
			} else {
				UsernamePasswordCredentials userPassCreds = (UsernamePasswordCredentials) credentials;
				if (userPassCreds.getUsername().equals(USERNAME) && userPassCreds.getPassword().equals(PASSWORD)) {
					User user = new User();
					user.setUsername(userPassCreds.getUsername());
					return new BasicAuthenticated(user, "IN MEMORY AUTH SCHEME");
				} else {
					throw new ContextAuthenticationException();
				}
			}
		}
	}
	
	@Before
	public void setup() throws NoSuchFieldException, IllegalAccessException {
		Context.setDAO(contextDAO);
		
		ServiceContext mockServiceContext = mock(ServiceContext.class);
		Class<?> serviceContextHolderClass = ServiceContext.class.getDeclaredClasses()[0];
		Field instanceField = serviceContextHolderClass.getDeclaredField("instance");
		instanceField.setAccessible(true);
		instanceField.set(null, mockServiceContext);
		
		when(mockServiceContext.getRegisteredComponents(any())).thenReturn(new ArrayList<>(0));
		
		Field authSchemeField = Context.class.getDeclaredField("authenticationScheme");
		authSchemeField.setAccessible(true);
		authSchemeField.set(null, new InMemoryAuthenticationScheme());
		
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
		MockHttpServletRequest servletRequest = new MockHttpServletRequest();
		MockHttpServletResponse servletResponse = new MockHttpServletResponse();
		
		servletRequest.setRequestURI("/openmrs/ws/fhir2/Patient?_id=aa1c7cf0-6a54-4a06-9d77-b26107ad9144");
		servletRequest.addHeader(HttpHeaders.AUTHORIZATION, "Basic "
		        + Base64.getEncoder().encodeToString((USERNAME + ":" + "badpassword").getBytes(StandardCharsets.UTF_8)));
		
		authenticationFilter.doFilter(servletRequest, servletResponse, filterChain);
		
		assertThat(servletResponse.getStatus(), equalTo(401));
	}
	
	@Test
	public void shouldBypassAuthenticationForConformanceStatement() throws Exception {
		MockHttpServletRequest servletRequest = new MockHttpServletRequest();
		MockHttpServletResponse servletResponse = new MockHttpServletResponse();
		
		servletRequest.setRequestURI("/openmrs/ws/fhir2/metadata");
		
		authenticationFilter.doFilter(servletRequest, servletResponse, filterChain);
		
		assertThat(servletResponse.getStatus(), equalTo(200));
	}
	
	@Test
	public void shouldBypassAuthenticationForWellKnownDirectory() throws Exception {
		MockHttpServletRequest servletRequest = new MockHttpServletRequest();
		MockHttpServletResponse servletResponse = new MockHttpServletResponse();
		
		servletRequest.setRequestURI("/openmrs/ws/fhir2/.well-known/config.json");
		
		authenticationFilter.doFilter(servletRequest, servletResponse, filterChain);
		
		assertThat(servletResponse.getStatus(), equalTo(200));
	}
}
