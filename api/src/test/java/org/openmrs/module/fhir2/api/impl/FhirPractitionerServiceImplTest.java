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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.search.param.PractitionerSearchParams;
import org.openmrs.module.fhir2.providers.r3.MockIBundleProvider;

/**
 * Orchestrator-level tests for {@link FhirPractitionerServiceImpl}. Dispatch mechanics are covered
 * in {@link BaseCompositeFhirServiceTest}; provider/user CRUD + search lives in
 * {@code FhirOpenmrsProviderServiceImplTest} and the handler-side tests under
 * {@code o.o.m.fhir2.api.handler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FhirPractitionerServiceImplTest {
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 100;
	
	@Mock
	private FhirResourceHandler<Practitioner> providerHandler;
	
	@Mock
	private FhirResourceHandler<Practitioner> userHandler;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirPractitionerServiceImpl service;
	
	@Before
	public void setUp() {
		lenient().when(providerHandler.getImplicitProfile())
		        .thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-provider");
		lenient().when(userHandler.getImplicitProfile())
		        .thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-user");
		lenient().when(providerHandler.getBackingKey()).thenReturn("openmrs.provider");
		lenient().when(userHandler.getBackingKey()).thenReturn("openmrs.user");
		lenient().when(providerHandler.acceptsSearch(any())).thenReturn(true);
		lenient().when(userHandler.acceptsSearch(any())).thenReturn(true);
		lenient().when(providerHandler.canHandle(any())).thenReturn(true);
		lenient().when(userHandler.canHandle(any())).thenReturn(false);
		
		service = new FhirPractitionerServiceImpl();
		service.setHandlers(Arrays.asList(providerHandler, userHandler));
		service.setGlobalPropertyService(globalPropertyService);
	}
	
	// ---- create: orchestrator-level "must have identifier" pre-validation ----
	
	@Test
	public void create_shouldRejectPractitionerWithoutIdentifier() {
		Practitioner withoutId = new Practitioner();
		assertThrows(UnprocessableEntityException.class, () -> service.create(withoutId));
	}
	
	@Test
	public void create_shouldDispatchToProviderHandlerWhenIdentifierPresent() {
		Practitioner withId = new Practitioner();
		withId.addIdentifier(new Identifier().setValue("X"));
		when(providerHandler.create(withId)).thenReturn(withId);
		
		service.create(withId);
		
		verify(providerHandler).create(withId);
	}
	
	// ---- searchForPractitioners: fan-out ----
	
	@Test
	public void searchForPractitioners_shouldFanOutToBothHandlers() {
		when(providerHandler.search(any())).thenReturn(bundleOf(3));
		when(userHandler.search(any())).thenReturn(bundleOf(2));
		
		IBundleProvider results = service.searchForPractitioners(new PractitionerSearchParams());
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(5));
		verify(providerHandler).search(any());
		verify(userHandler).search(any());
	}
	
	private static IBundleProvider bundleOf(int n) {
		List<Practitioner> rows = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			rows.add(new Practitioner());
		}
		return new MockIBundleProvider<>(rows, 10, 1);
	}
}
