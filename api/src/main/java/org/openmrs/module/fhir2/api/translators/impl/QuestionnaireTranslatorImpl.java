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
import org.openmrs.module.fhir2.api.translators.*;
import org.openmrs.module.fhir2.api.util.FormResourceAuditable;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class QuestionnaireTranslatorImpl implements QuestionnaireTranslator {

    @Override
    public Questionnaire toFhirResource(@Nonnull FormResourceAuditable openmrsFormResource) {
        notNull(openmrsFormResource, "The Openmrs FormResource object should not be null");

        FhirContext ctx = FhirContext.forR4();
        IParser p = ctx.newJsonParser();
        return p.parseResource(Questionnaire.class, openmrsFormResource.getValue().toString());
    }

    @Override
    public FormResourceAuditable toOpenmrsType(@Nonnull org.hl7.fhir.r4.model.Questionnaire questionnaire) {
        notNull(questionnaire, "The Questionnaire object should not be null");
        return toOpenmrsType(new FormResourceAuditable(), questionnaire);
    }

    @Override
    public FormResourceAuditable toOpenmrsType(@Nonnull FormResourceAuditable openmrsForm,
                                               @Nonnull org.hl7.fhir.r4.model.Questionnaire questionnaire) {
        notNull(openmrsForm, "The existing Openmrs Form object should not be null");
        notNull(questionnaire, "The Questionnaire object should not be null");

        return null;
    }
}
