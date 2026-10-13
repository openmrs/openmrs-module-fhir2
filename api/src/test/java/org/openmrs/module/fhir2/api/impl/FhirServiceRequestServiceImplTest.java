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
import static org.hl7.fhir.r4.model.Patient.SP_GIVEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

/**
 * Orchestrator-level tests for {@link FhirServiceRequestServiceImpl}. Dispatch mechanics are
 * covered in {@link BaseCompositeFhirServiceTest}; backing-specific read/search lives in
 * {@code TestOrderBackedServiceRequestHandlerTest}. ServiceRequest is read/search at the provider,
 * so this class only covers that {@code searchForServiceRequests} forwards through
 * {@code doSearch}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FhirServiceRequestServiceImplTest {
	
	private static final String PATIENT_GIVEN_NAME = "Meantex";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 100;
	
	@Mock
	private FhirResourceHandler<ServiceRequest> handler;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirServiceRequestServiceImpl service;
	
	@Before
	public void setUp() {
		lenient().when(handler.getImplicitProfile())
		        .thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-servicerequest");
		lenient().when(handler.acceptsSearch(any())).thenReturn(true);
		
		service = new FhirServiceRequestServiceImpl();
		service.setHandlers(Collections.singletonList(handler));
		service.setGlobalPropertyService(globalPropertyService);
	}
	
	@Test
	public void searchForServiceRequests_shouldFanOutAndReturnHandlerResults() {
		when(handler.search(any())).thenReturn(bundleOf(1));
		
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(SP_GIVEN)));
		
		IBundleProvider results = service.searchForServiceRequests(patientReference, null, null, null, null, null, null,
		    null);
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(1));
		verify(handler).search(any());
	}
	
	private static IBundleProvider bundleOf(int n) {
		List<ServiceRequest> rows = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			rows.add(new ServiceRequest());
		}
		return new MockIBundleProvider<>(rows, 10, 1);
	}
}
