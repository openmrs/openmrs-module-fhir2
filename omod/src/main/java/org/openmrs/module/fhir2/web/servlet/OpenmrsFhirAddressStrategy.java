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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import ca.uhn.fhir.rest.server.IServerAddressStrategy;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class OpenmrsFhirAddressStrategy implements IServerAddressStrategy {
	
	@Autowired
	private FhirGlobalPropertyService globalPropertyService;
	
	@Override
	public String determineServerBase(ServletContext context, HttpServletRequest request) {
		String gpPrefix = globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_URI_PREFIX);
		
		if (StringUtils.isNotBlank(gpPrefix)) {
			return gpPrefix;
		}
		
		int serverPort = request.getServerPort();
		String port;
		if ("http".equalsIgnoreCase(request.getScheme())) {
			port = serverPort == 80 ? "" : ":" + serverPort;
		} else if ("https".equalsIgnoreCase(request.getScheme())) {
			port = serverPort == 443 ? "" : ":" + serverPort;
		} else {
			port = ":" + serverPort;
		}
		
		String contextPath = StringUtils.defaultString(request.getContextPath());
		if (contextPath.charAt(0) == '/') {
			if (contextPath.length() > 1) {
				contextPath = contextPath.substring(1);
			} else {
				contextPath = "";
			}
		}
		
		return request.getScheme() + "://" + request.getServerName() + port + "/" + contextPath + "/ws/fhir2/";
	}
}
