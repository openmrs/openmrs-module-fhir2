
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
import org.hl7.fhir.r4.model.Period;
import org.openmrs.CohortMembership;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.GroupMemberTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.MODULE)
@OpenmrsProfile(openmrsPlatformVersion = "2.1.* - 2.*")
public class GroupMemberTranslatorImpl_2_1 implements GroupMemberTranslator<CohortMembership> {
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private FhirPatientDao patientDao;
	
	@Override
	public Group.GroupMemberComponent toFhirResource(@Nonnull CohortMembership cohortMember) {
		notNull(cohortMember, "CohortMember object should not be null");
		Group.GroupMemberComponent groupMemberComponent = new Group.GroupMemberComponent();
		groupMemberComponent.setId(cohortMember.getUuid());
		groupMemberComponent.setEntity(
		    patientReferenceTranslator.toFhirResource(patientDao.getPatientById(cohortMember.getPatientId())));
		Period period = new Period();
		period.setStart(cohortMember.getStartDate());
		period.setEnd(cohortMember.getEndDate());
		groupMemberComponent.setPeriod(period);
		groupMemberComponent.setInactive(!cohortMember.isActive());
		
		return groupMemberComponent;
	}
	
	@Override
	public CohortMembership toOpenmrsType(@Nonnull Group.GroupMemberComponent groupMemberComponent) {
		notNull(groupMemberComponent, "GroupMemberComponent object should not be null");
		return toOpenmrsType(new CohortMembership(), groupMemberComponent);
	}
	
	@Override
	public CohortMembership toOpenmrsType(@Nonnull CohortMembership existingCohort,
	        @Nonnull Group.GroupMemberComponent groupMemberComponent) {
		notNull(groupMemberComponent, "GroupMemberComponent object should not be null");
		notNull(existingCohort, "ExistingCohort object should not be null");
		
		if (groupMemberComponent.hasEntity()) {
			existingCohort
			        .setPatientId(patientReferenceTranslator.toOpenmrsType(groupMemberComponent.getEntity()).getPatientId());
		}
		
		if (groupMemberComponent.hasPeriod()) {
			existingCohort.setStartDate(groupMemberComponent.getPeriod().getStart());
			existingCohort.setEndDate(groupMemberComponent.getPeriod().getEnd());
		}
		
		if (groupMemberComponent.hasInactive()) {
			existingCohort.setVoided(groupMemberComponent.getInactive());
		}
		
		return existingCohort;
	}
}
