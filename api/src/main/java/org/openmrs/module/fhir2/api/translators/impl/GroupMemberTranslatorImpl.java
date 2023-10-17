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
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.GroupMemberTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.model.GroupMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.MODULE)
public class GroupMemberTranslatorImpl implements GroupMemberTranslator {
	
	@Autowired
	private FhirPatientDao patientDao;
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Override
	public GroupMember toFhirResource(@Nonnull Integer memberId) {
		notNull(memberId, "MemberId should not be null");
		return new GroupMember(patientReferenceTranslator.toFhirResource(patientDao.getPatientById(memberId)));
	}
	
	@Override
	public Integer toOpenmrsType(@Nonnull GroupMember groupMember) {
		notNull(groupMember, "GroupMember object cannot not be null");
		return toOpenmrsType(-1, groupMember);
	}
	
	@Override
	public Integer toOpenmrsType(@Nonnull Integer existingMemberId, @Nonnull GroupMember groupMember) {
		notNull(existingMemberId, "Existing memberId should not be null");
		notNull(groupMember, "GroupMember Object should not be null");
		return patientReferenceTranslator.toOpenmrsType(groupMember.getEntity()).getPatientId();
	}
}
