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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Group;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.GroupMemberTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.MODULE)
@OpenmrsProfile(openmrsPlatformVersion = "2.0.* - 2.1")
public class GroupMemberTranslatorImpl implements GroupMemberTranslator<Integer> {
	
	@Autowired
	private FhirPatientDao patientDao;
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Override
	public Group.GroupMemberComponent toFhirResource(@Nonnull Integer memberId) {
		notNull(memberId, "MemberId should not be null");
		return new Group.GroupMemberComponent()
		        .setEntity(patientReferenceTranslator.toFhirResource(patientDao.getPatientById(memberId)));
	}
	
	@Override
	public Integer toOpenmrsType(@Nonnull Group.GroupMemberComponent groupMemberComponent) {
		notNull(groupMemberComponent, "GroupComponent object cannot not be null");
		return toOpenmrsType(-1, groupMemberComponent);
	}
	
	@Override
	public Integer toOpenmrsType(@Nonnull Integer existingMemberId,
	        @Nonnull Group.GroupMemberComponent groupMemberComponent) {
		notNull(existingMemberId, "Existing memberId should not be null");
		notNull(groupMemberComponent, "GroupMemberComponent Object should not be null");
		if (groupMemberComponent.hasEntity()) {
			existingMemberId = patientReferenceTranslator.toOpenmrsType(groupMemberComponent.getEntity()).getPatientId();
		}
		return existingMemberId;
	}
}
