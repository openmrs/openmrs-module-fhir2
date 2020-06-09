/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.util;

import static org.openmrs.module.fhir2.web.util.FhirVersionUtils.FhirVersion.UNKNOWN;

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
			StringBuilder gpUrl = new StringBuilder().append(gpPrefix);
			if (gpPrefix.endsWith("/")) {
				gpUrl.deleteCharAt(gpUrl.length() - 1);
			}
			
			FhirVersionUtils.FhirVersion fhirVersion = FhirVersionUtils.getFhirVersion(request);
			
			return gpUrl.append('/').append(fhirVersion == UNKNOWN ? "" : fhirVersion.toString()).toString();
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
		
		FhirVersionUtils.FhirVersion fhirVersion = FhirVersionUtils.getFhirVersion(request);
		
		return request.getScheme() + "://" + request.getServerName() + port
		        + StringUtils.defaultString(request.getContextPath()) + "/ws/fhir2/"
		        + (fhirVersion == UNKNOWN ? "" : fhirVersion.toString());
	}
}
