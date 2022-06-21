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
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.api.translators.impl.MedicationDispenseStatusTranslatorImpl.CONCEPT_SOURCE_URI;

import java.util.Optional;

import org.hl7.fhir.r4.model.MedicationDispense;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.ConceptSource;
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;

@RunWith(MockitoJUnitRunner.class)
public class MedicationDispenseStatusTranslatorImplTest {
	
	@Mock
	FhirConceptSourceService conceptSourceService;
	
	@Mock
	FhirConceptService conceptService;
	
	private MedicationDispenseStatusTranslatorImpl dispenseStatusTranslator;
	
	@Mock
	ConceptSource dispenseStatusSource;
	
	@Before
	public void setup() {
		dispenseStatusTranslator = new MedicationDispenseStatusTranslatorImpl();
		dispenseStatusTranslator.setConceptSourceService(conceptSourceService);
		dispenseStatusTranslator.setConceptService(conceptService);
		when(conceptSourceService.getConceptSourceByUrl(CONCEPT_SOURCE_URI)).thenReturn(Optional.of(dispenseStatusSource));
	}
	
	@Test
	public void toFhirResource_shouldTranslateConceptToDispenseStatus() {
		for (MedicationDispense.MedicationDispenseStatus expected : MedicationDispense.MedicationDispenseStatus.values()) {
			if (expected != MedicationDispense.MedicationDispenseStatus.NULL) {
				Concept concept = new Concept();
				when(conceptService.getSameAsMappingForConceptInSource(dispenseStatusSource, concept))
				        .thenReturn(Optional.of(expected.toCode()));
				
				MedicationDispense.MedicationDispenseStatus actual = dispenseStatusTranslator.toFhirResource(concept);
				
				assertThat(actual, notNullValue());
				assertThat(actual, equalTo(expected));
			}
		}
	}
	
	@Test
	public void toFhirResource_shouldNotTranslateConceptWithoutCorrectSource() {
		Concept concept = new Concept();
		MedicationDispense.MedicationDispenseStatus actual = dispenseStatusTranslator.toFhirResource(concept);
		assertThat(actual, nullValue());
	}
	
	@Test
	public void toFhirResource_shouldNotTranslateConceptWithoutCorrectMapping() {
		Concept concept = new Concept();
		when(conceptService.getSameAsMappingForConceptInSource(dispenseStatusSource, concept))
		        .thenReturn(Optional.of("prep"));
		MedicationDispense.MedicationDispenseStatus actual = dispenseStatusTranslator.toFhirResource(concept);
		assertThat(actual, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldNotTranslateToConceptWithCorrectMapping() {
		for (MedicationDispense.MedicationDispenseStatus status : MedicationDispense.MedicationDispenseStatus.values()) {
			if (status != MedicationDispense.MedicationDispenseStatus.NULL) {
				Concept expected = new Concept();
				when(conceptService.getConceptWithSameAsMappingInSource(dispenseStatusSource, status.toCode()))
				        .thenReturn(Optional.of(expected));
				
				Concept actual = dispenseStatusTranslator.toOpenmrsType(status);
				
				assertThat(actual, notNullValue());
				assertThat(actual, equalTo(expected));
			}
		}
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfNoConceptWithCorrectMappingIsFound() {
		Concept concept = dispenseStatusTranslator.toOpenmrsType(MedicationDispense.MedicationDispenseStatus.COMPLETED);
		assertThat(concept, nullValue());
	}
}
