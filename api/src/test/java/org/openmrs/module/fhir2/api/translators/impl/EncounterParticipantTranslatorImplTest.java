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
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
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
import org.openmrs.Provider;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;

@RunWith(MockitoJUnitRunner.class)
public class EncounterParticipantTranslatorImplTest {
	
	private static final String PROVIDER_UUID = "122344-234xx23-2323kk-232k2h2";
	
	private static final String PROVIDER_URI = FhirConstants.PRACTITIONER + "/" + PROVIDER_UUID;
	
	@Mock
	FhirPractitionerService practitionerService;
	
	@Mock
	PractitionerTranslator practitionerTranslator;
	
	private EncounterParticipantTranslatorImpl participantTranslator;
	
	private EncounterProvider encounterProvider;
	
	private Encounter.EncounterParticipantComponent encounterParticipantComponent;
	
	private Provider provider;
	
	private Practitioner practitioner;
	
	@Before
	public void setUp() {
		participantTranslator = new EncounterParticipantTranslatorImpl();
		participantTranslator.setPractitionerTranslator(practitionerTranslator);
		participantTranslator.setPractitionerService(practitionerService);
		
		encounterProvider = new EncounterProvider();
		provider = new Provider();
		provider.setUuid(PROVIDER_UUID);
		encounterProvider.setProvider(provider);
		
		encounterParticipantComponent = new Encounter.EncounterParticipantComponent();
		Reference reference = new Reference(PROVIDER_URI);
		encounterParticipantComponent.setIndividual(reference);
		
		practitioner = new Practitioner();
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
		EncounterProvider encounterProvider = participantTranslator.toOpenmrsType(new EncounterProvider(),
		    encounterParticipantComponent);
		assertThat(encounterProvider, notNullValue());
	}
	
	@Test
	public void shouldTranslateEncounterParticipantToEncounterProviderWithCorrectProvider() {
		when(practitionerService.getPractitionerByUuid(PROVIDER_UUID)).thenReturn(practitioner);
		when(practitionerTranslator.toOpenmrsType(practitioner)).thenReturn(provider);
		EncounterProvider encounterProvider = participantTranslator.toOpenmrsType(new EncounterProvider(),
		    encounterParticipantComponent);
		assertThat(encounterProvider, notNullValue());
		assertThat(encounterProvider.getProvider(), notNullValue());
		assertThat(encounterProvider.getProvider().getUuid(), equalTo(PROVIDER_UUID));
	}
	
	@Test
	public void shouldReturnInstanceOfEncounterProviderWhenEncounterParticipantIsNull() {
		EncounterProvider encounterProvider = participantTranslator.toOpenmrsType(new EncounterProvider(), null);
		assertThat(encounterProvider, notNullValue());
		assertThat(encounterProvider, is(instanceOf(EncounterProvider.class)));
	}
	
}
