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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Condition;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirDiagnosisService;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.TwoSearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.param.ConditionSearchParams;
import org.openmrs.module.fhir2.api.search.param.DiagnosisSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirConditionServiceImplTest {
	
	private static final String CONDITION_UUID = "43578769-f1a4-46af-b08b-d9fe8a07066f";
	
	private static final String WRONG_CONDITION_UUID = "90378769-f1a4-46af-b08b-d9fe8a09034j";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirConditionDao dao;
	
	@Mock
	private ConditionTranslator<Condition> conditionTranslator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<org.hl7.fhir.r4.model.Condition> searchQueryInclude;
	
	@Mock
	private SearchQuery<Condition, org.hl7.fhir.r4.model.Condition, FhirConditionDao, ConditionTranslator<Condition>, SearchQueryInclude<org.hl7.fhir.r4.model.Condition>> searchQuery;
	
	private FhirConditionServiceImpl conditionService;
	
	@Mock
	private FhirDiagnosisService diagnosisService;
	
	private Condition openmrsCondition;
	
	private org.hl7.fhir.r4.model.Condition fhirCondition;
	
	@Before
	public void setup() {
		conditionService = new FhirConditionServiceImpl() {
			
			@Override
			protected void validateObject(Condition object) {
			}
		};
		conditionService.setDao(dao);
		conditionService.setTranslator(conditionTranslator);
		conditionService.setSearchQuery(searchQuery);
		conditionService.setSearchQueryInclude(searchQueryInclude);
		conditionService.setDiagnosisService(diagnosisService);
		conditionService.setGlobalPropertyService(globalPropertyService);
		
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
	public void shouldThrowExceptionWhenGetMissingUuid() {
		when(diagnosisService.get(WRONG_CONDITION_UUID)).thenThrow(ResourceNotFoundException.class);
		assertThrows(ResourceNotFoundException.class, () -> conditionService.get(WRONG_CONDITION_UUID));
	}
	
	@Test
	public void create_shouldCreateNewCondition() {
		Condition openMrsCondition = new Condition();
		openMrsCondition.setUuid(CONDITION_UUID);
		
		org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
		condition.setId(CONDITION_UUID);
		
		when(conditionTranslator.toFhirResource(openMrsCondition)).thenReturn(condition);
		when(dao.createOrUpdate(openMrsCondition)).thenReturn(openMrsCondition);
		when(conditionTranslator.toOpenmrsType(condition)).thenReturn(openMrsCondition);
		
		org.hl7.fhir.r4.model.Condition result = conditionService.create(condition);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void create_shouldThrowExceptionWhenConditionIsNull() {
		assertThrows(InvalidRequestException.class, () -> conditionService.create(null));
	}
	
	@Test
	public void update_shouldUpdateExistingCondition() {
		Condition openmrsCondition = new Condition();
		openmrsCondition.setUuid(CONDITION_UUID);
		
		org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
		condition.setId(CONDITION_UUID);
		
		when(dao.get(CONDITION_UUID)).thenReturn(openmrsCondition);
		when(conditionTranslator.toFhirResource(openmrsCondition)).thenReturn(condition);
		when(dao.createOrUpdate(openmrsCondition)).thenReturn(openmrsCondition);
		when(conditionTranslator.toOpenmrsType(any(Condition.class), any(org.hl7.fhir.r4.model.Condition.class)))
		        .thenReturn(openmrsCondition);
		
		org.hl7.fhir.r4.model.Condition result = conditionService.update(CONDITION_UUID, condition);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void update_shouldThrowExceptionWhenIdIsNull() {
		org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
		
		assertThrows(InvalidRequestException.class, () -> conditionService.update(null, condition));
	}
	
	@Test
	public void update_shouldThrowExceptionWhenConditionIsNull() {
		assertThrows(InvalidRequestException.class, () -> conditionService.update(CONDITION_UUID, null));
	}
	
	@Test
	public void update_shouldThrowExceptionWhenConditionIdIsNull() {
		org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
		
		assertThrows(InvalidRequestException.class, () -> conditionService.update(CONDITION_UUID, condition));
	}
	
	@Test
	public void update_shouldThrowExceptionWhenConditionIdDoesNotMatchCurrentId() {
		org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
		condition.setId(WRONG_CONDITION_UUID);
		
		assertThrows(InvalidRequestException.class, () -> conditionService.update(CONDITION_UUID, condition));
	}
	
	@Test
	public void delete_shouldDeleteExistingCondition() {
		Condition openmrsCondition = new Condition();
		openmrsCondition.setUuid(CONDITION_UUID);
		
		org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
		condition.setId(CONDITION_UUID);
		
		when(dao.delete(CONDITION_UUID)).thenReturn(openmrsCondition);
		
		conditionService.delete(CONDITION_UUID);
	}
	
	@Test
	public void delete_shouldThrowExceptionWhenIdIsNull() {
		assertThrows(InvalidRequestException.class, () -> conditionService.delete(null));
	}
	
	@Test
	public void searchConditions_shouldReturnTranslatedConditionReturnedByDao() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam();
		patientReference.addValue(new ReferenceOrListParam().add(new ReferenceParam(Patient.SP_GIVEN, "patient name")));
		
		TokenAndListParam codeList = new TokenAndListParam();
		codeList.addValue(new TokenOrListParam().add(new TokenParam("test code")));
		
		TokenAndListParam clinicalList = new TokenAndListParam();
		clinicalList.addValue(new TokenOrListParam().add(new TokenParam("test clinical")));
		
		DateRangeParam onsetDate = new DateRangeParam().setLowerBound("gt2020-05-01").setUpperBound("lt2021-05-01");
		
		QuantityAndListParam onsetAge = new QuantityAndListParam();
		onsetAge.addValue(new QuantityOrListParam().add(new QuantityParam(12)));
		
		DateRangeParam recordDate = new DateRangeParam().setLowerBound("gt2020-05-01").setUpperBound("lt2021-05-01");
		
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(CONDITION_UUID));
		
		TokenAndListParam category = new TokenAndListParam().addAnd(new TokenOrListParam()
		        .add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_CONDITION));
		
		DateRangeParam lastUpdated = new DateRangeParam().setLowerBound(LAST_UPDATED_DATE).setUpperBound(LAST_UPDATED_DATE);
		
		SortSpec sort = new SortSpec("sort param");
		
		HashSet<Include> includes = new HashSet<>();
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference)
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, codeList)
		        .addParameter(FhirConstants.CONDITION_CLINICAL_STATUS_HANDLER, clinicalList)
		        .addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, onsetAge)
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "onsetDate", onsetDate)
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "dateCreated", recordDate)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)
		        .setSortSpec(sort);
		
		List<Condition> daoResults = Collections.singletonList(openmrsCondition);
		when(dao.getSearchResults(any())).thenReturn(daoResults);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, conditionTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(conditionTranslator.toFhirResources(daoResults)).thenReturn(Collections.singletonList(fhirCondition));
		
		IBundleProvider result = conditionService.searchConditions(new ConditionSearchParams(patientReference, codeList,
		        clinicalList, onsetDate, onsetAge, recordDate, category, uuid, lastUpdated, sort, includes));
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void create_shouldDelegateToDiagnosisServiceForDiagnosis() {
		org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
		CodeableConcept category = new CodeableConcept();
		category.addCoding(
		    new Coding(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS, null));
		condition.addCategory(category);
		
		org.hl7.fhir.r4.model.Condition expected = new org.hl7.fhir.r4.model.Condition();
		when(diagnosisService.create(condition)).thenReturn(expected);
		
		org.hl7.fhir.r4.model.Condition result = conditionService.create(condition);
		assertThat(result, equalTo(expected));
	}
	
	@Test
	public void update_shouldDelegateToDiagnosisServiceForDiagnosis() {
		org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
		condition.setId(CONDITION_UUID);
		CodeableConcept category = new CodeableConcept();
		category.addCoding(
		    new Coding(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS, null));
		condition.addCategory(category);
		
		org.hl7.fhir.r4.model.Condition expected = new org.hl7.fhir.r4.model.Condition();
		when(diagnosisService.update(CONDITION_UUID, condition)).thenReturn(expected);
		org.hl7.fhir.r4.model.Condition result = conditionService.update(CONDITION_UUID, condition);
		assertThat(result, equalTo(expected));
	}
	
	@Test
	public void shouldSearchExplicitlyFor_shouldReturnTrueForNullParam() {
		boolean result = conditionService.shouldSearchExplicitlyFor(null, FhirConstants.CONDITION_CATEGORY_CODE_CONDITION);
		
		assertThat(result, equalTo(true));
	}
	
	@Test
	public void shouldSearchExplicitlyFor_shouldReturnTrueWhenTagMatches() {
		TokenAndListParam category = new TokenAndListParam().addAnd(new TokenOrListParam()
		        .add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_CONDITION));
		
		boolean result = conditionService.shouldSearchExplicitlyFor(category,
		    FhirConstants.CONDITION_CATEGORY_CODE_CONDITION);
		
		assertThat(result, equalTo(true));
	}
	
	@Test
	public void shouldSearchExplicitlyFor_shouldReturnFalseWhenTagDoesNotMatch() {
		TokenAndListParam category = new TokenAndListParam().addAnd(new TokenOrListParam()
		        .add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS));
		
		boolean result = conditionService.shouldSearchExplicitlyFor(category,
		    FhirConstants.CONDITION_CATEGORY_CODE_CONDITION);
		
		assertThat(result, equalTo(false));
	}
	
	@Test
	public void get_shouldReturnDiagnosisWhenConditionNotFound() {
		when(dao.get(CONDITION_UUID)).thenReturn(null);
		org.hl7.fhir.r4.model.Condition diagnosis = new org.hl7.fhir.r4.model.Condition();
		diagnosis.setId(CONDITION_UUID);
		when(diagnosisService.get(CONDITION_UUID)).thenReturn(diagnosis);
		
		org.hl7.fhir.r4.model.Condition result = conditionService.get(CONDITION_UUID);
		
		assertThat(result, equalTo(diagnosis));
	}
	
	@Test
	public void delete_shouldDelegateToDiagnosisServiceWhenConditionMissing() {
		when(dao.delete(CONDITION_UUID)).thenReturn(null);
		
		conditionService.delete(CONDITION_UUID);
		
		verify(diagnosisService).delete(CONDITION_UUID);
	}
	
	@Test
	public void searchConditions_shouldReturnDiagnosisBundleWhenOnlyDiagnosisMatches() {
		TokenAndListParam category = new TokenAndListParam().addAnd(new TokenOrListParam()
		        .add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS));
		
		IBundleProvider diagBundle = new SimpleBundleProvider();
		when(diagnosisService.searchDiagnoses(any(DiagnosisSearchParams.class))).thenReturn(diagBundle);
		
		IBundleProvider result = conditionService.searchConditions(
		    new ConditionSearchParams(null, null, null, null, null, null, category, null, null, null, new HashSet<>()));
		
		assertThat(result, sameInstance(diagBundle));
	}
	
	@Test
	public void searchConditions_shouldReturnTwoBundleProviderWhenBothMatch() {
		TokenAndListParam category = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		            FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS))
		        .addAnd(new TokenOrListParam().add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI,
		            FhirConstants.CONDITION_CATEGORY_CODE_CONDITION));
		
		IBundleProvider diagBundle = new SimpleBundleProvider();
		IBundleProvider condBundle = new SimpleBundleProvider();
		when(diagnosisService.searchDiagnoses(any(DiagnosisSearchParams.class))).thenReturn(diagBundle);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(condBundle);
		
		IBundleProvider result = conditionService.searchConditions(
		    new ConditionSearchParams(null, null, null, null, null, null, category, null, null, null, new HashSet<>()));
		
		assertThat(result instanceof TwoSearchQueryBundleProvider, equalTo(true));
	}
	
	@Test
	public void searchConditions_shouldReturnConditionBundleWhenOnlyConditionMatches() {
		TokenAndListParam category = new TokenAndListParam().addAnd(new TokenOrListParam()
		        .add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_CONDITION));
		
		IBundleProvider condBundle = new SimpleBundleProvider();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(condBundle);
		
		IBundleProvider result = conditionService.searchConditions(
		    new ConditionSearchParams(null, null, null, null, null, null, category, null, null, null, new HashSet<>()));
		
		assertThat(result, sameInstance(condBundle));
	}
	
	@Test
	public void searchConditions_shouldReturnEmptyBundleWhenNoMatches() {
		TokenAndListParam category = new TokenAndListParam();
		
		IBundleProvider result = conditionService.searchConditions(
		    new ConditionSearchParams(null, null, null, null, null, null, category, null, null, null, new HashSet<>()));
		
		assertThat(result.getResources(0, 1), empty());
	}
	
}
