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

import org.openmrs.module.fhir2.model.GroupMember;

public interface GroupMemberTranslator extends OpenmrsFhirUpdatableTranslator<Integer, GroupMember> {
	
	/**
	 * Maps an OpenMRS data element to a FHIR resource
	 *
	 * @param patientId the OpenMRS data element to translate
	 * @return the corresponding FHIR resource
	 */
	@Override
	GroupMember toFhirResource(@Nonnull Integer patientId);
	
	/**
	 * Maps a FHIR resource to an OpenMRS data element
	 *
	 * @param groupMember the FHIR resource to translate
	 * @return the corresponding OpenMRS data element
	 */
	@Override
	Integer toOpenmrsType(@Nonnull GroupMember groupMember);
	
	/**
	 * Maps a FHIR resource to an existing OpenMRS data element
	 *
	 * @param existingPatientId the existingObject to update
	 * @param groupMember the resource to map
	 * @return an updated version of the existingObject
	 */
	@Override
	Integer toOpenmrsType(@Nonnull Integer existingPatientId, @Nonnull GroupMember groupMember);
}
