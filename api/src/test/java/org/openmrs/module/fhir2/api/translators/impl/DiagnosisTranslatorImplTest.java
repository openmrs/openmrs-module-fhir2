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
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class DiagnosisTranslatorImplTest {
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;
	
	@Mock
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	private DiagnosisTranslatorImpl translator;
	
	@Before
	public void setup() {
		translator = new DiagnosisTranslatorImpl();
		translator.setPatientReferenceTranslator(patientReferenceTranslator);
		translator.setEncounterReferenceTranslator(encounterReferenceTranslator);
		translator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
		translator.setConceptTranslator(conceptTranslator);
		when(practitionerReferenceTranslator.toFhirResource(isNull())).thenReturn(null);
	}
	
	@Test
	public void toFhirResource_shouldAddRankExtensionAndSetCertainty() {
		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setUuid("diag-uuid");
		diagnosis.setRank(1);
		diagnosis.setCertainty(ConditionVerificationStatus.CONFIRMED);
		diagnosis.setVoided(false);
		
		Condition result = translator.toFhirResource(diagnosis);
		
		Extension rank = result.getExtensionByUrl(FhirConstants.DIAGNOSIS_RANK_EXTENSION_URI);
		assertThat(rank, notNullValue());
		assertThat(((IntegerType) rank.getValue()).getValue(), equalTo(1));
		
		assertThat(result.getVerificationStatus(), notNullValue());
		assertThat(result.getVerificationStatus().getCodingFirstRep().getCode(), equalTo("confirmed"));
	}
	
	@Test
	public void toOpenmrsType_shouldMapVerificationStatusToCertainty() {
		Condition condition = new Condition();
		CodeableConcept verification = new CodeableConcept();
		verification.addCoding(new Coding().setSystem(FhirConstants.CONDITION_VER_STATUS_SYSTEM_URI).setCode("provisional"));
		condition.setVerificationStatus(verification);
		
		Diagnosis result = translator.toOpenmrsType(condition);
		assertThat(result.getCertainty(), equalTo(ConditionVerificationStatus.PROVISIONAL));
	}
	
	@Test
	public void toFhirResource_shouldIncludeNonCodedExtensionAndRecordedDate() {
		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setUuid("diag-uuid");
		diagnosis.setVoided(false);
		diagnosis.setDiagnosis(new org.openmrs.CodedOrFreeText());
		diagnosis.getDiagnosis().setNonCoded("Other");
		Encounter encounter = new Encounter();
		encounter.setEncounterDatetime(new Date());
		diagnosis.setEncounter(encounter);
		
		Reference encounterReference = new Reference();
		when(encounterReferenceTranslator.toFhirResource(encounter)).thenReturn(encounterReference);
		
		Condition result = translator.toFhirResource(diagnosis);
		
		Extension ext = result.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_NON_CODED_CONDITION);
		assertThat(ext, notNullValue());
		assertThat(((StringType) ext.getValue()).getValue(), equalTo("Other"));
		assertThat(result.getRecordedDate(), notNullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldMapRankAndCertaintyAndSetAsVoidedIfClinicalStatusIsInactive() {
		Condition condition = new Condition();
		condition.setId("diag-uuid");
		condition.addExtension(new Extension(FhirConstants.DIAGNOSIS_RANK_EXTENSION_URI, new IntegerType(2)));
		
		CodeableConcept verificationStatus = new CodeableConcept();
		verificationStatus.addCoding(new Coding().setSystem(FhirConstants.CONDITION_VER_STATUS_SYSTEM_URI)
		        .setCode("confirmed").setDisplay("Confirmed"));
		condition.setVerificationStatus(verificationStatus);
		
		condition.setClinicalStatus(new CodeableConcept().addCoding(
		    new Coding().setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical").setCode("inactive")));
		
		Diagnosis result = translator.toOpenmrsType(condition);
		
		assertThat(result.getRank(), equalTo(2));
		assertThat(result.getCertainty(), equalTo(ConditionVerificationStatus.CONFIRMED));
		assertThat(result.getVoided(), equalTo(true));
	}
	
	@Test
	public void toFhirResource_shouldSetPatientEncounter() {
		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setUuid("diag-uuid");
		Patient patient = new Patient();
		patient.setUuid("patient-uuid");
		diagnosis.setPatient(patient);
		Encounter encounter = new Encounter();
		encounter.setUuid("enc-uuid");
		diagnosis.setEncounter(encounter);
		User creator = new User();
		diagnosis.setCreator(creator);
		
		Reference patientReference = new Reference("Patient/patient-uuid");
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientReference);
		Reference encounterRef = new Reference("Encounter/enc-uuid");
		when(encounterReferenceTranslator.toFhirResource(encounter)).thenReturn(encounterRef);
		Reference practitionerRef = new Reference("Practitioner/prac-uuid");
		when(practitionerReferenceTranslator.toFhirResource(creator)).thenReturn(practitionerRef);
		
		Condition result = translator.toFhirResource(diagnosis);
		
		assertThat(result.getSubject().getReference(), equalTo("Patient/patient-uuid"));
		assertThat(result.getEncounter().getReference(), equalTo("Encounter/enc-uuid"));
		assertThat(result.getRecorder(), sameInstance(practitionerRef));
	}
	
	@Test
	public void toFhirResource_voidedDiagnosisShouldNotBeReturned() {
		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setUuid("diag-uuid");
		diagnosis.setVoided(true);
		
		Condition result = translator.toFhirResource(diagnosis);
		
		assertThat(result, equalTo(null));
	}
	
	@Test
	public void toOpenmrsType_shouldSetPatientEncounterAndCreator() {
		Condition condition = new Condition();
		condition.setId("diag-uuid");
		Reference patientRef = new Reference("Patient/patient-uuid");
		condition.setSubject(patientRef);
		Reference encounterRef = new Reference("Encounter/enc-uuid");
		condition.setEncounter(encounterRef);
		Reference practitionerRef = new Reference("Practitioner/prac-uuid");
		condition.setRecorder(practitionerRef);
		
		Patient patient = new Patient();
		when(patientReferenceTranslator.toOpenmrsType(patientRef)).thenReturn(patient);
		Encounter encounter = new Encounter();
		when(encounterReferenceTranslator.toOpenmrsType(encounterRef)).thenReturn(encounter);
		User user = new User();
		when(practitionerReferenceTranslator.toOpenmrsType(practitionerRef)).thenReturn(user);
		
		Diagnosis result = translator.toOpenmrsType(condition);
		
		assertThat(result.getPatient(), sameInstance(patient));
		assertThat(result.getEncounter(), sameInstance(encounter));
		assertThat(result.getCreator(), sameInstance(user));
	}
	
	@Test
	public void toOpenmrsType_shouldSetCertaintyToProvisional() {
		Condition condition = new Condition();
		CodeableConcept verificationStatus = new CodeableConcept();
		verificationStatus.addCoding(new Coding().setSystem(FhirConstants.CONDITION_VER_STATUS_SYSTEM_URI)
		        .setCode("provisional").setDisplay("Provisional"));
		condition.setVerificationStatus(verificationStatus);
		
		Diagnosis result = translator.toOpenmrsType(condition);
		
		assertThat(result.getCertainty(), equalTo(ConditionVerificationStatus.PROVISIONAL));
	}
	
	@Test
	public void toOpenmrsType_shouldDefaultCertaintyWhenVerificationStatusUnknown() {
		Condition condition = new Condition();
		condition.setVerificationStatus(new CodeableConcept().addCoding(
		    new Coding().setSystem("http://terminology.hl7.org/CodeSystem/condition-ver-status").setCode("unknown")));
		
		Diagnosis result = translator.toOpenmrsType(condition);
		
		assertThat(result.getCertainty(), equalTo(ConditionVerificationStatus.PROVISIONAL));
	}
	
	@Test
	public void toOpenmrsType_shouldIgnoreRankExtensionIfNotInteger() {
		Condition condition = new Condition();
		condition.addExtension(
		    new Extension("http://openmrs.org/fhir/StructureDefinition/diagnosis-rank", new StringType("not-int")));
		
		Diagnosis result = translator.toOpenmrsType(condition);
		
		assertThat(result.getRank(), equalTo(null));
	}
}
