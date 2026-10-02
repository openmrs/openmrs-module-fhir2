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
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
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
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirOpenmrsProviderServiceImplTest {
	
	private static final String PROVIDER_UUID = "28923n23-nmkn23-23923-23sd";
	
	private static final String WRONG_PROVIDER_UUID = "Wrong uuid";
	
	private static final String NAME = "John";
	
	private static final String IDENTIFIER = "3r34g346-tk";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirPractitionerDao dao;
	
	@Mock
	private PractitionerTranslator<Provider> translator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<Practitioner> searchQueryInclude;
	
	@Mock
	private SearchQuery<Provider, Practitioner, FhirPractitionerDao, PractitionerTranslator<Provider>, SearchQueryInclude<Practitioner>> searchQuery;
	
	private FhirOpenmrsProviderServiceImpl service;
	
	private Provider provider;
	
	private Practitioner practitioner;
	
	@Before
	public void setUp() {
		service = new FhirOpenmrsProviderServiceImpl() {
			
			@Override
			protected void validateObject(Provider object) {
			}
		};
		service.setDao(dao);
		service.setTranslator(translator);
		service.setSearchQuery(searchQuery);
		service.setSearchQueryInclude(searchQueryInclude);
		
		provider = new Provider();
		provider.setUuid(PROVIDER_UUID);
		provider.setRetired(false);
		provider.setName(NAME);
		provider.setIdentifier(IDENTIFIER);
		
		practitioner = new Practitioner();
		practitioner.setId(PROVIDER_UUID);
		practitioner.setIdentifier(Collections.singletonList(new Identifier().setValue(IDENTIFIER)));
		practitioner.setActive(false);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	// ---- get ----
	
	@Test
	public void get_shouldReturnPractitionerWhenDaoFindsProvider() {
		when(dao.get(PROVIDER_UUID)).thenReturn(provider);
		when(translator.toFhirResource(provider)).thenReturn(practitioner);
		
		Practitioner result = service.get(PROVIDER_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(PROVIDER_UUID));
	}
	
	// ---- create ----
	
	@Test
	public void create_shouldCreateProvider() {
		when(translator.toOpenmrsType(practitioner)).thenReturn(provider);
		when(dao.createOrUpdate(provider)).thenReturn(provider);
		when(translator.toFhirResource(provider)).thenReturn(practitioner);
		
		Practitioner result = service.create(practitioner);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(PROVIDER_UUID));
	}
	
	// ---- update ----
	
	@Test
	public void update_shouldUpdateProvider() {
		when(translator.toOpenmrsType(provider, practitioner)).thenReturn(provider);
		when(dao.createOrUpdate(provider)).thenReturn(provider);
		when(dao.get(PROVIDER_UUID)).thenReturn(provider);
		when(translator.toFhirResource(provider)).thenReturn(practitioner);
		
		Practitioner result = service.update(PROVIDER_UUID, practitioner);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(PROVIDER_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void update_shouldThrowWhenIdMismatch() {
		service.update(WRONG_PROVIDER_UUID, practitioner);
	}
	
	// ---- delete ----
	
	@Test
	public void delete_shouldDeleteProvider() {
		when(dao.delete(PROVIDER_UUID)).thenReturn(provider);
		
		service.delete(PROVIDER_UUID);
	}
	
	// ---- search ----
	
	@Test
	public void searchForPractitioners_shouldReturnPractitionersByName() {
		StringAndListParam name = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.NAME_PROPERTY, name);
		
		when(dao.getSearchResults(any())).thenReturn(singletonList(provider));
		lenient().when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(translator.toFhirResource(provider)).thenReturn(practitioner);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = service.searchForPractitioners(theParams);
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(((Practitioner) resultList.iterator().next()).getId(), equalTo(PROVIDER_UUID));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnPractitionersByIdentifier() {
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(IDENTIFIER));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    identifier);
		
		when(dao.getSearchResults(any())).thenReturn(singletonList(provider));
		lenient().when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(translator.toFhirResource(provider)).thenReturn(practitioner);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = service.searchForPractitioners(theParams);
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPractitioners_shouldAddIncludedResources() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("Practitioner:encounter"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		when(dao.getSearchResults(any())).thenReturn(singletonList(provider));
		lenient().when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(translator.toFhirResource(provider)).thenReturn(practitioner);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.singleton(new Encounter()));
		
		IBundleProvider results = service.searchForPractitioners(theParams);
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(2));
		assertThat(resultList, hasItem(is(instanceOf(Encounter.class))));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnEmptyCollectionWhenDaoReturnsEmpty() {
		StringAndListParam name = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam("nothing")));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.NAME_PROPERTY, name);
		
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = service.searchForPractitioners(theParams);
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
}
