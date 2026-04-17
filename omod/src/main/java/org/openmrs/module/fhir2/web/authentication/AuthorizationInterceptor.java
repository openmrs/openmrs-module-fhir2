/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.authentication;

import java.lang.reflect.InvocationTargetException;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.api.APIAuthenticationException;

/**
 * Interceptor that converts OpenMRS APIAuthenticationException to HAPI FHIR
 * ForbiddenOperationException to ensure proper HTTP 403 responses when users lack required
 * privileges.
 */
@Slf4j
@Interceptor
public class AuthorizationInterceptor {
	
	@Hook(Pointcut.SERVER_PRE_PROCESS_OUTGOING_EXCEPTION)
	public BaseServerResponseException handleAuthorizationException(Throwable theException) {
		// Check if APIAuthenticationException exists anywhere in the exception chain or message
		if (containsAPIAuthenticationException(theException)) {
			// Throw ForbiddenOperationException which HAPI will convert to HTTP 403
			return new ForbiddenOperationException("Insufficient privileges to perform this operation");
		}
		
		// Return true to continue processing with the original exception
		return (BaseServerResponseException) theException;
	}
	
	private boolean containsAPIAuthenticationException(Throwable throwable) {
		if (throwable == null) {
			return false;
		}
		
		// Check if the current exception is APIAuthenticationException
		if (throwable instanceof APIAuthenticationException) {
			return true;
		}
		
		// Check if the exception message contains APIAuthenticationException
		if (throwable instanceof InvocationTargetException) {
			return containsAPIAuthenticationException(((InvocationTargetException) throwable).getTargetException());
		}
		
		// Recursively check the cause
		return containsAPIAuthenticationException(throwable.getCause());
	}
}
