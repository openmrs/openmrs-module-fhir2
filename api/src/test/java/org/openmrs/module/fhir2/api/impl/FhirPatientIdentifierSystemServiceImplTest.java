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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.api.dao.FhirPatientIdentifierSystemDao;

@ExtendWith(MockitoExtension.class)
public class FhirPatientIdentifierSystemServiceImplTest {
	
	private static final String PATIENT_IDENTIFIER_URL = "www.example.com";
	
	@Mock
	private FhirPatientIdentifierSystemDao dao;
	
	private FhirPatientIdentifierSystemServiceImpl patientIdentifierSystemService;
	
	private PatientIdentifierType patientIdentifierType;
	
	@BeforeEach
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
}
