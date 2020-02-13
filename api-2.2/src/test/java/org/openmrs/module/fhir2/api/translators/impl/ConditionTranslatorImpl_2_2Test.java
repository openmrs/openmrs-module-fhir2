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

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Patient;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConditionClinicalStatusTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionVerificationStatusTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class ConditionTranslatorImpl_2_2Test {
	
	private static final String CONDITION_UUID = "36aa91ad-66f3-455b-b28a-71beb6ca3195";
	
	private static final String PATIENT_UUID = "fc8b217b-2ed4-4dde-b9f7-a5334347e7ca";
	
	private static final String PATIENT_REF = "Patient/" + PATIENT_UUID;
	
	private static final String ACTIVE = "active";
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	private static final String CONFIRMED = "confirmed";
	
	private static final String PROVISIONAL = "provisional";
	
	@Mock
	private ConditionClinicalStatusTranslator<ConditionClinicalStatus> clinicalStatusTranslator;
	
	@Mock
	private ConditionVerificationStatusTranslator<ConditionVerificationStatus> verificationStatusTranslator;
	
	private ConditionTranslatorImpl_2_2 conditionTranslator;
	
	private Condition fhirCondition;
	
	private org.openmrs.Condition openmrsCondition;
	
	private Patient patient;
	
	private Reference patientRef;
	
	@Before
	public void setup() {
		conditionTranslator = new ConditionTranslatorImpl_2_2();
		conditionTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
		conditionTranslator.setClinicalStatusTranslator(clinicalStatusTranslator);
		conditionTranslator.setVerificationStatusTranslator(verificationStatusTranslator);
		
		patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		
		patientRef = new Reference();
		patientRef.setReference(PATIENT_REF);
		
		openmrsCondition = new org.openmrs.Condition();
		openmrsCondition.setUuid(CONDITION_UUID);
		openmrsCondition.setPatient(patient);
		
		fhirCondition = new Condition();
		fhirCondition.setId(CONDITION_UUID);
		fhirCondition.setSubject(patientRef);
	}
	
	@Test
	public void shouldTranslateConditionIdToFhirType() {
		Condition condition = conditionTranslator.toFhirResource(openmrsCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getId(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void shouldTranslateConditionUuidToOpenMrsType() {
		org.openmrs.Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getUuid(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void shouldTranslateConditionSubjectToOpenMrsType() {
		when(patientReferenceTranslator.toOpenmrsType(patientRef)).thenReturn(patient);
		org.openmrs.Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getPatient(), notNullValue());
		assertThat(condition.getPatient().getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void shouldTranslateConditionPatientToFhirType() {
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientRef);
		Condition condition = conditionTranslator.toFhirResource(openmrsCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getSubject(), notNullValue());
		assertThat(condition.getSubject(), equalTo(patientRef));
		assertThat(condition.getSubject().getReference(), equalTo(PATIENT_REF));
	}
	
	@Test
	public void shouldTranslateConditionClinicalStatusToOpenMrsType() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding(new Coding().setCode(ACTIVE).setSystem(FhirConstants.OPENMRS_URI));
		fhirCondition.setClinicalStatus(codeableConcept);
		when(clinicalStatusTranslator.toOpenmrsType(codeableConcept)).thenReturn(ConditionClinicalStatus.ACTIVE);
		org.openmrs.Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
		
		assertThat(condition, notNullValue());
		assertThat(condition.getClinicalStatus(), notNullValue());
		assertThat(condition.getClinicalStatus(), equalTo(ConditionClinicalStatus.ACTIVE));
		
	}
	
	@Test
	public void shouldTranslateConditionClinicalStatusToFhirType() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding(new Coding().setCode(ACTIVE).setSystem(FhirConstants.OPENMRS_URI));
		openmrsCondition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
		when(clinicalStatusTranslator.toFhirResource(ConditionClinicalStatus.ACTIVE)).thenReturn(codeableConcept);
		
		Condition condition = conditionTranslator.toFhirResource(openmrsCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getClinicalStatus(), notNullValue());
		assertThat(condition.getClinicalStatus(), equalTo(codeableConcept));
	}
	
	@Test
	public void shouldTranslateConditionVerificationStatusToFhirType() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding(new Coding().setCode(CONFIRMED).setSystem(FhirConstants.OPENMRS_URI));
		openmrsCondition.setVerificationStatus(ConditionVerificationStatus.CONFIRMED);
		when(verificationStatusTranslator.toFhirResource(ConditionVerificationStatus.CONFIRMED)).thenReturn(codeableConcept);
		Condition condition = conditionTranslator.toFhirResource(openmrsCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getVerificationStatus(), equalTo(codeableConcept));
		assertThat(condition.getVerificationStatus().getCodingFirstRep().getCode().toLowerCase(), equalTo(CONFIRMED));
	}
	
	@Test
	public void shouldTranslateConditionVerificationStatusToOpenMrsType() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding(new Coding().setCode(PROVISIONAL).setSystem(FhirConstants.OPENMRS_URI));
		fhirCondition.setVerificationStatus(codeableConcept);
		when(verificationStatusTranslator.toOpenmrsType(codeableConcept))
		        .thenReturn(ConditionVerificationStatus.PROVISIONAL);
		org.openmrs.Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getVerificationStatus(), equalTo(ConditionVerificationStatus.PROVISIONAL));
	}
}
