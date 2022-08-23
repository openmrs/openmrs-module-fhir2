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
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;

@RunWith(MockitoJUnitRunner.class)
public class LocationTypeTranslatorImpl25Test {
	
	private static final String TYPE_CONCEPT_UUID = "91df3897-1066-46a1-a403-714b737af00b";
	
	private static final String FHIR_TYPE_CONCEPT_UUID = "693a6e1f-7026-4406-adf9-c7ea3b1e8d6e";
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	private Location omrsLocation;
	
	private LocationTypeTranslatorImpl_2_5 locationTypeTranslator;
	
	@Before
	public void setup() {
		omrsLocation = new Location();
		locationTypeTranslator = new LocationTypeTranslatorImpl_2_5();
		locationTypeTranslator.setConceptTranslator(conceptTranslator);
	}
	
	@Test
	public void toFhirResource_shouldTranslateLocationConceptAttributeToFhir() {
		Concept typeConcept = new Concept();
		CodeableConcept fhirTypeConcept = new CodeableConcept();
		
		fhirTypeConcept.setId(FHIR_TYPE_CONCEPT_UUID);
		typeConcept.setUuid(TYPE_CONCEPT_UUID);
		
		omrsLocation.setType(typeConcept);
		
		when(conceptTranslator.toFhirResource(eq(typeConcept))).thenReturn(fhirTypeConcept);
		
		List<CodeableConcept> result = locationTypeTranslator.toFhirResource(omrsLocation);
		
		assertThat(result, notNullValue());
		assertThat(result, hasSize(greaterThan(0)));
		assertThat(result.get(0).getId(), equalTo(FHIR_TYPE_CONCEPT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirTypeCodeableConceptToLocationAttribute() {
		Concept typeConcept = new Concept();
		Coding typeCoding = new Coding();
		CodeableConcept fhirTypeConcept = new CodeableConcept();
		
		typeCoding.setId(FHIR_TYPE_CONCEPT_UUID);
		fhirTypeConcept.setId(FHIR_TYPE_CONCEPT_UUID);
		fhirTypeConcept.setCoding(Collections.singletonList(typeCoding));
		typeConcept.setUuid(TYPE_CONCEPT_UUID);
		
		when(conceptTranslator.toOpenmrsType(eq(fhirTypeConcept))).thenReturn(typeConcept);
		
		Location result = locationTypeTranslator.toOpenmrsType(omrsLocation, Collections.singletonList(fhirTypeConcept));
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), notNullValue());
		assertThat(result.getType(), equalTo(typeConcept));
	}
}
