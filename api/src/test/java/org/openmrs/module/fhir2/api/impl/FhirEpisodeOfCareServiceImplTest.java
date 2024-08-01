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
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.PatientProgram;
import org.openmrs.module.fhir2.api.dao.FhirEpisodeOfCareDao;
import org.openmrs.module.fhir2.api.translators.EpisodeOfCareTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirEpisodeOfCareServiceImplTest {
	
	private static final String EPISODE_OF_CARE_UUID = "9119b9f8-af3d-4ad8-9e2e-2317c3de91c6";
	
	@Mock
	private FhirEpisodeOfCareDao dao;
	
	@Mock
	private EpisodeOfCareTranslator episodeOfCareTranslator;
	
	private FhirEpisodeOfCareServiceImpl episodeOfCareService;
	
	private PatientProgram patientProgram;
	
	private EpisodeOfCare episodeOfCare;
	
	@Before
	public void setUp() {
		episodeOfCareService = new FhirEpisodeOfCareServiceImpl();
		
		episodeOfCareService.setDao(dao);
		episodeOfCareService.setTranslator(episodeOfCareTranslator);
		
		patientProgram = new PatientProgram();
		patientProgram.setUuid(EPISODE_OF_CARE_UUID);
		
		episodeOfCare = new EpisodeOfCare();
		episodeOfCare.setId(EPISODE_OF_CARE_UUID);
	}
	
	@Test
	public void get_shouldGetEncounterByUuid() {
		when(dao.get(EPISODE_OF_CARE_UUID)).thenReturn(patientProgram);
		when(episodeOfCareTranslator.toFhirResource(patientProgram)).thenReturn(episodeOfCare);
		
		EpisodeOfCare actualEpisodeOfCare = episodeOfCareService.get(EPISODE_OF_CARE_UUID);
		
		assertThat(actualEpisodeOfCare, notNullValue());
		assertThat(actualEpisodeOfCare.getId(), notNullValue());
		assertThat(actualEpisodeOfCare.getId(), equalTo(EPISODE_OF_CARE_UUID));
	}
}
