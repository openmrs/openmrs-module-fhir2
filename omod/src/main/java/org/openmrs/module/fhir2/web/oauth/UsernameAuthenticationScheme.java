/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
///**
// * This Source Code Form is subject to the terms of the Mozilla Public License,
// * v. 2.0. If a copy of the MPL was not distributed with this file, You can
// * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
// * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
// *
// * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
// * graphic logo is a trademark of OpenMRS Inc.
// */
package org.openmrs.module.fhir2.web.oauth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Authenticated;
import org.openmrs.api.context.AuthenticationScheme;
import org.openmrs.api.context.BasicAuthenticated;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.Credentials;
import org.springframework.stereotype.Component;

/**
 * A scheme that authenticates with OpenMRS based on the 'username'.
 */
@Component
public class UsernameAuthenticationScheme implements AuthenticationScheme {
	
	protected Log log = LogFactory.getLog(getClass());
	
	@Override
	public Authenticated authenticate(Credentials credentials) throws ContextAuthenticationException {
		
		OAuth2TokenCredentials creds;
		try {
			creds = (OAuth2TokenCredentials) credentials;
		}
		catch (ClassCastException e) {
			throw new ContextAuthenticationException(
			        "The credentials provided did not match those needed for the authentication scheme.", e);
		}
		
		User user = new User();
		user.setUsername(credentials.getClientName());
		
		return new BasicAuthenticated(user, credentials.getAuthenticationScheme());
	}
}
