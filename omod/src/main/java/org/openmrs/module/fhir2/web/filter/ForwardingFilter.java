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

import static org.openmrs.module.fhir2.web.servlet.FhirVersionUtils.FhirVersion.R3;
import static org.openmrs.module.fhir2.web.servlet.FhirVersionUtils.FhirVersion.R4;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.openmrs.module.fhir2.web.servlet.FhirVersionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForwardingFilter implements Filter {
	
	private static final Logger log = LoggerFactory.getLogger(ForwardingFilter.class);
	
	@Override
	public void init(FilterConfig filterConfig) {
	}
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		if (req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
			HttpServletRequest request = (HttpServletRequest) req;
			String requestURI = request.getRequestURI();
			
			String contextPath = ((HttpServletRequest) req).getContextPath();
			String prefix = contextPath + "/ws/fhir2";
			Enum<FhirVersionUtils.FhirVersion> fhirVersionCase = FhirVersionUtils.getFhirResourceVersion(request);
			String fhirVersion = String.valueOf(FhirVersionUtils.getFhirResourceVersion(request));
			
			String replacement;
			if (R3.equals(fhirVersionCase)) {
				prefix += "/" + fhirVersion;
				replacement = "/ms/fhir2R3Servlet";
			} else if (R4.equals(fhirVersionCase)) {
				prefix += "/" + fhirVersion;
				replacement = "/ms/fhir2Servlet";
			} else {
				((HttpServletResponse) res).sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			String newURI = requestURI.replace(prefix, replacement);
			req.getRequestDispatcher(newURI).forward(req, res);
			return;
		}
		
		((HttpServletResponse) res).sendError(HttpServletResponse.SC_NOT_FOUND);
	}
	
	@Override
	public void destroy() {
	}
}
