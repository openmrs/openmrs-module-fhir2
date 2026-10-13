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
import org.hl7.fhir.r4.model.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.search.param.PersonSearchParams;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

/**
 * Orchestrator-level tests for {@link FhirPersonServiceImpl}. Dispatch mechanics are covered in
 * {@link BaseCompositeFhirServiceTest}; backing-specific CRUD/search lives in
 * {@code PersonBackedPersonHandlerTest}. What this class covers is that create/update/search reach
 * the handler through the composite.
 */
@RunWith(MockitoJUnitRunner.class)
public class FhirPersonServiceImplTest {
	
	private static final String PERSON_UUID = "1223-2323-2323-nd23";
	
	private static final String GIVEN_NAME = "John";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 100;
	
	@Mock
	private FhirResourceHandler<Person> handler;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirPersonServiceImpl service;
	
	@Before
	public void setup() {
		lenient().when(handler.getImplicitProfile())
		        .thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-person");
		lenient().when(handler.acceptsSearch(any())).thenReturn(true);
		
		service = new FhirPersonServiceImpl();
		service.setHandlers(Collections.singletonList(handler));
		service.setGlobalPropertyService(globalPropertyService);
	}
	
	@Test
	public void searchForPeople_shouldFanOutAndReturnHandlerResults() {
		when(handler.search(any())).thenReturn(bundleOf(1));
		
		StringAndListParam name = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(GIVEN_NAME)));
		
		IBundleProvider results = service
		        .searchForPeople(new PersonSearchParams(name, null, null, null, null, null, null, null, null, null, null));
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(1));
		verify(handler).search(any());
	}
	
	@Test
	public void create_shouldDispatchToHandler() {
		Person input = new Person();
		Person created = new Person();
		created.setId(PERSON_UUID);
		when(handler.canHandle(input)).thenReturn(true);
		when(handler.create(input)).thenReturn(created);
		
		Person result = service.create(input);
		
		assertThat(result, notNullValue());
		verify(handler).create(input);
	}
	
	@Test
	public void update_shouldDispatchToHandler() {
		Person input = new Person();
		input.setId(PERSON_UUID);
		Person updated = new Person();
		updated.setId(PERSON_UUID);
		when(handler.exists(PERSON_UUID)).thenReturn(true);
		when(handler.update(PERSON_UUID, input, null, false)).thenReturn(updated);
		
		Person result = service.update(PERSON_UUID, input);
		
		assertThat(result, notNullValue());
		verify(handler).update(PERSON_UUID, input, null, false);
	}
	
	private static IBundleProvider bundleOf(int n) {
		List<Person> rows = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			rows.add(new Person());
		}
		return new MockIBundleProvider<>(rows, 10, 1);
	}
}
