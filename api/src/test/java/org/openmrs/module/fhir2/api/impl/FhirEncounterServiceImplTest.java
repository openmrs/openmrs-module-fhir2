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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.util.ArrayList;
import java.util.Collection;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hamcrest.Matchers;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
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
	
	private static final String ENCOUNTER_DATETIME = "2005-01-01T00:00:00.0";
	
	private static final String PATIENT_FAMILY_NAME = "Doe";
	
	private static final String ENCOUNTER_ADDRESS_STATE = "MA";
	
	private static final String PARTICIPANT_IDENTIFIER = "1";
	
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
		when(dao.get(ENCOUNTER_UUID)).thenReturn(openMrsEncounter);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		org.hl7.fhir.r4.model.Encounter fhirEncounter = encounterService.get(ENCOUNTER_UUID);
		assertThat(fhirEncounter, notNullValue());
		assertThat(fhirEncounter.getId(), notNullValue());
		assertThat(fhirEncounter.getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchForEncounter_shouldReturnCollectionOfEncounterByDate() {
		Collection<Encounter> encounters = new ArrayList<>();
		DateRangeParam dateRangeParam = new DateRangeParam(new DateParam(ENCOUNTER_DATETIME));
		
		encounters.add(openMrsEncounter);
		
		fhirEncounter.setId(ENCOUNTER_UUID);
		when(dao.searchForEncounters(argThat(is(dateRangeParam)), any(), any(), any())).thenReturn(encounters);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		
		Collection<org.hl7.fhir.r4.model.Encounter> results = encounterService.searchForEncounters(dateRangeParam, null,
		    null, null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchForEncounter_shouldReturnCollectionOfEncounterByLocation() {
		ReferenceAndListParam location = new ReferenceAndListParam();
		
		location.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(ENCOUNTER_ADDRESS_STATE).setChain(Location.SP_ADDRESS_CITY)));
		
		Collection<Encounter> encounters = new ArrayList<>();
		encounters.add(openMrsEncounter);
		fhirEncounter.setId(ENCOUNTER_UUID);
		
		when(dao.searchForEncounters(any(), argThat(is(location)), any(), any())).thenReturn(encounters);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		
		Collection<org.hl7.fhir.r4.model.Encounter> results = encounterService.searchForEncounters(null, location, null,
		    null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForEncounter_shouldReturnCollectionOfEncounterByParticipant() {
		ReferenceAndListParam participant = new ReferenceAndListParam();
		
		participant.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PARTICIPANT_IDENTIFIER).setChain(Practitioner.SP_IDENTIFIER)));
		
		Collection<Encounter> encounters = new ArrayList<>();
		encounters.add(openMrsEncounter);
		
		fhirEncounter.setId(ENCOUNTER_UUID);
		
		when(dao.searchForEncounters(any(), any(), argThat(is(participant)), any())).thenReturn(encounters);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		
		Collection<org.hl7.fhir.r4.model.Encounter> results = encounterService.searchForEncounters(null, null, participant,
		    null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForEncounter_shouldReturnCollectionOfEncounterBySubject() {
		ReferenceAndListParam subject = new ReferenceAndListParam();
		
		subject.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_FAMILY_NAME).setChain(Patient.SP_FAMILY)));
		
		Collection<Encounter> encounters = new ArrayList<>();
		encounters.add(openMrsEncounter);
		
		fhirEncounter.setId(ENCOUNTER_UUID);
		
		when(dao.searchForEncounters(any(), any(), any(), argThat(is(subject)))).thenReturn(encounters);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		
		Collection<org.hl7.fhir.r4.model.Encounter> results = encounterService.searchForEncounters(null, null, null,
		    subject);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
}
