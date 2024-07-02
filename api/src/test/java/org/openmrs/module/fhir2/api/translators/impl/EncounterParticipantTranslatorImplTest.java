/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.Provider;
import org.openmrs.api.EncounterService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;

@RunWith(MockitoJUnitRunner.class)
public class EncounterParticipantTranslatorImplTest {
	
	private static final String PROVIDER_UUID = "122344-234xx23-2323kk-232k2h2";
	
	private static final String PROVIDER_URI = FhirConstants.PRACTITIONER + "/" + PROVIDER_UUID;
	
	@Mock
	private FhirPractitionerDao practitionerDao;
	
	@Mock
	private EncounterService encounterService;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private EncounterParticipantTranslatorImpl participantTranslator;
	
	private EncounterProvider encounterProvider;
	
	private Encounter.EncounterParticipantComponent encounterParticipantComponent;
	
	private Provider provider;
	
	@Before
	public void setUp() {
		participantTranslator = new EncounterParticipantTranslatorImpl();
		participantTranslator.setPractitionerDao(practitionerDao);
		participantTranslator.setGlobalPropertyService(globalPropertyService);
		
		encounterProvider = new EncounterProvider();
		provider = new Provider();
		provider.setUuid(PROVIDER_UUID);
		encounterProvider.setProvider(provider);
		participantTranslator.setEncounterService(encounterService);
		
		encounterParticipantComponent = new Encounter.EncounterParticipantComponent();
		Reference reference = new Reference(PROVIDER_URI);
		encounterParticipantComponent.setIndividual(reference);
		
		Practitioner practitioner = new Practitioner();
		practitioner.setId(PROVIDER_UUID);
	}
	
	@Test
	public void shouldTranslateEncounterProviderToFhirType() {
		Encounter.EncounterParticipantComponent result = participantTranslator.toFhirResource(encounterProvider);
		assertThat(result, notNullValue());
		assertThat(result.hasIndividual(), is(true));
	}
	
	@Test
	public void shouldTranslateEncounterProviderToFhirTypeWithCorrectIndividualReference() {
		Encounter.EncounterParticipantComponent result = participantTranslator.toFhirResource(encounterProvider);
		assertThat(result, notNullValue());
		assertThat(result.getIndividual(), notNullValue());
		assertThat(result.getIndividual().getReference(), equalTo(PROVIDER_URI));
	}
	
	@Test
	public void shouldTranslateEncounterParticipantToOpenMrsType() {
		when(encounterService.getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID))
		        .thenReturn(new EncounterRole());
		EncounterProvider encounterProvider = participantTranslator.toOpenmrsType(new EncounterProvider(),
		    encounterParticipantComponent);
		assertThat(encounterProvider, notNullValue());
	}
	
	@Test
	public void shouldTranslateEncounterParticipantToEncounterProviderWithCorrectProvider() {
		when(practitionerDao.get(PROVIDER_UUID)).thenReturn(provider);
		when(encounterService.getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID))
		        .thenReturn(new EncounterRole());
		
		EncounterProvider encounterProvider = participantTranslator.toOpenmrsType(new EncounterProvider(),
		    encounterParticipantComponent);
		
		assertThat(encounterProvider, notNullValue());
		assertThat(encounterProvider.getProvider(), notNullValue());
		assertThat(encounterProvider.getProvider().getUuid(), equalTo(PROVIDER_UUID));
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldThrowExceptionWhenEncounterParticipantIsNull() {
		participantTranslator.toOpenmrsType(new EncounterProvider(), null);
	}
	
	@Test
	public void toFhirResource_shouldThrowExceptionWhenUnknownRoleIsNull() {
		assertThrows(IllegalStateException.class,
		    () -> participantTranslator.toOpenmrsType(new EncounterProvider(), encounterParticipantComponent));
	}
}
