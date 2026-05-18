/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirOpenmrsProviderService;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

/**
 * Tests the dispatch hooks and the delegation behaviour. The CRUD and search logic lives in
 * {@code FhirOpenmrsProviderServiceImpl} and is tested in
 * {@code FhirOpenmrsProviderServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProviderBackedPractitionerHandlerTest {
	
	private static final String PRACTITIONER_UUID = "28923n23-nmkn23-23923-23sd";
	
	@Mock
	private FhirOpenmrsProviderService providerService;
	
	private ProviderBackedPractitionerHandler handler;
	
	private Practitioner practitioner;
	
	@Before
	public void setUp() {
		handler = new ProviderBackedPractitionerHandler();
		handler.setProviderService(providerService);
		
		practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeProviderImplicitProfile() {
		assertThat(handler.getImplicitProfile(), equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-provider"));
	}
	
	@Test
	public void shouldExposeProviderBackingKey() {
		assertThat(handler.getBackingKey(), equalTo("openmrs.provider"));
	}
	
	@Test
	public void canHandle_shouldAlwaysReturnTrue() {
		// Provider is the default backing for new Practitioner resources — claims every input.
		assertTrue(handler.canHandle(new Practitioner()));
		assertTrue(handler.canHandle(practitioner));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptAnySearch() {
		// No tag-routing protocol for Practitioner; handler participates in every fan-out.
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	// ---- delegation ----
	
	@Test
	public void get_shouldDelegateToProviderService() {
		when(providerService.get(PRACTITIONER_UUID)).thenReturn(practitioner);
		
		Practitioner result = handler.get(PRACTITIONER_UUID);
		
		assertThat(result, sameInstance(practitioner));
		verify(providerService).get(PRACTITIONER_UUID);
	}
	
	@Test
	public void exists_shouldDelegateToProviderServiceExists() {
		when(providerService.exists(PRACTITIONER_UUID)).thenReturn(true);
		
		assertTrue(handler.exists(PRACTITIONER_UUID));
		verify(providerService).exists(PRACTITIONER_UUID);
		verify(providerService, never()).get(PRACTITIONER_UUID);
	}
	
	@Test
	public void create_shouldDelegateToProviderService() {
		when(providerService.create(practitioner)).thenReturn(practitioner);
		
		Practitioner result = handler.create(practitioner);
		
		assertThat(result, sameInstance(practitioner));
		verify(providerService).create(practitioner);
	}
	
	@Test
	public void update_shouldDelegateToProviderService() {
		when(providerService.update(PRACTITIONER_UUID, practitioner)).thenReturn(practitioner);
		
		Practitioner result = handler.update(PRACTITIONER_UUID, practitioner);
		
		assertThat(result, sameInstance(practitioner));
		verify(providerService).update(PRACTITIONER_UUID, practitioner);
	}
	
	@Test
	public void delete_shouldDelegateToProviderService() {
		handler.delete(PRACTITIONER_UUID);
		verify(providerService).delete(PRACTITIONER_UUID);
	}
	
	@Test
	public void search_shouldDelegateToProviderServiceSearchForPractitioners() {
		IBundleProvider expected = new SimpleBundleProvider();
		SearchParameterMap params = new SearchParameterMap();
		when(providerService.searchForPractitioners(params)).thenReturn(expected);
		
		IBundleProvider result = handler.search(params);
		
		assertThat(result, is(expected));
		verify(providerService).searchForPractitioners(params);
	}
}
