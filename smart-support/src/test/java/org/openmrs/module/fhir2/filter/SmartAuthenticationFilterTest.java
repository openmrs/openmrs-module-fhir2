/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.web.filter.SmartAuthenticationFilter;
import org.openmrs.module.fhir2.web.smart.SmartTokenCredentials;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;

@SkipBaseSetup
@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class SmartAuthenticationFilterTest extends BaseModuleContextSensitiveTest {
	
	private SmartAuthenticationFilter filter;
	
	@Mock
	private User user;
	
	@Before
	public void setup() {
		filter = new SmartAuthenticationFilter();
		user = new User();
		user.setUsername("admin");
	}
	
	@Test
	public void shouldReturnUsername() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		
		request.setMethod("GET");
		request.addHeader("Authorization", "Bearer abcd1234");
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		when(Context.authenticate(new SmartTokenCredentials("admin")).getUser()).thenReturn(user);
		
		filter.doFilter(request, response, new MockFilterChain());
		
		assertThat(user.getUsername(), notNullValue());
		assertThat(user.getUsername(), equalTo("admin"));
		assertThat(response.getStatus(), equalTo(200));
	}
}
