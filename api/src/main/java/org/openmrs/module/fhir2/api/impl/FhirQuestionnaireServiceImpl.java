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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Questionnaire;
import org.openmrs.module.fhir2.api.FhirQuestionnaireService;
import org.openmrs.module.fhir2.api.dao.FhirQuestionnaireDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.translators.QuestionnaireTranslator;
import org.openmrs.module.fhir2.api.util.FormResourceAuditable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PACKAGE)
public class FhirQuestionnaireServiceImpl extends BaseFhirService<Questionnaire, FormResourceAuditable> implements FhirQuestionnaireService {
	
	@Autowired
	private FhirQuestionnaireDao dao;
	
	@Autowired
	private QuestionnaireTranslator translator;
	
	@Autowired
	private SearchQueryInclude<Questionnaire> searchQueryInclude;
	
	@Autowired
	private SearchQuery<FormResourceAuditable, Questionnaire, FhirQuestionnaireDao, QuestionnaireTranslator, SearchQueryInclude<Questionnaire>> searchQuery;
	
	/*
	@Override
	public IBundleProvider searchForQuestionnaire(QuestionnaireSearchParams questionnaireSearchParams) {
		return searchQuery.getQueryResults(questionnaireSearchParams.toSearchParameterMap(), dao, translator,
		    searchQueryInclude);
	}
	*/
	
	@Override
	protected boolean isVoided(FormResourceAuditable formResource) {
		return formResource.getForm().getRetired();
	}
}
