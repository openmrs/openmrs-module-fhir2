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
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;

import org.exparity.hamcrest.date.DateMatchers;
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
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTimingTranslator;

@RunWith(MockitoJUnitRunner.class)
public class DosageTranslatorImplTest {
	
	private static final String DRUG_ORDER_UUID = "44fdc8ad-fe4d-499b-93a8-8a991c1d477e";
	
	private static final String CONCEPT_UUID = "33fdc8ad-fe4d-499b-93a8-8a991c1d488g";
	
	private static final String DOSING_INSTRUCTION = "dosing instructions";
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private MedicationRequestTimingTranslator timingTranslator;
	
	private DosageTranslatorImpl dosageTranslator;
	
	private DrugOrder drugOrder;
	
	@Before
	public void setup() {
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
	public void toFhirResource_shouldTranslateDrugOrderAsNeededToAsNeeded() {
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
	public void toFhirResource_shouldTranslateDrugOrderRouteToRoute() {
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
		Timing.TimingRepeatComponent repeatComponent = new Timing.TimingRepeatComponent();
		repeatComponent.setPeriod(1);
		repeatComponent.setPeriodUnit(Timing.UnitsOfTime.D);
		
		Timing timing = new Timing();
		timing.addEvent(new Date());
		timing.setRepeat(repeatComponent);
		when(timingTranslator.toFhirResource(drugOrder)).thenReturn(timing);
		
		Dosage result = dosageTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getTiming(), notNullValue());
		assertThat(result.getTiming().getEvent(), not(empty()));
		assertThat(result.getTiming().getEvent().get(0).getValue(), DateMatchers.sameDay(new Date()));
		assertThat(result.getTiming().getRepeat().getPeriod(), equalTo(new BigDecimal(1)));
		assertThat(result.getTiming().getRepeat().getPeriodUnit(), equalTo(Timing.UnitsOfTime.D));
	}
}
