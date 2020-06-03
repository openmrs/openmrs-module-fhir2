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

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.OidcKeycloakAccount;
import org.keycloak.adapters.servlet.KeycloakOIDCFilter;
import org.keycloak.adapters.spi.KeycloakAccount;
import org.openmrs.api.context.Context;

public class ClientAuthenticationFilter extends KeycloakOIDCFilter {
	
	public ClientAuthenticationFilter(KeycloakConfigResolver definedconfigResolver) {
		super(definedconfigResolver);
	}
	
	public ClientAuthenticationFilter() {
		super();
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
	}
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		if (req instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) req;
			if (!httpRequest.getAuthType().contains("BASIC")) {
				
				super.doFilter(req, res, chain);
				if (httpRequest.getRequestedSessionId() != null && !httpRequest.isRequestedSessionIdValid()) {
					Context.logout();
				}
				if (httpRequest.getAttribute(KeycloakAccount.class.getName()) != null) {
					OidcKeycloakAccount account = (OidcKeycloakAccount) httpRequest
					        .getAttribute(KeycloakAccount.class.getName());
					Context.becomeUser(account.getPrincipal().getName());
				} else {
					if (!res.isCommitted()) {
						HttpServletResponse httpResponse = (HttpServletResponse) res;
						httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
						return;
					}
				}
			}
		}
		
		chain.doFilter(req, res);
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}
}
