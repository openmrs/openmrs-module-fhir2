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
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ForwardingFilterTest {
	
	private ForwardingFilter filter;
	
	@Before
	public void setup() {
		filter = new ForwardingFilter();
	}
	
	@Test
	public void shouldRedirectForR4() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/ws/fhir2/R4/Person");
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		filter.doFilter(request, response, new MockFilterChain());
		
		assertThat(response.getForwardedUrl(), notNullValue());
		assertThat(response.getForwardedUrl(), equalTo("/ms/fhir2Servlet/Person"));
	}
	
	@Test
	public void shouldRedirectForR3() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/ws/fhir2/R3/Person");
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		filter.doFilter(request, response, new MockFilterChain());
		
		assertThat(response.getForwardedUrl(), notNullValue());
		assertThat(response.getForwardedUrl(), equalTo("/ms/fhir2R3Servlet/Person"));
	}
	
	@Test
	public void shouldReturn404WhenUsedWithoutVersion() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/ws/fhir2/Person");
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		filter.doFilter(request, response, new MockFilterChain());
		
		assertThat(response.getStatus(), equalTo(404));
	}
	
	@Test
	public void shouldReturn404WhenUsedWithInvalidVersion() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/ws/fhir2/R27/Person");
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		filter.doFilter(request, response, new MockFilterChain());
		
		assertThat(response.getStatus(), equalTo(404));
	}
	
	@Test
	public void shouldReturn404WhenUsedWithEmptyVersion() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/ws/fhir2//Person");
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		filter.doFilter(request, response, new MockFilterChain());
		
		assertThat(response.getStatus(), equalTo(404));
	}
	
	@Test
	public void shouldReturn404WhenUsedWithoutPath() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/ws/fhir2");
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		filter.doFilter(request, response, new MockFilterChain());
		
		assertThat(response.getStatus(), equalTo(404));
	}
	
	@Test
	public void shouldReturn404WhenUsedWithoutPathAndUrlEndsWithSlash() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/ws/fhir2/");
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		filter.doFilter(request, response, new MockFilterChain());
		
		assertThat(response.getStatus(), equalTo(404));
	}
}
