/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

import java.util.EnumSet;

import org.openmrs.module.fhir2.web.filter.SmartAuthenticationFilter;
import org.openmrs.module.fhir2.web.servlet.FhirSmartConfigServlet;
import org.springframework.stereotype.Component;
import org.springframework.web.WebApplicationInitializer;

@Component
public class SmartWebInitializer implements WebApplicationInitializer {
	
	@Override
	public void onStartup(ServletContext servletContext) {
		servletContext.getContext(servletContext.getContextPath() + "/ms").addServlet("fhir2SmartConfig",
		    FhirSmartConfigServlet.class);
		
		servletContext.getContext(servletContext.getContextPath())
		        .addFilter("smartAuthenticationFilter", SmartAuthenticationFilter.class)
		        .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/ws/fhir2", "/ws/fhir2/*",
		            "/ms/fhir2Servlet", "/ms/fhir2Servlet/*", "/ms/fhir2R3Servlet", "/ms/fhir2R3Servlet/*");
	}
}
