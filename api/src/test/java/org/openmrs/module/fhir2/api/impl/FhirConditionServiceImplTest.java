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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
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
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirConditionServiceImplTest {
	
	private static final String OBS_UUID = "12345-abcde-12345";
	
	private static final String WRONG_OBS_CONDITION_UUID = "90378769-f1a4-46af-034j";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirConditionDao<Obs> dao;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<Condition> searchQueryInclude;
	
	@Mock
	private SearchQuery<org.openmrs.Obs, Condition, FhirConditionDao<org.openmrs.Obs>, ConditionTranslator<org.openmrs.Obs>, SearchQueryInclude<Condition>> searchQuery;
	
	@Mock
	private ConditionTranslator<Obs> translator;
	
	private FhirConditionServiceImpl fhirConditionService;
	
	private Obs obsCondition;
	
	private org.hl7.fhir.r4.model.Condition condition;
	
	@Before
	public void setup() {
		fhirConditionService = new FhirConditionServiceImpl() {
			
			@Override
			protected void validateObject(Obs object) {
			}
		};
		
		fhirConditionService.setDao(dao);
		fhirConditionService.setTranslator(translator);
		fhirConditionService.setSearchQueryInclude(searchQueryInclude);
		fhirConditionService.setSearchQuery(searchQuery);
		
		obsCondition = new Obs();
		obsCondition.setUuid(OBS_UUID);
		
		condition = new org.hl7.fhir.r4.model.Condition();
		condition.setId(OBS_UUID);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void getObsConditionByUuid_shouldReturnConditionByUuid() {
		when(dao.get(OBS_UUID)).thenReturn(obsCondition);
		when(translator.toFhirResource(obsCondition)).thenReturn(condition);
		
		Condition result = fhirConditionService.get(OBS_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(OBS_UUID));
	}
	
	@Test
	public void shouldThrowExceptionWhenGetMissingUuid() {
		assertThrows(ResourceNotFoundException.class, () -> fhirConditionService.get(WRONG_OBS_CONDITION_UUID));
	}
	
	@Test
	public void create_shouldCreateNewCondition() {
		when(translator.toFhirResource(obsCondition)).thenReturn(condition);
		when(dao.createOrUpdate(obsCondition)).thenReturn(obsCondition);
		when(translator.toOpenmrsType(condition)).thenReturn(obsCondition);
		
		org.hl7.fhir.r4.model.Condition result = fhirConditionService.create(condition);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(OBS_UUID));
	}
	
	@Test
	public void update_shouldUpdateExistingObsCondition() {
		when(dao.get(OBS_UUID)).thenReturn(obsCondition);
		when(translator.toFhirResource(obsCondition)).thenReturn(condition);
		when(dao.createOrUpdate(obsCondition)).thenReturn(obsCondition);
		when(translator.toOpenmrsType(any(Obs.class), any(org.hl7.fhir.r4.model.Condition.class))).thenReturn(obsCondition);
		
		org.hl7.fhir.r4.model.Condition result = fhirConditionService.update(OBS_UUID, condition);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(OBS_UUID));
	}
	
	@Test
	public void update_shouldThrowExceptionWhenIdIsNull() {
		org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
		
		assertThrows(InvalidRequestException.class, () -> fhirConditionService.update(null, condition));
	}
	
	@Test
	public void update_shouldThrowExceptionWhenConditionIsNull() {
		assertThrows(InvalidRequestException.class, () -> fhirConditionService.update(OBS_UUID, null));
	}
	
	@Test
	public void update_shouldThrowExceptionWhenConditionIdDoesNotMatchCurrentId() {
		org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
		condition.setId(OBS_UUID);
		
		assertThrows(InvalidRequestException.class, () -> fhirConditionService.update(WRONG_OBS_CONDITION_UUID, condition));
	}
	
	@Test
	public void delete_shouldDeleteExistingCondition() {
		when(dao.delete(OBS_UUID)).thenReturn(obsCondition);
		when(translator.toFhirResource(obsCondition)).thenReturn(condition);
		
		org.hl7.fhir.r4.model.Condition result = fhirConditionService.delete(OBS_UUID);
		
		assertThat(result, notNullValue());
	}
	
	@Test
	public void delete_shouldThrowExceptionWhenIdIsNull() {
		assertThrows(InvalidRequestException.class, () -> fhirConditionService.delete(null));
	}
	
	@Test
	public void searchConditions_shouldReturnTranslatedConditionReturnedByDao() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam();
		patientReference.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam(org.hl7.fhir.r4.model.Patient.SP_GIVEN, "patient name")));
		
		TokenAndListParam codeList = new TokenAndListParam();
		codeList.addValue(new TokenOrListParam().add(new TokenParam("test code")));
		
		TokenAndListParam clinicalList = new TokenAndListParam();
		clinicalList.addValue(new TokenOrListParam().add(new TokenParam("test clinical")));
		
		DateRangeParam onsetDate = new DateRangeParam().setLowerBound("gt2020-05-01").setUpperBound("lt2021-05-01");
		
		QuantityAndListParam onsetAge = new QuantityAndListParam();
		onsetAge.addValue(new QuantityOrListParam().add(new QuantityParam(12)));
		
		DateRangeParam recordDate = new DateRangeParam().setLowerBound("gt2020-05-01").setUpperBound("lt2021-05-01");
		
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(OBS_UUID));
		
		DateRangeParam lastUpdated = new DateRangeParam().setLowerBound(LAST_UPDATED_DATE).setUpperBound(LAST_UPDATED_DATE);
		
		SortSpec sort = new SortSpec("sort param");
		
		HashSet<Include> includes = new HashSet<>();
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference)
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, codeList)
		        .addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, onsetAge)
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "onsetDate", onsetDate)
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "dateCreated", recordDate)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)
		        .setSortSpec(sort);
		
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(OBS_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(obsCondition));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(translator.toFhirResource(obsCondition)).thenReturn(condition);
		
		IBundleProvider result = fhirConditionService.searchConditions(patientReference, codeList, clinicalList, onsetDate,
		    onsetAge, recordDate, uuid, lastUpdated, sort, includes);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
}
