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
import javax.servlet.http.HttpServletRequestWrapper;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.apache.commons.lang.StringUtils;

@Interceptor
public class SupportMergePatchInterceptor {
	
	private static final String JSON_MERGE_PATCH_CONTENT_TYPE = "application/merge-patch+json";
	
	private static final String OPENMRS_REQUEST_HOLD = "_openmrs_patch_request_holder";
	
	private static final class MergePatchRequestWrapper extends HttpServletRequestWrapper {
		
		public MergePatchRequestWrapper(HttpServletRequest request) {
			super(request);
		}
		
		@Override
		public String getHeader(String name) {
			if (Constants.HEADER_CONTENT_TYPE.equalsIgnoreCase(name)) {
				return PatchTypeEnum.JSON_PATCH.getContentType();
			}
			
			return super.getHeader(name);
		}
	}
	
	@Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLER_SELECTED)
	public boolean handleIncomingPatchRequest(RequestDetails requestDetails) {
		if (requestDetails.getRequestType() == RequestTypeEnum.PATCH && requestDetails instanceof ServletRequestDetails) {
			ServletRequestDetails srd = (ServletRequestDetails) requestDetails;
			
			String originalContentType = StringUtils.defaultString(srd.getHeader(Constants.HEADER_CONTENT_TYPE));
			int semiColonIdx = originalContentType.indexOf(';');
			
			String sniffedContentType;
			if (semiColonIdx != -1) {
				sniffedContentType = originalContentType.substring(0, semiColonIdx).trim();
			} else {
				sniffedContentType = originalContentType.trim();
			}
			
			if (JSON_MERGE_PATCH_CONTENT_TYPE.equalsIgnoreCase(sniffedContentType)) {
				srd.setAttribute(OPENMRS_REQUEST_HOLD, srd.getServletRequest());
				srd.setServletRequest(new MergePatchRequestWrapper(srd.getServletRequest()));
			}
		}
		
		return true;
	}
	
	@Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLED)
	public void handlePostProcessedPatchRequest(RequestDetails requestDetails) {
		if (requestDetails.getRequestType() == RequestTypeEnum.PATCH && requestDetails instanceof ServletRequestDetails) {
			Object originalServletRequest = requestDetails.getAttribute(OPENMRS_REQUEST_HOLD);
			if (originalServletRequest instanceof HttpServletRequest) {
				((ServletRequestDetails) requestDetails).setServletRequest((HttpServletRequest) originalServletRequest);
			}
		}
		
		requestDetails.setAttribute(OPENMRS_REQUEST_HOLD, null);
	}
	
}
