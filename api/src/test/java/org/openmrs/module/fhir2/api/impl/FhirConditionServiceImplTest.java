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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
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
import org.openmrs.module.fhir2.api.handler.ConditionBackedConditionHandler;
import org.openmrs.module.fhir2.api.handler.DiagnosisBackedConditionHandler;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.search.param.ConditionSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;

@RunWith(MockitoJUnitRunner.class)
public class FhirConditionServiceImplTest {
	
	private static final String CONDITION_UUID = "a7b6c5d4-1234-4f5e-9a8b-0c1d2e3f4a5b";
	
	private static final String DIAGNOSIS_PROFILE = "http://fhir.openmrs.org/StructureDefinition/openmrs-diagnosis";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 100;
	
	@Mock(extraInterfaces = Ordered.class)
	private FhirResourceHandler<Condition> conditionHandler;
	
	@Mock(extraInterfaces = Ordered.class)
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
		// The real handlers' precedence decides a create that both claim.
		lenient().when(((Ordered) conditionHandler).getOrder())
		        .thenReturn(OrderUtils.getOrder(ConditionBackedConditionHandler.class));
		lenient().when(((Ordered) diagnosisHandler).getOrder())
		        .thenReturn(OrderUtils.getOrder(DiagnosisBackedConditionHandler.class));
		// Mirrors the handlers' acceptsSearch: participate unless the category names the other side.
		lenient().when(conditionHandler.acceptsSearch(any())).thenAnswer(
		    inv -> participatesByCategory(inv.getArgument(0), FhirConstants.CONDITION_CATEGORY_CODE_CONDITION));
		lenient().when(diagnosisHandler.acceptsSearch(any())).thenAnswer(
		    inv -> participatesByCategory(inv.getArgument(0), FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS));
		// Mirrors the handlers' canHandle: each looks only for its own category code, and the condition
		// backing additionally owns the unmarked case. A body carrying both is therefore claimed by
		// both, and precedence decides between them.
		lenient().when(conditionHandler.canHandle(any())).thenAnswer(inv -> {
			Condition condition = inv.getArgument(0);
			return !hasAnyCategoryCoding(condition)
			        || hasCategoryCode(condition, FhirConstants.CONDITION_CATEGORY_CODE_CONDITION);
		});
		lenient().when(diagnosisHandler.canHandle(any()))
		        .thenAnswer(inv -> hasCategoryCode(inv.getArgument(0), FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS));
		
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
	
	private static boolean hasAnyCategoryCoding(Condition condition) {
		return condition.getCategory().stream().anyMatch(CodeableConcept::hasCoding);
	}
	
	private static boolean hasCategoryCode(Condition condition, String code) {
		return condition.getCategory().stream().flatMap(category -> category.getCoding().stream())
		        .anyMatch(coding -> FhirConstants.CONDITION_CATEGORY_SYSTEM_URI.equals(coding.getSystem())
		                && code.equals(coding.getCode()));
	}
	
	// ---- create: category-based dispatch ----
	//
	// The rejections below come from BaseCompositeFhirService, not from any Condition-specific
	// pre-check: a category naming neither backing is claimed by neither handler.
	
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
	
	/**
	 * Both backings claim a body carrying both categories, and the condition backing has precedence.
	 */
	@Test
	public void create_shouldDispatchConditionCarryingBothCategoriesToConditionHandler() {
		Condition condition = newConditionWithCategory(FhirConstants.CONDITION_CATEGORY_CODE_CONDITION);
		CodeableConcept alsoDiagnosis = new CodeableConcept();
		alsoDiagnosis.addCoding(
		    new Coding(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS, null));
		condition.addCategory(alsoDiagnosis);
		when(conditionHandler.create(condition)).thenReturn(condition);
		
		service.create(condition);
		
		verify(conditionHandler).create(condition);
		verify(diagnosisHandler, never()).create(any());
	}
	
	@Test
	public void create_shouldDispatchToConditionHandlerForKnownCondition() {
		Condition condition = newConditionWithCategory(FhirConstants.CONDITION_CATEGORY_CODE_CONDITION);
		when(conditionHandler.create(condition)).thenReturn(condition);
		
		service.create(condition);
		
		verify(conditionHandler).create(condition);
		verify(diagnosisHandler, never()).create(any());
	}
	
	@Test
	public void create_shouldDispatchToDiagnosisHandlerForEncounterDiagnosis() {
		Condition condition = newConditionWithCategory(FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS);
		when(diagnosisHandler.create(condition)).thenReturn(condition);
		
		service.create(condition);
		
		verify(diagnosisHandler).create(condition);
		verify(conditionHandler, never()).create(any());
	}
	
