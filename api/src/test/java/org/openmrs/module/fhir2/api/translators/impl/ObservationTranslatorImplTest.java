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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationComponentTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationStatusTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationValueTranslator;

@RunWith(MockitoJUnitRunner.class)
public class ObservationTranslatorImplTest {
	
	private static final String OBS_UUID = "12345-abcde-12345";
	
	private static final String CONCEPT_UUID = "54321-abcde-54321";
	
	@Mock
	private ObservationStatusTranslator observationStatusTranslator;
	
	@Mock
	private ObservationComponentTranslator observationComponentTranslator;
	
	@Mock
	private ObservationValueTranslator observationValueTranslator;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	private ObservationTranslatorImpl observationTranslator;
	
	@Before
	public void setup() {
		observationTranslator = new ObservationTranslatorImpl();
		observationTranslator.setObservationStatusTranslator(observationStatusTranslator);
		observationTranslator.setObservationComponentTranslator(observationComponentTranslator);
		observationTranslator.setConceptTranslator(conceptTranslator);
		observationTranslator.setObservationValueTranslator(observationValueTranslator);
	}
	
	@Test
	public void toFhirResource_shouldConvertObsToObservation() {
		Obs observation = new Obs();
		
		Observation result = observationTranslator.toFhirResource(observation);
		
		assertThat(result, notNullValue());
	}
	
	@Test
	public void toFhirResource_shouldConvertUuidToId() {
		Obs observation = new Obs();
		observation.setUuid(OBS_UUID);
		
		Observation result = observationTranslator.toFhirResource(observation);
		
		assertThat(result.getId(), equalTo(OBS_UUID));
	}
	
	@Test
	public void toFhirResource_shouldConvertStatus() {
		Obs observation = new Obs();
		when(observationStatusTranslator.toFhirResource(observation)).thenReturn(Observation.ObservationStatus.UNKNOWN);
		
		Observation result = observationTranslator.toFhirResource(observation);
		
		assertThat(result.getStatus(), is(Observation.ObservationStatus.UNKNOWN));
	}
	
	@Test
	public void toFhirResource_shouldConvertConceptToCodeableConcept() {
		Obs observation = new Obs();
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		observation.setConcept(concept);
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.setId(CONCEPT_UUID);
		when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
		
		Observation result = observationTranslator.toFhirResource(observation);
		
		assertThat(result.getCode(), notNullValue());
		assertThat(result.getCode().getId(), equalTo(CONCEPT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldConvertObsValueToValue() {
		Obs observation = new Obs();
		observation.setValueNumeric(130d);
		when(observationValueTranslator.toFhirResource(observation)).thenReturn(new Quantity(130d));
		
		Observation result = observationTranslator.toFhirResource(observation);
		
		assertThat(result.getValue(), notNullValue());
		assertThat(result.getValueQuantity(), notNullValue());
		assertThat(result.getValueQuantity().getValue().doubleValue(), equalTo(130d));
	}
	
	@Test
	public void toFhirResource_shouldConvertObsGroupToComponent() {
		Obs observation = new Obs();
		Obs childObs = new Obs();
		childObs.setUuid(OBS_UUID);
		observation.addGroupMember(childObs);
		
		Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
		component.setId(OBS_UUID);
		when(observationComponentTranslator.toFhirResource(childObs)).thenReturn(component);
		
		Observation result = observationTranslator.toFhirResource(observation);
		
		assertThat(result.getComponent(), notNullValue());
		assertThat(result.getComponent().size(), equalTo(1));
		assertThat(result.getComponent(), contains(hasProperty("id", equalTo(OBS_UUID))));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfObsIsNull() {
		Observation result = observationTranslator.toFhirResource(null);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateIdToUuid() {
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		
		Obs result = observationTranslator.toOpenmrsType(new Obs(), observation);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(OBS_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateCodeToConcept() {
		Observation observation = new Observation();
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.setId(CONCEPT_UUID);
		observation.setCode(codeableConcept);
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(concept);
		
		Obs result = observationTranslator.toOpenmrsType(new Obs(), observation);
		
		assertThat(result, notNullValue());
		assertThat(result.getConcept(), notNullValue());
		assertThat(result.getConcept().getUuid(), equalTo(CONCEPT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateComponentsToGroupObs() {
		Observation observation = new Observation();
		Observation.ObservationComponentComponent component = observation.addComponent();
		component.setId(OBS_UUID);
		Obs groupedObs = new Obs();
		groupedObs.setUuid(OBS_UUID);
		when(observationComponentTranslator.toOpenmrsType(any(), same(component))).thenReturn(groupedObs);
		
		Obs result = observationTranslator.toOpenmrsType(new Obs(), observation);
		
		assertThat(result, notNullValue());
		assertThat(result.hasGroupMembers(), is(true));
		assertThat(result.getGroupMembers(), hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfObsNull() {
		Observation observation = new Observation();
		
		Obs result = observationTranslator.toOpenmrsType(null, observation);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldNotModifyObsIfObservationNull() {
		Obs expected = new Obs();
		
		Obs result = observationTranslator.toOpenmrsType(expected, null);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(expected));
	}
}
