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
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;

@RunWith(MockitoJUnitRunner.class)
public class AllergyIntoleranceSeverityTranslatorImplTest {
	
	private static final String GLOBAL_PROPERTY_MILD_VALUE = "102553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String GLOBAL_PROPERTY_SEVERE_VALUE = "202553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String GLOBAL_PROPERTY_MODERATE_VALUE = "302553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String GLOBAL_PROPERTY_OTHER_VALUE = "402553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Mock
	private FhirConceptService conceptService;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private AllergyIntoleranceSeverityTranslatorImpl severityTranslator;
	
	private final Map<String, String> severityConceptUUIDs = new HashMap<>();
	
	private Concept concept;
	
	@Before
	public void setup() {
		severityTranslator = new AllergyIntoleranceSeverityTranslatorImpl();
		severityTranslator.setConceptService(conceptService);
		severityTranslator.setGlobalPropertyService(globalPropertyService);
		concept = new Concept();
	}
	
	@Before
	public void setupMocks() {
		severityConceptUUIDs.put(FhirConstants.GLOBAL_PROPERTY_MILD, GLOBAL_PROPERTY_MILD_VALUE);
		severityConceptUUIDs.put(FhirConstants.GLOBAL_PROPERTY_MODERATE, GLOBAL_PROPERTY_MODERATE_VALUE);
		severityConceptUUIDs.put(FhirConstants.GLOBAL_PROPERTY_SEVERE, GLOBAL_PROPERTY_SEVERE_VALUE);
		severityConceptUUIDs.put(FhirConstants.GLOBAL_PROPERTY_OTHER, GLOBAL_PROPERTY_OTHER_VALUE);
		
		when(globalPropertyService.getGlobalProperties(FhirConstants.GLOBAL_PROPERTY_MILD,
		    FhirConstants.GLOBAL_PROPERTY_MODERATE, FhirConstants.GLOBAL_PROPERTY_SEVERE,
		    FhirConstants.GLOBAL_PROPERTY_OTHER)).thenReturn(severityConceptUUIDs);
	}
	
	@Test
	public void shouldTranslateAllergyReactionToSEVEREAllergyIntoleranceSeverity() {
		Concept severeConcept = new Concept();
		severeConcept.setUuid(GLOBAL_PROPERTY_SEVERE_VALUE);
		AllergyIntolerance.AllergyIntoleranceSeverity severity = severityTranslator.toFhirResource(severeConcept);
		assertThat(severity, notNullValue());
		assertThat(severity, equalTo(AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE));
	}
	
	@Test
	public void shouldTranslateAllergyReactionToMODERATEAllergyIntoleranceSeverity() {
		Concept moderateConcept = new Concept();
		moderateConcept.setUuid(GLOBAL_PROPERTY_MODERATE_VALUE);
		AllergyIntolerance.AllergyIntoleranceSeverity severity = severityTranslator.toFhirResource(moderateConcept);
		assertThat(severity, notNullValue());
		assertThat(severity, equalTo(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE));
	}
	
	@Test
	public void shouldTranslateAllergyReactionToMILDAllergyIntoleranceSeverity() {
		Concept mildConcept = new Concept();
		mildConcept.setUuid(GLOBAL_PROPERTY_MILD_VALUE);
		AllergyIntolerance.AllergyIntoleranceSeverity severity = severityTranslator.toFhirResource(mildConcept);
		assertThat(severity, notNullValue());
		assertThat(severity, equalTo(AllergyIntolerance.AllergyIntoleranceSeverity.MILD));
	}
	
	@Test
	public void shouldTranslateAllergyReactionToNULLAllergyIntoleranceSeverity() {
		Concept otherConcept = new Concept();
		otherConcept.setUuid(GLOBAL_PROPERTY_OTHER_VALUE);
		AllergyIntolerance.AllergyIntoleranceSeverity severity = severityTranslator.toFhirResource(otherConcept);
		assertThat(severity, notNullValue());
		assertThat(severity, equalTo(AllergyIntolerance.AllergyIntoleranceSeverity.NULL));
	}
	
	@Test
	public void shouldTranslateSEVEREToOpenMrsTypeCorrectly() {
		concept.setUuid(GLOBAL_PROPERTY_SEVERE_VALUE);
		when(conceptService.get(GLOBAL_PROPERTY_SEVERE_VALUE)).thenReturn(concept);
		Concept concept = severityTranslator.toOpenmrsType(AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE);
		assertThat(concept, notNullValue());
		assertThat(concept.getUuid(), equalTo(GLOBAL_PROPERTY_SEVERE_VALUE));
	}
	
	@Test
	public void shouldTranslateMODERATEToOpenMrsTypeCorrectly() {
		concept.setUuid(GLOBAL_PROPERTY_MODERATE_VALUE);
		when(conceptService.get(GLOBAL_PROPERTY_MODERATE_VALUE)).thenReturn(concept);
		Concept concept = severityTranslator.toOpenmrsType(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE);
		assertThat(concept, notNullValue());
		assertThat(concept.getUuid(), equalTo(GLOBAL_PROPERTY_MODERATE_VALUE));
	}
	
	@Test
	public void shouldTranslateMILDToOpenMrsTypeCorrectly() {
		concept.setUuid(GLOBAL_PROPERTY_MILD_VALUE);
		when(conceptService.get(GLOBAL_PROPERTY_MILD_VALUE)).thenReturn(concept);
		Concept concept = severityTranslator.toOpenmrsType(AllergyIntolerance.AllergyIntoleranceSeverity.MILD);
		assertThat(concept, notNullValue());
		assertThat(concept.getUuid(), equalTo(GLOBAL_PROPERTY_MILD_VALUE));
	}
	
	@Test
	public void shouldTranslateNULLToOpenMrsTypeCorrectly() {
		concept.setUuid(GLOBAL_PROPERTY_OTHER_VALUE);
		when(conceptService.get(GLOBAL_PROPERTY_OTHER_VALUE)).thenReturn(concept);
		Concept concept = severityTranslator.toOpenmrsType(AllergyIntolerance.AllergyIntoleranceSeverity.NULL);
		assertThat(concept, notNullValue());
		assertThat(concept.getUuid(), equalTo(GLOBAL_PROPERTY_OTHER_VALUE));
	}
}