	@Test
	public void create_shouldDispatchToConditionHandlerWhenNoCategoryProvided() {
		Condition condition = new Condition();
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
	
	// ---- get / update / patch / delete: ownership by uuid ----
	
	@Test
	public void get_shouldFallThroughToDiagnosisHandlerWhenConditionHandlerDoesNotKnowTheUuid() {
		// given
		Condition diagnosis = new Condition();
		diagnosis.setId(CONDITION_UUID);
		when(conditionHandler.get(CONDITION_UUID)).thenThrow(new ResourceNotFoundException("not a condition"));
		when(diagnosisHandler.get(CONDITION_UUID)).thenReturn(diagnosis);
		
		// when
		Condition result = service.get(CONDITION_UUID);
		
		// then
		assertThat(result, sameInstance(diagnosis));
		assertThat(result.getMeta().getProfile().get(0).getValue(), equalTo(DIAGNOSIS_PROFILE));
	}
	
	@Test
	public void update_shouldRouteToConditionHandlerWhenItOwnsTheUuid() {
		// given
		Condition condition = newConditionWithCategory(FhirConstants.CONDITION_CATEGORY_CODE_CONDITION);
		when(conditionHandler.exists(CONDITION_UUID)).thenReturn(true);
		when(conditionHandler.update(CONDITION_UUID, condition, null, false)).thenReturn(condition);
		
		// when
		Condition result = service.update(CONDITION_UUID, condition);
		
		// then
		assertThat(result, sameInstance(condition));
		verify(diagnosisHandler, never()).update(any(), any(), any(), anyBoolean());
	}
	
	@Test
	public void update_shouldRouteToDiagnosisHandlerWhenItOwnsTheUuid() {
		// given
		Condition condition = newConditionWithCategory(FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS);
		when(conditionHandler.exists(CONDITION_UUID)).thenReturn(false);
		when(diagnosisHandler.exists(CONDITION_UUID)).thenReturn(true);
		when(diagnosisHandler.update(CONDITION_UUID, condition, null, false)).thenReturn(condition);
		
		// when
		Condition result = service.update(CONDITION_UUID, condition);
		
		// then
		assertThat(result, sameInstance(condition));
		verify(conditionHandler, never()).update(any(), any(), any(), anyBoolean());
	}
	
	/**
	 * An uncategorised body would be claimed by the condition backing on create, but an update targets
	 * an existing row, so the backing that already holds the uuid must receive it.
	 */
	@Test
	public void update_shouldRouteByOwnershipRatherThanContent() {
		// given
		Condition uncategorised = new Condition();
		when(conditionHandler.exists(CONDITION_UUID)).thenReturn(false);
		when(diagnosisHandler.exists(CONDITION_UUID)).thenReturn(true);
		when(diagnosisHandler.update(CONDITION_UUID, uncategorised, null, false)).thenReturn(uncategorised);
		
		// when
		Condition result = service.update(CONDITION_UUID, uncategorised);
		
		// then
		assertThat(result, sameInstance(uncategorised));
		verify(conditionHandler, never()).update(any(), any(), any(), anyBoolean());
	}
	
	@Test
	public void patch_shouldRouteToDiagnosisHandlerWhenItOwnsTheUuid() {
		// given
		Condition patched = new Condition();
		when(conditionHandler.exists(CONDITION_UUID)).thenReturn(false);
		when(diagnosisHandler.exists(CONDITION_UUID)).thenReturn(true);
		when(diagnosisHandler.patch(CONDITION_UUID, PatchTypeEnum.JSON_PATCH, "[]", null)).thenReturn(patched);
		
		// when
		Condition result = service.patch(CONDITION_UUID, PatchTypeEnum.JSON_PATCH, "[]", null);
		
		// then
		assertThat(result, sameInstance(patched));
		verify(conditionHandler, never()).patch(any(), any(), any(), any());
	}
	
	@Test
	public void delete_shouldRouteToDiagnosisHandlerWhenItOwnsTheUuid() {
		// given
		when(conditionHandler.exists(CONDITION_UUID)).thenReturn(false);
		when(diagnosisHandler.exists(CONDITION_UUID)).thenReturn(true);
		
		// when
		service.delete(CONDITION_UUID);
		
		// then
		verify(diagnosisHandler).delete(CONDITION_UUID);
		verify(conditionHandler, never()).delete(any());
	}
	
	@Test
	public void update_shouldThrowResourceNotFoundForUnknownUuid() {
		// given
		Condition condition = newConditionWithCategory(FhirConstants.CONDITION_CATEGORY_CODE_CONDITION);
		when(conditionHandler.exists(CONDITION_UUID)).thenReturn(false);
		when(diagnosisHandler.exists(CONDITION_UUID)).thenReturn(false);
		
		// when / then
		assertThrows(ResourceNotFoundException.class, () -> service.update(CONDITION_UUID, condition));
		verify(conditionHandler, never()).update(any(), any(), any(), anyBoolean());
		verify(diagnosisHandler, never()).update(any(), any(), any(), anyBoolean());
	}
	
	@Test
	public void patch_shouldThrowResourceNotFoundForUnknownUuid() {
		// given
		when(conditionHandler.exists(CONDITION_UUID)).thenReturn(false);
		when(diagnosisHandler.exists(CONDITION_UUID)).thenReturn(false);
		
		// when / then
		assertThrows(ResourceNotFoundException.class,
		    () -> service.patch(CONDITION_UUID, PatchTypeEnum.JSON_PATCH, "[]", null));
		verify(conditionHandler, never()).patch(any(), any(), any(), any());
		verify(diagnosisHandler, never()).patch(any(), any(), any(), any());
	}
	
	@Test
	public void delete_shouldThrowResourceNotFoundForUnknownUuid() {
		// given
		when(conditionHandler.exists(CONDITION_UUID)).thenReturn(false);
		when(diagnosisHandler.exists(CONDITION_UUID)).thenReturn(false);
		
		// when / then
		assertThrows(ResourceNotFoundException.class, () -> service.delete(CONDITION_UUID));
		verify(conditionHandler, never()).delete(any());
		verify(diagnosisHandler, never()).delete(any());
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
