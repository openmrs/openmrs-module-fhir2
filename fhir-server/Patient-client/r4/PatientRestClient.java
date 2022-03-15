/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.Patient-client.r4;

public class PatientRestClient {

    FhirInstanceValidator instanceValidator;
    private static final String PATIENT_UUID = "30e2aa2a-4ed1-415d-84c5-ba29016c14b7";

    //Create a context
    FhirContext ctx = FhirContext.forR4();

    //Create a client
    IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/openmrs/ws/fhir2/R4");

    System.out.println("GET http://localhost:8080/openmrs/ws/fhir2/R4/Patient?identifier=PATIENT_UUID");
    
    //Read a patient with the given ID
     Patient patient = client.read().resource(Patient.class).withId(PATIENT_UUID).execute();
    
    // Print the output
    String string = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
    System.out.println(string);

    Bundle results = client
    .search()
    .forResource(Patient.class)
    .where(Patient.IDENTIFIER.exactly().systemAndCode(PATIENT_UUID))
    .returnBundle(Bundle.class)
    .execute();

    System.out.println(parser.setPrettyPrint(true).encodeResourceToString(results));
}

	
