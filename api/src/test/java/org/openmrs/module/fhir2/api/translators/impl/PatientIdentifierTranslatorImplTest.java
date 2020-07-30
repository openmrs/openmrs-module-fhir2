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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import org.hl7.fhir.r4.model.Identifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.api.FhirPatientService;

@RunWith(MockitoJUnitRunner.class)
public class PatientIdentifierTranslatorImplTest {
	
	private static final String IDENTIFIER_TYPE_UUID = "123456-abcdef-123456";
	
	private static final String IDENTIFIER_TYPE_NAME = "MRN";
	
	private static final String IDENTIFIER_UUID = "654321-fedcba-654321";
	
	private static final String IDENTIFIER = "M10000RN";
	
	private PatientIdentifierTranslatorImpl identifierTranslator;
	
	@Mock
	private FhirPatientService patientService;
	
	@Before
	public void setup() {
		identifierTranslator = new PatientIdentifierTranslatorImpl();
		identifierTranslator.setPatientService(patientService);
	}
	
	@Test
	public void shouldConvertPatientIdentifierToIdentifier() {
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setUuid(IDENTIFIER_TYPE_UUID);
		identifierType.setName(IDENTIFIER_TYPE_NAME);
		identifierType.setRequired(true);
		patientIdentifier.setIdentifierType(identifierType);
		patientIdentifier.setUuid(IDENTIFIER_UUID);
		patientIdentifier.setIdentifier(IDENTIFIER);
		
		Identifier result = identifierTranslator.toFhirResource(patientIdentifier);
		
		assertThat(result, notNullValue());
		assertThat(result.getType().getText(), equalTo(IDENTIFIER_TYPE_NAME));
		assertThat(result.getId(), equalTo(IDENTIFIER_UUID));
		assertThat(result.getValue(), equalTo(IDENTIFIER));
	}
	
	@Test
	public void shouldSetUseToOfficialIfIdentifierIsPreferred() {
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setUuid(IDENTIFIER_TYPE_UUID);
		identifierType.setName(IDENTIFIER_TYPE_NAME);
		patientIdentifier.setPreferred(true);
		patientIdentifier.setIdentifierType(identifierType);
		patientIdentifier.setUuid(IDENTIFIER_UUID);
		patientIdentifier.setIdentifier(IDENTIFIER);
		
		assertThat(identifierTranslator.toFhirResource(patientIdentifier).getUse(), is(Identifier.IdentifierUse.OFFICIAL));
	}
	
	@Test
	public void shouldSetUseToUsualIfIdentifierIsNotPreferred() {
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setUuid(IDENTIFIER_TYPE_UUID);
		identifierType.setName(IDENTIFIER_TYPE_NAME);
		patientIdentifier.setIdentifierType(identifierType);
		patientIdentifier.setUuid(IDENTIFIER_UUID);
		patientIdentifier.setIdentifier(IDENTIFIER);
		
		assertThat(identifierTranslator.toFhirResource(patientIdentifier).getUse(), is(Identifier.IdentifierUse.USUAL));
	}
	
	@Test
	public void shouldConvertNullPatientIdentifierToNull() {
		assertThat(identifierTranslator.toFhirResource(null), nullValue());
	}
	
	@Test
	public void shouldConvertIdentifierToPatientIdentifier() {
		Identifier identifier = new Identifier();
		identifier.setSystem(IDENTIFIER_TYPE_NAME);
		identifier.setId(IDENTIFIER_UUID);
		identifier.setValue(IDENTIFIER);
		
		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setUuid(IDENTIFIER_TYPE_UUID);
		identifierType.setName(IDENTIFIER_TYPE_NAME);
		
		when(patientService.getPatientIdentifierTypeByIdentifier(argThat(equalTo(identifier)))).thenReturn(identifierType);
		
		PatientIdentifier result = identifierTranslator.toOpenmrsType(identifier);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(IDENTIFIER_UUID));
		assertThat(result.getIdentifier(), equalTo(IDENTIFIER));
		assertThat(result.getIdentifierType(), notNullValue());
		assertThat(result.getIdentifierType().getName(), equalTo(IDENTIFIER_TYPE_NAME));
	}
	
	@Test
	public void shouldSetIdentifierAsPreferredIfUseIsOfficial() {
		Identifier identifier = new Identifier();
		identifier.setUse(Identifier.IdentifierUse.OFFICIAL);
		
		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setUuid(IDENTIFIER_TYPE_UUID);
		identifierType.setName(IDENTIFIER_TYPE_NAME);
		
		when(patientService.getPatientIdentifierTypeByIdentifier(argThat(equalTo(identifier)))).thenReturn(identifierType);
		
		assertThat(identifierTranslator.toOpenmrsType(identifier).getPreferred(), is(true));
	}
	
	@Test
	public void shouldSetIdentifierAsNotPreferredIfUseIsNotOfficial() {
		Identifier identifier = new Identifier();
		identifier.setUse(Identifier.IdentifierUse.USUAL);
		
		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setUuid(IDENTIFIER_TYPE_UUID);
		identifierType.setName(IDENTIFIER_TYPE_NAME);
		
		when(patientService.getPatientIdentifierTypeByIdentifier(argThat(equalTo(identifier)))).thenReturn(identifierType);
		
		assertThat(identifierTranslator.toOpenmrsType(identifier).getPreferred(), is(false));
	}
	
	@Test
	public void shouldConvertNullIdentifierToNull() {
		assertThat(identifierTranslator.toOpenmrsType(null), nullValue());
	}
	
}
