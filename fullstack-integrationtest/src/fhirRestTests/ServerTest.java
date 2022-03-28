/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.fullstack-integrationtest.fhirRestTests;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;

import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;


public class ServerTest {

	private static final String PATIENT_UUID = "9a79b2e3-b2f3-4d01-8304-4193f273dee8";

      private static IGenericClient client;

	@Before
	public void setup() {
		FhirContext context = FhirContext.forR4();
		client = context.newRestfulGenericClient("https://qa-refapp.openmrs.org/openmrs/ws/fhir2/R4/");
		client.registerInterceptor(new BasicAuthInterceptor("admin", "Admin123"));
	}

	@Test
	public void shouldLoadExistingPatientAsJson() throws IOException {
		
		client.read().resource(Patient.class).withId(PATIENT_UUID).accept("application/json").execute();
		assertThat(patient.getId(), equalTo(PATIENT_UUID));
	}
    
}
