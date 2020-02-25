/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.EncounterProvider;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.EncounterLocationTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterParticipantTranslator;
import org.openmrs.module.fhir2.api.translators.PatientIdentifierTranslator;

@RunWith(MockitoJUnitRunner.class)
public class EncounterTranslatorImplTest {
	
	private static final String ENCOUNTER_UUID = "34h34hj-343jk32-34nl3kd-34jk34";
	
	private static final String PATIENT_UUID = "xxx78xxx-343kk43-ccc90ccc-oo45oo";
	
	private static final String PATIENT_URI = FhirConstants.PATIENT + "/" + PATIENT_UUID;
	
	private static final String PATIENT_IDENTIFIER = "384tt45-t";
	
	private static final String GIVEN_NAME = "Ricky";
	
	private static final String FAMILY_NAME = "sanchez";
	
	private static final String TEST_IDENTIFIER_TYPE_NAME = "test identifierType Name";
	
	private static final String PRACTITIONER_UUID = "2323hh34-2323kk3-23gh23-23k23";
	
	private static final String PRACTITIONER_URI = FhirConstants.PRACTITIONER + "/" + PRACTITIONER_UUID;
	
	private static final String LOCACTION_UUID = "123hh34-23jk23-23jk213-23jkl";
	
	private static final String TEST_LOCATION_NAME = "test location name";
	
	private static final String LOCATION_URI = FhirConstants.LOCATION + "" + LOCACTION_UUID;
	
	@Mock
	private PatientService patientService;
	
	@Mock
	private EncounterParticipantTranslator participantTranslator;
	
	@Mock
	private EncounterLocationTranslator encounterLocationTranslator;
	
	@Mock
	private PatientIdentifierTranslator patientIdentifierTranslator;
	
	private Patient patient;
	
	private Encounter fhirEncounter;
	
	private Location location;
	
	private org.openmrs.Encounter omrsEncounter;
	
	private EncounterTranslatorImpl encounterTranslator;
	
	@Before
	public void setUp() {
		fhirEncounter = new Encounter();
		omrsEncounter = new org.openmrs.Encounter();
		encounterTranslator = new EncounterTranslatorImpl();
		encounterTranslator.setPatientService(patientService);
		encounterTranslator.setParticipantTranslator(participantTranslator);
		encounterTranslator.setPatientIdentifierTranslator(patientIdentifierTranslator);
		encounterTranslator.setEncounterLocationTranslator(encounterLocationTranslator);
		
		PatientIdentifier identifier = new PatientIdentifier();
		identifier.setIdentifier(PATIENT_IDENTIFIER);
		
		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setName(TEST_IDENTIFIER_TYPE_NAME);
		identifier.setIdentifierType(identifierType);
		
		PersonName name = new PersonName();
		name.setGivenName(GIVEN_NAME);
		name.setFamilyName(FAMILY_NAME);
		
		patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		patient.addIdentifier(identifier);
		patient.addName(name);
		patient.setPersonId(0);
		omrsEncounter.setPatient(patient);
		
		Reference patientRef = new Reference();
		patientRef.setReference(PATIENT_URI);
		fhirEncounter.setSubject(patientRef);
		
		location = new Location();
		location.setUuid(LOCACTION_UUID);
		location.setName(TEST_LOCATION_NAME);
	}
	
