/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.api.translators.AddressTranslator;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PractitionerTranslatorImplTest {
	
	private static final String PROVIDER_UUID = "328934-34ni23-23j34-23923";
	
	private static final String RETIRE_REASON = "Retired By FHIR module";
	
	private static final String GENDER = "M";
	
	private static final String GIVEN_NAME = "kipchumba";
	
	private static final String FAMILY_NAME = "vannessa";
	
	private static final String ADDRESS_1 = "address1";
	
	private static final String CITY_VILLAGE = "chemasta";
	
	private static final String COUNTRY = "Kenya";
	
	private static final String PRACTITIONER_UUID = "934j934-34923n-23923n-2321";
	
	private static final String PRACTITIONER_IDENTIFIER = "practitioner-identifier";
	
	@Mock
	private GenderTranslator genderTranslator;
	
	@Mock
	private PersonNameTranslator nameTranslator;
	
	@Mock
	private AddressTranslator addressTranslator;
	
	private PractitionerTranslatorImpl practitionerTranslator;
	
	private Provider provider;
	
	private Practitioner practitioner;
	
	@Before
	public void setUp() {
		practitionerTranslator = new PractitionerTranslatorImpl();
		practitionerTranslator.setAddressTranslator(addressTranslator);
		practitionerTranslator.setGenderTranslator(genderTranslator);
		practitionerTranslator.setNameTranslator(nameTranslator);
		
		Person person = new Person();
		person.setGender(GENDER);
		provider = new Provider();
		provider.setUuid(PROVIDER_UUID);
		provider.setPerson(person);
		
		practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		Identifier identifier = new Identifier();
		identifier.setValue(PRACTITIONER_IDENTIFIER);
		practitioner.addIdentifier();
	}
	
	@Test
	public void shouldTranslateOpenMrsProviderToFhirPractitioner() {
		Practitioner practitioner = practitionerTranslator.toFhirResource(provider);
		assertThat(practitioner, notNullValue());
	}
	
	@Test
	public void shouldReturnUnchangedExistingProviderWhenPractitionerIsNull() {
		Provider result = practitionerTranslator.toOpenmrsType(provider, null);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), notNullValue());
		assertThat(result.getUuid(), equalTo(PROVIDER_UUID));
	}
	
	@Test
	public void shouldReturnEmptyPractitionerWhenProviderIsNull() {
		Practitioner result = practitionerTranslator.toFhirResource(null);
		assertThat(result, notNullValue());
		assertThat(result.getId(), nullValue());
	}
	
	@Test
	public void shouldTranslateProviderUuidToFhirIdType() {
		Practitioner practitioner = practitionerTranslator.toFhirResource(provider);
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getId(), notNullValue());
		assertThat(practitioner.getId(), equalTo(PROVIDER_UUID));
	}
	
	@Test
	public void shouldTranslateProviderGenderToFhirPractitionerType() {
		Person person = new Person();
		PersonName name = new PersonName();
		name.setGivenName(GIVEN_NAME);
		name.setFamilyName(FAMILY_NAME);
		person.addName(name);
		provider.setPerson(person);
		HumanName humanName = new HumanName();
		humanName.setFamily(FAMILY_NAME);
		humanName.addGiven(GIVEN_NAME);
		when(nameTranslator.toFhirResource(name)).thenReturn(humanName);
		Practitioner practitioner = practitionerTranslator.toFhirResource(provider);
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getName(), notNullValue());
		assertThat(practitioner.getName(), not(empty()));
		assertThat(practitioner.getName().get(0).getGiven().get(0).getValue(), equalTo(GIVEN_NAME));
		assertThat(practitioner.getName().get(0).getFamily(), equalTo(FAMILY_NAME));
	}
	
	@Test
	public void shouldTranslateProviderAddressToFhirPractitionerAddressType() {
		Person person = new Person();
		PersonAddress personAddress = new PersonAddress();
		personAddress.setCityVillage(CITY_VILLAGE);
		personAddress.setCountry(COUNTRY);
		person.addAddress(personAddress);
		provider.setPerson(person);
		Address fhirAddress = new Address();
		fhirAddress.setCity(CITY_VILLAGE);
		fhirAddress.setCountry(COUNTRY);
		when(addressTranslator.toFhirResource(personAddress)).thenReturn(fhirAddress);
		Practitioner practitioner = practitionerTranslator.toFhirResource(provider);
		
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getAddress(), not(empty()));
		assertThat(practitioner.getAddress().get(0).getCountry(), equalTo(COUNTRY));
		assertThat(practitioner.getAddress().get(0).getCity(), equalTo(CITY_VILLAGE));
	}
	
	@Test
	public void shouldTranslateProviderGenderToFhirPractitionerGender() {
		when(genderTranslator.toFhirResource(GENDER)).thenReturn(Enumerations.AdministrativeGender.MALE);
		Practitioner practitioner = practitionerTranslator.toFhirResource(provider);
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getGender(), notNullValue());
		assertThat(practitioner.getGender(), equalTo(Enumerations.AdministrativeGender.MALE));
	}
	
	@Test
	public void shouldTranslateToOpenMrsType() {
		Provider provider = practitionerTranslator.toOpenmrsType(practitioner);
		assertThat(provider, notNullValue());
		assertThat(provider.getUuid(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void shouldReturnUpdatedProvider() {
		provider.setRetired(true);
		provider.setRetireReason(RETIRE_REASON);
		Practitioner practitioner = new Practitioner();
		practitioner.setId(PROVIDER_UUID);
		practitioner.addIdentifier(new Identifier().setValue("349023n23b-t"));
		practitioner.setActive(false);
		Provider result = practitionerTranslator.toOpenmrsType(provider, practitioner);
		assertThat(result, notNullValue());
		assertThat(result.getRetired(), is(false));
		assertThat(result.getRetireReason(), notNullValue());
		assertThat(result.getRetireReason(), equalTo(RETIRE_REASON));
	}
}
