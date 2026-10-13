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
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.search.param.DiagnosticReportSearchParams;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

/**
 * Orchestrator-level tests for {@link FhirDiagnosticReportServiceImpl}. Dispatch mechanics are
 * covered in {@link BaseCompositeFhirServiceTest}; backing-specific CRUD/search lives in
 * {@code FhirDiagnosticReportBackedDiagnosticReportHandlerTest}. What this class covers is that
 * create/update/search reach the handler through the composite.
 */
@RunWith(MockitoJUnitRunner.class)
public class FhirDiagnosticReportServiceImplTest {
	
	private static final String UUID = "249b9094-b812-4b0c-a204-0052a05c657f";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 100;
	
	@Mock
	private FhirResourceHandler<DiagnosticReport> handler;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirDiagnosticReportServiceImpl service;
	
	@Before
	public void setUp() {
		lenient().when(handler.getImplicitProfile())
		        .thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-diagnosticreport");
		lenient().when(handler.acceptsSearch(any())).thenReturn(true);
		
		service = new FhirDiagnosticReportServiceImpl();
		service.setHandlers(Collections.singletonList(handler));
		service.setGlobalPropertyService(globalPropertyService);
	}
	
	@Test
	public void searchForDiagnosticReports_shouldFanOutAndReturnHandlerResults() {
		when(handler.search(any())).thenReturn(bundleOf(2));
		
		IBundleProvider results = service.searchForDiagnosticReports(
		    new DiagnosticReportSearchParams(null, null, null, null, null, null, null, null, null));
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(2));
		verify(handler).search(any());
	}
	
	@Test
	public void create_shouldDispatchToHandler() {
		DiagnosticReport input = new DiagnosticReport();
		DiagnosticReport created = new DiagnosticReport();
		created.setId(UUID);
		when(handler.canHandle(input)).thenReturn(true);
		when(handler.create(input)).thenReturn(created);
		
		DiagnosticReport result = service.create(input);
		
		assertThat(result, notNullValue());
		verify(handler).create(input);
	}
	
	@Test
	public void update_shouldDispatchToHandler() {
		DiagnosticReport input = new DiagnosticReport();
		input.setId(UUID);
		DiagnosticReport updated = new DiagnosticReport();
		updated.setId(UUID);
		when(handler.exists(UUID)).thenReturn(true);
		when(handler.update(UUID, input, null, false)).thenReturn(updated);
		
		DiagnosticReport result = service.update(UUID, input);
		
		assertThat(result, notNullValue());
		verify(handler).update(UUID, input, null, false);
	}
	
	private static IBundleProvider bundleOf(int n) {
		List<DiagnosticReport> rows = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			rows.add(new DiagnosticReport());
		}
		return new MockIBundleProvider<>(rows, 10, 1);
	}
}
