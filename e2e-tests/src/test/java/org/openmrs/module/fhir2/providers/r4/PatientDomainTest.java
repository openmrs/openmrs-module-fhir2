/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.UUID;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.server.exceptions.ResourceGoneException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.google.common.collect.Sets;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;

public class PatientDomainTest {
	
	private static final String SERVER_BASE = "http://localhost:8080/openmrs/ws/fhir2/R4";
	
	private static final FhirContext FHIR_CONTEXT = FhirContext.forR4();
	
	private static final IGenericClient CLIENT = FHIR_CONTEXT.newRestfulGenericClient(SERVER_BASE);
	
	private final String NON_EXISTING_PATIENT_UUID = UUID.randomUUID().toString();
	
	private static String patientUuid = "34d45fr6-f3a9-4f46-8341-15f0de3d8476";
	
	private static final String PATIENT_FAMILY_NAME_JAMESON = "Jameson";
	
	private static final String PATIENT_GIVEN_NAME_JONAH = "Jonah";
	
	private static final String PATIENT_LOCATION_UUID = "58c57d25-8d39-41ab-8422-108a0c277d98";
	
	private static final String PATIENT_IDENTIFIER_ID = "1000H7Y";
	
	private static final String PATIENT_CONCEPT_TEXT = "OpenMRS ID";
	
	private static final String CREDENTIAL_STRING = "admin:Admin123";
	
	@BeforeClass
	public static void authenticate() {
		FHIR_CONTEXT.getRestfulClientFactory().setConnectTimeout(200 * 1000);
		CLIENT.registerInterceptor(new BasicAuthInterceptor(CREDENTIAL_STRING));
	}
	
	@BeforeClass
	public static void shouldCreatePatient() throws Exception {
		CodeableConcept concept = new CodeableConcept();
		concept.setText(PATIENT_CONCEPT_TEXT);
		
		HumanName name = new HumanName();
		name.setId(UUID.randomUUID().toString());
		name.setFamily(PATIENT_FAMILY_NAME_JAMESON);
		name.addGiven(PATIENT_GIVEN_NAME_JONAH);
		
		Reference patientReference = new Reference().setReference(FhirConstants.LOCATION + "/" + PATIENT_LOCATION_UUID)
		        .setType(FhirConstants.LOCATION).setIdentifier(new Identifier().setValue(PATIENT_LOCATION_UUID));
		
		Identifier identifier = new Identifier();
		identifier.setId(UUID.randomUUID().toString());
		identifier.setType(concept);
		identifier.setValue(PATIENT_IDENTIFIER_ID);
		identifier.addExtension(
		    new Extension().setUrl(FhirConstants.OPENMRS_FHIR_EXT_PATIENT_IDENTIFIER_LOCATION).setValue(patientReference));
		
		Patient newPatient = new Patient();
		newPatient.setIdentifier(new ArrayList<>(Sets.newHashSet(identifier)));
		newPatient.setName(new ArrayList<>(Sets.newHashSet(name)));
		newPatient.setGender(Enumerations.AdministrativeGender.MALE);
		newPatient.setId(patientUuid);
		
		final MethodOutcome outcome = CLIENT.create().resource(newPatient).prettyPrint().encodedJson().execute();
		assertThat(outcome.getResource(), notNullValue());
		assertThat(outcome.getResource(), instanceOf(Patient.class));
		
		Patient returned = (Patient) outcome.getResource();
		patientUuid = returned.getIdElement().getIdPart();
		assertThat(returned, notNullValue());
		assertThat(returned.getIdElement().getIdPart(), notNullValue());
		assertThat(returned.getGender(), equalTo(Enumerations.AdministrativeGender.MALE));
		assertThat(returned.getName().get(0).getGiven().get(0).toString(), equalToIgnoringCase(PATIENT_GIVEN_NAME_JONAH));
		assertThat(returned.getName().get(0).getFamily(), equalToIgnoringCase(PATIENT_FAMILY_NAME_JAMESON));
	}
	
	@Test
	public void shouldGetPatientDomainByUUID() {
		Patient patient = CLIENT.read().resource(Patient.class).withId(patientUuid).execute();
		assertThat(patient, notNullValue());
		assertThat(patient.getIdElement().getIdPart(), equalTo(patientUuid));
		assertThat(patient.getGender(), equalTo(Enumerations.AdministrativeGender.MALE));
		assertThat(patient.getName().get(0).getGiven().get(0).toString(), equalToIgnoringCase(PATIENT_GIVEN_NAME_JONAH));
		assertThat(patient.getName().get(0).getFamily(), equalToIgnoringCase(PATIENT_FAMILY_NAME_JAMESON));
	}
	
	@Test
	public void shouldThrow404WithNonExistingPatient() {
		String expectedErrorMessage = "HTTP 404 : Resource of type Patient with ID " + NON_EXISTING_PATIENT_UUID
		        + " is not known";
		Throwable e = assertThrows(ResourceNotFoundException.class,
		    () -> CLIENT.read().resource(Patient.class).withId(NON_EXISTING_PATIENT_UUID).execute());
		assertThat(e.getMessage(), is(expectedErrorMessage));
	}
	
	@Test
	public void updatePatientByUUID() {
		HumanName name = new HumanName();
		name.setId(UUID.randomUUID().toString());
		String patientFamilyNameWhite = "White";
		name.setFamily(patientFamilyNameWhite);
		String patientGivenNameLinda = "Linda";
		name.addGiven(patientGivenNameLinda);
		
		Patient patient = new Patient();
		patient.setId(patientUuid);
		patient.setName(new ArrayList<>(Sets.newHashSet(name)));
		patient.setGender(Enumerations.AdministrativeGender.FEMALE);
		
		final MethodOutcome outcome = CLIENT.update().resource(patient).execute();
		assertThat(outcome.getResource(), notNullValue());
		assertThat(outcome.getResource(), instanceOf(Patient.class));
		
		Patient returned = (Patient) outcome.getResource();
		assertThat(returned.getIdElement().getIdPart(), equalTo(patientUuid));
		assertThat(returned.getGender(), equalTo(Enumerations.AdministrativeGender.FEMALE));
		assertThat(patient.getName().get(0).getGiven().get(0).toString(), equalToIgnoringCase(patientGivenNameLinda));
		assertThat(patient.getName().get(0).getFamily(), equalToIgnoringCase(patientFamilyNameWhite));
	}
	
	@AfterClass
	public static void shouldDeletePatientByUUID() {
		CLIENT.delete().resourceById(new IdType(FhirConstants.PATIENT + "/" + patientUuid)).execute();
		String expectedErrorMessage = "HTTP 410 : Resource of type Patient with ID " + patientUuid + " is gone/deleted";
		Throwable e = assertThrows(ResourceGoneException.class,
		    () -> CLIENT.read().resource(Patient.class).withId(patientUuid).execute());
		assertThat(e.getMessage(), is(expectedErrorMessage));
	}
}
