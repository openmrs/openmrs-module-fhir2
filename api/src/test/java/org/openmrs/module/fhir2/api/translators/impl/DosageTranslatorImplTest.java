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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Locale;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.SimpleQuantity;
import org.hl7.fhir.r4.model.Timing;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.DrugOrder;
import org.openmrs.Duration;
import org.openmrs.OrderFrequency;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.util.LocaleUtility;

@RunWith(MockitoJUnitRunner.class)
public class DosageTranslatorImplTest {
	
	private static final String DRUG_ORDER_UUID = "44fdc8ad-fe4d-499b-93a8-8a991c1d477e";
	
	private static final String CONCEPT_UUID = "33fdc8ad-fe4d-499b-93a8-8a991c1d488g";
	
	private static final String DOSING_INSTRUCTION = "dosing instructions";
	
	private ConceptTranslatorImpl conceptTranslator;
	
	private MedicationQuantityCodingTranslatorImpl quantityCodingTranslator;
	
	@Mock
	private FhirConceptService conceptService;
	
	@Mock
	private FhirConceptSourceService conceptSourceService;
	
	@Mock
	private OrderService orderService;
	
	private MedicationRequestTimingTranslatorImpl timingTranslator;
	
	private DosageTranslatorImpl dosageTranslator;
	
	private DrugOrder drugOrder;
	
	@Before
	public void setup() {
		conceptTranslator = new ConceptTranslatorImpl();
		conceptTranslator.setConceptService(conceptService);
		conceptTranslator.setConceptSourceService(conceptSourceService);
		
		quantityCodingTranslator = new MedicationQuantityCodingTranslatorImpl();
		quantityCodingTranslator.setConceptTranslator(conceptTranslator);
		
		timingTranslator = new MedicationRequestTimingTranslatorImpl();
		timingTranslator.setConceptTranslator(conceptTranslator);
		timingTranslator.setOrderService(orderService);
		timingTranslator.setTimingRepeatComponentTranslator(new MedicationRequestTimingRepeatComponentTranslatorImpl());
		
		dosageTranslator = new DosageTranslatorImpl();
		dosageTranslator.setConceptTranslator(conceptTranslator);
		dosageTranslator.setTimingTranslator(timingTranslator);
		dosageTranslator.setQuantityCodingTranslator(quantityCodingTranslator);
		
		drugOrder = new DrugOrder();
		drugOrder.setUuid(DRUG_ORDER_UUID);
		
		LocaleUtility.setLocalesAllowedListCache(Arrays.asList(Locale.ENGLISH));
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
		drugOrder.setRoute(concept);
		
		Dosage result = dosageTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getRoute().getCodingFirstRep().getCode(), equalTo(CONCEPT_UUID));
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
		timingConcept.setUuid(CONCEPT_UUID);
		
		OrderFrequency timingFrequency = new OrderFrequency();
		timingFrequency.setConcept(timingConcept);
		drugOrder.setFrequency(timingFrequency);
		
