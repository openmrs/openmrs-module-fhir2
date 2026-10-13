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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
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

/**
 * Tests the read/search wiring and dispatch predicates of the default ValueSet handler.
 * Orchestrator-level concerns (fan-out, the typed {@code searchForValueSets} entry point) live in
 * {@code FhirValueSetServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConceptBackedValueSetHandlerTest {
	
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
	
	private ConceptBackedValueSetHandler handler;
	
	private Concept concept;
	
	private ValueSet valueSet;
	
	@Before
	public void setup() {
		handler = new ConceptBackedValueSetHandler();
		handler.setDao(dao);
		handler.setTranslator(translator);
		handler.setSearchQuery(searchQuery);
		handler.setSearchQueryInclude(searchQueryInclude);
		
		concept = new Concept();
		concept.setUuid(ROOT_CONCEPT_UUID);
		
		valueSet = new ValueSet();
		valueSet.setId(ROOT_CONCEPT_UUID);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeValueSetImplicitProfile() {
		assertThat(handler.getImplicitProfile(), equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-valueset"));
	}
	
	@Test
	public void canHandle_shouldAlwaysReturnTrue() {
		assertTrue(handler.canHandle(new ValueSet()));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptAnySearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	// ---- get ----
	
	@Test
	public void get_shouldGetValueSetByUuid() {
		when(dao.get(ROOT_CONCEPT_UUID)).thenReturn(concept);
		when(translator.toFhirResource(concept)).thenReturn(valueSet);
		
		ValueSet result = handler.get(ROOT_CONCEPT_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(ROOT_CONCEPT_UUID));
	}
	
	// ---- search ----
	
	@Test
	public void search_shouldSearchForValueSetsByName() {
		StringAndListParam titleParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(ROOT_CONCEPT_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.TITLE_SEARCH_HANDLER, titleParam);
		
		when(dao.getSearchResults(any())).thenReturn(singletonList(concept));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(translator.toFhirResource(concept)).thenReturn(valueSet);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		List<IBaseResource> resultList = get(handler.search(theParams));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldReturnEmptyCollectionByWrongName() {
		StringAndListParam titleParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam("wrong name")));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.TITLE_SEARCH_HANDLER, titleParam);
		
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		List<IBaseResource> resultList = get(handler.search(theParams));
		
		assertThat(resultList, empty());
	}
}
