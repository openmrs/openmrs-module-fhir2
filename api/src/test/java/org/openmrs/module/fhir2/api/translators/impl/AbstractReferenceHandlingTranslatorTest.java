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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.PatientIdentifierTranslator;

@RunWith(MockitoJUnitRunner.class)
public class AbstractReferenceHandlingTranslatorTest {
	
	private static final String GIVEN_NAME = "Ricky";
	
	private static final String FAMILY_NAME = "Morty";
	
	private static final String IDENTIFIER = "34ty5jsd-u";
	
	private static final String PATIENT_UUID = "123456-abc5def-123456";
	
	private static final String PATIENT_URI = FhirConstants.PATIENT + "/" + PATIENT_UUID;
	
	private static final String TEST_IDENTIFIER_TYPE_NAME = "Test IdentifierType Name";
	
	private static final String NAME_DISPLAY = "Ricky Morty(" + TEST_IDENTIFIER_TYPE_NAME + ":34ty5jsd-u)";
	
	private static final String PROVIDER_UUID = "234hj34-34ty34-324k5-6uh034";
	
	private static final String PROVIDER_TEST_IDENTIFIER = "test-identifier";
	
	private static final String TEST_PROVIDER_NAME = "test provider name";
	
	private static final String PERSON_UUID = "12234-xx34xx-342823-342kk3";
	
	private static final String PROVIDER_URI = FhirConstants.PRACTITIONER + "/" + PROVIDER_UUID;
	
	private static final String PROVIDER_DISPLAY = "Ricky Morty(" + FhirConstants.IDENTIFIER + ":" + PROVIDER_TEST_IDENTIFIER
	        + ")";
	
	private static final String LOCATION_UUID = "2321gh23-kj34h45-34jk3-34k34k";
	
	private static final String TEST_LOCATION_NAME = "Test location name";
	
	private static final String LOCATION_URI = FhirConstants.LOCATION + "/" + LOCATION_UUID;
	
	private static final String ENCOUNTER_UUID = "9878asdh-jlkasdf8-1u387kjd";
	
	private static final String ENCOUNTER_URI = FhirConstants.ENCOUNTER + "/" + ENCOUNTER_UUID;
	
	private static final String USER_UUID = "2ffb1a5f-bcd3-4243-8f40-78edc2642789";
	
	private static final String PRACTITIONER_REFERENCE = FhirConstants.PRACTITIONER + "/" + USER_UUID;
	
	private Patient patient;
	
	private Provider provider;
	
	private Location location;
	
	private User user;
	
	private org.openmrs.Encounter encounter;
	
	@Mock
	private PatientIdentifierTranslator patientIdentifierTranslator;
	
	private AbstractReferenceHandlingTranslator referenceHandlingTranslator;
	
	@Before
	public void setUp() {
		referenceHandlingTranslator = new AbstractReferenceHandlingTranslator() {};
		referenceHandlingTranslator.setPatientIdentifierTranslator(patientIdentifierTranslator);
		
		patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		
		PersonName name = new PersonName();
		name.setFamilyName(FAMILY_NAME);
		name.setGivenName(GIVEN_NAME);
		
		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setName(TEST_IDENTIFIER_TYPE_NAME);
		
		PatientIdentifier identifier = new PatientIdentifier();
		identifier.setIdentifier(IDENTIFIER);
		identifier.setIdentifierType(identifierType);
		patient.addName(name);
		patient.addIdentifier(identifier);
		
		provider = new Provider();
		provider.setUuid(PROVIDER_UUID);
		provider.setName(TEST_PROVIDER_NAME);
		provider.setIdentifier(PROVIDER_TEST_IDENTIFIER);
		
		location = new Location();
		location.setUuid(LOCATION_UUID);
		location.setName(TEST_LOCATION_NAME);
		
		encounter = new Encounter();
		encounter.setUuid(ENCOUNTER_UUID);
		
		Person person = new Person();
		person.setUuid(PERSON_UUID);
		person.addName(name);
		
		user = new User();
		user.setUuid(USER_UUID);
		user.setPerson(person);
	}
	
	@Test
	public void shouldExtractIdFromReference() {
		assertThat(referenceHandlingTranslator.getReferenceId(new Reference().setReference(PATIENT_URI)),
		    equalTo(PATIENT_UUID));
	}
	
	@Test
	public void shouldReturnNullIdForNullReference() {
		assertThat(referenceHandlingTranslator.getReferenceId(new Reference()), nullValue());
	}
	
	@Test
	public void shouldNotExtractIdWhenIdMissingFromReference() {
		assertThat(referenceHandlingTranslator.getReferenceId(new Reference().setReference(FhirConstants.PATIENT + "/")),
		    nullValue());
	}
	
	@Test
	public void shouldReturnNullIdWhenNoSlashFound() {
		assertThat(referenceHandlingTranslator.getReferenceId(new Reference().setReference(PATIENT_UUID)), nullValue());
	}
	
