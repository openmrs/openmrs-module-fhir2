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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class RequireAuthenticationInterceptorTest {
	
	private MockHttpServletRequest request;
	
	private MockHttpServletResponse response;
	
	@Before
	public void setup() throws ServletException {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}
	
	@Test
	public void ensureUserAuthenticated_shouldReturnTrueGivenWellKnownUri() throws Exception {
		// setup
		request.setRequestURI("/.well-known");
		
		// replay and verify
		RequireAuthenticationInterceptor interceptor = getInterceptorForTesting(true);
		assertThat(interceptor.ensureUserAuthenticated(request, response), is(true));
	}
	
	@Test
	public void ensureUserAuthenticated_shouldReturnTrueGivenMetadataUri() throws Exception {
		// setup
		request.setRequestURI("/metadata");
		
		// replay and verify
		RequireAuthenticationInterceptor interceptor = getInterceptorForTesting(true);
		assertThat(interceptor.ensureUserAuthenticated(request, response), is(true));
	}
	
	@Test
	public void ensureUserAuthenticated_shouldReturnFalseGivenUserIsNotAuthenticated() throws Exception {
		// setup
		request.setRequestURI("/ws/fhir2/R4/Someresource");
		
		// replay and verify
		RequireAuthenticationInterceptor interceptor = getInterceptorForTesting(false);
		assertThat(interceptor.ensureUserAuthenticated(request, response), is(false));
		assertThat(response.getErrorMessage(), is("Not authenticated"));
		assertThat(response.getStatus(), is(HttpServletResponse.SC_UNAUTHORIZED));
	}
	
	@Test
	public void ensureUserAuthenticated_shouldReturnTrueGivenUserIsAuthenticated() throws Exception {
		// setup
		request.setRequestURI("/ws/fhir2/R4/Someresource");
		
		// replay and verify
		RequireAuthenticationInterceptor interceptor = getInterceptorForTesting(true);
		assertThat(interceptor.ensureUserAuthenticated(request, response), is(true));
	}
	
	public RequireAuthenticationInterceptor getInterceptorForTesting(boolean isUserAuthenticated) {
		return new RequireAuthenticationInterceptor() {
			
			@Override
			protected boolean isAuthenticated() {
				return isUserAuthenticated;
			}
		};
	}
}
