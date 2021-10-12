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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.exparity.hamcrest.date.DateMatchers;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
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
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConditionTranslatorImplTest {
	
	private static final String CONDITION_UUID = "36aa91ad-66f3-455b-b28a-71beb6ca3195";
	
	private static final Integer CONDITION_ID = 1284;
	
	private static final String PATIENT_UUID = "fc8b217b-2ed4-4dde-b9f7-a5334347e7ca";
	
	private static final String PATIENT_REF = "Patient/" + PATIENT_UUID;
	
	private static final String SYSTEM = "https://openconceptlab.org/orgs/CIEL/sources/CIEL";
	
	private static final Integer CODE = 102309;
	
	private static final String CONCEPT_UUID = "31d754f5-3e9e-4ca3-805c-87f97a1f5e4b";
	
	private static final String PRACTITIONER_UUID = "2ffb1a5f-bcd3-4243-8f40-78edc2642789";
	
	private static final String PRACTITIONER_REFERENCE = FhirConstants.PRACTITIONER + "/" + PRACTITIONER_UUID;
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private ProvenanceTranslator<Obs> provenanceTranslator;
	
	@Mock
	ConceptService conceptService;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private PractitionerReferenceTranslator<User> creatorReferenceTranslator;
	
	private ConditionTranslatorImpl conditionTranslator;
	
	private Condition fhirCondition;
	
	private Obs openmrsCondition;
	
	private Patient patient;
	
	private Reference patientRef;
	
	private Concept concept;
	
	@Before
	public void setup() {
		conditionTranslator = new ConditionTranslatorImpl();
		conditionTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
		conditionTranslator.setConceptTranslator(conceptTranslator);
		conditionTranslator.setPractitionerReferenceTranslator(creatorReferenceTranslator);
		conditionTranslator.setProvenanceTranslator(provenanceTranslator);
		conditionTranslator.setConceptService(conceptService);
		
		patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		
		patientRef = new Reference();
		patientRef.setReference(PATIENT_REF);
		
		concept = new Concept();
		concept.setUuid(CONDITION_UUID);
		concept.setConceptId(CONDITION_ID);
		
		Concept valueCoded = new Concept();
		concept.setUuid(CONDITION_UUID);
		concept.setConceptId(CODE);
		
		openmrsCondition = new Obs();
		openmrsCondition.setUuid(CONDITION_UUID);
		openmrsCondition.setPerson(patient);
		openmrsCondition.setConcept(concept);
		openmrsCondition.setValueCoded(valueCoded);
		
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
		when(conceptService.getConceptByUuid(FhirConstants.CONDITION_OBSERVATION_CONCEPT_UUID)).thenReturn(concept);
		Obs obsCondition = conditionTranslator.toOpenmrsType(fhirCondition);
		assertThat(obsCondition, notNullValue());
		assertThat(obsCondition.getUuid(), equalTo(CONDITION_UUID));
	}
	
	@Test(expected = NullPointerException.class)
	public void toFhirResource_shouldThrowExceptionIfConditionToTranslateIsNull() {
		conditionTranslator.toFhirResource(null);
	}
	
	@Test(expected = InternalErrorException.class)
	public void toFhirOpenmrsType_shouldThrowExceptionIfConceptProblemListIsNotFound() {
		when(patientReferenceTranslator.toOpenmrsType(patientRef)).thenReturn(patient);
		Obs obsCondition = conditionTranslator.toOpenmrsType(fhirCondition);
		assertThat(obsCondition, notNullValue());
		assertThat(obsCondition.getPerson(), notNullValue());
		assertThat(obsCondition.getPerson().getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void shouldTranslateConditionSubjectToOpenMrsType() {
		when(conceptService.getConceptByUuid(FhirConstants.CONDITION_OBSERVATION_CONCEPT_UUID)).thenReturn(concept);
		when(patientReferenceTranslator.toOpenmrsType(patientRef)).thenReturn(patient);
		Obs obsCondition = conditionTranslator.toOpenmrsType(fhirCondition);
		assertThat(obsCondition, notNullValue());
		assertThat(obsCondition.getPerson(), notNullValue());
		assertThat(obsCondition.getPerson().getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void shouldTranslateConditionPatientToFhirType() {
		when(patientReferenceTranslator.toFhirResource(patient, null)).thenReturn(patientRef);
		
		Condition condition = conditionTranslator.toFhirResource(openmrsCondition);
		
		assertThat(condition, notNullValue());
		assertThat(condition.getSubject(), notNullValue());
		assertThat(condition.getSubject(), equalTo(patientRef));
		assertThat(condition.getSubject().getReference(), equalTo(PATIENT_REF));
	}
	
	@Test
	public void shouldTranslateOpenMrsConditionOnsetDateToFhirType() {
		openmrsCondition.setObsDatetime(new Date());
		
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openmrsCondition);
		
		assertThat(condition, notNullValue());
		assertThat(condition.getOnsetDateTimeType().getValue(), notNullValue());
		assertThat(condition.getOnsetDateTimeType().getValue(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void shouldTranslateFhirConditionOnsetToOpenMrsOnsetDate() {
		when(conceptService.getConceptByUuid(FhirConstants.CONDITION_OBSERVATION_CONCEPT_UUID)).thenReturn(concept);
		
		DateTimeType theDateTime = new DateTimeType();
		theDateTime.setValue(new Date());
		fhirCondition.setOnset(theDateTime);
		
		Obs condition = conditionTranslator.toOpenmrsType(fhirCondition);
		
		assertThat(condition, notNullValue());
		assertThat(condition.getObsDatetime(), notNullValue());
		assertThat(condition.getObsDatetime(), DateMatchers.sameDay(new Date()));
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
		when(conceptService.getConceptByUuid(FhirConstants.CONDITION_OBSERVATION_CONCEPT_UUID)).thenReturn(concept);
		when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(concept);
		Obs condition = conditionTranslator.toOpenmrsType(fhirCondition);
		assertThat(condition, notNullValue());
		assertThat(condition.getValueCoded(), notNullValue());
		assertThat(condition.getValueCoded().getConceptId(), equalTo(CODE));
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
		openmrsCondition.setValueCoded(concept);
		when(conceptTranslator.toFhirResource(concept, null)).thenReturn(codeableConcept);
		
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openmrsCondition);
		
		assertThat(condition, notNullValue());
		assertThat(condition.getCode(), notNullValue());
		assertThat(condition.getCode().getCoding(), not(Collections.emptyList()));
		assertThat(condition.getCode().getCoding().get(0).getCode(), equalTo(CODE.toString()));
		assertThat(condition.getCode().getCoding().get(0).getSystem(), equalTo(SYSTEM));
	}
	
	@Test
	public void shouldTranslateConditionDateCreatedToRecordedDateFhirType() {
		openmrsCondition.setDateCreated(new Date());
		
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openmrsCondition);
		
		assertThat(condition, notNullValue());
		assertThat(condition.getRecordedDate(), notNullValue());
		assertThat(condition.getRecordedDate(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void shouldTranslateConditionRecorderToOpenmrsUser() {
		Reference userRef = new Reference();
		userRef.setReference(FhirConstants.PRACTITIONER + "/" + PRACTITIONER_UUID);
		fhirCondition.setRecorder(userRef);
		User user = new User();
		user.setUuid(PRACTITIONER_UUID);
		when(conceptService.getConceptByUuid(FhirConstants.CONDITION_OBSERVATION_CONCEPT_UUID)).thenReturn(concept);
		when(creatorReferenceTranslator.toOpenmrsType(userRef)).thenReturn(user);
		Obs condition = conditionTranslator.toOpenmrsType(fhirCondition);
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
		when(creatorReferenceTranslator.toFhirResource(user, null)).thenReturn(userRef);
		
		org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openmrsCondition);
		
		assertThat(condition, notNullValue());
		assertThat(condition.getRecorder(), notNullValue());
		assertThat(condition.getRecorder().getReference(), equalTo(PRACTITIONER_REFERENCE));
	}
	
	@Test
	public void shouldAddProvenanceToConditionResource() {
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.newUuid()));
		when(provenanceTranslator.getCreateProvenance(openmrsCondition)).thenReturn(provenance);
		when(provenanceTranslator.getUpdateProvenance(openmrsCondition)).thenReturn(provenance);
		
		org.hl7.fhir.r4.model.Condition result = conditionTranslator.toFhirResource(openmrsCondition);
		List<Resource> resources = result.getContained();
		assertThat(resources, Matchers.notNullValue());
		assertThat(resources, Matchers.not(empty()));
		assertThat(resources.stream().findAny().isPresent(), CoreMatchers.is(true));
		assertThat(resources.stream().findAny().get().isResource(), CoreMatchers.is(true));
		assertThat(resources.stream().findAny().get().getResourceType().name(),
		    Matchers.equalTo(Provenance.class.getSimpleName()));
	}
}
