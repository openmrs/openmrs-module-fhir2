/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.r4;

import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

public class PatientRestClient {

    FhirContext ctx = FhirContext.forR4();
    IGenericClient client = ctx.newRestfulGenericClient("https://qa-refapp.openmrs.org/openmrs/ws/fhir2/R4/patient");
    client.registerInterceptor(new BasicAuthInterceptor("admin", "Admin123"));

    Patient patient;
    try {
        patient = client.read().resource(Patient.class).withId("30e2aa2a-4ed1-415d-84c5-ba29016c14b7").execute();
    } catch (ResourceNotFoundException e) {
        System.out.println("Resource not found!");
        return;
    }

    String string = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(patient);
    System.out.println(string);
}

}


	
