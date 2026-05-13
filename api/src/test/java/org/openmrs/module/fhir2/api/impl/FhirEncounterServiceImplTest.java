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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
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
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.search.param.EncounterSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.providers.r3.MockIBundleProvider;

/**
 * Orchestrator-level tests for {@link FhirEncounterServiceImpl}. Dispatch mechanics (probe-by-uuid,
 * profile/canHandle routing, fan-out merge) are covered in {@link BaseCompositeFhirServiceTest},
 * and encounter/visit-specific CRUD lives in the handler tests under
 * {@code o.o.m.fhir2.api.handler}. What's left for this class is the encounter-specific
 * orchestration: that {@code searchForEncounters} forwards the {@code SearchParameterMap} through
 * {@code doSearch} and that {@code getEncounterEverything} builds the right
 * {@code SearchParameterMap}. Tag-based routing is the handlers' own concern (their
 * {@code acceptsSearch}); these tests simulate that with per-test mocks rather than re-testing
 * handler logic.
 */
@RunWith(MockitoJUnitRunner.class)
public class FhirEncounterServiceImplTest {
	
	private static final String ENCOUNTER_UUID = "344kk343-45hj45-34jk34-34ui33";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 100;
	
	@Mock
	private FhirResourceHandler<Encounter> encounterHandler;
	
	@Mock
	private FhirResourceHandler<Encounter> visitHandler;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirEncounterServiceImpl service;
	
	@Before
	public void setUp() {
		lenient().when(encounterHandler.getImplicitProfile())
		        .thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-encounter");
		lenient().when(visitHandler.getImplicitProfile())
		        .thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-visit");
		lenient().when(encounterHandler.getBackingKey()).thenReturn("openmrs.encounter");
		lenient().when(visitHandler.getBackingKey()).thenReturn("openmrs.visit");
		// Each handler's acceptsSearch decides participation based on the tag in params (mirrors
		// what the real handlers do — orchestrator just calls acceptsSearch and respects it).
		lenient().when(encounterHandler.acceptsSearch(any()))
		        .thenAnswer(inv -> participatesByTag(inv.getArgument(0), "encounter"));
		lenient().when(visitHandler.acceptsSearch(any())).thenAnswer(inv -> participatesByTag(inv.getArgument(0), "visit"));
		
		service = new FhirEncounterServiceImpl();
		service.setHandlers(Arrays.asList(encounterHandler, visitHandler));
		service.setGlobalPropertyService(globalPropertyService);
	}
	
	/**
	 * Mimics handler-side acceptsSearch — opt-in if no tag, opt-out if a routing-system tag specifies a
	 * different code.
	 */
	private static boolean participatesByTag(SearchParameterMap params, String myCode) {
		TokenAndListParam tag = (TokenAndListParam) params.getParameters().stream()
		        .filter(e -> FhirConstants.TAG_SEARCH_HANDLER.equals(e.getKey())).flatMap(e -> e.getValue().stream())
		        .map(p -> p.getParam()).filter(v -> v instanceof TokenAndListParam).findFirst().orElse(null);
		if (tag == null || tag.size() == 0) {
			return true;
		}
		for (Object orListObj : tag.getValuesAsQueryTokens()) {
			ca.uhn.fhir.rest.param.TokenOrListParam orList = (ca.uhn.fhir.rest.param.TokenOrListParam) orListObj;
			boolean systemSeen = false;
			boolean codeMatched = false;
			for (TokenParam token : orList.getValuesAsQueryTokens()) {
				if (FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG.equals(token.getSystem())) {
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
	
	// ---- searchForEncounters: tag-based dispatch ----
	
	@Test
	public void searchForEncounters_shouldFanOutToBothHandlersWhenNoTag() {
		when(encounterHandler.search(any())).thenReturn(bundleOf(9));
		when(visitHandler.search(any())).thenReturn(bundleOf(5));
		
		IBundleProvider results = service.searchForEncounters(new EncounterSearchParams());
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(14));
		verify(encounterHandler).search(any());
		verify(visitHandler).search(any());
	}
	
	@Test
	public void searchForEncounters_shouldRestrictToEncounterHandlerForEncounterTag() {
		when(encounterHandler.search(any())).thenReturn(bundleOf(9));
		
		TokenAndListParam tag = new TokenAndListParam()
		        .addAnd(new TokenParam(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "encounter"));
		EncounterSearchParams params = new EncounterSearchParams();
		params.setTag(tag);
		
		IBundleProvider results = service.searchForEncounters(params);
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(resultList, hasSize(9));
		verify(encounterHandler).search(any());
		verify(visitHandler, never()).search(any());
	}
	
	@Test
	public void searchForEncounters_shouldRestrictToVisitHandlerForVisitTag() {
		when(visitHandler.search(any())).thenReturn(bundleOf(5));
		
		TokenAndListParam tag = new TokenAndListParam()
		        .addAnd(new TokenParam(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "visit"));
		EncounterSearchParams params = new EncounterSearchParams();
		params.setTag(tag);
		
		IBundleProvider results = service.searchForEncounters(params);
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(resultList, hasSize(5));
		verify(visitHandler).search(any());
		verify(encounterHandler, never()).search(any());
	}
	
	@Test
	public void searchForEncounters_shouldFanOutWhenTagOrListMatchesBoth() {
		when(encounterHandler.search(any())).thenReturn(bundleOf(9));
		when(visitHandler.search(any())).thenReturn(bundleOf(5));
		
		TokenAndListParam tag = new TokenAndListParam();
		TokenOrListParam orList = new TokenOrListParam();
		orList.add(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "encounter");
		orList.add(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "visit");
		tag.addAnd(orList);
		
		EncounterSearchParams params = new EncounterSearchParams();
		params.setTag(tag);
		
		IBundleProvider results = service.searchForEncounters(params);
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(resultList, hasSize(14));
	}
	
	// ---- getEncounterEverything ----
	
	@Test
	public void getEncounterEverything_shouldFanOutWithIncludesAndRevIncludes() {
		when(encounterHandler.search(any())).thenReturn(bundleOf(1));
		when(visitHandler.search(any())).thenReturn(bundleOf(0));
		
		TokenParam encounterId = new TokenParam().setValue(ENCOUNTER_UUID);
		IBundleProvider results = service.getEncounterEverything(encounterId);
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(1));
		verify(encounterHandler).search(any());
		verify(visitHandler).search(any());
	}
	
	private static IBundleProvider bundleOf(int n) {
		List<Encounter> encounters = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			encounters.add(new Encounter());
		}
		return new MockIBundleProvider<>(encounters, 10, 1);
	}
}
