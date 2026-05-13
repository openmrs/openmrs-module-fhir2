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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.search.CompositeBundleProvider;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

@RunWith(MockitoJUnitRunner.class)
public class BaseCompositeFhirServiceTest {
	
	private static final String PROFILE_PRIMARY = "http://example.org/StructureDefinition/primary";
	
	private static final String PROFILE_SECONDARY = "http://example.org/StructureDefinition/secondary";
	
	private static final String UUID_PRIMARY = "11111111-1111-1111-1111-111111111111";
	
	private static final String UUID_SECONDARY = "22222222-2222-2222-2222-222222222222";
	
	private static final String UUID_UNKNOWN = "99999999-9999-9999-9999-999999999999";
	
	@Mock
	private FhirResourceHandler<Encounter> primaryHandler;
	
	@Mock
	private FhirResourceHandler<Encounter> secondaryHandler;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private TestCompositeService service;
	
	@Before
	public void setUp() {
		// lenient — most tests only exercise a subset of the handler API
		lenient().when(primaryHandler.getImplicitProfile()).thenReturn(PROFILE_PRIMARY);
		lenient().when(secondaryHandler.getImplicitProfile()).thenReturn(PROFILE_SECONDARY);
		// Mockito mocks return null for getBackingKey() by default — the orchestrator's collapse
		// would then treat both handlers as the same backing and drop one. Stub distinct keys so
		// the tests see both handlers as independent.
		lenient().when(primaryHandler.getBackingKey()).thenReturn("test.primary");
		lenient().when(secondaryHandler.getBackingKey()).thenReturn("test.secondary");
		lenient().when(primaryHandler.acceptsSearch(any())).thenReturn(true);
		lenient().when(secondaryHandler.acceptsSearch(any())).thenReturn(true);
		// Default UUID probe: nothing is owned. Tests that need ownership stub exists() per UUID.
		lenient().when(primaryHandler.exists(anyString())).thenReturn(false);
		lenient().when(secondaryHandler.exists(anyString())).thenReturn(false);
		// Default get: throws ResourceNotFoundException — matches FhirService contract for missing
		// UUIDs. Tests that need a successful read use doReturn(...).when(handler).get(uuid) to
		// override per UUID without re-invoking the throwing default during stubbing.
		// (Mockito mocks otherwise return null for unstubbed methods, which would mask the contract.)
		lenient().doThrow(new ResourceNotFoundException("not found")).when(primaryHandler).get(anyString());
		lenient().doThrow(new ResourceNotFoundException("not found")).when(secondaryHandler).get(anyString());
		
		service = new TestCompositeService();
		service.setHandlers(Arrays.asList(primaryHandler, secondaryHandler));
		service.setGlobalPropertyService(globalPropertyService);
	}
	
	// ---- get ----
	
	@Test
	public void getShouldRejectNullUuid() {
		assertThrows(InvalidRequestException.class, () -> service.get((String) null));
	}
	
	@Test
	public void getShouldThrowResourceNotFoundWhenNoHandlerClaimsUuid() {
		assertThrows(ResourceNotFoundException.class, () -> service.get(UUID_UNKNOWN));
	}
	
	@Test
	public void getShouldReturnFromFirstClaimingHandlerInPriorityOrder() {
		// given — primary returns the resource; secondary should never be invoked past it
		Encounter fromPrimary = encounter(UUID_PRIMARY);
		doReturn(fromPrimary).when(primaryHandler).get(UUID_PRIMARY);
		
		Encounter result = service.get(UUID_PRIMARY);
		
		assertThat(result, sameInstance(fromPrimary));
		verify(secondaryHandler, never()).get(anyString());
	}
	
