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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.FhirConstants.ADDRESS_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.FhirConstants.CITY_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.COMMON_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.FhirConstants.COUNTRY_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.IDENTIFIER_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.FhirConstants.ID_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.LAST_UPDATED_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.NAME_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.NAME_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.FhirConstants.POSTAL_CODE_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.STATE_PROPERTY;

import java.util.Collections;
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
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirUserDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirUserServiceImplTest {
	
	private static final String USER_UUID = "5f07c6ff-c483-4e77-815e-44dd650470e7";
	
	private static final String WRONG_USER_UUID = "1a1d2623-2f67-47de-8fb0-b02f51e378b7";
	
	private static final String USER_SYSTEM_ID = "2-10";
	
	private static final String WRONG_USER_SYSTEM_ID = "2-101";
	
	private static final String USER_NAME = "Doug";
	
	private static final String WRONG_NAME = "WRONG NAME";
	
	private static final String CITY = "Indianapolis";
	
	private static final String WRONG_CITY = "Wrong city";
	
	private static final String STATE = "IN";
	
	private static final String WRONG_STATE = "Wrong state";
	
	private static final String POSTAL_CODE = "46202";
	
	private static final String WRONG_POSTAL_CODE = "Wrong postal code";
	
	private static final String COUNTRY = "USA";
	
	private static final String WRONG_COUNTRY = "Wrong country";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final String WRONG_LAST_UPDATED_DATE = "2020-09-09";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirUserDao dao;
	
	private FhirUserServiceImpl userService;
	
	@Mock
	private SearchQueryInclude<Practitioner> searchQueryInclude;
	
	@Mock
	private SearchQuery<User, Practitioner, FhirUserDao, PractitionerTranslator<User>, SearchQueryInclude<Practitioner>> searchQuery;
	
	@Mock
	private PractitionerTranslator<User> translator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private User user;
	
	private Practitioner practitioner;
	
	@Before
	public void setup() {
		userService = new FhirUserServiceImpl();
		userService.setDao(dao);
		userService.setTranslator(translator);
		userService.setSearchQuery(searchQuery);
		userService.setSearchQueryInclude(searchQueryInclude);
		
		user = new User();
		user.setUuid(USER_UUID);
		user.setRetired(false);
		user.setUsername(USER_NAME);
		user.setSystemId(USER_SYSTEM_ID);
		
		practitioner = new Practitioner();
		practitioner.setId(USER_UUID);
		practitioner.setIdentifier(Collections.singletonList(new Identifier().setValue(USER_SYSTEM_ID)));
		practitioner.setActive(false);
		
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void shouldSearchForUsersByName() {
		StringAndListParam name = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(USER_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(NAME_SEARCH_HANDLER, NAME_PROPERTY, name);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(USER_UUID));
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(singletonList(user));
		
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(translator.toFhirResource(user)).thenReturn(practitioner);
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = userService.searchForUsers(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongName() {
		StringAndListParam name = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(WRONG_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(NAME_SEARCH_HANDLER, NAME_PROPERTY, name);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = userService.searchForUsers(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldsearchForUsersByIdentifier() {
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(USER_SYSTEM_ID));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(IDENTIFIER_SEARCH_HANDLER, identifier);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(USER_UUID));
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(singletonList(user));
		
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(translator.toFhirResource(user)).thenReturn(practitioner);
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = userService.searchForUsers(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongIdentifier() {
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(WRONG_USER_SYSTEM_ID));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    identifier);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = userService.searchForUsers(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldsearchForUsersByAddressCity() {
		StringAndListParam city = new StringAndListParam().addAnd(new StringParam(CITY));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(ADDRESS_SEARCH_HANDLER, CITY_PROPERTY, city);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(USER_UUID));
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(singletonList(user));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(translator.toFhirResource(user)).thenReturn(practitioner);
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = userService.searchForUsers(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongAddressCity() {
		StringAndListParam city = new StringAndListParam().addAnd(new StringParam(WRONG_CITY));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.CITY_PROPERTY, city);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = userService.searchForUsers(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldsearchForUsersByAddressState() {
		StringAndListParam state = new StringAndListParam().addAnd(new StringParam(STATE));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(ADDRESS_SEARCH_HANDLER, STATE_PROPERTY, state);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(USER_UUID));
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(singletonList(user));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(translator.toFhirResource(user)).thenReturn(practitioner);
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = userService.searchForUsers(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongAddressState() {
		StringAndListParam state = new StringAndListParam().addAnd(new StringParam(WRONG_STATE));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.STATE_PROPERTY, state);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = userService.searchForUsers(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldsearchForUsersByAddressPostalCode() {
		StringAndListParam postalCode = new StringAndListParam().addAnd(new StringParam(POSTAL_CODE));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(ADDRESS_SEARCH_HANDLER, POSTAL_CODE_PROPERTY,
		    postalCode);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(USER_UUID));
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(singletonList(user));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(translator.toFhirResource(user)).thenReturn(practitioner);
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = userService.searchForUsers(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongAddressPostalCode() {
		StringAndListParam postalCode = new StringAndListParam().addAnd(new StringParam(WRONG_POSTAL_CODE));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.POSTAL_CODE_PROPERTY, postalCode);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = userService.searchForUsers(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForUsersByAddressCountry() {
		StringAndListParam country = new StringAndListParam().addAnd(new StringParam(COUNTRY));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(ADDRESS_SEARCH_HANDLER, COUNTRY_PROPERTY,
		    country);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(USER_UUID));
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(singletonList(user));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(translator.toFhirResource(user)).thenReturn(practitioner);
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = userService.searchForUsers(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongAddressCountry() {
		StringAndListParam country = new StringAndListParam().addAnd(new StringParam(WRONG_COUNTRY));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.COUNTRY_PROPERTY, country);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = userService.searchForUsers(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForUsersByUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(USER_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(COMMON_SEARCH_HANDLER, ID_PROPERTY, uuid);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(USER_UUID));
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(singletonList(user));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(translator.toFhirResource(user)).thenReturn(practitioner);
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = userService.searchForUsers(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(WRONG_USER_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = userService.searchForUsers(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldsearchForUsersByLastUpdated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(COMMON_SEARCH_HANDLER, LAST_UPDATED_PROPERTY,
		    lastUpdated);
		
		when(dao.getSearchResultUuids(any())).thenReturn(singletonList(USER_UUID));
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(singletonList(user));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(translator.toFhirResource(user)).thenReturn(practitioner);
		
		IBundleProvider results = userService.searchForUsers(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongLastUpdated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(WRONG_LAST_UPDATED_DATE)
		        .setLowerBound(WRONG_LAST_UPDATED_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, lastUpdated);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = userService.searchForUsers(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
}
