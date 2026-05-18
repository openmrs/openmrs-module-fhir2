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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirUserService;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

@RunWith(MockitoJUnitRunner.class)
public class UserBackedPractitionerHandlerTest {
	
	private static final String PRACTITIONER_UUID = "28923n23-nmkn23-23923-23sd";
	
	@Mock
	private FhirUserService userService;
	
	private UserBackedPractitionerHandler handler;
	
	private Practitioner practitioner;
	
	@Before
	public void setUp() {
		handler = new UserBackedPractitionerHandler();
		handler.setUserService(userService);
		
		practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeUserImplicitProfile() {
		assertThat(handler.getImplicitProfile(), equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-user"));
	}
	
	@Test
	public void shouldExposeUserBackingKey() {
		assertThat(handler.getBackingKey(), equalTo("openmrs.user"));
	}
	
	@Test
	public void canHandle_shouldAlwaysReturnFalse() {
		// Preserves OLD behaviour: the previous orchestrator never created a User from a FHIR
		// Practitioner write. An override handler with the same backing key could enable this.
		assertFalse(handler.canHandle(new Practitioner()));
		assertFalse(handler.canHandle(practitioner));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptAnySearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	// ---- delegation ----
	
	@Test
	public void get_shouldDelegateToUserService() {
		when(userService.get(PRACTITIONER_UUID)).thenReturn(practitioner);
		
		Practitioner result = handler.get(PRACTITIONER_UUID);
		
		assertThat(result, sameInstance(practitioner));
		verify(userService).get(PRACTITIONER_UUID);
	}
	
	@Test
	public void exists_shouldDelegateToUserServiceExists() {
		when(userService.exists(PRACTITIONER_UUID)).thenReturn(true);
		
		assertTrue(handler.exists(PRACTITIONER_UUID));
		verify(userService).exists(PRACTITIONER_UUID);
		verify(userService, never()).get(PRACTITIONER_UUID);
	}
	
	@Test
	public void create_shouldDelegateToUserService() {
		when(userService.create(practitioner)).thenReturn(practitioner);
		
		Practitioner result = handler.create(practitioner);
		
		assertThat(result, sameInstance(practitioner));
		verify(userService).create(practitioner);
	}
	
	@Test
	public void update_shouldDelegateToUserService() {
		when(userService.update(PRACTITIONER_UUID, practitioner)).thenReturn(practitioner);
		
		Practitioner result = handler.update(PRACTITIONER_UUID, practitioner);
		
		assertThat(result, sameInstance(practitioner));
		verify(userService).update(PRACTITIONER_UUID, practitioner);
	}
	
	@Test
	public void delete_shouldDelegateToUserService() {
		handler.delete(PRACTITIONER_UUID);
		verify(userService).delete(PRACTITIONER_UUID);
	}
	
	@Test
	public void search_shouldDelegateToUserServiceWithEmptySearchParams() {
		// Preserves the OLD behaviour: user search ignored practitioner search params and always
		// returned every user. Documented limitation worth fixing separately.
		IBundleProvider expected = new SimpleBundleProvider();
		when(userService.searchForUsers(any())).thenReturn(expected);
		
		SearchParameterMap caller = new SearchParameterMap();
		IBundleProvider result = handler.search(caller);
		
		assertThat(result, is(expected));
		
		ArgumentCaptor<SearchParameterMap> captor = ArgumentCaptor.forClass(SearchParameterMap.class);
		verify(userService).searchForUsers(captor.capture());
		// Handler passes a fresh, empty SearchParameterMap — not the caller's filters.
		assertThat(captor.getValue(), is(not(sameInstance(caller))));
		assertThat(captor.getValue().getParameters(), is(empty()));
	}
	
	@Test
	public void acceptsSearch_shouldNotInteractWithUserService() {
		handler.acceptsSearch(new SearchParameterMap());
		verifyNoInteractions(userService);
	}
}
