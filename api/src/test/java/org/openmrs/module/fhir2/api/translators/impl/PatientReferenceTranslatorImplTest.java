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
import org.openmrs.Patient;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;

@RunWith(MockitoJUnitRunner.class)
public class PatientReferenceTranslatorImplTest {
	
	private static final String PATIENT_UUID = "12345-abcde-12345";
	
	@Mock
	private FhirPatientDao dao;
	
	private PatientReferenceTranslatorImpl patientReferenceTranslator;
	
	@Before
	public void setup() {
		patientReferenceTranslator = new PatientReferenceTranslatorImpl();
		patientReferenceTranslator.setPatientDao(dao);
	}
	
	@Test
	public void toFhirResource_shouldConvertEncounterToReference() {
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		
		Reference result = patientReferenceTranslator.toFhirResource(patient);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(FhirConstants.PATIENT));
		assertThat(patientReferenceTranslator.getReferenceId(result), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfEncounterNull() {
		Reference result = patientReferenceTranslator.toFhirResource(null);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldConvertReferenceToEncounter() {
		Reference patientReference = new Reference().setReference(FhirConstants.PATIENT + "/" + PATIENT_UUID)
		        .setType(FhirConstants.PATIENT).setIdentifier(new Identifier().setValue(PATIENT_UUID));
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		when(dao.get(PATIENT_UUID)).thenReturn(patient);
		
		Patient result = patientReferenceTranslator.toOpenmrsType(patientReference);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfReferenceNull() {
		Patient result = patientReferenceTranslator.toOpenmrsType(null);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfEncounterHasNoIdentifier() {
		Reference patientReference = new Reference().setReference(FhirConstants.PATIENT + "/" + PATIENT_UUID)
		        .setType(FhirConstants.PATIENT);
		
		Patient result = patientReferenceTranslator.toOpenmrsType(patientReference);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfEncounterIdentifierHasNoValue() {
		Reference patientReference = new Reference().setReference(FhirConstants.PATIENT + "/" + PATIENT_UUID)
		        .setType(FhirConstants.PATIENT).setIdentifier(new Identifier());
		
		Patient result = patientReferenceTranslator.toOpenmrsType(patientReference);
		
		assertThat(result, nullValue());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void toOpenmrsType_shouldThrowExceptionIfReferenceIsntForEncounter() {
		Reference reference = new Reference().setReference("Unknown" + "/" + PATIENT_UUID).setType("Unknown");
		
		patientReferenceTranslator.toOpenmrsType(reference);
	}
}