	@Test
	public void getShouldFallThroughWhenFirstHandlerThrowsResourceNotFound() {
		// given — primary throws (default lenient setUp), secondary returns
		Encounter fromSecondary = encounter(UUID_SECONDARY);
		doReturn(fromSecondary).when(secondaryHandler).get(UUID_SECONDARY);
		
		Encounter result = service.get(UUID_SECONDARY);
		
		assertThat(result, sameInstance(fromSecondary));
		verify(primaryHandler).get(UUID_SECONDARY);
	}
	
	@Test
	public void getShouldPropagateResourceGoneFromOwningHandler() {
		// given — primary owns the UUID but the underlying record is voided/retired
		doThrow(new ca.uhn.fhir.rest.server.exceptions.ResourceGoneException("gone")).when(primaryHandler).get(UUID_PRIMARY);
		
		// when / then — ResourceGone propagates without falling through to secondary
		assertThrows(ca.uhn.fhir.rest.server.exceptions.ResourceGoneException.class, () -> service.get(UUID_PRIMARY));
		verify(secondaryHandler, never()).get(anyString());
	}
	
	@Test
	public void getShouldStampImplicitProfile() {
		Encounter raw = encounter(UUID_PRIMARY);
		doReturn(raw).when(primaryHandler).get(UUID_PRIMARY);
		
		Encounter result = service.get(UUID_PRIMARY);
		
		assertThat(profileUrlsOf(result), contains(PROFILE_PRIMARY));
	}
	
	@Test
	public void getShouldNotDuplicateAlreadyPresentProfile() {
		// given — handler returns a resource that already has its profile set
		Encounter raw = encounter(UUID_PRIMARY);
		raw.getMeta().addProfile(PROFILE_PRIMARY);
		doReturn(raw).when(primaryHandler).get(UUID_PRIMARY);
		
		Encounter result = service.get(UUID_PRIMARY);
		
		assertThat(profileUrlsOf(result), contains(PROFILE_PRIMARY));
	}
	
	// ---- get(Collection) ----
	
	@Test
	public void getCollectionShouldAggregateAcrossHandlers() {
		// given
		List<String> uuids = Arrays.asList(UUID_PRIMARY, UUID_SECONDARY);
		when(primaryHandler.get(uuids)).thenReturn(Collections.singletonList(encounter(UUID_PRIMARY)));
		when(secondaryHandler.get(uuids)).thenReturn(Collections.singletonList(encounter(UUID_SECONDARY)));
		
		// when
		List<Encounter> results = service.get(uuids);
		
		// then
		assertThat(results, hasSize(2));
		assertThat(results.stream().map(e -> e.getIdElement().getIdPart()).collect(Collectors.toList()),
		    contains(UUID_PRIMARY, UUID_SECONDARY));
	}
	
	// ---- create ----
	
	@Test
	public void createShouldRejectNullResource() {
		assertThrows(InvalidRequestException.class, () -> service.create(null));
	}
	
	@Test
	public void createShouldThrowNotImplementedWhenNoHandlerClaims() {
		Encounter resource = new Encounter();
		when(primaryHandler.canHandle(any())).thenReturn(false);
		when(secondaryHandler.canHandle(any())).thenReturn(false);
		
		assertThrows(NotImplementedOperationException.class, () -> service.create(resource));
	}
	
	@Test
	public void createShouldRouteByMetaProfileWhenSet() {
		// given — meta.profile names the secondary handler, even though primary canHandle is true
		Encounter resource = new Encounter();
		resource.getMeta().addProfile(PROFILE_SECONDARY);
		Encounter persisted = encounter(UUID_SECONDARY);
		lenient().when(primaryHandler.canHandle(any())).thenReturn(true);
		when(secondaryHandler.create(resource)).thenReturn(persisted);
		
		Encounter result = service.create(resource);
		
		assertThat(result, sameInstance(persisted));
		verify(primaryHandler, never()).create(any());
	}
	
