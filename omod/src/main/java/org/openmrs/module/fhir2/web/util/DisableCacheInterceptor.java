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
			requestDetails.getResponse().addHeader(Constants.HEADER_CACHE_CONTROL, "no-cache");
		}
		
		return true;
	}
}
