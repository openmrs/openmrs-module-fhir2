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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestPriorityTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestStatusTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTimingTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class MedicationRequestTranslatorImplTest {
	
	public static final String DRUG_ORDER_TYPE_UUID = "131168f4-15f5-102d-96e4-000c29c2a5d7";
	
	private static final String DRUG_ORDER_UUID = "44fdc8ad-fe4d-499b-93a8-8a991c1d477e";
	
	private static final String DISCONTINUED_DRUG_ORDER_UUID = "efca4077-493c-496b-8312-856ee5d1cc27";
	
	private static final String DRUG_ORDER_NUMBER = "ORD-1";
	
	private static final String DISCONTINUED_DRUG_ORDER_NUMBER = "ORD-2";
	
	private static final String PRIOR_MEDICATION_REQUEST_REFERENCE = FhirConstants.MEDICATION_REQUEST + "/"
	        + DRUG_ORDER_UUID;
	
	private static final String DRUG_UUID = "99fdc8ad-fe4d-499b-93a8-8a991c1d477g";
	
	private static final String CONCEPT_UUID = "33fdc8ad-fe4d-499b-93a8-8a991c1d488g";
	
	private static final String PRACTITIONER_UUID = "88fdc8ad-fe4d-499b-93a8-8a991c1d477d";
	
	private static final String PATIENT_UUID = "3fe9b1dc-5385-40a4-9f79-67e54a8f3c27";
	
	private static final String ENCOUNTER_UUID = "5beacff1-de27-49b2-8672-14b646517826";
	
	private static final String COMMENT_TO_THE_FULL_FILLER = "comment to the full filler";
	
	private static final String DOSING_INSTRUCTIONS = "Dosing instructions";
	
	@Mock
	private PractitionerReferenceTranslator<Provider> providerPractitionerReferenceTranslator;
	
	@Mock
	private MedicationRequestStatusTranslator medicationRequestStatusTranslator;
	
	@Mock
	private MedicationRequestPriorityTranslator medicationRequestPriorityTranslator;
	
	@Mock
	private MedicationReferenceTranslator medicationReferenceTranslator;
	
	@Mock
	private EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private MedicationRequestTimingTranslator timingTranslator;
	
	private MedicationRequestDispenseRequestComponentTranslatorImpl dispenseRequestComponentTranslator;
	
	private DosageTranslatorImpl dosageTranslator;
	
	private MedicationRequestTranslatorImpl medicationRequestTranslator;
	
	private MedicationQuantityCodingTranslatorImpl quantityCodingTranslator;
	
	private MedicationRequest medicationRequest;
	
	private DrugOrder drugOrder;
	
	private DrugOrder discontinuedDrugOrder;
	
	@Before
	public void setup() {
		quantityCodingTranslator = new MedicationQuantityCodingTranslatorImpl();
		quantityCodingTranslator.setConceptTranslator(conceptTranslator);
		
		dosageTranslator = new DosageTranslatorImpl();
		dosageTranslator.setConceptTranslator(conceptTranslator);
		dosageTranslator.setTimingTranslator(timingTranslator);
		dosageTranslator.setQuantityCodingTranslator(quantityCodingTranslator);
		
		dispenseRequestComponentTranslator = new MedicationRequestDispenseRequestComponentTranslatorImpl();
		dispenseRequestComponentTranslator.setQuantityCodingTranslator(quantityCodingTranslator);
		
		medicationRequestTranslator = new MedicationRequestTranslatorImpl();
		medicationRequestTranslator.setStatusTranslator(medicationRequestStatusTranslator);
		medicationRequestTranslator.setPractitionerReferenceTranslator(providerPractitionerReferenceTranslator);
		medicationRequestTranslator.setMedicationRequestPriorityTranslator(medicationRequestPriorityTranslator);
		medicationRequestTranslator.setMedicationReferenceTranslator(medicationReferenceTranslator);
		medicationRequestTranslator.setConceptTranslator(conceptTranslator);
		medicationRequestTranslator.setDosageTranslator(dosageTranslator);
		medicationRequestTranslator.setEncounterReferenceTranslator(encounterReferenceTranslator);
		medicationRequestTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
		medicationRequestTranslator.setOrderIdentifierTranslator(new OrderIdentifierTranslatorImpl());
		medicationRequestTranslator
		        .setMedicationRequestDispenseRequestComponentTranslator(dispenseRequestComponentTranslator);
		
		drugOrder = new DrugOrder();
		drugOrder.setUuid(DRUG_ORDER_UUID);
		setOrderNumberByReflection(drugOrder, DRUG_ORDER_NUMBER);
		
		OrderType ordertype = new OrderType();
		ordertype.setUuid(DRUG_ORDER_TYPE_UUID);
		drugOrder.setOrderType(ordertype);
		
		discontinuedDrugOrder = new DrugOrder();
		discontinuedDrugOrder.setUuid(DISCONTINUED_DRUG_ORDER_UUID);
		setOrderNumberByReflection(discontinuedDrugOrder, DISCONTINUED_DRUG_ORDER_NUMBER);
		discontinuedDrugOrder.setPreviousOrder(drugOrder);
		
		medicationRequest = new MedicationRequest();
		medicationRequest.setId(DRUG_ORDER_UUID);
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateToOpenType() {
		DrugOrder result = medicationRequestTranslator.toOpenmrsType(new DrugOrder(), medicationRequest);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), notNullValue());
		assertThat(result.getUuid(), equalTo(DRUG_ORDER_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToFhirResource() {
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(DRUG_ORDER_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToFhirResourceWithReplacesFieldGivenDiscontinuedOrder() {
		discontinuedDrugOrder.setAction(Order.Action.DISCONTINUE);
		
		MedicationRequest result = medicationRequestTranslator.toFhirResource(discontinuedDrugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(DISCONTINUED_DRUG_ORDER_UUID));
		assertThat(result.getPriorPrescription().getReference(), equalTo(PRIOR_MEDICATION_REQUEST_REFERENCE));
		assertThat(result.getPriorPrescription().getIdentifier().getValue(), equalTo(DRUG_ORDER_NUMBER));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToFhirResourceWithReplacesFieldGivenRevisedOrder() {
		discontinuedDrugOrder.setAction(Order.Action.REVISE);
		
		MedicationRequest result = medicationRequestTranslator.toFhirResource(discontinuedDrugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(DISCONTINUED_DRUG_ORDER_UUID));
		assertThat(result.getPriorPrescription().getReference(), equalTo(PRIOR_MEDICATION_REQUEST_REFERENCE));
		assertThat(result.getPriorPrescription().getIdentifier().getValue(), equalTo(DRUG_ORDER_NUMBER));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToFhirResourceWithBasedOnFieldGivenRenewOrder() {
		discontinuedDrugOrder.setAction(Order.Action.RENEW);
		
		MedicationRequest result = medicationRequestTranslator.toFhirResource(discontinuedDrugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(DISCONTINUED_DRUG_ORDER_UUID));
		assertThat(result.getBasedOn().get(0).getReference(), equalTo(PRIOR_MEDICATION_REQUEST_REFERENCE));
		assertThat(result.getBasedOn().get(0).getIdentifier().getValue(), equalTo(DRUG_ORDER_NUMBER));
	}
	
	@Test(expected = NullPointerException.class)
	public void toOpenMrsType_shouldThrowExceptionIfMedicationIsNull() {
		medicationRequestTranslator.toOpenmrsType(new DrugOrder(), null);
	}
	
	@Test
	public void toFhirResource_shouldConvertStatusToFhirType() {
		drugOrder.setVoided(true);
		drugOrder.setVoidedBy(new User());
		
		when(medicationRequestStatusTranslator.toFhirResource(drugOrder))
		        .thenReturn(MedicationRequest.MedicationRequestStatus.CANCELLED);
		
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), notNullValue());
		assertThat(result.getStatus(), equalTo(MedicationRequest.MedicationRequestStatus.CANCELLED));
	}
	
	@Test
	public void toFhirResource_shouldConvertActiveStatusToFhirType() {
		when(medicationRequestStatusTranslator.toFhirResource(drugOrder))
		        .thenReturn(MedicationRequest.MedicationRequestStatus.ACTIVE);
		
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), notNullValue());
		assertThat(result.getStatus(), equalTo(MedicationRequest.MedicationRequestStatus.ACTIVE));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateRequesterToOpenMrsType() {
		Provider provider = new Provider();
		provider.setUuid(PRACTITIONER_UUID);
		Reference requesterReference = new Reference();
		requesterReference.setReference(FhirConstants.PRACTITIONER + "/" + PRACTITIONER_UUID);
		medicationRequest.setRequester(requesterReference);
		when(providerPractitionerReferenceTranslator.toOpenmrsType(requesterReference)).thenReturn(provider);
		
		DrugOrder result = medicationRequestTranslator.toOpenmrsType(new DrugOrder(), medicationRequest);
		assertThat(result, notNullValue());
		assertThat(result.getOrderer(), notNullValue());
		assertThat(result.getOrderer().getUuid(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOrdererToFhirType() {
		Provider provider = new Provider();
		provider.setUuid(PRACTITIONER_UUID);
		drugOrder.setOrderer(provider);
		
		Reference requesterReference = new Reference();
		requesterReference.setReference(FhirConstants.PRACTITIONER + "/" + PRACTITIONER_UUID);
		
		when(providerPractitionerReferenceTranslator.toFhirResource(provider)).thenReturn(requesterReference);
		
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getRequester(), notNullValue());
		assertThat(result.getRequester().getReference(), equalTo(requesterReference.getReference()));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslatePriorityToOpenMrsType() {
		medicationRequest.setPriority(MedicationRequest.MedicationRequestPriority.ROUTINE);
		when(medicationRequestPriorityTranslator.toOpenmrsType(MedicationRequest.MedicationRequestPriority.ROUTINE))
		        .thenReturn(DrugOrder.Urgency.ROUTINE);
		DrugOrder result = medicationRequestTranslator.toOpenmrsType(new DrugOrder(), medicationRequest);
		
		assertThat(result, notNullValue());
		assertThat(result.getUrgency(), notNullValue());
		assertThat(result.getUrgency(), equalTo(DrugOrder.Urgency.ROUTINE));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateEncounterToOpenMrsType() {
		Encounter encounter = new Encounter();
		encounter.setUuid(ENCOUNTER_UUID);
		
		Reference encounterReference = new Reference();
		encounterReference.setReference(FhirConstants.ENCOUNTER + "/" + ENCOUNTER_UUID);
		medicationRequest.setEncounter(encounterReference);
		
		when(encounterReferenceTranslator.toOpenmrsType(encounterReference)).thenReturn(encounter);
		DrugOrder result = medicationRequestTranslator.toOpenmrsType(new DrugOrder(), medicationRequest);
		
		assertThat(result, notNullValue());
		assertThat(result.getEncounter(), notNullValue());
		assertThat(result.getEncounter().getUuid(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateEncounterToFhirType() {
		Encounter encounter = new Encounter();
		encounter.setUuid(ENCOUNTER_UUID);
		drugOrder.setEncounter(encounter);
		
		Reference encounterReference = new Reference();
		encounterReference.setReference(FhirConstants.ENCOUNTER + "/" + ENCOUNTER_UUID);
		
		when(encounterReferenceTranslator.toFhirResource(encounter)).thenReturn(encounterReference);
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getEncounter(), notNullValue());
		assertThat(result.getEncounter().getReference(), equalTo(encounterReference.getReference()));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslatePatientToOpenMrsType() {
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		
		Reference patientReference = new Reference();
		patientReference.setReference(FhirConstants.PATIENT + "/" + PATIENT_UUID);
		medicationRequest.setSubject(patientReference);
		
		when(patientReferenceTranslator.toOpenmrsType(patientReference)).thenReturn(patient);
		DrugOrder result = medicationRequestTranslator.toOpenmrsType(new DrugOrder(), medicationRequest);
		
		assertThat(result, notNullValue());
		assertThat(result.getPatient(), notNullValue());
		assertThat(result.getPatient().getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslatePatientToFhirType() {
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		drugOrder.setPatient(patient);
		
		Reference patientReference = new Reference();
		patientReference.setReference(FhirConstants.PATIENT + "/" + PATIENT_UUID);
		
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientReference);
		
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getSubject(), notNullValue());
		assertThat(result.getSubject().getReference(), equalTo(patientReference.getReference()));
	}
	
	@Test
	public void toFhirResource_shouldTranslateUrgencyToFhirType() {
		drugOrder.setUrgency(DrugOrder.Urgency.ON_SCHEDULED_DATE);
		when(medicationRequestPriorityTranslator.toFhirResource(DrugOrder.Urgency.ON_SCHEDULED_DATE))
		        .thenReturn(MedicationRequest.MedicationRequestPriority.URGENT);
		
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getPriority(), notNullValue());
		assertThat(result.getPriority(), equalTo(MedicationRequest.MedicationRequestPriority.URGENT));
		
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateMedicationToOpenMrsDrug() {
		Reference medicationRef = new Reference();
		medicationRef.setReference(FhirConstants.MEDICATION_REQUEST + "/" + DRUG_UUID);
		Drug drug = new Drug();
		drug.setUuid(DRUG_UUID);
		
		medicationRequest.setMedication(medicationRef);
		when(medicationReferenceTranslator.toOpenmrsType(medicationRef)).thenReturn(drug);
		DrugOrder result = medicationRequestTranslator.toOpenmrsType(new DrugOrder(), medicationRequest);
		
		assertThat(result, notNullValue());
		assertThat(result.getDrug(), notNullValue());
		assertThat(result.getDrug().getUuid(), equalTo(DRUG_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugToMedicationReference() {
		Drug drug = new Drug();
		drug.setUuid(DRUG_UUID);
		drugOrder.setDrug(drug);
		Reference medicationRef = new Reference();
		medicationRef.setReference(FhirConstants.MEDICATION_REQUEST + "/" + DRUG_UUID);
		
		when(medicationReferenceTranslator.toFhirResource(drug)).thenReturn(medicationRef);
		
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getMedication(), notNullValue());
		assertThat(result.getMedication(), equalTo(medicationRef));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOrderReasonToReasonCode() {
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		concept.setConceptId(10023);
		drugOrder.setOrderReason(concept);
		
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding(new Coding().setCode(concept.getConceptId().toString()));
		
		when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
		
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getReasonCode(), not(empty()));
		assertThat(result.getReasonCodeFirstRep(), equalTo(codeableConcept));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateReasonCodeToOrderReason() {
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		concept.setConceptId(10023);
		
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding(new Coding().setCode(concept.getConceptId().toString()));
		medicationRequest.addReasonCode(codeableConcept);
		
		when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(concept);
		
		DrugOrder drugOrder = medicationRequestTranslator.toOpenmrsType(new DrugOrder(), medicationRequest);
		
		assertThat(drugOrder, notNullValue());
		assertThat(drugOrder.getOrderReason(), notNullValue());
		assertThat(drugOrder.getOrderReason().getUuid(), equalTo(CONCEPT_UUID));
		assertThat(drugOrder.getOrderReason().getConceptId(), equalTo(10023));
	}
	
	@Test
	public void toFhirResource_shouldTranslateCommentToFulFillerToNote() {
		drugOrder.setCommentToFulfiller(COMMENT_TO_THE_FULL_FILLER);
		
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getNote(), not(empty()));
		assertThat(result.getNoteFirstRep().getText(), equalTo(COMMENT_TO_THE_FULL_FILLER));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateNoteToCommentToFullFiller() {
		medicationRequest.addNote(new Annotation().setText(COMMENT_TO_THE_FULL_FILLER));
		
		DrugOrder result = medicationRequestTranslator.toOpenmrsType(new DrugOrder(), medicationRequest);
		
		assertThat(result, notNullValue());
		assertThat(result.getCommentToFulfiller(), equalTo(COMMENT_TO_THE_FULL_FILLER));
	}
	
	@Test
	public void toFhirResource_shouldAddDosageInstructions() {
		CodeableConcept routeFhirConcept = new CodeableConcept();
		Concept routeConcept = new Concept();
		when(conceptTranslator.toFhirResource(routeConcept)).thenReturn(routeFhirConcept);
		
		drugOrder.setAsNeeded(true);
		drugOrder.setRoute(routeConcept);
		drugOrder.setDosingInstructions(DOSING_INSTRUCTIONS);
		
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getDosageInstructionFirstRep(), notNullValue());
		assertThat(result.getDosageInstructionFirstRep().getText(), equalTo(DOSING_INSTRUCTIONS));
		assertThat(result.getDosageInstructionFirstRep().getAsNeededBooleanType().booleanValue(), is(true));
		assertThat(result.getDosageInstructionFirstRep().getRoute(), equalTo(routeFhirConcept));
	}
	
	@Test
	public void toOpenMrsType_shouldAddDosageInstructions() {
		CodeableConcept routeFhirConcept = new CodeableConcept();
		Concept routeConcept = new Concept();
		when(conceptTranslator.toOpenmrsType(routeFhirConcept)).thenReturn(routeConcept);
		
		Dosage dosage = new Dosage();
		dosage.setRoute(routeFhirConcept);
		dosage.setAsNeeded(new BooleanType(true));
		dosage.setText(DOSING_INSTRUCTIONS);
		medicationRequest.addDosageInstruction(dosage);
		
		DrugOrder result = medicationRequestTranslator.toOpenmrsType(new DrugOrder(), medicationRequest);
		assertThat(result, notNullValue());
		assertThat(result.getRoute(), equalTo(routeConcept));
		assertThat(result.getAsNeeded(), is(true));
		assertThat(result.getDosingInstructions(), equalTo(DOSING_INSTRUCTIONS));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDispenseRequest() {
		drugOrder.setQuantity(100.0);
		
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getDispenseRequest(), notNullValue());
		assertThat(result.getDispenseRequest().getQuantity().getValue().doubleValue(), equalTo(100.0));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateDispenseRequest() {
		MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequestComponent = new MedicationRequest.MedicationRequestDispenseRequestComponent();
		Quantity quantity = new Quantity();
		quantity.setValue(200.0);
		dispenseRequestComponent.setQuantity(quantity);
		medicationRequest.setDispenseRequest(dispenseRequestComponent);
		
		DrugOrder result = medicationRequestTranslator.toOpenmrsType(drugOrder, medicationRequest);
		
		assertThat(result, notNullValue());
		assertThat(result.getQuantity(), equalTo(200.0));
	}
	
	@SneakyThrows
	private void setOrderNumberByReflection(DrugOrder order, String orderNumber) {
		Class<? extends DrugOrder> clazz = order.getClass();
		Field orderNumberField = clazz.getSuperclass().getDeclaredField("orderNumber");
		boolean isAccessible = orderNumberField.isAccessible();
		if (!isAccessible) {
			orderNumberField.setAccessible(true);
		}
		
		orderNumberField.set(order, orderNumber);
	}
}
