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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirPractitionerServiceImplTest {
	
	private static final String UUID = "28923n23-nmkn23-23923-23sd";
	
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
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private PractitionerTranslator<Provider> practitionerTranslator;
	
	@Mock
	private FhirPractitionerDao practitionerDao;
	
	@Mock
	private SearchQuery<Provider, Practitioner, FhirPractitionerDao, PractitionerTranslator<Provider>> searchQuery;
	
	private FhirPractitionerServiceImpl practitionerService;
	
	private Provider provider;
	
	private Practitioner practitioner;
	
	@Before
	public void setUp() {
		practitionerService = new FhirPractitionerServiceImpl();
		practitionerService.setDao(practitionerDao);
		practitionerService.setTranslator(practitionerTranslator);
		practitionerService.setSearchQuery(searchQuery);
		
		provider = new Provider();
		provider.setUuid(UUID);
		provider.setRetired(false);
		provider.setName(NAME);
		provider.setIdentifier(IDENTIFIER);
		
		practitioner = new Practitioner();
		practitioner.setId(UUID);
		practitioner.setIdentifier(Collections.singletonList(new Identifier().setValue(IDENTIFIER)));
		practitioner.setActive(false);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void shouldRetrievePractitionerByUuid() {
		when(practitionerDao.get(UUID)).thenReturn(provider);
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		
		Practitioner result = practitionerService.get(UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(UUID));
	}
	
	@Test
	public void shouldSearchForPractitionersByName() {
		StringAndListParam name = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PRACTITIONER_NAME_SEARCH_HANDLER,
		    name);
		
		when(practitionerDao.search(any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(provider));
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, practitionerDao, practitionerTranslator));
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		
		IBundleProvider results = practitionerService.searchForPractitioners(name, null, null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongName() {
		StringAndListParam name = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(WRONG_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PRACTITIONER_NAME_SEARCH_HANDLER,
		    name);
		
		when(practitionerDao.search(any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, practitionerDao, practitionerTranslator));
		
		IBundleProvider results = practitionerService.searchForPractitioners(name, null, null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForPractitionersByIdentifier() {
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(IDENTIFIER));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    identifier);
		
		when(practitionerDao.search(any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(provider));
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, practitionerDao, practitionerTranslator));
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, identifier, null, null, null, null, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongIdentifier() {
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(WRONG_IDENTIFIER));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    identifier);
		
		when(practitionerDao.search(any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, practitionerDao, practitionerTranslator));
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, identifier, null, null, null, null, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForPractitionersByGivenName() {
		StringAndListParam givenName = new StringAndListParam().addAnd(new StringParam(PRACTITIONER_GIVEN_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.GIVEN_PROPERTY, givenName);
		
		when(practitionerDao.search(any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(provider));
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, practitionerDao, practitionerTranslator));
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, givenName, null, null, null, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongGivenName() {
		StringAndListParam givenName = new StringAndListParam().addAnd(new StringParam(WRONG_GIVEN_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.GIVEN_PROPERTY, givenName);
		
		when(practitionerDao.search(any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, practitionerDao, practitionerTranslator));
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, givenName, null, null, null, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForPractitionersByFamilyName() {
		StringAndListParam familyName = new StringAndListParam().addAnd(new StringParam(PRACTITIONER_FAMILY_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.FAMILY_PROPERTY, familyName);
		
		when(practitionerDao.search(any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(provider));
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, practitionerDao, practitionerTranslator));
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, familyName, null, null, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongFamilyName() {
		StringAndListParam familyName = new StringAndListParam().addAnd(new StringParam(WRONG_FAMILY_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.FAMILY_PROPERTY, familyName);
		
		when(practitionerDao.search(any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, practitionerDao, practitionerTranslator));
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, familyName, null, null, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForPractitionersByAddressCity() {
		StringAndListParam city = new StringAndListParam().addAnd(new StringParam(CITY));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.CITY_PROPERTY, city);
		
		when(practitionerDao.search(any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(provider));
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, practitionerDao, practitionerTranslator));
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, city, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongAddressCity() {
		StringAndListParam city = new StringAndListParam().addAnd(new StringParam(WRONG_CITY));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.CITY_PROPERTY, city);
		
		when(practitionerDao.search(any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, practitionerDao, practitionerTranslator));
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, city, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForPractitionersByAddressState() {
		StringAndListParam state = new StringAndListParam().addAnd(new StringParam(STATE));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.STATE_PROPERTY, state);
		
		when(practitionerDao.search(any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(provider));
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, practitionerDao, practitionerTranslator));
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, state, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongAddressState() {
		StringAndListParam state = new StringAndListParam().addAnd(new StringParam(WRONG_STATE));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.STATE_PROPERTY, state);
		
		when(practitionerDao.search(any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, practitionerDao, practitionerTranslator));
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, state, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForPractitionersByAddressPostalCode() {
		StringAndListParam postalCode = new StringAndListParam().addAnd(new StringParam(POSTAL_CODE));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.POSTAL_CODE_PROPERTY, postalCode);
		
		when(practitionerDao.search(any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(provider));
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, practitionerDao, practitionerTranslator));
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, null, postalCode,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongAddressPostalCode() {
		StringAndListParam postalCode = new StringAndListParam().addAnd(new StringParam(WRONG_POSTAL_CODE));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.POSTAL_CODE_PROPERTY, postalCode);
		
		when(practitionerDao.search(any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, practitionerDao, practitionerTranslator));
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, null, postalCode,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldSearchForPractitionersByAddressCountry() {
		StringAndListParam country = new StringAndListParam().addAnd(new StringParam(COUNTRY));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.COUNTRY_PROPERTY, country);
		
		when(practitionerDao.search(any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(provider));
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, practitionerDao, practitionerTranslator));
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, null, null,
		    country);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongAddressCountry() {
		StringAndListParam country = new StringAndListParam().addAnd(new StringParam(WRONG_COUNTRY));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.COUNTRY_PROPERTY, country);
		
		when(practitionerDao.search(any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, practitionerDao, practitionerTranslator));
		
		IBundleProvider results = practitionerService.searchForPractitioners(null, null, null, null, null, null, null,
		    country);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
}
