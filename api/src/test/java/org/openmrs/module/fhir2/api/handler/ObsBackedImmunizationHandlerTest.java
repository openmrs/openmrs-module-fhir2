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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.FhirConstants.CODED_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.IMMUNIZATION_GROUPING_CONCEPT;

import java.util.List;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.hl7.fhir.r4.model.Immunization;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.PropParam;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ImmunizationTranslator;
import org.openmrs.module.fhir2.api.util.ImmunizationObsGroupHelper;

/**
 * Tests the dispatch predicates and search wiring of the default Immunization handler. The CRUD
 * paths (create / update / delete against a real obs group) are exercised end-to-end through the
 * composite by {@code FhirImmunizationServiceImplTest}, which runs against a real database.
 */
@RunWith(MockitoJUnitRunner.class)
public class ObsBackedImmunizationHandlerTest {
	
	private static final Integer GROUPING_CONCEPT_ID = 1421;
	
	private static final String PATIENT_IDENTIFIER = "12345K";
	
	@Mock
	private FhirObservationDao dao;
	
	@Mock
	private ImmunizationTranslator translator;
	
	@Mock
	private ImmunizationObsGroupHelper helper;
	
	@Mock
	private SearchQueryInclude<Immunization> searchQueryInclude;
	
	@Mock
	private SearchQuery<Obs, Immunization, FhirObservationDao, ImmunizationTranslator, SearchQueryInclude<Immunization>> searchQuery;
	
	private ObsBackedImmunizationHandler handler;
	
	@Before
	public void setUp() {
		handler = new ObsBackedImmunizationHandler();
		
		handler.setDao(dao);
		handler.setTranslator(translator);
		handler.setHelper(helper);
		handler.setSearchQuery(searchQuery);
		handler.setSearchQueryInclude(searchQueryInclude);
	}
	
	private void stubGroupingConcept() {
		Concept groupingConcept = new Concept();
		groupingConcept.setConceptId(GROUPING_CONCEPT_ID);
		when(helper.concept(IMMUNIZATION_GROUPING_CONCEPT)).thenReturn(groupingConcept);
	}
	
	private SearchParameterMap capturedSearchParams() {
		ArgumentCaptor<SearchParameterMap> captor = ArgumentCaptor.forClass(SearchParameterMap.class);
		verify(searchQuery).getQueryResults(captor.capture(), eq(dao), eq(translator), eq(searchQueryInclude));
		return captor.getValue();
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeImmunizationImplicitProfile() {
		assertThat(handler.getImplicitProfile(),
		    equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-immunization"));
	}
	
	@Test
	public void canHandle_shouldAlwaysReturnTrue() {
		assertTrue(handler.canHandle(new Immunization()));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptAnySearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	// ---- search ----
	
	@Test
	public void search_shouldRestrictSearchToImmunizationGroupingConcept() {
		stubGroupingConcept();
		
		handler.search(new SearchParameterMap());
		
		List<PropParam<TokenAndListParam>> codedParams = capturedSearchParams().getParameters(CODED_SEARCH_HANDLER);
		
		assertThat(codedParams, hasSize(1));
		TokenAndListParam conceptParam = (TokenAndListParam) codedParams.get(0).getParam();
		assertThat(conceptParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(GROUPING_CONCEPT_ID.toString()));
	}
	
	@Test
	public void search_shouldPreserveIncomingSearchParameters() {
		stubGroupingConcept();
		
		ReferenceAndListParam patientParam = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam("identifier", PATIENT_IDENTIFIER)));
		
		handler.search(new SearchParameterMap().addParameter(PATIENT_REFERENCE_SEARCH_HANDLER, patientParam));
		
		assertThat(capturedSearchParams().getParameters(PATIENT_REFERENCE_SEARCH_HANDLER), hasSize(1));
	}
	
	/**
	 * The orchestrator hands one map to every handler in a fan-out search, so adding the grouping
	 * concept must not leak into the caller's instance.
	 */
	@Test
	public void search_shouldNotMutateTheSuppliedSearchParameterMap() {
		stubGroupingConcept();
		
		SearchParameterMap theParams = new SearchParameterMap();
		
		handler.search(theParams);
		
		assertThat(theParams.getParameters(CODED_SEARCH_HANDLER), hasSize(0));
		assertThat(theParams.getParameters(), hasSize(0));
	}
	
	@Test
	public void search_shouldNotAlterSortSpecOrPagingOfTheCopy() {
		stubGroupingConcept();
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.setFromIndex(5);
		theParams.setToIndex(25);
		
		handler.search(theParams);
		
		SearchParameterMap captured = capturedSearchParams();
		assertThat(captured.getFromIndex(), equalTo(5));
		assertThat(captured.getToIndex(), equalTo(25));
	}
	
	@Test
	public void search_shouldCopyParametersCarryingAPropertyName() {
		stubGroupingConcept();
		
		handler.search(new SearchParameterMap().addParameter("some.handler", "somePropertyName", "aValue"));
		
		List<PropParam<Object>> copied = capturedSearchParams().getParameters("some.handler");
		
		assertThat(copied, hasSize(1));
		assertThat(copied.get(0).getPropertyName(), equalTo("somePropertyName"));
		assertThat(copied.get(0).getParam(), equalTo("aValue"));
	}
	
	@Test
	public void search_shouldDelegateToSearchQueryWithBackingDaoAndTranslator() {
		stubGroupingConcept();
		
		handler.search(new SearchParameterMap());
		
		verify(searchQuery).getQueryResults(any(), eq(dao), eq(translator), eq(searchQueryInclude));
	}
}