		Dosage result = dosageTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getTiming(), notNullValue());
		assertThat(result.getTiming().getCode().getCodingFirstRep().getCode(), equalTo(CONCEPT_UUID));
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
		routeFhirConcept.addCoding(new Coding(null, CONCEPT_UUID, "route"));
		when(conceptService.get(CONCEPT_UUID)).thenReturn(routeConcept);
		
		Dosage dosage = new Dosage();
		dosage.setRoute(routeFhirConcept);
		DrugOrder result = dosageTranslator.toOpenmrsType(new DrugOrder(), dosage);
		assertThat(result.getRoute(), is(routeConcept));
	}
	
	@Test
	public void toOpenmrsType_shouldSetDosageTiming() {
		Concept timingConcept = new Concept();
		CodeableConcept timingFhirConcept = new CodeableConcept();
		timingFhirConcept.addCoding(new Coding(null, CONCEPT_UUID, "timing"));
		when(conceptService.get(CONCEPT_UUID)).thenReturn(timingConcept);
		OrderFrequency timingFrequency = new OrderFrequency();
		when(orderService.getOrderFrequencyByConcept(timingConcept)).thenReturn(timingFrequency);
		
		Dosage dosage = new Dosage();
		Timing timing = new Timing();
		timing.setCode(timingFhirConcept);
		dosage.setTiming(timing);
		
		DrugOrder result = dosageTranslator.toOpenmrsType(new DrugOrder(), dosage);
		assertThat(result, notNullValue());
		assertThat(result.getFrequency(), equalTo(timingFrequency));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderDoseToDoseQuantity() {
		Concept mg = new Concept();
		mg.addName(new ConceptName("mg", Locale.ENGLISH));
		mg.setUuid(CONCEPT_UUID);
		
		drugOrder.setDose(20.0);
		drugOrder.setDoseUnits(mg);
		Dosage result = dosageTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getDoseAndRate().get(0).getDoseQuantity().getValue().doubleValue(), equalTo(20.0));
		assertThat(result.getDoseAndRate().get(0).getDoseQuantity().getUnit(), is("mg"));
		assertNull(result.getDoseAndRate().get(0).getDoseQuantity().getSystem());
		assertThat(result.getDoseAndRate().get(0).getDoseQuantity().getCode(), is(CONCEPT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderDoseToDoseQuantityPreferringRxNormIfPresent() {
		ConceptMapType sameAs = new ConceptMapType();
		sameAs.setUuid(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
		
		ConceptSource snomed = new ConceptSource();
		snomed.setHl7Code(Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE);
		
		ConceptSource rxNorm = new ConceptSource();
		rxNorm.setName("rxnorm");
		when(conceptSourceService.getUrlForConceptSource(rxNorm)).thenReturn(FhirConstants.RX_NORM_SYSTEM_URI);
		
		Concept mg = new Concept();
		mg.addName(new ConceptName("mg", Locale.ENGLISH));
		mg.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(snomed, "snomed-ct-mg-code", "snomed"), sameAs));
		mg.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(rxNorm, "rx-norm-mg-code", "rxnorm"), sameAs));
		
		drugOrder.setDose(20.0);
		drugOrder.setDoseUnits(mg);
		Dosage result = dosageTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getDoseAndRate().get(0).getDoseQuantity().getValue().doubleValue(), equalTo(20.0));
		assertThat(result.getDoseAndRate().get(0).getDoseQuantity().getUnit(), is("mg"));
		assertThat(result.getDoseAndRate().get(0).getDoseQuantity().getSystem(), is(FhirConstants.RX_NORM_SYSTEM_URI));
		assertThat(result.getDoseAndRate().get(0).getDoseQuantity().getCode(), is("rx-norm-mg-code"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugOrderDoseToDoseQuantityPreferringSnomedCtIfPresent() {
		ConceptMapType sameAs = new ConceptMapType();
		sameAs.setUuid(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
		
		ConceptSource snomed = new ConceptSource();
		snomed.setHl7Code(Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE);
		when(conceptSourceService.getUrlForConceptSource(snomed)).thenReturn(FhirConstants.SNOMED_SYSTEM_URI);
		
		ConceptSource rxNorm = new ConceptSource();
		rxNorm.setName("rxnorm");
		
		Concept mg = new Concept();
		mg.addName(new ConceptName("mg", Locale.ENGLISH));
		mg.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(snomed, "snomed-ct-mg-code", "snomed"), sameAs));
		
		drugOrder.setDose(20.0);
		drugOrder.setDoseUnits(mg);
		
		Dosage result = dosageTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getDoseAndRate().get(0).getDoseQuantity().getValue().doubleValue(), equalTo(20.0));
		assertThat(result.getDoseAndRate().get(0).getDoseQuantity().getUnit(), is("mg"));
		assertThat(result.getDoseAndRate().get(0).getDoseQuantity().getSystem(), is(FhirConstants.SNOMED_SYSTEM_URI));
		assertThat(result.getDoseAndRate().get(0).getDoseQuantity().getCode(), is("snomed-ct-mg-code"));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateDoseQuantityToDrugOrderDose() {
		double dosageValue = 12345.0;
		Dosage dosage = new Dosage();
		Dosage.DosageDoseAndRateComponent doseAndRateComponent = new Dosage.DosageDoseAndRateComponent();
		SimpleQuantity doseQuantity = new SimpleQuantity();
		doseQuantity.setValue(dosageValue);
		doseAndRateComponent.setDose(doseQuantity);
		dosage.addDoseAndRate(doseAndRateComponent);
		
		DrugOrder result = dosageTranslator.toOpenmrsType(new DrugOrder(), dosage);
		assertThat(result, notNullValue());
		assertThat(result.getDose(), equalTo(dosageValue));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateDoseQuantityUnitsToDrugOrderDoseUnits() {
		Concept mg = new Concept();
		when(conceptService.get(CONCEPT_UUID)).thenReturn(mg);
		
		Dosage dosage = new Dosage();
		Dosage.DosageDoseAndRateComponent doseAndRateComponent = new Dosage.DosageDoseAndRateComponent();
		SimpleQuantity doseQuantity = new SimpleQuantity();
		doseQuantity.setValue(12345.0);
		doseQuantity.setSystem(null);
		doseQuantity.setCode(CONCEPT_UUID);
		doseAndRateComponent.setDose(doseQuantity);
		dosage.addDoseAndRate(doseAndRateComponent);
		
		DrugOrder result = dosageTranslator.toOpenmrsType(new DrugOrder(), dosage);
		assertThat(result, notNullValue());
		assertThat(result.getDoseUnits(), equalTo(mg));
	}
}
