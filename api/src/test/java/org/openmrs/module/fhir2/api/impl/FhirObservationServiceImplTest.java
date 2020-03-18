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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.hl7.fhir.r4.model.Observation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.translators.ObservationTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirObservationServiceImplTest {
	
	private static final String OBS_UUID = "12345-abcde-12345";
	
	@Mock
	FhirObservationDao dao;
	
	@Mock
	ObservationTranslator observationTranslator;
	
	private FhirObservationServiceImpl fhirObservationService;
	
	@Before
	public void setup() {
		fhirObservationService = new FhirObservationServiceImpl();
		fhirObservationService.setDao(dao);
		fhirObservationService.setObservationTranslator(observationTranslator);
	}
	
	@Test
	public void getObservationByUuid_shouldReturnObservationByUuid() {
		Obs obs = new Obs();
		obs.setUuid(OBS_UUID);
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		when(dao.getObsByUuid(OBS_UUID)).thenReturn(obs);
		when(observationTranslator.toFhirResource(obs)).thenReturn(observation);
		
		Observation result = fhirObservationService.getObservationByUuid(OBS_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(OBS_UUID));
	}
	
	@Test
	public void searchForObservations_shouldReturnObservationsByParameters() {
		Collection<Obs> obs = new ArrayList<>();
		Obs ob = new Obs();
		ob.setUuid(OBS_UUID);
		obs.add(ob);
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		when(dao.searchForObservations(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(obs);
		when(observationTranslator.toFhirResource(ob)).thenReturn(observation);
		
		Collection<Observation> results = fhirObservationService.searchForObservations(null, null, null, null, null, null,
		    null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("id", equalTo(OBS_UUID))));
	}
}
