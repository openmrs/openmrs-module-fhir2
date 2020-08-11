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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirPersonDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
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
	
	private static final String WRONG_PERSON_UUID = "Wrong uuid";
	
	private static final String PERSON_NAME_UUID = "test-uuid-1223-2312";
	
	private static final String PERSON_BIRTH_DATE = "1996-12-12";
	
	private static final String NOT_FOUND_PERSON_BIRTH_DATE = "0001-10-10";
	
	private static final String CITY = "Washington";
	
	private static final String STATE = "Washington";
	
	private static final String POSTAL_CODE = "98136";
	
	private static final String COUNTRY = "Washington";
	
	private static final String NOT_ADDRESS_FIELD = "not an address field";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final String WRONG_LAST_UPDATED_DATE = "2020-09-09";
	
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	private org.openmrs.Person person;
	
	@Mock
	private FhirPersonDao dao;
	
	@Mock
	private PersonTranslator personTranslator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<Person> searchQueryInclude;
	
	@Mock
	private SearchQuery<org.openmrs.Person, Person, FhirPersonDao, PersonTranslator, SearchQueryInclude<Person>> searchQuery;
	
	private FhirPersonServiceImpl personService;
	
	private Person fhirPerson;
	
	@Before
	public void setUp() {
		personService = new FhirPersonServiceImpl() {
			
			@Override
			protected void validateObject(org.openmrs.Person object) {
			}
		};
		
		personService.setDao(dao);
		personService.setTranslator(personTranslator);
		personService.setSearchQuery(searchQuery);
		personService.setSearchQueryInclude(searchQueryInclude);
		
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
		
		HumanName humanName = new HumanName();
		humanName.addGiven(GIVEN_NAME);
		humanName.setFamily(FAMILY_NAME);
		
		fhirPerson = new Person();
		fhirPerson.setId(PERSON_UUID);
		fhirPerson.setGender(Enumerations.AdministrativeGender.MALE);
		fhirPerson.addName(humanName);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPersonForGivenNameMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(GIVEN_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    stringAndListParam);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(person));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(PERSON_UUID));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(personTranslator.toFhirResource(person)).thenReturn(fhirPerson);
		
		IBundleProvider results = personService.searchForPeople(stringAndListParam, null, null, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPersonForPartialMatchOnName() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(PERSON_PARTIAL_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    stringAndListParam);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(person));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(PERSON_UUID));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(personTranslator.toFhirResource(person)).thenReturn(fhirPerson);
		
		IBundleProvider results = personService.searchForPeople(stringAndListParam, null, null, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenPersonNameNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_FOUND_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    stringAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = personService.searchForPeople(stringAndListParam, null, null, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPersonWhenPersonGenderMatched() {
		TokenAndListParam tokenAndListParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(GENDER));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.GENDER_SEARCH_HANDLER,
		    tokenAndListParam);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(person));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(PERSON_UUID));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(personTranslator.toFhirResource(person)).thenReturn(fhirPerson);
		
		IBundleProvider results = personService.searchForPeople(null, tokenAndListParam, null, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenPersonGenderNotMatched() {
		TokenAndListParam tokenAndListParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(WRONG_GENDER));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.GENDER_SEARCH_HANDLER,
		    tokenAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = personService.searchForPeople(null, tokenAndListParam, null, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPersonWhenPersonBirthDateMatched() throws ParseException {
		Date birthDate = dateFormatter.parse(PERSON_BIRTH_DATE);
		person.setBirthdate(birthDate);
		
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(PERSON_BIRTH_DATE)
		        .setUpperBound(PERSON_BIRTH_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    dateRangeParam);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(person));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(PERSON_UUID));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(personTranslator.toFhirResource(person)).thenReturn(fhirPerson);
		
		IBundleProvider results = personService.searchForPeople(null, null, dateRangeParam, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenPersonBirthDateNotMatched() {
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(NOT_FOUND_PERSON_BIRTH_DATE)
		        .setUpperBound(NOT_FOUND_PERSON_BIRTH_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    dateRangeParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = personService.searchForPeople(null, null, dateRangeParam, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPersonWhenPersonCityMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(CITY)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.CITY_PROPERTY, stringAndListParam);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(person));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(PERSON_UUID));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(personTranslator.toFhirResource(person)).thenReturn(fhirPerson);
		
		IBundleProvider results = personService.searchForPeople(null, null, null, stringAndListParam, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenPersonCityNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.CITY_PROPERTY, stringAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = personService.searchForPeople(null, null, null, stringAndListParam, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPersonWhenPersonStateMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(STATE)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.STATE_PROPERTY, stringAndListParam);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(person));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(PERSON_UUID));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(personTranslator.toFhirResource(person)).thenReturn(fhirPerson);
		
		IBundleProvider results = personService.searchForPeople(null, null, null, null, stringAndListParam, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenPersonStateNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.STATE_PROPERTY, stringAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = personService.searchForPeople(null, null, null, null, stringAndListParam, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPersonWhenPersonPostalCodeMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.POSTAL_CODE_PROPERTY, stringAndListParam);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(person));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(PERSON_UUID));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(personTranslator.toFhirResource(person)).thenReturn(fhirPerson);
		
		IBundleProvider results = personService.searchForPeople(null, null, null, null, null, stringAndListParam, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenPersonPostalCodeNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.POSTAL_CODE_PROPERTY, stringAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = personService.searchForPeople(null, null, null, null, null, stringAndListParam, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPersonWhenPersonCountryMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(COUNTRY)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.COUNTRY_PROPERTY, stringAndListParam);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(person));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(PERSON_UUID));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(personTranslator.toFhirResource(person)).thenReturn(fhirPerson);
		
		IBundleProvider results = personService.searchForPeople(null, null, null, null, null, null, stringAndListParam, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenPersonCountryNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.COUNTRY_PROPERTY, stringAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = personService.searchForPeople(null, null, null, null, null, null, stringAndListParam, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPeopleWhenUUIDMatched() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PERSON_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(person));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(PERSON_UUID));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(personTranslator.toFhirResource(person)).thenReturn(fhirPerson);
		
		IBundleProvider results = personService.searchForPeople(null, null, null, null, null, null, null, uuid, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenUUIDNotMatched() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(WRONG_PERSON_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = personService.searchForPeople(null, null, null, null, null, null, null, uuid, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), empty());
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPeopleWhenLastUpdatedMatched() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(person));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(PERSON_UUID));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(personTranslator.toFhirResource(person)).thenReturn(fhirPerson);
		
		IBundleProvider results = personService.searchForPeople(null, null, null, null, null, null, null, null, lastUpdated,
		    null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenLastUpdatedNotMatched() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(WRONG_LAST_UPDATED_DATE)
		        .setLowerBound(WRONG_LAST_UPDATED_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, personTranslator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = personService.searchForPeople(null, null, null, null, null, null, null, null, lastUpdated,
		    null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), empty());
	}
	
}
