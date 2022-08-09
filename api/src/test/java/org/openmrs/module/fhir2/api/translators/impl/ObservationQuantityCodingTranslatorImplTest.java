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
import static org.openmrs.module.fhir2.FhirConstants.UCUM_SYSTEM_URI;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Optional;

import junitparams.FileParameters;
import junitparams.JUnitParamsRunner;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.SimpleQuantity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSource;
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;

@RunWith(JUnitParamsRunner.class)
public class ObservationQuantityCodingTranslatorImplTest {
	
	private static final String CONCEPT_UUID = "33fdc8ad-fe4d-499b-93a8-8a991c1d488g";
	
	private static final String baseUcumServiceXml = "ucum-essence.xml";
	
	private static final String fhirUcumServiceXml = "ucum-fhir-essence.xml";
	
	@Rule
	public MockitoRule rule = MockitoJUnit.rule();
	
	@Mock
	private FhirConceptService conceptService;
	
	@Mock
	private FhirConceptSourceService conceptSourceService;
	
	ObservationQuantityCodingTranslatorImpl quantityCodingTranslator;
	
	@Before
	public void setup() throws UcumException, IOException {
		ConceptTranslatorImpl conceptTranslator = new ConceptTranslatorImpl();
		conceptTranslator.setConceptService(conceptService);
		conceptTranslator.setConceptSourceService(conceptSourceService);
		
		// Define UCUM services
		ArrayList<UcumEssenceService> ucumServices = new ArrayList<>();
		try (InputStream baseUcum = UcumService.class.getClassLoader().getResourceAsStream(baseUcumServiceXml);
		        InputStream fhirUcum = this.getClass().getClassLoader().getResourceAsStream(fhirUcumServiceXml)) {
			ucumServices.add(new UcumEssenceService(baseUcum));
			ucumServices.add(new UcumEssenceService(fhirUcum));
		}
		
		quantityCodingTranslator = new ObservationQuantityCodingTranslatorImpl();
		quantityCodingTranslator.setConceptTranslator(conceptTranslator);
		quantityCodingTranslator.setUcumServices(ucumServices);
		
		quantityCodingTranslator.setConceptTranslator(conceptTranslator);
	}
	
	/**
	 * Tests all common fhir ucum codes specified in https://www.hl7.org/fhir/valueset-ucum-common.html
	 */
	@Test
	@FileParameters("classpath:org/openmrs/module/fhir2/api/translators.impl/fhir-ucum-common.csv")
	public void toFhirResource_shouldTranslateConceptWithCommonUCUMUnitCodes(String code) {
		ConceptNumeric cn = new ConceptNumeric();
		
		cn.setUnits(code);
		Coding coding = quantityCodingTranslator.toFhirResource(cn);
		
		assertThat(String.format("%s should be a valid UCUM code", code), coding.getSystem(), is(UCUM_SYSTEM_URI));
		assertThat(coding.getCode(), is(code));
	}
	
	@Test
	public void toFhirResource_shouldFallbackToUUIDAndNullSystemIfNoUCUMUnitCode() {
		ConceptNumeric cn = new ConceptNumeric();
		cn.setUuid(CONCEPT_UUID);
		
		cn.setUnits("thisiscertainlynotaunit");
		Coding coding = quantityCodingTranslator.toFhirResource(cn);
		assertThat(coding.getCode(), is(CONCEPT_UUID));
		assertNull(coding.getSystem());
		
		// UCUM lookup is case-sensitive
		cn.setUnits("mmhg");
		coding = quantityCodingTranslator.toFhirResource(cn);
		assertThat(coding.getCode(), is(CONCEPT_UUID));
		assertNull(coding.getSystem());
		
		cn.setUnits("mm[hg]");
		coding = quantityCodingTranslator.toFhirResource(cn);
		assertThat(coding.getCode(), is(CONCEPT_UUID));
		assertNull(coding.getSystem());
		
		cn.setUnits("/MIN");
		coding = quantityCodingTranslator.toFhirResource(cn);
		assertThat(coding.getCode(), is(CONCEPT_UUID));
		assertNull(coding.getSystem());
		
		cn.setUnits(null);
		coding = quantityCodingTranslator.toFhirResource(cn);
		assertNull(coding.getSystem());
		assertThat(coding.getCode(), is(CONCEPT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateNullSystemQuantityToNullSystemConcept() {
		Concept mg = new Concept();
		when(conceptService.get(CONCEPT_UUID)).thenReturn(mg);
		
		SimpleQuantity observationQuantity = new SimpleQuantity();
		observationQuantity.setValue(1000d);
		observationQuantity.setSystem(null);
		observationQuantity.setCode(CONCEPT_UUID);
		
		Concept result = quantityCodingTranslator.toOpenmrsType(observationQuantity);
		assertThat(result, notNullValue());
		assertThat(result, equalTo(mg));
	}
	
	@Test
	public void toOpenmrsType_shouldBeUnableToTranslateUCUMSystemQuantityIfOpenMRSUcumSystemIsUndefined() {
		when(conceptSourceService.getConceptSourceByUrl(UCUM_SYSTEM_URI)).thenReturn(Optional.empty());
		
		SimpleQuantity observationQuantity = new SimpleQuantity();
		observationQuantity.setValue(1000d);
		observationQuantity.setSystem(UCUM_SYSTEM_URI);
		observationQuantity.setCode("mg");
		
		Concept result = quantityCodingTranslator.toOpenmrsType(observationQuantity);
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateUCUMSystemQuantityIfOpenMRSUcumSystemDefined() {
		Concept mg = new Concept();
		ConceptSource mgSource = new ConceptSource();
		mgSource.setName("UCUM");
		
		when(conceptSourceService.getConceptSourceByUrl(UCUM_SYSTEM_URI)).thenReturn(Optional.of(mgSource));
		when(conceptService.getConceptWithAnyMappingInSource(mgSource, "mg")).thenReturn(Optional.of(mg));
		
		SimpleQuantity observationQuantity = new SimpleQuantity();
		observationQuantity.setValue(1000d);
		observationQuantity.setSystem(UCUM_SYSTEM_URI);
		observationQuantity.setCode("mg");
		
		Concept result = quantityCodingTranslator.toOpenmrsType(observationQuantity);
		assertThat(result, notNullValue());
		assertThat(result, equalTo(mg));
	}
}
