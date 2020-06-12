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

import static org.openmrs.module.fhir2.web.util.FhirVersionUtils.FhirVersion.R3;
import static org.openmrs.module.fhir2.web.util.FhirVersionUtils.FhirVersion.R4;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.module.fhir2.web.util.FhirVersionUtils;

@Slf4j
public class ForwardingFilter implements Filter {
	
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
			Enum<FhirVersionUtils.FhirVersion> fhirVersionCase = FhirVersionUtils.getFhirVersion(request);
			String fhirVersion = String.valueOf(FhirVersionUtils.getFhirVersion(request));
			
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
			if (requestURI.contains("/.well-known")) {
				replacement = "/ms/fhir2SmartConfig";
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
