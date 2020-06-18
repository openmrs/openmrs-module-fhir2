/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.smart;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.User;
import org.openmrs.api.context.Authenticated;
import org.openmrs.api.context.AuthenticationScheme;
import org.openmrs.api.context.BasicAuthenticated;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.Credentials;
import org.openmrs.api.context.UsernamePasswordAuthenticationScheme;
import org.openmrs.api.context.UsernamePasswordCredentials;
import org.openmrs.module.fhir2.api.dao.FhirUserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A scheme that authenticates with OpenMRS based on the 'username'.
 */
@Component
@Slf4j
public class SmartAuthenticationScheme implements AuthenticationScheme {
	
	private volatile UsernamePasswordAuthenticationScheme delegate = null;
	
	@Autowired
	private FhirUserDao dao;
	
	@Override
	public Authenticated authenticate(Credentials credentials) throws ContextAuthenticationException {
		if (!(credentials instanceof SmartTokenCredentials)) {
			// fall back to the default authentication scheme if we cannot handle these credentials
			if (credentials instanceof UsernamePasswordCredentials) {
				if (delegate == null) {
					synchronized (this) {
						if (delegate == null) {
							delegate = new UsernamePasswordAuthenticationScheme();
						}
					}
				}
				
				return delegate.authenticate(credentials);
			}
			
			throw new ContextAuthenticationException("Invalid credentials");
		}
		
		User user = null;
		try {
			user = dao.getUserByUserName(credentials.getClientName());
		}
		catch (Exception ignored) {
			
		}
		
		if (user == null) {
			throw new ContextAuthenticationException("Invalid credentials");
		}
		
		return new BasicAuthenticated(user, credentials.getAuthenticationScheme());
	}
}
