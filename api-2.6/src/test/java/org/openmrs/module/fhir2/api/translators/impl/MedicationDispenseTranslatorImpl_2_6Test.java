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

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
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

import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

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
	private MedicationQuantityCodingTranslatorImpl quantityCodingTranslator;

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
	public void toFhirResource_shouldHandleNullProperties() {
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense, notNullValue());
		assertThat(dispense.getId(), equalTo(MEDICATION_DISPENSE_UUID));
		assertThat(dispense.getSubject().hasReference(), equalTo(false));
		assertThat(dispense.getContext().hasReference(), equalTo(false));
		assertThat(dispense.getAuthorizingPrescription().size(), equalTo(0));
		assertThat(dispense.getStatus(), nullValue());
		assertThat(dispense.getStatusReason(), nullValue());
		assertThat(dispense.getLocation().hasReference(), equalTo(false));
		assertThat(dispense.getCategory().getId(), nullValue());
		assertThat(dispense.getType().getId(), nullValue());
		assertThat(dispense.getWhenPrepared(), nullValue());
		assertThat(dispense.getWhenHandedOver(), nullValue());
		assertThat(dispense.getMedication(), nullValue());
		assertThat(dispense.getDosageInstruction().size(), equalTo(1));
		assertThat(dispense.getQuantity().getValue(), nullValue());
		assertThat(dispense.getQuantity().getSystem(), nullValue());
		assertThat(dispense.getQuantity().getCode(), nullValue());
		assertThat(dispense.getPerformer().size(), equalTo(0));
		assertThat(dispense.getSubstitution().getReason().size(), equalTo(0));
		assertThat(dispense.getSubstitution().getType().hasCoding(), equalTo(false));
		assertThat(dispense.getSubstitution().getType().hasText(), equalTo(false));
		assertThat(dispense.getSubstitution().getWasSubstituted(), equalTo(false));
		assertThat(dispense.getMeta().getLastUpdated(), nullValue());
	}
	
	@Test
	public void toFhirResource_shouldTranslatePatient() {
		Patient openmrsObject = new Patient();
		Reference fhirObject = new Reference();
		when(patientReferenceTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);

		openmrsDispense.setPatient(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getSubject(), notNullValue());
		assertThat(dispense.getSubject(), equalTo(fhirObject));
	}

	@Test
	public void toFhirResource_shouldTranslateEncounter() {
		Encounter openmrsObject = new Encounter();
		Reference fhirObject = new Reference();
		when(encounterReferenceTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);

		openmrsDispense.setEncounter(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getContext(), notNullValue());
		assertThat(dispense.getContext(), equalTo(fhirObject));
	}

	@Test
	public void toFhirResource_shouldTranslateDrugOrder() {
		DrugOrder openmrsObject = new DrugOrder();
		Reference fhirObject = new Reference();
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
		CodeableConcept fhirObject = new CodeableConcept();
		when(conceptTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);

		openmrsDispense.setStatusReason(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getStatusReason(), notNullValue());
		assertThat(dispense.getStatusReason(), equalTo(fhirObject));
	}

	@Test
	public void toFhirResource_shouldTranslateLocation() {
		Location openmrsObject = new Location();
		Reference fhirObject = new Reference();
		when(locationReferenceTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);

		openmrsDispense.setLocation(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getLocation(), notNullValue());
		assertThat(dispense.getLocation(), equalTo(fhirObject));
	}

	@Test
	public void toFhirResource_shouldTranslateType() {
		Concept openmrsObject = new Concept();
		CodeableConcept fhirObject = new CodeableConcept();
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
		CodeableConcept fhirObject = new CodeableConcept();
		when(conceptTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		openmrsDispense.setConcept(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getMedicationCodeableConcept(), equalTo(fhirObject));
		assertThat(dispense.hasMedicationReference(), equalTo(false));
	}

	@Test
	public void toFhirResource_shouldTranslateMedicationDrug() {
		Drug openmrsObject = new Drug();
		Reference fhirObject = new Reference();
		when(medicationReferenceTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		openmrsDispense.setDrug(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getMedicationReference(), equalTo(fhirObject));
		assertThat(dispense.hasMedicationCodeableConcept(), equalTo(false));
	}

	@Test
	public void toFhirResource_shouldTranslateDoseQuantityAndUnits() {
		Concept openmrsObject = new Concept();
		Coding fhirObject = new Coding("system", "code", "display");
		when(quantityCodingTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
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
		CodeableConcept fhirObject = new CodeableConcept();
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
		CodeableConcept fhirObject = new CodeableConcept();
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
		Coding fhirObject = new Coding("system", "code", "display");
		when(quantityCodingTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
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
		Reference fhirObject = new Reference();
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
		CodeableConcept fhirObject = new CodeableConcept();
		when(conceptTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		openmrsDispense.setSubstitutionType(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getSubstitution().getType(), equalTo(fhirObject));
	}

	@Test
	public void toFhirResource_shouldTranslateSubstitutionReason() {
		Concept openmrsObject = new Concept();
		CodeableConcept fhirObject = new CodeableConcept();
		when(conceptTranslator.toFhirResource(openmrsObject)).thenReturn(fhirObject);
		openmrsDispense.setSubstitutionReason(openmrsObject);
		org.hl7.fhir.r4.model.MedicationDispense dispense = translator.toFhirResource(openmrsDispense);
		assertThat(dispense.getSubstitution().getReasonFirstRep(), equalTo(fhirObject));
	}
}
