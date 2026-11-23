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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import org.hamcrest.Matchers;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.search.param.RelatedPersonSearchParams;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

@RunWith(MockitoJUnitRunner.class)
public class FhirRelatedPersonServiceImplTest {
	
	private static final String RELATED_PERSON_UUID = "5f07c6ff-c483-4e77-815e-44dd650470e7";
	
	private static final String WRONG_RELATED_PERSON_UUID = "1a1d2623-2f67-47de-8fb0-b02f51e378b7";
	
	private static final String GIVEN_NAME = "John";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirResourceHandler<RelatedPerson> handler;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirRelatedPersonServiceImpl service;
	
	@Before
	public void setup() {
		lenient().when(handler.getImplicitProfile())
		        .thenReturn("http://fhir.openmrs.org/StructureDefinition/openmrs-relatedperson");
		lenient().when(handler.acceptsSearch(any())).thenReturn(true);
		
		service = new FhirRelatedPersonServiceImpl();
		service.setHandlers(Collections.singletonList(handler));
		service.setGlobalPropertyService(globalPropertyService);
	}
	
	@Test
	public void searchForPeople_shouldFanOutAndReturnHandlerResults() {
		when(handler.search(any())).thenReturn(bundleOf(1));
		
		StringAndListParam name = new StringAndListParam().addAnd(new StringParam().setValue(GIVEN_NAME));
		
		IBundleProvider results = service.searchForRelatedPeople(RelatedPersonSearchParams.builder().name(name).build());
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(1));
		verify(handler).search(any());
	}
	
	@Test
	public void create_shouldDispatchToHandler() {
		RelatedPerson input = new RelatedPerson();
		RelatedPerson created = new RelatedPerson();
		created.setId(RELATED_PERSON_UUID);
		when(handler.canHandle(input)).thenReturn(true);
		when(handler.create(input)).thenReturn(created);
		
		RelatedPerson result = service.create(input);
		
		assertThat(result, Matchers.notNullValue());
		verify(handler).create(input);
	}
	
	@Test
	public void update_shouldDispatchToHandler() {
		RelatedPerson input = new RelatedPerson();
		input.setId(RELATED_PERSON_UUID);
		RelatedPerson updated = new RelatedPerson();
		updated.setId(RELATED_PERSON_UUID);
		when(handler.exists(RELATED_PERSON_UUID)).thenReturn(true);
		when(handler.update(RELATED_PERSON_UUID, input, null, false)).thenReturn(updated);
		
		RelatedPerson result = service.update(RELATED_PERSON_UUID, input);
		
		assertThat(result, Matchers.notNullValue());
		verify(handler).update(RELATED_PERSON_UUID, input, null, false);
	}
	
	private static IBundleProvider bundleOf(int n) {
		List<RelatedPerson> rows = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			rows.add(new RelatedPerson());
		}
		return new MockIBundleProvider<>(rows, 10, 1);
	}
	
}
