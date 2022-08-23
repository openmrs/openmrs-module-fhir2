/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.authentication;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class RequireAuthenticationInterceptorTest {
	
	private RequireAuthenticationInterceptor interceptor;
	
	private MockHttpServletRequest request;
	
	private MockHttpServletResponse response;
	
	@Before
	public void setup() throws ServletException {
		interceptor = new RequireAuthenticationInterceptor();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		
		PowerMockito.mockStatic(Context.class);
		when(Context.isAuthenticated()).thenReturn(false);
		
	}
	
	@Test
	public void ensureUserAuthenticated_shouldReturnTrueGivenWellKnownUri() throws Exception {
		// setup
		request.setRequestURI("/.well-known");
		
		// replay and verify
		assertThat(interceptor.ensureUserAuthenticated(request, response), is(true));
	}
	
	@Test
	public void ensureUserAuthenticated_shouldReturnTrueGivenMetadataUri() throws Exception {
		// setup
		request.setRequestURI("/metadata");
		
		// replay and verify
		assertThat(interceptor.ensureUserAuthenticated(request, response), is(true));
	}
	
	@Test
	public void ensureUserAuthenticated_shouldReturnFalseGivenUserIsNotAuthenticated() throws Exception {
		// setup
		request.setRequestURI("/ws/fhir2/R4/Someresource");
		
		// replay and verify
		assertThat(interceptor.ensureUserAuthenticated(request, response), is(false));
		assertThat(response.getErrorMessage(), is("Not authenticated"));
		assertThat(response.getStatus(), is(HttpServletResponse.SC_UNAUTHORIZED));
	}
	
	@Test
	public void ensureUserAuthenticated_shouldReturnTrueGivenUserIsAuthenticated() throws Exception {
		// setup
		request.setRequestURI("/ws/fhir2/R4/Someresource");
		when(Context.isAuthenticated()).thenReturn(true);
		
		// replay and verify
		assertThat(interceptor.ensureUserAuthenticated(request, response), is(true));
	}
}
