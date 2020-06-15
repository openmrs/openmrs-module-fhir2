/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.servlet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.web.servlet.FhirSmartConfigServlet;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class FhirSmartConfigServletTest {
	
	private FhirSmartConfigServlet servlet;
	
	@Before
	public void setup() {
		servlet = new FhirSmartConfigServlet();
	}
	
	@Test
	public void shouldReturn200Status() throws Exception {
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/ws/fhir2/R4/.well-known/smart-configuration");
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.doGet(request, response);
		
		assertThat(response.getStatus(), equalTo(200));
		
	}
}
