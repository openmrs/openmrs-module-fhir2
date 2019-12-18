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

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import java.util.Collection;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.module.fhir2.FhirConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.MODULE)
public class FhirRestServlet extends RestfulServer {
	
	private static final long serialVersionUID = 1L;
	
	@Inject
	private LoggingInterceptor loggingInterceptor;
	
	@Override
	protected void initialize() {
		// ensure properties for this class are properly injected
		// TODO find a way around this hack!
		try {
			((ServiceContext) FieldUtils.readStaticField(Context.class, "serviceContext", true)).getApplicationContext()
			        .getAutowireCapableBeanFactory().autowireBean(this);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		setDefaultResponseEncoding(EncodingEnum.JSON);
		registerInterceptor(loggingInterceptor);
	}
	
	@Override
	protected String createPoweredByHeaderComponentName() {
		return FhirConstants.OPENMRS_FHIR_SERVER_NAME;
	}
	
	@Override
	protected String getRequestPath(String requestFullPath, String servletContextPath, String servletPath) {
		return requestFullPath.substring(escapedLength(servletContextPath) + escapedLength(servletPath)
		        + escapedLength("/fhir2Servlet"));
	}
	
	@Override
	@Inject
	@Named("fhirR4")
	public void setFhirContext(FhirContext theFhirContext) {
		super.setFhirContext(theFhirContext);
	}
	
	@Override
	@Autowired
	@Qualifier("fhirResources")
	public void setResourceProviders(Collection<IResourceProvider> theProviders) {
		super.setResourceProviders(theProviders);
	}
}
