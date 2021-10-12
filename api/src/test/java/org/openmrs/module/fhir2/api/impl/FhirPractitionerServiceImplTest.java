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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.FhirConstants.FAMILY_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.NAME_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.NAME_SEARCH_HANDLER;

import java.util.Arrays;
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
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import org.hamcrest.Matchers;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.FhirUserService;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirPractitionerServiceImplTest {
	
	private static final String UUID = "28923n23-nmkn23-23923-23sd";
	
	private static final String WRONG_UUID = "Wrong uuid";
	
	private static final String UUID2 = "28923n90-nmkn23-23923-23sd";
	
	private static final String USER_SYSTEM_ID = "2-10";
	
	private static final String USER_NAME = "Doug";
	
	private static final String NAME = "John";
	
	private static final String WRONG_NAME = "Wrong name";
	
	private static final String IDENTIFIER = "3r34g346-tk";
	
	private static final String WRONG_IDENTIFIER = "Wrong identifier";
	
	private static final String PRACTITIONER_GIVEN_NAME = "John";
	
	private static final String WRONG_GIVEN_NAME = "Wrong given name";
	
	private static final String PRACTITIONER_FAMILY_NAME = "Doe";
	
	private static final String WRONG_FAMILY_NAME = "Wrong family name";
	
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
	private PractitionerTranslator<Provider> practitionerTranslator;
	
	@Mock
	private FhirPractitionerDao practitionerDao;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<Practitioner> searchQueryInclude;
	
	@Mock
	private SearchQuery<Provider, Practitioner, FhirPractitionerDao, PractitionerTranslator<Provider>, SearchQueryInclude<Practitioner>> searchQuery;
	
	@Mock
	private FhirUserService userService;
	
	@Mock
	private SearchQueryBundleProvider<User, Practitioner> userPractitionerSearchQueryBundleProvider;
	
	private FhirPractitionerServiceImpl practitionerService;
	
	private Provider provider;
	
	private Practitioner practitioner;
	
	private Practitioner practitioner2;
	
	@Before
	public void setUp() {
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		practitionerService = new FhirPractitionerServiceImpl();
		practitionerService.setDao(practitionerDao);
		practitionerService.setTranslator(practitionerTranslator);
		practitionerService.setSearchQuery(searchQuery);
		practitionerService.setUserService(userService);
		practitionerService.setSearchQueryInclude(searchQueryInclude);
		practitionerService.setGlobalPropertyService(globalPropertyService);
		
		provider = new Provider();
		provider.setUuid(UUID);
		provider.setRetired(false);
		provider.setName(NAME);
		provider.setIdentifier(IDENTIFIER);
		
		practitioner = new Practitioner();
		practitioner.setId(UUID);
		practitioner.setIdentifier(Collections.singletonList(new Identifier().setValue(IDENTIFIER)));
		practitioner.setActive(false);
		
		practitioner2 = new Practitioner();
		practitioner2.setId(UUID2);
		practitioner2.setIdentifier(Collections.singletonList(new Identifier().setValue(USER_SYSTEM_ID)));
		practitioner2.setActive(false);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void shouldRetrievePractitionerByUuidWhoIsProvider() {
		when(practitionerDao.get(UUID)).thenReturn(provider);
		when(practitionerTranslator.toFhirResource(provider, null)).thenReturn(practitioner);
		
		Practitioner result = practitionerService.get(UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(UUID));
	}
	
	@Test
	public void shouldRetrievePractitionerByUuidWhoIsUser() {
		when(practitionerDao.get(UUID2)).thenReturn(null);
		when(userService.get(UUID2)).thenReturn(practitioner2);
		
		Practitioner result = practitionerService.get(UUID2);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(UUID2));
	}
	
	@Test
	public void shouldSearchForPractitionersByNameWhoIsProvider() {
		StringAndListParam name = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(NAME_SEARCH_HANDLER, NAME_PROPERTY, name);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(singletonList(UUID));
		when(practitionerDao.getSearchResults(any(), any())).thenReturn(singletonList(provider));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, name, null, null, null, null, null, null,
		    null, null, null);
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void shouldSearchForPractitionersByNameWhoIsUser() {
		StringAndListParam name = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(USER_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(NAME_SEARCH_HANDLER, NAME_PROPERTY, name);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider(practitioner2));
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, name, null, null, null, null, null, null,
		    null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void shouldSearchForPractitionersByNameWhoIsUserAndProvider() {
		StringAndListParam name = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(USER_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(NAME_SEARCH_HANDLER, NAME_PROPERTY, name);
		
		when(practitionerDao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(provider));
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(UUID));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider(practitioner2));
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, name, null, null, null, null, null, null,
		    null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(2)));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongName() {
		StringAndListParam name = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(WRONG_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(NAME_SEARCH_HANDLER, NAME_PROPERTY, name);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, name, null, null, null, null, null, null,
		    null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForPractitionersByIdentifier() {
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(IDENTIFIER));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    identifier);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(UUID));
		when(practitionerDao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(provider));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(identifier, null, null, null, null, null, null,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongIdentifier() {
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(WRONG_IDENTIFIER));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    identifier);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(identifier, null, null, null, null, null, null,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForPractitionersByGivenNameWhoIsUserAndProvider() {
		StringAndListParam givenName = new StringAndListParam().addAnd(new StringParam(PRACTITIONER_GIVEN_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.GIVEN_PROPERTY, givenName);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(UUID));
		when(practitionerDao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(provider));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider(practitioner2));
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, givenName, null, null, null, null,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(2)));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongGivenName() {
		StringAndListParam givenName = new StringAndListParam().addAnd(new StringParam(WRONG_GIVEN_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.GIVEN_PROPERTY, givenName);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, givenName, null, null, null, null,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForPractitionersByFamilyNameWhoIsProvider() {
		StringAndListParam familyName = new StringAndListParam().addAnd(new StringParam(PRACTITIONER_FAMILY_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.FAMILY_PROPERTY, familyName);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(UUID));
		when(practitionerDao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(provider));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, familyName, null, null, null,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void shouldSearchForPractitionersByFamilyNameWhoIsUser() {
		StringAndListParam familyName = new StringAndListParam().addAnd(new StringParam(PRACTITIONER_FAMILY_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(NAME_SEARCH_HANDLER, FAMILY_PROPERTY,
		    familyName);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider(practitioner2));
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, familyName, null, null, null,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void shouldSearchForPractitionersByFamilyNameWhoIsUserAndProvider() {
		StringAndListParam familyName = new StringAndListParam().addAnd(new StringParam(PRACTITIONER_FAMILY_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(NAME_SEARCH_HANDLER, FAMILY_PROPERTY,
		    familyName);
		
		when(practitionerDao.getSearchResults(any(), any())).thenReturn(singletonList(provider));
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(singletonList(UUID));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider(practitioner2));
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, familyName, null, null, null,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(2)));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongFamilyName() {
		StringAndListParam familyName = new StringAndListParam().addAnd(new StringParam(WRONG_FAMILY_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.FAMILY_PROPERTY, familyName);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, familyName, null, null, null,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForPractitionersByAddressCity() {
		StringAndListParam city = new StringAndListParam().addAnd(new StringParam(CITY));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.CITY_PROPERTY, city);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(UUID));
		when(practitionerDao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(provider));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, city, null, null, null,
		    null, null, null);
		
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
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, city, null, null, null,
		    null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForPractitionersByAddressState() {
		StringAndListParam state = new StringAndListParam().addAnd(new StringParam(STATE));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.STATE_PROPERTY, state);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(UUID));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(practitionerDao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(provider));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, state, null, null,
		    null, null, null);
		
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
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, state, null, null,
		    null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForPractitionersByAddressPostalCode() {
		StringAndListParam postalCode = new StringAndListParam().addAnd(new StringParam(POSTAL_CODE));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.POSTAL_CODE_PROPERTY, postalCode);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(UUID));
		when(practitionerDao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(provider));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, null, postalCode,
		    null, null, null, null);
		
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
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, null, postalCode,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForPractitionersByAddressCountry() {
		StringAndListParam country = new StringAndListParam().addAnd(new StringParam(COUNTRY));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.COUNTRY_PROPERTY, country);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(UUID));
		when(practitionerDao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(provider));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, null, null,
		    country, null, null, null);
		
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
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, null, null,
		    country, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForPractitionersByUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(UUID));
		when(practitionerDao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(provider));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, null, null, null,
		    uuid, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(WRONG_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, null, null, null,
		    uuid, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForPractitionersByLastUpdated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(UUID));
		when(practitionerDao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(provider));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, null, null, null,
		    null, lastUpdated, null);
		
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
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, null, null, null,
		    null, lastUpdated, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldAddRelatedResourcesToReturnedResultsWhenReverseIncluded() {
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("Encounter:participant"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER,
		    revIncludes);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(singletonList(UUID));
		when(practitionerDao.getSearchResults(any(), any())).thenReturn(singletonList(provider));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.singleton(new Encounter()));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, null, null, null,
		    null, null, revIncludes);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, Matchers.not(empty()));
		assertThat(resultList.size(), Matchers.equalTo(2));
		assertThat(resultList, hasItem(is(instanceOf(Encounter.class))));
	}
	
	@Test
	public void shouldNotAddRelatedResourcesToReturnedResultsForEmptyReverseInclude() {
		HashSet<Include> revIncludes = new HashSet<>();
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER,
		    revIncludes);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(singletonList(UUID));
		when(practitionerDao.getSearchResults(any(), any())).thenReturn(singletonList(provider));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		when(userService.searchForUsers(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, null, null, null,
		    null, null, revIncludes);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, Matchers.not(empty()));
		assertThat(resultList.size(), Matchers.equalTo(1));
	}
	
	@Test
	public void shouldAddRelatedResourcesFromProviderAndUserWhenReverseIncluded() {
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("Encounter:participant"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER,
		    revIncludes);
		
		when(practitionerDao.getSearchResultUuids(any())).thenReturn(singletonList(UUID));
		when(practitionerDao.getSearchResults(any(), any())).thenReturn(singletonList(provider));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.singleton(new Encounter()));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        practitionerDao, practitionerTranslator, globalPropertyService, searchQueryInclude));
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		when(userService.searchForUsers(any())).thenReturn(userPractitionerSearchQueryBundleProvider);
		
		when(userPractitionerSearchQueryBundleProvider.size()).thenReturn(1);
		when(userPractitionerSearchQueryBundleProvider.getResources(anyInt(), anyInt()))
		        .thenReturn(Arrays.asList(practitioner2, new Encounter()));
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, null, null, null,
		    null, null, revIncludes);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, Matchers.not(empty()));
		assertThat(resultList.size(), Matchers.equalTo(4));
		
		// check if the order is as expected
		assertThat(resultList.get(0), equalTo(practitioner));
		assertThat(resultList.get(1), equalTo(practitioner2));
		assertThat(resultList.subList(2, 4), everyItem(is(instanceOf(Encounter.class))));
	}
	
}
