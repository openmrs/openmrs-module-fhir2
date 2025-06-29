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
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.FhirConstants.RX_NORM_SYSTEM_URI;
import static org.openmrs.module.fhir2.FhirConstants.UCUM_SYSTEM_URI;

import java.util.Date;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.module.fhir2.api.dao.impl.FhirConceptDaoImpl;
import org.openmrs.module.fhir2.api.impl.FhirConceptServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class ObservationValueTranslatorImplTest {
	
	private static final String CONCEPT_VALUE_UUID = "12345-abcde-54321";
	
	private static final String OBS_STRING = "An ingenious observation";
	
	private ConceptTranslatorImpl conceptTranslator;
	
	@Mock
	private ObservationQuantityCodingTranslatorImpl quantityCodingTranslator;
	
	@Mock
	ConceptService conceptService;
	
	@Mock
	FhirConceptSourceService fhirConceptSourceService;
	
	private Obs obs;
	
	private ObservationValueTranslatorImpl obsValueTranslator;
	
	@Before
	public void setup() {
		FhirConceptDaoImpl fhirConceptDao = new FhirConceptDaoImpl();
		fhirConceptDao.setConceptService(conceptService);
		FhirConceptServiceImpl fhirConceptService = new FhirConceptServiceImpl();
		fhirConceptService.setDao(fhirConceptDao);
		conceptTranslator = new ConceptTranslatorImpl();
		conceptTranslator.setConceptService(fhirConceptService);
		conceptTranslator.setConceptSourceService(fhirConceptSourceService);
		obsValueTranslator = new ObservationValueTranslatorImpl() {
			
			@Override
			protected Boolean getValueBoolean(Obs obs) {
				if (obs.getValueCoded() != null) {
					if (obs.getValueCoded().equals(conceptService.getTrueConcept())) {
						return true;
					} else if (obs.getValueCoded().equals(conceptService.getFalseConcept())) {
						return false;
					}
				}
				return null;
			}
			
			@Override
			protected void setValueBoolean(Obs obs, Boolean valueBoolean) {
				if (valueBoolean == Boolean.TRUE) {
					obs.setValueCoded(conceptService.getTrueConcept());
				} else if (valueBoolean == Boolean.FALSE) {
					obs.setValueCoded(conceptService.getFalseConcept());
				} else {
					obs.setValueCoded(null);
				}
			}
		};
		obsValueTranslator.setConceptTranslator(conceptTranslator);
		obsValueTranslator.setQuantityCodingTranslator(quantityCodingTranslator);
		
		obs = new Obs();
	}
	
	@Test
	public void toFhirResource_shouldConvertObsWithCodedValueToCodeableConcept() {
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_VALUE_UUID);
		obs.setValueCoded(concept);
		
		Type result = obsValueTranslator.toFhirResource(obs);
		
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(CodeableConcept.class));
		CodeableConcept codeableConceptResult = (CodeableConcept) result;
		assertThat(codeableConceptResult.getCodingFirstRep().getCode(), equalTo(CONCEPT_VALUE_UUID));
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
	public void toFhirResource_shouldConvertObsWithNumericValueToIntegerQuantity() {
		ConceptNumeric cn = new ConceptNumeric();
		cn.setAllowDecimal(false);
		
		obs.setValueNumeric(130.0d);
		obs.setConcept(cn);
		
		Type result = obsValueTranslator.toFhirResource(obs);
		
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(Quantity.class));
		assertThat(((Quantity) result).getValue().longValueExact(), equalTo(130l));
	}
	
	@Test
	public void toFhirResource_shouldConvertObsWithNumericValueAndUnitsToQuantityWithUnits() {
		ConceptNumeric cn = new ConceptNumeric();
		cn.setUnits("cm");
		obs.setValueNumeric(130d);
		obs.setConcept(cn);
		
		Coding coding = new Coding(null, "cm", "cm");
		when(quantityCodingTranslator.toFhirResource(cn)).thenReturn(coding);
		
		Quantity result = (Quantity) obsValueTranslator.toFhirResource(obs);
		
		assertThat(result, notNullValue());
		assertThat(result.getValue().doubleValue(), equalTo(130d));
		assertThat(result.getUnit(), equalTo("cm"));
		assertThat(result.getSystem(), equalTo(null));
		assertThat(result.getCode(), equalTo(null));
	}
	
	@Test
	public void toFhirResource_shouldIgnoreNumericValueUnitSystemIfNotUCUM() {
		ConceptNumeric cn = new ConceptNumeric();
		cn.setUnits("mm[Hg]");
		obs.setValueNumeric(130d);
		obs.setConcept(cn);
		
		Coding coding = new Coding(RX_NORM_SYSTEM_URI, "mm[Hg]", "mmHg");
		when(quantityCodingTranslator.toFhirResource(cn)).thenReturn(coding);
		
		Quantity result = (Quantity) obsValueTranslator.toFhirResource(obs);
		
		assertThat(result, notNullValue());
		assertThat(result.getValue().doubleValue(), equalTo(130d));
		assertThat(result.getUnit(), equalTo("mm[Hg]"));
		assertThat(result.getSystem(), equalTo(null));
		assertThat(result.getCode(), equalTo(null));
	}
	
	@Test
	public void toFhirResource_shouldConvertObsWithCommonUCUMUnitsToQuantityWithUCUMSystem() {
		ConceptNumeric cn = new ConceptNumeric();
		cn.setUnits("mm[Hg]");
		obs.setValueNumeric(130d);
		obs.setConcept(cn);
		
		Coding coding = new Coding(UCUM_SYSTEM_URI, "mm[Hg]", "mmHg");
		when(quantityCodingTranslator.toFhirResource(cn)).thenReturn(coding);
		
		Quantity result = (Quantity) obsValueTranslator.toFhirResource(obs);
		
		assertThat(result, notNullValue());
		assertThat(result.getValue().doubleValue(), equalTo(130d));
		assertThat(result.getUnit(), equalTo("mm[Hg]"));
		assertThat(result.getSystem(), equalTo(UCUM_SYSTEM_URI));
		assertThat(result.getCode(), equalTo("mm[Hg]"));
	}
	
	@Test
	public void toFhirResource_shouldConvertObsWithBooleanValueToBoolean() {
		Concept trueConcept = new Concept();
		trueConcept.setId(1046);
		when(conceptService.getTrueConcept()).thenReturn(trueConcept);
		Concept obsConcept = new Concept();
		
		obs.setConcept(obsConcept);
		obs.setValueCoded(conceptService.getTrueConcept());
		
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
		codeableConcept.addCoding(new Coding(null, CONCEPT_VALUE_UUID, CONCEPT_VALUE_UUID));
		
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_VALUE_UUID);
		when(conceptService.getConceptByUuid(CONCEPT_VALUE_UUID)).thenReturn(concept);
		
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
		Concept trueConcept = new Concept();
		trueConcept.setId(1046);
		when(conceptService.getTrueConcept()).thenReturn(trueConcept);
		
		Concept obsConcept = new Concept();
		obs.setConcept(obsConcept);
		
		BooleanType booleanType = new BooleanType();
		booleanType.setValue(true);
		
		Obs result = obsValueTranslator.toOpenmrsType(obs, booleanType);
		
		assertThat(result, notNullValue());
		assertThat(result.getValueCoded(), is(conceptService.getTrueConcept()));
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
	public void toOpenmrsType_shouldNotThrowExceptionIfValueIsNull() {
		Obs result = obsValueTranslator.toOpenmrsType(obs, null);
		
		assertThat(result, nullValue());
	}
	
	@Test(expected = NullPointerException.class)
	public void toOpenmrsType_shouldThrowExceptionIfObsIsNull() {
		obsValueTranslator.toOpenmrsType(null, new BooleanType());
	}
}