	@Test
	public void shouldTranslateEncounterUuidToIdFhirType() {
		omrsEncounter.setUuid(ENCOUNTER_UUID);
		Encounter result = encounterTranslator.toFhirResource(omrsEncounter);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void shouldTranslateIdToOpenmrsType() {
		fhirEncounter.setId(ENCOUNTER_UUID);
		when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
		org.openmrs.Encounter result = encounterTranslator.toOpenmrsType(fhirEncounter);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), notNullValue());
		assertThat(result.getUuid(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void shouldAlwaysTranslateEncounterStatusToUnknownEncounterStatus() {
		assertThat(encounterTranslator.toFhirResource(omrsEncounter).getStatus(),
		    equalTo(Encounter.EncounterStatus.UNKNOWN));
	}
	
	@Test
	public void shouldTranslateSubjectToOpenMrsPatient() {
		Reference patientRef = new Reference();
		patientRef.setReference(PATIENT_URI);
		fhirEncounter.setId(ENCOUNTER_UUID);
		fhirEncounter.setSubject(patientRef);
		when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
		org.openmrs.Encounter result = encounterTranslator.toOpenmrsType(fhirEncounter);
		assertThat(result, notNullValue());
		assertThat(result.getPatient(), notNullValue());
		assertThat(result.getPatient().getUuid(), notNullValue());
		assertThat(result.getPatient().getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void shouldTranslatePatientToFhirSubjectAsReference() {
		PatientIdentifier identifier = new PatientIdentifier();
		identifier.setIdentifier(PATIENT_IDENTIFIER);
		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setName(TEST_IDENTIFIER_TYPE_NAME);
		identifier.setIdentifierType(identifierType);
		PersonName name = new PersonName();
		name.setGivenName(GIVEN_NAME);
		name.setFamilyName(FAMILY_NAME);
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		patient.addIdentifier(identifier);
		patient.addName(name);
		omrsEncounter.setUuid(ENCOUNTER_UUID);
		omrsEncounter.setPatient(patient);
		Encounter result = encounterTranslator.toFhirResource(omrsEncounter);
		assertThat(result, notNullValue());
		assertThat(result.getSubject(), notNullValue());
		assertThat(result.getSubject().getReference(), equalTo(PATIENT_URI));
	}
	
	@Test
	public void shouldTranslateParticipantToEncounterProviderOpenMrsType() {
		List<Encounter.EncounterParticipantComponent> participantComponents = new ArrayList<>();
		Encounter.EncounterParticipantComponent participantComponent = new Encounter.EncounterParticipantComponent();
		Reference practitionerRef = new Reference();
		practitionerRef.setReference(PRACTITIONER_URI);
		participantComponent.setIndividual(practitionerRef);
		participantComponents.add(participantComponent);
		fhirEncounter.setParticipant(participantComponents);
		EncounterProvider encounterProvider = new EncounterProvider();
		Provider provider = new Provider();
		provider.setUuid(PRACTITIONER_UUID);
		encounterProvider.setProvider(provider);
		when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
		
		org.openmrs.Encounter result = encounterTranslator.toOpenmrsType(fhirEncounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getEncounterProviders(), not(Collections.emptySet()));
		assertThat(result.getEncounterProviders().size(), equalTo(1));
	}
	
	@Test
	public void shouldTranslateEncounterProviderToFhirParticipant() {
		Set<EncounterProvider> providerList = new HashSet<>();
		EncounterProvider encounterProvider = new EncounterProvider();
		Provider provider = new Provider();
		provider.setUuid(PRACTITIONER_UUID);
		encounterProvider.setProvider(provider);
		providerList.add(encounterProvider);
		Encounter.EncounterParticipantComponent participantComponent = new Encounter.EncounterParticipantComponent();
		Reference practitionerRef = new Reference();
		practitionerRef.setReference(PRACTITIONER_URI);
		participantComponent.setIndividual(practitionerRef);
		omrsEncounter.setEncounterProviders(providerList);
		when(participantTranslator.toFhirResource(encounterProvider)).thenReturn(participantComponent);
		
		Encounter result = encounterTranslator.toFhirResource(omrsEncounter);
		assertThat(result, notNullValue());
		assertThat(result.getParticipant(), notNullValue());
		assertThat(result.getParticipant().size(), equalTo(1));
		assertThat(result.getParticipant().get(0).getIndividual().getReference(), equalTo(PRACTITIONER_URI));
	}
	
	@Test
	public void shouldTranslateLocationToEncounterLocationComponentFhirType() {
		omrsEncounter.setLocation(location);
		
		Encounter result = encounterTranslator.toFhirResource(omrsEncounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getLocation(), notNullValue());
		assertThat(result.getLocation().size(), is(1));
	}
	
	@Test
	public void shouldTranslateLocationToEncounterLocationFhirTypeWithCorrectReference() {
		omrsEncounter.setLocation(location);
		Encounter.EncounterLocationComponent locationComponent = new Encounter.EncounterLocationComponent();
		Reference locationRef = new Reference();
		locationRef.setReference(LOCATION_URI);
		locationRef.setDisplay(TEST_LOCATION_NAME);
		locationComponent.setLocation(locationRef);
		when(encounterLocationTranslator.toFhirResource(location)).thenReturn(locationComponent);
		Encounter result = encounterTranslator.toFhirResource(omrsEncounter);
		assertThat(result, notNullValue());
		assertThat(result.getLocation(), notNullValue());
		assertThat(result.getLocation().size(), is(1));
		assertThat(result.getLocation().get(0).getLocation().getDisplay(), equalTo(TEST_LOCATION_NAME));
	}
	
	@Test
	public void shouldTranslateLocationToEncounterLocationOpenMrsType() {
		List<Encounter.EncounterLocationComponent> locationComponents = new ArrayList<>();
		Encounter.EncounterLocationComponent locationComponent = new Encounter.EncounterLocationComponent();
		Reference locationRef = new Reference();
		locationRef.setReference(LOCATION_URI);
		locationRef.setDisplay(TEST_LOCATION_NAME);
		locationComponent.setLocation(locationRef);
		locationComponents.add(locationComponent);
		fhirEncounter.setLocation(locationComponents);
		when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
		assertThat(encounterTranslator.toOpenmrsType(fhirEncounter), notNullValue());
	}
	
	@Test
	public void shouldTranslateEncounterLocationToLocationOpenMrsTypeWithCorrectReference() {
		List<Encounter.EncounterLocationComponent> locationComponents = new ArrayList<>();
		Encounter.EncounterLocationComponent locationComponent = new Encounter.EncounterLocationComponent();
		Reference locationRef = new Reference();
		locationRef.setReference(LOCATION_URI);
		locationRef.setDisplay(TEST_LOCATION_NAME);
		locationComponent.setLocation(locationRef);
		locationComponents.add(locationComponent);
		fhirEncounter.setLocation(locationComponents);
		when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
		when(encounterLocationTranslator.toOpenmrsType(locationComponent)).thenReturn(location);
		org.openmrs.Encounter result = encounterTranslator.toOpenmrsType(fhirEncounter);
		assertThat(result, notNullValue());
		assertThat(result.getLocation(), notNullValue());
		assertThat(result.getLocation().getUuid(), equalTo(LOCACTION_UUID));
		assertThat(result.getLocation().getName(), equalTo(TEST_LOCATION_NAME));
	}
	
	@Test
	public void shouldTranslateOpenMrsDateChangedToLastUpdatedDate() {
		omrsEncounter.setDateChanged(new Date());
		
		Encounter result = encounterTranslator.toFhirResource(omrsEncounter);
		assertThat(result, notNullValue());
		assertThat(result.getMeta().getLastUpdated(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void shouldTranslateLastUpdatedDateToDateChanged() {
		fhirEncounter.getMeta().setLastUpdated(new Date());
		when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
		
		org.openmrs.Encounter result = encounterTranslator.toOpenmrsType(fhirEncounter);
		assertThat(result, notNullValue());
		assertThat(result.getDateChanged(), DateMatchers.sameDay(new Date()));
	}
}
