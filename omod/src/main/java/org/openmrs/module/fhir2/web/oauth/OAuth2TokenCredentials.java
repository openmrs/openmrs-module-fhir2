/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.oauth;

import org.openmrs.User;
import org.openmrs.api.context.Credentials;

/**
 * Credentials consisting of the token received from the OAuth 2 provider. In pactise this token is
 * made of the user info sent over from the OAuth 2 provider.
 */
public class OAuth2TokenCredentials implements Credentials {
	
	final public static String SCHEME_NAME = "USER_TOKEN_AUTH_SCHEME";
	
	private User user;
	
	public OAuth2TokenCredentials(User oauth2User) {
		this.user = oauth2User;
	}
	
	public User getOAuth2User() {
		return user;
	}
	
	@Override
	public String getAuthenticationScheme() {
		return SCHEME_NAME;
	}
	
	@Override
	public String getClientName() {
		return user.getUsername();
	}
}
