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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationInterpretationTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationValueTranslator;

@RunWith(MockitoJUnitRunner.class)
public class ObservationComponentTranslatorImplTest {
	
	private static final String OBS_UUID = "12345-abcde-54321";
	
	private static final String OBS_CONCEPT_UUID = "54321-edcba-12345";
	
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
	public void toFhirResource_shouldConvertObsToObservationComponent() {
		Obs obs = new Obs();
		
		Observation.ObservationComponentComponent result = observationComponentTranslator.toFhirResource(obs);
		
		assertThat(result, notNullValue());
	}
	
	@Test
	public void toFhirResource_shouldSetObservationComponentIdToUuid() {
		Obs obs = new Obs();
		obs.setUuid(OBS_UUID);
		
		Observation.ObservationComponentComponent result = observationComponentTranslator.toFhirResource(obs);
		
		assertThat(result.getId(), equalTo(OBS_UUID));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfObsNull() {
		Observation.ObservationComponentComponent result = observationComponentTranslator.toFhirResource(null);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toFhirResource_shouldConvertObsConceptToCodeableConcept() {
		Obs obs = new Obs();
		Concept obsConcept = new Concept();
		obsConcept.setUuid(OBS_CONCEPT_UUID);
		obs.setConcept(obsConcept);
		
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.setId(OBS_CONCEPT_UUID);
		when(conceptTranslator.toFhirResource(obsConcept)).thenReturn(codeableConcept);
		
		Observation.ObservationComponentComponent result = observationComponentTranslator.toFhirResource(obs);
		
		assertThat(result.getCode(), notNullValue());
		assertThat(result.getCode().getId(), equalTo(OBS_CONCEPT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldConvertObsValueToType() {
		Obs obs = new Obs();
		obs.setValueNumeric(130d);
		when(observationValueTranslator.toFhirResource(obs)).thenReturn(new Quantity(130d));
		
		Observation.ObservationComponentComponent result = observationComponentTranslator.toFhirResource(obs);
		
		assertThat(result.getValue(), notNullValue());
		assertThat(result.getValue(), instanceOf(Quantity.class));
		assertThat(((Quantity) result.getValue()).getValue().doubleValue(), equalTo(130d));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullValueForInterpretation() {
		Obs observation = new Obs();
		when(interpretationTranslator.toFhirResource(observation)).thenReturn(null);
		
		CodeableConcept result = interpretationTranslator.toFhirResource(observation);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfObsIsNull() {
		Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
		
		Obs result = observationComponentTranslator.toOpenmrsType(null, component);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnObsIfComponentNull() {
		Obs obs = new Obs();
		
		Obs result = observationComponentTranslator.toOpenmrsType(obs, null);
		
		assertThat(result, is(obs));
	}
	
	@Test
	public void toOpenmrsType_shouldSetObsUuidToComponentId() {
		Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
		component.setId(OBS_UUID);
		
		Obs obs = new Obs();
		Obs result = observationComponentTranslator.toOpenmrsType(obs, component);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(OBS_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldSetObsConceptToComponentCode() {
		Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.setId(OBS_CONCEPT_UUID);
		component.setCode(codeableConcept);
		
		Concept concept = new Concept();
		concept.setUuid(OBS_CONCEPT_UUID);
		when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(concept);
		
		Obs obs = new Obs();
		Obs result = observationComponentTranslator.toOpenmrsType(obs, component);
		
		assertThat(result, notNullValue());
		assertThat(result.getConcept(), notNullValue());
		assertThat(result.getConcept().getUuid(), equalTo(OBS_CONCEPT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldSetObsValue() {
		Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
		Quantity value = new Quantity(130d);
		component.setValue(value);
		when(observationValueTranslator.toOpenmrsType(any(Obs.class), same(value))).thenAnswer((i) -> {
			((Obs) i.getArguments()[0]).setValueNumeric(((Quantity) i.getArguments()[1]).getValue().doubleValue());
			return i.getArguments()[0];
		});
		
		Obs obs = new Obs();
		Obs result = observationComponentTranslator.toOpenmrsType(obs, component);
		
		assertThat(result, notNullValue());
		assertThat(result.getValueNumeric(), notNullValue());
		assertThat(result.getValueNumeric(), equalTo(130d));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnObsAsItIsForAddInterpretation() {
		Obs obs = new Obs();
		CodeableConcept interpretation = new CodeableConcept()
		        .addCoding(new Coding(FhirConstants.INTERPRETATION_VALUE_SET_URI, "N", "Normal"));
		when(interpretationTranslator.toOpenmrsType(obs, interpretation)).thenReturn(obs);
		Obs result = interpretationTranslator.toOpenmrsType(obs, interpretation);
		
		assertThat(result, equalTo(obs));
	}
}
