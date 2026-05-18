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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirOpenmrsConditionService;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

/**
 * Tests the dispatch hooks and delegation behaviour. CRUD and search logic live in
 * {@code FhirOpenmrsConditionServiceImpl} and are tested in
 * {@code FhirOpenmrsConditionServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConditionBackedConditionHandlerTest {
	
	private static final String CONDITION_UUID = "43578769-f1a4-46af-b08b-d9fe8a07066f";
	
	@Mock
	private FhirOpenmrsConditionService conditionService;
	
	private ConditionBackedConditionHandler handler;
	
	private Condition fhirCondition;
	
	@Before
	public void setUp() {
		handler = new ConditionBackedConditionHandler();
		handler.setConditionService(conditionService);
		
		fhirCondition = new Condition();
		fhirCondition.setId(CONDITION_UUID);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeConditionImplicitProfile() {
		assertThat(handler.getImplicitProfile(), equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-condition"));
	}
	
	@Test
	public void shouldExposeConditionBackingKey() {
		assertThat(handler.getBackingKey(), equalTo("openmrs.condition"));
	}
	
	@Test
	public void canHandle_shouldReturnTrueForProblemListCategory() {
		Condition condition = withCategory(FhirConstants.CONDITION_CATEGORY_CODE_CONDITION);
		assertTrue(handler.canHandle(condition));
	}
	
	@Test
	public void canHandle_shouldReturnTrueWhenNoCategoryProvided() {
		// FhirUtils.getOpenmrsConditionType defaults to CONDITION when category absent — this handler
		// is the default backing.
		assertTrue(handler.canHandle(new Condition()));
	}
	
	@Test
	public void canHandle_shouldReturnFalseForEncounterDiagnosisCategory() {
		Condition condition = withCategory(FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS);
		assertFalse(handler.canHandle(condition));
	}
	
	@Test
	public void canHandle_shouldReturnFalseForCategoryInUnknownSystem() {
		Condition condition = new Condition();
		CodeableConcept cc = new CodeableConcept();
		cc.addCoding(new Coding("http://example.org/some-system", "problem-list-item", null));
		condition.addCategory(cc);
		assertFalse(handler.canHandle(condition));
	}
	
	// ---- acceptsSearch (category-aware opt-out) ----
	
	@Test
	public void acceptsSearch_shouldAcceptUntaggedSearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptProblemListCategory() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		            FhirConstants.CONDITION_CATEGORY_CODE_CONDITION)));
		assertTrue(handler.acceptsSearch(params));
	}
	
	@Test
	public void acceptsSearch_shouldOptOutOnEncounterDiagnosisCategory() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		            FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS)));
		assertFalse(handler.acceptsSearch(params));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptOrListContainingProblemListCode() {
		TokenOrListParam orList = new TokenOrListParam();
		orList.add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_CONDITION);
		orList.add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS);
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(orList));
		
		assertTrue(handler.acceptsSearch(params));
	}
	
	@Test
	public void acceptsSearch_shouldIgnoreCategoryInUnrelatedSystems() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam("http://example.org/some-other-system", "value")));
		assertTrue(handler.acceptsSearch(params));
	}
	
	// ---- delegation ----
	
	@Test
	public void get_shouldDelegateToConditionService() {
		when(conditionService.get(CONDITION_UUID)).thenReturn(fhirCondition);
		
		Condition result = handler.get(CONDITION_UUID);
		
		assertThat(result, sameInstance(fhirCondition));
		verify(conditionService).get(CONDITION_UUID);
	}
	
	@Test
	public void exists_shouldDelegateToConditionServiceExists() {
		when(conditionService.exists(CONDITION_UUID)).thenReturn(true);
		
		assertTrue(handler.exists(CONDITION_UUID));
		verify(conditionService).exists(CONDITION_UUID);
		verify(conditionService, never()).get(CONDITION_UUID);
	}
	
	@Test
	public void create_shouldDelegateToConditionService() {
		when(conditionService.create(fhirCondition)).thenReturn(fhirCondition);
		
		Condition result = handler.create(fhirCondition);
		
		assertThat(result, sameInstance(fhirCondition));
		verify(conditionService).create(fhirCondition);
	}
	
	@Test
	public void update_shouldDelegateToConditionService() {
		when(conditionService.update(CONDITION_UUID, fhirCondition)).thenReturn(fhirCondition);
		
		Condition result = handler.update(CONDITION_UUID, fhirCondition);
		
		assertThat(result, sameInstance(fhirCondition));
		verify(conditionService).update(CONDITION_UUID, fhirCondition);
	}
	
	@Test
	public void delete_shouldDelegateToConditionService() {
		handler.delete(CONDITION_UUID);
		verify(conditionService).delete(CONDITION_UUID);
	}
	
	@Test
	public void search_shouldDelegateToConditionServiceSearchForConditions() {
		IBundleProvider expected = new SimpleBundleProvider();
		SearchParameterMap params = new SearchParameterMap();
		when(conditionService.searchForConditions(params)).thenReturn(expected);
		
		IBundleProvider result = handler.search(params);
		
		assertThat(result, is(expected));
		verify(conditionService).searchForConditions(params);
	}
	
	private static Condition withCategory(String code) {
		Condition condition = new Condition();
		CodeableConcept cc = new CodeableConcept();
		cc.addCoding(new Coding(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, code, null));
		condition.addCategory(cc);
		return condition;
	}
}
