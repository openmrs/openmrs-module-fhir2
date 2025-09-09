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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import org.openmrs.api.context.Context;

@Interceptor
public class RequireAuthenticationInterceptor {
	
	@Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
	public boolean ensureUserAuthenticated(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!(request.getRequestURI().contains("/.well-known") || request.getRequestURI().endsWith("/metadata"))
		        && !isAuthenticated()) {
			// This sends 401 error if not authenticated
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
			return false;
		}
		return true;
	}
	
	protected boolean isAuthenticated() {
		return Context.isAuthenticated();
	}
}
