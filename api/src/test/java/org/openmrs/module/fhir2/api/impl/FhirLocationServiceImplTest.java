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
import org.hl7.fhir.r4.model.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.search.param.LocationSearchParams;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

/**
 * Orchestrator-level tests for {@link FhirLocationServiceImpl}. Dispatch mechanics are covered in
 * {@link BaseCompositeFhirServiceTest}; backing-specific CRUD/search lives in
 * {@code LocationBackedLocationHandlerTest}. What this class covers is that create/update/search
 * reach the handler through the composite.
 */
@RunWith(MockitoJUnitRunner.class)
public class FhirLocationServiceImplTest {
	
	private static final String LOCATION_UUID = "a1758922-b132-4ead-8ebe-5e2b4eaf43a1";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 100;
	
	@Mock
	private FhirResourceHandler<Location> handler;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirLocationServiceImpl service;
	
	@Before
	public void setup() {
		lenient().when(handler.getImplicitProfile())
		        .thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-location");
		lenient().when(handler.acceptsSearch(any())).thenReturn(true);
		
		service = new FhirLocationServiceImpl();
		service.setHandlers(Collections.singletonList(handler));
		service.setGlobalPropertyService(globalPropertyService);
	}
	
	@Test
	public void searchForLocations_shouldFanOutAndReturnHandlerResults() {
		when(handler.search(any())).thenReturn(bundleOf(1));
		
		IBundleProvider results = service.searchForLocations(
		    new LocationSearchParams(null, null, null, null, null, null, null, null, null, null, null, null));
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(1));
		verify(handler).search(any());
	}
	
	@Test
	public void create_shouldDispatchToHandler() {
		Location input = new Location();
		Location created = new Location();
		created.setId(LOCATION_UUID);
		when(handler.canHandle(input)).thenReturn(true);
		when(handler.create(input)).thenReturn(created);
		
		Location result = service.create(input);
		
		assertThat(result, notNullValue());
		verify(handler).create(input);
	}
	
	@Test
	public void update_shouldDispatchToHandler() {
		Location input = new Location();
		input.setId(LOCATION_UUID);
		Location updated = new Location();
		updated.setId(LOCATION_UUID);
		when(handler.exists(LOCATION_UUID)).thenReturn(true);
		when(handler.update(LOCATION_UUID, input, null, false)).thenReturn(updated);
		
		Location result = service.update(LOCATION_UUID, input);
		
		assertThat(result, notNullValue());
		verify(handler).update(LOCATION_UUID, input, null, false);
	}
	
	private static IBundleProvider bundleOf(int n) {
		List<Location> rows = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			rows.add(new Location());
		}
		return new MockIBundleProvider<>(rows, 10, 1);
	}
}
