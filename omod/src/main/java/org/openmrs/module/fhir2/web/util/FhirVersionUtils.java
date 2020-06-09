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

import javax.servlet.http.HttpServletRequest;

public class FhirVersionUtils {
	
	public enum FhirVersion {
		UNKNOWN,
		R3,
		R4
	}
	
	public static FhirVersion getFhirVersion(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		String contextPath = request.getContextPath();
		
		String prefix = contextPath + "/ws/fhir2";
		if (requestURI.startsWith(prefix)) {
			int prefixIdx = requestURI.indexOf(prefix);
			if (prefixIdx >= 0) {
				int prefixEnd = prefixIdx + prefix.length();
				int pathIdx = requestURI.indexOf('/', prefixEnd + 1);
				
				if (pathIdx >= 0) {
					String version = requestURI.substring(prefixEnd, pathIdx);
					
					if (version.isEmpty()) {
						return FhirVersion.UNKNOWN;
					}
					
					if (version.charAt(0) == '/') {
						version = version.substring(1);
					}
					
					switch (version) {
						case "R3":
							return FhirVersion.R3;
						case "R4":
							return FhirVersion.R4;
						default:
							return FhirVersion.UNKNOWN;
					}
				}
			}
		}
		
		return FhirVersion.UNKNOWN;
	}
}
