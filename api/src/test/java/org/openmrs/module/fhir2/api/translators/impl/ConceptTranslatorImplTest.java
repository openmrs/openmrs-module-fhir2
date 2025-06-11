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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
import org.openmrs.module.fhir2.model.FhirConceptSource;
import org.openmrs.util.LocaleUtility;

@RunWith(MockitoJUnitRunner.class)
public class ConceptTranslatorImplTest {
	
	private static final String CONCEPT_UUID = "12345-abcdef-12345";
	
	private static final String CONCEPT_NAME = "concept-name";
	
	@Mock
	private FhirConceptService conceptService;
	
	@Mock
	private FhirConceptSourceService conceptSourceService;
	
	private ConceptTranslatorImpl conceptTranslator;
	
	private ConceptMapType sameAs;
	
	private ConceptMapType narrowerThan;
	
	private ConceptMapType broaderThan;
	
	private Concept concept;
	
	private CodeableConcept fhirConcept;
	
	private ConceptSource loinc;
	
	private FhirConceptSource fhirLoinc;
	
	private ConceptSource ciel;
	
	private FhirConceptSource fhirCiel;
	
	@Before
	public void setup() {
		LocaleUtility.setLocalesAllowedListCache(Arrays.asList(Locale.ENGLISH));
		conceptTranslator = new ConceptTranslatorImpl();
		conceptTranslator.setConceptService(conceptService);
		conceptTranslator.setConceptSourceService(conceptSourceService);
		
		sameAs = new ConceptMapType();
		sameAs.setName("SAME-AS");
		narrowerThan = new ConceptMapType();
		narrowerThan.setName("NARROWER-THAN");
		broaderThan = new ConceptMapType();
		broaderThan.setName("BROADER-THAN");
		loinc = new ConceptSource();
		loinc.setName("LOINC");
		ciel = new ConceptSource();
		ciel.setName("CIEL");
		
		concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		concept.addName(new ConceptName(CONCEPT_NAME, Locale.ENGLISH));
		when(conceptService.get(CONCEPT_UUID)).thenReturn(concept);
		
		fhirConcept = new CodeableConcept();
		Coding baseCoding = fhirConcept.addCoding();
		baseCoding.setCode(CONCEPT_UUID);
		
		fhirLoinc = new FhirConceptSource();
		fhirLoinc.setConceptSource(loinc);
		fhirLoinc.setUrl(FhirTestConstants.LOINC_SYSTEM_URL);
		when(conceptSourceService.getConceptSourceByUrl(FhirTestConstants.LOINC_SYSTEM_URL)).thenReturn(Optional.of(loinc));
		
		fhirCiel = new FhirConceptSource();
		fhirCiel.setConceptSource(ciel);
		fhirCiel.setUrl(FhirTestConstants.CIEL_SYSTEM_URN);
		when(conceptSourceService.getConceptSourceByUrl(FhirTestConstants.CIEL_SYSTEM_URN)).thenReturn(Optional.of(ciel));
		when(conceptSourceService.getFhirConceptSources()).thenReturn(Arrays.asList(fhirLoinc, fhirCiel));
	}
	
