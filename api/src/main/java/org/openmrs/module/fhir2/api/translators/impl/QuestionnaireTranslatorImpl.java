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

import static org.apache.commons.lang3.Validate.notNull;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getLastUpdated;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getVersionId;

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.*;
import org.openmrs.module.fhir2.api.dao.FhirQuestionnaireDao;
import org.openmrs.module.fhir2.api.translators.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class QuestionnaireTranslatorImpl implements QuestionnaireTranslator {
	
	@Autowired
	private FhirQuestionnaireDao questionnaireDao;
	
	@Override
	public org.hl7.fhir.r4.model.Questionnaire toFhirResource(@Nonnull Form openmrsForm) {
		notNull(openmrsForm, "The Openmrs Form object should not be null");
		
		org.hl7.fhir.r4.model.Questionnaire questionnaire = new org.hl7.fhir.r4.model.Questionnaire();
		questionnaire.setId(openmrsForm.getUuid());
		questionnaire.setName(openmrsForm.getName());
		
		questionnaire.getMeta().setLastUpdated(getLastUpdated(openmrsForm));
		questionnaire.getMeta().setVersionId(getVersionId(openmrsForm));
		
		return questionnaire;
	}
	
	@Override
	public Form toOpenmrsType(@Nonnull org.hl7.fhir.r4.model.Questionnaire questionnaire) {
		notNull(questionnaire, "The Questionnaire object should not be null");
		return toOpenmrsType(new Form(), questionnaire);
	}
	
	@Override
	public Form toOpenmrsType(@Nonnull Form openmrsForm, @Nonnull org.hl7.fhir.r4.model.Questionnaire questionnaire) {
		notNull(openmrsForm, "The existing Openmrs Form object should not be null");
		notNull(questionnaire, "The Questionnaire object should not be null");
		
		if (questionnaire.hasId()) {
			openmrsForm.setUuid(questionnaire.getIdElement().getIdPart());
		}
		
		openmrsForm.setName(questionnaire.getName());
		//TODO
		
		return openmrsForm;
	}
}
