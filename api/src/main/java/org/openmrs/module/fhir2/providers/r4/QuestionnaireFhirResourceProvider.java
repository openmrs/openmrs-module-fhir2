/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static lombok.AccessLevel.PACKAGE;

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Person;
import org.hl7.fhir.r4.model.Questionnaire;
import org.openmrs.module.fhir2.api.FhirQuestionnaireService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.api.search.param.QuestionnaireSearchParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("questionnaireFhirR4ResourceProvider")
@R4Provider
@Setter(PACKAGE)
public class QuestionnaireFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirQuestionnaireService fhirQuestionnaireService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Questionnaire.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public Questionnaire getQuestionnaireById(@IdParam IdType id) {
		Questionnaire questionnaire = fhirQuestionnaireService.get(id.getIdPart());
		if (questionnaire == null) {
			throw new ResourceNotFoundException("Could not find Questionnaire with Id " + id.getIdPart());
		}
		return questionnaire;
	}
	
	@Search
	@SuppressWarnings("unused")
	public IBundleProvider searchQuestionnaire(@OptionalParam(name = Person.SP_NAME) StringAndListParam name,
	        @OptionalParam(name = Person.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated, @Sort SortSpec sort, @IncludeParam(allow = {
	                "Person:" + Person.SP_LINK + ":Patient", "Person:" + Person.SP_PATIENT }) HashSet<Include> includes) {
		if (CollectionUtils.isEmpty(includes)) {
			includes = null;
		}
		
		return fhirQuestionnaireService
		        .searchForQuestionnaire(new QuestionnaireSearchParams(name, id, lastUpdated, sort, includes));
	}
}
