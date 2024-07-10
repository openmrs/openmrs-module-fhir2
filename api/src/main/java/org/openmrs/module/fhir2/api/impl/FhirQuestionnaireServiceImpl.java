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

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Questionnaire;
import org.openmrs.Form;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirQuestionnaireService;
import org.openmrs.module.fhir2.api.dao.FhirQuestionnaireDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.QuestionnaireSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.QuestionnaireTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Transactional
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PACKAGE)
public class FhirQuestionnaireServiceImpl extends BaseFhirService<Questionnaire, Form> implements FhirQuestionnaireService {
	
	@Autowired
	private FhirQuestionnaireDao dao;
	
	@Autowired
	private QuestionnaireTranslator translator;
	
	@Autowired
	private SearchQueryInclude<Questionnaire> searchQueryInclude;
	
	@Autowired
	private SearchQuery<Form, Questionnaire, FhirQuestionnaireDao, QuestionnaireTranslator, SearchQueryInclude<Questionnaire>> searchQuery;

	@Override
	public List<Questionnaire> getQuestionnairesByIds(@Nonnull Collection<Integer> ids) {
		List<org.openmrs.Form> questionnaires = dao.getQuestionnairesByIds(ids);
		return questionnaires.stream().map(translator::toFhirResource).collect(Collectors.toList());
	}

	@Override
	public Questionnaire getById(@Nonnull Integer id) {
		return translator.toFhirResource(dao.getQuestionnaireById(id));
	}

	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchForQuestionnaires(QuestionnaireSearchParams questionnaireSearchParams) {
		return searchQuery.getQueryResults(questionnaireSearchParams.toSearchParameterMap(), dao, translator, searchQueryInclude);
	}

	@Override
	@Transactional(readOnly = true)
	public IBundleProvider getQuestionnaireEverything(TokenParam questionnairId) {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.EVERYTHING_SEARCH_HANDLER, "")
				.addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY,
						new TokenAndListParam().addAnd(questionnairId));

		populateEverythingOperationParams(theParams);
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}

	@Override
	public IBundleProvider getQuestionnaireEverything() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.EVERYTHING_SEARCH_HANDLER, "");

		populateEverythingOperationParams(theParams);
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}

	private void populateEverythingOperationParams(SearchParameterMap theParams) {
		// Do nothing
	}
}
