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
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.*;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.*;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirQuestionnaireDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.QuestionnaireSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.QuestionnaireTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirQuestionnaireServiceImplTest {
	
	private static final String FORM_UUID = "3434gh32-34h3j4-34jk34-3422h";
	
	private static final String FORM_NAME = "Form name";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final String WRONG_LAST_UPDATED_DATE = "2020-09-09";
	
	@Mock
	private QuestionnaireTranslator questionnaireTranslator;
	
	@Mock
	private FhirQuestionnaireDao dao;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<org.hl7.fhir.r4.model.Questionnaire> searchQueryInclude;
	
	@Mock
	private SearchQuery<Form, org.hl7.fhir.r4.model.Questionnaire, FhirQuestionnaireDao, QuestionnaireTranslator, SearchQueryInclude<org.hl7.fhir.r4.model.Questionnaire>> searchQuery;
	
	private FhirQuestionnaireServiceImpl questionnaireService;
	
	private org.hl7.fhir.r4.model.Questionnaire fhirQuestionnaire;
	
	private Form form;
	
	@Before
	public void setUp() {
		questionnaireService = new FhirQuestionnaireServiceImpl() {
			
			@Override
			protected void validateObject(Form object) {
			}
		};
		
		questionnaireService.setDao(dao);
		questionnaireService.setTranslator(questionnaireTranslator);
		questionnaireService.setSearchQuery(searchQuery);
		questionnaireService.setSearchQueryInclude(searchQueryInclude);
		
		form = new Form();
		form.setUuid(FORM_UUID);
		form.setName(FORM_NAME);
		
		FormResource formResource = new FormResource();
		formResource.setForm(form);
		
		fhirQuestionnaire = new org.hl7.fhir.r4.model.Questionnaire();
		fhirQuestionnaire.setId(FORM_UUID);
		fhirQuestionnaire.setName(FhirConstants.FHIR_QUESTIONNAIRE_TYPE);
	}
	
	@Test
	public void getQuestionnaireByUuid_shouldRetrieveQuestionnaireByUuid() {
		when(dao.get(FORM_UUID)).thenReturn(form);
		when(questionnaireTranslator.toFhirResource(form)).thenReturn(fhirQuestionnaire);
		
		org.hl7.fhir.r4.model.Questionnaire result = questionnaireService.get(FORM_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(FORM_UUID));
	}
	
	@Test
	public void getById_shouldReturnQuestionnaireById() {
		when(dao.getQuestionnaireById(1)).thenReturn(form);
		when(questionnaireTranslator.toFhirResource(form)).thenReturn(fhirQuestionnaire);
		
		org.hl7.fhir.r4.model.Questionnaire result = questionnaireService.getById(1);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(FORM_UUID));
	}
	
	@Test
	public void getByIds_shouldRetrieveQuestionnairesByIds() {
		when(dao.getQuestionnairesByIds(anySet())).thenReturn(Arrays.asList(form, form, form));
		when(questionnaireTranslator.toFhirResource(form)).thenReturn(fhirQuestionnaire);
		
		List<org.hl7.fhir.r4.model.Questionnaire> questionnaireList = questionnaireService
		        .getQuestionnairesByIds(new HashSet<>(Arrays.asList(1, 2, 3)));
		
		assertThat(questionnaireList, notNullValue());
		assertThat(questionnaireList, not(empty()));
		assertThat(questionnaireList, hasSize(3));
	}
	
	@Test
	public void searchForQuestionnaires_shouldSearchForQuestionnairesByName() {
		List<Form> forms = new ArrayList<>();
		forms.add(form);
		StringAndListParam stringAndListParam = new StringAndListParam().addAnd(new StringParam(FORM_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    stringAndListParam);
		
		when(dao.getSearchResults(any())).thenReturn(forms);
		when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        dao, questionnaireTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(questionnaireTranslator.toFhirResource(form)).thenReturn(fhirQuestionnaire);
		
		IBundleProvider results = questionnaireService
		        .searchForQuestionnaires(new QuestionnaireSearchParams(stringAndListParam, null, null, null));
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		assertThat(get(results), hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForQuestionnaires_shouldReturnCollectionOfQuestionnaireWhenLastUpdatedMatched() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(form));
		when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        dao, questionnaireTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(questionnaireTranslator.toFhirResource(form)).thenReturn(fhirQuestionnaire);
		
		IBundleProvider results = questionnaireService
		        .searchForQuestionnaires(new QuestionnaireSearchParams(null, null, lastUpdated, null));
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		assertThat(get(results), not(empty()));
	}
	
	@Test
	public void searchForQuestionnaires_shouldReturnEmptyCollectionWhenLastUpdatedNotMatched() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(WRONG_LAST_UPDATED_DATE)
		        .setLowerBound(WRONG_LAST_UPDATED_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        dao, questionnaireTranslator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = questionnaireService
		        .searchForQuestionnaires(new QuestionnaireSearchParams(null, null, lastUpdated, null));
		
		assertThat(results, notNullValue());
		assertThat(get(results), empty());
	}
	
	@Test
	public void getQuestionnaireEverything_shouldReturnAllInformationAboutAllQuestionnaires() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.EVERYTHING_SEARCH_HANDLER, "");
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(form));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        dao, questionnaireTranslator, globalPropertyService, searchQueryInclude));
		
		when(questionnaireTranslator.toFhirResource(form)).thenReturn(fhirQuestionnaire);
		
		IBundleProvider results = questionnaireService.getQuestionnaireEverything();
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void getQuestionnaireEverything_shouldReturnAllInformationAboutSpecifiedQuestionnaire() {
		TokenParam questionnaireId = new TokenParam().setValue(FORM_UUID);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.EVERYTHING_SEARCH_HANDLER, "")
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, questionnaireId);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(form));
		when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(questionnaireTranslator.toFhirResource(form)).thenReturn(fhirQuestionnaire);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        dao, questionnaireTranslator, globalPropertyService, searchQueryInclude));
		
		IBundleProvider results = questionnaireService.getQuestionnaireEverything(questionnaireId);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(0, 10);
	}
}
