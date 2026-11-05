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
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirOpenmrsConditionServiceImplTest {
	
	private static final String CONDITION_UUID = "43578769-f1a4-46af-b08b-d9fe8a07066f";
	
	private static final String WRONG_CONDITION_UUID = "Wrong uuid";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirConditionDao dao;
	
	@Mock
	private ConditionTranslator<org.openmrs.Condition> translator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<Condition> searchQueryInclude;
	
	@Mock
	private SearchQuery<org.openmrs.Condition, Condition, FhirConditionDao, ConditionTranslator<org.openmrs.Condition>, SearchQueryInclude<Condition>> searchQuery;
	
	private FhirOpenmrsConditionServiceImpl service;
	
	private org.openmrs.Condition openmrsCondition;
	
	private Condition fhirCondition;
	
	@Before
	public void setUp() {
		service = new FhirOpenmrsConditionServiceImpl() {
			
			@Override
			protected void validateObject(org.openmrs.Condition object) {
			}
		};
		service.setDao(dao);
		service.setTranslator(translator);
		service.setSearchQuery(searchQuery);
		service.setSearchQueryInclude(searchQueryInclude);
		
		openmrsCondition = new org.openmrs.Condition();
		openmrsCondition.setUuid(CONDITION_UUID);
		
		fhirCondition = new Condition();
		fhirCondition.setId(CONDITION_UUID);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	// ---- get ----
	
	@Test
	public void get_shouldReturnConditionWhenDaoFindsIt() {
		when(dao.get(CONDITION_UUID)).thenReturn(openmrsCondition);
		when(translator.toFhirResource(openmrsCondition)).thenReturn(fhirCondition);
		
		Condition result = service.get(CONDITION_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(CONDITION_UUID));
	}
	
	// ---- create ----
	
	@Test
	public void create_shouldCreateCondition() {
		when(translator.toOpenmrsType(fhirCondition)).thenReturn(openmrsCondition);
		when(dao.createOrUpdate(openmrsCondition)).thenReturn(openmrsCondition);
		when(translator.toFhirResource(openmrsCondition)).thenReturn(fhirCondition);
		
		Condition result = service.create(fhirCondition);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(CONDITION_UUID));
	}
	
	// ---- update ----
	
	@Test
	public void update_shouldUpdateCondition() {
		when(translator.toOpenmrsType(openmrsCondition, fhirCondition)).thenReturn(openmrsCondition);
		when(dao.createOrUpdate(openmrsCondition)).thenReturn(openmrsCondition);
		when(dao.get(CONDITION_UUID)).thenReturn(openmrsCondition);
		when(translator.toFhirResource(openmrsCondition)).thenReturn(fhirCondition);
		
		Condition result = service.update(CONDITION_UUID, fhirCondition);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(CONDITION_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void update_shouldThrowWhenIdMismatch() {
		service.update(WRONG_CONDITION_UUID, fhirCondition);
	}
	
	// ---- delete ----
	
	@Test
	public void delete_shouldDeleteCondition() {
		when(dao.delete(CONDITION_UUID)).thenReturn(openmrsCondition);
		
		service.delete(CONDITION_UUID);
	}
	
	// ---- search ----
	
	@Test
	public void searchForConditions_shouldReturnConditionsByPatient() {
		ReferenceAndListParam patient = new ReferenceAndListParam();
		patient.addValue(new ReferenceOrListParam().add(new ReferenceParam(Patient.SP_GIVEN, "Jane")));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patient);
		
		when(dao.getSearchResults(any())).thenReturn(singletonList(openmrsCondition));
		lenient().when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(translator.toFhirResource(openmrsCondition)).thenReturn(fhirCondition);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = service.searchForConditions(theParams);
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(((Condition) resultList.iterator().next()).getId(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void searchForConditions_shouldReturnEmptyCollectionWhenDaoReturnsEmpty() {
		SearchParameterMap theParams = new SearchParameterMap();
		
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = service.searchForConditions(theParams);
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
}
