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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.search.param.ConditionSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.providers.r3.MockIBundleProvider;

/**
 * Orchestrator-level tests for {@link FhirConditionServiceImpl}. Dispatch mechanics (probe-by-uuid,
 * profile/canHandle routing, fan-out merge) are covered in {@link BaseCompositeFhirServiceTest};
 * backing-specific CRUD lives in {@code FhirOpenmrsConditionServiceImplTest} and
 * {@code FhirDiagnosisServiceImplTest}. Category-based search routing is the handlers' own concern
 * (their {@code acceptsSearch}); these tests simulate that with per-test mocks rather than
 * re-testing handler logic.
 */
@RunWith(MockitoJUnitRunner.class)
public class FhirConditionServiceImplTest {
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 100;
	
	@Mock
	private FhirResourceHandler<Condition> conditionHandler;
	
	@Mock
	private FhirResourceHandler<Condition> diagnosisHandler;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirConditionServiceImpl service;
	
	@Before
	public void setUp() {
		lenient().when(conditionHandler.getImplicitProfile())
		        .thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-condition");
		lenient().when(diagnosisHandler.getImplicitProfile())
		        .thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-diagnosis");
		lenient().when(conditionHandler.getBackingKey()).thenReturn("openmrs.condition");
		lenient().when(diagnosisHandler.getBackingKey()).thenReturn("openmrs.diagnosis");
		// Mirror handler acceptsSearch — opt-in unless category names the other side.
		lenient().when(conditionHandler.acceptsSearch(any())).thenAnswer(
		    inv -> participatesByCategory(inv.getArgument(0), FhirConstants.CONDITION_CATEGORY_CODE_CONDITION));
		lenient().when(diagnosisHandler.acceptsSearch(any())).thenAnswer(
		    inv -> participatesByCategory(inv.getArgument(0), FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS));
		
		service = new FhirConditionServiceImpl();
		service.setHandlers(Arrays.asList(conditionHandler, diagnosisHandler));
		service.setGlobalPropertyService(globalPropertyService);
	}
	
	private static boolean participatesByCategory(SearchParameterMap params, String myCode) {
		TokenAndListParam category = (TokenAndListParam) params.getParameters().stream()
		        .filter(e -> FhirConstants.CATEGORY_SEARCH_HANDLER.equals(e.getKey())).flatMap(e -> e.getValue().stream())
		        .map(p -> p.getParam()).filter(v -> v instanceof TokenAndListParam).findFirst().orElse(null);
		if (category == null || category.size() == 0) {
			return true;
		}
		for (TokenOrListParam orList : category.getValuesAsQueryTokens()) {
			boolean systemSeen = false;
			boolean codeMatched = false;
			for (TokenParam token : orList.getValuesAsQueryTokens()) {
				if (FhirConstants.CONDITION_CATEGORY_SYSTEM_URI.equals(token.getSystem())) {
					systemSeen = true;
					if (myCode.equals(token.getValue())) {
						codeMatched = true;
						break;
					}
				}
			}
			if (systemSeen && !codeMatched) {
				return false;
			}
		}
		return true;
	}
	
	// ---- create: orchestrator-level category pre-validation ----
	
	@Test
	public void create_shouldRejectConditionWithUnknownCategoryCoding() {
		Condition condition = new Condition();
		// Coding present in the official condition-category system but with an unrecognised code.
		CodeableConcept cc = new CodeableConcept();
		cc.addCoding(new Coding(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, "not-a-real-code", null));
		condition.addCategory(cc);
		
		assertThrows(InvalidRequestException.class, () -> service.create(condition));
	}
	
	@Test
	public void create_shouldRejectConditionWithCategoryInUnknownSystem() {
		Condition condition = new Condition();
		CodeableConcept cc = new CodeableConcept();
		cc.addCoding(new Coding("http://example.org/some-system", "problem-list-item", null));
		condition.addCategory(cc);
		
		assertThrows(InvalidRequestException.class, () -> service.create(condition));
	}
	
	@Test
	public void create_shouldDispatchToConditionHandlerForKnownCondition() {
		Condition condition = newConditionWithCategory(FhirConstants.CONDITION_CATEGORY_CODE_CONDITION);
		when(conditionHandler.canHandle(condition)).thenReturn(true);
		when(conditionHandler.create(condition)).thenReturn(condition);
		
		service.create(condition);
		verify(conditionHandler).create(condition);
	}
	
	// ---- searchConditions: fan-out + category-based routing ----
	
	@Test
	public void searchConditions_shouldFanOutToBothHandlersWhenNoCategory() {
		when(conditionHandler.search(any())).thenReturn(bundleOf(3));
		when(diagnosisHandler.search(any())).thenReturn(bundleOf(2));
		
		IBundleProvider results = service.searchConditions(new ConditionSearchParams());
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(5));
		verify(conditionHandler).search(any());
		verify(diagnosisHandler).search(any());
	}
	
	@Test
	public void searchConditions_shouldRestrictToConditionHandlerForProblemListCategory() {
		when(conditionHandler.search(any())).thenReturn(bundleOf(3));
		
		ConditionSearchParams params = ConditionSearchParams.builder().category(new TokenAndListParam().addAnd(
		    new TokenParam(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_CONDITION)))
		        .build();
		
		IBundleProvider results = service.searchConditions(params);
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(resultList, hasSize(3));
		verify(conditionHandler).search(any());
		verify(diagnosisHandler, never()).search(any());
	}
	
	@Test
	public void searchConditions_shouldRestrictToDiagnosisHandlerForEncounterDiagnosisCategory() {
		when(diagnosisHandler.search(any())).thenReturn(bundleOf(2));
		
		ConditionSearchParams params = ConditionSearchParams.builder().category(new TokenAndListParam().addAnd(
		    new TokenParam(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS)))
		        .build();
		
		IBundleProvider results = service.searchConditions(params);
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(resultList, hasSize(2));
		verify(diagnosisHandler).search(any());
		verify(conditionHandler, never()).search(any());
	}
	
	@Test
	public void searchConditions_shouldFanOutWhenCategoryOrListMatchesBoth() {
		when(conditionHandler.search(any())).thenReturn(bundleOf(3));
		when(diagnosisHandler.search(any())).thenReturn(bundleOf(2));
		
		TokenOrListParam orList = new TokenOrListParam();
		orList.add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_CONDITION);
		orList.add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS);
		
		ConditionSearchParams params = ConditionSearchParams.builder().category(new TokenAndListParam().addAnd(orList))
		        .build();
		
		IBundleProvider results = service.searchConditions(params);
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(resultList, hasSize(5));
	}
	
	private static Condition newConditionWithCategory(String code) {
		Condition condition = new Condition();
		CodeableConcept cc = new CodeableConcept();
		cc.addCoding(new Coding(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, code, null));
		condition.addCategory(cc);
		return condition;
	}
	
	private static IBundleProvider bundleOf(int n) {
		List<Condition> rows = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			rows.add(new Condition());
		}
		return new MockIBundleProvider<>(rows, 10, 1);
	}
}
