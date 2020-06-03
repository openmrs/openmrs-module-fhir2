/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.module.fhir2.web.SmartConformance;
import org.springframework.stereotype.Component;

@Component
public class Fhir2SmartConfig extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		SmartConformance smartConformance = new SmartConformance();
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		smartConformance.setAuthorizationEndpoint("https://ehr.example.com/auth/authorize");
		smartConformance.setTokenEndpoint("https://ehr.example.com/auth/token");
		smartConformance.setTokenEndpointAuthMethodsSupported(new String[] { "client_secret_basic" });
		smartConformance.setRegistrationEndpoint("https://ehr.example.com/auth/register");
		smartConformance.setScopesSupported(
		    new String[] { "openid", "profile", "launch", "launch/patient", "patient/*.*", "user/*.*", "offline_access" });
		smartConformance.setResponseTypesSupported(new String[] { "code", "code id_token", "id_token", "refresh_token" });
		smartConformance.setManagementEndpoint("https://ehr.example.com/user/manage");
		smartConformance.setIntrospectionEndpoint("https://ehr.example.com/user/introspect");
		smartConformance.setRevocationEndpoint("https://ehr.example.com/user/revoke");
		smartConformance.setCapabilities(new String[] { "launch-ehr", "client-public", "client-confidential-symmetric",
		        "context-ehr-patient", "sso-openid-connect" });
		
		PrintWriter out = res.getWriter();
		res.setContentType("application/json");
		res.setCharacterEncoding("UTF-8");
		res.setStatus(200);
		objectMapper.writeValue(out, smartConformance);
		out.print(objectMapper);
		out.flush();
	}
}
