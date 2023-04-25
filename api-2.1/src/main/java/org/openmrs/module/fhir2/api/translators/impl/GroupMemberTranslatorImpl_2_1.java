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

import java.util.Date;

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
	public CohortMembership toOpenmrsType(@Nonnull CohortMembership existingMembership, @Nonnull GroupMember groupMember) {
		notNull(groupMember, "groupMemberReference object should not be null");
		notNull(existingMembership, "ExistingCohort object should not be null");
		
		if (groupMember.hasEntity()) {
			Patient patient = patientReferenceTranslator.toOpenmrsType(groupMember.getEntity());
			if (patient != null) {
				existingMembership.setPatientId(patient.getPatientId());
			}
		}
		
		if (groupMember.hasPeriod()) {
			existingMembership.setStartDate(groupMember.getPeriod().getStart());
			existingMembership.setEndDate(groupMember.getPeriod().getEnd());
		} else {
			if (existingMembership.getStartDate() == null) {
				existingMembership.setStartDate(new Date());
			}
		}
		
		if (groupMember.hasInactive()) {
			Date now = new Date();
			if (existingMembership.getEndDate() == null || existingMembership.getEndDate().after(now)) {
				existingMembership.setEndDate(now);
			}
		}
		
		return existingMembership;
	}
}
