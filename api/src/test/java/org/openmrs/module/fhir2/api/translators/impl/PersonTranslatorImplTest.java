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
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.PersonService;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.translators.AddressTranslator;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;

import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PersonTranslatorImplTest {
	
	private static final String PERSON_FAMILY_NAME = "bett";
	
	private static final String PERSON_UUID = "1223et-098342-2723bsd";
	
	private static final String PERSON_GIVEN_NAME = "cornelious";
	
	private static final String ADDRESS_UUID = "135791-xxxxxx-135791";
	
	private static final String ADDRESS_CITY = "Eldoret";
	
	private static final String PERSON_ATTRIBUTE_UUID = "12o3et5kl3-2e323-23g23-232h3y343s";
	
	private static final String PERSON_ATTRIBUTE_VALUE = "254723723456";

	private static final String PERSON_ATTRIBUTE_TYPE_NAME = "Contact";

	private static final String PERSON_ATTRIBUTE_TYPE_UUID = "14d4f066-15f5-102d-96e4-000c29c2a5d7";

	private static final String CONTACT_VALUE = "254701884000";

	private static final String CONTACT_ID = "uu23823gf-3834sd-s934n-34nss";

	@Mock
	private GenderTranslator genderTranslator;
	
	@Mock
	private PersonNameTranslator nameTranslator;
	
	@Mock
	private AddressTranslator addressTranslator;
	
	@Mock
	private TelecomTranslator telecomTranslator;

	@Mock
	private FhirGlobalPropertyService administrationService;

	@Mock
	private PersonService personService;
	
	private PersonTranslatorImpl personTranslator;
	
	@Before
	public void setup() {
		personTranslator = new PersonTranslatorImpl();
		personTranslator.setGenderTranslator(genderTranslator);
		personTranslator.setNameTranslator(nameTranslator);
		personTranslator.setAddressTranslator(addressTranslator);
		personTranslator.setTelecomTranslator(telecomTranslator);
		personTranslator.setPersonService(personService);
		personTranslator.setGlobalPropertyService(administrationService);
	}
	
	@Test
	public void shouldTranslateOpenmrsPersonToFhirPerson() {
		Person person = new Person();
		org.hl7.fhir.r4.model.Person result = personTranslator.toFhirResource(person);
		assertThat(result, notNullValue());
	}
	
	@Test
	public void shouldTranslatePersonUuidToFhirIdType() {
		Person person = new Person();
		person.setUuid(PERSON_UUID);
		
		org.hl7.fhir.r4.model.Person result = personTranslator.toFhirResource(person);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(PERSON_UUID));
	}
	
	@Test
	public void shouldTranslateOpenmrsGenderToFhirGenderType() {
		Person person = new Person();
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(CONTACT_ID);
		contactPoint.setValue(CONTACT_VALUE);
		PersonAttributeType attributeType = new PersonAttributeType();
		attributeType.setName(PERSON_ATTRIBUTE_TYPE_NAME);
		attributeType.setUuid(PERSON_ATTRIBUTE_TYPE_UUID);
		PersonAttribute personAttribute = new PersonAttribute();
		personAttribute.setUuid(PERSON_ATTRIBUTE_UUID);
		personAttribute.setValue(PERSON_ATTRIBUTE_VALUE);
		personAttribute.setAttributeType(attributeType);

		when(genderTranslator.toFhirResource(argThat(equalTo("F")))).thenReturn(Enumerations.AdministrativeGender.FEMALE);
		person.setGender("F");
		
		org.hl7.fhir.r4.model.Person result = personTranslator.toFhirResource(person);
		
		assertThat(result, notNullValue());
		assertThat(result.getGender(), is(Enumerations.AdministrativeGender.FEMALE));
	}
	
	@Test
	public void shouldTranslateUnVoidedPersonToActive() {
		Person person = new Person();
		person.setVoided(false);
		
		org.hl7.fhir.r4.model.Person result = personTranslator.toFhirResource(person);
		
		assertThat(result, notNullValue());
		assertThat(result.getActive(), is(true));
	}
	
	@Test
	public void shouldTranslateVoidedPersonToInactive() {
		Person person = new Person();
		person.setVoided(true);
		
		org.hl7.fhir.r4.model.Person result = personTranslator.toFhirResource(person);
		
		assertThat(result, notNullValue());
		assertThat(result.getActive(), is(false));
	}
	
	@Test
	public void shouldTranslateOpenmrsPersonNameToFhirPersonName() {
		HumanName humanName = new HumanName();
		humanName.addGiven(PERSON_GIVEN_NAME);
		humanName.setFamily(PERSON_FAMILY_NAME);
		when(
		    nameTranslator.toFhirResource(argThat(allOf(hasProperty("givenName", equalTo(PERSON_GIVEN_NAME)),
		        hasProperty("familyName", equalTo(PERSON_FAMILY_NAME)))))).thenReturn(humanName);
		
		Person person = new Person();
		PersonName name = new PersonName();
		name.setGivenName(PERSON_GIVEN_NAME);
		name.setFamilyName(PERSON_FAMILY_NAME);
		person.addName(name);
		
		org.hl7.fhir.r4.model.Person result = personTranslator.toFhirResource(person);
		
		assertThat(result.getName(), not(empty()));
		assertThat(result.getName().get(0), notNullValue());
		assertThat(result.getName().get(0).getGivenAsSingleString(), equalTo(PERSON_GIVEN_NAME));
		assertThat(result.getName().get(0).getFamily(), equalTo(PERSON_FAMILY_NAME));
	}
	
	@Test
	public void shouldTranslateOpenmrsAddressToFhirAddress() {
		Address address = new Address();
		address.setId(ADDRESS_UUID);
		address.setCity(ADDRESS_CITY);
		when(
		    addressTranslator.toFhirResource(argThat(allOf(hasProperty("uuid", equalTo(ADDRESS_UUID)),
		        hasProperty("cityVillage", equalTo(ADDRESS_CITY)))))).thenReturn(address);
		
		Person person = new Person();
		PersonAddress personAddress = new PersonAddress();
		personAddress.setUuid(ADDRESS_UUID);
		personAddress.setCityVillage(ADDRESS_CITY);
		person.addAddress(personAddress);
		
		org.hl7.fhir.r4.model.Person result = personTranslator.toFhirResource(person);
		
		assertThat(result.getAddress(), notNullValue());
		assertThat(result.getAddress(), not(empty()));
		assertThat(result.getAddress().get(0), equalTo(address));
	}
	
	@Test
	public void shouldAddPersonLinksIfPatient() {
		Person person = new Person();
		person.setUuid(PERSON_UUID);
		
		org.hl7.fhir.r4.model.Person result = personTranslator.toFhirResource(person);
		
		assertThat(result, notNullValue());
		assertThat(result.getLink(), notNullValue());
	}
	
	@Test
	public void shouldTranslateFhirPersonToOpenmrsPerson() {
		org.hl7.fhir.r4.model.Person person = new org.hl7.fhir.r4.model.Person();
		
		Person result = personTranslator.toOpenmrsType(person);
		
		assertThat(result, notNullValue());
	}
	
	@Test
	public void shouldTranslateFhirPersonIdToUuid() {
		org.hl7.fhir.r4.model.Person person = new org.hl7.fhir.r4.model.Person();
		person.setId(PERSON_UUID);
		
		Person result = personTranslator.toOpenmrsType(person);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(PERSON_UUID));
	}
	
	@Test
	public void shouldTranslateFhirGenderToOpenmrsGender() {
		org.hl7.fhir.r4.model.Person person = new org.hl7.fhir.r4.model.Person();
		person.setGender(Enumerations.AdministrativeGender.MALE);
		when(genderTranslator.toOpenmrsType(Enumerations.AdministrativeGender.MALE)).thenReturn("M");
		
		Person result = personTranslator.toOpenmrsType(person);
		
		assertThat(result, notNullValue());
		assertThat(result.getGender(), equalTo("M"));
	}
	
	@Test
	public void shouldTranslateFhirNameToPersonName() {
		PersonName personName = new PersonName();
		personName.setGivenName(PERSON_GIVEN_NAME);
		personName.setFamilyName(PERSON_FAMILY_NAME);
		
		org.hl7.fhir.r4.model.Person person = new org.hl7.fhir.r4.model.Person();
		HumanName name = person.addName();
		name.addGiven(PERSON_GIVEN_NAME);
		name.setFamily(PERSON_FAMILY_NAME);
		when(nameTranslator.toOpenmrsType(name)).thenReturn(personName);
		
		Person result = personTranslator.toOpenmrsType(person);
		
		assertThat(result, notNullValue());
		assertThat(result.getGivenName(), equalTo(PERSON_GIVEN_NAME));
		assertThat(result.getFamilyName(), equalTo(PERSON_FAMILY_NAME));
	}
	
	@Test
	public void shouldTranslateFhirAddressToPersonAddress() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setUuid(ADDRESS_UUID);
		personAddress.setCityVillage(ADDRESS_CITY);
		
		org.hl7.fhir.r4.model.Person person = new org.hl7.fhir.r4.model.Person();
		Address address = person.addAddress();
		address.setId(ADDRESS_UUID);
		address.setCity(ADDRESS_CITY);
		when(addressTranslator.toOpenmrsType(address)).thenReturn(personAddress);
		
		Person result = personTranslator.toOpenmrsType(person);
		
		assertThat(result, notNullValue());
		assertThat(result.getPersonAddress(), notNullValue());
		assertThat(result.getPersonAddress().getUuid(), equalTo(ADDRESS_UUID));
		assertThat(result.getPersonAddress().getCityVillage(), equalTo(ADDRESS_CITY));
	}
	
	@Test
	public void shouldTranslatePersonAttributeToFhirContactPoint() {
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(CONTACT_ID);
		contactPoint.setValue(CONTACT_VALUE);
		PersonAttributeType attributeType = new PersonAttributeType();
		attributeType.setName(PERSON_ATTRIBUTE_TYPE_NAME);
		attributeType.setUuid(PERSON_ATTRIBUTE_TYPE_UUID);
		PersonAttribute personAttribute = new PersonAttribute();
		personAttribute.setUuid(PERSON_ATTRIBUTE_UUID);
		personAttribute.setValue(PERSON_ATTRIBUTE_VALUE);
		personAttribute.setAttributeType(attributeType);
		Person person = new Person();

		when(telecomTranslator.toFhirResource(personAttribute)).thenReturn(contactPoint);

		org.hl7.fhir.r4.model.Person result = personTranslator.toFhirResource(person);
		assertThat(result, notNullValue());
		assertThat(result.getTelecom(), notNullValue());

	}
	
	@Test
	public void shouldTranslateFhirContactPointToPersonAttribute() {
		PersonAttributeType attributeType = new PersonAttributeType();
		attributeType.setName(PERSON_ATTRIBUTE_TYPE_NAME);
		attributeType.setUuid(PERSON_ATTRIBUTE_TYPE_UUID);
		PersonAttribute personAttribute = new PersonAttribute();
		personAttribute.setUuid(PERSON_ATTRIBUTE_UUID);
		personAttribute.setValue(PERSON_ATTRIBUTE_VALUE);
		personAttribute.setAttributeType(attributeType);
		
		org.hl7.fhir.r4.model.Person person = new org.hl7.fhir.r4.model.Person();
		ContactPoint contactPoint = person.getTelecomFirstRep();
		contactPoint.setId(PERSON_ATTRIBUTE_UUID);
		contactPoint.setValue(PERSON_ATTRIBUTE_VALUE);
		
		when(telecomTranslator.toOpenmrsType(contactPoint)).thenReturn(personAttribute);
		
		Person people = personTranslator.toOpenmrsType(person);
		assertThat(people, notNullValue());
		assertThat(people.getActiveAttributes(), notNullValue());
		assertThat(people.getAttributes().size(), greaterThanOrEqualTo(1));
		assertThat(people.getActiveAttributes().stream().findAny().isPresent(), is(true));
		assertThat(people.getActiveAttributes().stream().findAny().get().getAttributeType(), equalTo(attributeType));
	}

	@Test
	public void shouldReturnPersonContactPointGivenOpenMrsPerson() {
		PersonAttributeType attributeType = new PersonAttributeType();
		attributeType.setName(PERSON_ATTRIBUTE_TYPE_NAME);
		attributeType.setUuid(PERSON_ATTRIBUTE_TYPE_UUID);
		PersonAttribute personAttribute = new PersonAttribute();
		personAttribute.setUuid(PERSON_ATTRIBUTE_UUID);
		personAttribute.setValue(PERSON_ATTRIBUTE_VALUE);
		personAttribute.setAttributeType(attributeType);
		Person person = new Person();
		person.addAttribute(personAttribute);

		List<ContactPoint> contactPoints = personTranslator.getPersonContactDetails(person);
		assertThat(contactPoints, notNullValue());
	}
}
