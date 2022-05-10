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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.MedicationDispense;
import org.openmrs.Patient;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class MedicationDispenseTranslatorImpl_2_6Test {
	
	private static final String PATIENT_UUID = "fc8b217b-2ed4-4dde-b9f7-a5334347e7ca";
	
	private static final String PATIENT_REF = "Patient/" + PATIENT_UUID;
	
	public static final String MEDICATION_DISPENSE_UUID = "43578769-f1a4-46af-b08b-d9fe8a07066f";
	
	private static final String MEDICATION_CONCEPT_UUID = "36aa91ad-66f3-455b-b28a-71beb6ca3195";
	
	private static final String STATUS_CONCEPT_UUID = "aaaaaaaa-66f3-455b-b28a-71beb6ca3195";
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	private MedicationDispense openmrsDispense;
	
	private org.hl7.fhir.r4.model.MedicationDispense fhirDispense;
	
	private MedicationDispenseTranslatorImpl_2_6 translator;
	
	@Before
	public void setup() {
		translator = new MedicationDispenseTranslatorImpl_2_6();
		translator.setPatientReferenceTranslator(patientReferenceTranslator);
		
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		
		Reference patientRef = new Reference();
		patientRef.setReference(PATIENT_REF);
		
		Concept medConcept = new Concept();
		medConcept.setUuid(MEDICATION_CONCEPT_UUID);
		
		Concept statusConcept = new Concept();
		medConcept.setUuid(STATUS_CONCEPT_UUID);
		
		openmrsDispense = new MedicationDispense();
		openmrsDispense.setUuid(MEDICATION_DISPENSE_UUID);
		openmrsDispense.setPatient(patient);
		openmrsDispense.setConcept(medConcept);
		openmrsDispense.setStatus(statusConcept);
		
		fhirDispense = new org.hl7.fhir.r4.model.MedicationDispense();
		fhirDispense.setId(MEDICATION_DISPENSE_UUID);
		fhirDispense.setSubject(patientRef);
		
		when(patientReferenceTranslator.toOpenmrsType(patientRef)).thenReturn(patient);
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientRef);
	}
	
	@Test
	public void shouldTranslateToFHIRFromOpenMRS() {
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense, notNullValue());
		assertThat(dispense.getId(), equalTo(MEDICATION_DISPENSE_UUID));
		assertThat(dispense.getSubject().getReference(), equalTo(PATIENT_REF));
	}
	
	@Test
	public void shouldTranslateFromOpenMRSToFHIR() {
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense, notNullValue());
		assertThat(dispense.getUuid(), equalTo(MEDICATION_DISPENSE_UUID));
		assertThat(dispense.getPatient().getUuid(), equalTo(PATIENT_UUID));
	}
}
