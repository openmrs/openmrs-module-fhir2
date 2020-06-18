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
package org.openmrs.module.fhir2.web.smart;

import org.openmrs.api.context.Credentials;

/**
 * Credentials consisting of the token received from the OAuth 2 provider. In pactise this token is
 * made of the user info sent over from the OAuth 2 provider.
 */
public class SmartTokenCredentials implements Credentials {
	
	final public static String SCHEME_NAME = "SMART_AUTH_SCHEME";
	
	private final String smartUser;
	
	public SmartTokenCredentials(String smartUser) {
		this.smartUser = smartUser;
	}
	
	@Override
	public String getAuthenticationScheme() {
		return SCHEME_NAME;
	}
	
	@Override
	public String getClientName() {
		return smartUser;
	}
}
