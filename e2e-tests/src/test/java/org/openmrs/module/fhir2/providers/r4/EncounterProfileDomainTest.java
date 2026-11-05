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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;

/**
 * Round-trips a visit-backed Encounter through the composite Encounter service on a running server:
 * create it by naming the visit handler's profile, read it back, and find it by {@code _profile}.
 * Expects the Reference Application, whose metadata provides the "Facility Visit" visit type.
 */
public class EncounterProfileDomainTest {
	
	private static final String SERVER_BASE = "http://localhost:8080/openmrs/ws/fhir2/R4";
	
	private static final FhirContext FHIR_CONTEXT = FhirContext.forR4();
	
	private static final IGenericClient CLIENT = FHIR_CONTEXT.newRestfulGenericClient(SERVER_BASE);
	
	private static final String CREDENTIAL_STRING = "admin:Admin123";
	
	private static final String VISIT_PROFILE = FhirConstants.OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX + "/openmrs-visit";
	
	private static final String ENCOUNTER_PROFILE = FhirConstants.OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX
	        + "/openmrs-encounter";
	
	private static final String FACILITY_VISIT_TYPE_UUID = "7b0f5697-27e3-40c4-8bae-f4049abfb4ed";
	
	// "OpenMRS ID" values carry a Luhn mod-30 check character, so this cannot be an arbitrary string
	private static final String PATIENT_IDENTIFIER = "1000PRM";
	
	private static final String PATIENT_LOCATION_UUID = "58c57d25-8d39-41ab-8422-108a0c277d98";
	
	private static String patientUuid;
	
	private static String visitUuid;
	
	@BeforeClass
	public static void createPatientAndVisit() {
		FHIR_CONTEXT.getRestfulClientFactory().setConnectTimeout(200 * 1000);
		CLIENT.registerInterceptor(new BasicAuthInterceptor(CREDENTIAL_STRING));
		
		Patient patient = new Patient();
		patient.addName(new HumanName().setFamily("Profile").addGiven("Routing"));
		patient.setGender(Enumerations.AdministrativeGender.FEMALE);
		Reference identifierLocation = new Reference().setReference(FhirConstants.LOCATION + "/" + PATIENT_LOCATION_UUID)
		        .setType(FhirConstants.LOCATION);
		Identifier identifier = new Identifier().setType(new CodeableConcept().setText("OpenMRS ID"))
		        .setValue(PATIENT_IDENTIFIER);
		identifier.addExtension(
		    new Extension().setUrl(FhirConstants.OPENMRS_FHIR_EXT_PATIENT_IDENTIFIER_LOCATION).setValue(identifierLocation));
		patient.addIdentifier(identifier);
		patientUuid = CLIENT.create().resource(patient).execute().getId().getIdPart();
		
		Encounter visit = new Encounter();
		visit.getMeta().addProfile(VISIT_PROFILE);
		visit.setStatus(Encounter.EncounterStatus.INPROGRESS);
		visit.addType(new CodeableConcept()
		        .addCoding(new Coding(FhirConstants.VISIT_TYPE_SYSTEM_URI, FACILITY_VISIT_TYPE_UUID, "Facility Visit")));
		visit.setSubject(new Reference(FhirConstants.PATIENT + "/" + patientUuid));
		visit.setPeriod(new Period().setStart(new Date()));
		
		MethodOutcome outcome = CLIENT.create().resource(visit).execute();
		
		assertThat(outcome.getResource(), instanceOf(Encounter.class));
		Encounter created = (Encounter) outcome.getResource();
		visitUuid = created.getIdElement().getIdPart();
		assertThat(visitUuid, notNullValue());
		assertThat(profilesOf(created), contains(VISIT_PROFILE));
	}
	
	@Test
	public void shouldReadBackVisitWithVisitProfile() {
		// when
		Encounter encounter = CLIENT.read().resource(Encounter.class).withId(visitUuid).execute();
		
		// then
		assertThat(profilesOf(encounter), contains(VISIT_PROFILE));
	}
	
	@Test
	public void shouldFindVisitWhenSearchingByVisitProfile() {
		// when
		List<Encounter> results = searchSubjectEncounters(VISIT_PROFILE);
		
		// then
		assertThat(idsOf(results), hasItem(visitUuid));
		assertThat(results.stream().map(EncounterProfileDomainTest::profilesOf).collect(Collectors.toList()),
		    everyItem(contains(VISIT_PROFILE)));
	}
	
	@Test
	public void shouldNotFindVisitWhenSearchingByEncounterProfile() {
		// when
		List<Encounter> results = searchSubjectEncounters(ENCOUNTER_PROFILE);
		
		// then
		assertThat(idsOf(results), not(hasItem(visitUuid)));
	}
	
	@AfterClass
	public static void deletePatientAndVisit() {
		if (visitUuid != null) {
			CLIENT.delete().resourceById(new IdType(FhirConstants.ENCOUNTER, visitUuid)).execute();
		}
		
		if (patientUuid != null) {
			CLIENT.delete().resourceById(new IdType(FhirConstants.PATIENT, patientUuid)).execute();
		}
	}
	
	private static List<Encounter> searchSubjectEncounters(String profile) {
		Bundle bundle = CLIENT.search().forResource(Encounter.class)
		        .where(Encounter.SUBJECT.hasId(FhirConstants.PATIENT + "/" + patientUuid)).withProfile(profile)
		        .returnBundle(Bundle.class).execute();
		
		return bundle.getEntry().stream().map(Bundle.BundleEntryComponent::getResource).filter(Encounter.class::isInstance)
		        .map(Encounter.class::cast).collect(Collectors.toList());
	}
	
	private static List<String> idsOf(List<Encounter> encounters) {
		return encounters.stream().map(e -> e.getIdElement().getIdPart()).collect(Collectors.toList());
	}
	
	private static List<String> profilesOf(IBaseResource resource) {
		return resource.getMeta().getProfile().stream().map(IPrimitiveType::getValue).collect(Collectors.toList());
	}
}
