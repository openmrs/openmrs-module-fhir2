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

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.NodesRegistrationManagement;
import org.keycloak.adapters.OidcKeycloakAccount;
import org.keycloak.adapters.servlet.KeycloakOIDCFilter;
import org.keycloak.adapters.spi.KeycloakAccount;
import org.openmrs.api.context.Authenticated;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.fhir2.web.smart.SmartTokenCredentials;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Slf4j
public class SmartAuthenticationFilter extends KeycloakOIDCFilter {
	
	private static final Pattern ACCESS_TOKEN = Pattern.compile("(&?access_token=[^&]+)");
	
	@Override
	public void init(FilterConfig filterConfig) {
		final Resource keycloakConfig = getKeycloakConfig();
		
		if (keycloakConfig == null) {
			log.error("Could not find Keycloak configuration file");
			this.deploymentContext = new AdapterDeploymentContext(new KeycloakDeployment());
		} else {
			try {
				this.deploymentContext = new AdapterDeploymentContext(
				        KeycloakDeploymentBuilder.build(keycloakConfig.getInputStream()));
			}
			catch (IOException e) {
				log.error("Error while trying to load Keycloak configuration", e);
				this.deploymentContext = new AdapterDeploymentContext(new KeycloakDeployment());
			}
		}
		
		String skipPatternDefinition = filterConfig.getInitParameter(SKIP_PATTERN_PARAM);
		if (skipPatternDefinition != null) {
			skipPattern = Pattern.compile(skipPatternDefinition, Pattern.DOTALL);
		}
		
		filterConfig.getServletContext().setAttribute(AdapterDeploymentContext.class.getName(), deploymentContext);
		this.nodesRegistrationManagement = new NodesRegistrationManagement();
	}
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		if (req instanceof HttpServletRequest) {
			final HttpServletRequest httpRequest = (HttpServletRequest) req;
			if (httpRequest.getRequestedSessionId() != null) {
				Context.logout();
			}
			
			String authorization = httpRequest.getHeader("Authorization");
			if (!Context.isAuthenticated() && (authorization == null || !authorization.startsWith("Basic"))) {
				// KeycloakOIDCFilter does the actual request handling
				super.doFilter(req, res, (rq, rs) -> {});
				
				if (res.isCommitted()) {
					return;
				}
				
				if (httpRequest.getAttribute(KeycloakAccount.class.getName()) != null) {
					OidcKeycloakAccount account = (OidcKeycloakAccount) httpRequest
					        .getAttribute(KeycloakAccount.class.getName());
					
					String userName = account.getKeycloakSecurityContext().getToken().getPreferredUsername();
					Authenticated authenticated;
					try {
						authenticated = Context.authenticate(new SmartTokenCredentials(userName));
					}
					catch (ContextAuthenticationException e) {
						HttpServletResponse httpResponse = (HttpServletResponse) res;
						httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
						return;
					}
					
					log.debug("The user '{}' was successfully authenticated as OpenMRS user {}", userName,
					    authenticated.getUser());
					
					// attempt to remove access_token from the query string
					Matcher m = ACCESS_TOKEN.matcher(httpRequest.getRequestURI());
					if (m.matches()) {
						httpRequest.getRequestDispatcher(m.replaceFirst("")).forward(req, res);
						return;
					}
				} else {
					HttpServletResponse httpResponse = (HttpServletResponse) res;
					httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
					return;
				}
			}
		}
		
		chain.doFilter(req, res);
	}
	
	private Resource getKeycloakConfig() {
		final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource resource = resolver.getResource(
		    OpenmrsUtil.getDirectoryInApplicationDataDirectory("config") + File.separator + "smart-keycloak.json");
		if (resource != null) {
			resource = resolver.getResource("classpath:smart-keycloak.json");
		}
		
		return resource;
	}
}
