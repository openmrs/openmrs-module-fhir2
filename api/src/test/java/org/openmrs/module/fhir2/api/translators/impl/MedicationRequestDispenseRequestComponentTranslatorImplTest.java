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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Quantity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.util.LocaleUtility;

@RunWith(MockitoJUnitRunner.class)
public class MedicationRequestDispenseRequestComponentTranslatorImplTest {
	
	@Mock
	private FhirConceptService conceptService;
	
	@Mock
	private FhirConceptSourceService conceptSourceService;
	
	private ConceptTranslatorImpl conceptTranslator;
	
	private MedicationQuantityCodingTranslatorImpl quantityCodingTranslator;
	
	private MedicationRequestDispenseRequestComponentTranslatorImpl requestTimingComponentTranslator;
	
	private DrugOrder drugOrder;
	
	private MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest;
	
	private final String CONCEPT_UUID = UUID.randomUUID().toString();
	
	private Concept unitsConcept;
	
	@Before
	public void setup() {
		conceptTranslator = new ConceptTranslatorImpl();
		conceptTranslator.setConceptService(conceptService);
		conceptTranslator.setConceptSourceService(conceptSourceService);
		
		quantityCodingTranslator = new MedicationQuantityCodingTranslatorImpl();
		quantityCodingTranslator.setConceptTranslator(conceptTranslator);
		
		requestTimingComponentTranslator = new MedicationRequestDispenseRequestComponentTranslatorImpl();
		requestTimingComponentTranslator.setQuantityCodingTranslator(quantityCodingTranslator);
		
		unitsConcept = new Concept();
		unitsConcept.setUuid(CONCEPT_UUID);
		unitsConcept.addName(new ConceptName("testUnits", Locale.ENGLISH));
		when(conceptService.get(CONCEPT_UUID)).thenReturn(unitsConcept);
		
		LocaleUtility.setLocalesAllowedListCache(Arrays.asList(Locale.ENGLISH));
		
		drugOrder = new DrugOrder();
		dispenseRequest = new MedicationRequest.MedicationRequestDispenseRequestComponent();
	}
	
	@Test
	public void toFhirResource_shouldTranslateFromQuantity() {
		drugOrder.setQuantity(123.0);
		dispenseRequest = requestTimingComponentTranslator.toFhirResource(drugOrder);
		assertThat(dispenseRequest, notNullValue());
		assertThat(dispenseRequest.getQuantity(), notNullValue());
		assertThat(dispenseRequest.getQuantity().getValue(), equalTo(new BigDecimal("123.0")));
	}
	
	@Test
	public void toFhirResource_shouldTranslateFromQuantityUnits() {
		drugOrder.setQuantity(123.0);
		drugOrder.setQuantityUnits(unitsConcept);
		dispenseRequest = requestTimingComponentTranslator.toFhirResource(drugOrder);
		assertThat(dispenseRequest, notNullValue());
		assertThat(dispenseRequest.getQuantity(), notNullValue());
		assertThat(dispenseRequest.getQuantity().getValue(), equalTo(new BigDecimal("123.0")));
		assertThat(dispenseRequest.getQuantity().getCode(), equalTo(CONCEPT_UUID));
		assertThat(dispenseRequest.getQuantity().getSystem(), nullValue());
	}
	
	@Test
	public void toFhirResource_shouldSetNullQuantityAndUnitsIfDrugOrderQuantityIsNull() {
		drugOrder.setDuration(null);
		dispenseRequest = requestTimingComponentTranslator.toFhirResource(drugOrder);
		Quantity quantity = dispenseRequest.getQuantity();
		assertThat(quantity, notNullValue());
		assertThat(quantity.getValue(), nullValue());
		assertThat(quantity.getSystem(), nullValue());
		assertThat(quantity.getCode(), nullValue());
		assertThat(quantity.getDisplay(), nullValue());
	}
	
	@Test
	public void toFhirResource_shouldTranslateFromNumRefills() {
		drugOrder.setNumRefills(9);
		dispenseRequest = requestTimingComponentTranslator.toFhirResource(drugOrder);
		assertThat(dispenseRequest, notNullValue());
		assertThat(dispenseRequest.getNumberOfRepeatsAllowed(), equalTo(9));
	}

	@Test
	public void toFhirResource_shouldTranslateDateActivatedToValidityPeriodStart() {
		Date now = new Date();
		drugOrder.setDateActivated(now);
		dispenseRequest = requestTimingComponentTranslator.toFhirResource(drugOrder);
		assertThat(dispenseRequest, notNullValue());
		assertThat(dispenseRequest.getValidityPeriod().getStart(), equalTo(now));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateToQuantity() {
		Quantity quantity = new Quantity();
		quantity.setValue(456.0);
		dispenseRequest.setQuantity(quantity);
		drugOrder = requestTimingComponentTranslator.toOpenmrsType(drugOrder, dispenseRequest);
		assertThat(drugOrder.getQuantity(), equalTo(456.0));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateToQuantityUnits() {
		Quantity quantity = new Quantity();
		quantity.setValue(456.0);
		quantity.setCode(CONCEPT_UUID);
		dispenseRequest.setQuantity(quantity);
		drugOrder = requestTimingComponentTranslator.toOpenmrsType(drugOrder, dispenseRequest);
		assertThat(drugOrder.getQuantity(), equalTo(456.0));
		assertThat(drugOrder.getQuantityUnits(), equalTo(unitsConcept));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateToNullQuantityAndUnitsIfValueIsNull() {
		Quantity quantity = new Quantity();
		quantity.setValue(null);
		quantity.setCode(CONCEPT_UUID);
		dispenseRequest.setQuantity(quantity);
		drugOrder = requestTimingComponentTranslator.toOpenmrsType(drugOrder, dispenseRequest);
		assertThat(drugOrder.getQuantity(), nullValue());
		assertThat(drugOrder.getQuantityUnits(), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateToNumRefills() {
		dispenseRequest.setNumberOfRepeatsAllowed(32);
		drugOrder = requestTimingComponentTranslator.toOpenmrsType(drugOrder, dispenseRequest);
		assertThat(drugOrder.getNumRefills(), equalTo(32));
	}

	@Test
	public void toOpenmrsType_translateToNumRefillsShouldPresesrveZero() {
		dispenseRequest.setNumberOfRepeatsAllowed(0);
		drugOrder = requestTimingComponentTranslator.toOpenmrsType(drugOrder, dispenseRequest);
		assertThat(drugOrder.getNumRefills(), equalTo(0));
	}

	@Test
	public void toOpenmrsType_translateToNumRefillsShouldPreserveNull() {
		dispenseRequest.setNumberOfRepeatsAllowedElement(null);
		drugOrder = requestTimingComponentTranslator.toOpenmrsType(drugOrder, dispenseRequest);
		assertNull(drugOrder.getNumRefills());
	}

	@Test
	public void toOpenmrsType_shouldTranslateValidityPeriodStartToDateActivated() {
		Date now = new Date();
		Period validityPeriod = new Period();
		validityPeriod.setStart(now);
		dispenseRequest.setValidityPeriod(validityPeriod);
		drugOrder = requestTimingComponentTranslator.toOpenmrsType(drugOrder, dispenseRequest);
		assertThat(drugOrder.getDateActivated(), equalTo(now));
		
	}
}
