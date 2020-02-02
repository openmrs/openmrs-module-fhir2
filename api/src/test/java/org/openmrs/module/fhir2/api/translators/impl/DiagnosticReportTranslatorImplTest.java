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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class DiagnosticReportTranslatorImplTest {
	
	private static final String PARENT_UUID = "249b9094-b812-4b0c-a204-0052a05c657f";
	
	private static final String CHILD_UUID = "faf75a02-5083-454c-a4ec-9a4babf26558";
	
	private static final String PATIENT_UUID = "249b9094-5083-454c-a4ec-9a4babf26558";
	
	@Mock
	private EncounterReferenceTranslator encounterReferenceTranslator;
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private ObservationReferenceTranslator observationReferenceTranslator;
	
	DiagnosticReportTranslatorImpl translator;
	
	Obs obsGroup;
	
	@Before
	public void setup() {
		translator = new DiagnosticReportTranslatorImpl();
		translator.setObservationReferenceTranslator(observationReferenceTranslator);
		translator.setConceptTranslator(conceptTranslator);
		translator.setEncounterReferenceTranslator(encounterReferenceTranslator);
		translator.setPatientReferenceTranslator(patientReferenceTranslator);
		
		obsGroup = new Obs();
		Obs childObs = new Obs();
		obsGroup.setUuid(PARENT_UUID);
		childObs.setUuid(CHILD_UUID);
		obsGroup.addGroupMember(childObs);
		
		Reference obsReference = new Reference();
		obsReference.setType("Observation");
		obsReference.setReference("Observation/" + CHILD_UUID);
		
		when(observationReferenceTranslator.toFhirResource(childObs)).thenReturn(obsReference);
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenmrsObsGroupToDiagnosticReport() {
		DiagnosticReport result = translator.toFhirResource(obsGroup);
		
		assertThat(result, notNullValue());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void toFhirResource_shouldThrowIllegalArgumentExceptionIfObsIsNotAnObsgroup() {
		Obs obs = new Obs();
		
		DiagnosticReport result = translator.toFhirResource(obs);
		
		assertThat(result, notNullValue());
	}
	
	@Test
	public void toFhirResource_shouldConvertUUID() {
		DiagnosticReport result = translator.toFhirResource(obsGroup);
		
		assertThat(result.getId(), equalTo(PARENT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldSetUnknownStatus() {
		DiagnosticReport result = translator.toFhirResource(obsGroup);
		
		assertThat(result.getStatus(), equalTo(DiagnosticReport.DiagnosticReportStatus.UNKNOWN));
	}
	
	@Test
	public void toFhirResource_shouldConvertSubject() {
		Patient subject = new Patient();
		Reference subjectReference = new Reference();
		obsGroup.setPerson(subject);
		
		when(patientReferenceTranslator.toFhirResource(subject)).thenReturn(subjectReference);
		
		DiagnosticReport result = translator.toFhirResource(obsGroup);
		
		assertThat(result.getSubject(), equalTo(subjectReference));
	}
	
	@Test
	public void toFhirResource_shouldConvertEncounter() {
		Encounter encounter = new Encounter();
		Reference encounterReference = new Reference();
		obsGroup.setEncounter(encounter);
		
		when(encounterReferenceTranslator.toFhirResource(encounter)).thenReturn(encounterReference);
		
		DiagnosticReport result = translator.toFhirResource(obsGroup);
		
		assertThat(result.getEncounter(), equalTo(encounterReference));
	}
	
	@Test
	public void toFhirResource_shouldConvertCode() {
		Concept code = new Concept();
		CodeableConcept translatedCode = new CodeableConcept();
		obsGroup.setConcept(code);
		
		when(conceptTranslator.toFhirResource(code)).thenReturn(translatedCode);
		
		DiagnosticReport result = translator.toFhirResource(obsGroup);
		
		assertThat(result.getCode(), equalTo(translatedCode));
	}
	
	@Test
	public void toFhirResource_shouldSetLabCategory() {
		DiagnosticReport result = translator.toFhirResource(obsGroup);
		
		assertThat(result.getCategory(), notNullValue());
		assertThat(result.getCategory().size(), equalTo(1));
		
		List<Coding> coding = result.getCategory().iterator().next().getCoding();
		assertThat(coding,
		    contains(hasProperty("system", equalTo("http://hl7.org/fhir/ValueSet/diagnostic-service-sections"))));
		assertThat(coding, contains(hasProperty("code", equalTo("LAB"))));
	}
	
	@Test
	public void toFhirResource_shouldConvertIssued() {
		Date createdDate = new Date();
		obsGroup.setDateCreated(createdDate);
		
		DiagnosticReport result = translator.toFhirResource(obsGroup);
		
		assertThat(result.getIssued(), notNullValue());
		assertThat(result.getIssued(), equalTo(createdDate));
	}
	
	@Test
	public void toFhirResource_shouldConvertResult() {
		DiagnosticReport result = translator.toFhirResource(obsGroup);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(PARENT_UUID));
		assertThat(result.getResult(), notNullValue());
		assertThat(result.getResult().size(), equalTo(1));
		assertThat(result.getResult(), contains(hasProperty("reference", equalTo("Observation/" + CHILD_UUID))));
	}
}
