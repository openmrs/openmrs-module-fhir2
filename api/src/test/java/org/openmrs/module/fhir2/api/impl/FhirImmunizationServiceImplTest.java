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
import static org.hamcrest.Matchers.equalTo;
import static org.hl7.fhir.r4.model.Patient.SP_IDENTIFIER;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import ca.uhn.fhir.rest.param.ReferenceParam;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;

@RunWith(MockitoJUnitRunner.class)
public class FhirImmunizationServiceImplTest {
	
	private FhirImmunizationServiceImpl service = new FhirImmunizationServiceImpl();
	
	private static final String PATIENT_UUID = "e4948cce-815d-4236-81ea-f49b4b4da2f0";
	
	private static final String PATIENT_IDENTIFIER = "10000ABC";
	
	@Mock
	private PatientService patientService;
	
	private Patient patient = new Patient();
	
	@Before
	public void setup() {
		service.setPatientService(patientService);
		patient.setUuid(PATIENT_UUID);
	}
	
	@Test
	public void getPatient_shouldFetchByIdentifier() {
		// setup
		when(patientService.getPatients(PATIENT_IDENTIFIER, false, 0, 1)).thenReturn(Arrays.asList(patient));
		
		// replay
		Patient patient = service.getPatient(new ReferenceParam(SP_IDENTIFIER, PATIENT_IDENTIFIER));
		
		// verify
		assertThat(patient.getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void getPatient_shouldFetchByUuid() {
		// setup
		when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
		
		// replay
		Patient patient = service.getPatient(new ReferenceParam("", PATIENT_UUID));
		
		// verify
		assertThat(patient.getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void getPatient_shouldThrowWhenNoPatientForIdentifier() {
		// verify
		Assert.assertThrows("No patient could be found for the following OpenMRS identifier: '" + PATIENT_IDENTIFIER + "'.",
		    IllegalArgumentException.class, () -> service.getPatient(new ReferenceParam(SP_IDENTIFIER, PATIENT_IDENTIFIER)));
	}
	
	@Test
	public void getPatient_shouldThrowWhenNoPatientForUuid() {
		// verify
		Assert.assertThrows("No patient could be found for the following OpenMRS UUID: '" + PATIENT_UUID + "'.",
		    IllegalArgumentException.class, () -> service.getPatient(new ReferenceParam("", PATIENT_UUID)));
	}
}
