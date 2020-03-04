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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.exparity.hamcrest.date.DateMatchers;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionClinicalStatusTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConditionTranslatorImpl_2_0Test {
	
	private static final String CONDITION_UUID = "00af6f0f-ed07-4cef-b0f1-a76a999db987";
	
	private static final String PATIENT_UUID = "258797db-1524-4a13-9f09-2881580b0f5b";
	
	private static final String PRACTITIONER_UUID = "2ffb1a5f-bcd3-4243-8f40-78edc2642789";
	
	private static final String PATIENT_REFERENCE = "Patient/" + PATIENT_UUID;
	
	private static final String FAMILY_NAME = "Wambua";
	
	private static final String GIVEN_NAME = "Janet";
	
	private static final String TEST_IDENTIFIER_TYPE = "test identifierType";
	
	private static final String IDENTIFIER = "identifier";
	
	private static final String ACTIVE = "Active";
	
	private static final String SYSTEM = "urn:oid:2.16.840.1.113883.3.7201";
	
	private static final Integer CODE = 102309;
	
	private static final String CONDITION_NON_CODED = "condition non coded";
	
	private static final String CONCEPT_UUID = "31d754f5-3e9e-4ca3-805c-87f97a1f5e4b";
	
	private static final String PRACTITIONER_REFERENCE = FhirConstants.PRACTITIONER + "/" + PRACTITIONER_UUID;
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private ConditionClinicalStatusTranslator<Condition.Status> clinicalStatusTranslator;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	@Mock
	private ProvenanceTranslator<Condition> provenanceTranslator;
	
	private ConditionTranslatorImpl_2_0 conditionTranslator;
	
	private Condition openMrsCondition;
	
	private org.hl7.fhir.r4.model.Condition fhirCondition;
	
	private Patient patient;
	
	@Before
	public void setUp() {
		conditionTranslator = new ConditionTranslatorImpl_2_0();
		conditionTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
		conditionTranslator.setClinicalStatusTranslator(clinicalStatusTranslator);
		conditionTranslator.setConceptTranslator(conceptTranslator);
		conditionTranslator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
		conditionTranslator.setProvenanceTranslator(provenanceTranslator);
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
	
	@Test
	public void shouldTranslateOpenMrsConditionOnsetDateToFhirType() {
		openMrsCondition.setOnsetDate(new Date());
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openMrsCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getOnsetDateTimeType().getValue(), notNullValue());
		assertThat(condition.getOnsetDateTimeType().getValue(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void shouldTranslateFhirConditionOnsetToOpenMrsOnsetDate() {
		DateTimeType theDateTime = new DateTimeType();
		theDateTime.setValue(new Date());
		fhirCondition.setOnset(theDateTime);
		Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getOnsetDate(), notNullValue());
		assertThat(condition.getOnsetDate(), DateMatchers.sameDay(new Date()));
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
		Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getConcept(), notNullValue());
		assertThat(condition.getConcept().getConceptId(), equalTo(CODE));
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
		openMrsCondition.setConcept(concept);
		when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openMrsCondition);
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
		coding.setCode(CONDITION_NON_CODED);
		codeableConcept.addCoding(coding);
		when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(null);
		fhirCondition.setCode(codeableConcept);
		Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getConditionNonCoded(), notNullValue());
		assertThat(condition.getConditionNonCoded(), equalTo(CONDITION_NON_CODED));
	}
	
	@Test
	public void shouldTranslateConditionNonCodedToFhirType() {
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding coding = new Coding();
		coding.setCode(CONDITION_NON_CODED);
		codeableConcept.addCoding(coding);
		openMrsCondition.setConditionNonCoded(CONDITION_NON_CODED);
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openMrsCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getCode(), notNullValue());
		assertThat(condition.getCode().getCoding(), not(Collections.emptyList()));
		assertThat(condition.getCode().getCoding().get(0).getCode(), equalTo(CONDITION_NON_CODED));
	}
	
	@Test
	public void shouldTranslateConditionDateCreatedToRecordedDateFhirType() {
		openMrsCondition.setDateCreated(new Date());
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openMrsCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getRecordedDate(), notNullValue());
		assertThat(condition.getRecordedDate(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void shouldTranslateConditionRecordedDateToDateCreatedOpenMrsType() {
		fhirCondition.setRecordedDate(new Date());
		Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getDateCreated(), notNullValue());
		assertThat(condition.getDateCreated(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void shouldTranslateConditionRecorderToOpenmrsUser() {
		Reference userRef = new Reference();
		userRef.setReference(FhirConstants.PRACTITIONER + "/" + PRACTITIONER_UUID);
		fhirCondition.setRecorder(userRef);
		User user = new User();
		user.setUuid(PRACTITIONER_UUID);
		when(practitionerReferenceTranslator.toOpenmrsType(userRef)).thenReturn(user);
		Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
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
		openMrsCondition.setCreator(user);
		when(practitionerReferenceTranslator.toFhirResource(user)).thenReturn(userRef);
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openMrsCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getRecorder(), notNullValue());
		assertThat(condition.getRecorder().getReference(), equalTo(PRACTITIONER_REFERENCE));
	}
	
	@Test
	public void shouldAddProvenanceToConditionResource() {
		Condition condition = new Condition();
		condition.setUuid(CONDITION_UUID);
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.uniqueUuid()));
		when(provenanceTranslator.getCreateProvenance(condition)).thenReturn(provenance);
		when(provenanceTranslator.getUpdateProvenance(condition)).thenReturn(provenance);
		
		org.hl7.fhir.r4.model.Condition result = conditionTranslator.toFhirResource(condition);
		List<Resource> resources = result.getContained();
		assertThat(resources, Matchers.notNullValue());
		assertThat(resources, Matchers.not(empty()));
		assertThat(resources.stream().findAny().isPresent(), CoreMatchers.is(true));
		assertThat(resources.stream().findAny().get().isResource(), CoreMatchers.is(true));
		assertThat(resources.stream().findAny().get().getResourceType().name(),
		    Matchers.equalTo(Provenance.class.getSimpleName()));
	}
}
