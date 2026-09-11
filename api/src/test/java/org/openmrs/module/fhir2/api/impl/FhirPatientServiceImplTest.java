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
import static org.hamcrest.Matchers.contains;
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
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.search.param.OpenmrsPatientSearchParams;
import org.openmrs.module.fhir2.api.search.param.PatientSearchParams;
import org.openmrs.module.fhir2.api.search.param.PropParam;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

/**
 * Orchestrator-level tests for {@link FhirPatientServiceImpl}. Dispatch mechanics are covered in
 * {@link BaseCompositeFhirServiceTest}; backing-specific CRUD/search lives in
 * {@code PatientBackedPatientHandlerTest}. What this class covers is that create/update/search
 * reach the handler through the composite, and that the {@code $everything} operations build the
 * search parameters they promise.
 */
@RunWith(MockitoJUnitRunner.class)
public class FhirPatientServiceImplTest {
	
	private static final String PATIENT_UUID = "3434gh32-34h3j4-34jk34-3422h";
	
	private static final String PATIENT_GIVEN_NAME = "Jeannette";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 100;
	
	@Mock
	private FhirResourceHandler<Patient> handler;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirPatientServiceImpl service;
	
	@Before
	public void setup() {
		lenient().when(handler.getImplicitProfile())
		        .thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-patient");
		lenient().when(handler.acceptsSearch(any())).thenReturn(true);
		
		service = new FhirPatientServiceImpl();
		service.setHandlers(Collections.singletonList(handler));
		service.setGlobalPropertyService(globalPropertyService);
	}
	
	private SearchParameterMap capturedSearchParams() {
		ArgumentCaptor<SearchParameterMap> captor = ArgumentCaptor.forClass(SearchParameterMap.class);
		verify(handler).search(captor.capture());
		return captor.getValue();
	}
	
	@Test
	public void searchForPatients_shouldFanOutAndReturnHandlerResults() {
		when(handler.search(any())).thenReturn(bundleOf(1));
		
		StringAndListParam name = new StringAndListParam().addAnd(new StringParam(PATIENT_GIVEN_NAME));
		
		IBundleProvider results = service.searchForPatients(new PatientSearchParams(name, null, null, null, null, null, null,
		        null, null, null, null, null, null, null, null, null, null));
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(1));
		verify(handler).search(any());
	}
	
	@Test
	public void searchForPatients_shouldFanOutForOpenmrsQuery() {
		when(handler.search(any())).thenReturn(bundleOf(1));
		
		StringAndListParam query = new StringAndListParam().addAnd(new StringParam(PATIENT_GIVEN_NAME));
		
		IBundleProvider results = service.searchForPatients(OpenmrsPatientSearchParams.builder().query(query).build());
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(1));
		verify(handler).search(any());
	}
	
	@Test
	public void create_shouldDispatchToHandler() {
		Patient input = new Patient();
		Patient created = new Patient();
		created.setId(PATIENT_UUID);
		when(handler.canHandle(input)).thenReturn(true);
		when(handler.create(input)).thenReturn(created);
		
		Patient result = service.create(input);
		
		assertThat(result, notNullValue());
		verify(handler).create(input);
	}
	
	@Test
	public void update_shouldDispatchToHandler() {
		Patient input = new Patient();
		input.setId(PATIENT_UUID);
		Patient updated = new Patient();
		updated.setId(PATIENT_UUID);
		when(handler.exists(PATIENT_UUID)).thenReturn(true);
		when(handler.update(PATIENT_UUID, input, null, false)).thenReturn(updated);
		
		Patient result = service.update(PATIENT_UUID, input);
		
		assertThat(result, notNullValue());
		verify(handler).update(PATIENT_UUID, input, null, false);
	}
	
	@Test
	public void getPatientEverything_shouldFanOutAndReturnHandlerResults() {
		when(handler.search(any())).thenReturn(bundleOf(1));
		
		IBundleProvider results = service.getPatientEverything(new TokenParam().setValue(PATIENT_UUID));
		
		assertThat(results, notNullValue());
		assertThat(results.getResources(START_INDEX, END_INDEX), hasSize(1));
		verify(handler).search(any());
	}
	
	@Test
	public void getPatientEverything_shouldRequestEverythingForTheGivenPatient() {
		when(handler.search(any())).thenReturn(bundleOf(1));
		
		service.getPatientEverything(new TokenParam().setValue(PATIENT_UUID));
		
		SearchParameterMap theParams = capturedSearchParams();
		
		assertThat(theParams.getParameters(FhirConstants.EVERYTHING_SEARCH_HANDLER), hasSize(1));
		assertThat(theParams.getParameters(FhirConstants.COMMON_SEARCH_HANDLER), hasSize(1));
		assertThat(revIncludeTargets(theParams),
		    contains(FhirConstants.ALLERGY_INTOLERANCE, FhirConstants.DIAGNOSTIC_REPORT, FhirConstants.ENCOUNTER,
		        FhirConstants.MEDICATION_REQUEST, FhirConstants.OBSERVATION, FhirConstants.PROCEDURE_REQUEST,
		        FhirConstants.SERVICE_REQUEST));
	}
	
	@Test
	public void getPatientEverything_shouldRequestEverythingForAllPatientsWhenNoIdSupplied() {
		when(handler.search(any())).thenReturn(bundleOf(1));
		
		service.getPatientEverything();
		
		SearchParameterMap theParams = capturedSearchParams();
		
		assertThat(theParams.getParameters(FhirConstants.EVERYTHING_SEARCH_HANDLER), hasSize(1));
		assertThat(theParams.getParameters(FhirConstants.COMMON_SEARCH_HANDLER), empty());
		assertThat(revIncludeTargets(theParams),
		    contains(FhirConstants.ALLERGY_INTOLERANCE, FhirConstants.DIAGNOSTIC_REPORT, FhirConstants.ENCOUNTER,
		        FhirConstants.MEDICATION_REQUEST, FhirConstants.OBSERVATION, FhirConstants.PROCEDURE_REQUEST,
		        FhirConstants.SERVICE_REQUEST));
	}
	
	/**
	 * The reverse-included resource types requested by an {@code $everything} search, sorted so the
	 * assertion does not depend on {@code HashSet} iteration order.
	 */
	@SuppressWarnings("unchecked")
	private static List<String> revIncludeTargets(SearchParameterMap theParams) {
		List<PropParam<Object>> params = theParams.getParameters(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER);
		HashSet<Include> revIncludes = (HashSet<Include>) params.get(0).getParam();
		
		return revIncludes.stream().map(Include::getParamType).sorted().collect(Collectors.toList());
	}
	
	private static IBundleProvider bundleOf(int n) {
		List<Patient> rows = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			rows.add(new Patient());
		}
		return new MockIBundleProvider<>(rows, 10, 1);
	}
}
