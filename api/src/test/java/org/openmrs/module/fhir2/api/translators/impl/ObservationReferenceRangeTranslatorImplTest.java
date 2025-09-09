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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.math.BigDecimal;
import java.util.List;

import org.hl7.fhir.r4.model.Observation;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;

public class ObservationReferenceRangeTranslatorImplTest {
	
	private static final BigDecimal LOW_NORMAL_VALUE = BigDecimal.valueOf(1L);
	
	private static final BigDecimal HIGH_NORMAL_VALUE = BigDecimal.valueOf(2L);
	
	private static final BigDecimal LOW_ABSOLUTE_VALUE = BigDecimal.valueOf(3L);
	
	private static final BigDecimal HIGH_ABSOLUTE_VALUE = BigDecimal.valueOf(4L);
	
	private static final BigDecimal LOW_CRITICAL_VALUE = BigDecimal.valueOf(5L);
	
	private static final BigDecimal HIGH_CRITICAL_VALUE = BigDecimal.valueOf(6L);
	
	private static final String CONCEPT_UUID = "12345-abcdef-12345";
	
	private ObservationReferenceRangeTranslatorImpl observationReferenceRangeTranslator;
	
	@Before
	public void setup() {
		observationReferenceRangeTranslator = new ObservationReferenceRangeTranslatorImpl();
	}
	
