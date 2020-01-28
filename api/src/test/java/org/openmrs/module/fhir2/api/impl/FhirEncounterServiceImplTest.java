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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Encounter;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.openmrs.module.fhir2.api.translators.EncounterTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirEncounterServiceImplTest {
	
	private static final String ENCOUNTER_UUID = "344kk343-45hj45-34jk34-34ui33";

	private static final String PATIENT_IDENTIFIER = "1003GH";

	@Mock
	private FhirEncounterDao dao;
	
	@Mock
	private EncounterTranslator encounterTranslator;
	
	private FhirEncounterServiceImpl encounterService;
	
	private org.openmrs.Encounter openMrsEncounter;
	
	private org.hl7.fhir.r4.model.Encounter fhirEncounter;
	
	@Before
	public void setUp() {
		encounterService = new FhirEncounterServiceImpl();
		encounterService.setDao(dao);
		encounterService.setTranslator(encounterTranslator);
		
		openMrsEncounter = new Encounter();
		openMrsEncounter.setUuid(ENCOUNTER_UUID);
		
		fhirEncounter = new org.hl7.fhir.r4.model.Encounter();
		fhirEncounter.setId(ENCOUNTER_UUID);
	}
	
	@Test
	public void shouldGetEncounterByUuid() {
		when(dao.getEncounterByUuid(ENCOUNTER_UUID)).thenReturn(openMrsEncounter);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		org.hl7.fhir.r4.model.Encounter fhirEncounter = encounterService.getEncounterByUuid(ENCOUNTER_UUID);
		assertThat(fhirEncounter, notNullValue());
		assertThat(fhirEncounter.getId(), notNullValue());
		assertThat(fhirEncounter.getId(), equalTo(ENCOUNTER_UUID));
	}

	@Test
	public void findEncountersByPatientIdentifier_shouldReturnCollectionOfEncounters() {
		when(dao.findEncountersByPatientIdentifier(PATIENT_IDENTIFIER)).thenReturn(Collections.singletonList(openMrsEncounter));
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);

		Collection<org.hl7.fhir.r4.model.Encounter> results = encounterService.findEncountersByPatientIdentifier(PATIENT_IDENTIFIER);
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		assertThat(results.stream().findFirst().isPresent(), is(true));
	}
	
}
