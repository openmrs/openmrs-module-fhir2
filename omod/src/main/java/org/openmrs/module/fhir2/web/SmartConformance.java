/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web;

import lombok.Data;

@Data
public class SmartConformance {
	
	private String authorization_endpoint;
	
	private String token_endpoint;
	
	private String[] token_endpoint_auth_methods_supported;
	
	private String registration_endpoint;
	
	private String[] scopes_supported;
	
	private String[] response_types_supported;
	
	private String management_endpoint;
	
	private String introspection_endpoint;
	
	private String revocation_endpoint;
	
	private String[] capabilities;
	
}
