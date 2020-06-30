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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PersonAddressTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;

@RunWith(MockitoJUnitRunner.class)
public class PractitionerTranslatorUserImplTest {
	
	private static final String USER_UUID = "5b598dd8-42f8-4d7d-9041-098e4ee8ad30";
	
	private static final String SYSTEM_ID = "1002";
	
	private static final String GENDER = "M";
	
	private static final String GIVEN_NAME = "John";
	
	private static final String FAMILY_NAME = "Taylor";
	
	private static final String CITY_VILLAGE = "panvilla";
	
	private static final String COUNTRY = "Kenya";
	
	@Mock
	private GenderTranslator genderTranslator;
	
	@Mock
	private PersonNameTranslator nameTranslator;
	
	@Mock
	private PersonAddressTranslator addressTranslator;
	
	private PractitionerTranslatorUserImpl practitionerTranslatorUser;
	
	private User user;
	
	private Practitioner practitioner;
	
	@Before
	public void setup() {
		practitionerTranslatorUser = new PractitionerTranslatorUserImpl();
		practitionerTranslatorUser.setNameTranslator(nameTranslator);
		practitionerTranslatorUser.setAddressTranslator(addressTranslator);
		practitionerTranslatorUser.setGenderTranslator(genderTranslator);
		
		user = new User();
		user.setUuid(USER_UUID);
		user.setSystemId(SYSTEM_ID);
		
		Identifier identifier = new Identifier();
		identifier.setValue((SYSTEM_ID));
		identifier.setSystem(FhirConstants.OPENMRS_FHIR_EXT_USER_IDENTIFIER);
		
		practitioner = new Practitioner();
		practitioner.setId(USER_UUID);
		practitioner.addIdentifier(identifier);
		
	}
	
	@Test
	public void shouldTranslateOpenMrsUserToFhirPractitioner() {
		Practitioner practitioner = practitionerTranslatorUser.toFhirResource(user);
		assertThat(practitioner, notNullValue());
	}
	
	@Test
	public void shouldReturnUnchangedExistingUserWhenPractitionerIsNull() {
		User result = practitionerTranslatorUser.toOpenmrsType(user, null);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), notNullValue());
		assertThat(result.getUuid(), equalTo(USER_UUID));
	}
	
	@Test
	public void shouldTranslateUserUuidToFhirPractitionerId() {
		Practitioner practitioner = practitionerTranslatorUser.toFhirResource(user);
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getId(), notNullValue());
		assertThat(practitioner.getId(), equalTo(USER_UUID));
	}
	
	@Test
	public void shouldTranslateUserIdToFhirPractitionerIdentifier() {
		Practitioner result = practitionerTranslatorUser.toFhirResource(user);
		assertThat(result, notNullValue());
		assertThat(result.getIdentifier(), not(empty()));
		assertThat(result.getIdentifier().size(), equalTo(1));
		assertThat(result.getIdentifier().get(0).getValue(), equalTo((SYSTEM_ID)));
		
	}
	
	@Test
	public void shouldTranslateFhirPractitionerIdentifierToOpenMrsUserId() {
		User result = practitionerTranslatorUser.toOpenmrsType(practitioner);
		assertThat(result, notNullValue());
		assertThat(result.getSystemId(), notNullValue());
		assertThat(result.getSystemId(), equalTo(SYSTEM_ID));
	}
	
	@Test
	public void shouldTranslateUserGenderToFhirPractitionerType() {
		Person person = new Person();
		PersonName name = new PersonName();
		name.setGivenName(GIVEN_NAME);
		name.setFamilyName(FAMILY_NAME);
		person.addName(name);
		user.setPerson(person);
		HumanName humanName = new HumanName();
		humanName.setFamily(FAMILY_NAME);
		humanName.addGiven(GIVEN_NAME);
		when(nameTranslator.toFhirResource(name)).thenReturn(humanName);
		Practitioner practitioner = practitionerTranslatorUser.toFhirResource(user);
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getName(), notNullValue());
		assertThat(practitioner.getName(), not(empty()));
		assertThat(practitioner.getName().get(0).getGiven().get(0).getValue(), equalTo(GIVEN_NAME));
		assertThat(practitioner.getName().get(0).getFamily(), equalTo(FAMILY_NAME));
	}
	
	@Test
	public void shouldTranslateUserAddressToFhirPractitionerAddressType() {
		Person person = new Person();
		PersonAddress personAddress = new PersonAddress();
		personAddress.setCityVillage(CITY_VILLAGE);
		personAddress.setCountry(COUNTRY);
		person.addAddress(personAddress);
		user.setPerson(person);
		Address fhirAddress = new Address();
		fhirAddress.setCity(CITY_VILLAGE);
		fhirAddress.setCountry(COUNTRY);
		when(addressTranslator.toFhirResource(personAddress)).thenReturn(fhirAddress);
		Practitioner practitioner = practitionerTranslatorUser.toFhirResource(user);
		
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getAddress(), not(empty()));
		assertThat(practitioner.getAddress().get(0).getCountry(), equalTo(COUNTRY));
		assertThat(practitioner.getAddress().get(0).getCity(), equalTo(CITY_VILLAGE));
	}
	
	@Test
	public void shouldTranslateUserGenderToFhirPractitionerGender() {
		Person person = new Person();
		person.setGender(GENDER);
		user.setPerson(person);
		when(genderTranslator.toFhirResource(GENDER)).thenReturn(Enumerations.AdministrativeGender.MALE);
		Practitioner practitioner = practitionerTranslatorUser.toFhirResource(user);
		assertThat(practitioner, notNullValue());
		assertThat(practitioner.getGender(), notNullValue());
		assertThat(practitioner.getGender(), equalTo(Enumerations.AdministrativeGender.MALE));
	}
	
	@Test
	public void shouldTranslateOpenMrsDateChangedToLastUpdatedDate() {
		user.setDateChanged(new Date());
		
		Practitioner result = practitionerTranslatorUser.toFhirResource(user);
		assertThat(result, notNullValue());
		assertThat(result.getMeta().getLastUpdated(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void shouldTranslateLastUpdatedDateToDateChanged() {
		practitioner.getMeta().setLastUpdated(new Date());
		
		User result = practitionerTranslatorUser.toOpenmrsType(practitioner);
		assertThat(result, notNullValue());
		assertThat(result.getDateChanged(), DateMatchers.sameDay(new Date()));
	}
	
}
