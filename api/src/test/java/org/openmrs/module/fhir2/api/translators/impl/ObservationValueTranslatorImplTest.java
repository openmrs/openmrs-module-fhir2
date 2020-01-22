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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.Date;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareOnlyThisForTest({ Context.class })
public class ObservationValueTranslatorImplTest {
	
	private static final String CONCEPT_VALUE_UUID = "12345-abcde-54321";
	
	private static final String OBS_STRING = "An ingenious observation";
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	private Obs obs;
	
	private ObservationValueTranslatorImpl obsValueTranslator;
	
	@Before
	public void setup() {
		obsValueTranslator = new ObservationValueTranslatorImpl();
		obsValueTranslator.setConceptTranslator(conceptTranslator);
		
		obs = new Obs();
	}
	
	@Test
	public void toFhirResource_shouldConvertObsWithCodedValueToCodeableConcept() {
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_VALUE_UUID);
		obs.setValueCoded(concept);
		
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.setId(CONCEPT_VALUE_UUID);
		when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
		
		Type result = obsValueTranslator.toFhirResource(obs);
		
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(CodeableConcept.class));
		assertThat(result.getId(), equalTo(CONCEPT_VALUE_UUID));
	}
	
	@Test
	public void toFhirResource_shouldConvertObsWithDatetimeToDateTimeType() {
		Date expected = new Date();
		obs.setValueDate(expected);
		
		Type result = obsValueTranslator.toFhirResource(obs);
		
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(DateTimeType.class));
		assertThat(((DateTimeType) result).getValue(), equalTo(expected));
	}
	
	@Test
	public void toFhirResource_shouldConvertObsWithNumericValueToQuantity() {
		obs.setValueNumeric(130d);
		
		Type result = obsValueTranslator.toFhirResource(obs);
		
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(Quantity.class));
		assertThat(((Quantity) result).getValue().doubleValue(), equalTo(130d));
	}
	
	@Test
	public void toFhirResource_shouldConvertObsWithBooleanValueToBoolean() {
		mockStatic(Context.class);
		Concept trueConcept = new Concept();
		trueConcept.setId(1046);
		ConceptService conceptService = mock(ConceptService.class);
		when(Context.getConceptService()).thenReturn(conceptService);
		when(conceptService.getTrueConcept()).thenReturn(trueConcept);
		ConceptDatatype booleanDatatype = mock(ConceptDatatype.class);
		when(booleanDatatype.isBoolean()).thenReturn(true);
		
		Concept obsConcept = new Concept();
		obsConcept.setDatatype(booleanDatatype);
		
		obs.setConcept(obsConcept);
		obs.setValueBoolean(true);
		
		Type result = obsValueTranslator.toFhirResource(obs);
		
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(BooleanType.class));
		assertThat(((BooleanType) result).booleanValue(), is(true));
	}
	
	@Test
	public void toFhirResource_shouldConvertObsWithTextValueToString() {
		obs.setValueText(OBS_STRING);
		
		Type result = obsValueTranslator.toFhirResource(obs);
		
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(StringType.class));
		assertThat(((StringType) result).getValue(), equalTo(OBS_STRING));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfObsIsNull() {
		Type result = obsValueTranslator.toFhirResource(null);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldConvertCodeableConceptToConcept() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.setId(CONCEPT_VALUE_UUID);
		
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_VALUE_UUID);
		when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(concept);
		
		Obs result = obsValueTranslator.toOpenmrsType(obs, codeableConcept);
		
		assertThat(result, notNullValue());
		assertThat(result.getValueCoded().getUuid(), equalTo(CONCEPT_VALUE_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldConvertDateTimeToDateValue() {
		Date expectedValue = new Date();
		DateTimeType dateTimeType = new DateTimeType();
		dateTimeType.setValue(expectedValue);
		
		Obs result = obsValueTranslator.toOpenmrsType(obs, dateTimeType);
		
		assertThat(result, notNullValue());
		assertThat(result.getValueDatetime(), equalTo(expectedValue));
	}
	
	@Test
	public void toOpenmrsType_shouldConvertIntegerToNumericValue() {
		IntegerType integerType = new IntegerType();
		integerType.setValue(130);
		
		Obs result = obsValueTranslator.toOpenmrsType(obs, integerType);
		
		assertThat(result, notNullValue());
		assertThat(result.getValueNumeric(), equalTo(130d));
	}
	
	@Test
	public void toOpenmrsType_shouldConvertQuantityToNumericValue() {
		Quantity quantity = new Quantity();
		quantity.setValue(130d);
		
		Obs result = obsValueTranslator.toOpenmrsType(obs, quantity);
		
		assertThat(result, notNullValue());
		assertThat(result.getValueNumeric(), equalTo(130d));
	}
	
	@Test
	public void toOpenmrsType_shouldConvertBooleanToBooleanValue() {
		mockStatic(Context.class);
		Concept trueConcept = new Concept();
		trueConcept.setId(1046);
		ConceptService conceptService = mock(ConceptService.class);
		when(Context.getConceptService()).thenReturn(conceptService);
		when(conceptService.getTrueConcept()).thenReturn(trueConcept);
		ConceptDatatype booleanDatatype = mock(ConceptDatatype.class);
		when(booleanDatatype.isBoolean()).thenReturn(true);
		
		Concept obsConcept = new Concept();
		obsConcept.setDatatype(booleanDatatype);
		
		obs.setConcept(obsConcept);
		
		BooleanType booleanType = new BooleanType();
		booleanType.setValue(true);
		
		Obs result = obsValueTranslator.toOpenmrsType(obs, booleanType);
		
		assertThat(result, notNullValue());
		assertThat(result.getValueBoolean(), is(true));
	}
	
	@Test
	public void toOpenmrsType_shouldConvertStringToTextValue() {
		StringType stringType = new StringType();
		stringType.setValue(OBS_STRING);
		
		Obs result = obsValueTranslator.toOpenmrsType(obs, stringType);
		
		assertThat(result, notNullValue());
		assertThat(result.getValueText(), equalTo(OBS_STRING));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnObsIfValueIsNull() {
		Obs result = obsValueTranslator.toOpenmrsType(obs, null);
		
		assertThat(result, notNullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfObsIsNull() {
		Obs result = obsValueTranslator.toOpenmrsType(null, new BooleanType());
		
		assertThat(result, nullValue());
	}
}
