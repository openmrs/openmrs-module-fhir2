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

public interface GroupMemberTranslator<T> extends OpenmrsFhirUpdatableTranslator<T, Group.GroupMemberComponent> {
	
	/**
	 * Maps an OpenMRS data element to a FHIR resource
	 *
	 * @param cohortMember the OpenMRS data element to translate
	 * @return the corresponding FHIR resource
	 */
	@Override
	Group.GroupMemberComponent toFhirResource(@Nonnull T cohortMember);
	
	/**
	 * Maps a FHIR resource to an OpenMRS data element
	 *
	 * @param groupMemberComponent the FHIR resource to translate
	 * @return the corresponding OpenMRS data element
	 */
	@Override
	T toOpenmrsType(@Nonnull Group.GroupMemberComponent groupMemberComponent);
	
	/**
	 * Maps a FHIR resource to an existing OpenMRS data element
	 *
	 * @param existingCohort the existingObject to update
	 * @param groupMemberComponent the resource to map
	 * @return an updated version of the existingObject
	 */
	@Override
	T toOpenmrsType(@Nonnull T existingCohort, @Nonnull Group.GroupMemberComponent groupMemberComponent);
}
