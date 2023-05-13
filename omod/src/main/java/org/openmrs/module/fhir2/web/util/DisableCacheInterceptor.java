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

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;

@Interceptor
public class DisableCacheInterceptor {
	
	@Hook(Pointcut.SERVER_OUTGOING_RESPONSE)
	public boolean handleOutgoingResponse(RequestDetails requestDetails) {
		if (requestDetails.getRestOperationType() == RestOperationTypeEnum.READ
		        || requestDetails.getRestOperationType() == RestOperationTypeEnum.SEARCH_TYPE) {
			requestDetails.getResponse().addHeader(Constants.HEADER_CACHE_CONTROL, "no-store");
		}
		
		return true;
	}
}
