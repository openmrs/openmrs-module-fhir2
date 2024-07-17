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

import javax.annotation.Nonnull;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.*;
import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.api.FormService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class QuestionnaireTranslatorImpl implements QuestionnaireTranslator {
	
	@Autowired
	FormService formService;
	
	@Override
	public Questionnaire toFhirResource(@Nonnull Form openmrsForm) {
		notNull(openmrsForm, "The Openmrs Form object should not be null");
		
		FormResource resource = formService.getFormResource(openmrsForm, FhirConstants.FHIR_QUESTIONNAIRE_TYPE);
		notNull(resource, "The Openmrs Form doesn't contain an FHIR Questionnaire");
		
		FhirContext ctx = FhirContext.forR4();
		IParser p = ctx.newJsonParser();
		return p.parseResource(Questionnaire.class, resource.getValue().toString());
	}
	
	@Override
	public Form toOpenmrsType(@Nonnull Questionnaire resource) {
		return formService.getForm(resource.getId());
	}
}
