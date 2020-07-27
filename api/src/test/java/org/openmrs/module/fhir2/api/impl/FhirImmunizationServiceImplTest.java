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

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
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
	public void getPatient_shouldFetchByIdentifierFirst() {
		// setup
		when(patientService.getPatients(PATIENT_IDENTIFIER, 0, 1)).thenReturn(Arrays.asList(patient));
		
		// replay
		ReferenceAndListParam param = new ReferenceAndListParam();
		param.addValue(new ReferenceOrListParam().add(new ReferenceParam(SP_IDENTIFIER, PATIENT_IDENTIFIER)));
		param.addValue(new ReferenceOrListParam().add(new ReferenceParam("", PATIENT_UUID)));
		
		Patient patient = service.getPatient(param);
		
		// verify
		assertThat(patient.getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void getPatient_shouldFetchByUuidAsLastResort() {
		// setup
		when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
		
		// replay
		ReferenceAndListParam param = new ReferenceAndListParam();
		param.addValue(new ReferenceOrListParam().add(new ReferenceParam("", PATIENT_UUID)));
		
		Patient patient = service.getPatient(param);
		
		// verify
		assertThat(patient.getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void getPatient_shouldThrowWhenNoPatientForIdentifier() {
		// replay
		ReferenceAndListParam param = new ReferenceAndListParam();
		param.addValue(new ReferenceOrListParam().add(new ReferenceParam(SP_IDENTIFIER, PATIENT_IDENTIFIER)));
		
		// verify
		Assert.assertThrows("No patient could be found for the following OpenMRS identifier: '" + PATIENT_IDENTIFIER + "'.",
		    IllegalArgumentException.class, () -> service.getPatient(param));
	}
	
	@Test
	public void getPatient_shouldThrowWhenNoPatientForUuid() {
		// replay
		ReferenceAndListParam param = new ReferenceAndListParam();
		param.addValue(new ReferenceOrListParam().add(new ReferenceParam("", PATIENT_UUID)));
		
		// verify
		Assert.assertThrows("No patient could be found for the following OpenMRS UUID: '" + PATIENT_UUID + "'.",
		    IllegalArgumentException.class, () -> service.getPatient(param));
	}
	
	@Test
	public void getPatient_shouldThrowWhenPatientIdentifierAndUuidDoNotMatch() {
		// setup
		Patient patientWithOtherUuid = new Patient();
		patientWithOtherUuid.setUuid("a3bec526-287a-45a4-b01a-030cdea96023");
		when(patientService.getPatients(PATIENT_IDENTIFIER, 0, 1)).thenReturn(Arrays.asList(patientWithOtherUuid));
		
		// replay
		ReferenceAndListParam param = new ReferenceAndListParam();
		param.addValue(new ReferenceOrListParam().add(new ReferenceParam(SP_IDENTIFIER, PATIENT_IDENTIFIER)));
		param.addValue(new ReferenceOrListParam().add(new ReferenceParam("", PATIENT_UUID)));
		
		// verify
		Assert.assertThrows("The provided UUID '" + PATIENT_UUID + "' is not that of the patient identified by '"
		        + PATIENT_IDENTIFIER + "'.",
		    IllegalArgumentException.class, () -> service.getPatient(param));
	}
	
}
