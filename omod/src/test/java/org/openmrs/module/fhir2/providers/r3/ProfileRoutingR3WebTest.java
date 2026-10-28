/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;
import org.openmrs.module.fhir2.api.impl.FhirEncounterServiceImpl;
import org.openmrs.module.fhir2.api.util.ProfileRoutingContext;
import org.openmrs.module.fhir2.web.servlet.FhirR3RestServlet;
import org.openmrs.module.fhir2.web.servlet.FhirRestServlet;
import org.openmrs.module.fhir2.web.util.ProfileRoutingInterceptor;
import org.springframework.mock.web.MockHttpServletResponse;

/** Drives {@code _profile} routing and profile stamping end to end through the R3 servlet. */
@RunWith(MockitoJUnitRunner.Silent.class)
public class ProfileRoutingR3WebTest extends BaseFhirR3ResourceProviderWebTest<EncounterFhirResourceProvider, Encounter> {
	
	private static final String ENCOUNTER_PROFILE = "http://fhir.openmrs.org/StructureDefinition/openmrs-encounter";
	
	private static final String VISIT_PROFILE = "http://fhir.openmrs.org/StructureDefinition/openmrs-visit";
	
	private ProfileRoutingContext routingContext;
	
	private FhirResourceHandler<org.hl7.fhir.r4.model.Encounter> encounterHandler;
	
	private EncounterFhirResourceProvider encounterProvider;
	
	@Override
	@Before
	@SuppressWarnings("unchecked")
	public void setup() throws javax.servlet.ServletException {
		routingContext = new ProfileRoutingContext();
		
		encounterHandler = mock(FhirResourceHandler.class);
		FhirResourceHandler<org.hl7.fhir.r4.model.Encounter> visitHandler = mock(FhirResourceHandler.class);
		lenient().when(encounterHandler.getImplicitProfile()).thenReturn(ENCOUNTER_PROFILE);
		lenient().when(visitHandler.getImplicitProfile()).thenReturn(VISIT_PROFILE);
		lenient().when(encounterHandler.acceptsSearch(any())).thenReturn(true);
		lenient().when(visitHandler.acceptsSearch(any())).thenReturn(true);
		lenient().when(encounterHandler.search(any())).thenAnswer(i -> bundleOf(2, "enc-"));
		lenient().when(visitHandler.search(any())).thenAnswer(i -> bundleOf(3, "visit-"));
		
		FhirGlobalPropertyService globalPropertyService = mock(FhirGlobalPropertyService.class);
		lenient().when(globalPropertyService.getGlobalPropertyAsInteger(any(), any(Integer.class))).thenReturn(10);
		
		final ProfileRoutingContext context = routingContext;
		final List<FhirResourceHandler<org.hl7.fhir.r4.model.Encounter>> handlers = Arrays.asList(encounterHandler,
		    visitHandler);
		FhirEncounterServiceImpl encounterService = new FhirEncounterServiceImpl() {
			
			{
				setHandlers(handlers);
				setProfileRoutingContext(context);
				setGlobalPropertyService(globalPropertyService);
			}
		};
		
		encounterProvider = new EncounterFhirResourceProvider();
		encounterProvider.setEncounterService(encounterService);
		
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
		
		return new FhirR3RestServlet() {
			
			@Override
			public void setProfileRoutingInterceptor(ProfileRoutingInterceptor ignored) {
				super.setProfileRoutingInterceptor(interceptor);
			}
		};
	}
	
	@Test
	public void shouldRouteR3SearchByProfileAndCarryTheStampedProfile() throws Exception {
		// when
		MockHttpServletResponse response = get("/Encounter?_profile=" + VISIT_PROFILE).accept(FhirMediaTypes.JSON).go();
		
		// then
		assertThat(response, isOk());
		verify(encounterHandler, never()).search(any());
		Bundle bundle = (Bundle) readBundleResponse(response);
		assertThat(bundle.getEntry().size(), equalTo(3));
		List<String> profiles = bundle.getEntry().stream().map(e -> e.getResource().getMeta().getProfile())
		        .flatMap(List::stream).map(UriType::getValue).collect(Collectors.toList());
		assertThat(profiles, everyItem(equalTo(VISIT_PROFILE)));
		assertThat(profiles.size(), equalTo(3));
	}
	
	private static SimpleBundleProvider bundleOf(int count, String prefix) {
		List<IBaseResource> resources = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			org.hl7.fhir.r4.model.Encounter encounter = new org.hl7.fhir.r4.model.Encounter();
			encounter.setId(prefix + i);
			resources.add(encounter);
		}
		return new SimpleBundleProvider(resources);
	}
}
