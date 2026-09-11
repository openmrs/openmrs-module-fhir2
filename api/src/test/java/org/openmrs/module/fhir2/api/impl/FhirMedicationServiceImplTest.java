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
import org.hl7.fhir.r4.model.Medication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.search.param.MedicationSearchParams;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

/**
 * Orchestrator-level tests for {@link FhirMedicationServiceImpl}. Dispatch mechanics are covered in
 * {@link BaseCompositeFhirServiceTest}; backing-specific CRUD/search lives in
 * {@code DrugBackedMedicationHandlerTest}. What this class covers is that the typed
 * {@code searchForMedications} entry point forwards through {@code doSearch}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FhirMedicationServiceImplTest {
	
	private static final String MEDICATION_UUID = "1359f03d-55d9-4961-b8f8-9a59eddc1f59";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 100;
	
	@Mock
	private FhirResourceHandler<Medication> handler;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirMedicationServiceImpl service;
	
	@Before
	public void setup() {
		lenient().when(handler.getImplicitProfile())
		        .thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-medication");
		lenient().when(handler.acceptsSearch(any())).thenReturn(true);
		
		service = new FhirMedicationServiceImpl();
		service.setHandlers(Collections.singletonList(handler));
		service.setGlobalPropertyService(globalPropertyService);
	}
	
	@Test
	public void searchForMedications_shouldFanOutAndReturnHandlerResults() {
		when(handler.search(any())).thenReturn(bundleOf(2));
		
		IBundleProvider results = service
		        .searchForMedications(new MedicationSearchParams(null, null, null, null, null, null));
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(2));
		verify(handler).search(any());
	}
	
	@Test
	public void create_shouldDispatchToHandler() {
		Medication input = new Medication();
		Medication created = new Medication();
		created.setId(MEDICATION_UUID);
		when(handler.canHandle(input)).thenReturn(true);
		when(handler.create(input)).thenReturn(created);
		
		Medication result = service.create(input);
		
		assertThat(result, notNullValue());
		verify(handler).create(input);
	}
	
	@Test
	public void update_shouldDispatchToHandler() {
		Medication input = new Medication();
		input.setId(MEDICATION_UUID);
		Medication updated = new Medication();
		updated.setId(MEDICATION_UUID);
		when(handler.exists(MEDICATION_UUID)).thenReturn(true);
		when(handler.update(MEDICATION_UUID, input, null, false)).thenReturn(updated);
		
		Medication result = service.update(MEDICATION_UUID, input);
		
		assertThat(result, notNullValue());
		verify(handler).update(MEDICATION_UUID, input, null, false);
	}
	
	private static IBundleProvider bundleOf(int n) {
		List<Medication> rows = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			rows.add(new Medication());
		}
		return new MockIBundleProvider<>(rows, 10, 1);
	}
}
