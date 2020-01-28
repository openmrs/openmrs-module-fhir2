/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.hl7.fhir.r4.model.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.api.dao.FhirPersonDao;
import org.openmrs.module.fhir2.api.translators.PersonTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirPersonServiceImplTest {
	
	private static final String GIVEN_NAME = "John";
	
	private static final String FAMILY_NAME = "kipchumba";
	
	private static final String PERSON_PARTIAL_NAME = "kip";
	
	private static final String NOT_FOUND_NAME = "not found name";
	
	private static final String GENDER = "M";
	
	private static final String WRONG_GENDER = "wrong-gender";
	
	private static final int PERSON_BIRTH_YEAR = 1999;
	
	private static final int NON_PERSON_BIRTH_YEAR = 1000;
	
	private static final String PERSON_UUID = "1223-2323-2323-nd23";
	
	private static final String PERSON_NAME_UUID = "test-uuid-1223-2312";
	
	private static final String PERSON_BIRTH_DATE = "1996-12-12";
	
	private static final String NOT_FOUND_PERSON_BIRTH_DATE = "0000-00-00";
	
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	
	@Mock
	private FhirPersonDao dao;
	
	@Mock
	private PersonTranslator personTranslator;
	
	private FhirPersonServiceImpl personService;
	
	@Before
	public void setUp() {
		personService = new FhirPersonServiceImpl();
		personService.setFhirPersonDao(dao);
		personService.setPersonTranslator(personTranslator);
	}
	
	@Test
	public void shouldReturnCollectionOfPersonForMatchOnPersonName() {
		Collection<org.openmrs.Person> people = new ArrayList<>();
		PersonName name = new PersonName();
		name.setUuid("test-uuid-1223-2312");
		name.setGivenName(GIVEN_NAME);
		org.openmrs.Person person = new org.openmrs.Person();
		person.setUuid("1223-2323-2323-nd23");
		person.setGender("M");
		person.addName(name);
		people.add(person);
		
		when(dao.findPersonsByName(GIVEN_NAME)).thenReturn(people);
		
		Collection<Person> personCollection = personService.findPersonsByName(GIVEN_NAME);
		assertNotNull(personCollection);
		assertEquals(personCollection.size(), 1);
	}
	
	@Test
	public void shouldReturnCollectionOfPersonsForPartialMatchOnPersonName() {
		Collection<org.openmrs.Person> people = new ArrayList<>();
		PersonName name = new PersonName();
		name.setUuid("test-uuid-1223-2312");
		name.setFamilyName(FAMILY_NAME);
		org.openmrs.Person person = new org.openmrs.Person();
		person.setUuid("1223-2323-2323-nd23");
		person.setGender("M");
		person.addName(name);
		people.add(person);
		when(dao.findPersonsByName(PERSON_PARTIAL_NAME)).thenReturn(people);
		
		Collection<Person> personCollection = personService.findPersonsByName(PERSON_PARTIAL_NAME);
		assertNotNull(personCollection);
		assertThat(personCollection, not(empty()));
		assertThat(personCollection.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findPersonsByNonExistingName_shouldReturnEmptyCollection() {
		Collection<Person> people = personService.findPersonsByName(NOT_FOUND_NAME);
		assertThat(people, is(empty()));
	}
	
	@Test
	public void shouldReturnCollectionOfPersonForMatchOnSimilarPeople() {
		Collection<org.openmrs.Person> people = new ArrayList<>();
		PersonName name = new PersonName();
		name.setUuid(PERSON_NAME_UUID);
		name.setGivenName(GIVEN_NAME);
		org.openmrs.Person person = new org.openmrs.Person();
		person.setUuid(PERSON_UUID);
		person.setGender(GENDER);
		person.addName(name);
		people.add(person);
		
		when(dao.findSimilarPeople(GIVEN_NAME, PERSON_BIRTH_YEAR, GENDER)).thenReturn(people);
		
		Collection<Person> personCollection = personService.findSimilarPeople(GIVEN_NAME, PERSON_BIRTH_YEAR, GENDER);
		assertNotNull(personCollection);
		assertThat(personCollection.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnEmptyListWhenPersonNameGenderBirthYearNotMatched() {
		Collection<org.openmrs.Person> people = new ArrayList<>();
		
		when(dao.findSimilarPeople(NOT_FOUND_NAME, NON_PERSON_BIRTH_YEAR, GENDER)).thenReturn(people);
		
		Collection<Person> results = personService.findSimilarPeople(NOT_FOUND_NAME, NON_PERSON_BIRTH_YEAR, GENDER);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void shouldReturnCollectionOfPersonWhenPersonBirthDateMatched() throws ParseException {
		Date birthDate = dateFormatter.parse(PERSON_BIRTH_DATE);
		Collection<org.openmrs.Person> people = new ArrayList<>();
		PersonName name = new PersonName();
		name.setUuid(PERSON_NAME_UUID);
		name.setFamilyName(FAMILY_NAME);
		org.openmrs.Person person = new org.openmrs.Person();
		person.setUuid(PERSON_UUID);
		person.setGender(GENDER);
		person.setBirthdate(birthDate);
		person.addName(name);
		people.add(person);
		when(dao.findPersonsByBirthDate(birthDate)).thenReturn(people);
		
		Collection<Person> results = personService.findPersonsByBirthDate(birthDate);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnEmptyCollectionWhenPersonBirthDateNotMatched() throws ParseException {
		Date birthDate = dateFormatter.parse(NOT_FOUND_PERSON_BIRTH_DATE);
		Collection<org.openmrs.Person> people = new ArrayList<>();
		when(dao.findPersonsByBirthDate(birthDate)).thenReturn(people);
		Collection<Person> results = personService.findPersonsByBirthDate(birthDate);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void shouldReturnCollectionOfPersonWhenPersonGenderMatched() {
		Collection<org.openmrs.Person> people = new ArrayList<>();
		PersonName name = new PersonName();
		name.setUuid(PERSON_NAME_UUID);
		name.setGivenName(GIVEN_NAME);
		org.openmrs.Person person = new org.openmrs.Person();
		person.setUuid(PERSON_UUID);
		person.setGender(GENDER);
		person.addName(name);
		people.add(person);
		when(dao.findPersonsByGender(GENDER)).thenReturn(people);
		
		Collection<Person> results = personService.findPersonsByGender(GENDER);
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnEmptyCollectionWhenPersonGenderNotMatched() {
		Collection<org.openmrs.Person> people = new ArrayList<>();
		
		when(dao.findPersonsByGender(WRONG_GENDER)).thenReturn(people);
		
		Collection<Person> results = personService.findPersonsByGender(WRONG_GENDER);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
}
