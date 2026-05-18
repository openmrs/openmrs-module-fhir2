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
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.openmrs.module.fhir2.api.FhirDiagnosisService;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

@RunWith(MockitoJUnitRunner.class)
public class DiagnosisBackedConditionHandlerTest {
	
	private static final String CONDITION_UUID = "43578769-f1a4-46af-b08b-d9fe8a07066f";
	
	@Mock
	private FhirDiagnosisService diagnosisService;
	
	private DiagnosisBackedConditionHandler handler;
	
	private Condition fhirCondition;
	
	@Before
	public void setUp() {
		handler = new DiagnosisBackedConditionHandler();
		handler.setDiagnosisService(diagnosisService);
		
		fhirCondition = new Condition();
		fhirCondition.setId(CONDITION_UUID);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeDiagnosisImplicitProfile() {
		assertThat(handler.getImplicitProfile(), equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-diagnosis"));
	}
	
	@Test
	public void shouldExposeDiagnosisBackingKey() {
		assertThat(handler.getBackingKey(), equalTo("openmrs.diagnosis"));
	}
	
	@Test
	public void canHandle_shouldReturnTrueForEncounterDiagnosisCategory() {
		Condition condition = withCategory(FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS);
		assertTrue(handler.canHandle(condition));
	}
	
	@Test
	public void canHandle_shouldReturnFalseForProblemListCategory() {
		Condition condition = withCategory(FhirConstants.CONDITION_CATEGORY_CODE_CONDITION);
		assertFalse(handler.canHandle(condition));
	}
	
	@Test
	public void canHandle_shouldReturnFalseWhenNoCategoryProvided() {
		// FhirUtils.getOpenmrsConditionType defaults to CONDITION when category absent — diagnosis
		// handler does not claim those.
		assertFalse(handler.canHandle(new Condition()));
	}
	
	// ---- acceptsSearch (category-aware opt-out) ----
	
	@Test
	public void acceptsSearch_shouldAcceptUntaggedSearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptEncounterDiagnosisCategory() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		            FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS)));
		assertTrue(handler.acceptsSearch(params));
	}
	
	@Test
	public void acceptsSearch_shouldOptOutOnProblemListCategory() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		            FhirConstants.CONDITION_CATEGORY_CODE_CONDITION)));
		assertFalse(handler.acceptsSearch(params));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptOrListContainingDiagnosisCode() {
		TokenOrListParam orList = new TokenOrListParam();
		orList.add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_CONDITION);
		orList.add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS);
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(orList));
		
		assertTrue(handler.acceptsSearch(params));
	}
	
	// ---- delegation ----
	
	@Test
	public void get_shouldDelegateToDiagnosisService() {
		when(diagnosisService.get(CONDITION_UUID)).thenReturn(fhirCondition);
		
		Condition result = handler.get(CONDITION_UUID);
		
		assertThat(result, sameInstance(fhirCondition));
		verify(diagnosisService).get(CONDITION_UUID);
	}
	
	@Test
	public void exists_shouldDelegateToDiagnosisServiceExists() {
		when(diagnosisService.exists(CONDITION_UUID)).thenReturn(true);
		
		assertTrue(handler.exists(CONDITION_UUID));
		verify(diagnosisService).exists(CONDITION_UUID);
		verify(diagnosisService, never()).get(CONDITION_UUID);
	}
	
	@Test
	public void create_shouldDelegateToDiagnosisService() {
		when(diagnosisService.create(fhirCondition)).thenReturn(fhirCondition);
		
		Condition result = handler.create(fhirCondition);
		
		assertThat(result, sameInstance(fhirCondition));
		verify(diagnosisService).create(fhirCondition);
	}
	
	@Test
	public void update_shouldDelegateToDiagnosisService() {
		when(diagnosisService.update(CONDITION_UUID, fhirCondition)).thenReturn(fhirCondition);
		
		Condition result = handler.update(CONDITION_UUID, fhirCondition);
		
		assertThat(result, sameInstance(fhirCondition));
		verify(diagnosisService).update(CONDITION_UUID, fhirCondition);
	}
	
	@Test
	public void delete_shouldDelegateToDiagnosisService() {
		handler.delete(CONDITION_UUID);
		verify(diagnosisService).delete(CONDITION_UUID);
	}
	
	@Test
	public void search_shouldDelegateToDiagnosisServiceSearchDiagnoses() {
		IBundleProvider expected = new SimpleBundleProvider();
		SearchParameterMap params = new SearchParameterMap();
		when(diagnosisService.searchDiagnoses(params)).thenReturn(expected);
		
		IBundleProvider result = handler.search(params);
		
		assertThat(result, is(expected));
		verify(diagnosisService).searchDiagnoses(params);
	}
	
	@Test
	public void acceptsSearch_shouldNotInteractWithDiagnosisService() {
		handler.acceptsSearch(new SearchParameterMap());
		verifyNoInteractions(diagnosisService);
	}
	
	private static Condition withCategory(String code) {
		Condition condition = new Condition();
		CodeableConcept cc = new CodeableConcept();
		cc.addCoding(new Coding(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, code, null));
		condition.addCategory(cc);
		return condition;
	}
}
