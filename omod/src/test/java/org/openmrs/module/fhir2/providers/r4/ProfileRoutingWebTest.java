/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.impl.FhirEncounterServiceImpl;
import org.openmrs.module.fhir2.api.search.param.EncounterSearchParams;
import org.openmrs.module.fhir2.api.search.param.LocationSearchParams;
import org.openmrs.module.fhir2.api.util.ProfileRoutingContext;
import org.openmrs.module.fhir2.web.servlet.FhirRestServlet;
import org.openmrs.module.fhir2.web.util.ProfileRoutingInterceptor;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Drives {@code _profile} routing end to end: HTTP request, {@link ProfileRoutingInterceptor}, a
 * real {@link FhirEncounterServiceImpl} composite and two mocked Encounter handlers sharing one
 * {@link ProfileRoutingContext}.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class ProfileRoutingWebTest extends BaseFhirR4ResourceProviderWebTest<EncounterFhirResourceProvider, Encounter> {
	
	private static final String ENCOUNTER_PROFILE = "http://fhir.openmrs.org/StructureDefinition/openmrs-encounter";
	
	private static final String VISIT_PROFILE = "http://fhir.openmrs.org/StructureDefinition/openmrs-visit";
	
	private static final String LOCATION_PROFILE = "http://fhir.openmrs.org/StructureDefinition/openmrs-location";
	
	private ProfileRoutingContext routingContext;
	
	private FhirResourceHandler<Encounter> encounterHandler;
	
	private FhirResourceHandler<Encounter> visitHandler;
	
	private FhirEncounterServiceImpl encounterService;
	
	private EncounterFhirResourceProvider encounterProvider;
	
	private LocationFhirResourceProvider locationProvider;
	
	@Override
	@Before
	@SuppressWarnings("unchecked")
	public void setup() throws javax.servlet.ServletException {
		routingContext = new ProfileRoutingContext();
		
		encounterHandler = mock(FhirResourceHandler.class);
		visitHandler = mock(FhirResourceHandler.class);
		lenient().when(encounterHandler.getImplicitProfile()).thenReturn(ENCOUNTER_PROFILE);
		lenient().when(visitHandler.getImplicitProfile()).thenReturn(VISIT_PROFILE);
		lenient().when(encounterHandler.acceptsSearch(any())).thenReturn(true);
		lenient().when(visitHandler.acceptsSearch(any())).thenReturn(true);
		lenient().when(encounterHandler.search(any())).thenAnswer(i -> bundleOf(5, "enc-"));
		lenient().when(visitHandler.search(any())).thenAnswer(i -> bundleOf(15, "visit-"));
		
		FhirGlobalPropertyService globalPropertyService = mock(FhirGlobalPropertyService.class);
		lenient().when(globalPropertyService.getGlobalPropertyAsInteger(any(), any(Integer.class))).thenReturn(10);
		
		final ProfileRoutingContext context = routingContext;
		final List<FhirResourceHandler<Encounter>> handlers = Arrays.asList(encounterHandler, visitHandler);
		encounterService = new FhirEncounterServiceImpl() {
			
			{
				setHandlers(handlers);
				setProfileRoutingContext(context);
				setGlobalPropertyService(globalPropertyService);
			}
		};
		
		encounterProvider = new EncounterFhirResourceProvider();
		encounterProvider.setEncounterService(encounterService);
		
		// Mirrors SearchQueryIncludeImpl#handleLocationReverseInclude for Encounter:location: a Location
		// search with _revinclude=Encounter:location issues an Encounter search on the same thread.
		FhirLocationService locationService = mock(FhirLocationService.class);
		lenient().when(locationService.searchForLocations(any(LocationSearchParams.class))).thenAnswer(i -> {
			encounterService.searchForEncounters(new EncounterSearchParams());
			Location location = new Location();
			location.setId("loc-1");
			return new SimpleBundleProvider(Collections.singletonList(location));
		});
		locationProvider = new LocationFhirResourceProvider();
		locationProvider.setFhirLocationService(locationService);
		
		super.setup();
	}
	
	@Override
	public EncounterFhirResourceProvider getResourceProvider() {
		return encounterProvider;
	}
	
	@Override
	public FhirRestServlet getRestfulServer() {
		ProfileRoutingInterceptor interceptor = new ProfileRoutingInterceptor();
		interceptor.setProfileRoutingContext(routingContext);
		
		return new FhirRestServlet() {
			
			@Override
			public void setProfileRoutingInterceptor(ProfileRoutingInterceptor ignored) {
				// share the context the composite service reads from
				super.setProfileRoutingInterceptor(interceptor);
			}
			
			@Override
			public void setResourceProviders(IResourceProvider... providers) {
				List<IResourceProvider> all = new ArrayList<>(Arrays.asList(providers));
				all.add(locationProvider);
				super.setResourceProviders(all.toArray(new IResourceProvider[0]));
			}
		};
	}
	
	@Test
	public void shouldRouteSearchToTheHandlerNamedByProfileAndStampItsProfile() throws Exception {
		// when
		MockHttpServletResponse response = get("/Encounter?_profile=" + VISIT_PROFILE).accept(FhirMediaTypes.JSON).go();
		
		// then
		assertThat(response, isOk());
		verify(encounterHandler, never()).search(any());
		Bundle bundle = readBundleResponse(response);
		assertThat(bundle.getTotal(), equalTo(15));
		assertThat(profilesOf(bundle), everyItem(equalTo(VISIT_PROFILE)));
	}
	
	@Test
	public void shouldKeepRoutingWhenFollowingTheNextPageLink() throws Exception {
		// given
		MockHttpServletResponse first = get("/Encounter?_count=10&_profile=" + VISIT_PROFILE).accept(FhirMediaTypes.JSON)
		        .go();
		assertThat(first, isOk());
		Bundle.BundleLinkComponent next = readBundleResponse(first).getLink("next");
		assertThat(next, notNullValue());
		
		// when
		String nextUrl = next.getUrl();
		MockHttpServletResponse second = get(nextUrl.substring(nextUrl.indexOf('?'))).accept(FhirMediaTypes.JSON).go();
		
		// then
		assertThat(second, isOk());
		Bundle page = readBundleResponse(second);
		assertThat(page.getEntry(), hasSize(5));
		assertThat(profilesOf(page), everyItem(equalTo(VISIT_PROFILE)));
		verify(encounterHandler, never()).search(any());
	}
	
	@Test
	public void shouldNotLeakProfileIntoTheNextRequestWhenASearchFails() throws Exception {
		// given
		when(visitHandler.search(any())).thenThrow(new RuntimeException("boom"));
		MockHttpServletResponse failed = get("/Encounter?_profile=" + VISIT_PROFILE).accept(FhirMediaTypes.JSON).go();
		assertThat(failed.getStatus(), equalTo(500));
		
		// when
		MockHttpServletResponse response = get("/Encounter").accept(FhirMediaTypes.JSON).go();
		
		// then
		assertThat(routingContext.getRequestedProfiles("Encounter").isEmpty(), equalTo(true));
		verify(encounterHandler, atLeastOnce()).search(any());
	}
	
	/**
	 * {@code _profile} on a Location search constrains Location; per FHIR it says nothing about the
	 * Encounters pulled in by {@code _revinclude}, even when it also names an Encounter handler.
	 */
	@Test
	public void shouldNotRouteRevIncludedEncountersByAProfileGivenForTheLocationSearch() throws Exception {
		// when
		MockHttpServletResponse response = get(
		    "/Location?_profile=" + LOCATION_PROFILE + "," + VISIT_PROFILE + "&_revinclude=Encounter:location")
		            .accept(FhirMediaTypes.JSON).go();
		
		// then
		assertThat(response, isOk());
		verify(encounterHandler, atLeastOnce()).search(any());
	}
	
	private static SimpleBundleProvider bundleOf(int count, String prefix) {
		List<IBaseResource> resources = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			Encounter encounter = new Encounter();
			encounter.setId(prefix + i);
			resources.add(encounter);
		}
		return new SimpleBundleProvider(resources);
	}
	
	private static List<String> profilesOf(Bundle bundle) {
		return bundle.getEntry().stream().map(e -> e.getResource().getMeta().getProfile()).flatMap(List::stream)
		        .map(CanonicalType::getValue).collect(Collectors.toList());
	}
}
