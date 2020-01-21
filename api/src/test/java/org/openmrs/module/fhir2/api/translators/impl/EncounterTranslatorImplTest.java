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

import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.FhirConstants;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncounterTranslatorImplTest {
	
	private static final String ENCOUNTER_UUID = "34h34hj-343jk32-34nl3kd-34jk34";
	
	private static final String PATIENT_UUID = "xxx78xxx-343kk43-ccc90ccc-oo45oo";
	
	private static final String PATIENT_URI = FhirConstants.PATIENT + "/" + PATIENT_UUID;
	
	private static final String PATIENT_IDENTIFIER = "384tt45-t";
	
	private static final String GIVEN_NAME = "Ricky";
	
	private static final String FAMILY_NAME = "sanchez";
	
	private static final String TEST_IDENTIFIER_TYPE_NAME = "test identifierType Name";
	
	@Mock
	private PatientService patientService;
	
	private Patient patient;
	
	private Encounter fhirEncounter;
	
	private org.openmrs.Encounter omrsEncounter;
	
	private EncounterTranslatorImpl encounterTranslator;
	
	@Before
	public void setUp() {
		fhirEncounter = new Encounter();
		omrsEncounter = new org.openmrs.Encounter();
		encounterTranslator = new EncounterTranslatorImpl();
		encounterTranslator.setPatientService(patientService);
		
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
		omrsEncounter.setPatient(patient);
		
		Reference patientRef = new Reference();
		patientRef.setReference(PATIENT_URI);
		fhirEncounter.setSubject(patientRef);
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
	public void shouldTranslateIdToOpenMrsType() {
		fhirEncounter.setId(ENCOUNTER_UUID);
		when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
		org.openmrs.Encounter result = encounterTranslator.toOpenmrsType(fhirEncounter);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), notNullValue());
		assertThat(result.getUuid(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void shouldAlwaysTranslateEncounterStatusToUnknownEncounterStatus() {
		assertThat(encounterTranslator.toFhirResource(omrsEncounter).getStatus(), equalTo(Encounter.EncounterStatus.UNKNOWN));
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
	
}
