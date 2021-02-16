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

import java.math.BigDecimal;
import java.util.List;

import org.hl7.fhir.r4.model.Observation;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConceptNumeric;

public class ObservationReferenceRangeTranslatorImplTest {
	
	private static final Double LOW_NORMAL_VALUE = 1.0;
	
	private static final Double HIGH_NORMAL_VALUE = 2.0;
	
	private static final Double LOW_ABSOLUTE_VALUE = 3.0;
	
	private static final Double HIGH_ABSOLUTE_VALUE = 4.0;
	
	private static final Double LOW_CRITICAL_VALUE = 5.0;
	
	private static final Double HIGH_CRITICAL_VALUE = 6.0;
	
	private static final String CONCEPT_UUID = "12345-abcdef-12345";
	
	private ObservationReferenceRangeTranslatorImpl observationReferenceRangeTranslator;
	
	@Before
	public void setup() {
		observationReferenceRangeTranslator = new ObservationReferenceRangeTranslatorImpl();
	}
	
	@Test
	public void toFhirType_shouldMapHiAndLowCriticalObservationReferenceRangeToExpected() {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setUuid(CONCEPT_UUID);
		
		conceptNumeric.setLowCritical(LOW_CRITICAL_VALUE);
		conceptNumeric.setHiCritical(HIGH_CRITICAL_VALUE);
		
		List<Observation.ObservationReferenceRangeComponent> result = observationReferenceRangeTranslator
		        .toFhirResource(conceptNumeric);
		
		assertThat(result, notNullValue());
		assertThat(result,
		    hasItem(hasProperty("low", hasProperty("value", equalTo(BigDecimal.valueOf(LOW_CRITICAL_VALUE))))));
		assertThat(result,
		    hasItem(hasProperty("high", hasProperty("value", equalTo(BigDecimal.valueOf(HIGH_CRITICAL_VALUE))))));
		
	}
	
	@Test
	public void toFhirType_shouldMapHiAndLowNormalObservationReferenceRangeToExpected() {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setUuid(CONCEPT_UUID);
		conceptNumeric.setLowNormal(LOW_NORMAL_VALUE);
		conceptNumeric.setHiNormal(HIGH_NORMAL_VALUE);
		
		List<Observation.ObservationReferenceRangeComponent> result = observationReferenceRangeTranslator
		        .toFhirResource(conceptNumeric);
		
		assertThat(result, notNullValue());
		assertThat(result, hasItem(hasProperty("low", hasProperty("value", equalTo(BigDecimal.valueOf(LOW_NORMAL_VALUE))))));
		assertThat(result,
		    hasItem(hasProperty("high", hasProperty("value", equalTo(BigDecimal.valueOf(HIGH_NORMAL_VALUE))))));
	}
	
	@Test
	public void toFhirType_shouldMapHiAndLowAbsoluteObservationReferenceRangeToExpected() {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setUuid(CONCEPT_UUID);
		conceptNumeric.setLowAbsolute(LOW_ABSOLUTE_VALUE);
		conceptNumeric.setHiAbsolute(HIGH_ABSOLUTE_VALUE);
		
		List<Observation.ObservationReferenceRangeComponent> result = observationReferenceRangeTranslator
		        .toFhirResource(conceptNumeric);
		
		assertThat(result, notNullValue());
		assertThat(result,
		    hasItem(hasProperty("low", hasProperty("value", equalTo(BigDecimal.valueOf(LOW_ABSOLUTE_VALUE))))));
		assertThat(result,
		    hasItem(hasProperty("high", hasProperty("value", equalTo(BigDecimal.valueOf(HIGH_ABSOLUTE_VALUE))))));
	}
	
	@Test
	public void toFhirType_shouldMapHiAndLowAbsoluteToExpectedIfOneIsNull() {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setUuid(CONCEPT_UUID);
		conceptNumeric.setLowAbsolute(null);
		conceptNumeric.setHiAbsolute(HIGH_ABSOLUTE_VALUE);
		
		List<Observation.ObservationReferenceRangeComponent> result = observationReferenceRangeTranslator
		        .toFhirResource(conceptNumeric);
		
		assertThat(result, not(empty()));
		assertThat(result.get(0).hasLow(), equalTo(false));
		assertThat(result,
		    hasItem(hasProperty("high", hasProperty("value", equalTo(BigDecimal.valueOf(HIGH_ABSOLUTE_VALUE))))));
	}
	
	@Test
	public void toFhirType_shouldMapHiAndLowNormalToExpectedIfOneIsNull() {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setUuid(CONCEPT_UUID);
		conceptNumeric.setLowNormal(null);
		conceptNumeric.setHiNormal(HIGH_NORMAL_VALUE);
		
		List<Observation.ObservationReferenceRangeComponent> result = observationReferenceRangeTranslator
		        .toFhirResource(conceptNumeric);
		
		assertThat(result, not(empty()));
		assertThat(result.get(0).hasLow(), equalTo(false));
		assertThat(result,
		    hasItem(hasProperty("high", hasProperty("value", equalTo(BigDecimal.valueOf(HIGH_NORMAL_VALUE))))));
	}
	
	@Test
	public void toFhirType_shouldMapHiAndLowCriticalToExpectedIfOneIsNull() {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setUuid(CONCEPT_UUID);
		conceptNumeric.setLowCritical(null);
		conceptNumeric.setHiCritical(HIGH_CRITICAL_VALUE);
		
		List<Observation.ObservationReferenceRangeComponent> result = observationReferenceRangeTranslator
		        .toFhirResource(conceptNumeric);
		
		assertThat(result, not(empty()));
		assertThat(result.get(0).hasLow(), equalTo(false));
		assertThat(result,
		    hasItem(hasProperty("high", hasProperty("value", equalTo(BigDecimal.valueOf(HIGH_CRITICAL_VALUE))))));
	}
}
