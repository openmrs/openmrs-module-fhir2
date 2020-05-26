/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.controller;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WellKnownController {
	
	@RequestMapping(value = "/ws/fhir2/{fhirVersion:R[1-9][0-9]*}/.well-known/smart-configuration", method = RequestMethod.GET)
	public ResponseEntity<HashMap<String, Object>> getConfigurationData(@PathVariable("fhirVersion") String fhirVersion) {
		
		//TODO correct this
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("authorization_endpoint", "https://ehr.example.com/auth/authorize");
		hashMap.put("token_endpoint", "https://ehr.example.com/auth/token");
		hashMap.put("token_endpoint_auth_methods_supported", "[\"client_secret_basic\"]");
		hashMap.put("registration_endpoint", "https://ehr.example.com/auth/register");
		hashMap.put("scopes_supported",
		    "[\"openid\", \"profile\", \"launch\", \"launch/patient\", \"patient/*.*\", \"user/*.*\", \"offline_access\"]");
		hashMap.put("response_types_supported", "[\"code\", \"code id_token\", \"id_token\", \"refresh_token\"]");
		hashMap.put("introspection_endpoint", "https://ehr.example.com/user/manage");
		hashMap.put("revocation_endpoint", "https://ehr.example.com/user/introspect");
		hashMap.put("capabilities", "https://ehr.example.com/user/revoke");
		hashMap.put("management_endpoint",
		    "[\"launch-ehr\", \"client-public\", \"client-confidential-symmetric\", \"context-ehr-patient\", \"sso-openid-connect\"]");
		
		return new ResponseEntity<HashMap<String, Object>>(hashMap, HttpStatus.OK);
	}
}
