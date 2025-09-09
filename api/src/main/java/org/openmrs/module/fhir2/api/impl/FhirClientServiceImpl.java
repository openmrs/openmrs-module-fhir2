/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.openmrs.module.fhir2.api.FhirClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class FhirClientServiceImpl implements FhirClientService {
	
	private final FhirContext fhirR3;
	
	private final FhirContext fhirR4;
	
	@Autowired
	public FhirClientServiceImpl(@Qualifier("fhirR3") FhirContext fhirR3, @Qualifier("fhirR4") FhirContext fhirR4) {
		this.fhirR3 = fhirR3;
		this.fhirR4 = fhirR4;
	}
	
	@Override
	public IGenericClient getClientForR3(String baseUrl) {
		return fhirR3.newRestfulGenericClient(baseUrl);
	}
	
	@Override
	public IGenericClient getClientForR4(String baseUrl) {
		return fhirR4.newRestfulGenericClient(baseUrl);
	}
}
