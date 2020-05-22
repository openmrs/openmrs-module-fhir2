/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Person;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Provenance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirPersonService;

@RunWith(MockitoJUnitRunner.class)
public class PersonFhirResourceProviderTest extends BaseFhirR3ProvenanceResourceTest<org.hl7.fhir.r4.model.Person> {
	
	private static final String PERSON_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";
	
	private static final String WRONG_PERSON_UUID = "9bf0d1ac-62a8-4440-a5a1-eb1015a7cc65";
	
	private static final String GENDER = "M";
	
	private static final String BIRTH_DATE = "1992-03-04";
	
	private static final String GIVEN_NAME = "Jeanne";
	
	private static final String FAMILY_NAME = "we";
	
	private static final String CITY = "Seattle";
	
	private static final String STATE = "Washington";
	
	private static final String POSTAL_CODE = "98136";
	
	private static final String COUNTRY = "Canada";
	
	@Mock
	private FhirPersonService fhirPersonService;
	
	private PersonFhirResourceProvider resourceProvider;
	
	private org.hl7.fhir.r4.model.Person person;
	
	@Before
	public void setup() {
		resourceProvider = new PersonFhirResourceProvider();
		resourceProvider.setPersonService(fhirPersonService);
	}
	
	@Before
	public void initPerson() {
		HumanName name = new HumanName();
		name.addGiven(GIVEN_NAME);
		name.setFamily(FAMILY_NAME);
		
		person = new org.hl7.fhir.r4.model.Person();
		person.setId(PERSON_UUID);
		person.setGender(Enumerations.AdministrativeGender.MALE);
		person.addName(name);
		setProvenanceResources(person);
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
		when(fhirPersonService.get(PERSON_UUID)).thenReturn(person);
		
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
	public void searchPeople_shouldReturnMatchingBundleOfPeopleByName() {
		StringAndListParam nameParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(GIVEN_NAME)));
		when(fhirPersonService.searchForPeople(argThat(is(nameParam)), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(Collections.singletonList(person));
		
		Bundle results = resourceProvider.searchPeople(nameParam, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnMatchingBundleOfPeopleByGender() {
		TokenAndListParam genderParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(GENDER));
		when(fhirPersonService.searchForPeople(isNull(), argThat(is(genderParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(Collections.singletonList(person));
		
		Bundle results = resourceProvider.searchPeople(null, genderParam, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnMatchingBundleOfPeopleByBirthDate() {
		DateRangeParam birthDateParam = new DateRangeParam().setLowerBound(BIRTH_DATE).setUpperBound(BIRTH_DATE);
		when(fhirPersonService.searchForPeople(isNull(), isNull(), argThat(is(birthDateParam)), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(Collections.singletonList(person));
		
		Bundle results = resourceProvider.searchPeople(null, null, birthDateParam, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnMatchingBundleOfPeopleByCity() {
		StringAndListParam cityParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(CITY)));
		when(fhirPersonService.searchForPeople(isNull(), isNull(), isNull(), argThat(is(cityParam)), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(Collections.singletonList(person));
		
		Bundle results = resourceProvider.searchPeople(null, null, null, cityParam, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnMatchingBundleOfPeopleByState() {
		StringAndListParam stateParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(STATE)));
		when(fhirPersonService.searchForPeople(isNull(), isNull(), isNull(), isNull(), argThat(is(stateParam)), isNull(),
		    isNull(), isNull())).thenReturn(Collections.singletonList(person));
		
		Bundle results = resourceProvider.searchPeople(null, null, null, null, stateParam, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnMatchingBundleOfPeopleByPostalCode() {
		StringAndListParam postalCodeParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		when(fhirPersonService.searchForPeople(isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(is(postalCodeParam)), isNull(), isNull())).thenReturn(Collections.singletonList(person));
		
		Bundle results = resourceProvider.searchPeople(null, null, null, null, null, postalCodeParam, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnMatchingBundleOfPeopleByCountry() {
		StringAndListParam countryParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(COUNTRY)));
		when(fhirPersonService.searchForPeople(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(is(countryParam)), isNull())).thenReturn(Collections.singletonList(person));
		
		Bundle results = resourceProvider.searchPeople(null, null, null, null, null, null, countryParam, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void getPatientResourceHistory_shouldReturnListOfResource() {
		IdType id = new IdType();
		id.setValue(PERSON_UUID);
		when(fhirPersonService.get(PERSON_UUID)).thenReturn(person);
		
		List<Resource> resources = resourceProvider.getPersonHistoryById(id);
		assertThat(resources, notNullValue());
		assertThat(resources, not(empty()));
		assertThat(resources.size(), equalTo(2));
	}
	
	@Test
	public void getPatientResourceHistory_shouldReturnProvenanceResources() {
		IdType id = new IdType();
		id.setValue(PERSON_UUID);
		when(fhirPersonService.get(PERSON_UUID)).thenReturn(person);
		
		List<Resource> resources = resourceProvider.getPersonHistoryById(id);
		assertThat(resources, not(empty()));
		assertThat(resources.stream().findAny().isPresent(), is(true));
		assertThat(resources.stream().findAny().get().getResourceType().name(), equalTo(Provenance.class.getSimpleName()));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getPatientHistoryByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_PERSON_UUID);
		assertThat(resourceProvider.getPersonHistoryById(idType).isEmpty(), is(true));
		assertThat(resourceProvider.getPersonHistoryById(idType).size(), equalTo(0));
	}
	
}
