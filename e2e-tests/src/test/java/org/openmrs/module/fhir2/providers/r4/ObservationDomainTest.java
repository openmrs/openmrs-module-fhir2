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

import java.util.UUID;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.server.exceptions.ResourceGoneException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;

public class ObservationDomainTest {
	
	private static final String SERVER_BASE = "http://localhost:8080/openmrs/ws/fhir2/R4";
	
	private static final FhirContext FHIR_CONTEXT = FhirContext.forR4();
	
	private static final IGenericClient CLIENT = FHIR_CONTEXT.newRestfulGenericClient(SERVER_BASE);
	
	private final String NON_EXISTING_OBSERVATION_UUID = UUID.randomUUID().toString();
	
	private static String observationUuid = "34d45fr6-f3a9-4f46-8341-15f0de3d8476";
	
	private static final String OBSERVATION_PATIENT_UUID = "58c57d25-8d39-41ab-8422-108a0c277d98";
	
	private static final String OBSERVATION_ENCOUNTER_UUID_1 = "58c57d25-8d39-41ab-8422-108a0c277d98";
	
	private static final String OBSERVATION_ENCOUNTER_UUID_2 = "58c57d25-8d39-41ab-8422-108a0c277d98";
	
	private static final String OBSERVATION_CONCEPT_UUID = "140AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CREDENTIAL_STRING = "admin:Admin123";
	
	@BeforeClass
	public static void authenticate() {
		FHIR_CONTEXT.getRestfulClientFactory().setConnectTimeout(200 * 1000);
		CLIENT.registerInterceptor(new BasicAuthInterceptor(CREDENTIAL_STRING));
	}
	
	@BeforeClass
	public static void shouldCreateObservation() throws Exception {
		CodeableConcept concept = new CodeableConcept();
		concept.addCoding().setCode(OBSERVATION_CONCEPT_UUID);
		
		Observation observation = new Observation();
		observation.setCode(concept);
		observation.setId(observationUuid);
		observation.setSubject(new Reference().setReference(FhirConstants.OBSERVATION + "/" + OBSERVATION_PATIENT_UUID)
		        .setType(FhirConstants.OBSERVATION).setIdentifier(new Identifier().setValue(OBSERVATION_PATIENT_UUID)));
		observation.setEncounter(new Reference().setReference(FhirConstants.ENCOUNTER + "/" + OBSERVATION_ENCOUNTER_UUID_1)
		        .setType(FhirConstants.ENCOUNTER).setIdentifier(new Identifier().setValue(OBSERVATION_ENCOUNTER_UUID_1)));
		observation.setValue(new Quantity(156).setUnit("cm"));
		
		final MethodOutcome outcome = CLIENT.create().resource(observation).prettyPrint().encodedJson().execute();
		assertThat(outcome.getResource(), notNullValue());
		assertThat(outcome.getResource(), instanceOf(Observation.class));
		
		Observation returned = (Observation) outcome.getResource();
		observationUuid = returned.getIdElement().getIdPart();
		assertThat(observation, notNullValue());
		assertThat(observation.getIdElement().getIdPart(), equalTo(observationUuid));
		assertThat(observation.getEncounter().getReference(),
		    equalTo((FhirConstants.OBSERVATION + "/" + OBSERVATION_ENCOUNTER_UUID_1)));
		assertThat(observation.getSubject().getReference(),
		    equalToIgnoringCase(FhirConstants.OBSERVATION + "/" + OBSERVATION_PATIENT_UUID));
		assertThat(observation.getCode().getCodingFirstRep().getCode(), equalToIgnoringCase(OBSERVATION_CONCEPT_UUID));
	}
	
	@Test
	public void shouldGetObservationDomainByUUID() {
		Observation observation = CLIENT.read().resource(Observation.class).withId(observationUuid).execute();
		assertThat(observation, notNullValue());
		assertThat(observation.getIdElement().getIdPart(), equalTo(observationUuid));
		assertThat(observation.getEncounter().getReference(),
		    equalTo((FhirConstants.OBSERVATION + "/" + OBSERVATION_ENCOUNTER_UUID_1)));
		assertThat(observation.getSubject().getReference(),
		    equalToIgnoringCase(FhirConstants.OBSERVATION + "/" + OBSERVATION_PATIENT_UUID));
		assertThat(observation.getCode().getCodingFirstRep().getCode(), equalToIgnoringCase(OBSERVATION_CONCEPT_UUID));
	}
	
	@Test
	public void shouldThrow404WithNonExistingObservation() {
		String expectedErrorMessage = "HTTP 404 : Resource of type Observation with ID " + NON_EXISTING_OBSERVATION_UUID
		        + " is not known";
		Throwable e = assertThrows(ResourceNotFoundException.class,
		    () -> CLIENT.read().resource(Observation.class).withId(NON_EXISTING_OBSERVATION_UUID).execute());
		assertThat(e.getMessage(), is(expectedErrorMessage));
	}
	
	@Test
	public void updateObservationByUUID() {
		HumanName name = new HumanName();
		name.setId(UUID.randomUUID().toString());
		String observationFamilyNameWhite = "White";
		name.setFamily(observationFamilyNameWhite);
		String observationGivenNameLinda = "Linda";
		name.addGiven(observationGivenNameLinda);
		
		Observation observation = new Observation();
		observation.setId(observationUuid);
		observation.setEncounter(new Reference().setReference(FhirConstants.ENCOUNTER + "/" + OBSERVATION_ENCOUNTER_UUID_2)
		        .setType(FhirConstants.ENCOUNTER).setIdentifier(new Identifier().setValue(OBSERVATION_ENCOUNTER_UUID_2)));
		
		final MethodOutcome outcome = CLIENT.update().resource(observation).execute();
		assertThat(outcome.getResource(), notNullValue());
		assertThat(outcome.getResource(), instanceOf(Observation.class));
		
		Observation returned = (Observation) outcome.getResource();
		assertThat(returned.getIdElement().getIdPart(), equalTo(observationUuid));
		assertThat(returned.getEncounter(), equalTo(FhirConstants.ENCOUNTER + "/" + OBSERVATION_ENCOUNTER_UUID_2));
	}
	
	@AfterClass
	public static void shouldDeleteObservationByUUID() {
		CLIENT.delete().resourceById(new IdType(FhirConstants.OBSERVATION + "/" + observationUuid)).execute();
		String expectedErrorMessage = "HTTP 410 : Resource of type Observation with ID " + observationUuid
		        + " is gone/deleted";
		Throwable e = assertThrows(ResourceGoneException.class,
		    () -> CLIENT.read().resource(Observation.class).withId(observationUuid).execute());
		assertThat(e.getMessage(), is(expectedErrorMessage));
	}
}
