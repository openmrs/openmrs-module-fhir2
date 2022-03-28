package org.openmrs.module.fhir2.provider.fhirRestTests;

import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertThat;

import java.io.IOException;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;

import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
//import org.slf4j.Logger;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;



public class ServerTest {

	private static final String PATIENT_UUID = "30e2aa2a-4ed1-415d-84c5-ba29016c14b7";

    private static IGenericClient client;

	@Before
	public void setup() {
		FhirContext context = FhirContext.forR4();
		client = context.newRestfulGenericClient("https://qa-refapp.openmrs.org/openmrs/ws/fhir2/R4");
		client.registerInterceptor(new BasicAuthInterceptor("admin", "Admin123"));
	}

	@Test
	public void shouldLoadExistingPatientAsJson() throws IOException {
		
		Patient patient = new Patient();

		client.read().resource(Patient.class).withId(PATIENT_UUID).accept("application/json").execute();
		assertEquals(patient.getId(), equals(PATIENT_UUID));
	}
    
}
