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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.PatientProgram;
import org.openmrs.module.fhir2.api.dao.FhirEpisodeOfCareDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.EpisodeOfCareTranslator;

/**
 * Tests the CRUD wiring and dispatch predicates of the default EpisodeOfCare handler.
 * Orchestrator-level concerns live in {@code FhirEpisodeOfCareServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PatientProgramBackedEpisodeOfCareHandlerTest {
	
	private static final String EPISODE_OF_CARE_UUID = "9119b9f8-af3d-4ad8-9e2e-2317c3de91c6";
	
	@Mock
	private FhirEpisodeOfCareDao dao;
	
	@Mock
	private EpisodeOfCareTranslator translator;
	
	private PatientProgramBackedEpisodeOfCareHandler handler;
	
	private PatientProgram patientProgram;
	
	private EpisodeOfCare episodeOfCare;
	
	@Before
	public void setUp() {
		handler = new PatientProgramBackedEpisodeOfCareHandler();
		
		handler.setDao(dao);
		handler.setTranslator(translator);
		
		patientProgram = new PatientProgram();
		patientProgram.setUuid(EPISODE_OF_CARE_UUID);
		
		episodeOfCare = new EpisodeOfCare();
		episodeOfCare.setId(EPISODE_OF_CARE_UUID);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeEpisodeOfCareImplicitProfile() {
		assertThat(handler.getImplicitProfile(),
		    equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-episodeofcare"));
	}
	
	@Test
	public void canHandle_shouldAlwaysReturnTrue() {
		assertTrue(handler.canHandle(new EpisodeOfCare()));
	}
	
	@Test
	public void acceptsSearch_shouldAlwaysReturnFalse() {
		assertFalse(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	@Test
	public void search_shouldThrowUnsupportedOperation() {
		assertThrows(UnsupportedOperationException.class, () -> handler.search(new SearchParameterMap()));
	}
	
	// ---- get ----
	
	@Test
	public void get_shouldGetEpisodeOfCareByUuid() {
		when(dao.get(EPISODE_OF_CARE_UUID)).thenReturn(patientProgram);
		when(translator.toFhirResource(patientProgram)).thenReturn(episodeOfCare);
		
		EpisodeOfCare result = handler.get(EPISODE_OF_CARE_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(EPISODE_OF_CARE_UUID));
	}
}
