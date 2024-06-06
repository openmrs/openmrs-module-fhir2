/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators;

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.Questionnaire;
import org.openmrs.module.fhir2.api.util.FormResourceAuditable;

public interface QuestionnaireTranslator extends OpenmrsFhirUpdatableTranslator<FormResourceAuditable, Questionnaire> {
	
	/**
	 * Maps an {@link org.openmrs.Form} to a {@link Questionnaire}
	 * 
	 * @param formResource the form to translate
	 * @return the corresponding FHIR questionnaire resource
	 */
	@Override
	Questionnaire toFhirResource(@Nonnull FormResourceAuditable formResource);
	
	/**
	 * Maps a {@link Questionnaire} to an {@link org.openmrs.FormResource}
	 * 
	 * @param questionnaire the FHIR questionnaire to translate
	 * @return the corresponding OpenMRS FormResource
	 */
	@Override
	FormResourceAuditable toOpenmrsType(@Nonnull Questionnaire questionnaire);
	
	/**
	 * Maps a {@link Questionnaire} to an existing {@link org.openmrs.FormResource}
	 * 
	 * @param currentFormResource the existing OpenMRS formResource to update
	 * @param questionnaire the FHIR questionnaire to translate
	 * @return the updated OpenMRS formResource
	 */
	@Override
	FormResourceAuditable toOpenmrsType(@Nonnull FormResourceAuditable currentFormResource,
	        @Nonnull Questionnaire questionnaire);
}
