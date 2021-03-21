/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search.interceptor;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;

@Interceptor
public class SummaryInterceptor {
	
	@Hook(Pointcut.SERVER_INCOMING_REQUEST_POST_PROCESSED)
	public void handleSummaryCount(RequestDetails requestDetails) {
		
		if (requestDetails.getParameters().containsKey("_summary")) {
			
			if (requestDetails.getParameters().get("_summary")[0].equals("count")) {
				
				if (!requestDetails.getParameters().containsKey("_count")) {
					
					requestDetails.addParameter("_count", new String[] { "1" });
				}
			}
		}
	}
}
