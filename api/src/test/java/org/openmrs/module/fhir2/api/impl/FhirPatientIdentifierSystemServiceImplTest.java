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

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Identifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.api.dao.FhirPatientIdentifierSystemDao;

@RunWith(MockitoJUnitRunner.class)
public class FhirPatientIdentifierSystemServiceImplTest {
	
	private static final String PATIENT_IDENTIFIER_URL = "www.example.com";
	
	@Mock
	private FhirPatientIdentifierSystemDao dao;
	
	private FhirPatientIdentifierSystemServiceImpl patientIdentifierSystemService;
	
	private PatientIdentifierType patientIdentifierType;
	
	@Before
	public void setup() {
		patientIdentifierSystemService = new FhirPatientIdentifierSystemServiceImpl();
		patientIdentifierType = new PatientIdentifierType();
		patientIdentifierSystemService.setDao(dao);
	}
	
	@Test
	public void getUrlByPatientIdentifierType_shouldReturnUrl() {
		when(dao.getUrlByPatientIdentifierType(patientIdentifierType)).thenReturn(PATIENT_IDENTIFIER_URL);
		
		String result = patientIdentifierSystemService.getUrlByPatientIdentifierType(patientIdentifierType);
		
		assertThat(result, notNullValue());
	}
	
	@Test
	public void getUrlByPatientIdentifierType_shouldReturnNullWhenPatientIdentifierTypeNotFound() {
		when(dao.getUrlByPatientIdentifierType(patientIdentifierType)).thenReturn(null);
		
		String result = patientIdentifierSystemService.getUrlByPatientIdentifierType(patientIdentifierType);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void getPatientIdentifierTypeByIdentifier_shouldReturnIdentifierTypeWhenPresent() {
		String typeName = "some-type";
		
		Identifier identifier = new Identifier();
		identifier.setType(new CodeableConcept().setText(typeName));
		
		PatientIdentifierType expectedType = new PatientIdentifierType();
		expectedType.setName(typeName);
		
		when(dao.getPatientIdentifierTypeByNameOrUuid(typeName, null)).thenReturn(expectedType);
		
		PatientIdentifierType result = patientIdentifierSystemService.getPatientIdentifierTypeByIdentifier(identifier);
		assertNull(identifier.getSystem());
		assertEquals(expectedType, result);
	}
	
	@Test
	public void getPatientIdentifierTypeByIdentifier_shouldReturnIdentifierTypeWithSystemWhenSystemIsPresent() {
		String systemName = "some-system";
		String expectedTypeName = "some-type";
		String anotherTypeName = "another-type";
		
		Identifier identifierWithSystem = new Identifier();
		identifierWithSystem.setSystem(systemName);
		identifierWithSystem.setType(new CodeableConcept().setText(expectedTypeName));
		
		PatientIdentifierType expectedType = new PatientIdentifierType();
		expectedType.setName(expectedTypeName);
		
		PatientIdentifierType unexpectedType = new PatientIdentifierType();
		unexpectedType.setName(anotherTypeName);
		
		when(dao.getPatientIdentifierTypeByUrl(systemName)).thenReturn(expectedType);
		
		PatientIdentifierType result = patientIdentifierSystemService
		        .getPatientIdentifierTypeByIdentifier(identifierWithSystem);
		assertNotEquals(expectedType, unexpectedType);
		assertEquals(expectedType, result);
	}
	
	@Test
	public void getPatientIdentifierTypeByIdentifier_shouldReturnNullWhenSystemAndIdentifierTypeNotPresent() {
		Identifier identifier = new Identifier();
		identifier.setType(null);
		
		PatientIdentifierType result = patientIdentifierSystemService.getPatientIdentifierTypeByIdentifier(identifier);
		assertNull(identifier.getSystem());
		assertFalse(identifier.hasType());
		assertNull(result);
	}
	
	@Test
	public void getPatientIdentifierTypeByIdentifier_shouldReturnNullWhenIdentifierTypeIsNull() {
		Identifier identifier = new Identifier();
		identifier.setType(null);
		
		PatientIdentifierType result = patientIdentifierSystemService.getPatientIdentifierTypeByIdentifier(identifier);
		assertNull(identifier.getSystem());
		assertTrue(identifier.getType().isEmpty());
		assertNull(result);
	}
	
	@Test
	public void getPatientIdentifierTypeByIdentifier_shouldReturnNullWhenIdentifierTypeTextIsNull() {
		Identifier identifier = new Identifier();
		identifier.setType(new CodeableConcept().setText(null));
		
		PatientIdentifierType result = patientIdentifierSystemService.getPatientIdentifierTypeByIdentifier(identifier);
		assertNull(identifier.getSystem());
		assertTrue(identifier.getType().isEmpty());
		assertNull(result);
	}
	
	@Test
	public void getPatientIdentifierTypeByIdentifier_shouldReturnNullWhenIdentifierTypeTextEmpty() {
		Identifier identifier = new Identifier();
		identifier.setType(new CodeableConcept().setText(""));
		
		PatientIdentifierType result = patientIdentifierSystemService.getPatientIdentifierTypeByIdentifier(identifier);
		assertNull(identifier.getSystem());
		assertTrue(identifier.getType().isEmpty());
		assertNull(result);
	}
}
