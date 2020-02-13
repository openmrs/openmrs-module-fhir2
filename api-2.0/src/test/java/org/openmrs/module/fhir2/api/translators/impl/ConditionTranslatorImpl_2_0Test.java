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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConditionClinicalStatusTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class ConditionTranslatorImpl_2_0Test {
	
	private static final String CONDITION_UUID = "00af6f0f-ed07-4cef-b0f1-a76a999db987";
	
	private static final String PATIENT_UUID = "258797db-1524-4a13-9f09-2881580b0f5b";
	
	private static final String PATIENT_REFERENCE = "Patient/" + PATIENT_UUID;
	
	private static final String FAMILY_NAME = "Wambua";
	
	private static final String GIVEN_NAME = "Janet";
	
	private static final String TEST_IDENTIFIER_TYPE = "test identifierType";
	
	private static final String IDENTIFIER = "identifier";
	
	private static final String ACTIVE = "Active";
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private ConditionClinicalStatusTranslator<Condition.Status> clinicalStatusTranslator;
	
	private ConditionTranslatorImpl_2_0 conditionTranslator;
	
	private Condition openMrsCondition;
	
	private org.hl7.fhir.r4.model.Condition fhirCondition;
	
	private Patient patient;
	
	@Before
	public void setUp() {
		conditionTranslator = new ConditionTranslatorImpl_2_0();
		conditionTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
		conditionTranslator.setClinicalStatusTranslator(clinicalStatusTranslator);
	}
	
	@Before
	public void initCondition() {
		PersonName name = new PersonName();
		name.setGivenName(GIVEN_NAME);
		name.setFamilyName(FAMILY_NAME);
		
		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setName(TEST_IDENTIFIER_TYPE);
		
		PatientIdentifier identifier = new PatientIdentifier();
		identifier.setIdentifierType(identifierType);
		identifier.setIdentifier(IDENTIFIER);
		
		patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		patient.addIdentifier(identifier);
		patient.addName(name);
		
		Reference patientRef = new Reference();
		patientRef.setReference(PATIENT_UUID);
		
		openMrsCondition = new Condition();
		openMrsCondition.setUuid(CONDITION_UUID);
		openMrsCondition.setPatient(patient);
		openMrsCondition.setStatus(Condition.Status.ACTIVE);
		
		fhirCondition = new org.hl7.fhir.r4.model.Condition();
		fhirCondition.setId(CONDITION_UUID);
		fhirCondition.setSubject(patientRef);
		fhirCondition.setClinicalStatus(clinicalStatusTranslator.toFhirResource(Condition.Status.ACTIVE));
	}
	
	@Test
	public void shouldTranslateConditionToOpenMrsType() {
		Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getUuid(), notNullValue());
		assertThat(condition.getUuid(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void shouldTranslateConditionToFhirType() {
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openMrsCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getId(), notNullValue());
		assertThat(condition.getId(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void shouldUpdateExistingCondition() {
		org.hl7.fhir.r4.model.Condition theCondition = new org.hl7.fhir.r4.model.Condition();
		theCondition.setId(CONDITION_UUID);
		theCondition.setClinicalStatus(clinicalStatusTranslator.toFhirResource(Condition.Status.HISTORY_OF));
		Condition condition = conditionTranslator.toOpenmrsType(openMrsCondition, theCondition);
		
		assertThat(condition, notNullValue());
		assertThat(condition.getStatus(), is(clinicalStatusTranslator.toFhirResource(Condition.Status.HISTORY_OF)));
	}
	
	@Test
	public void shouldTranslatePatientToSubjectFhirType() {
		Reference patientRef = new Reference();
		patientRef.setReference(PATIENT_UUID);
		when(patientReferenceTranslator.toFhirResource(openMrsCondition.getPatient())).thenReturn(patientRef);
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openMrsCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getSubject(), notNullValue());
		assertThat(condition.getSubject().getReference(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void shouldTranslateStatusToClinicalStatusFhirType() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setCode(ACTIVE).setSystem(FhirConstants.OPENMRS_URI).setDisplay(ACTIVE);
		when(clinicalStatusTranslator.toFhirResource(Condition.Status.ACTIVE)).thenReturn(codeableConcept);
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openMrsCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getClinicalStatus(), notNullValue());
		assertThat(condition.getClinicalStatus().getCodingFirstRep().getCode().toLowerCase(), equalTo("active"));
	}
	
	@Test
	public void shouldReturnNullWhenTranslateNullConditionToFhirType() {
		assertThat(conditionTranslator.toFhirResource(null), nullValue());
	}
	
	@Test
	public void shouldReturnExistingConditionWhenTranslateNullConditionToOpenMrsType() {
		Condition existingCondition = new Condition();
		existingCondition.setUuid(CONDITION_UUID);
		Condition condition = conditionTranslator.toOpenmrsType(existingCondition, null);
		assertThat(condition, notNullValue());
		assertThat(condition.getUuid(), equalTo(CONDITION_UUID));
	}
	
}
