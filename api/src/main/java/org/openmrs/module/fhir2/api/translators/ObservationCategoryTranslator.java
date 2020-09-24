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

import org.hl7.fhir.r4.model.CodeableConcept;
import org.openmrs.Concept;

/**
 * A one-way translator from {@link Concept#getConceptClass()} to a {@link CodeableConcept}
 */
public interface ObservationCategoryTranslator extends ToFhirTranslator<Concept, CodeableConcept> {
	
	/**
	 * Translates a concept into a {@link CodeableConcept} valid for the Observation.category field
	 *
	 * @param concept the OpenMRS concept to translate
	 * @return a codeable concept appropriate to be added to the Observation.category field
	 */
	@Override
	CodeableConcept toFhirResource(@Nonnull Concept concept);
}
