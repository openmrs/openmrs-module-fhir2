/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirPersonService;

@RunWith(MockitoJUnitRunner.class)
public class PersonFhirResourceProviderTest {
	
	private static final String PERSON_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";
	
	private static final String WRONG_PERSON_UUID = "9bf0d1ac-62a8-4440-a5a1-eb1015a7cc65";
	
	private static final String GENDER = "M";
	
	private static final String WRONG_GENDER = "wrong gender";
	
	private static final String BIRTH_DATE = "1992-03-04";
	
	private static final String WRONG_BIRTH_DATE = "0000-00-00";
	
	private static final String GIVEN_NAME = "Jeanne";
	
	private static final String FAMILY_NAME = "we";
	
	@Mock
	private FhirPersonService fhirPersonService;
	
	private PersonFhirResourceProvider resourceProvider;
	
	private Person person;
	
	@Before
	public void setup() {
		resourceProvider = new PersonFhirResourceProvider();
		resourceProvider.setFhirPersonService(fhirPersonService);
	}
	
	@Before
	public void initPerson() {
		HumanName name = new HumanName();
		name.addGiven(GIVEN_NAME);
		name.setFamily(FAMILY_NAME);
		
		person = new Person();
		person.setId(PERSON_UUID);
		person.setGender(Enumerations.AdministrativeGender.MALE);
		person.addName(name);
	}
	
	private static Date parseStringDate(String date) throws ParseException {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormatter.parse(date);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Person.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Person.class.getName()));
	}
	
	@Test
	public void getPersonById_shouldReturnPerson() {
		IdType id = new IdType();
		id.setValue(PERSON_UUID);
		when(fhirPersonService.getPersonByUuid(PERSON_UUID)).thenReturn(person);
		
		Person result = resourceProvider.getPersonById(id);
		assertThat(result.isResource(), is(true));
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(PERSON_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getPersonByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_PERSON_UUID);
		assertThat(resourceProvider.getPersonById(idType).isResource(), is(true));
		assertThat(resourceProvider.getPersonById(idType), nullValue());
	}
	
	@Test
	public void findPersonsByGender_shouldReturnMatchingBundleOfPersons() {
		when(fhirPersonService.findPersonsByGender(GENDER)).thenReturn(Collections.singletonList(person));
		StringParam param = new StringParam();
		param.setValue(GENDER);
		Bundle results = resourceProvider.findPersonsByGender(param);
		assertThat(results, notNullValue());
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
		assertThat(results.getEntry().get(0).getResource().getChildByName("id").getValues().get(0).toString(),
		    equalTo(PERSON_UUID));
		
	}
	
	@Test
	public void findPersonsByWrongGender_shouldReturnBundleWithEmptyEntries() {
		StringParam param = new StringParam();
		param.setValue(WRONG_GENDER);
		Bundle results = resourceProvider.findPersonsByGender(param);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry(), is(empty()));
	}
	
	@Test
	public void findPersonsByBirthDate_shouldReturnMatchingBundleOfPersons() throws ParseException {
		when(fhirPersonService.findPersonsByBirthDate(parseStringDate(BIRTH_DATE)))
		        .thenReturn(Collections.singletonList(person));
		DateParam param = new DateParam();
		param.setValue(parseStringDate(BIRTH_DATE));
		Bundle results = resourceProvider.findPersonsByBirthDate(param);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findPersonsByWrongBirthDate_shouldReturnBundleWithEmptyEntries() throws ParseException {
		DateParam param = new DateParam();
		param.setValue(parseStringDate(WRONG_BIRTH_DATE));
		Bundle results = resourceProvider.findPersonsByBirthDate(param);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry(), is(empty()));
	}
	
	@Test
	public void findSimilarPeople_ShouldReturnBundleOfMatchingPeople() {
		when(fhirPersonService.findSimilarPeople(GIVEN_NAME, null, GENDER)).thenReturn(Collections.singletonList(person));
		StringParam nameParam = new StringParam();
		nameParam.setValue(GIVEN_NAME);
		
		Bundle results = resourceProvider.findSimilarPeople(nameParam, null, GENDER);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findSimilarPeopleWithNullBirthDateAndGender_ShouldReturnBundleOfMatchingPeopleByName() {
		when(fhirPersonService.findSimilarPeople(GIVEN_NAME, null, null)).thenReturn(Collections.singletonList(person));
		StringParam nameParam = new StringParam();
		nameParam.setValue(GIVEN_NAME);
		
		Bundle results = resourceProvider.findSimilarPeople(nameParam, null, null);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
}
