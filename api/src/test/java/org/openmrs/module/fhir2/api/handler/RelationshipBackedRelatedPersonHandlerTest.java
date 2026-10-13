/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.handler;

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
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import java.util.Collections;
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

/**
 * Tests the CRUD/search wiring and dispatch predicates of the default RelatedPerson handler.
 * Orchestrator-level concerns (fan-out, the typed {@code searchForRelatedPeople} entry point) live
 * in {@code FhirRelatedPersonServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RelationshipBackedRelatedPersonHandlerTest {
	
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
	
	private RelationshipBackedRelatedPersonHandler handler;
	
	private org.openmrs.Person person;
	
	private Relationship relationship;
	
	private RelatedPerson relatedPerson;
	
	@Before
	public void setUp() {
		handler = new RelationshipBackedRelatedPersonHandler() {
			
			@Override
			protected void validateObject(Relationship object) {
			}
		};
		
		handler.setDao(dao);
		handler.setTranslator(translator);
		handler.setSearchQuery(searchQuery);
		handler.setSearchQueryInclude(searchQueryInclude);
		
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
		
		relationship = new Relationship();
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
	
	private IBundleProvider matchedBundle(SearchParameterMap theParams) {
		when(dao.getSearchResults(any())).thenReturn(singletonList(relationship));
		when(translator.toFhirResource(relationship)).thenReturn(relatedPerson);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		return handler.search(theParams);
	}
	
	private IBundleProvider emptyBundle(SearchParameterMap theParams) {
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		return handler.search(theParams);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeRelatedPersonImplicitProfile() {
		assertThat(handler.getImplicitProfile(),
		    equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-relatedperson"));
	}
	
	@Test
	public void canHandle_shouldAlwaysReturnTrue() {
		assertTrue(handler.canHandle(new RelatedPerson()));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptAnySearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	// ---- get ----
	
	@Test
	public void get_shouldRetrieveRelatedPersonByUUID() {
		Relationship relationship = new Relationship();
		relationship.setUuid(RELATED_PERSON_UUID);
		
		RelatedPerson relatedPerson = new RelatedPerson();
		relatedPerson.setId(RELATED_PERSON_UUID);
		
		when(dao.get(RELATED_PERSON_UUID)).thenReturn(relationship);
		when(translator.toFhirResource(relationship)).thenReturn(relatedPerson);
		
		RelatedPerson result = handler.get(RELATED_PERSON_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(RELATED_PERSON_UUID));
	}
	
	@Test
	public void get_shouldThrowResourceNotFoundForBadUuid() {
		assertThrows(ResourceNotFoundException.class, () -> handler.get(WRONG_RELATED_PERSON_UUID));
	}
	
	// ---- search ----
	
	@Test
	public void search_shouldReturnCollectionOfRelatedPersonForNameMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(GIVEN_NAME)));
		
		List<IBaseResource> resultList = get(
		    matchedBundle(new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, stringAndListParam)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void search_shouldReturnCollectionOfRelatedPersonForPartialMatchOnName() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(PERSON_PARTIAL_NAME)));
		
		List<IBaseResource> resultList = get(
		    matchedBundle(new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, stringAndListParam)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void search_shouldReturnEmptyCollectionWhenRelatedPersonNameNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_FOUND_NAME)));
		
		List<IBaseResource> resultList = get(
		    emptyBundle(new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, stringAndListParam)));
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void search_shouldReturnCollectionOfRelatedPersonWhenPersonGenderMatched() {
		TokenAndListParam tokenAndListParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(GENDER));
		
		List<IBaseResource> resultList = get(
		    matchedBundle(new SearchParameterMap().addParameter(FhirConstants.GENDER_SEARCH_HANDLER, tokenAndListParam)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void search_shouldReturnEmptyCollectionWhenRelatedPersonGenderNotMatched() {
		TokenAndListParam tokenAndListParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(WRONG_GENDER));
		
		List<IBaseResource> resultList = get(
		    emptyBundle(new SearchParameterMap().addParameter(FhirConstants.GENDER_SEARCH_HANDLER, tokenAndListParam)));
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void search_shouldReturnCollectionOfRelatedPersonWhenPersonBirthDateMatched() {
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(PERSON_BIRTH_DATE)
		        .setUpperBound(PERSON_BIRTH_DATE);
		
		List<IBaseResource> resultList = get(
		    matchedBundle(new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, dateRangeParam)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void search_shouldReturnEmptyCollectionWhenRelatedPersonBirthDateNotMatched() {
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(NOT_FOUND_PERSON_BIRTH_DATE)
		        .setUpperBound(NOT_FOUND_PERSON_BIRTH_DATE);
		
		List<IBaseResource> resultList = get(
		    emptyBundle(new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, dateRangeParam)));
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void search_shouldReturnCollectionOfRelatedPersonWhenPersonCityMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(CITY)));
		
		List<IBaseResource> resultList = get(matchedBundle(new SearchParameterMap()
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.CITY_PROPERTY, stringAndListParam)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void search_shouldReturnEmptyCollectionWhenRelatedPersonCityNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD)));
		
		List<IBaseResource> resultList = get(emptyBundle(new SearchParameterMap()
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.CITY_PROPERTY, stringAndListParam)));
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void search_shouldReturnCollectionOfRelatedPersonWhenPersonStateMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(STATE)));
		
		List<IBaseResource> resultList = get(matchedBundle(new SearchParameterMap()
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.STATE_PROPERTY, stringAndListParam)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void search_shouldReturnEmptyCollectionWhenRelatedPersonStateNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD)));
		
		List<IBaseResource> resultList = get(emptyBundle(new SearchParameterMap()
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.STATE_PROPERTY, stringAndListParam)));
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void search_shouldReturnCollectionOfRelatedPersonWhenPersonPostalCodeMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		
		List<IBaseResource> resultList = get(matchedBundle(new SearchParameterMap().addParameter(
		    FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.POSTAL_CODE_PROPERTY, stringAndListParam)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void search_shouldReturnEmptyCollectionWhenRelatedPersonPostalCodeNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD)));
		
		List<IBaseResource> resultList = get(emptyBundle(new SearchParameterMap().addParameter(
		    FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.POSTAL_CODE_PROPERTY, stringAndListParam)));
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void search_shouldReturnCollectionOfRelatedPersonWhenPersonCountryMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(COUNTRY)));
		
		List<IBaseResource> resultList = get(matchedBundle(new SearchParameterMap()
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.COUNTRY_PROPERTY, stringAndListParam)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void search_shouldReturnEmptyCollectionWhenPersonCountryNotMatched() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_ADDRESS_FIELD)));
		
		List<IBaseResource> resultList = get(emptyBundle(new SearchParameterMap()
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.COUNTRY_PROPERTY, stringAndListParam)));
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void search_shouldReturnCollectionOfPeopleWhenUUIDMatched() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PERSON_UUID));
		
		List<IBaseResource> resultList = get(matchedBundle(
		    new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldReturnEmptyCollectionWhenUUIDNotMatched() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(WRONG_PERSON_UUID));
		
		List<IBaseResource> resultList = get(emptyBundle(
		    new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)));
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void search_shouldReturnCollectionOfPeopleWhenLastUpdatedMatched() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		List<IBaseResource> resultList = get(matchedBundle(new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldReturnEmptyCollectionWhenLastUpdatedNotMatched() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(WRONG_LAST_UPDATED_DATE)
		        .setLowerBound(WRONG_LAST_UPDATED_DATE);
		
		List<IBaseResource> resultList = get(emptyBundle(new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)));
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void search_shouldAddRelatedResourcesWhenIncluded() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("RelatedPerson:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		when(dao.getSearchResults(any())).thenReturn(singletonList(relationship));
		when(translator.toFhirResource(relationship)).thenReturn(relatedPerson);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.singleton(new Patient()));
		
		List<IBaseResource> resultList = get(handler.search(theParams));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2));
		assertThat(resultList, hasItem(is(instanceOf(Patient.class))));
	}
	
	@Test
	public void search_shouldNotAddRelatedResourcesForEmptyInclude() {
		List<IBaseResource> resultList = get(matchedBundle(
		    new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, new HashSet<Include>())));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(1));
	}
}
