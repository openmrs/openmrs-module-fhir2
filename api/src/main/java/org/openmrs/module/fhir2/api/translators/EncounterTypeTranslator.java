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

import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;

public interface EncounterTypeTranslator<T> extends OpenmrsFhirTranslator<T, List<CodeableConcept>> {
	
	/**
	 * @param encounterType the OpenMRS encounter type or visit type to translate
	 * @return a list consisting of an encoded version of the OpenMRS encounter type or visit type
	 */
	@Override
	List<CodeableConcept> toFhirResource(@Nonnull T encounterType);
	
	/**
	 * @param encounterTypes a list consisting of an encoded version of the OpenMRS encounter type or
	 *            visit type
	 * @return the OpenMRS encounter type or visit type
	 */
	@Override
	T toOpenmrsType(@Nonnull List<CodeableConcept> encounterTypes);
}
