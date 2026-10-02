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
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

/**
 * Orchestrator-level tests for {@link FhirValueSetServiceImpl}. Dispatch mechanics are covered in
 * {@link BaseCompositeFhirServiceTest}; backing-specific read/search lives in
 * {@code ConceptBackedValueSetHandlerTest}. ValueSet is read-only at the provider ({@code @Read} +
 * {@code @Search}), so this class only covers that {@code searchForValueSets} forwards through
 * {@code doSearch}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FhirValueSetServiceImplTest {
	
	private static final String ROOT_CONCEPT_NAME = "FOOD CONSTRUCT";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 100;
	
	@Mock
	private FhirResourceHandler<ValueSet> handler;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirValueSetServiceImpl service;
	
	@Before
	public void setup() {
		lenient().when(handler.getImplicitProfile())
		        .thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-valueset");
		lenient().when(handler.acceptsSearch(any())).thenReturn(true);
		
		service = new FhirValueSetServiceImpl();
		service.setHandlers(Collections.singletonList(handler));
		service.setGlobalPropertyService(globalPropertyService);
	}
	
	@Test
	public void searchForValueSets_shouldFanOutAndReturnHandlerResults() {
		when(handler.search(any())).thenReturn(bundleOf(1));
		
		StringAndListParam title = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(ROOT_CONCEPT_NAME)));
		IBundleProvider results = service.searchForValueSets(title);
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(1));
		verify(handler).search(any());
	}
	
	private static IBundleProvider bundleOf(int n) {
		List<ValueSet> rows = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			rows.add(new ValueSet());
		}
		return new MockIBundleProvider<>(rows, 10, 1);
	}
}
