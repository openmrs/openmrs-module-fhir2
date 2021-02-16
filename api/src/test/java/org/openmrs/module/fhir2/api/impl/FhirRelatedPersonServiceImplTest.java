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

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirRelatedPersonDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
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
	
	@Mock
	private FhirRelatedPersonDao dao;
	
	@Mock
	private RelatedPersonTranslator translator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<RelatedPerson> searchQueryInclude;
	
	@Mock
	private SearchQuery<Relationship, RelatedPerson, FhirRelatedPersonDao, RelatedPersonTranslator, SearchQueryInclude<RelatedPerson>> searchQuery;
	
	private FhirRelatedPersonServiceImpl relatedPersonService;
	
	private org.openmrs.Person person;
	
	private org.openmrs.Relationship relationship;
	
	private RelatedPerson relatedPerson;
	
	@Before
	public void setup() {
		relatedPersonService = new FhirRelatedPersonServiceImpl() {
			
			@Override
			protected void validateObject(Relationship object) {
			}
		};
		
		relatedPersonService.setDao(dao);
		relatedPersonService.setTranslator(translator);
		relatedPersonService.setSearchQuery(searchQuery);
		relatedPersonService.setSearchQueryInclude(searchQueryInclude);
		
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
		
		HumanName humanName = new HumanName();
		humanName.addGiven(GIVEN_NAME);
		humanName.setFamily(FAMILY_NAME);
		humanName.setId(PERSON_NAME_UUID);
		
		Address relatedPersonAddress = new Address();
		relatedPersonAddress.setCity(CITY);
		relatedPersonAddress.setState(STATE);
		relatedPersonAddress.setPostalCode(POSTAL_CODE);
		relatedPersonAddress.setCountry(COUNTRY);
		
		relatedPerson = new RelatedPerson();
		relatedPerson.addName(humanName);
		relatedPerson.addAddress(relatedPersonAddress);
		relatedPerson.setGender(Enumerations.AdministrativeGender.MALE);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
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
	public void shouldThrowResourceNotFoundWhenGetByWrongUuid() {
		assertThrows(ResourceNotFoundException.class, () -> relatedPersonService.get(WRONG_RELATED_PERSON_UUID));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnCollectionOfRelatedPersonForNameMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(GIVEN_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    stringAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(RELATED_PERSON_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(singletonList(relationship));
		when(translator.toFhirResource(relationship)).thenReturn(relatedPerson);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(stringAndListParam, null, null, null, null,
		    null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnCollectionOfRelatedPersonForPartialMatchOnName() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(PERSON_PARTIAL_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    stringAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(RELATED_PERSON_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(singletonList(relationship));
		when(translator.toFhirResource(relationship)).thenReturn(relatedPerson);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(stringAndListParam, null, null, null, null,
		    null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnEmptyCollectionWhenRelatedPersonNameNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_FOUND_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    stringAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(stringAndListParam, null, null, null, null,
		    null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnCollectionOfRelatedPersonWhenPersonGenderMatched() {
		TokenAndListParam tokenAndListParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(GENDER));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.GENDER_SEARCH_HANDLER,
		    tokenAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(RELATED_PERSON_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(singletonList(relationship));
		when(translator.toFhirResource(relationship)).thenReturn(relatedPerson);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, tokenAndListParam, null, null, null,
		    null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnEmptyCollectionWhenRelatedPersonGenderNotMatched() {
		TokenAndListParam tokenAndListParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(WRONG_GENDER));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.GENDER_SEARCH_HANDLER,
		    tokenAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, tokenAndListParam, null, null, null,
		    null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnCollectionOfRelatedPersonWhenPersonBirthDateMatched()
	        throws ParseException {
		Date birthDate = dateFormatter.parse(PERSON_BIRTH_DATE);
		person.setBirthdate(birthDate);
		
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(PERSON_BIRTH_DATE)
		        .setUpperBound(PERSON_BIRTH_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    dateRangeParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(RELATED_PERSON_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(singletonList(relationship));
		when(translator.toFhirResource(relationship)).thenReturn(relatedPerson);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, null, dateRangeParam, null, null, null,
		    null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnEmptyCollectionWhenRelatedPersonBirthDateNotMatched() {
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(NOT_FOUND_PERSON_BIRTH_DATE)
		        .setUpperBound(NOT_FOUND_PERSON_BIRTH_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    dateRangeParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, null, dateRangeParam, null, null, null,
		    null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnCollectionOfRelatedPersonWhenPersonCityMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(CITY)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.CITY_PROPERTY, stringAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(RELATED_PERSON_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(singletonList(relationship));
		when(translator.toFhirResource(relationship)).thenReturn(relatedPerson);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, null, null, stringAndListParam, null,
		    null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnEmptyCollectionWhenRelatedPersonCityNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.CITY_PROPERTY, stringAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, null, null, stringAndListParam, null,
		    null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnCollectionOfRelatedPersonWhenPersonStateMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(STATE)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.STATE_PROPERTY, stringAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(RELATED_PERSON_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(singletonList(relationship));
		when(translator.toFhirResource(relationship)).thenReturn(relatedPerson);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, null, null, null, stringAndListParam,
		    null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnEmptyCollectionWhenRelatedPersonStateNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.STATE_PROPERTY, stringAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, null, null, null, stringAndListParam,
		    null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnCollectionOfRelatedPersonWhenPersonPostalCodeMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.POSTAL_CODE_PROPERTY, stringAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(RELATED_PERSON_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(singletonList(relationship));
		when(translator.toFhirResource(relationship)).thenReturn(relatedPerson);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, null, null, null, null,
		    stringAndListParam, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnEmptyCollectionWhenRelatedPersonPostalCodeNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.POSTAL_CODE_PROPERTY, stringAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, null, null, null, null,
		    stringAndListParam, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnCollectionOfPersonWhenPersonCountryMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(COUNTRY)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.COUNTRY_PROPERTY, stringAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(RELATED_PERSON_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(singletonList(relationship));
		when(translator.toFhirResource(relationship)).thenReturn(relatedPerson);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, null, null, null, null, null,
		    stringAndListParam, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldReturnEmptyCollectionWhenPersonCountryNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.COUNTRY_PROPERTY, stringAndListParam);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, null, null, null, null, null,
		    stringAndListParam, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPeopleWhenUUIDMatched() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PERSON_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(RELATED_PERSON_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(singletonList(relationship));
		when(translator.toFhirResource(relationship)).thenReturn(relatedPerson);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, null, null, null, null, null, null, uuid,
		    null, null, null);
		
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
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, null, null, null, null, null, null, uuid,
		    null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), empty());
	}
	
	@Test
	public void searchForPeople_shouldReturnCollectionOfPeopleWhenLastUpdatedMatched() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(RELATED_PERSON_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(singletonList(relationship));
		when(translator.toFhirResource(relationship)).thenReturn(relatedPerson);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, null, null, null, null, null, null, null,
		    lastUpdated, null, null);
		
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
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, null, null, null, null, null, null, null,
		    lastUpdated, null, null);
		
		assertThat(results, notNullValue());
		assertThat(get(results), empty());
	}
	
	@Test
	public void searchForPeople_shouldAddRelatedResourcesWhenIncluded() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("RelatedPerson:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(RELATED_PERSON_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(singletonList(relationship));
		when(translator.toFhirResource(relationship)).thenReturn(relatedPerson);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.singleton(new Patient()));
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, null, null, null, null, null, null, null,
		    null, null, includes);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2));
		assertThat(resultList, hasItem(is(instanceOf(Patient.class))));
	}
	
	@Test
	public void searchForPeople_shouldNotAddRelatedResourcesForEmptyInclude() {
		HashSet<Include> includes = new HashSet<>();
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(RELATED_PERSON_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(singletonList(relationship));
		when(translator.toFhirResource(relationship)).thenReturn(relatedPerson);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = relatedPersonService.searchForRelatedPeople(null, null, null, null, null, null, null, null,
		    null, null, includes);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(1));
	}
	
}
