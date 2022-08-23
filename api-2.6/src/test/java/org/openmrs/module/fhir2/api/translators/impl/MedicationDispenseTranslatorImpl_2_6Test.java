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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Timing;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.MedicationDispense;
import org.openmrs.OrderFrequency;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.LocationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationDispenseStatusTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestPriorityTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestStatusTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTimingRepeatComponentTranslator;
import org.openmrs.module.fhir2.api.translators.OrderIdentifierTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class MedicationDispenseTranslatorImpl_2_6Test {
	
	public static final String MEDICATION_DISPENSE_UUID = "43578769-f1a4-46af-b08b-d9fe8a07066f";
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;
	
	@Mock
	private MedicationRequestReferenceTranslator medicationRequestReferenceTranslator;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private LocationReferenceTranslator locationReferenceTranslator;
	
	@Mock
	private PractitionerReferenceTranslator<Provider> practitionerReferenceTranslator;
	
	@Mock
	private MedicationDispenseStatusTranslator medicationDispenseStatusTranslator;
	
	@Mock
	private MedicationRequestStatusTranslator statusTranslator;
	
	@Mock
	private MedicationReferenceTranslator medicationReferenceTranslator;
	
	@Mock
	private MedicationRequestPriorityTranslator medicationRequestPriorityTranslator;
	
	@Mock
	private MedicationRequestTimingRepeatComponentTranslator medicationRequestTimingRepeatComponentTranslator;
	
	@Mock
	private OrderIdentifierTranslator orderIdentifierTranslator;
	
	@Mock
	private OrderService orderService;
	
	private MedicationDispense openmrsDispense;
	
	private org.hl7.fhir.r4.model.MedicationDispense fhirDispense;
	
	private MedicationDispenseTranslatorImpl_2_6 translator;
	
	@Before
	public void setup() {
		MedicationQuantityCodingTranslatorImpl quantityCodingTranslator = new MedicationQuantityCodingTranslatorImpl();
		quantityCodingTranslator.setConceptTranslator(conceptTranslator);
		
		MedicationRequestDispenseRequestComponentTranslatorImpl dispenseRequestTranslator = new MedicationRequestDispenseRequestComponentTranslatorImpl();
		dispenseRequestTranslator.setQuantityCodingTranslator(quantityCodingTranslator);
		
		MedicationRequestTimingTranslatorImpl timingTranslator = new MedicationRequestTimingTranslatorImpl();
		timingTranslator.setTimingRepeatComponentTranslator(medicationRequestTimingRepeatComponentTranslator);
		timingTranslator.setConceptTranslator(conceptTranslator);
		timingTranslator.setOrderService(orderService);
		
		DosageTranslatorImpl dosageTranslator = new DosageTranslatorImpl();
		dosageTranslator.setConceptTranslator(conceptTranslator);
		dosageTranslator.setQuantityCodingTranslator(quantityCodingTranslator);
		dosageTranslator.setTimingTranslator(timingTranslator);
		
		MedicationRequestTranslatorImpl medicationRequestTranslator = new MedicationRequestTranslatorImpl();
		medicationRequestTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
		medicationRequestTranslator.setEncounterReferenceTranslator(encounterReferenceTranslator);
		medicationRequestTranslator.setConceptTranslator(conceptTranslator);
		medicationRequestTranslator.setOrderIdentifierTranslator(orderIdentifierTranslator);
		medicationRequestTranslator.setMedicationReferenceTranslator(medicationReferenceTranslator);
		medicationRequestTranslator.setDosageTranslator(dosageTranslator);
		medicationRequestTranslator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
		medicationRequestTranslator.setStatusTranslator(statusTranslator);
		medicationRequestTranslator.setMedicationRequestDispenseRequestComponentTranslator(dispenseRequestTranslator);
		medicationRequestTranslator.setMedicationRequestPriorityTranslator(medicationRequestPriorityTranslator);
		
		translator = new MedicationDispenseTranslatorImpl_2_6();
		translator.setPatientReferenceTranslator(patientReferenceTranslator);
		translator.setEncounterReferenceTranslator(encounterReferenceTranslator);
		translator.setMedicationRequestReferenceTranslator(medicationRequestReferenceTranslator);
		translator.setMedicationRequestTranslator(medicationRequestTranslator);
		translator.setConceptTranslator(conceptTranslator);
		translator.setLocationReferenceTranslator(locationReferenceTranslator);
		translator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
		translator.setMedicationDispenseStatusTranslator(medicationDispenseStatusTranslator);
		
		openmrsDispense = new MedicationDispense();
		openmrsDispense.setUuid(MEDICATION_DISPENSE_UUID);
		
		fhirDispense = new org.hl7.fhir.r4.model.MedicationDispense();
		fhirDispense.setId(MEDICATION_DISPENSE_UUID);
	}
	
	@Test
	public void toFhirResource_shouldTranslateId() {
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense, notNullValue());
		assertThat(dispense.getId(), equalTo(MEDICATION_DISPENSE_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslatePatient() {
		Patient openmrsObject = new Patient();
		Reference fhirObject = newReference();
		when(patientReferenceTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		
		openmrsDispense.setPatient(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getSubject(), notNullValue());
		assertThat(dispense.getSubject(), equalTo(fhirObject));
	}
	
	@Test
	public void toFhirResource_shouldTranslateEncounter() {
		Encounter openmrsObject = new Encounter();
		Reference fhirObject = newReference();
		when(encounterReferenceTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		
		openmrsDispense.setEncounter(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getContext(), notNullValue());
		assertThat(dispense.getContext(), equalTo(fhirObject));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrder() {
		DrugOrder openmrsObject = new DrugOrder();
		Reference fhirObject = newReference();
		when(medicationRequestReferenceTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		
		openmrsDispense.setDrugOrder(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getAuthorizingPrescription().size(), equalTo(1));
		assertThat(dispense.getAuthorizingPrescriptionFirstRep(), equalTo(fhirObject));
	}
	
	@Test
	public void toFhirResource_shouldTranslateStatus() {
		Concept openmrsObject = new Concept();
		org.hl7.fhir.r4.model.MedicationDispense.MedicationDispenseStatus fhirObject = org.hl7.fhir.r4.model.MedicationDispense.MedicationDispenseStatus.COMPLETED;
		when(medicationDispenseStatusTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		
		openmrsDispense.setStatus(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getStatus(), notNullValue());
		assertThat(dispense.getStatus(), equalTo(fhirObject));
	}
	
	@Test
	public void toFhirResource_shouldTranslateStatusReason() {
		Concept openmrsObject = new Concept();
		CodeableConcept fhirObject = newCodeableConcept();
		when(conceptTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		
		openmrsDispense.setStatusReason(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getStatusReason(), notNullValue());
		assertThat(dispense.getStatusReason(), equalTo(fhirObject));
	}
	
	@Test
	public void toFhirResource_shouldTranslateLocation() {
		Location openmrsObject = new Location();
		Reference fhirObject = newReference();
		when(locationReferenceTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		
		openmrsDispense.setLocation(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getLocation(), notNullValue());
		assertThat(dispense.getLocation(), equalTo(fhirObject));
	}
	
	@Test
	public void toFhirResource_shouldTranslateType() {
		Concept openmrsObject = new Concept();
		CodeableConcept fhirObject = newCodeableConcept();
		when(conceptTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		
		openmrsDispense.setType(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getType(), notNullValue());
		assertThat(dispense.getType(), equalTo(fhirObject));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDatePrepared() {
		openmrsDispense.setDatePrepared(new Date());
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getWhenPrepared(), notNullValue());
		assertThat(dispense.getWhenPrepared(), equalTo(openmrsDispense.getDatePrepared()));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDateHandedOver() {
		openmrsDispense.setDateHandedOver(new Date());
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getWhenHandedOver(), notNullValue());
		assertThat(dispense.getWhenHandedOver(), equalTo(openmrsDispense.getDateHandedOver()));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDateLastUpdated() {
		openmrsDispense.setDateCreated(new Date());
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getMeta().getLastUpdated(), notNullValue());
		assertThat(dispense.getMeta().getLastUpdated(), equalTo(openmrsDispense.getDateCreated()));
	}
	
	@Test
	@Ignore // TODO: Un-ignore once we merge in other PR around medication request concepts
	public void toFhirResource_shouldTranslateMedicationConcept() {
		Concept openmrsObject = new Concept();
		CodeableConcept fhirObject = newCodeableConcept();
		when(conceptTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		openmrsDispense.setConcept(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getMedicationCodeableConcept(), equalTo(fhirObject));
		assertThat(dispense.hasMedicationReference(), equalTo(false));
	}
	
	@Test
	public void toFhirResource_shouldTranslateMedicationDrug() {
		Drug openmrsObject = new Drug();
		Reference fhirObject = newReference();
		when(medicationReferenceTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		openmrsDispense.setDrug(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getMedicationReference(), equalTo(fhirObject));
		assertThat(dispense.hasMedicationCodeableConcept(), equalTo(false));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDoseQuantityAndUnits() {
		Concept openmrsObject = new Concept();
		CodeableConcept fhirObject = newCodeableConcept();
		when(conceptTranslator.toFhirResource(any())).thenReturn(fhirObject);
		openmrsDispense.setDose(100d);
		openmrsDispense.setDoseUnits(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		Quantity quantity = dispense.getDosageInstructionFirstRep().getDoseAndRateFirstRep().getDoseQuantity();
		assertThat(quantity, notNullValue());
		assertThat(quantity.getValue().doubleValue(), equalTo(openmrsDispense.getDose()));
		assertThat(quantity.getSystem(), equalTo("system"));
		assertThat(quantity.getCode(), equalTo("code"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDoseRoute() {
		Concept openmrsObject = new Concept();
		CodeableConcept fhirObject = newCodeableConcept();
		when(conceptTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		openmrsDispense.setRoute(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getDosageInstructionFirstRep().getRoute(), notNullValue());
		assertThat(dispense.getDosageInstructionFirstRep().getRoute(), equalTo(fhirObject));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDoseFrequency() {
		OrderFrequency frequencyConcept = new OrderFrequency();
		Concept openmrsObject = new Concept();
		frequencyConcept.setConcept(openmrsObject);
		CodeableConcept fhirObject = newCodeableConcept();
		when(conceptTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		openmrsDispense.setFrequency(frequencyConcept);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense, notNullValue());
		assertThat(dispense.getDosageInstructionFirstRep().getTiming().getCode(), equalTo(fhirObject));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDoseAsNeeded() {
		openmrsDispense.setAsNeeded(true);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense, notNullValue());
		assertThat(dispense.getDosageInstructionFirstRep().getAsNeededBooleanType().getValue(), equalTo(true));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDosingInstructions() {
		openmrsDispense.setDosingInstructions("These are dosing instructions");
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense, notNullValue());
		assertThat(dispense.getDosageInstructionFirstRep().getText(), equalTo(openmrsDispense.getDosingInstructions()));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDispenseQuantityAndUnits() {
		Concept openmrsObject = new Concept();
		CodeableConcept fhirObject = newCodeableConcept();
		when(conceptTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		openmrsDispense.setQuantity(100d);
		openmrsDispense.setQuantityUnits(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		Quantity quantity = dispense.getQuantity();
		assertThat(quantity, notNullValue());
		assertThat(quantity.getValue().doubleValue(), equalTo(openmrsDispense.getQuantity()));
		assertThat(quantity.getSystem(), equalTo("system"));
		assertThat(quantity.getCode(), equalTo("code"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDispenser() {
		Provider openmrsObject = new Provider();
		Reference fhirObject = newReference();
		when(practitionerReferenceTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		openmrsDispense.setDispenser(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getPerformer().size(), equalTo(1));
		assertThat(dispense.getPerformer().get(0).getActor(), equalTo(fhirObject));
		assertThat(dispense.getPerformer().get(0).getFunction().hasCoding(), equalTo(false));
	}
	
	@Test
	public void toFhirResource_shouldTranslateWasSubstituted() {
		openmrsDispense.setWasSubstituted(true);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getSubstitution().getWasSubstituted(), equalTo(true));
	}
	
	@Test
	public void toFhirResource_shouldTranslateSubstitutionType() {
		Concept openmrsObject = new Concept();
		CodeableConcept fhirObject = newCodeableConcept();
		when(conceptTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		openmrsDispense.setSubstitutionType(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getSubstitution().getType(), equalTo(fhirObject));
	}
	
	@Test
	public void toFhirResource_shouldTranslateSubstitutionReason() {
		Concept openmrsObject = new Concept();
		CodeableConcept fhirObject = newCodeableConcept();
		when(conceptTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		openmrsDispense.setSubstitutionReason(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getSubstitution().getReasonFirstRep(), equalTo(fhirObject));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateId() {
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense, notNullValue());
		assertThat(dispense.getUuid(), equalTo(MEDICATION_DISPENSE_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslatePatient() {
		Patient openmrsObject = new Patient();
		Reference fhirObject = newReference();
		when(patientReferenceTranslator.toOpenmrsType(fhirObject)).thenReturn(openmrsObject);
		
		fhirDispense.setSubject(fhirObject);
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getPatient(), notNullValue());
		assertThat(dispense.getPatient(), equalTo(openmrsObject));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateEncounter() {
		Encounter openmrsObject = new Encounter();
		Reference fhirObject = newReference();
		when(encounterReferenceTranslator.toOpenmrsType(fhirObject)).thenReturn(openmrsObject);
		
		fhirDispense.setContext(fhirObject);
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getEncounter(), notNullValue());
		assertThat(dispense.getEncounter(), equalTo(openmrsObject));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateDrugOrder() {
		DrugOrder openmrsObject = new DrugOrder();
		Reference fhirObject = newReference();
		when(medicationRequestReferenceTranslator.toOpenmrsType(fhirObject)).thenReturn(openmrsObject);
		
		fhirDispense.addAuthorizingPrescription(fhirObject);
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getDrugOrder(), notNullValue());
		assertThat(dispense.getDrugOrder(), equalTo(openmrsObject));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateStatus() {
		Concept openmrsObject = new Concept();
		org.hl7.fhir.r4.model.MedicationDispense.MedicationDispenseStatus fhirObject = org.hl7.fhir.r4.model.MedicationDispense.MedicationDispenseStatus.COMPLETED.COMPLETED;
		when(medicationDispenseStatusTranslator.toOpenmrsType(fhirObject)).thenReturn(openmrsObject);
		
		fhirDispense.setStatus(fhirObject);
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getStatus(), notNullValue());
		assertThat(dispense.getStatus(), equalTo(openmrsObject));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateStatusReason() {
		Concept openmrsObject = new Concept();
		CodeableConcept fhirObject = newCodeableConcept();
		when(conceptTranslator.toOpenmrsType(fhirObject)).thenReturn(openmrsObject);
		
		fhirDispense.setStatusReason(fhirObject);
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getStatusReason(), notNullValue());
		assertThat(dispense.getStatusReason(), equalTo(openmrsObject));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateLocation() {
		Location openmrsObject = new Location();
		Reference fhirObject = newReference();
		when(locationReferenceTranslator.toOpenmrsType(fhirObject)).thenReturn(openmrsObject);
		
		fhirDispense.setLocation(fhirObject);
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getLocation(), notNullValue());
		assertThat(dispense.getLocation(), equalTo(openmrsObject));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateType() {
		Concept openmrsObject = new Concept();
		CodeableConcept fhirObject = newCodeableConcept();
		when(conceptTranslator.toOpenmrsType(fhirObject)).thenReturn(openmrsObject);
		
		fhirDispense.setType(fhirObject);
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getType(), notNullValue());
		assertThat(dispense.getType(), equalTo(openmrsObject));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateDatePrepared() {
		fhirDispense.setWhenPrepared(new Date());
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getDatePrepared(), notNullValue());
		assertThat(dispense.getDatePrepared(), equalTo(fhirDispense.getWhenPrepared()));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateDateHandedOver() {
		fhirDispense.setWhenHandedOver(new Date());
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getDateHandedOver(), notNullValue());
		assertThat(dispense.getDateHandedOver(), equalTo(fhirDispense.getWhenHandedOver()));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateDateLastUpdated() {
		fhirDispense.setMeta(new Meta().setLastUpdated(new Date()));
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getDateCreated(), notNullValue());
		assertThat(dispense.getDateCreated(), equalTo(fhirDispense.getMeta().getLastUpdated()));
	}
	
	@Test
	@Ignore // TODO: Un-ignore once we merge in other PR around medication request concepts
	public void toOpenmrsType_shouldTranslateMedicationConcept() {
		Concept openmrsObject = new Concept();
		CodeableConcept fhirObject = newCodeableConcept();
		when(conceptTranslator.toOpenmrsType(fhirObject)).thenReturn(openmrsObject);
		fhirDispense.setMedication(fhirObject);
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getConcept(), equalTo(openmrsObject));
		assertThat(dispense.getDrug(), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateMedicationDrug() {
		Drug openmrsObject = new Drug();
		Reference fhirObject = newReference();
		when(medicationReferenceTranslator.toOpenmrsType(fhirObject)).thenReturn(openmrsObject);
		fhirDispense.setMedication(fhirObject);
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getDrug(), equalTo(openmrsObject));
		assertThat(dispense.getConcept(), equalTo(dispense.getDrug().getConcept()));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateDoseQuantityAndUnits() {
		Concept openmrsObject = new Concept();
		when(conceptTranslator.toOpenmrsType(any())).thenReturn(openmrsObject);
		Dosage.DosageDoseAndRateComponent doseAndRateComponent = new Dosage.DosageDoseAndRateComponent();
		Quantity quantity = new Quantity();
		quantity.setValue(100d);
		quantity.setSystem("system");
		quantity.setCode("code");
		doseAndRateComponent.setDose(quantity);
		fhirDispense.addDosageInstruction(new Dosage().addDoseAndRate(doseAndRateComponent));
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getDose(), equalTo(100d));
		assertThat(dispense.getDoseUnits(), equalTo(openmrsObject));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateDoseRoute() {
		Concept openmrsObject = new Concept();
		CodeableConcept fhirObject = newCodeableConcept();
		when(conceptTranslator.toOpenmrsType(fhirObject)).thenReturn(openmrsObject);
		fhirDispense.addDosageInstruction().setRoute(fhirObject);
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getRoute(), notNullValue());
		assertThat(dispense.getRoute(), equalTo(openmrsObject));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateDoseFrequency() {
		OrderFrequency frequencyConcept = new OrderFrequency();
		Concept openmrsObject = new Concept();
		frequencyConcept.setConcept(openmrsObject);
		CodeableConcept fhirObject = newCodeableConcept();
		when(conceptTranslator.toOpenmrsType(fhirObject)).thenReturn(openmrsObject);
		when(orderService.getOrderFrequencyByConcept(openmrsObject)).thenReturn(frequencyConcept);
		fhirDispense.addDosageInstruction().setTiming(new Timing().setCode(fhirObject));
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense, notNullValue());
		assertThat(dispense.getFrequency(), equalTo(frequencyConcept));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateDoseAsNeeded() {
		fhirDispense.addDosageInstruction().setAsNeeded(new BooleanType(true));
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense, notNullValue());
		assertThat(dispense.getAsNeeded(), equalTo(true));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateDosingInstructions() {
		fhirDispense.addDosageInstruction().setText("These are dosing instructions");
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense, notNullValue());
		assertThat(dispense.getDosingInstructions(), equalTo("These are dosing instructions"));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateDispenseQuantityAndUnits() {
		Concept openmrsObject = new Concept();
		when(conceptTranslator.toOpenmrsType(any())).thenReturn(openmrsObject);
		Quantity quantity = new Quantity();
		quantity.setValue(100d);
		quantity.setSystem("system");
		quantity.setCode("code");
		fhirDispense.setQuantity(quantity);
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getQuantity(), equalTo(100d));
		assertThat(dispense.getQuantityUnits(), equalTo(openmrsObject));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateDispenser() {
		Provider openmrsObject = new Provider();
		Reference fhirObject = newReference();
		when(practitionerReferenceTranslator.toOpenmrsType(fhirObject)).thenReturn(openmrsObject);
		fhirDispense.addPerformer().setActor(fhirObject);
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getDispenser(), equalTo(openmrsObject));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateWasSubstituted() {
		fhirDispense.setSubstitution(
		    new org.hl7.fhir.r4.model.MedicationDispense.MedicationDispenseSubstitutionComponent().setWasSubstituted(true));
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getWasSubstituted(), equalTo(true));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSubstitutionType() {
		Concept openmrsObject = new Concept();
		CodeableConcept fhirObject = newCodeableConcept();
		when(conceptTranslator.toOpenmrsType(fhirObject)).thenReturn(openmrsObject);
		fhirDispense.setSubstitution(
		    new org.hl7.fhir.r4.model.MedicationDispense.MedicationDispenseSubstitutionComponent().setType(fhirObject));
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getSubstitutionType(), equalTo(openmrsObject));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSubstitutionReason() {
		Concept openmrsObject = new Concept();
		CodeableConcept fhirObject = newCodeableConcept();
		when(conceptTranslator.toOpenmrsType(fhirObject)).thenReturn(openmrsObject);
		fhirDispense.setSubstitution(
		    new org.hl7.fhir.r4.model.MedicationDispense.MedicationDispenseSubstitutionComponent().addReason(fhirObject));
		MedicationDispense dispense = translator.toOpenmrsType(fhirDispense);
		assertThat(dispense.getSubstitutionReason(), equalTo(openmrsObject));
	}
	
	private CodeableConcept newCodeableConcept() {
		CodeableConcept c = new CodeableConcept();
		c.addCoding(new Coding("system", "code", "display"));
		return c;
	}
	
	private Reference newReference() {
		Reference r = new Reference();
		r.setReference("reference");
		return r;
	}
}
