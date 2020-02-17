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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationInterpretationTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationValueTranslator;

@RunWith(MockitoJUnitRunner.class)
public class ObservationComponentTranslatorImpl_2_1Test {
	
	@Mock
	private ObservationValueTranslator observationValueTranslator;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private ObservationInterpretationTranslator interpretationTranslator;
	
	private ObservationComponentTranslatorImpl observationComponentTranslator;
	
	@Before
	public void setup() {
		observationComponentTranslator = new ObservationComponentTranslatorImpl();
		observationComponentTranslator.setObservationValueTranslator(observationValueTranslator);
		observationComponentTranslator.setConceptTranslator(conceptTranslator);
		observationComponentTranslator.setInterpretationTranslator(interpretationTranslator);
	}
	
	@Test
	public void toFhirResource_shouldFhirInterpretationCorrectly() {
		Obs obs = new Obs();
		obs.setInterpretation(Obs.Interpretation.NORMAL);
		
		CodeableConcept interpretation = new CodeableConcept(
		        new Coding(FhirConstants.INTERPRETATION_VALUE_SET_URI, "N", "Normal"));
		when(interpretationTranslator.toFhirResource(obs)).thenReturn(interpretation);
		Observation.ObservationComponentComponent component = observationComponentTranslator.toFhirResource(obs);
		
		assertThat(component.getInterpretation().size(), greaterThanOrEqualTo(1));
		assertThat(component.getInterpretation().get(0).getCoding().get(0).getCode(), equalTo("N"));
		assertThat(component.getInterpretation().get(0).getCoding().get(0).getSystem(),
		    equalTo(FhirConstants.INTERPRETATION_VALUE_SET_URI));
		assertThat(component.getInterpretation().get(0).getCoding().get(0).getDisplay(), equalTo("Normal"));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnObsAsItIsIfFhirInterpretationIsNull() {
		Obs obs = new Obs();
		
		Obs result = observationComponentTranslator.toOpenmrsType(obs, null);
		assertThat(result, equalTo(obs));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateToObsInterpretation() {
		Obs obs = new Obs();
		obs.setInterpretation(Obs.Interpretation.NORMAL);
		Observation.ObservationComponentComponent observationComponent = new Observation.ObservationComponentComponent();
		
		CodeableConcept interpretation = new CodeableConcept(
		        new Coding(FhirConstants.INTERPRETATION_VALUE_SET_URI, "N", "Normal"));
		
		observationComponent.addInterpretation(interpretation);
		
		when(interpretationTranslator.toOpenmrsType(obs, interpretation)).thenReturn(obs);
		
		Obs result = observationComponentTranslator.toOpenmrsType(obs, observationComponent);
		
		assertThat(result.getInterpretation(), notNullValue());
		assertThat(result.getInterpretation(), equalTo(Obs.Interpretation.NORMAL));
		
	}
	
}
