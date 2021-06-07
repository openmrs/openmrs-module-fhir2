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
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Period;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.GroupMemberTranslator_2_1;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.model.GroupMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
@Setter(AccessLevel.MODULE)
@OpenmrsProfile(openmrsPlatformVersion = "2.1.* - 2.*")
public class GroupMemberTranslatorImpl_2_1 implements GroupMemberTranslator_2_1 {
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private FhirPatientDao patientDao;
	
	@Override
	public GroupMember toFhirResource(@Nonnull CohortMembership cohortMember) {
		notNull(cohortMember, "CohortMember object should not be null");
		GroupMember groupMember = new GroupMember();
		groupMember.setId(cohortMember.getUuid());
		groupMember.setInactive(!cohortMember.isActive());
		
		Patient patient = patientDao.getPatientById(cohortMember.getPatientId());
		if (patient != null) {
			groupMember.setEntity(patientReferenceTranslator.toFhirResource(patient));
		}
		
		Period period = new Period();
		period.setStart(cohortMember.getStartDate());
		period.setEnd(cohortMember.getEndDate());
		groupMember.setPeriod(period);
		
		return groupMember;
	}
	
	@Override
	public CohortMembership toOpenmrsType(@Nonnull GroupMember groupMember) {
		notNull(groupMember, "GroupMember object should not be null");
		return toOpenmrsType(new CohortMembership(), groupMember);
	}
	
	@Override
	public CohortMembership toOpenmrsType(@Nonnull CohortMembership existingCohort, @Nonnull GroupMember groupMember) {
		notNull(groupMember, "groupMemberReference object should not be null");
		notNull(existingCohort, "ExistingCohort object should not be null");
		
		if (groupMember.hasEntity()) {
			existingCohort.setPatientId(patientReferenceTranslator.toOpenmrsType(groupMember.getEntity()).getPatientId());
		}
		
		if (groupMember.hasPeriod()) {
			existingCohort.setStartDate(groupMember.getPeriod().getStart());
			existingCohort.setEndDate(groupMember.getPeriod().getEnd());
		}
		
		if (groupMember.hasInactive()) {
			existingCohort.setVoided(groupMember.getInactive());
		}
		
		return existingCohort;
	}
}
