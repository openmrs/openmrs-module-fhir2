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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirConceptDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ValueSetTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirValueSetServiceImplTest {
	
	private static final Integer ROOT_CONCEPT_ID = 123;
	
	private static final String ROOT_CONCEPT_UUID = "0f97e14e-cdc2-49ac-9255-b5126f8a5147";
	
	private static final String ROOT_CONCEPT_NAME = "FOOD CONSTRUCT";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirConceptDao dao;
	
	@Mock
	private ValueSetTranslator translator;
	
	@Mock
	private SearchQueryInclude<ValueSet> searchQueryInclude;
	
	@Mock
	private SearchQuery<Concept, ValueSet, FhirConceptDao, ValueSetTranslator, SearchQueryInclude<ValueSet>> searchQuery;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirValueSetServiceImpl fhirValueSetService;
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	private Concept concept;
	
	private ValueSet valueSet;
	
	@Before
	public void setup() {
		fhirValueSetService = new FhirValueSetServiceImpl();
		fhirValueSetService.setDao(dao);
		fhirValueSetService.setTranslator(translator);
		fhirValueSetService.setSearchQuery(searchQuery);
		fhirValueSetService.setSearchQueryInclude(searchQueryInclude);
		
		concept = new Concept();
		concept.setUuid(ROOT_CONCEPT_UUID);
		
		valueSet = new ValueSet();
		valueSet.setId(ROOT_CONCEPT_UUID);
	}
	
	@Test
	public void get_shouldGetEncounterByUuid() {
		when(dao.get(ROOT_CONCEPT_UUID)).thenReturn(concept);
		when(translator.toFhirResource(concept)).thenReturn(valueSet);
		
		ValueSet valueSet = fhirValueSetService.get(ROOT_CONCEPT_UUID);
		
		assertThat(valueSet, notNullValue());
		assertThat(valueSet.getId(), notNullValue());
		assertThat(valueSet.getId(), equalTo(ROOT_CONCEPT_UUID));
	}
	
	@Test
	public void shouldSearchForValueSetsByName() {
		StringAndListParam titleParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(ROOT_CONCEPT_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.TITLE_SEARCH_HANDLER, titleParam);
		
		when(dao.getSearchResultIds(any())).thenReturn(singletonList(ROOT_CONCEPT_ID));
		when(dao.getSearchResults(any(), any())).thenReturn(singletonList(concept));
		
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(translator.toFhirResource(concept)).thenReturn(valueSet);
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = fhirValueSetService.searchForValueSets(titleParam);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void shouldReturnEmptyCollectionByWrongName() {
		StringAndListParam titleParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam("wrong name")));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.TITLE_SEARCH_HANDLER, titleParam);
		
		when(dao.getSearchResultIds(any())).thenReturn(Collections.emptyList());
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = fhirValueSetService.searchForValueSets(titleParam);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
}
