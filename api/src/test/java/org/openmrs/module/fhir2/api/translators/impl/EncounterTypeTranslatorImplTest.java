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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.EncounterType;
import org.openmrs.api.EncounterService;
import org.openmrs.module.fhir2.FhirConstants;

@RunWith(MockitoJUnitRunner.class)
public class EncounterTypeTranslatorImplTest {
	
	private static final String ENCOUNTER_TYPE_UUID = "9a3e940d-24d4-4f39-b333-f1fe1aea8ed9";
	
	private static final String ENCOUNTER_TYPE_NAME = "My Encounter Type";
	
	@Mock
	private EncounterService encounterService;
	
	private EncounterTypeTranslatorImpl encounterTypeTranslator;
	
	@Before
	public void setup() {
		encounterTypeTranslator = new EncounterTypeTranslatorImpl();
		encounterTypeTranslator.setEncounterService(encounterService);
	}
	
	@Test
	public void toFhirResource_shouldMapEncounterTypeToCodeableConcepts() {
		EncounterType encounterType = new EncounterType();
		encounterType.setUuid(ENCOUNTER_TYPE_UUID);
		encounterType.setName(ENCOUNTER_TYPE_NAME);
		
		List<CodeableConcept> encounterTypes = encounterTypeTranslator.toFhirResource(encounterType);
		
		assertThat(encounterTypes, notNullValue());
		assertThat(encounterTypes, not(empty()));
		
		Coding fhirEncounterType = encounterTypes.get(0).getCodingFirstRep();
		
		assertThat(fhirEncounterType, notNullValue());
		assertThat(fhirEncounterType.getSystem(), equalTo(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI));
		assertThat(fhirEncounterType.getCode(), equalTo(ENCOUNTER_TYPE_UUID));
		assertThat(fhirEncounterType.getDisplay(), equalTo(ENCOUNTER_TYPE_NAME));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfEncounterTypeIsNull() {
		List<CodeableConcept> encounterTypes = encounterTypeTranslator.toFhirResource(null);
		
		assertThat(encounterTypes, nullValue());
	}
	
	@Test
	public void toOpenmrsObject_shouldMapCodeableConceptsToEncounterType() {
		EncounterType encounterType = new EncounterType();
		encounterType.setUuid(ENCOUNTER_TYPE_UUID);
		encounterType.setName(ENCOUNTER_TYPE_NAME);
		
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI).setCode(ENCOUNTER_TYPE_UUID)
		        .setDisplay(ENCOUNTER_TYPE_NAME);
		when(encounterService.getEncounterTypeByUuid(ENCOUNTER_TYPE_UUID)).thenReturn(encounterType);
		
		EncounterType result = encounterTypeTranslator.toOpenmrsType(Collections.singletonList(codeableConcept));
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(ENCOUNTER_TYPE_UUID));
		assertThat(result.getName(), equalTo(ENCOUNTER_TYPE_NAME));
	}
	
	@Test
	public void toOpenmrsObject_shouldNotRequireEncounterTypeName() {
		EncounterType encounterType = new EncounterType();
		encounterType.setUuid(ENCOUNTER_TYPE_UUID);
		encounterType.setName(ENCOUNTER_TYPE_NAME);
		
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI).setCode(ENCOUNTER_TYPE_UUID);
		when(encounterService.getEncounterTypeByUuid(ENCOUNTER_TYPE_UUID)).thenReturn(encounterType);
		
		EncounterType result = encounterTypeTranslator.toOpenmrsType(Collections.singletonList(codeableConcept));
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(ENCOUNTER_TYPE_UUID));
		assertThat(result.getName(), equalTo(ENCOUNTER_TYPE_NAME));
	}
	
	@Test
	public void toOpenmrsObject_shouldReturnNullWhenEncounterTypeIsNull() {
		EncounterType result = encounterTypeTranslator.toOpenmrsType(null);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsObject_shouldReturnNullWhenEncounterTypeIsEmpty() {
		EncounterType result = encounterTypeTranslator.toOpenmrsType(new ArrayList<>());
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsObject_shouldReturnNullWhenEncounterTypeNotFound() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI).setCode(ENCOUNTER_TYPE_UUID)
		        .setDisplay(ENCOUNTER_TYPE_NAME);
		when(encounterService.getEncounterTypeByUuid(ENCOUNTER_TYPE_UUID)).thenReturn(null);
		
		EncounterType result = encounterTypeTranslator.toOpenmrsType(Collections.singletonList(codeableConcept));
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsObject_shouldReturnNullWhenEncounterTypeSystemIsMissing() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setCode(ENCOUNTER_TYPE_UUID).setDisplay(ENCOUNTER_TYPE_NAME);
		
		EncounterType result = encounterTypeTranslator.toOpenmrsType(Collections.singletonList(codeableConcept));
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsObject_shouldReturnNullWhenEncounterTypeSystemIsIncorrect() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem("http://mygreatesystem.com").setCode(ENCOUNTER_TYPE_UUID)
		        .setDisplay(ENCOUNTER_TYPE_NAME);
		
		EncounterType result = encounterTypeTranslator.toOpenmrsType(Collections.singletonList(codeableConcept));
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsObject_shouldReturnNullWhenEncounterTypeCodeIsMissing() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI).setDisplay(ENCOUNTER_TYPE_NAME);
		
		EncounterType result = encounterTypeTranslator.toOpenmrsType(Collections.singletonList(codeableConcept));
		
		assertThat(result, nullValue());
	}
}