	@Test
	public void createShouldFallBackToCanHandleWhenProfileUnrecognized() {
		Encounter resource = new Encounter();
		resource.getMeta().addProfile("http://example.org/StructureDefinition/unknown");
		when(primaryHandler.canHandle(resource)).thenReturn(true);
		Encounter persisted = encounter(UUID_PRIMARY);
		when(primaryHandler.create(resource)).thenReturn(persisted);
		
		Encounter result = service.create(resource);
		
		assertThat(result, sameInstance(persisted));
		verify(secondaryHandler, never()).create(any());
	}
	
	@Test
	public void createShouldUseFirstCanHandleInPriorityOrder() {
		Encounter resource = new Encounter();
		when(primaryHandler.canHandle(resource)).thenReturn(true);
		lenient().when(secondaryHandler.canHandle(resource)).thenReturn(true);
		when(primaryHandler.create(resource)).thenReturn(encounter(UUID_PRIMARY));
		
		service.create(resource);
		
		verify(primaryHandler).create(resource);
		verify(secondaryHandler, never()).create(any());
	}
	
	@Test
	public void createShouldStampHandlerProfile() {
		Encounter resource = new Encounter();
		when(primaryHandler.canHandle(resource)).thenReturn(true);
		when(primaryHandler.create(resource)).thenReturn(encounter(UUID_PRIMARY));
		
		Encounter result = service.create(resource);
		
		assertThat(profileUrlsOf(result), contains(PROFILE_PRIMARY));
	}
	
	// ---- update ----
	
	@Test
	public void updateShouldRejectNullUuid() {
		assertThrows(InvalidRequestException.class, () -> service.update(null, new Encounter()));
	}
	
