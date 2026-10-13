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
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.search.param.MedicationRequestSearchParams;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

/**
 * Orchestrator-level tests for {@link FhirMedicationRequestServiceImpl}. Dispatch mechanics are
 * covered in {@link BaseCompositeFhirServiceTest}; backing-specific read/search lives in
 * {@code DrugOrderBackedMedicationRequestHandlerTest}. MedicationRequest is read/search/patch at
 * the provider (no create/update/delete), so this class only covers that
 * {@code searchForMedicationRequests} forwards through {@code doSearch}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FhirMedicationRequestServiceImplTest {
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 100;
	
	@Mock
	private FhirResourceHandler<MedicationRequest> handler;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirMedicationRequestServiceImpl service;
	
	@Before
	public void setup() {
		lenient().when(handler.getImplicitProfile())
		        .thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-medicationrequest");
		lenient().when(handler.acceptsSearch(any())).thenReturn(true);
		
		service = new FhirMedicationRequestServiceImpl();
		service.setHandlers(Collections.singletonList(handler));
		service.setGlobalPropertyService(globalPropertyService);
	}
	
	@Test
	public void searchForMedicationRequests_shouldFanOutAndReturnHandlerResults() {
		when(handler.search(any())).thenReturn(bundleOf(2));
		
		IBundleProvider results = service.searchForMedicationRequests(
		    new MedicationRequestSearchParams(null, null, null, null, null, null, null, null, null, null, null));
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(2));
		verify(handler).search(any());
	}
	
	private static IBundleProvider bundleOf(int n) {
		List<MedicationRequest> rows = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			rows.add(new MedicationRequest());
		}
		return new MockIBundleProvider<>(rows, 10, 1);
	}
}
