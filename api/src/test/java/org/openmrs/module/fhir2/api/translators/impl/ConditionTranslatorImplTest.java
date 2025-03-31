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

import static org.exparity.hamcrest.date.DateMatchers.sameDay;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;

import org.hamcrest.Matchers;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Annotation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Concept;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionClinicalStatusTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionVerificationStatusTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class ConditionTranslatorImplTest {
	
	private static final String CONDITION_UUID = "36aa91ad-66f3-455b-b28a-71beb6ca3195";
	
	private static final String PATIENT_UUID = "fc8b217b-2ed4-4dde-b9f7-a5334347e7ca";
	
	private static final String PATIENT_REF = "Patient/" + PATIENT_UUID;
	
	private static final String ACTIVE = "active";
	
	private static final String SYSTEM = "https://openconceptlab.org/orgs/CIEL/sources/CIEL";
	
	private static final Integer CODE = 102309;
	
	private static final Integer CONDITION_NON_CODED = 5622;
	
	private static final String CONDITION_NON_CODED_TEXT = "condition non coded";
	
	private static final String CONDITION_NON_CODED_VALUE = "Other";
	
	private static final String CONCEPT_UUID = "31d754f5-3e9e-4ca3-805c-87f97a1f5e4b";
	
	private static final String PRACTITIONER_UUID = "2ffb1a5f-bcd3-4243-8f40-78edc2642789";
	
	private static final String PRACTITIONER_REFERENCE = FhirConstants.PRACTITIONER + "/" + PRACTITIONER_UUID;
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	private static final String CONFIRMED = "confirmed";
	
	private static final String PROVISIONAL = "provisional";
	
	@Mock
	private ConditionClinicalStatusTranslator<ConditionClinicalStatus> clinicalStatusTranslator;
	
	@Mock
	private ConditionVerificationStatusTranslator<ConditionVerificationStatus> verificationStatusTranslator;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private PractitionerReferenceTranslator<User> creatorReferenceTranslator;
	
	private ConditionTranslatorImpl conditionTranslator;
	
	private Condition fhirCondition;
	
	private org.openmrs.Condition openmrsCondition;
	
	private Patient patient;
	
	private Reference patientRef;
	
	@Before
	public void setup() {
		conditionTranslator = new ConditionTranslatorImpl();
		conditionTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
		conditionTranslator.setClinicalStatusTranslator(clinicalStatusTranslator);
		conditionTranslator.setVerificationStatusTranslator(verificationStatusTranslator);
		conditionTranslator.setConceptTranslator(conceptTranslator);
		conditionTranslator.setPractitionerReferenceTranslator(creatorReferenceTranslator);
		
		patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		
		patientRef = new Reference();
		patientRef.setReference(PATIENT_REF);
		
		Concept concept = new Concept();
		concept.setUuid(CONDITION_UUID);
		concept.setConceptId(CODE);
		
		CodedOrFreeText conditionCoded = new CodedOrFreeText();
		conditionCoded.setCoded(concept);
		
		openmrsCondition = new org.openmrs.Condition();
		openmrsCondition.setUuid(CONDITION_UUID);
		openmrsCondition.setPatient(patient);
		openmrsCondition.setCondition(conditionCoded);
		
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
	
	@Test(expected = NullPointerException.class)
	public void toFhirResource_shouldThrowExceptionIfConditionToTranslateIsNull() {
		conditionTranslator.toFhirResource(null);
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
		codeableConcept.addCoding(new Coding().setCode(ACTIVE).setSystem(FhirConstants.OPENMRS_FHIR_PREFIX));
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
		codeableConcept.addCoding(new Coding().setCode(ACTIVE).setSystem(FhirConstants.OPENMRS_FHIR_PREFIX));
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
		codeableConcept.addCoding(new Coding().setCode(CONFIRMED).setSystem(FhirConstants.OPENMRS_FHIR_PREFIX));
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
		codeableConcept.addCoding(new Coding().setCode(PROVISIONAL).setSystem(FhirConstants.OPENMRS_FHIR_PREFIX));
		fhirCondition.setVerificationStatus(codeableConcept);
		when(verificationStatusTranslator.toOpenmrsType(codeableConcept))
		        .thenReturn(ConditionVerificationStatus.PROVISIONAL);
		org.openmrs.Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getVerificationStatus(), equalTo(ConditionVerificationStatus.PROVISIONAL));
	}
	
	@Test
	public void shouldTranslateOpenMrsConditionOnsetDateToFhirType() {
		openmrsCondition.setOnsetDate(new Date());
		
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openmrsCondition);
		
		assertThat(condition, notNullValue());
		assertThat(condition.hasOnsetDateTimeType(), is(true));
		assertThat(condition.getOnsetDateTimeType().getValue(), sameDay(new Date()));
	}

	@Test
	public void shouldTranslateOpenMrsConditionAdditionalDetailToFhirType() {
		openmrsCondition.setAdditionalDetail("Patient experiencing mild headaches.");
		org.hl7.fhir.r4.model.Condition fhirCondition = conditionTranslator.toFhirResource(openmrsCondition);
		assertThat(fhirCondition.getNote(), notNullValue());
		assertThat(fhirCondition.getNote().get(0).getText(), is("Patient experiencing mild headaches."));
	}

	@Test
	public void shouldTranslateFhirConditionNoteToOpenMrsConditionAdditionalDetail() {
		Annotation annotation = new Annotation();
		annotation.setText("Patient experiencing mild symptoms.");
		fhirCondition.addNote(annotation);
		openmrsCondition = conditionTranslator.toOpenmrsType(fhirCondition);
		assertThat(openmrsCondition, notNullValue());
		assertThat(openmrsCondition.getAdditionalDetail(), is("Patient experiencing mild symptoms."));
	}

	@Test
	public void shouldTranslateFhirConditionOnsetToOpenMrsOnsetDate() {
		DateTimeType theDateTime = new DateTimeType();
		theDateTime.setValue(new Date());
		fhirCondition.setOnset(theDateTime);
		
		org.openmrs.Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
		
		assertThat(condition, notNullValue());
		assertThat(condition.getOnsetDate(), notNullValue());
		assertThat(condition.getOnsetDate(), sameDay(new Date()));
	}
	
	@Test
	public void shouldTranslateOpenMrsConditionEndDateToFhirType() {
		openmrsCondition.setEndDate(new Date());
		
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openmrsCondition);
		
		assertThat(condition, notNullValue());
		assertThat(condition.hasAbatementDateTimeType(), is(true));
		assertThat(condition.getAbatementDateTimeType().getValue(), notNullValue());
		assertThat(condition.getAbatementDateTimeType().getValue(), sameDay(new Date()));
	}
	
	@Test
	public void shouldTranslateFhirConditionAbatementDateToOpenMrsEndDate() {
		DateTimeType theDateTime = new DateTimeType();
		theDateTime.setValue(new Date());
		fhirCondition.setAbatement(theDateTime);
		
		org.openmrs.Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
		
		assertThat(condition, notNullValue());
		assertThat(condition.getEndDate(), notNullValue());
		assertThat(condition.getEndDate(), sameDay(new Date()));
	}
	
	@Test
	public void shouldTranslateConditionCodeToOpenMrsConcept() {
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding coding = new Coding();
		coding.setCode(CODE.toString());
		coding.setSystem(SYSTEM);
		codeableConcept.addCoding(coding);
		fhirCondition.setCode(codeableConcept);
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		concept.setConceptId(CODE);
		when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(concept);
		org.openmrs.Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getCondition(), notNullValue());
		assertThat(condition.getCondition().getCoded().getConceptId(), equalTo(CODE));
	}
	
	@Test
	public void shouldTranslateConditionConceptToFhirType() {
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		concept.setConceptId(CODE);
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding coding = new Coding();
		coding.setCode(CODE.toString());
		coding.setSystem(SYSTEM);
		codeableConcept.addCoding(coding);
		CodedOrFreeText conceptCodeOrFreeText = new CodedOrFreeText();
		conceptCodeOrFreeText.setCoded(concept);
		openmrsCondition.setCondition(conceptCodeOrFreeText);
		when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openmrsCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getCode(), notNullValue());
		assertThat(condition.getCode().getCoding(), not(Collections.emptyList()));
		assertThat(condition.getCode().getCoding().get(0).getCode(), equalTo(CODE.toString()));
		assertThat(condition.getCode().getCoding().get(0).getSystem(), equalTo(SYSTEM));
	}
	
	@Test
	public void shouldTranslateConditionNonCodedToOpenMrsType() {
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding coding = new Coding();
		coding.setCode(String.valueOf(CONDITION_NON_CODED));
		coding.setDisplay(CONDITION_NON_CODED_VALUE);
		codeableConcept.addCoding(coding);
		Concept concept = new Concept();
		concept.setConceptId(CONDITION_NON_CODED);
		fhirCondition.setCode(codeableConcept);
		fhirCondition.addExtension(FhirConstants.OPENMRS_FHIR_EXT_NON_CODED_CONDITION,
		    new StringType(CONDITION_NON_CODED_TEXT));
		when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(concept);
		org.openmrs.Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getCondition().getCoded(), equalTo(concept));
		assertThat(condition.getCondition().getNonCoded(), notNullValue());
		assertThat(condition.getCondition().getNonCoded(), equalTo(CONDITION_NON_CODED_TEXT));
	}
	
	@Test
	public void shouldTranslateConditionNonCodedToFhirType() {
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding coding = new Coding();
		coding.setCode(String.valueOf(CONDITION_NON_CODED));
		coding.setDisplay(CONDITION_NON_CODED_VALUE);
		codeableConcept.addCoding(coding);
		
		Concept concept = new Concept();
		concept.setConceptId(CONDITION_NON_CODED);
		CodedOrFreeText conditionNonCoded = new CodedOrFreeText();
		conditionNonCoded.setCoded(concept);
		conditionNonCoded.setNonCoded(CONDITION_NON_CODED_TEXT);
		openmrsCondition.setCondition(conditionNonCoded);
		
		when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openmrsCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getCode(), notNullValue());
		assertThat(condition.getCode().getCoding(), not(Collections.emptyList()));
		assertThat(condition.getCode().getCoding().get(0).getCode(), equalTo(CONDITION_NON_CODED.toString()));
		assertThat(condition.getCode().getCoding().get(0).getDisplay(), equalTo(CONDITION_NON_CODED_VALUE));
		
		Extension extension = condition.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_NON_CODED_CONDITION);
		assertThat(extension, notNullValue());
		assertThat(extension.getValue().toString(), equalTo(CONDITION_NON_CODED_TEXT));
	}
	
	@Test
	public void shouldTranslateConditionDateCreatedToRecordedDateFhirType() {
		openmrsCondition.setDateCreated(new Date());
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openmrsCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getRecordedDate(), notNullValue());
		assertThat(condition.getRecordedDate(), sameDay(new Date()));
	}
	
	@Test
	public void shouldTranslateConditionRecorderToOpenmrsUser() {
		Reference userRef = new Reference();
		userRef.setReference(FhirConstants.PRACTITIONER + "/" + PRACTITIONER_UUID);
		fhirCondition.setRecorder(userRef);
		User user = new User();
		user.setUuid(PRACTITIONER_UUID);
		when(creatorReferenceTranslator.toOpenmrsType(userRef)).thenReturn(user);
		org.openmrs.Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getCreator(), notNullValue());
		assertThat(condition.getCreator().getUuid(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void shouldTranslateConditionCreatorToRecorderFhirType() {
		User user = new User();
		user.setUuid(PRACTITIONER_UUID);
		Reference userRef = new Reference();
		userRef.setReference(PRACTITIONER_REFERENCE);
		openmrsCondition.setCreator(user);
		when(creatorReferenceTranslator.toFhirResource(user)).thenReturn(userRef);
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openmrsCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getRecorder(), notNullValue());
		assertThat(condition.getRecorder().getReference(), equalTo(PRACTITIONER_REFERENCE));
	}
	
	@Test
	public void shouldTranslateOpenMrsDateChangedToLastUpdatedDate() {
		org.openmrs.Condition condition = new org.openmrs.Condition();
		condition.setDateChanged(new Date());
		
		org.hl7.fhir.r4.model.Condition result = conditionTranslator.toFhirResource(condition);
		
		assertThat(result, Matchers.notNullValue());
		assertThat(result.getMeta().getLastUpdated(), Matchers.notNullValue());
	}
	
	@Test
	public void shouldTranslateOpenMrsDateChangedToVersionId() {
		org.openmrs.Condition condition = new org.openmrs.Condition();
		condition.setDateChanged(new Date());
		
		org.hl7.fhir.r4.model.Condition result = conditionTranslator.toFhirResource(condition);
		
		assertThat(result, Matchers.notNullValue());
		assertThat(result.getMeta().getVersionId(), Matchers.notNullValue());
	}
}
