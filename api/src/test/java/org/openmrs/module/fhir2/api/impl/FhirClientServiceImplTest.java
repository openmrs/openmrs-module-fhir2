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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.junit.Before;
import org.junit.Test;

public class FhirClientServiceImplTest {
	
	private static final String R3_URL = "https://demo.openmrs.org/openmrs/ws/fhir2/R3";
	
	private static final String R4_URL = "https://demo.openmrs.org/openmrs/ws/fhir2/R4";
	
	private FhirClientServiceImpl fhirClientService;
	
	@Before
	public void setup() {
		FhirContext fhirR3 = FhirContext.forDstu3Cached();
		FhirContext fhirR4 = FhirContext.forR4Cached();
		
		fhirClientService = new FhirClientServiceImpl(fhirR3, fhirR4);
	}
	
	@Test
	public void getClientForR3_shouldReturnAValidClient() {
		IGenericClient client3 = fhirClientService.getClientForR3(R3_URL);
		assertThat(client3, notNullValue());
		assertThat(client3.getFhirContext().getVersion().getVersion(), equalTo(FhirVersionEnum.DSTU3));
		assertThat(client3.getServerBase(), equalTo(R3_URL));
	}
	
	@Test
	public void getClientForR4_shouldReturnaValidClient() {
		IGenericClient client4 = fhirClientService.getClientForR4(R4_URL);
		assertThat(client4, notNullValue());
		assertThat(client4.getFhirContext().getVersion().getVersion(), equalTo(FhirVersionEnum.R4));
		assertThat(client4.getServerBase(), equalTo(R4_URL));
	}
	
}
