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

import org.hl7.fhir.r4.model.ListResource;

public interface ListTranslator<T> extends ToFhirTranslator<T, ListResource>, UpdatableOpenmrsTranslator<T, ListResource> {
	
	/**
	 * Maps an {@link org.openmrs.Cohort} to a {@link org.hl7.fhir.r4.model.ListResource}
	 *
	 * @param cohort the cohort to translate
	 * @return the corresponding FHIR list resource
	 */
	@Override
	ListResource toFhirResource(T cohort);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.ListResource} to an {@link org.openmrs.Cohort}
	 *
	 * @param list the FHIR list to translate
	 * @param existingCohort the Openmrs cohort to translate to
	 * @return the corresponding OpenMRS cohort
	 */
	@Override
	T toOpenmrsType(T existingCohort, ListResource list);
	
}
