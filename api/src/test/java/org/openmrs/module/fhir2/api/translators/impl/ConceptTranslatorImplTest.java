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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
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
import org.openmrs.module.fhir2.FhirTestConstants;
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.openmrs.module.fhir2.model.FhirConceptSource;

@RunWith(MockitoJUnitRunner.class)
public class ConceptTranslatorImplTest {
	
	private static final String CONCEPT_UUID = "12345-abcdef-12345";
	
	private static final String CONCEPT_NAME = "concept-name";
	
	@Mock
	private FhirConceptService conceptService;
	
	@Mock
	private FhirConceptSourceService conceptSourceService;
	
	@Mock
	private Concept concept;
	
	private ConceptTranslatorImpl conceptTranslator;
	
	@Before
	public void setup() {
		conceptTranslator = new ConceptTranslatorImpl();
		conceptTranslator.setConceptService(conceptService);
		conceptTranslator.setConceptSourceService(conceptSourceService);
	}
	
	@Before
	public void setupMocks() {
		ConceptName conceptName = mock(ConceptName.class);
		concept.addName(conceptName);
		when(conceptName.getName()).thenReturn(CONCEPT_NAME);
		when(concept.getName()).thenReturn(conceptName);
	}
	
	@Test
	public void shouldTranslateConceptToCodeableConcept() {
		when(concept.getUuid()).thenReturn(CONCEPT_UUID);
		
		CodeableConcept result = conceptTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), not(empty()));
		assertThat(result.getCoding().get(0).getSystem(), nullValue());
		assertThat(result.getCoding().get(0).getCode(), equalTo(CONCEPT_UUID));
		assertThat(result.getCoding().get(0).getDisplay(), equalTo(CONCEPT_NAME));
	}
	
	@Test
	public void shouldTranslateLOINCMappingForLOINCMappedConcept() {
		Collection<ConceptMap> conceptMaps = new ArrayList<>();
		ConceptMap conceptMap = mock(ConceptMap.class);
		conceptMaps.add(conceptMap);
		ConceptReferenceTerm conceptReferenceTerm = mock(ConceptReferenceTerm.class);
		ConceptSource conceptSource = mock(ConceptSource.class);
		ConceptMapType conceptMapType = mock(ConceptMapType.class);
		conceptMap.setConceptMapType(conceptMapType);
		
		when(conceptMap.getConceptReferenceTerm()).thenReturn(conceptReferenceTerm);
		when(conceptReferenceTerm.getConceptSource()).thenReturn(conceptSource);
		when(conceptReferenceTerm.getCode()).thenReturn("1000-1");
		when(conceptSource.getName()).thenReturn("LOINC");
		when(concept.getConceptMappings()).thenReturn(conceptMaps);
		
		FhirConceptSource loinc = new FhirConceptSource();
		ConceptSource loincConceptSource = new ConceptSource();
		loincConceptSource.setName("LOINC");
		loinc.setConceptSource(loincConceptSource);
		loinc.setUrl(FhirTestConstants.LOINC_SYSTEM_URL);
		when(conceptSourceService.getFhirConceptSourceByConceptSourceName("LOINC")).thenReturn(Optional.of(loinc));
		when(conceptMap.getConceptMapType()).thenReturn(conceptMapType);
		when(conceptMapType.getUuid()).thenReturn(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
		
		CodeableConcept result = conceptTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), not(empty()));
		assertThat(result.getCoding(), hasItem(hasProperty("system", equalTo(FhirTestConstants.LOINC_SYSTEM_URL))));
		assertThat(result.getCoding(), hasItem(hasProperty("code", equalTo("1000-1"))));
		assertThat(result.getCoding(), hasItem(hasProperty("display", equalTo(CONCEPT_NAME))));
	}
	
	@Test
	public void shouldTranslateCIELMappingForCIELMappedConcept() {
		Collection<ConceptMap> conceptMaps = new ArrayList<>();
		ConceptMap conceptMap = mock(ConceptMap.class);
		conceptMaps.add(conceptMap);
		ConceptReferenceTerm conceptReferenceTerm = mock(ConceptReferenceTerm.class);
		ConceptSource conceptSource = mock(ConceptSource.class);
		ConceptMapType conceptMapType = mock(ConceptMapType.class);
		conceptMap.setConceptMapType(conceptMapType);
		
		when(conceptMap.getConceptReferenceTerm()).thenReturn(conceptReferenceTerm);
		when(conceptReferenceTerm.getConceptSource()).thenReturn(conceptSource);
		when(conceptReferenceTerm.getCode()).thenReturn("1650");
		when(conceptSource.getName()).thenReturn("CIEL");
		when(concept.getConceptMappings()).thenReturn(conceptMaps);
		when(conceptMap.getConceptMapType()).thenReturn(conceptMapType);
		when(conceptMapType.getUuid()).thenReturn(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
		
		FhirConceptSource ciel = new FhirConceptSource();
		ConceptSource cielConceptSource = new ConceptSource();
		cielConceptSource.setName("CIEL");
		ciel.setConceptSource(cielConceptSource);
		ciel.setUrl(FhirTestConstants.CIEL_SYSTEM_URN);
		when(conceptSourceService.getFhirConceptSourceByConceptSourceName("CIEL")).thenReturn(Optional.of(ciel));
		
		CodeableConcept result = conceptTranslator.toFhirResource(concept);
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), not(empty()));
		assertThat(result.getCoding(), hasItem(hasProperty("system", equalTo(FhirTestConstants.CIEL_SYSTEM_URN))));
		assertThat(result.getCoding(), hasItem(hasProperty("code", equalTo("1650"))));
		assertThat(result.getCoding(), hasItem(hasProperty("display", equalTo(CONCEPT_NAME))));
	}
	
	@Test
	public void shouldNotTranslateUnknownMappingCodeWithMappingTypeSAME_AS() {
		Collection<ConceptMap> conceptMaps = new ArrayList<>();
		ConceptMap conceptMap = mock(ConceptMap.class);
		conceptMaps.add(conceptMap);
		ConceptReferenceTerm conceptReferenceTerm = mock(ConceptReferenceTerm.class);
		ConceptSource conceptSource = mock(ConceptSource.class);
		ConceptMapType conceptMapType = mock(ConceptMapType.class);
		conceptMap.setConceptMapType(conceptMapType);
		
		when(conceptMap.getConceptReferenceTerm()).thenReturn(conceptReferenceTerm);
		when(conceptReferenceTerm.getConceptSource()).thenReturn(conceptSource);
		when(conceptSource.getName()).thenReturn("Unknown");
		when(concept.getConceptMappings()).thenReturn(conceptMaps);
		when(conceptSourceService.getFhirConceptSourceByConceptSourceName("Unknown")).thenReturn(Optional.empty());
		when(conceptMap.getConceptMapType()).thenReturn(conceptMapType);
		when(conceptMapType.getUuid()).thenReturn(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
		
		CodeableConcept result = conceptTranslator.toFhirResource(concept);
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), not(hasItem(hasProperty("code", equalTo("1650")))));
	}
	
	@Test
	public void shouldReturnNullWhenConceptNull() {
		assertThat(conceptTranslator.toFhirResource(null), nullValue());
	}
	
	@Test
	public void shouldTranslateCodeableConceptToConcept() {
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding baseCoding = codeableConcept.addCoding();
		baseCoding.setCode(CONCEPT_UUID);
		
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		when(conceptService.get(CONCEPT_UUID)).thenReturn(concept);
		
		Concept result = conceptTranslator.toOpenmrsType(codeableConcept);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(CONCEPT_UUID));
	}
	
	@Test
	public void shouldTranslateLOINCCodeableConceptToConcept() {
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding loincCoding = codeableConcept.addCoding();
		loincCoding.setSystem(FhirTestConstants.LOINC_SYSTEM_URL);
		loincCoding.setCode("1000-1");
		
		Concept concept = new Concept();
		ConceptMap conceptMap = new ConceptMap();
		ConceptReferenceTerm conceptReferenceTerm = new ConceptReferenceTerm();
		ConceptSource loinc = new ConceptSource();
		loinc.setName("LOINC");
		conceptReferenceTerm.setConceptSource(loinc);
		conceptReferenceTerm.setCode("1000-1");
		conceptMap.setConceptReferenceTerm(conceptReferenceTerm);
		ConceptMapType conceptMapType = new ConceptMapType();
		conceptMap.setConceptMapType(conceptMapType);
		concept.addConceptMapping(conceptMap);
		when(conceptService.getConceptBySourceNameAndCode("LOINC", "1000-1")).thenReturn(Optional.of(concept));
		
		FhirConceptSource fhirLoincSource = new FhirConceptSource();
		fhirLoincSource.setConceptSource(loinc);
		fhirLoincSource.setUrl(FhirTestConstants.LOINC_SYSTEM_URL);
		when(conceptSourceService.getFhirConceptSourceByUrl(FhirTestConstants.LOINC_SYSTEM_URL))
		        .thenReturn(Optional.of(fhirLoincSource));
		
		Concept result = conceptTranslator.toOpenmrsType(codeableConcept);
		assertThat(result, notNullValue());
		assertThat(result.getConceptMappings(), notNullValue());
		assertThat(result.getConceptMappings(), not(empty()));
		assertThat(result.getConceptMappings(),
		    hasItem(hasProperty("conceptReferenceTerm", hasProperty("code", equalTo("1000-1")))));
	}
	
	@Test
	public void shouldFavorConceptWithMatchingSystemOverUUID() {
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding baseCoding = codeableConcept.addCoding();
		baseCoding.setCode(CONCEPT_UUID);
		Coding loincCoding = codeableConcept.addCoding();
		loincCoding.setSystem(FhirTestConstants.LOINC_SYSTEM_URL);
		loincCoding.setCode("1000-1");
		
		Concept defaultConcept = new Concept();
		defaultConcept.setUuid(CONCEPT_UUID);
		when(conceptService.get(CONCEPT_UUID)).thenReturn(defaultConcept);
		
		Concept loincConcept = new Concept();
		loincConcept.setUuid(FhirUtils.newUuid());
		ConceptMap conceptMap = new ConceptMap();
		ConceptReferenceTerm conceptReferenceTerm = new ConceptReferenceTerm();
		ConceptSource loinc = new ConceptSource();
		loinc.setName("LOINC");
		conceptReferenceTerm.setConceptSource(loinc);
		conceptReferenceTerm.setCode("1000-1");
		conceptMap.setConceptReferenceTerm(conceptReferenceTerm);
		ConceptMapType conceptMapType = new ConceptMapType();
		conceptMap.setConceptMapType(conceptMapType);
		loincConcept.addConceptMapping(conceptMap);
		when(conceptService.getConceptBySourceNameAndCode("LOINC", "1000-1")).thenReturn(Optional.of(loincConcept));
		
		FhirConceptSource fhirLoincSource = new FhirConceptSource();
		fhirLoincSource.setConceptSource(loinc);
		fhirLoincSource.setUrl(FhirTestConstants.LOINC_SYSTEM_URL);
		when(conceptSourceService.getFhirConceptSourceByUrl(FhirTestConstants.LOINC_SYSTEM_URL))
		        .thenReturn(Optional.of(fhirLoincSource));
		
		Concept result = conceptTranslator.toOpenmrsType(codeableConcept);
		assertThat(result.getUuid(), equalTo(loincConcept.getUuid()));
	}
	
	@Test
	public void shouldTranslateCIELCodeableConceptToConcept() {
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding cielCoding = codeableConcept.addCoding();
		cielCoding.setSystem(FhirTestConstants.CIEL_SYSTEM_URN);
		cielCoding.setCode("1650");
		
		Concept concept = new Concept();
		ConceptMap conceptMap = new ConceptMap();
		ConceptReferenceTerm conceptReferenceTerm = new ConceptReferenceTerm();
		ConceptSource ciel = new ConceptSource();
		ciel.setName("CIEL");
		conceptReferenceTerm.setConceptSource(ciel);
		conceptReferenceTerm.setCode("1650");
		conceptMap.setConceptReferenceTerm(conceptReferenceTerm);
		ConceptMapType conceptMapType = new ConceptMapType();
		conceptMap.setConceptMapType(conceptMapType);
		concept.addConceptMapping(conceptMap);
		when(conceptService.getConceptBySourceNameAndCode("CIEL", "1650")).thenReturn(Optional.of(concept));
		
		FhirConceptSource fhirCielSource = new FhirConceptSource();
		fhirCielSource.setConceptSource(ciel);
		fhirCielSource.setUrl(FhirTestConstants.CIEL_SYSTEM_URN);
		when(conceptSourceService.getFhirConceptSourceByUrl(FhirTestConstants.CIEL_SYSTEM_URN))
		        .thenReturn(Optional.of(fhirCielSource));
		
		Concept result = conceptTranslator.toOpenmrsType(codeableConcept);
		assertThat(result, notNullValue());
		assertThat(result.getConceptMappings(), notNullValue());
		assertThat(result.getConceptMappings(), not(empty()));
		assertThat(result.getConceptMappings(),
		    hasItem(hasProperty("conceptReferenceTerm", hasProperty("code", equalTo("1650")))));
	}
	
	@Test
	public void shouldOnlyAddCodesThatAreMappedSAME_AS() {
		Collection<ConceptMap> conceptMaps = new ArrayList<>();
		ConceptMap conceptMap1 = mock(ConceptMap.class);
		conceptMaps.add(conceptMap1);
		ConceptMap conceptMap2 = mock(ConceptMap.class);
		conceptMaps.add(conceptMap2);
		ConceptMapType conceptMapType1 = mock(ConceptMapType.class);
		conceptMap1.setConceptMapType(conceptMapType1);
		ConceptMapType conceptMapType2 = mock(ConceptMapType.class);
		conceptMap2.setConceptMapType(conceptMapType2);
		
		ConceptReferenceTerm conceptReferenceTerm = mock(ConceptReferenceTerm.class);
		ConceptSource conceptSource1 = mock(ConceptSource.class);
		
		when(conceptMap1.getConceptReferenceTerm()).thenReturn(conceptReferenceTerm);
		when(conceptReferenceTerm.getConceptSource()).thenReturn(conceptSource1);
		when(conceptReferenceTerm.getCode()).thenReturn("1650");
		when(conceptSource1.getName()).thenReturn("CIEL");
		
		when(concept.getConceptMappings()).thenReturn(conceptMaps);
		when(conceptMap1.getConceptMapType()).thenReturn(conceptMapType1);
		when(conceptMapType1.getUuid()).thenReturn(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
		
		when(concept.getConceptMappings()).thenReturn(conceptMaps);
		when(conceptMap2.getConceptMapType()).thenReturn(conceptMapType2);
		when(conceptMapType2.getUuid()).thenReturn(FhirTestConstants.NARROWER_THAN_MAP_TYPE_UUID);
		
		FhirConceptSource ciel = new FhirConceptSource();
		ConceptSource cielConceptSource = new ConceptSource();
		cielConceptSource.setName("CIEL");
		ciel.setConceptSource(cielConceptSource);
		ciel.setUrl(FhirTestConstants.CIEL_SYSTEM_URN);
		when(conceptSourceService.getFhirConceptSourceByConceptSourceName("CIEL")).thenReturn(Optional.of(ciel));
		
		CodeableConcept result = conceptTranslator.toFhirResource(concept);
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), not(empty()));
		assertThat(result.getCoding(), hasItem(hasProperty("system", equalTo(FhirTestConstants.CIEL_SYSTEM_URN))));
		assertThat(result.getCoding(), hasItem(hasProperty("code", equalTo("1650"))));
		assertThat(result.getCoding(), hasItem(hasProperty("display", equalTo(CONCEPT_NAME))));
	}
	
	@Test
	public void shouldNotTranslateUnknownCodeableConceptSourceToConcept() {
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding cielCoding = codeableConcept.addCoding();
		cielCoding.setSystem("Unknown");
		cielCoding.setCode("1650");
		when(conceptSourceService.getFhirConceptSourceByUrl("Unknown")).thenReturn(Optional.empty());
		
		Concept result = conceptTranslator.toOpenmrsType(codeableConcept);
		assertThat(result, nullValue());
	}
	
	@Test
	public void shouldReturnNullWhenCodeableConceptNull() {
		assertThat(conceptTranslator.toOpenmrsType(null), nullValue());
	}
}
