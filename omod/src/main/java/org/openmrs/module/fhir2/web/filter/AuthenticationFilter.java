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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;

public class AuthenticationFilter implements Filter {
	
	@Override
	public void init(FilterConfig filterConfig) {
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	        throws IOException, ServletException {
		// skip if the session has timed out, we're already authenticated, or it's not an HTTP request
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			if (httpRequest.getRequestedSessionId() != null && !httpRequest.isRequestedSessionIdValid()) {
				Context.logout();
			}
			
			if (!Context.isAuthenticated()) {
				String basicAuth = httpRequest.getHeader("Authorization");
				if (!StringUtils.isBlank(basicAuth) && basicAuth.startsWith("Basic")) {
					// this is "Basic ${base64encode(username + ":" + password)}"
					try {
						basicAuth = basicAuth.substring(6); // remove the leading "Basic "
						String decoded = new String(Base64.decodeBase64(basicAuth), StandardCharsets.UTF_8);
						String[] userAndPass = decoded.split(":");
						Context.authenticate(userAndPass[0], userAndPass[1]);
					}
					catch (Exception e) {
						HttpServletResponse httpResponse = (HttpServletResponse) response;
						httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
						return;
					}
				} else {
					// This sends 401 error if not authenticated
					HttpServletResponse httpResponse = (HttpServletResponse) response;
					httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
					return;
				}
			}
		}
		
		if (!response.isCommitted()) {
			chain.doFilter(request, response);
		}
	}
	
	@Override
	public void destroy() {
	}
}
