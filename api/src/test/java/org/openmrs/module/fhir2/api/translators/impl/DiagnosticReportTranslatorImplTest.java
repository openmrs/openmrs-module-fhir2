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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.OrderReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;

@RunWith(MockitoJUnitRunner.class)
public class DiagnosticReportTranslatorImplTest {
	
	private static final String PARENT_UUID = "249b9094-b812-4b0c-a204-0052a05c657f";
	
	private static final String CHILD_UUID = "faf75a02-5083-454c-a4ec-9a4babf26558";
	
	private static final String CODE = "249b9094-5083-454c-a4ec-9a4babf26558";
	
	private static final String PATIENT_UUID = "3434gh32-34h3j4-34jk34-3422h";
	
	public static final String RADIOLOGY_ORDER_REFERENCE = "ServiceRequest/radiology-order-id";
	
	@Mock
	private EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private ObservationReferenceTranslator observationReferenceTranslator;
	
	@Mock
	OrderReferenceTranslator orderReferenceTranslator;
	
	private DiagnosticReportTranslatorImpl translator;
	
	private FhirDiagnosticReport fhirDiagnosticReport;
	
	private DiagnosticReport diagnosticReport;
	
	@Before
	public void setup() {
		translator = new DiagnosticReportTranslatorImpl();
		translator.setObservationReferenceTranslator(observationReferenceTranslator);
		translator.setConceptTranslator(conceptTranslator);
		translator.setEncounterReferenceTranslator(encounterReferenceTranslator);
		translator.setPatientReferenceTranslator(patientReferenceTranslator);
		
		translator.setOrderReferenceTranslator(orderReferenceTranslator);
		
		// OpenMRS setup
		fhirDiagnosticReport = new FhirDiagnosticReport();
		Obs childObs = new Obs();
		childObs.setUuid(CHILD_UUID);
		fhirDiagnosticReport.setUuid(PARENT_UUID);
		fhirDiagnosticReport.getResults().add(childObs);
		
		// FHIR setup
		Reference obsReference = new Reference().setType("Observation").setReference("Observation/" + CHILD_UUID);
		diagnosticReport = new DiagnosticReport();
		diagnosticReport.setId(PARENT_UUID);
		diagnosticReport.addResult(obsReference);
		
		// Mocks for DiagnosticReport.result
		when(observationReferenceTranslator.toFhirResource(childObs)).thenReturn(obsReference);
		when(observationReferenceTranslator.toOpenmrsType(obsReference)).thenReturn(childObs);
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenmrsObsGroupToDiagnosticReport() {
		DiagnosticReport result = translator.toFhirResource(fhirDiagnosticReport);
		
		assertThat(result, notNullValue());
	}
	
	@Test(expected = NullPointerException.class)
	public void toFhirResource_shouldThrowExceptionForNullObsGroup() {
		translator.toFhirResource(null);
	}
	
	@Test
	public void toFhirResource_shouldConvertUUID() {
		DiagnosticReport result = translator.toFhirResource(fhirDiagnosticReport);
		
		assertThat(result.getId(), equalTo(PARENT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldSetUnknownStatus() {
		DiagnosticReport result = translator.toFhirResource(fhirDiagnosticReport);
		
		assertThat(result.getStatus(), equalTo(DiagnosticReport.DiagnosticReportStatus.UNKNOWN));
	}
	
	@Test
	public void toFhirResource_shouldConvertSubject() {
		Patient subject = new Patient();
		Reference subjectReference = new Reference();
		fhirDiagnosticReport.setSubject(subject);
		
		when(patientReferenceTranslator.toFhirResource(subject)).thenReturn(subjectReference);
		
		DiagnosticReport result = translator.toFhirResource(fhirDiagnosticReport);
		
		assertThat(result.getSubject(), equalTo(subjectReference));
	}
	
	@Test
	public void toFhirResource_shouldConvertEncounter() {
		Encounter encounter = new Encounter();
		Reference encounterReference = new Reference();
		fhirDiagnosticReport.setEncounter(encounter);
		
		when(encounterReferenceTranslator.toFhirResource(encounter)).thenReturn(encounterReference);
		
		DiagnosticReport result = translator.toFhirResource(fhirDiagnosticReport);
		
		assertThat(result.getEncounter(), equalTo(encounterReference));
	}
	
	@Test
	public void toFhirResource_shouldConvertCode() {
		Concept code = new Concept();
		CodeableConcept translatedCode = new CodeableConcept();
		fhirDiagnosticReport.setCode(code);
		
		when(conceptTranslator.toFhirResource(code)).thenReturn(translatedCode);
		
		DiagnosticReport result = translator.toFhirResource(fhirDiagnosticReport);
		
		assertThat(result.getCode(), equalTo(translatedCode));
	}
	
	@Test
	public void toFhirResource_shouldSetLabCategory() {
		DiagnosticReport result = translator.toFhirResource(fhirDiagnosticReport);
		
		assertThat(result.getCategory(), notNullValue());
		assertThat(result.getCategory().size(), equalTo(1));
		
		List<Coding> coding = result.getCategory().iterator().next().getCoding();
		assertThat(coding, contains(hasProperty("system", equalTo(FhirConstants.DIAGNOSTIC_REPORT_SERVICE_SYSTEM_URI))));
		assertThat(coding, contains(hasProperty("code", equalTo("LAB"))));
	}
	
	@Test
	public void toFhirResource_shouldConvertIssued() {
		Date createdDate = new Date();
		fhirDiagnosticReport.setIssued(createdDate);
		
		DiagnosticReport result = translator.toFhirResource(fhirDiagnosticReport);
		
		assertThat(result.getIssued(), notNullValue());
		assertThat(result.getIssued(), equalTo(createdDate));
	}
	
	@Test
	public void toFhirResource_shouldConvertResult() {
		DiagnosticReport result = translator.toFhirResource(fhirDiagnosticReport);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(PARENT_UUID));
		assertThat(result.getResult(), notNullValue());
		assertThat(result.getResult().size(), equalTo(1));
		assertThat(result.getResult(), contains(hasProperty("reference", equalTo("Observation/" + CHILD_UUID))));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenMrsDateChangedToLastUpdatedDate() {
		fhirDiagnosticReport.setDateChanged(new Date());
		DiagnosticReport result = translator.toFhirResource(fhirDiagnosticReport);
		
		assertThat(result, notNullValue());
		assertThat(result.getMeta().getLastUpdated(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenMrsDateChangedToVersionId() {
		fhirDiagnosticReport.setDateChanged(new Date());
		DiagnosticReport result = translator.toFhirResource(fhirDiagnosticReport);
		
		assertThat(result, notNullValue());
		assertThat(result.getMeta().getVersionId(), notNullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldConvertDiagnosticReportToObsGroup() {
		FhirDiagnosticReport result = translator.toOpenmrsType(diagnosticReport);
		
		assertThat(result, notNullValue());
		assertThat(result.getResults().iterator().next().getUuid(), equalTo(CHILD_UUID));
	}
	
	@Test(expected = NullPointerException.class)
	public void toOpenmrsType_shouldThrowExceptionForCreatingNullDiagnosticReport() {
		translator.toOpenmrsType(null);
	}
	
	@Test(expected = NullPointerException.class)
	public void toOpenmrsType_shouldThrowExceptionForUpdatingWithNullDiagnosticReport() {
		translator.toOpenmrsType(fhirDiagnosticReport, null);
	}
	
	@Test
	public void toOpenmrsType_shouldConvertSubject() {
		Patient subject = new Patient();
		Reference subjectReference = new Reference();
		subjectReference.setType(FhirConstants.PATIENT);
		
		diagnosticReport.setSubject(subjectReference);
		
		when(patientReferenceTranslator.toOpenmrsType(subjectReference)).thenReturn(subject);
		
		FhirDiagnosticReport result = translator.toOpenmrsType(diagnosticReport);
		
		assertThat(result.getSubject(), equalTo(subject));
	}
	
	@Test
	public void toOpenmrsType_shouldConvertEncounter() {
		Encounter encounter = new Encounter();
		Reference encounterReference = new Reference();
		encounterReference.setType(FhirConstants.ENCOUNTER);
		
		diagnosticReport.setEncounter(encounterReference);
		
		when(encounterReferenceTranslator.toOpenmrsType(encounterReference)).thenReturn(encounter);
		
		FhirDiagnosticReport result = translator.toOpenmrsType(diagnosticReport);
		
		assertThat(result.getEncounter(), equalTo(encounter));
	}
	
	@Test
	public void toOpenmrsType_shouldConvertCode() {
		CodeableConcept code = new CodeableConcept();
		Concept translatedCode = new Concept();
		
		code.addCoding().setCode(CODE);
		diagnosticReport.setCode(code);
		
		when(conceptTranslator.toOpenmrsType(code)).thenReturn(translatedCode);
		
		FhirDiagnosticReport result = translator.toOpenmrsType(diagnosticReport);
		
		assertThat(result.getCode(), equalTo(translatedCode));
	}
	
	@Test
	public void toOpenmrsType_shouldConvertResult() {
		FhirDiagnosticReport result = translator.toOpenmrsType(diagnosticReport);
		
		assertThat(result, notNullValue());
		assertThat(result.getResults(), notNullValue());
		assertThat(result.getResults(), hasSize(equalTo(1)));
		assertThat(result.getResults(), contains(hasProperty("uuid", equalTo(CHILD_UUID))));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateExistingObsGroup() {
		CodeableConcept newCode = new CodeableConcept();
		Concept translatedCode = new Concept();
		
		newCode.addCoding().setCode(CODE);
		diagnosticReport.setCode(newCode);
		
		when(conceptTranslator.toOpenmrsType(newCode)).thenReturn(translatedCode);
		
		FhirDiagnosticReport result = translator.toOpenmrsType(fhirDiagnosticReport, diagnosticReport);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(fhirDiagnosticReport.getUuid()));
		assertThat(result.getCode(), equalTo(translatedCode));
	}
	
	@Test(expected = NullPointerException.class)
	public void toOpenmrsType_shouldThrowExceptionWhenNoneProvided() {
		translator.toOpenmrsType(null, diagnosticReport);
	}
	
	@Test
	public void toOpenmrsType_shouldResolveOrderId() {
		Reference radiologyOrderRef = new Reference(RADIOLOGY_ORDER_REFERENCE);
		diagnosticReport.setBasedOn(Collections.singletonList(radiologyOrderRef));
		Order radiologyOrder = new Order();
		when(orderReferenceTranslator.toOpenmrsType(radiologyOrderRef)).thenReturn(radiologyOrder);
		FhirDiagnosticReport result = translator.toOpenmrsType(diagnosticReport);
		Assert.assertEquals(radiologyOrder, result.getOrders().iterator().next());
	}
	
	@Test
	public void toFhirResource_shouldReturnOrderRef() {
		Order radiologyOrder = new Order();
		radiologyOrder.setUuid(RADIOLOGY_ORDER_REFERENCE.substring(RADIOLOGY_ORDER_REFERENCE.indexOf("/")));
		fhirDiagnosticReport.setOrders(Collections.singleton(radiologyOrder));
		when(orderReferenceTranslator.toFhirResource(radiologyOrder)).thenReturn(new Reference(RADIOLOGY_ORDER_REFERENCE));
		DiagnosticReport result = translator.toFhirResource(fhirDiagnosticReport);
		Assert.assertEquals(RADIOLOGY_ORDER_REFERENCE, result.getBasedOn().get(0).getReference());
	}
	
}
