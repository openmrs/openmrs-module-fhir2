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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.handler.FhirResourceHandler;

/**
 * Orchestrator-level tests for {@link FhirEpisodeOfCareServiceImpl}. Dispatch mechanics are covered
 * in {@link BaseCompositeFhirServiceTest}; backing-specific CRUD lives in
 * {@code PatientProgramBackedEpisodeOfCareHandlerTest}. EpisodeOfCare exposes only {@code @Read},
 * so this class asserts that get reaches the handler through the composite.
 */
@RunWith(MockitoJUnitRunner.class)
public class FhirEpisodeOfCareServiceImplTest {
	
	private static final String EPISODE_OF_CARE_UUID = "9119b9f8-af3d-4ad8-9e2e-2317c3de91c6";
	
	private static final String EPISODE_OF_CARE_PROFILE = "http://fhir.openmrs.org/StructureDefinition/openmrs-episodeofcare";
	
	@Mock
	private FhirResourceHandler<EpisodeOfCare> handler;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirEpisodeOfCareServiceImpl service;
	
	@Before
	public void setup() {
		lenient().when(handler.getImplicitProfile()).thenReturn(EPISODE_OF_CARE_PROFILE);
		
		service = new FhirEpisodeOfCareServiceImpl();
		service.setHandlers(Collections.singletonList(handler));
		service.setGlobalPropertyService(globalPropertyService);
	}
	
	@Test
	public void get_shouldDispatchToHandler() {
		EpisodeOfCare episodeOfCare = new EpisodeOfCare();
		episodeOfCare.setId(EPISODE_OF_CARE_UUID);
		when(handler.get(EPISODE_OF_CARE_UUID)).thenReturn(episodeOfCare);
		
		EpisodeOfCare result = service.get(EPISODE_OF_CARE_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(EPISODE_OF_CARE_UUID));
		verify(handler).get(EPISODE_OF_CARE_UUID);
	}
	
	@Test
	public void get_shouldStampHandlerProfileOnResult() {
		EpisodeOfCare episodeOfCare = new EpisodeOfCare();
		episodeOfCare.setId(EPISODE_OF_CARE_UUID);
		when(handler.get(EPISODE_OF_CARE_UUID)).thenReturn(episodeOfCare);
		
		EpisodeOfCare result = service.get(EPISODE_OF_CARE_UUID);
		
		assertThat(result.getMeta().getProfile().get(0).getValue(), equalTo(EPISODE_OF_CARE_PROFILE));
	}
}
