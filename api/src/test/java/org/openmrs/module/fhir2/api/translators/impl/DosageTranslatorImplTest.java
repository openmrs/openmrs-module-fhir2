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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Timing;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.OrderFrequency;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;

@RunWith(MockitoJUnitRunner.class)
public class DosageTranslatorImplTest {
	
	private static final String DRUG_ORDER_UUID = "44fdc8ad-fe4d-499b-93a8-8a991c1d477e";
	
	private static final String CONCEPT_UUID = "33fdc8ad-fe4d-499b-93a8-8a991c1d488g";
	
	private static final String DOSING_INSTRUCTION = "dosing instructions";
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private OrderService orderService;
	
	private MedicationRequestTimingTranslatorImpl timingTranslator;
	
	private DosageTranslatorImpl dosageTranslator;
	
	private DrugOrder drugOrder;
	
	@Before
	public void setup() {
		timingTranslator = new MedicationRequestTimingTranslatorImpl();
		timingTranslator.setConceptTranslator(conceptTranslator);
		timingTranslator.setOrderService(orderService);
		timingTranslator.setTimingRepeatComponentTranslator(new MedicationRequestTimingRepeatComponentTranslatorImpl());
		
		dosageTranslator = new DosageTranslatorImpl();
		dosageTranslator.setConceptTranslator(conceptTranslator);
		dosageTranslator.setTimingTranslator(timingTranslator);
		
		drugOrder = new DrugOrder();
		drugOrder.setUuid(DRUG_ORDER_UUID);
	}
	
	@Test
	public void toFhirResource_shouldTranslateDosingInstructionToDosageText() {
		drugOrder.setDosingInstructions(DOSING_INSTRUCTION);
		Dosage result = dosageTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getText(), equalTo(DOSING_INSTRUCTION));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderRouteToRoute() {
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		concept.setConceptId(1000);
		
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding(new Coding().setCode(concept.getConceptId().toString()));
		drugOrder.setRoute(concept);
		when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
		
		Dosage result = dosageTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getRoute(), equalTo(codeableConcept));
		assertThat(result.getRoute().getCodingFirstRep().getCode(), equalTo("1000"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderAsNeededToAsNeeded() {
		drugOrder.setAsNeeded(true);
		Dosage result = dosageTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getAsNeededBooleanType().booleanValue(), is(true));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfDrugOrderIsNull() {
		assertThat(dosageTranslator.toFhirResource(null), nullValue());
	}
	
	@Test
	public void toFhirResource_shouldSetDosageTiming() {
		Concept timingConcept = new Concept();
		CodeableConcept timingFhirConcept = new CodeableConcept();
		OrderFrequency timingFrequency = new OrderFrequency();
		timingFrequency.setConcept(timingConcept);
		when(conceptTranslator.toFhirResource(timingConcept)).thenReturn(timingFhirConcept);
		
		drugOrder.setFrequency(timingFrequency);
		
		Dosage result = dosageTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getTiming(), notNullValue());
		assertThat(result.getTiming().getCode(), equalTo(timingFhirConcept));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateTextToDosingInstruction() {
		Dosage dosage = new Dosage();
		dosage.setText(DOSING_INSTRUCTION);
		DrugOrder result = dosageTranslator.toOpenmrsType(new DrugOrder(), dosage);
		assertThat(result.getDosingInstructions(), equalTo(DOSING_INSTRUCTION));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateAsNeededToAsNeeded() {
		Dosage dosage = new Dosage();
		dosage.setAsNeeded(new BooleanType(true));
		DrugOrder result = dosageTranslator.toOpenmrsType(new DrugOrder(), dosage);
		assertThat(result.getAsNeeded(), is(true));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateRouteToRoute() {
		Concept routeConcept = new Concept();
		CodeableConcept routeFhirConcept = new CodeableConcept();
		when(conceptTranslator.toOpenmrsType(routeFhirConcept)).thenReturn(routeConcept);
		
		Dosage dosage = new Dosage();
		dosage.setRoute(routeFhirConcept);
		DrugOrder result = dosageTranslator.toOpenmrsType(new DrugOrder(), dosage);
		assertThat(result.getRoute(), is(routeConcept));
	}
	
	@Test
	public void toOpenmrsType_shouldSetDosageTiming() {
		Concept timingConcept = new Concept();
		CodeableConcept timingFhirConcept = new CodeableConcept();
		OrderFrequency timingFrequency = new OrderFrequency();
		when(conceptTranslator.toOpenmrsType(timingFhirConcept)).thenReturn(timingConcept);
		when(orderService.getOrderFrequencyByConcept(timingConcept)).thenReturn(timingFrequency);
		
		Dosage dosage = new Dosage();
		Timing timing = new Timing();
		timing.setCode(timingFhirConcept);
		dosage.setTiming(timing);
		
		DrugOrder result = dosageTranslator.toOpenmrsType(new DrugOrder(), dosage);
		assertThat(result, notNullValue());
		assertThat(result.getFrequency(), equalTo(timingFrequency));
	}
}
