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

import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
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
	public IBundleProvider searchQuestionnaire(@OptionalParam(name = Questionnaire.SP_NAME) StringAndListParam name,
	        @OptionalParam(name = Questionnaire.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated, @Sort SortSpec sort) {
		
		return fhirQuestionnaireService.searchForQuestionnaires(new QuestionnaireSearchParams(name, id, lastUpdated, sort));
	}
	
	/**
	 * The $everything operation fetches all the information related the specified questionnaire
	 *
	 * @param questionnaireId The id of the questionnaire
	 * @return a bundle of resources which reference to or are referenced from the questionnaire
	 */
	@Operation(name = "everything", idempotent = true, type = Questionnaire.class, bundleType = BundleTypeEnum.SEARCHSET)
	public IBundleProvider getQuestionnaireEverything(@IdParam IdType questionnaireId) {
		
		if (questionnaireId == null || questionnaireId.getIdPart() == null || questionnaireId.getIdPart().isEmpty()) {
			return null;
		}
		
		TokenParam questionnaireReference = new TokenParam().setValue(questionnaireId.getIdPart());
		
		return fhirQuestionnaireService.getQuestionnaireEverything(questionnaireReference);
	}
	
	/**
	 * The $everything operation fetches all the information related to all the questionnaires
	 *
	 * @return a bundle of resources which reference to or are referenced from the questionnaires
	 */
	@Operation(name = "everything", idempotent = true, type = Questionnaire.class, bundleType = BundleTypeEnum.SEARCHSET)
	public IBundleProvider getQuestionnaireEverything() {
		return fhirQuestionnaireService.getQuestionnaireEverything();
	}
	
}
