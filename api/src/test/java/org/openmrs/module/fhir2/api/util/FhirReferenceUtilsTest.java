/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.util;

import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.FhirConstants;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class FhirReferenceUtilsTest {
	
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
	
	private static final String PROVIDER_URI = FhirConstants.PROVIDER + "/" + PROVIDER_UUID;
	
	private static final String PROVIDER_DISPLAY = "Ricky Morty(" + FhirConstants.IDENTIFIER + ":"
	        + PROVIDER_TEST_IDENTIFIER + ")";
	
	private Patient patient;
	
	private Provider provider;
	
	@Before
	public void setUp() {
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
	}
	
	@Test
	public void shouldReturnPatientReference() {
		Reference reference = FhirReferenceUtils.addPatientReference(patient);
		assertThat(reference, notNullValue());
		assertThat(reference.getReference(), notNullValue());
		assertThat(reference.getReference(), equalTo(PATIENT_URI));
	}
	
	@Test
	public void shouldReturnReferenceWithCorrectDisplayName() {
		Reference reference = FhirReferenceUtils.addPatientReference(patient);
		assertThat(reference, notNullValue());
		assertThat(reference.getDisplay(), notNullValue());
		assertThat(reference.getDisplay(), equalTo(NAME_DISPLAY));
	}
	
	@Test
	public void shouldExtractUuidFromUri() {
		String uuid = FhirReferenceUtils.extractUuid(PATIENT_URI);
		assertThat(uuid, notNullValue());
		assertThat(uuid, equalTo(PATIENT_UUID));
	}
	
	@Test
	public void shouldNotExtractUuidFromNullUri() {
		assertThat(FhirReferenceUtils.extractUuid(null), nullValue());
	}
	
	@Test
	public void shouldAddPractitionerReference() {
		Reference reference = FhirReferenceUtils.addPractitionerReference(provider);
		assertThat(reference, notNullValue());
		assertThat(reference.getReference(), notNullValue());
		assertThat(reference.getReference(), equalTo(PROVIDER_URI));
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
		Reference reference = FhirReferenceUtils.addPractitionerReference(provider);
		assertThat(reference, notNullValue());
		assertThat(reference.getDisplay(), notNullValue());
		assertThat(reference.getReference(), equalTo(PROVIDER_URI));
		assertThat(reference.getDisplay(), equalTo(PROVIDER_DISPLAY));
	}
	
	@Test
	public void shouldReturnNullDisplayForPractitionerWithNullPerson() {
		Reference reference = FhirReferenceUtils.addPractitionerReference(provider);
		assertThat(reference, notNullValue());
		assertThat(reference.getDisplay(), nullValue());
	}
}
