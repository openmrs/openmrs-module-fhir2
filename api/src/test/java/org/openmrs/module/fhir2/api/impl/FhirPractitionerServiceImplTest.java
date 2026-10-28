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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
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
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

/**
 * Orchestrator-level tests for {@link FhirPractitionerServiceImpl}. Dispatch mechanics are covered
 * in {@link BaseCompositeFhirServiceTest}; provider/user CRUD + search lives in
 * {@code FhirOpenmrsProviderServiceImplTest} and the handler-side tests under
 * {@code o.o.m.fhir2.api.handler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FhirPractitionerServiceImplTest {
	
	private static final String PROVIDER_UUID = "f9badd80-ab76-11e2-9e96-0800200c9a66";
	
	private static final String USER_UUID = "c98a1558-e131-11de-babe-001e378eb67e";
	
	private static final String PROVIDER_PROFILE = "http://fhir.openmrs.org/StructureDefinition/openmrs-provider";
	
	private static final String USER_PROFILE = "http://fhir.openmrs.org/StructureDefinition/openmrs-user";
	
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
	
	/**
	 * meta.profile bypasses canHandle, so naming the user backing reaches it even though it never
	 * claims content; the read-only handler then refuses the write.
	 */
	@Test
	public void create_shouldRouteToUserHandlerWhenMetaProfileNamesIt() {
		// given
		Practitioner practitioner = new Practitioner();
		practitioner.addIdentifier(new Identifier().setValue("X"));
		practitioner.getMeta().addProfile(USER_PROFILE);
		when(userHandler.create(practitioner)).thenThrow(readOnly());
		
		// when / then
		assertThrows(MethodNotAllowedException.class, () -> service.create(practitioner));
		verify(providerHandler, never()).create(any());
	}
	
	// ---- get: provider first, then user ----
	
	@Test
	public void get_shouldFallThroughToUserHandlerWhenProviderHandlerDoesNotKnowTheUuid() {
		// given
		Practitioner user = new Practitioner();
		user.setId(USER_UUID);
		when(providerHandler.get(USER_UUID)).thenThrow(new ResourceNotFoundException("not a provider"));
		when(userHandler.get(USER_UUID)).thenReturn(user);
		
		// when
		Practitioner result = service.get(USER_UUID);
		
		// then
		assertThat(result, sameInstance(user));
		assertThat(result.getMeta().getProfile().get(0).getValue(), equalTo(USER_PROFILE));
	}
	
	@Test
	public void getCollection_shouldIncludeUsers() {
		// given
		List<String> uuids = Arrays.asList(PROVIDER_UUID, USER_UUID);
		Practitioner provider = new Practitioner();
		provider.setId(PROVIDER_UUID);
		Practitioner user = new Practitioner();
		user.setId(USER_UUID);
		when(providerHandler.get(uuids)).thenReturn(new ArrayList<>(Arrays.asList(provider)));
		when(userHandler.get(uuids)).thenReturn(new ArrayList<>(Arrays.asList(user)));
		
		// when
		List<Practitioner> results = service.get(uuids);
		
		// then
		assertThat(results, contains(sameInstance(provider), sameInstance(user)));
		assertThat(user.getMeta().getProfile().get(0).getValue(), equalTo(USER_PROFILE));
	}
	
	// ---- writes to user-owned uuids: refused by the read-only user backing ----
	
	@Test
	public void update_shouldSurfaceMethodNotAllowedForUserOwnedUuid() {
		// given
		Practitioner practitioner = new Practitioner();
		practitioner.setId(USER_UUID);
		when(providerHandler.exists(USER_UUID)).thenReturn(false);
		when(userHandler.exists(USER_UUID)).thenReturn(true);
		when(userHandler.update(USER_UUID, practitioner, null, false)).thenThrow(readOnly());
		
		// when / then
		assertThrows(MethodNotAllowedException.class, () -> service.update(USER_UUID, practitioner));
		verify(providerHandler, never()).update(any(), any(), any(), anyBoolean());
	}
	
	@Test
	public void patch_shouldSurfaceMethodNotAllowedForUserOwnedUuid() {
		// given
		when(providerHandler.exists(USER_UUID)).thenReturn(false);
		when(userHandler.exists(USER_UUID)).thenReturn(true);
		when(userHandler.patch(USER_UUID, PatchTypeEnum.JSON_PATCH, "[]", null)).thenThrow(readOnly());
		
		// when / then
		assertThrows(MethodNotAllowedException.class, () -> service.patch(USER_UUID, PatchTypeEnum.JSON_PATCH, "[]", null));
		verify(providerHandler, never()).patch(any(), any(), any(), any());
	}
	
	@Test
	public void delete_shouldSurfaceMethodNotAllowedForUserOwnedUuid() {
		// given
		when(providerHandler.exists(USER_UUID)).thenReturn(false);
		when(userHandler.exists(USER_UUID)).thenReturn(true);
		doThrow(readOnly()).when(userHandler).delete(USER_UUID);
		
		// when / then
		assertThrows(MethodNotAllowedException.class, () -> service.delete(USER_UUID));
		verify(providerHandler, never()).delete(any());
	}
	
	// ---- update with createIfNotExists: content-based dispatch for unknown uuids ----
	
	@Test
	public void update_shouldCreateViaProviderHandlerForUnknownUuidWithoutProfile() {
		// given
		Practitioner practitioner = new Practitioner();
		practitioner.setId(PROVIDER_UUID);
		when(providerHandler.exists(PROVIDER_UUID)).thenReturn(false);
		when(userHandler.exists(PROVIDER_UUID)).thenReturn(false);
		when(providerHandler.update(PROVIDER_UUID, practitioner, null, true)).thenReturn(practitioner);
		
		// when
		Practitioner result = service.update(PROVIDER_UUID, practitioner, null, true);
		
		// then
		assertThat(result, sameInstance(practitioner));
		assertThat(result.getMeta().getProfile().get(0).getValue(), equalTo(PROVIDER_PROFILE));
		verify(userHandler, never()).update(any(), any(), any(), anyBoolean());
	}
	
	/** Mirrors what {@code UserBackedPractitionerHandler} throws for every write. */
	private static MethodNotAllowedException readOnly() {
		return new MethodNotAllowedException("Practitioners backed by OpenMRS users are read-only through the FHIR API",
		        RequestTypeEnum.GET);
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
