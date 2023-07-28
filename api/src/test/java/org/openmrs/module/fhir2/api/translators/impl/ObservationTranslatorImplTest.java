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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationBasedOnReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationCategoryTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationEffectiveDatetimeTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationInterpretationTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationReferenceRangeTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationStatusTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationValueTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class ObservationTranslatorImplTest {
	
	private static final String OBS_UUID = "12345-abcde-12345";
	
	private static final String CONCEPT_UUID = "54321-abcde-54321";
	
	private static final String ENCOUNTER_UUID = "12345-abcde-54321";
	
	private static final String PATIENT_UUID = "12345-edcba-12345";
	
	private static final String ORDER_UUID = "12344-edcba-12345";
	
	private static final String LOCATION_UUID = "2321gh23-kj34h45-34jk3-34k34k";
	
	private static final Double LOW_NORMAL_VALUE = 1.0;
	
	private static final Double HIGH_NORMAL_VALUE = 2.0;
	
	@Mock
	private ObservationStatusTranslator observationStatusTranslator;
	
	@Mock
	private ObservationReferenceTranslator observationReferenceTranslator;
	
	@Mock
	private ObservationValueTranslator observationValueTranslator;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private ObservationCategoryTranslator categoryTranslator;
	
	@Mock
	private EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private ObservationInterpretationTranslator interpretationTranslator;
	
	@Mock
	private ObservationReferenceRangeTranslator referenceRangeTranslator;
	
	@Mock
	private ObservationBasedOnReferenceTranslator basedOnReferenceTranslator;
	
	@Mock
	private ObservationEffectiveDatetimeTranslator datetimeTranslator;
	
	private ObservationTranslatorImpl observationTranslator;
	
	@Before
	public void setup() {
		observationTranslator = new ObservationTranslatorImpl();
		observationTranslator.setObservationStatusTranslator(observationStatusTranslator);
		observationTranslator.setObservationReferenceTranslator(observationReferenceTranslator);
		observationTranslator.setObservationValueTranslator(observationValueTranslator);
		observationTranslator.setConceptTranslator(conceptTranslator);
		observationTranslator.setCategoryTranslator(categoryTranslator);
		observationTranslator.setEncounterReferenceTranslator(encounterReferenceTranslator);
		observationTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
		observationTranslator.setInterpretationTranslator(interpretationTranslator);
		observationTranslator.setReferenceRangeTranslator(referenceRangeTranslator);
		observationTranslator.setBasedOnReferenceTranslator(basedOnReferenceTranslator);
		observationTranslator.setDatetimeTranslator(datetimeTranslator);
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
	public void toFhirResource_shouldConvertEncounterToReference() {
		Obs observation = new Obs();
		Encounter encounter = new Encounter();
		encounter.setUuid(ENCOUNTER_UUID);
		observation.setEncounter(encounter);
		Reference encounterReference = new Reference();
		encounterReference.setType("Encounter");
		encounterReference.setId(ENCOUNTER_UUID);
		when(encounterReferenceTranslator.toFhirResource(encounter)).thenReturn(encounterReference);
		
		Observation result = observationTranslator.toFhirResource(observation);
		
		assertThat(result.getEncounter(), notNullValue());
		assertThat(result.getEncounter().getType(), equalTo("Encounter"));
		assertThat(result.getEncounter().getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void toFhirResource_shouldConvertPatientToReference() {
		Obs observation = new Obs();
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		observation.setPerson(patient);
		Reference patientReference = new Reference();
		patientReference.setType("Patient");
		patientReference.setId(PATIENT_UUID);
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientReference);
		
		Observation result = observationTranslator.toFhirResource(observation);
		
		assertThat(result.getSubject(), notNullValue());
		assertThat(result.getSubject().getType(), equalTo("Patient"));
		assertThat(result.getSubject().getId(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldNotConvertAPersonToSubject() {
		Obs observation = new Obs();
		Person person = new Person();
		person.setUuid(PATIENT_UUID);
		observation.setPerson(person);
		
		Observation result = observationTranslator.toFhirResource(observation);
		
		assertThat(result.hasSubject(), is(false));
		verify(patientReferenceTranslator, never()).toFhirResource(any());
	}
	
	@Test
	public void toFhirResource_shouldAddReferenceRange() {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setLowNormal(LOW_NORMAL_VALUE);
		conceptNumeric.setHiNormal(HIGH_NORMAL_VALUE);
		
		Obs observation = new Obs();
		observation.setConcept(conceptNumeric);
		observation.setValueNumeric((HIGH_NORMAL_VALUE + LOW_NORMAL_VALUE) / 2);
		
		Observation.ObservationReferenceRangeComponent referenceRangeComponent = new Observation.ObservationReferenceRangeComponent();
		referenceRangeComponent.setLow(new Quantity().setValue(LOW_NORMAL_VALUE));
		referenceRangeComponent.setHigh(new Quantity().setValue(HIGH_NORMAL_VALUE));
		
		List<Observation.ObservationReferenceRangeComponent> referenceRangeComponentList = new ArrayList<>();
		referenceRangeComponentList.add(referenceRangeComponent);
		
		when(referenceRangeTranslator.toFhirResource(conceptNumeric)).thenReturn(referenceRangeComponentList);
		
		Observation result = observationTranslator.toFhirResource(observation);
		
		assertThat(result.getReferenceRange(), notNullValue());
		assertThat(result.getReferenceRange(),
		    hasItem(hasProperty("low", hasProperty("value", equalTo(BigDecimal.valueOf(LOW_NORMAL_VALUE))))));
		assertThat(result.getReferenceRange(),
		    hasItem(hasProperty("high", hasProperty("value", equalTo(BigDecimal.valueOf(HIGH_NORMAL_VALUE))))));
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
	public void toFhirResource_shouldConvertObsGroupToHasMemberReferences() {
		Obs observation = new Obs();
		Obs childObs = new Obs();
		String referencePath = FhirConstants.OBSERVATION + "/" + OBS_UUID;
		childObs.setUuid(OBS_UUID);
		observation.addGroupMember(childObs);
		
		Reference reference = new Reference();
		reference.setType(FhirConstants.OBSERVATION).setReference(referencePath);
		when(observationReferenceTranslator.toFhirResource(childObs)).thenReturn(reference);
		
		Observation result = observationTranslator.toFhirResource(observation);
		
		assertThat(result.getHasMember(), notNullValue());
		assertThat(result.getHasMember().size(), equalTo(1));
		assertThat(result.getHasMember(), hasItem(hasProperty("reference", equalTo(referencePath))));
		assertThat(result.getHasMember(), hasItem(hasProperty("type", equalTo(FhirConstants.OBSERVATION))));
	}
	
	@Test(expected = NullPointerException.class)
	public void toFhirResource_shouldThrowExceptionIfObsIsNull() {
		observationTranslator.toFhirResource(null);
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenMrsDateChangedToLastUpdatedDate() {
		Obs observation = new Obs();
		observation.setDateChanged(new Date());
		Observation result = observationTranslator.toFhirResource(observation);
		
		assertThat(result, notNullValue());
		assertThat(result.getMeta().getLastUpdated(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenMrsDateChangedToVersionId() {
		Obs observation = new Obs();
		observation.setDateChanged(new Date());
		Observation result = observationTranslator.toFhirResource(observation);
		
		assertThat(result, notNullValue());
		assertThat(result.getMeta().getVersionId(), notNullValue());
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenMrsDateCreatedToDateIssued() {
		Obs observation = new Obs();
		observation.setDateCreated(new Date());
		Observation result = observationTranslator.toFhirResource(observation);
		
		assertThat(result, notNullValue());
		assertThat(result.getIssued(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenMrsObsDatetimeToEffectiveDatetime() {
		Obs observation = new Obs();
		observation.setObsDatetime(new Date());
		
		when(datetimeTranslator.toFhirResource(observation)).thenReturn(new DateTimeType(new Date()));
		
		Observation result = observationTranslator.toFhirResource(observation);
		
		assertThat(result, notNullValue());
		assertThat(result.getEffectiveDateTimeType(), notNullValue());
		assertThat(result.getEffectiveDateTimeType().getValue(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toFhirResource_shouldConvertOrderToReference() {
		Obs observation = new Obs();
		Order order = new Order();
		order.setUuid(ORDER_UUID);
		observation.setOrder(order);
		Reference orderReference = new Reference();
		orderReference.setType("Order");
		orderReference.setId(ORDER_UUID);
		when(basedOnReferenceTranslator.toFhirResource(observation.getOrder())).thenReturn(orderReference);
		
		Observation result = observationTranslator.toFhirResource(observation);
		
		assertThat(result.getBasedOn(), notNullValue());
		assertThat(result.getBasedOn().get(0).getType(), equalTo("Order"));
		assertThat(result.getBasedOn().get(0).getId(), equalTo(ORDER_UUID));
	}
	
	@Test
	public void toFhirResource_shouldAddLoationExtensionToObsWithTextValue() {
		Obs observation = new Obs();
		observation.setValueText(LOCATION_UUID);
		observation.setComment("org.openmrs.Location");
		
		Observation result = observationTranslator.toFhirResource(observation);
		
		assertThat(result, notNullValue());
		assertThat(result.getExtension(), is(notNullValue()));
		
		Extension extension = result.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_OBS_LOCATION_VALUE);
		assertThat(extension, is(notNullValue()));
		assertThat(extension.getValue(), instanceOf(Reference.class));
		assertThat(((Reference) extension.getValue()).getType(), equalTo(FhirConstants.LOCATION));
		assertThat(((Reference) extension.getValue()).getReference(), equalTo(FhirConstants.LOCATION + "/" + LOCATION_UUID));
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
	public void toOpenmrsType_shouldConvertValueToObsValue() {
		Observation observation = new Observation();
		observation.setValue(new Quantity(130));
		
		observationTranslator.toOpenmrsType(new Obs(), observation);
		
		ArgumentCaptor<Quantity> quantityCaptor = ArgumentCaptor.forClass(Quantity.class);
		verify(observationValueTranslator).toOpenmrsType(any(Obs.class), quantityCaptor.capture());
		assertThat(quantityCaptor.getValue().getValue(), equalTo(BigDecimal.valueOf(130)));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateHasMemberReferencesToGroupObs() {
		Observation observation = new Observation();
		Reference reference = observation.addHasMember();
		reference.setType(FhirConstants.OBSERVATION).setReference(FhirConstants.OBSERVATION + "/" + OBS_UUID);
		Obs groupedObs = new Obs();
		groupedObs.setUuid(OBS_UUID);
		when(observationReferenceTranslator.toOpenmrsType(reference)).thenReturn(groupedObs);
		
		Obs result = observationTranslator.toOpenmrsType(new Obs(), observation);
		
		assertThat(result, notNullValue());
		assertThat(result.hasGroupMembers(), is(true));
		assertThat(result.getGroupMembers(), hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test(expected = NullPointerException.class)
	public void toOpenmrsType_shouldThrowExceptionIfObsNull() {
		Observation observation = new Observation();
		observationTranslator.toOpenmrsType(null, observation);
	}
	
	@Test(expected = NullPointerException.class)
	public void toOpenmrsType_shouldThrowExceptionIfObservationNull() {
		Obs expected = new Obs();
		observationTranslator.toOpenmrsType(expected, null);
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateEffectiveDatetimeToObsDatetime() {
		Observation observation = new Observation();
		DateTimeType dateTime = new DateTimeType();
		dateTime.setValue(new Date());
		observation.setEffective(dateTime);
		
		Obs obs = new Obs();
		Obs expected = new Obs();
		expected.setObsDatetime(new Date());
		
		observationTranslator.toOpenmrsType(obs, observation);
		assertThat(expected, notNullValue());
		assertThat(expected.getObsDatetime(), notNullValue());
		assertThat(expected.getObsDatetime(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullObsOrderWhenFhirBasedOnIsNull() {
		Observation observation = new Observation();
		Obs obs = new Obs();
		Obs result = observationTranslator.toOpenmrsType(obs, observation);
		assertThat(result.getOrder(), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateBasedOnToObsOrder() {
		Observation observation = new Observation();
		Reference orderReference = new Reference();
		orderReference.setType("Order");
		orderReference.setId(ORDER_UUID);
		observation.setBasedOn(Collections.singletonList(orderReference));
		
		Obs obs = new Obs();
		Order order = new Order();
		order.setUuid(ORDER_UUID);
		
		when(basedOnReferenceTranslator.toOpenmrsType(orderReference)).thenReturn(order);
		Obs result = observationTranslator.toOpenmrsType(obs, observation);
		assertThat(result, notNullValue());
		assertThat(result.getOrder(), notNullValue());
		assertThat(result.getOrder(), equalTo(order));
	}
}
