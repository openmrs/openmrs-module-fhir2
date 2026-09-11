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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.search.param.FhirAllergyIntoleranceSearchParams;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

/**
 * Orchestrator-level tests for {@link FhirAllergyIntoleranceServiceImpl}. Dispatch mechanics
 * (probe-by-uuid, profile/canHandle routing, fan-out merge) are covered in
 * {@link BaseCompositeFhirServiceTest}; backing-specific CRUD/search lives in
 * {@code AllergyBackedAllergyIntoleranceHandlerTest}. What this class covers is that the typed
 * {@code searchForAllergies} entry point forwards through {@code doSearch}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FhirAllergyIntoleranceServiceImplTest {
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 100;
	
	@Mock
	private FhirResourceHandler<AllergyIntolerance> handler;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirAllergyIntoleranceServiceImpl service;
	
	@Before
	public void setup() {
		lenient().when(handler.getImplicitProfile())
		        .thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-allergyintolerance");
		lenient().when(handler.acceptsSearch(any())).thenReturn(true);
		
		service = new FhirAllergyIntoleranceServiceImpl();
		service.setHandlers(Collections.singletonList(handler));
		service.setGlobalPropertyService(globalPropertyService);
	}
	
	@Test
	public void searchForAllergies_shouldFanOutAndReturnHandlerResults() {
		when(handler.search(any())).thenReturn(bundleOf(2));
		
		IBundleProvider results = service.searchForAllergies(
		    new FhirAllergyIntoleranceSearchParams(null, null, null, null, null, null, null, null, null, null));
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(2));
		verify(handler).search(any());
	}
	
	@Test
	public void create_shouldDispatchToHandler() {
		AllergyIntolerance input = new AllergyIntolerance();
		AllergyIntolerance created = new AllergyIntolerance();
		created.setId(ALLERGY_UUID);
		when(handler.canHandle(input)).thenReturn(true);
		when(handler.create(input)).thenReturn(created);
		
		AllergyIntolerance result = service.create(input);
		
		assertThat(result, notNullValue());
		verify(handler).create(input);
	}
	
	@Test
	public void update_shouldDispatchToHandler() {
		AllergyIntolerance input = new AllergyIntolerance();
		input.setId(ALLERGY_UUID);
		AllergyIntolerance updated = new AllergyIntolerance();
		updated.setId(ALLERGY_UUID);
		when(handler.exists(ALLERGY_UUID)).thenReturn(true);
		when(handler.update(ALLERGY_UUID, input, null, false)).thenReturn(updated);
		
		AllergyIntolerance result = service.update(ALLERGY_UUID, input);
		
		assertThat(result, notNullValue());
		verify(handler).update(ALLERGY_UUID, input, null, false);
	}
	
	private static IBundleProvider bundleOf(int n) {
		List<AllergyIntolerance> rows = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			rows.add(new AllergyIntolerance());
		}
		return new MockIBundleProvider<>(rows, 10, 1);
	}
}
