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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.NumberParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Observation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.search.param.ObservationSearchParams;
import org.openmrs.module.fhir2.api.search.param.PropParam;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.providers.r3.MockIBundleProvider;

/**
 * Orchestrator-level tests for {@link FhirObservationServiceImpl}. Dispatch mechanics
 * (probe-by-uuid, profile/canHandle routing, fan-out merge) are covered in
 * {@link BaseCompositeFhirServiceTest}; backing-specific CRUD/search lives in
 * {@code ObservationBackedObservationHandlerTest}. What this class covers is the special-method
 * orchestration: that the typed entry points build the right {@link SearchParameterMap} and forward
 * through {@code doSearch}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FhirObservationServiceImplTest {
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirResourceHandler<Observation> observationHandler;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirObservationServiceImpl service;
	
	@Before
	public void setup() {
		lenient().when(observationHandler.getImplicitProfile())
		        .thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-observation");
		lenient().when(observationHandler.getBackingKey()).thenReturn("openmrs.observation");
		lenient().when(observationHandler.acceptsSearch(any())).thenReturn(true);
		
		service = new FhirObservationServiceImpl();
		service.setHandlers(Collections.singletonList(observationHandler));
		service.setGlobalPropertyService(globalPropertyService);
	}
	
	// ---- searchForObservations ----
	
	@Test
	public void searchForObservations_shouldFanOutAndReturnHandlerResults() {
		when(observationHandler.search(any())).thenReturn(bundleOf(3));
		
		ObservationSearchParams params = new ObservationSearchParams();
		IBundleProvider results = service.searchForObservations(params);
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(3));
		verify(observationHandler).search(any());
	}
	
	// ---- getLastnObservations ----
	
	@Test
	public void getLastnObservations_shouldStampLastnHandlerKeyOnSearchParams() {
		when(observationHandler.search(any())).thenReturn(bundleOf(1));
		
		service.getLastnObservations(new NumberParam(2), new ObservationSearchParams());
		
		SearchParameterMap captured = captureSearchParams();
		assertHandlerEntry(captured, FhirConstants.LASTN_OBSERVATION_SEARCH_HANDLER);
		assertHandlerEntry(captured, FhirConstants.MAX_SEARCH_HANDLER);
	}
	
	@Test
	public void getLastnObservations_shouldDefaultMaxToOneWhenNull() {
		when(observationHandler.search(any())).thenReturn(bundleOf(1));
		
		service.getLastnObservations(null, new ObservationSearchParams());
		
		SearchParameterMap captured = captureSearchParams();
		PropParam<?> max = captured.getParameters(FhirConstants.MAX_SEARCH_HANDLER).iterator().next();
		assertThat(((NumberParam) max.getParam()).getValue().intValue(), equalTo(1));
	}
	
	// ---- getLastnEncountersObservations ----
	
	@Test
	public void getLastnEncountersObservations_shouldStampLastnEncountersHandlerKeyOnSearchParams() {
		when(observationHandler.search(any())).thenReturn(bundleOf(1));
		
		service.getLastnEncountersObservations(new NumberParam(3), new ObservationSearchParams());
		
		SearchParameterMap captured = captureSearchParams();
		assertHandlerEntry(captured, FhirConstants.LASTN_ENCOUNTERS_SEARCH_HANDLER);
		assertHandlerEntry(captured, FhirConstants.MAX_SEARCH_HANDLER);
	}
	
	@Test
	public void getLastnEncountersObservations_shouldDefaultMaxToOneWhenNull() {
		when(observationHandler.search(any())).thenReturn(bundleOf(1));
		
		service.getLastnEncountersObservations(null, new ObservationSearchParams());
		
		SearchParameterMap captured = captureSearchParams();
		PropParam<?> max = captured.getParameters(FhirConstants.MAX_SEARCH_HANDLER).iterator().next();
		assertThat(((NumberParam) max.getParam()).getValue().intValue(), equalTo(1));
	}
	
	private SearchParameterMap captureSearchParams() {
		ArgumentCaptor<SearchParameterMap> captor = ArgumentCaptor.forClass(SearchParameterMap.class);
		verify(observationHandler).search(captor.capture());
		return captor.getValue();
	}
	
	private static void assertHandlerEntry(SearchParameterMap params, String key) {
		boolean found = params.getParameters().stream().anyMatch(e -> key.equals(e.getKey()));
		assertThat("expected SearchParameterMap to contain entry for handler key " + key, found, equalTo(true));
	}
	
	private static IBundleProvider bundleOf(int n) {
		List<Observation> rows = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			rows.add(new Observation());
		}
		return new MockIBundleProvider<>(rows, 10, 1);
	}
}
