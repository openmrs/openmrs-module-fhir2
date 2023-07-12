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

import java.util.Arrays;
import java.util.Locale;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.SimpleQuantity;
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
import org.openmrs.Duration;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.util.LocaleUtility;

@RunWith(MockitoJUnitRunner.class)
public class MedicationQuantityCodingTranslatorImplTest {
	
	private static final String CONCEPT_UUID = "33fdc8ad-fe4d-499b-93a8-8a991c1d488g";
	
	private ConceptTranslatorImpl conceptTranslator;
	
	@Mock
	private FhirConceptService conceptService;
	
	@Mock
	private FhirConceptSourceService conceptSourceService;
	
	private MedicationQuantityCodingTranslatorImpl quantityCodingTranslator;
	
	@Before
	public void setup() {
		conceptTranslator = new ConceptTranslatorImpl();
		conceptTranslator.setConceptService(conceptService);
		conceptTranslator.setConceptSourceService(conceptSourceService);
		
		quantityCodingTranslator = new MedicationQuantityCodingTranslatorImpl();
		quantityCodingTranslator.setConceptTranslator(conceptTranslator);
		
		LocaleUtility.setLocalesAllowedListCache(Arrays.asList(Locale.ENGLISH));
	}
	
	@Test
	public void toFhirResource_shouldTranslateMedicationQuantityPreferringRxNormIfPresent() {
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
		
		Coding result = quantityCodingTranslator.toFhirResource(mg);
		
		assertThat(result, notNullValue());
		assertThat(result.getDisplay(), is("mg"));
		assertThat(result.getSystem(), is(FhirConstants.RX_NORM_SYSTEM_URI));
		assertThat(result.getCode(), is("rx-norm-mg-code"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateMedicationQuantityPreferringSnomedCtIfPresent() {
		ConceptMapType sameAs = new ConceptMapType();
		sameAs.setUuid(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
		
		ConceptSource snomed = new ConceptSource();
		snomed.setHl7Code(Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE);
		when(conceptSourceService.getUrlForConceptSource(snomed)).thenReturn(FhirConstants.SNOMED_SYSTEM_URI);
		
		Concept mg = new Concept();
		mg.addName(new ConceptName("mg", Locale.ENGLISH));
		mg.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(snomed, "snomed-ct-mg-code", "snomed"), sameAs));
		
		Coding result = quantityCodingTranslator.toFhirResource(mg);
		
		assertThat(result, notNullValue());
		assertThat(result.getDisplay(), is("mg"));
		assertThat(result.getSystem(), is(FhirConstants.SNOMED_SYSTEM_URI));
		assertThat(result.getCode(), is("snomed-ct-mg-code"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateMedicationQuantityDefaultingToUuid() {
		Concept mg = new Concept();
		mg.setUuid(CONCEPT_UUID);
		mg.addName(new ConceptName("mg", Locale.ENGLISH));
		
		Coding result = quantityCodingTranslator.toFhirResource(mg);
		assertThat(result, notNullValue());
		assertThat(result.getDisplay(), is("mg"));
		assertThat(result.getSystem(), nullValue());
		assertThat(result.getCode(), is(CONCEPT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateDoseQuantityUnitsToDrugOrderDoseUnits() {
		Concept mg = new Concept();
		when(conceptService.get(CONCEPT_UUID)).thenReturn(mg);
		
		SimpleQuantity medicationQuantity = new SimpleQuantity();
		medicationQuantity.setValue(1000d);
		medicationQuantity.setSystem(null);
		medicationQuantity.setCode(CONCEPT_UUID);
		
		Concept result = quantityCodingTranslator.toOpenmrsType(medicationQuantity);
		assertThat(result, notNullValue());
		assertThat(result, equalTo(mg));
	}
}
