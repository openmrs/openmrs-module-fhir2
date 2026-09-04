/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import java.util.HashMap;
import java.util.Map;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.api.util.ProfileRoutingContext;

public class ProfileRoutingInterceptorTest {
	
	private static final String OPENMRS_ENCOUNTER_PROFILE = "http://fhir.openmrs.org/StructureDefinition/openmrs-encounter";
	
	private static final String OPENMRS_VISIT_PROFILE = "http://fhir.openmrs.org/StructureDefinition/openmrs-visit";
	
	private ProfileRoutingContext context;
	
	private ProfileRoutingInterceptor interceptor;
	
	@Before
	public void setUp() {
		context = new ProfileRoutingContext();
		interceptor = new ProfileRoutingInterceptor();
		interceptor.setProfileRoutingContext(context);
	}
	
	@Test
	public void shouldCaptureNothingWhenNoProfileRequested() {
		interceptor.captureRequestedProfiles(requestWithParameters(new HashMap<>()));
		
		assertThat(context.getRequestedProfiles(), empty());
	}
	
	@Test
	public void shouldCaptureASingleRequestedProfile() {
		interceptor.captureRequestedProfiles(requestWithProfile(OPENMRS_VISIT_PROFILE));
		
		assertThat(context.getRequestedProfiles(), contains(OPENMRS_VISIT_PROFILE));
	}
	
	@Test
	public void shouldFlattenACommaSeparatedProfileList() {
		interceptor.captureRequestedProfiles(requestWithProfile(OPENMRS_ENCOUNTER_PROFILE + "," + OPENMRS_VISIT_PROFILE));
		
		assertThat(context.getRequestedProfiles(), contains(OPENMRS_ENCOUNTER_PROFILE, OPENMRS_VISIT_PROFILE));
	}
	
	@Test
	public void shouldFlattenRepeatedProfileParameters() {
		interceptor.captureRequestedProfiles(requestWithProfile(OPENMRS_ENCOUNTER_PROFILE, OPENMRS_VISIT_PROFILE));
		
		assertThat(context.getRequestedProfiles(), contains(OPENMRS_ENCOUNTER_PROFILE, OPENMRS_VISIT_PROFILE));
	}
	
	@Test
	public void shouldIgnoreBlankProfileValues() {
		interceptor.captureRequestedProfiles(requestWithProfile("  ", "", OPENMRS_VISIT_PROFILE + " "));
		
		assertThat(context.getRequestedProfiles(), contains(OPENMRS_VISIT_PROFILE));
	}
	
	@Test
	public void shouldReplaceProfilesCapturedForAnEarlierRequest() {
		interceptor.captureRequestedProfiles(requestWithProfile(OPENMRS_ENCOUNTER_PROFILE));
		interceptor.captureRequestedProfiles(requestWithProfile(OPENMRS_VISIT_PROFILE));
		
		assertThat(context.getRequestedProfiles(), contains(OPENMRS_VISIT_PROFILE));
	}
	
	/**
	 * A request without {@code _profile} must not inherit whatever the previous request on this thread
	 * asked for.
	 */
	@Test
	public void shouldClearProfilesWhenAProfilelessRequestFollowsAProfiledOne() {
		interceptor.captureRequestedProfiles(requestWithProfile(OPENMRS_VISIT_PROFILE));
		interceptor.captureRequestedProfiles(requestWithParameters(new HashMap<>()));
		
		assertThat(context.getRequestedProfiles(), empty());
	}
	
	@Test
	public void shouldClearCapturedProfilesOnceProcessingCompletes() {
		RequestDetails requestDetails = requestWithProfile(OPENMRS_VISIT_PROFILE);
		interceptor.captureRequestedProfiles(requestDetails);
		
		interceptor.clearRequestedProfiles(requestDetails);
		
		assertThat(context.getRequestedProfiles(), empty());
	}
	
	private static RequestDetails requestWithProfile(String... values) {
		Map<String, String[]> parameters = new HashMap<>();
		parameters.put("_profile", values);
		return requestWithParameters(parameters);
	}
	
	private static RequestDetails requestWithParameters(Map<String, String[]> parameters) {
		ServletRequestDetails requestDetails = new ServletRequestDetails();
		requestDetails.setParameters(parameters);
		return requestDetails;
	}
}
