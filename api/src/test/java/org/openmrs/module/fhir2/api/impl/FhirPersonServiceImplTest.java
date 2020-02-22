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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import org.hl7.fhir.r4.model.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.PersonAddress;
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
	
	private static final String PERSON_UUID = "1223-2323-2323-nd23";
	
	private static final String PERSON_NAME_UUID = "test-uuid-1223-2312";
	
	private static final String PERSON_BIRTH_DATE = "1996-12-12";
	
	private static final String NOT_FOUND_PERSON_BIRTH_DATE = "0001-10-10";
	
	private static final String CITY = "Washington";
	
	private static final String STATE = "Washington";
	
	private static final String POSTAL_CODE = "98136";
	
	private static final String COUNTRY = "Washington";
	
	private static final String NOT_ADDRESS_FIELD = "not an address field";
	
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	
	private org.openmrs.Person person;
	
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
		
		PersonName name = new PersonName();
		name.setUuid(PERSON_NAME_UUID);
		name.setGivenName(GIVEN_NAME);
		name.setFamilyName(FAMILY_NAME);
		
		PersonAddress address = new PersonAddress();
		address.setCityVillage(CITY);
		address.setStateProvince(STATE);
		address.setPostalCode(POSTAL_CODE);
		address.setCountry(COUNTRY);
		
		person = new org.openmrs.Person();
		person.setUuid(PERSON_UUID);
		person.setGender("M");
		person.addName(name);
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPersonForGivenNameMatched() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(GIVEN_NAME));
		when(dao.searchForPeople(argThat(is(stringOrListParam)), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull())).thenReturn(Collections.singletonList(person));
		
		Collection<Person> results = personService.searchForPeople(stringOrListParam, null, null, null, null, null, null,
		    null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), equalTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPersonForPartialMatchOnName() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(PERSON_PARTIAL_NAME));
		when(dao.searchForPeople(argThat(is(stringOrListParam)), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull())).thenReturn(Collections.singletonList(person));
		
		Collection<Person> results = personService.searchForPeople(stringOrListParam, null, null, null, null, null, null,
		    null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), equalTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenPersonNameNotMatched() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(NOT_FOUND_NAME));
		when(dao.searchForPeople(argThat(is(stringOrListParam)), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull())).thenReturn(Collections.singletonList(person));
		
		Collection<Person> results = personService.searchForPeople(stringOrListParam, null, null, null, null, null, null,
		    null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), equalTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPersonWhenPersonGenderMatched() {
		TokenOrListParam tokenOrListParam = new TokenOrListParam().add(GENDER);
		when(dao.searchForPeople(isNull(), argThat(is(tokenOrListParam)), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull())).thenReturn(Collections.singletonList(person));
		
		Collection<Person> results = personService.searchForPeople(null, tokenOrListParam, null, null, null, null, null,
		    null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenPersonGenderNotMatched() {
		TokenOrListParam tokenOrListParam = new TokenOrListParam().add(WRONG_GENDER);
		when(dao.searchForPeople(isNull(), argThat(is(tokenOrListParam)), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull())).thenReturn(Collections.emptyList());
		
		Collection<Person> results = personService.searchForPeople(null, tokenOrListParam, null, null, null, null, null,
		    null);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPersonWhenPersonBirthDateMatched() throws ParseException {
		Date birthDate = dateFormatter.parse(PERSON_BIRTH_DATE);
		person.setBirthdate(birthDate);
		
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(PERSON_BIRTH_DATE)
		        .setUpperBound(PERSON_BIRTH_DATE);
		when(dao.searchForPeople(isNull(), isNull(), argThat(is(dateRangeParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull())).thenReturn(Collections.singletonList(person));
		
		Collection<Person> results = personService.searchForPeople(null, null, dateRangeParam, null, null, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenPersonBirthDateNotMatched() throws ParseException {
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(NOT_FOUND_PERSON_BIRTH_DATE)
		        .setUpperBound(NOT_FOUND_PERSON_BIRTH_DATE);
		when(dao.searchForPeople(isNull(), isNull(), argThat(is(dateRangeParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull())).thenReturn(Collections.emptyList());
		
		Collection<Person> results = personService.searchForPeople(null, null, dateRangeParam, null, null, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPersonWhenPersonCityMatched() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(CITY));
		when(dao.searchForPeople(isNull(), isNull(), isNull(), argThat(is(stringOrListParam)), isNull(), isNull(), isNull(),
		    isNull())).thenReturn(Collections.singletonList(person));
		
		Collection<Person> results = personService.searchForPeople(null, null, null, stringOrListParam, null, null, null,
		    null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenPersonCityNotMatched() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD));
		when(dao.searchForPeople(isNull(), isNull(), isNull(), argThat(is(stringOrListParam)), isNull(), isNull(), isNull(),
		    isNull())).thenReturn(Collections.emptyList());
		
		Collection<Person> results = personService.searchForPeople(null, null, null, stringOrListParam, null, null, null,
		    null);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPersonWhenPersonStateMatched() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(STATE));
		when(dao.searchForPeople(isNull(), isNull(), isNull(), isNull(), argThat(is(stringOrListParam)), isNull(), isNull(),
		    isNull())).thenReturn(Collections.singletonList(person));
		
		Collection<Person> results = personService.searchForPeople(null, null, null, null, stringOrListParam, null, null,
		    null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenPersonStateNotMatched() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD));
		when(dao.searchForPeople(isNull(), isNull(), isNull(), isNull(), argThat(is(stringOrListParam)), isNull(), isNull(),
		    isNull())).thenReturn(Collections.emptyList());
		
		Collection<Person> results = personService.searchForPeople(null, null, null, null, stringOrListParam, null, null,
		    null);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPersonWhenPersonPostalCodeMatched() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(POSTAL_CODE));
		when(dao.searchForPeople(isNull(), isNull(), isNull(), isNull(), isNull(), argThat(is(stringOrListParam)), isNull(),
		    isNull())).thenReturn(Collections.singletonList(person));
		
		Collection<Person> results = personService.searchForPeople(null, null, null, null, null, stringOrListParam, null,
		    null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenPersonPostalCodeNotMatched() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD));
		when(dao.searchForPeople(isNull(), isNull(), isNull(), isNull(), isNull(), argThat(is(stringOrListParam)), isNull(),
		    isNull())).thenReturn(Collections.emptyList());
		
		Collection<Person> results = personService.searchForPeople(null, null, null, null, null, stringOrListParam, null,
		    null);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPersonWhenPersonCountryMatched() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(COUNTRY));
		when(dao.searchForPeople(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), argThat(is(stringOrListParam)),
		    isNull())).thenReturn(Collections.singletonList(person));
		
		Collection<Person> results = personService.searchForPeople(null, null, null, null, null, null, stringOrListParam,
		    null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenPersonCountryNotMatched() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD));
		when(dao.searchForPeople(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), argThat(is(stringOrListParam)),
		    isNull())).thenReturn(Collections.emptyList());
		
		Collection<Person> results = personService.searchForPeople(null, null, null, null, null, null, stringOrListParam,
		    null);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
}
