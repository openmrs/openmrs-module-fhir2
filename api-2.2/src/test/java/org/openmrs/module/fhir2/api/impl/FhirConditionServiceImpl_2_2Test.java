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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.QuantityOrListParam;
import ca.uhn.fhir.rest.param.QuantityParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Condition;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirConditionServiceImpl_2_2Test {
	
	private static final String CONDITION_UUID = "43578769-f1a4-46af-b08b-d9fe8a07066f";
	
	private static final String WRONG_CONDITION_UUID = "90378769-f1a4-46af-b08b-d9fe8a09034j";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirConditionDao<Condition> dao;
	
	@Mock
	private ConditionTranslator<Condition> conditionTranslator;
	
	@Mock
	private SearchQuery<Condition, org.hl7.fhir.r4.model.Condition, FhirConditionDao<Condition>, ConditionTranslator<Condition>> searchQuery;
	
	private FhirConditionServiceImpl_2_2 conditionService;
	
	private Condition openmrsCondition;
	
	private org.hl7.fhir.r4.model.Condition fhirCondition;
	
	@Before
	public void setup() {
		conditionService = new FhirConditionServiceImpl_2_2();
		conditionService.setDao(dao);
		conditionService.setTranslator(conditionTranslator);
		conditionService.setSearchQuery(searchQuery);
		
		openmrsCondition = new Condition();
		openmrsCondition.setUuid(CONDITION_UUID);
		
		fhirCondition = new org.hl7.fhir.r4.model.Condition();
		fhirCondition.setId(CONDITION_UUID);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void shouldGetConditionByUuid() {
		when(dao.get(CONDITION_UUID)).thenReturn(openmrsCondition);
		when(conditionTranslator.toFhirResource(openmrsCondition)).thenReturn(fhirCondition);
		org.hl7.fhir.r4.model.Condition condition = conditionService.get(CONDITION_UUID);
		assertThat(condition, notNullValue());
		assertThat(condition.getId(), notNullValue());
		assertThat(condition.getId(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void whenGetConditionByWrongUuidShouldReturnNull() {
		assertThat(conditionService.get(WRONG_CONDITION_UUID), nullValue());
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
		
		TokenAndListParam codeList = new TokenAndListParam();
		codeList.addValue(new TokenOrListParam().add(new TokenParam("test code")));
		
		TokenAndListParam clinicalList = new TokenAndListParam();
		clinicalList.addValue(new TokenOrListParam().add(new TokenParam("test clinical")));
		
		DateRangeParam onsetDate = new DateRangeParam().setLowerBound("lower date").setUpperBound("upper date");
		
		QuantityAndListParam onsetAge = new QuantityAndListParam();
		onsetAge.addValue(new QuantityOrListParam().add(new QuantityParam(12)));
		
		DateRangeParam recordDate = new DateRangeParam().setLowerBound("lower record date")
		        .setUpperBound("upper record date");
		
		SortSpec sort = new SortSpec("sort param");
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference)
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, codeList)
		        .addParameter(FhirConstants.CONDITION_CLINICAL_STATUS_HANDLER, clinicalList)
		        .addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, onsetAge)
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "onsetDate", onsetDate)
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "dateCreated", recordDate).setSortSpec(sort);
		
		when(dao.search(any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(openmrsCondition));
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, conditionTranslator));
		when(conditionTranslator.toFhirResource(openmrsCondition)).thenReturn(fhirCondition);
		
		IBundleProvider result = conditionService.searchConditions(patientReference, codeList, clinicalList, onsetDate,
		    onsetAge, recordDate, sort);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
}
