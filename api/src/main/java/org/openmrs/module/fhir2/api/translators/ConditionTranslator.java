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

public interface ConditionTranslator<T> extends OpenmrsFhirUpdatableTranslator<T, org.hl7.fhir.r4.model.Condition> {
	
	/**
	 * Maps <T> an openMrs condition to a {@link org.hl7.fhir.r4.model.Condition}
	 *
	 * @param condition the OpenMRS condition to translate
	 * @return the corresponding FHIR condition resource
	 */
	@Override
	org.hl7.fhir.r4.model.Condition toFhirResource(T condition);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Condition} to an <T> an openMrs condition
	 *
	 * @param condition the FHIR condition to translate
	 * @return the corresponding OpenMRS condition
	 */
	@Override
	T toOpenmrsType(org.hl7.fhir.r4.model.Condition condition);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Condition} to an existing <T> an openMrs condition
	 *
	 * @param existingCondition the existing condition to update
	 * @param condition the condition to map
	 * @return an updated version of the existingCondition
	 */
	@Override
	T toOpenmrsType(T existingCondition, org.hl7.fhir.r4.model.Condition condition);
}
