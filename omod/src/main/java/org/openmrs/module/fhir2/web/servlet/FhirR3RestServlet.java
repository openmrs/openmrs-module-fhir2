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

import java.util.Collection;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class FhirR3RestServlet extends FhirRestServlet {
	
	@Override
	protected String getRequestPath(String requestFullPath, String servletContextPath, String servletPath) {
		return requestFullPath.substring(
		    escapedLength(servletContextPath) + escapedLength(servletPath) + escapedLength("/fhir2R3Servlet"));
	}
	
	@Override
	protected String getResourceProviderListName() {
		return "fhirR3Resources";
	}
	
	@Override
	@Autowired
	@Qualifier("fhirR3")
	public void setFhirContext(FhirContext theFhirContext) {
		super.setFhirContext(theFhirContext);
	}
	
	@Override
	@Autowired
	@Qualifier("fhirR3Resources")
	public void setResourceProviders(Collection<IResourceProvider> theProviders) {
		super.setResourceProviders(theProviders);
	}
}
