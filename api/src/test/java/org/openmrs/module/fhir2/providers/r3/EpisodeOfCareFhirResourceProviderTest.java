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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.EpisodeOfCare;
import org.hl7.fhir.dstu3.model.IdType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirEpisodeOfCareService;

@RunWith(MockitoJUnitRunner.class)
public class EpisodeOfCareFhirResourceProviderTest {
	
	private static final String EPISODE_OF_CARE_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";
	
	private static final String WRONG_EPISODE_OF_CARE_UUID = "b0a4b94e-cf7d-4a61-a3a1-4ca647580e2f";
	
	@Mock
	private FhirEpisodeOfCareService episodeOfCareService;
	
	private EpisodeOfCareFhirResourceProvider resourceProvider;
	
	private org.hl7.fhir.r4.model.EpisodeOfCare episodeOfCare;
	
	@Before
	public void setup() {
		resourceProvider = new EpisodeOfCareFhirResourceProvider();
		resourceProvider.setEpisodeOfCareService(episodeOfCareService);
	}
	
	@Before
	public void initEpisodeOfCare() {
		episodeOfCare = new org.hl7.fhir.r4.model.EpisodeOfCare();
		episodeOfCare.setId(EPISODE_OF_CARE_UUID);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(EpisodeOfCare.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(EpisodeOfCare.class.getName()));
	}
	
	@Test
	public void getEpisodeOfCareById_shouldReturnEpisodeOfCare() {
		IdType id = new IdType();
		id.setValue(EPISODE_OF_CARE_UUID);
		when(episodeOfCareService.get(EPISODE_OF_CARE_UUID)).thenReturn(episodeOfCare);
		
		EpisodeOfCare result = resourceProvider.getEpisodeOfCareById(id);
		assertThat(result.isResource(), is(true));
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(EPISODE_OF_CARE_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getEpisodeOfCareByWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_EPISODE_OF_CARE_UUID);
		assertThat(resourceProvider.getEpisodeOfCareById(idType).isResource(), is(true));
		assertThat(resourceProvider.getEpisodeOfCareById(idType), nullValue());
	}
}