	@Test
	public void updateShouldRejectNullResource() {
		assertThrows(InvalidRequestException.class, () -> service.update(UUID_PRIMARY, null));
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundWhenNoHandlerOwnsUuidAndNotCreateIfNotExists() {
		assertThrows(ResourceNotFoundException.class, () -> service.update(UUID_UNKNOWN, new Encounter()));
	}
	
	@Test
	public void updateShouldRouteToCurrentOwnerByExistsProbe() {
		// given — secondary handler currently owns the uuid
		Encounter incoming = new Encounter();
		when(secondaryHandler.exists(UUID_SECONDARY)).thenReturn(true);
		when(secondaryHandler.update(eq(UUID_SECONDARY), eq(incoming), any(), anyBoolean()))
		        .thenReturn(encounter(UUID_SECONDARY));
		
		service.update(UUID_SECONDARY, incoming);
		
		verify(secondaryHandler).update(eq(UUID_SECONDARY), eq(incoming), any(), anyBoolean());
		verify(primaryHandler, never()).update(anyString(), any(), any(), anyBoolean());
	}
	
	@Test
	public void updateShouldRouteThroughCreateDispatchWhenCreateIfNotExistsAndNoOwner() {
		// given — no handler owns the uuid, but primary canHandle the new resource
		Encounter incoming = new Encounter();
		when(primaryHandler.canHandle(incoming)).thenReturn(true);
		when(primaryHandler.update(eq(UUID_UNKNOWN), eq(incoming), any(), eq(true))).thenReturn(encounter(UUID_UNKNOWN));
		
		Encounter result = service.update(UUID_UNKNOWN, incoming, null, true);
		
		assertThat(result, notNullValue());
		verify(primaryHandler).update(eq(UUID_UNKNOWN), eq(incoming), any(), eq(true));
	}
	
	// ---- patch ----
	
	@Test
	public void patchShouldRejectNullUuid() {
		assertThrows(InvalidRequestException.class, () -> service.patch(null, PatchTypeEnum.JSON_PATCH, "[]", null));
	}
	
	@Test
	public void patchShouldThrowResourceNotFoundWhenNoHandlerOwnsUuid() {
		assertThrows(ResourceNotFoundException.class,
		    () -> service.patch(UUID_UNKNOWN, PatchTypeEnum.JSON_PATCH, "[]", null));
	}
	
	@Test
	public void patchShouldRouteToCurrentOwner() {
		when(secondaryHandler.exists(UUID_SECONDARY)).thenReturn(true);
		when(secondaryHandler.patch(eq(UUID_SECONDARY), eq(PatchTypeEnum.JSON_PATCH), eq("[]"), any()))
		        .thenReturn(encounter(UUID_SECONDARY));
		
		service.patch(UUID_SECONDARY, PatchTypeEnum.JSON_PATCH, "[]", null);
		
		verify(secondaryHandler).patch(eq(UUID_SECONDARY), eq(PatchTypeEnum.JSON_PATCH), eq("[]"), any());
		verify(primaryHandler, never()).patch(anyString(), any(), anyString(), any());
	}
	
	@Test
	public void patchShouldStampOwnerProfileOnReturnedResource() {
		when(primaryHandler.exists(UUID_PRIMARY)).thenReturn(true);
		when(primaryHandler.patch(eq(UUID_PRIMARY), any(), anyString(), any())).thenReturn(encounter(UUID_PRIMARY));
		
		Encounter result = service.patch(UUID_PRIMARY, PatchTypeEnum.JSON_PATCH, "[]", null);
		
		assertThat(profileUrlsOf(result), contains(PROFILE_PRIMARY));
	}
	
	// ---- delete ----
	
	@Test
	public void deleteShouldRejectNullUuid() {
		assertThrows(InvalidRequestException.class, () -> service.delete(null));
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundWhenNoHandlerOwnsUuid() {
		assertThrows(ResourceNotFoundException.class, () -> service.delete(UUID_UNKNOWN));
	}
	
	@Test
	public void deleteShouldRouteToCurrentOwner() {
		when(primaryHandler.exists(UUID_PRIMARY)).thenReturn(true);
		
		service.delete(UUID_PRIMARY);
		
		verify(primaryHandler).delete(UUID_PRIMARY);
		verify(secondaryHandler, never()).delete(anyString());
	}
	
	// ---- search ----
	
	@Test
	public void searchShouldReturnEmptyBundleWhenNoHandlersAccept() {
		when(primaryHandler.acceptsSearch(any())).thenReturn(false);
		when(secondaryHandler.acceptsSearch(any())).thenReturn(false);
		
		IBundleProvider result = service.exposedSearch(new SearchParameterMap());
		
		assertThat(result, instanceOf(SimpleBundleProvider.class));
		assertThat(result.size(), is(0));
	}
	
	@Test
	public void searchShouldReturnSingleProviderUnwrappedWhenOnlyOneHandlerAccepts() {
		// given — only primary accepts
		when(secondaryHandler.acceptsSearch(any())).thenReturn(false);
		IBundleProvider primaryBundle = mock(IBundleProvider.class);
		when(primaryHandler.search(any())).thenReturn(primaryBundle);
		
		IBundleProvider result = service.exposedSearch(new SearchParameterMap());
		
		assertThat(result, notNullValue());
		assertThat(result, is(not(instanceOf(CompositeBundleProvider.class))));
	}
	
	@Test
	public void searchShouldReturnCompositeBundleProviderWhenMultipleHandlersAccept() {
		IBundleProvider primaryBundle = emptyBundle();
		IBundleProvider secondaryBundle = emptyBundle();
		when(primaryHandler.search(any())).thenReturn(primaryBundle);
		when(secondaryHandler.search(any())).thenReturn(secondaryBundle);
		
		IBundleProvider result = service.exposedSearch(new SearchParameterMap());
		
		assertThat(result, instanceOf(CompositeBundleProvider.class));
	}
	
	@Test
	public void searchShouldOmitHandlersThatOptOutViaAcceptsSearch() {
		// given — secondary opts out for this params instance
		SearchParameterMap params = new SearchParameterMap();
		when(secondaryHandler.acceptsSearch(params)).thenReturn(false);
		IBundleProvider primaryBundle = emptyBundle();
		when(primaryHandler.search(any())).thenReturn(primaryBundle);
		
		service.exposedSearch(params);
		
		verify(primaryHandler).search(any());
		verify(secondaryHandler, never()).search(any());
	}
	
	@Test
	public void searchShouldStampHandlerProfileOnReturnedResources() {
		when(secondaryHandler.acceptsSearch(any())).thenReturn(false);
		IBundleProvider primaryBundle = mock(IBundleProvider.class);
		Encounter raw = encounter("e1");
		when(primaryBundle.getResources(anyInt(), anyInt())).thenReturn(Collections.singletonList((IBaseResource) raw));
		when(primaryHandler.search(any())).thenReturn(primaryBundle);
		
		IBundleProvider result = service.exposedSearch(new SearchParameterMap());
		List<IBaseResource> page = result.getResources(0, 10);
		
		assertThat(page, hasSize(1));
		Encounter stamped = (Encounter) page.get(0);
		assertThat(profileUrlsOf(stamped), contains(PROFILE_PRIMARY));
	}
	
	// ---- backing-key collapse ----
	
	@Test
	public void collapseShouldDropLowerPriorityHandlerWithDuplicateBackingKey() {
		// given — both handlers declare the same backing key; primary has higher priority by
		// virtue of being first in the injected list.
		FhirResourceHandler<Encounter> overridePrimary = primaryHandler;
		FhirResourceHandler<Encounter> overrideSecondary = secondaryHandler;
		lenient().when(overridePrimary.getBackingKey()).thenReturn("shared.key");
		lenient().when(overrideSecondary.getBackingKey()).thenReturn("shared.key");
		
		// Re-inject so the setter re-runs the collapse with the new keys.
		TestCompositeService localService = new TestCompositeService();
		localService.setHandlers(Arrays.asList(overridePrimary, overrideSecondary));
		localService.setGlobalPropertyService(globalPropertyService);
		
		// then — secondary was filtered out by the collapse; only primary is consulted, and its
		// default lenient setUp stub makes get() throw ResourceNotFoundException.
		assertThrows(ResourceNotFoundException.class, () -> localService.get(UUID_SECONDARY));
		verify(overrideSecondary, never()).get(UUID_SECONDARY);
	}
	
	@Test
	public void collapseShouldKeepHandlersWithDistinctBackingKeys() {
		// default setUp uses distinct keys ("test.primary", "test.secondary"); both should remain
		// in the active list. We verify by exercising fan-out search: both handlers participate.
		IBundleProvider primaryBundle = emptyBundle();
		IBundleProvider secondaryBundle = emptyBundle();
		when(primaryHandler.search(any())).thenReturn(primaryBundle);
		when(secondaryHandler.search(any())).thenReturn(secondaryBundle);
		
		service.exposedSearch(new SearchParameterMap());
		
		verify(primaryHandler).search(any());
		verify(secondaryHandler).search(any());
	}
	
	// ---- helpers ----
	
	private static Encounter encounter(String uuid) {
		Encounter e = new Encounter();
		e.setId(uuid);
		return e;
	}
	
	private static List<String> profileUrlsOf(Encounter e) {
		return e.getMeta().getProfile().stream().map(p -> p.getValue()).collect(Collectors.toList());
	}
	
	private static IBundleProvider emptyBundle() {
		IBundleProvider b = mock(IBundleProvider.class);
		lenient().when(b.size()).thenReturn(0);
		return b;
	}
	
	private static <T> org.hamcrest.Matcher<T> not(org.hamcrest.Matcher<T> matcher) {
		return org.hamcrest.Matchers.not(matcher);
	}
	
	/**
	 * Concrete subclass that exposes the protected {@code doSearch} for direct testing.
	 */
	private static class TestCompositeService extends BaseCompositeFhirService<Encounter> {
		
		IBundleProvider exposedSearch(SearchParameterMap params) {
			return doSearch(params);
		}
	}
}
