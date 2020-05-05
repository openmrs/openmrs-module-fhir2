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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Encounter;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;

@RunWith(MockitoJUnitRunner.class)
public class EncounterReferenceTranslatorImplTest {
	
	private static final String ENCOUNTER_UUID = "12345-abcde-12345";
	
	@Mock
	private FhirEncounterDao dao;
	
	private EncounterReferenceTranslatorImpl encounterReferenceTranslator;
	
	@Before
	public void setup() {
		encounterReferenceTranslator = new EncounterReferenceTranslatorImpl();
		encounterReferenceTranslator.setEncounterDao(dao);
	}
	
	@Test
	public void toFhirResource_shouldConvertEncounterToReference() {
		Encounter encounter = new Encounter();
		encounter.setUuid(ENCOUNTER_UUID);
		
		Reference result = encounterReferenceTranslator.toFhirResource(encounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(FhirConstants.ENCOUNTER));
		assertThat(encounterReferenceTranslator.getReferenceId(result), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfEncounterNull() {
		Reference result = encounterReferenceTranslator.toFhirResource(null);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldConvertReferenceToEncounter() {
		Reference encounterReference = new Reference().setReference(FhirConstants.ENCOUNTER + "/" + ENCOUNTER_UUID)
		        .setType(FhirConstants.ENCOUNTER).setIdentifier(new Identifier().setValue(ENCOUNTER_UUID));
		Encounter encounter = new Encounter();
		encounter.setUuid(ENCOUNTER_UUID);
		when(dao.get(ENCOUNTER_UUID)).thenReturn(encounter);
		
		Encounter result = encounterReferenceTranslator.toOpenmrsType(encounterReference);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfReferenceNull() {
		Encounter result = encounterReferenceTranslator.toOpenmrsType(null);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfEncounterHasNoIdentifier() {
		Reference encounterReference = new Reference().setReference(FhirConstants.ENCOUNTER + "/" + ENCOUNTER_UUID)
		        .setType(FhirConstants.ENCOUNTER);
		
		Encounter result = encounterReferenceTranslator.toOpenmrsType(encounterReference);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfEncounterIdentifierHasNoValue() {
		Reference encounterReference = new Reference().setReference(FhirConstants.ENCOUNTER + "/" + ENCOUNTER_UUID)
		        .setType(FhirConstants.ENCOUNTER).setIdentifier(new Identifier());
		
		Encounter result = encounterReferenceTranslator.toOpenmrsType(encounterReference);
		
		assertThat(result, nullValue());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void toOpenmrsType_shouldThrowExceptionIfReferenceIsntForEncounter() {
		Reference reference = new Reference().setReference("Unknown" + "/" + ENCOUNTER_UUID).setType("Unknown");
		
		encounterReferenceTranslator.toOpenmrsType(reference);
	}
}
