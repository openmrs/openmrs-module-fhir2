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
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.mappings.EncounterClassMap;
import org.openmrs.module.fhir2.api.translators.EncounterLocationTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterParticipantTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterPeriodTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterTypeTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;

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
	
	private static final String LOCATION_UUID = "123hh34-23jk23-23jk213-23jkl";
	
	private static final String TEST_LOCATION_NAME = "test location name";
	
	private static final String TEST_FHIR_CLASS = "test fhir class";
	
	private static final String LOCATION_URI = FhirConstants.LOCATION + "/" + LOCATION_UUID;
	
	private static final String VISIT_UUID = "65aefd46-973d-4526-89de-93842c80ad11";
	
	private static final String VISIT_URI = FhirConstants.ENCOUNTER + "/" + VISIT_UUID;
	
	private static final String TYPE_CODE = "encounter";
	
	private static final String TYPE_DISPLAY = "Encounter";
	
	@Mock
	private EncounterParticipantTranslator participantTranslator;
	
	@Mock
	private EncounterLocationTranslator encounterLocationTranslator;
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private EncounterReferenceTranslator<Visit> visitReferenceTranslator;
	
	@Mock
	private EncounterTypeTranslator<EncounterType> encounterTypeTranslator;
	
	@Mock
	private EncounterPeriodTranslator<org.openmrs.Encounter> encounterPeriodTranslator;
	
	@Mock
	private EncounterClassMap encounterClassMap;
	
	private Patient patient;
	
	private Encounter fhirEncounter;
	
	private Location location;
	
	private Reference patientRef;
	
	private org.openmrs.Encounter omrsEncounter;
	
	private EncounterTranslatorImpl encounterTranslator;
	
	@Before
	public void setUp() {
		fhirEncounter = new Encounter();
		omrsEncounter = new org.openmrs.Encounter();
		encounterTranslator = new EncounterTranslatorImpl();
		encounterTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
		encounterTranslator.setParticipantTranslator(participantTranslator);
		encounterTranslator.setEncounterLocationTranslator(encounterLocationTranslator);
		encounterTranslator.setEncounterClassMap(encounterClassMap);
		encounterTranslator.setVisitReferenceTranlator(visitReferenceTranslator);
		encounterTranslator.setEncounterTypeTranslator(encounterTypeTranslator);
		encounterTranslator.setEncounterPeriodTranslator(encounterPeriodTranslator);
		
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
		
		patientRef = new Reference();
		patientRef.setReference(PATIENT_URI);
		fhirEncounter.setSubject(patientRef);
		
		location = new Location();
		location.setUuid(LOCATION_UUID);
		location.setName(TEST_LOCATION_NAME);
	}
	
	@Test
	public void toFhirResource_shouldTranslateEncounterUuidToId() {
		omrsEncounter.setUuid(ENCOUNTER_UUID);
		Encounter result = encounterTranslator.toFhirResource(omrsEncounter);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateIdToUuid() {
		fhirEncounter.setId(ENCOUNTER_UUID);
		when(patientReferenceTranslator.toOpenmrsType(patientRef)).thenReturn(patient);
		
		org.openmrs.Encounter result = encounterTranslator.toOpenmrsType(fhirEncounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), notNullValue());
		assertThat(result.getUuid(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test(expected = NullPointerException.class)
	public void toFhirResource_shouldThrowExceptionWhenOpenMrsEncounterIsNull() {
		encounterTranslator.toFhirResource(null);
	}
	
	@Test
	public void toFhirResource_shouldAlwaysToUnknownEncounterStatus() {
		assertThat(encounterTranslator.toFhirResource(omrsEncounter).getStatus(),
		    equalTo(Encounter.EncounterStatus.UNKNOWN));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateToPatient() {
		Reference patientRef = new Reference();
		patientRef.setReference(PATIENT_URI);
		fhirEncounter.setId(ENCOUNTER_UUID);
		fhirEncounter.setSubject(patientRef);
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		when(patientReferenceTranslator.toOpenmrsType(patientRef)).thenReturn(patient);
		org.openmrs.Encounter result = encounterTranslator.toOpenmrsType(fhirEncounter);
		assertThat(result, notNullValue());
		assertThat(result.getPatient(), notNullValue());
		assertThat(result.getPatient().getUuid(), notNullValue());
		assertThat(result.getPatient().getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToFhirSubjectAsReference() {
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
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientRef);
		Encounter result = encounterTranslator.toFhirResource(omrsEncounter);
		assertThat(result, notNullValue());
		assertThat(result.getSubject(), notNullValue());
		assertThat(result.getSubject().getReference(), equalTo(PATIENT_URI));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateParticipantToEncounterProvider() {
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
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		when(patientReferenceTranslator.toOpenmrsType(patientRef)).thenReturn(patient);
		org.openmrs.Encounter result = encounterTranslator.toOpenmrsType(fhirEncounter);
		
		assertThat(result.getEncounterProviders(), not(empty()));
		assertThat(result.getEncounterProviders().size(), equalTo(1));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToFhirParticipant() {
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
	public void toFhirResource_shouldTranslateLocationToEncounterLocationComponent() {
		omrsEncounter.setLocation(location);
		
		Encounter result = encounterTranslator.toFhirResource(omrsEncounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getLocation(), notNullValue());
		assertThat(result.getLocation().size(), is(1));
	}
	
	@Test
	public void toFhirResource_shouldTranslateLocationToEncounterLocationWithCorrectReference() {
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
	public void toOpenMrsType_shouldTranslateLocationToEncounterLocation() {
		List<Encounter.EncounterLocationComponent> locationComponents = new ArrayList<>();
		Encounter.EncounterLocationComponent locationComponent = new Encounter.EncounterLocationComponent();
		Reference locationRef = new Reference();
		locationRef.setReference(LOCATION_URI);
		locationRef.setDisplay(TEST_LOCATION_NAME);
		locationComponent.setLocation(locationRef);
		locationComponents.add(locationComponent);
		fhirEncounter.setLocation(locationComponents);
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		when(patientReferenceTranslator.toOpenmrsType(patientRef)).thenReturn(patient);
		when(encounterLocationTranslator.toOpenmrsType(locationComponent)).thenReturn(location);
		org.openmrs.Encounter result = encounterTranslator.toOpenmrsType(fhirEncounter);
		assertThat(result, notNullValue());
		assertThat(result.getLocation(), notNullValue());
		assertThat(result.getLocation().getName(), equalTo(TEST_LOCATION_NAME));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateToLocationWithCorrectUuid() {
		List<Encounter.EncounterLocationComponent> locationComponents = new ArrayList<>();
		Encounter.EncounterLocationComponent locationComponent = new Encounter.EncounterLocationComponent();
		Reference locationRef = new Reference();
		locationRef.setReference(LOCATION_URI);
		locationRef.setDisplay(TEST_LOCATION_NAME);
		locationComponent.setLocation(locationRef);
		locationComponents.add(locationComponent);
		fhirEncounter.setLocation(locationComponents);
		when(patientReferenceTranslator.toOpenmrsType(patientRef)).thenReturn(patient);
		when(encounterLocationTranslator.toOpenmrsType(locationComponent)).thenReturn(location);
		org.openmrs.Encounter result = encounterTranslator.toOpenmrsType(fhirEncounter);
		assertThat(result, notNullValue());
		assertThat(result.getLocation(), notNullValue());
		assertThat(result.getLocation().getUuid(), equalTo(LOCATION_UUID));
		assertThat(result.getLocation().getName(), equalTo(TEST_LOCATION_NAME));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateTypeToEncounterType() {
		EncounterType omrsEncounterType = new EncounterType();
		omrsEncounterType.setName(TYPE_DISPLAY);
		omrsEncounterType.setUuid(TYPE_CODE);
		when(encounterTypeTranslator.toOpenmrsType(ArgumentMatchers.any())).thenReturn(omrsEncounterType);
		when(patientReferenceTranslator.toOpenmrsType(patientRef)).thenReturn(patient);
		
		org.openmrs.Encounter result = encounterTranslator.toOpenmrsType(fhirEncounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getEncounterType(), equalTo(omrsEncounterType));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslatePeriodToEncounterDatetime() {
		Date encounterDate = new java.util.Date();
		Period period = new Period();
		period.setStart(encounterDate);
		
		fhirEncounter.setPeriod(period);
		
		when(encounterPeriodTranslator.toOpenmrsType(ArgumentMatchers.any(), ArgumentMatchers.any())).then(invocation -> {
			org.openmrs.Encounter encounter = invocation.getArgument(0);
			encounter.setEncounterDatetime(((Period) invocation.getArgument(1)).getStart());
			return encounter;
		});
		when(patientReferenceTranslator.toOpenmrsType(patientRef)).thenReturn(patient);
		
		org.openmrs.Encounter result = encounterTranslator.toOpenmrsType(fhirEncounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getEncounterDatetime(), equalTo(encounterDate));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToPartOf() {
		Visit visit = new Visit();
		visit.setUuid(VISIT_UUID);
		
		org.openmrs.Encounter encounter = new org.openmrs.Encounter();
		encounter.setUuid(ENCOUNTER_UUID);
		encounter.setVisit(visit);
		
		Reference reference = new Reference();
		reference.setReference(VISIT_URI);
		
		when(visitReferenceTranslator.toFhirResource(visit)).thenReturn(reference);
		Encounter result = encounterTranslator.toFhirResource(encounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getPartOf(), notNullValue());
		assertThat(result.getPartOf(), equalTo(reference));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateToVisit() {
		Visit visit = new Visit();
		visit.setUuid(VISIT_UUID);
		
		Reference reference = new Reference();
		reference.setReference(VISIT_URI);
		
		fhirEncounter.setPartOf(reference);
		
		when(patientReferenceTranslator.toOpenmrsType(patientRef)).thenReturn(patient);
		when(visitReferenceTranslator.toOpenmrsType(reference)).thenReturn(visit);
		
		org.openmrs.Encounter result = encounterTranslator.toOpenmrsType(fhirEncounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getVisit(), notNullValue());
		assertThat(result.getVisit(), equalTo(visit));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToLastUpdatedDate() {
		omrsEncounter.setDateChanged(new Date());
		
		Encounter result = encounterTranslator.toFhirResource(omrsEncounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getMeta().getLastUpdated(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToEncounterClassFhirType() {
		when(encounterClassMap.getFhirClass(LOCATION_UUID)).thenReturn(TEST_FHIR_CLASS);
		omrsEncounter.setLocation(location);
		
		Encounter result = encounterTranslator.toFhirResource(omrsEncounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getClass_(), notNullValue());
		assertThat(result.getClass_().getSystem(), is(FhirConstants.ENCOUNTER_CLASS_VALUE_SET_URI));
		assertThat(result.getClass_().getCode(), is(TEST_FHIR_CLASS));
	}
	
	@Test
	public void toFhirResource_shouldTranslateLocationToEncounterDefaultClass() {
		omrsEncounter.setLocation(location);
		
		Encounter result = encounterTranslator.toFhirResource(omrsEncounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getClass_(), notNullValue());
		assertThat(result.getClass_().getSystem(), is(FhirConstants.ENCOUNTER_CLASS_VALUE_SET_URI));
		assertThat(result.getClass_().getCode(), is("AMB"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateEncounterTypeToEncounterTypeField() {
		CodeableConcept fhirEncounterType = new CodeableConcept();
		fhirEncounterType.addCoding().setSystem(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI).setCode("1");
		when(encounterTypeTranslator.toFhirResource(ArgumentMatchers.any()))
		        .thenReturn(Collections.singletonList(fhirEncounterType));
		
		EncounterType omrsEncounterType = new EncounterType();
		omrsEncounter.setEncounterType(omrsEncounterType);
		
		Encounter result = encounterTranslator.toFhirResource(omrsEncounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), not(empty()));
		assertThat(result.getTypeFirstRep(), equalTo(fhirEncounterType));
	}
	
	@Test
	public void toFhirResource_shouldHaveEncounterTag() {
		Encounter result = encounterTranslator.toFhirResource(omrsEncounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getMeta().getTag(), notNullValue());
		assertThat(result.getMeta().getTag().get(0).getSystem(), equalTo(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG));
		assertThat(result.getMeta().getTag().get(0).getCode(), equalTo(TYPE_CODE));
		assertThat(result.getMeta().getTag().get(0).getDisplay(), equalTo(TYPE_DISPLAY));
	}
	
	@Test
	public void toFhirResource_shouldTranslateEncounterDatetimeToPeriod() {
		Date encounterDate = new java.util.Date();
		Period period = new Period();
		
		omrsEncounter.setEncounterDatetime(encounterDate);
		
		period.setStart(encounterDate);
		
		when(encounterPeriodTranslator.toFhirResource(ArgumentMatchers.any())).thenReturn(period);
		
		Encounter result = encounterTranslator.toFhirResource(omrsEncounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getPeriod(), equalTo(period));
	}
}