	@Test
	public void shouldExtractReferenceTypeFromReference() {
		assertThat(referenceHandlingTranslator.getReferenceType(new Reference().setReference(PATIENT_URI)),
		    equalTo(FhirConstants.PATIENT));
	}
	
	@Test
	public void shouldReturnNullTypeForNullReference() {
		assertThat(referenceHandlingTranslator.getReferenceType(new Reference()), nullValue());
	}
	
	@Test
	public void shouldReturnNullTypeWhenTypeMissing() {
		assertThat(referenceHandlingTranslator.getReferenceType(new Reference().setReference("/" + PATIENT_UUID)),
		    nullValue());
	}
	
	@Test
	public void shouldReturnNullTypeWhenNoSlashFound() {
		assertThat(referenceHandlingTranslator.getReferenceType(new Reference().setReference(PATIENT_UUID)), nullValue());
	}
	
	@Test
	public void shouldUseExplicitType() {
		assertThat(referenceHandlingTranslator.getReferenceType(new Reference().setType(FhirConstants.PATIENT)),
		    equalTo(FhirConstants.PATIENT));
	}
	
	@Test
	public void shouldReturnPatientReference() {
		Reference reference = referenceHandlingTranslator.createPatientReference(patient);
		
		assertThat(reference, notNullValue());
		assertThat(reference.getReference(), equalTo(PATIENT_URI));
		assertThat(reference.getType(), equalTo(FhirConstants.PATIENT));
	}
	
	@Test
	public void shouldReturnPatientReferenceWithCorrectDisplayName() {
		Reference reference = referenceHandlingTranslator.createPatientReference(patient);
		
		assertThat(reference, notNullValue());
		assertThat(reference.getDisplay(), equalTo(NAME_DISPLAY));
	}
	
	@Test
	public void shouldAddPractitionerReference() {
		Reference reference = referenceHandlingTranslator.createPractitionerReference(provider);
		
		assertThat(reference, notNullValue());
		assertThat(reference.getReference(), equalTo(PROVIDER_URI));
		assertThat(reference.getType(), equalTo(FhirConstants.PRACTITIONER));
	}
	
	@Test
	public void shouldReturnReferenceWithDisplayForProviderWithPerson() {
		PersonName name = new PersonName();
		name.setGivenName(GIVEN_NAME);
		name.setFamilyName(FAMILY_NAME);
		Person person = new Person();
		person.setUuid(PERSON_UUID);
		person.addName(name);
		provider.setPerson(person);
		
		Reference reference = referenceHandlingTranslator.createPractitionerReference(provider);
		
		assertThat(reference, notNullValue());
		assertThat(reference.getDisplay(), equalTo(PROVIDER_DISPLAY));
	}
	
	@Test
	public void shouldReturnNullDisplayForPractitionerWithNullPerson() {
		Reference reference = referenceHandlingTranslator.createPractitionerReference(provider);
		assertThat(reference, notNullValue());
		assertThat(reference.getDisplay(), nullValue());
	}
	
	@Test
	public void shouldAddLocationReference() {
		Reference reference = referenceHandlingTranslator.createLocationReference(location);
		
		assertThat(reference, notNullValue());
		assertThat(reference.getReference(), equalTo(LOCATION_URI));
		assertThat(reference.getType(), equalTo(FhirConstants.LOCATION));
		assertThat(reference.getDisplay(), equalTo(TEST_LOCATION_NAME));
	}
	
	@Test
	public void shouldReturnNullDisplayWhenLocationNameIsNull() {
		location.setName(null);
		
		Reference reference = referenceHandlingTranslator.createLocationReference(location);
		
		assertThat(reference, notNullValue());
		assertThat(reference.getDisplay(), nullValue());
	}
	
	@Test
	public void shouldAddEncounterReference() {
		Reference reference = referenceHandlingTranslator.createEncounterReference(encounter);
		
		assertThat(reference, notNullValue());
		assertThat(reference.getReference(), equalTo(ENCOUNTER_URI));
		assertThat(reference.getType(), equalTo(FhirConstants.ENCOUNTER));
	}
	
	@Test
	public void shouldAddPractitionerGivenOpenMrsUserReference() {
		Reference reference = referenceHandlingTranslator.createPractitionerReference(user);
		assertThat(reference, notNullValue());
		assertThat(reference.getReference(), equalTo(PRACTITIONER_REFERENCE));
		assertThat(reference.getType(), equalTo(FhirConstants.PRACTITIONER));
		assertThat(reference.getDisplay(), notNullValue());
	}
	
	@Test
	public void shouldReturnReferenceWithNullDisplayIfUserPersonNameIsNull() {
		User user = new User();
		user.setUuid(USER_UUID);
		Reference reference = referenceHandlingTranslator.createPractitionerReference(user);
		assertThat(reference, notNullValue());
		assertThat(reference.getReference(), equalTo(PRACTITIONER_REFERENCE));
		assertThat(reference.getDisplay(), nullValue());
	}
}
