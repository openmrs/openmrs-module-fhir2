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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.convertors.conv30_40.Person30_40;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Person;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirPersonService;
import org.openmrs.module.fhir2.api.search.param.PersonSearchParams;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

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
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final int PREFERRED_PAGE_SIZE = 10;
	
	private static final int COUNT = 1;
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
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
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
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
		when(fhirPersonService.searchForPeople(new PersonSearchParams(argThat(is(nameParam)), isNull(), isNull(), isNull(),
		        isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))).thenReturn(
		            new MockIBundleProvider<>(Collections.singletonList(person), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchPeople(nameParam, null, null, null, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PERSON));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldReturnMatchingBundleOfPeopleByGender() {
		TokenAndListParam genderParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(GENDER));
		when(fhirPersonService.searchForPeople(new PersonSearchParams(isNull(), argThat(is(genderParam)), isNull(), isNull(),
		        isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))).thenReturn(
		            new MockIBundleProvider<>(Collections.singletonList(person), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchPeople(null, genderParam, null, null, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PERSON));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldReturnMatchingBundleOfPeopleByBirthDate() {
		DateRangeParam birthDateParam = new DateRangeParam().setLowerBound(BIRTH_DATE).setUpperBound(BIRTH_DATE);
		when(fhirPersonService.searchForPeople(new PersonSearchParams(isNull(), isNull(), argThat(is(birthDateParam)),
		        isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))).thenReturn(
		            new MockIBundleProvider<>(Collections.singletonList(person), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchPeople(null, null, birthDateParam, null, null, null, null, null,
		    null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PERSON));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldReturnMatchingBundleOfPeopleByCity() {
		StringAndListParam cityParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(CITY)));
		when(fhirPersonService.searchForPeople(new PersonSearchParams(isNull(), isNull(), isNull(), argThat(is(cityParam)),
		        isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))).thenReturn(
		            new MockIBundleProvider<>(Collections.singletonList(person), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchPeople(null, null, null, cityParam, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PERSON));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldReturnMatchingBundleOfPeopleByState() {
		StringAndListParam stateParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(STATE)));
		when(fhirPersonService.searchForPeople(new PersonSearchParams(isNull(), isNull(), isNull(), isNull(),
		        argThat(is(stateParam)), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))).thenReturn(
		            new MockIBundleProvider<>(Collections.singletonList(person), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchPeople(null, null, null, null, stateParam, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PERSON));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldReturnMatchingBundleOfPeopleByPostalCode() {
		StringAndListParam postalCodeParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		when(fhirPersonService.searchForPeople(new PersonSearchParams(isNull(), isNull(), isNull(), isNull(), isNull(),
		        argThat(is(postalCodeParam)), isNull(), isNull(), isNull(), isNull(), isNull()))).thenReturn(
		            new MockIBundleProvider<>(Collections.singletonList(person), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchPeople(null, null, null, null, null, postalCodeParam, null, null,
		    null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PERSON));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldReturnMatchingBundleOfPeopleByCountry() {
		StringAndListParam countryParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(COUNTRY)));
		when(fhirPersonService.searchForPeople(new PersonSearchParams(isNull(), isNull(), isNull(), isNull(), isNull(),
		        isNull(), argThat(is(countryParam)), isNull(), isNull(), isNull(), isNull()))).thenReturn(
		            new MockIBundleProvider<>(Collections.singletonList(person), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchPeople(null, null, null, null, null, null, countryParam, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PERSON));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldReturnMatchingBundleOfPeopleByUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PERSON_UUID));
		
		when(fhirPersonService.searchForPeople(new PersonSearchParams(isNull(), isNull(), isNull(), isNull(), isNull(),
		        isNull(), isNull(), argThat(is(uuid)), isNull(), isNull(), isNull()))).thenReturn(
		            new MockIBundleProvider<>(Collections.singletonList(person), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchPeople(null, null, null, null, null, null, null, uuid, null, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PERSON));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldReturnMatchingBundleOfPeopleByLastUpdated() {
		DateRangeParam lastUpdated = new DateRangeParam().setLowerBound(LAST_UPDATED_DATE).setUpperBound(LAST_UPDATED_DATE);
		
		when(fhirPersonService.searchForPeople(new PersonSearchParams(isNull(), isNull(), isNull(), isNull(), isNull(),
		        isNull(), isNull(), isNull(), argThat(is(lastUpdated)), isNull(), isNull()))).thenReturn(
		            new MockIBundleProvider<>(Collections.singletonList(person), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchPeople(null, null, null, null, null, null, null, null, lastUpdated,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PERSON));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldAddRelatedResourcesWhenIncluded() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("Person:patient"));
		
		when(fhirPersonService.searchForPeople(new PersonSearchParams(isNull(), isNull(), isNull(), isNull(), isNull(),
		        isNull(), isNull(), isNull(), isNull(), isNull(), argThat(is(includes))))).thenReturn(
		            new MockIBundleProvider<>(Arrays.asList(person, new Patient()), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchPeople(null, null, null, null, null, null, null, null, null, null,
		    includes);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.PERSON));
		assertThat(resultList.get(1).fhirType(), is(FhirConstants.PATIENT));
		assertThat(resultList.size(), equalTo(2));
	}
	
	@Test
	public void searchForPeople_shouldNotAddRelatedResourcesForEmptyInclude() {
		HashSet<Include> includes = new HashSet<>();
		
		when(fhirPersonService.searchForPeople(new PersonSearchParams(isNull(), isNull(), isNull(), isNull(), isNull(),
		        isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))).thenReturn(
		            new MockIBundleProvider<>(Collections.singletonList(person), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchPeople(null, null, null, null, null, null, null, null, null, null,
		    includes);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.PERSON));
		assertThat(resultList.size(), equalTo(1));
	}
	
	@Test
	public void createPerson_shouldCreateNewPerson() {
		when(fhirPersonService.create(any(org.hl7.fhir.r4.model.Person.class))).thenReturn(person);
		
		MethodOutcome result = resourceProvider.createPerson(Person30_40.convertPerson(person));
		
		assertThat(result, notNullValue());
		assertThat(result.getCreated(), is(true));
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(PERSON_UUID));
	}
	
	@Test
	public void deletePerson_shouldDeletePerson() {
		OperationOutcome result = resourceProvider.deletePerson(new IdType().setValue(PERSON_UUID));
		assertThat(result, notNullValue());
		assertThat(result.getIssue(), notNullValue());
		assertThat(result.getIssueFirstRep().getSeverity(), equalTo(OperationOutcome.IssueSeverity.INFORMATION));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getCode(), equalTo("MSG_DELETED"));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getDisplay(),
		    equalTo("This resource has been deleted"));
	}
	
	@Test
	public void updatePerson_shouldUpdatePerson() {
		when(fhirPersonService.update(eq(PERSON_UUID), any(org.hl7.fhir.r4.model.Person.class))).thenReturn(person);
		
		MethodOutcome result = resourceProvider.updatePerson(new IdType().setValue(PERSON_UUID),
		    Person30_40.convertPerson(person));
		
		assertThat(result, notNullValue());
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(PERSON_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updatePerson_shouldThrowInvalidRequestForUuidMismatch() {
		when(fhirPersonService.update(eq(WRONG_PERSON_UUID), any(org.hl7.fhir.r4.model.Person.class)))
		        .thenThrow(InvalidRequestException.class);
		
		resourceProvider.updatePerson(new IdType().setValue(WRONG_PERSON_UUID), Person30_40.convertPerson(person));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updatePerson_shouldThrowInvalidRequestForMissingId() {
		org.hl7.fhir.r4.model.Person noIdPerson = new org.hl7.fhir.r4.model.Person();
		
		when(fhirPersonService.update(eq(PERSON_UUID), any(org.hl7.fhir.r4.model.Person.class)))
		        .thenThrow(InvalidRequestException.class);
		
		resourceProvider.updatePerson(new IdType().setValue(PERSON_UUID), Person30_40.convertPerson(noIdPerson));
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void updatePerson_shouldThrowMethodNotAllowedIfDoesNotExist() {
		org.hl7.fhir.r4.model.Person wrongPerson = new org.hl7.fhir.r4.model.Person();
		wrongPerson.setId(WRONG_PERSON_UUID);
		
		when(fhirPersonService.update(eq(WRONG_PERSON_UUID), any(org.hl7.fhir.r4.model.Person.class)))
		        .thenThrow(MethodNotAllowedException.class);
		
		resourceProvider.updatePerson(new IdType().setValue(WRONG_PERSON_UUID), Person30_40.convertPerson(wrongPerson));
	}
	
}