	@Test
	public void shouldTranslateConceptToCodeableConcept() {
		CodeableConcept result = conceptTranslator.toFhirResource(concept);
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), not(empty()));
		assertThat(result.getCoding().get(0).getSystem(), nullValue());
		assertThat(result.getCoding().get(0).getCode(), equalTo(CONCEPT_UUID));
		assertThat(result.getCoding().get(0).getDisplay(), equalTo(CONCEPT_NAME));
	}
	
	@Test
	public void shouldTranslateLOINCMappingForLOINCMappedConcept() {
		addMapping(sameAs, loinc, "1000-1");
		CodeableConcept result = conceptTranslator.toFhirResource(concept);
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), not(empty()));
		assertThat(result.getCoding(), hasItem(allOf(hasProperty("system", equalTo(FhirTestConstants.LOINC_SYSTEM_URL)),
		    hasProperty("code", equalTo("1000-1")), hasProperty("display", nullValue()))));
	}
	
	@Test
	public void shouldTranslateCIELMappingForCIELMappedConcept() {
		addMapping(sameAs, ciel, "1650");
		CodeableConcept result = conceptTranslator.toFhirResource(concept);
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), not(empty()));
		assertThat(result.getCoding(), hasItem(allOf(hasProperty("system", equalTo(FhirTestConstants.CIEL_SYSTEM_URN)),
		    hasProperty("code", equalTo("1650")), hasProperty("display", nullValue()))));
	}
	
	@Test
	public void shouldNotTranslateNullSource() {
		addMapping(sameAs, null, "1650");
		CodeableConcept result = conceptTranslator.toFhirResource(concept);
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), hasSize(1));
		assertThat(result.getCoding(), not(hasItem(hasProperty("system", notNullValue()))));
	}
	
	@Test
	public void shouldReturnNullWhenConceptNull() {
		assertThat(conceptTranslator.toFhirResource(null), nullValue());
	}
	
	@Test
	public void shouldTranslateCodeableConceptToConcept() {
		Concept result = conceptTranslator.toOpenmrsType(fhirConcept);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(CONCEPT_UUID));
	}
	
	@Test
	public void shouldTranslateLOINCCodeableConceptToConceptWithSAMEASMapType() {
		addMapping(sameAs, loinc, "1000-1");
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding loincCoding = codeableConcept.addCoding();
		loincCoding.setSystem(FhirTestConstants.LOINC_SYSTEM_URL);
		loincCoding.setCode("1000-1");
		
		Concept concept2 = new Concept();
		concept2.setUuid("1234-666-999");
		concept2.addName(new ConceptName("Sample Concept", Locale.ENGLISH));
		
		ConceptMap m = new ConceptMap();
		m.setConceptMapType(broaderThan);
		m.setConcept(concept2);
		m.setConceptReferenceTerm(new ConceptReferenceTerm(loinc, "1000-1", "1000-1"));
		concept.addConceptMapping(m);
		
		List<Concept> matchingConcepts = new ArrayList<>();
		matchingConcepts.add(concept);
		matchingConcepts.add(concept2);
		
		when(conceptService.getConceptsWithAnyMappingInSource(loinc, "1000-1")).thenReturn(matchingConcepts);
		
		Concept result = conceptTranslator.toOpenmrsType(codeableConcept);
		assertThat(result, notNullValue());
		assertThat(result.getConceptMappings(), notNullValue());
		assertThat(result.getConceptMappings(), not(empty()));
		assertThat(result.getConceptMappings(),
		    hasItem(hasProperty("conceptReferenceTerm", hasProperty("code", equalTo("1000-1")))));
	}
	
	@Test
	public void shouldNotTranslateCodeableConceptToAnyConceptIfMultipleConceptsExistWithNoSAMEASMapType() {
		addMapping(narrowerThan, loinc, "1000-1");
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding loincCoding = codeableConcept.addCoding();
		loincCoding.setSystem(FhirTestConstants.LOINC_SYSTEM_URL);
		loincCoding.setCode("1000-1");
		
		Concept concept2 = new Concept();
		concept2.setUuid("1234-666-999");
		concept2.addName(new ConceptName("Sample Concept", Locale.ENGLISH));
		
		ConceptMap m = new ConceptMap();
		m.setConceptMapType(broaderThan);
		m.setConcept(concept2);
		m.setConceptReferenceTerm(new ConceptReferenceTerm(loinc, "1000-1", "1000-1"));
		concept.addConceptMapping(m);
		
		List<Concept> matchingConcepts = new ArrayList<>();
		matchingConcepts.add(concept);
		matchingConcepts.add(concept2);
		
		when(conceptService.getConceptsWithAnyMappingInSource(loinc, "1000-1")).thenReturn(matchingConcepts);
		
		Concept result = conceptTranslator.toOpenmrsType(codeableConcept);
		assertThat(result, nullValue());
	}
	
	@Test
	public void shouldNotTranslateCodeableConceptToAnyConceptIfNoConceptMatches() {
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding loincCoding = codeableConcept.addCoding();
		loincCoding.setSystem(FhirTestConstants.LOINC_SYSTEM_URL);
		loincCoding.setCode("1000-1");
		
		List<Concept> matchingConcepts = new ArrayList<>();
		when(conceptService.getConceptsWithAnyMappingInSource(loinc, "1000-1")).thenReturn(matchingConcepts);
		Concept result = conceptTranslator.toOpenmrsType(codeableConcept);
		assertThat(result, nullValue());
	}
	
	@Test
	public void shouldFavorConceptWithMatchingSystemOverUUID() {
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding baseCoding = codeableConcept.addCoding();
		baseCoding.setCode(CONCEPT_UUID);
		Coding loincCoding = codeableConcept.addCoding();
		loincCoding.setSystem(FhirTestConstants.LOINC_SYSTEM_URL);
		loincCoding.setCode("1000-1");
		
		Concept result = conceptTranslator.toOpenmrsType(codeableConcept);
		assertThat(result.getUuid(), equalTo(concept.getUuid()));
	}
	
	@Test
	public void shouldTranslateCIELCodeableConceptToConcept() {
		addMapping(sameAs, ciel, "1650");
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding cielCoding = codeableConcept.addCoding();
		cielCoding.setSystem(FhirTestConstants.CIEL_SYSTEM_URN);
		cielCoding.setCode("1650");
		
		List<Concept> matchingConcepts = new ArrayList<>();
		matchingConcepts.add(concept);
		when(conceptService.getConceptsWithAnyMappingInSource(ciel, "1650")).thenReturn(matchingConcepts);
		
		Concept result = conceptTranslator.toOpenmrsType(codeableConcept);
		assertThat(result, notNullValue());
		assertThat(result.getConceptMappings(), notNullValue());
		assertThat(result.getConceptMappings(), not(empty()));
		assertThat(result.getConceptMappings(),
		    hasItem(hasProperty("conceptReferenceTerm", hasProperty("code", equalTo("1650")))));
	}
	
	@Test
	public void shouldTranslateCodeableConceptToConceptWithNoSAMEASMappingIfOnlyOneConceptExists() {
		addMapping(narrowerThan, ciel, "1650");
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding cielCoding = codeableConcept.addCoding();
		cielCoding.setSystem(FhirTestConstants.CIEL_SYSTEM_URN);
		cielCoding.setCode("1650");
		
		List<Concept> matchingConcepts = new ArrayList<>();
		matchingConcepts.add(concept);
		when(conceptService.getConceptsWithAnyMappingInSource(ciel, "1650")).thenReturn(matchingConcepts);
		
		Concept result = conceptTranslator.toOpenmrsType(codeableConcept);
		assertThat(result, notNullValue());
		assertThat(result.getConceptMappings(), notNullValue());
		assertThat(result.getConceptMappings(), not(empty()));
		assertThat(result.getConceptMappings(),
		    hasItem(hasProperty("conceptReferenceTerm", hasProperty("code", equalTo("1650")))));
	}
	
	@Test
	public void shouldNotTranslateUnknownCodeableConceptSourceToConcept() {
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding cielCoding = codeableConcept.addCoding();
		cielCoding.setSystem("Unknown");
		cielCoding.setCode("1650");
		Concept result = conceptTranslator.toOpenmrsType(codeableConcept);
		assertThat(result, nullValue());
	}
	
	@Test
	public void shouldAddCodesThatAreMappedSAME_ASIfTheyExist() {
		addMapping(sameAs, ciel, "1650");
		addMapping(narrowerThan, ciel, "1690");
		
		CodeableConcept result = conceptTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), not(empty()));
		// one SAME-AS, one UUID
		assertThat(result.getCoding(), hasSize(2));
		assertThat(result.getCoding(), hasItem(hasProperty("system", equalTo(FhirTestConstants.CIEL_SYSTEM_URN))));
		assertThat(result.getCoding(), hasItem(hasProperty("code", equalTo("1650"))));
		assertThat(result.getCoding(), hasItem(hasProperty("display", equalTo(CONCEPT_NAME))));
	}
	
	@Test
	public void shouldAddCodesThatAreMappedSAME_ASIfOneMappingExists() {
		addMapping(sameAs, ciel, "1650");
		
		CodeableConcept result = conceptTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), not(empty()));
		// one SAME-AS, one UUID
		assertThat(result.getCoding(), hasSize(2));
		assertThat(result.getCoding(), hasItem(hasProperty("system", equalTo(FhirTestConstants.CIEL_SYSTEM_URN))));
		assertThat(result.getCoding(), hasItem(hasProperty("code", equalTo("1650"))));
		assertThat(result.getCoding(), hasItem(hasProperty("display", equalTo(CONCEPT_NAME))));
	}
	
	@Test
	public void shouldAddCodesThatAreNotMappedSAME_ASIfOnlyOneMappingExistsPerSource() {
		addMapping(narrowerThan, ciel, "1650");
		
		CodeableConcept result = conceptTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), not(empty()));
		// one NARROWER-THAN, one UUID
		assertThat(result.getCoding(), hasSize(2));
		assertThat(result.getCoding(), hasItem(hasProperty("system", equalTo(FhirTestConstants.CIEL_SYSTEM_URN))));
		assertThat(result.getCoding(), hasItem(hasProperty("code", equalTo("1650"))));
		assertThat(result.getCoding(), hasItem(hasProperty("display", equalTo(CONCEPT_NAME))));
	}
	
	@Test
	public void shouldNotAddCodesThatAreNotMappedSAME_ASIfManyMappingExistsForTheSameSource() {
		addMapping(narrowerThan, ciel, "1650");
		addMapping(broaderThan, ciel, "1690");
		
		CodeableConcept result = conceptTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), not(empty()));
		assertThat(result.getCoding(), hasSize(1));
	}
	
	@Test
	public void shouldAddCodesThatAreNotMappedSAME_ASIfOneMappingPerSourceExistsForMultipleSources() {
		addMapping(narrowerThan, ciel, "1650");
		addMapping(broaderThan, loinc, "1000-1");
		
		CodeableConcept result = conceptTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), not(empty()));
		assertThat(result.getCoding(), hasSize(3));
		assertThat(result.getCoding(), hasItem(hasProperty("system", equalTo(FhirTestConstants.CIEL_SYSTEM_URN))));
		assertThat(result.getCoding(), hasItem(hasProperty("code", equalTo("1650"))));
		assertThat(result.getCoding(), hasItem(hasProperty("display", equalTo(CONCEPT_NAME))));
		assertThat(result.getCoding(), hasItem(hasProperty("system", equalTo(FhirTestConstants.LOINC_SYSTEM_URL))));
		assertThat(result.getCoding(), hasItem(hasProperty("code", equalTo("1000-1"))));
		assertThat(result.getCoding(), hasItem(hasProperty("display", equalTo(CONCEPT_NAME))));
	}
	
	@Test
	public void shouldNotAddCodesThatAreNotMappedSAME_ASIfManyMappingExistsPerSourceForMultipleSources() {
		addMapping(narrowerThan, ciel, "1650");
		addMapping(broaderThan, ciel, "1659");
		addMapping(broaderThan, loinc, "1000-1");
		addMapping(narrowerThan, loinc, "1000-2");
		
		CodeableConcept result = conceptTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), not(empty()));
		assertThat(result.getCoding(), hasSize(1));
	}
	
	@Test
	public void shouldAddCodesThatAreMappedSAME_ASIfTheyExistForMultipleSources() {
		addMapping(sameAs, ciel, "1650");
		addMapping(narrowerThan, ciel, "1680");
		addMapping(sameAs, loinc, "1000-1");
		addMapping(broaderThan, loinc, "1000-2");
		
		CodeableConcept result = conceptTranslator.toFhirResource(concept);
		
		assertThat(result, notNullValue());
		assertThat(result.getCoding(), not(empty()));
		assertThat(result.getCoding(), hasSize(3));
		assertThat(result.getCoding(), hasItem(hasProperty("system", equalTo(FhirTestConstants.CIEL_SYSTEM_URN))));
		assertThat(result.getCoding(), hasItem(hasProperty("code", equalTo("1650"))));
		assertThat(result.getCoding(), hasItem(hasProperty("display", equalTo(CONCEPT_NAME))));
		assertThat(result.getCoding(), hasItem(hasProperty("system", equalTo(FhirTestConstants.LOINC_SYSTEM_URL))));
		assertThat(result.getCoding(), hasItem(hasProperty("code", equalTo("1000-1"))));
		assertThat(result.getCoding(), hasItem(hasProperty("display", equalTo(CONCEPT_NAME))));
	}
	
	@Test
	public void shouldReturnNullWhenCodeableConceptNull() {
		assertThat(conceptTranslator.toOpenmrsType(null), nullValue());
	}
	
	private void addMapping(ConceptMapType mapType, ConceptSource conceptSource, String code) {
		ConceptMap m = new ConceptMap();
		m.setConceptMapType(mapType);
		m.setConcept(concept);
		m.setConceptReferenceTerm(new ConceptReferenceTerm(conceptSource, code, code));
		concept.addConceptMapping(m);
	}
}
