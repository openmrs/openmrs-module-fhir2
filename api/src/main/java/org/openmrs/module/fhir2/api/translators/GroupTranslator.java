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

import org.hl7.fhir.r4.model.Group;
import org.openmrs.Cohort;

public interface GroupTranslator extends OpenmrsFhirUpdatableTranslator<Cohort, Group> {
	
	/**
	 * Maps an OpenMRS cohort to a FHIR group resource
	 *
	 * @param cohort the OpenMRS cohort to translate
	 * @return the corresponding FHIR resource
	 */
	@Override
	Group toFhirResource(@Nonnull Cohort cohort);
	
	/**
	 * Maps a FHIR group resource to an OpenMRS cohort
	 *
	 * @param group the FHIR group resource to translate
	 * @return the corresponding OpenMRS cohort
	 */
	@Override
	Cohort toOpenmrsType(@Nonnull Group group);
	
	/**
	 * Maps a FHIR group resource to an existing OpenMRS cohort
	 *
	 * @param existingCohort the existingCohort to update
	 * @param group the group resource to map
	 * @return an updated version of the existing cohort
	 */
	@Override
	Cohort toOpenmrsType(@Nonnull Cohort existingCohort, @Nonnull Group group);
}
