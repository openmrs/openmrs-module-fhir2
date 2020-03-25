/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Condition;
import org.openmrs.module.fhir2.api.dao.impl.FhirConditionDaoImpl_2_2;
import org.openmrs.module.fhir2.api.translators.impl.ConditionTranslatorImpl_2_2;

@RunWith(MockitoJUnitRunner.class)
public class FhirConditionServiceImpl_2_2Test {
	
	private static final String CONDITION_UUID = "43578769-f1a4-46af-b08b-d9fe8a07066f";
	
	private static final String WRONG_CONDITION_UUID = "90378769-f1a4-46af-b08b-d9fe8a09034j";
	
	@Mock
	private FhirConditionDaoImpl_2_2 dao;
	
	@Mock
	private ConditionTranslatorImpl_2_2 conditionTranslator;
	
	private FhirConditionServiceImpl_2_2 conditionService;
	
	private Condition openmrsCondition;
	
	private org.hl7.fhir.r4.model.Condition fhirCondition;
	
	@Before
	public void setup() {
		conditionService = new FhirConditionServiceImpl_2_2();
		conditionService.setDao(dao);
		conditionService.setConditionTranslator(conditionTranslator);
		
		openmrsCondition = new Condition();
		openmrsCondition.setUuid(CONDITION_UUID);
		
		fhirCondition = new org.hl7.fhir.r4.model.Condition();
		fhirCondition.setId(CONDITION_UUID);
	}
	
	@Test
	public void shouldGetConditionByUuid() {
		when(dao.getConditionByUuid(CONDITION_UUID)).thenReturn(openmrsCondition);
		when(conditionTranslator.toFhirResource(openmrsCondition)).thenReturn(fhirCondition);
		org.hl7.fhir.r4.model.Condition condition = conditionService.getConditionByUuid(CONDITION_UUID);
		assertThat(condition, notNullValue());
		assertThat(condition.getId(), notNullValue());
		assertThat(condition.getId(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void whenGetConditionByWrongUuidShouldReturnNull() {
		assertThat(conditionService.getConditionByUuid(WRONG_CONDITION_UUID), nullValue());
	}
	
	@Test
	public void saveCondition_shouldSaveNewCondition() {
		Condition openMrsCondition = new Condition();
		openMrsCondition.setUuid(CONDITION_UUID);
		
		org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
		condition.setId(CONDITION_UUID);
		
		when(conditionTranslator.toFhirResource(openMrsCondition)).thenReturn(condition);
		when(dao.saveCondition(openMrsCondition)).thenReturn(openMrsCondition);
		when(conditionTranslator.toOpenmrsType(condition)).thenReturn(openMrsCondition);
		
		org.hl7.fhir.r4.model.Condition result = conditionService.saveCondition(condition);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void searchConditions_shouldReturnTranslatedConditionReturnedByDao() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam();
		patientReference.addValue(new ReferenceOrListParam().add(new ReferenceParam(Patient.SP_GIVEN, "patient name")));
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		subjectReference.addValue(new ReferenceOrListParam().add(new ReferenceParam(Patient.SP_GIVEN, "subject name")));
		TokenAndListParam codeList = new TokenAndListParam();
		codeList.addValue(new TokenOrListParam().add(new TokenParam("test code")));
		TokenAndListParam clinicalList = new TokenAndListParam();
		clinicalList.addValue(new TokenOrListParam().add(new TokenParam("test clinical")));
		DateRangeParam onsetDate = new DateRangeParam().setLowerBound("lower date").setUpperBound("upper date");
		QuantityParam onsetAge = new QuantityParam(12);
		DateRangeParam recordDate = new DateRangeParam().setLowerBound("lower record date")
		        .setUpperBound("upper record date");
		SortSpec sort = new SortSpec("sort param");
		when(dao.searchForConditions(patientReference, subjectReference, codeList, clinicalList, onsetDate, onsetAge,
		    recordDate, sort)).thenReturn(Arrays.asList(openmrsCondition));
		when(conditionTranslator.toFhirResource(openmrsCondition)).thenReturn(fhirCondition);
		
		Collection<org.hl7.fhir.r4.model.Condition> result = conditionService.searchConditions(patientReference,
		    subjectReference, codeList, clinicalList, onsetDate, onsetAge, recordDate, sort);
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(1));
		assertThat(result, equalTo(Arrays.asList(fhirCondition)));
	}
}