	@Test
	public void toFhirType_shouldMapHighAndLowNormalObservationReferenceRangeToExpected() {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setUuid(CONCEPT_UUID);
		conceptNumeric.setLowNormal(LOW_NORMAL_VALUE.doubleValue());
		conceptNumeric.setHiNormal(HIGH_NORMAL_VALUE.doubleValue());
		
		List<Observation.ObservationReferenceRangeComponent> result = observationReferenceRangeTranslator
		        .toFhirResource(getObs(conceptNumeric));
		
		assertThat(result, not(empty()));
		
		Observation.ObservationReferenceRangeComponent component = result.get(0);
		
		assertThat(component.getType().hasCoding(), is(true));
		assertThat(component.getType().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.OBSERVATION_REFERENCE_RANGE_SYSTEM_URI));
		assertThat(component.getType().getCodingFirstRep().getCode(), equalTo(FhirConstants.OBSERVATION_REFERENCE_NORMAL));
		assertThat(component.hasLow(), is(true));
		assertThat(component.getLow().getValue(), equalTo(LOW_NORMAL_VALUE));
		assertThat(component.hasHigh(), is(true));
		assertThat(component.getHigh().getValue(), equalTo(HIGH_NORMAL_VALUE));
	}
	
	@Test
	public void toFhirType_shouldMapHighAndLowCriticalObservationReferenceRangeToExpected() {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setUuid(CONCEPT_UUID);
		
		conceptNumeric.setLowCritical(LOW_CRITICAL_VALUE.doubleValue());
		conceptNumeric.setHiCritical(HIGH_CRITICAL_VALUE.doubleValue());
		
		List<Observation.ObservationReferenceRangeComponent> result = observationReferenceRangeTranslator
		        .toFhirResource(getObs(conceptNumeric));
		
		assertThat(result, not(empty()));
		
		Observation.ObservationReferenceRangeComponent component = result.get(0);
		
		assertThat(component.getType().hasCoding(), is(true));
		assertThat(component.getType().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.OBSERVATION_REFERENCE_RANGE_SYSTEM_URI));
		assertThat(component.getType().getCodingFirstRep().getCode(),
		    equalTo(FhirConstants.OBSERVATION_REFERENCE_TREATMENT));
		assertThat(component.hasLow(), is(true));
		assertThat(component.getLow().getValue(), equalTo(LOW_CRITICAL_VALUE));
		assertThat(component.hasHigh(), is(true));
		assertThat(component.getHigh().getValue(), equalTo(HIGH_CRITICAL_VALUE));
	}
	
	@Test
	public void toFhirType_shouldMapHighAndLowAbsoluteObservationReferenceRangeToExpected() {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setUuid(CONCEPT_UUID);
		conceptNumeric.setLowAbsolute(LOW_ABSOLUTE_VALUE.doubleValue());
		conceptNumeric.setHiAbsolute(HIGH_ABSOLUTE_VALUE.doubleValue());
		
		List<Observation.ObservationReferenceRangeComponent> result = observationReferenceRangeTranslator
		        .toFhirResource(getObs(conceptNumeric));
		
		assertThat(result, not(empty()));
		
		Observation.ObservationReferenceRangeComponent component = result.get(0);
		
		assertThat(component.getType().hasCoding(), is(true));
		assertThat(component.getType().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.OPENMRS_FHIR_EXT_OBSERVATION_REFERENCE_RANGE));
		assertThat(component.getType().getCodingFirstRep().getCode(), equalTo(FhirConstants.OBSERVATION_REFERENCE_ABSOLUTE));
		assertThat(component.hasLow(), is(true));
		assertThat(component.getLow().getValue(), equalTo(LOW_ABSOLUTE_VALUE));
		assertThat(component.hasHigh(), is(true));
		assertThat(component.getHigh().getValue(), equalTo(HIGH_ABSOLUTE_VALUE));
	}
	
	@Test
	public void toFhirType_shouldMapHighAndLowAbsoluteToExpectedIfLowIsNull() {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setUuid(CONCEPT_UUID);
		conceptNumeric.setLowAbsolute(null);
		conceptNumeric.setHiAbsolute(HIGH_ABSOLUTE_VALUE.doubleValue());
		
		List<Observation.ObservationReferenceRangeComponent> result = observationReferenceRangeTranslator
		        .toFhirResource(getObs(conceptNumeric));
		
		assertThat(result, not(empty()));
		assertThat(result.get(0).hasLow(), equalTo(false));
		assertThat(result, hasItem(hasProperty("high", hasProperty("value", equalTo(HIGH_ABSOLUTE_VALUE)))));
	}
	
	@Test
	public void toFhirType_shouldMapHighAndLowAbsoluteToExpectedIfHighIsNull() {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setUuid(CONCEPT_UUID);
		conceptNumeric.setLowAbsolute(LOW_ABSOLUTE_VALUE.doubleValue());
		conceptNumeric.setHiAbsolute(null);
		
		List<Observation.ObservationReferenceRangeComponent> result = observationReferenceRangeTranslator
		        .toFhirResource(getObs(conceptNumeric));
		
		assertThat(result, not(empty()));
		assertThat(result.get(0).hasHigh(), equalTo(false));
		assertThat(result, hasItem(hasProperty("low", hasProperty("value", equalTo(LOW_ABSOLUTE_VALUE)))));
	}
	
	@Test
	public void toFhirType_shouldMapHighAndLowNormalToExpectedIfLowIsNull() {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setUuid(CONCEPT_UUID);
		conceptNumeric.setLowNormal(null);
		conceptNumeric.setHiNormal(HIGH_NORMAL_VALUE.doubleValue());
		
		List<Observation.ObservationReferenceRangeComponent> result = observationReferenceRangeTranslator
		        .toFhirResource(getObs(conceptNumeric));
		
		assertThat(result, not(empty()));
		assertThat(result.get(0).hasLow(), equalTo(false));
		assertThat(result, hasItem(hasProperty("high", hasProperty("value", equalTo(HIGH_NORMAL_VALUE)))));
	}
	
	@Test
	public void toFhirType_shouldMapHighAndLowNormalToExpectedIfHighIsNull() {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setUuid(CONCEPT_UUID);
		conceptNumeric.setLowNormal(LOW_NORMAL_VALUE.doubleValue());
		conceptNumeric.setHiNormal(null);
		
		List<Observation.ObservationReferenceRangeComponent> result = observationReferenceRangeTranslator
		        .toFhirResource(getObs(conceptNumeric));
		
		assertThat(result, not(empty()));
		assertThat(result.get(0).hasHigh(), equalTo(false));
		assertThat(result, hasItem(hasProperty("low", hasProperty("value", equalTo(LOW_NORMAL_VALUE)))));
	}
	
	@Test
	public void toFhirType_shouldMapHighAndLowCriticalToExpectedIfLowIsNull() {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setUuid(CONCEPT_UUID);
		conceptNumeric.setLowCritical(null);
		conceptNumeric.setHiCritical(HIGH_CRITICAL_VALUE.doubleValue());
		
		List<Observation.ObservationReferenceRangeComponent> result = observationReferenceRangeTranslator
		        .toFhirResource(getObs(conceptNumeric));
		
		assertThat(result, not(empty()));
		assertThat(result.get(0).hasLow(), equalTo(false));
		assertThat(result, hasItem(hasProperty("high", hasProperty("value", equalTo(HIGH_CRITICAL_VALUE)))));
	}
	
	@Test
	public void toFhirType_shouldMapHighAndLowCriticalToExpectedIHighIsNull() {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setUuid(CONCEPT_UUID);
		conceptNumeric.setLowCritical(LOW_CRITICAL_VALUE.doubleValue());
		conceptNumeric.setHiCritical(null);
		
		List<Observation.ObservationReferenceRangeComponent> result = observationReferenceRangeTranslator
		        .toFhirResource(getObs(conceptNumeric));
		
		assertThat(result, not(empty()));
		assertThat(result.get(0).hasHigh(), equalTo(false));
		assertThat(result, hasItem(hasProperty("low", hasProperty("value", equalTo(LOW_CRITICAL_VALUE)))));
	}
	
	private Obs getObs(ConceptNumeric concept) {
		Obs obs = new Obs();
		obs.setConcept(concept);
		return obs;
	}
}
