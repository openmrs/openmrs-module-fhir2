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
import static org.hamcrest.CoreMatchers.nullValue;
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
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import org.hl7.fhir.r4.model.Person;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.module.fhir2.api.dao.FhirRelatedPersonDao;
import org.openmrs.module.fhir2.api.translators.RelatedPersonTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirRelatedPersonServiceImplTest {
	
	private static final String RELATED_PERSON_UUID = "5f07c6ff-c483-4e77-815e-44dd650470e7";
	
	private static final String WRONG_RELATED_PERSON_UUID = "1a1d2623-2f67-47de-8fb0-b02f51e378b7";
	
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
	
	@Mock
	private FhirRelatedPersonDao dao;
	
	@Mock
	private RelatedPersonTranslator translator;
	
	private FhirRelatedPersonServiceImpl relatedPersonService;
	
	private org.openmrs.Person person;
	
	private org.openmrs.Relationship relationship;
	
	@Before
	public void setup() {
		relatedPersonService = new FhirRelatedPersonServiceImpl();
		relatedPersonService.setDao(dao);
		relatedPersonService.setTranslator(translator);
		
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
		
		relationship = new org.openmrs.Relationship();
		relationship.setRelationshipId(1000);
		relationship.setPersonA(person);
		
	}
	
	@Test
	public void shouldGetRelatedPersonById() {
		Relationship relationship = new Relationship();
		relationship.setUuid(RELATED_PERSON_UUID);
		
		RelatedPerson relatedPerson = new RelatedPerson();
		relatedPerson.setId(RELATED_PERSON_UUID);
		
		when(dao.get(RELATED_PERSON_UUID)).thenReturn(relationship);
		when(translator.toFhirResource(relationship)).thenReturn(relatedPerson);
		
		RelatedPerson result = relatedPersonService.get(RELATED_PERSON_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(RELATED_PERSON_UUID));
	}
	
	@Test
	public void shouldReturnNullWhenGetByWrongUuid() {
		when(dao.get(WRONG_RELATED_PERSON_UUID)).thenReturn(null);
		assertThat(relatedPersonService.get(WRONG_RELATED_PERSON_UUID), nullValue());
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnCollectionOfRelatedPersonForGivenNameMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(GIVEN_NAME)));
		when(dao.searchRelationships(argThat(is(stringAndListParam)), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(Collections.singletonList(relationship));
		
		Collection<RelatedPerson> results = relatedPersonService.searchForRelatedPeople(stringAndListParam, null, null, null,
		    null, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), equalTo(1));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnCollectionOfRelatedPersonForPartialMatchOnName() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(PERSON_PARTIAL_NAME)));
		when(dao.searchRelationships(argThat(is(stringAndListParam)), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(Collections.singletonList(relationship));
		
		Collection<RelatedPerson> results = relatedPersonService.searchForRelatedPeople(stringAndListParam, null, null, null,
		    null, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), equalTo(1));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnEmptyCollectionWhenRelatedPersonNameNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_FOUND_NAME)));
		when(dao.searchRelationships(argThat(is(stringAndListParam)), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(Collections.singletonList(relationship));
		
		Collection<RelatedPerson> results = relatedPersonService.searchForRelatedPeople(stringAndListParam, null, null, null,
		    null, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), equalTo(1));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnCollectionOfRelatedPersonWhenPersonGenderMatched() {
		TokenAndListParam tokenAndListParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(GENDER));
		when(dao.searchRelationships(isNull(), argThat(is(tokenAndListParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(Collections.singletonList(relationship));
		
		Collection<RelatedPerson> results = relatedPersonService.searchForRelatedPeople(null, tokenAndListParam, null, null,
		    null, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnEmptyCollectionWhenRelatedPersonGenderNotMatched() {
		TokenAndListParam tokenAndListParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(WRONG_GENDER));
		when(dao.searchRelationships(isNull(), argThat(is(tokenAndListParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(Collections.emptyList());
		
		Collection<RelatedPerson> results = relatedPersonService.searchForRelatedPeople(null, tokenAndListParam, null, null,
		    null, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnCollectionOfRelatedPersonWhenPersonBirthDateMatched()
	        throws ParseException {
		Date birthDate = dateFormatter.parse(PERSON_BIRTH_DATE);
		person.setBirthdate(birthDate);
		
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(PERSON_BIRTH_DATE)
		        .setUpperBound(PERSON_BIRTH_DATE);
		when(dao.searchRelationships(isNull(), isNull(), argThat(is(dateRangeParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull())).thenReturn(Collections.singletonList(relationship));
		
		Collection<RelatedPerson> results = relatedPersonService.searchForRelatedPeople(null, null, dateRangeParam, null,
		    null, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnEmptyCollectionWhenRelatedPersonBirthDateNotMatched() {
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(NOT_FOUND_PERSON_BIRTH_DATE)
		        .setUpperBound(NOT_FOUND_PERSON_BIRTH_DATE);
		when(dao.searchRelationships(isNull(), isNull(), argThat(is(dateRangeParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull())).thenReturn(Collections.emptyList());
		
		Collection<RelatedPerson> results = relatedPersonService.searchForRelatedPeople(null, null, dateRangeParam, null,
		    null, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnCollectionOfRelatedPersonWhenPersonCityMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(CITY)));
		when(dao.searchRelationships(isNull(), isNull(), isNull(), argThat(is(stringAndListParam)), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(Collections.singletonList(relationship));
		
		Collection<RelatedPerson> results = relatedPersonService.searchForRelatedPeople(null, null, null, stringAndListParam,
		    null, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnEmptyCollectionWhenRelatedPersonCityNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD)));
		when(dao.searchRelationships(isNull(), isNull(), isNull(), argThat(is(stringAndListParam)), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(Collections.emptyList());
		
		Collection<RelatedPerson> results = relatedPersonService.searchForRelatedPeople(null, null, null, stringAndListParam,
		    null, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnCollectionOfRelatedPersonWhenPersonStateMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(STATE)));
		when(dao.searchRelationships(isNull(), isNull(), isNull(), isNull(), argThat(is(stringAndListParam)), isNull(),
		    isNull(), isNull())).thenReturn(Collections.singletonList(relationship));
		
		Collection<RelatedPerson> results = relatedPersonService.searchForRelatedPeople(null, null, null, null,
		    stringAndListParam, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnEmptyCollectionWhenRelatedPersonStateNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD)));
		when(dao.searchRelationships(isNull(), isNull(), isNull(), isNull(), argThat(is(stringAndListParam)), isNull(),
		    isNull(), isNull())).thenReturn(Collections.emptyList());
		
		Collection<RelatedPerson> results = relatedPersonService.searchForRelatedPeople(null, null, null, null,
		    stringAndListParam, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnCollectionOfRelatedPersonWhenPersonPostalCodeMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		when(dao.searchRelationships(isNull(), isNull(), isNull(), isNull(), isNull(), argThat(is(stringAndListParam)),
		    isNull(), isNull())).thenReturn(Collections.singletonList(relationship));
		
		Collection<RelatedPerson> results = relatedPersonService.searchForRelatedPeople(null, null, null, null, null,
		    stringAndListParam, null, null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnEmptyCollectionWhenRelatedPersonPostalCodeNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD)));
		when(dao.searchRelationships(isNull(), isNull(), isNull(), isNull(), isNull(), argThat(is(stringAndListParam)),
		    isNull(), isNull())).thenReturn(Collections.emptyList());
		
		Collection<RelatedPerson> results = relatedPersonService.searchForRelatedPeople(null, null, null, null, null,
		    stringAndListParam, null, null);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnCollectionOfPersonWhenPersonCountryMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(COUNTRY)));
		when(dao.searchRelationships(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(is(stringAndListParam)), isNull())).thenReturn(Collections.singletonList(relationship));
		
		Collection<RelatedPerson> results = relatedPersonService.searchForRelatedPeople(null, null, null, null, null, null,
		    stringAndListParam, null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenPersonCountryNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD)));
		when(dao.searchRelationships(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(is(stringAndListParam)), isNull())).thenReturn(Collections.emptyList());
		
		Collection<RelatedPerson> results = relatedPersonService.searchForRelatedPeople(null, null, null, null, null, null,
		    stringAndListParam, null);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
}
