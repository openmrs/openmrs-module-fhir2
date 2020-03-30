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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.ListResource;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Setter(AccessLevel.PACKAGE)
@Primary
@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.1.* - 2.*")
public class CohortListEntryTranslatorImpl_2_1 extends CohortListEntryTranslatorImpl {
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private FhirPatientDao patientDao;
	
	@Override
	public List<ListResource.ListEntryComponent> toFhirResource(Cohort cohort) {
		if (cohort == null) {
			return null;
		}
		
		List<ListResource.ListEntryComponent> list = new ArrayList<>();
		ListResource.ListEntryComponent entry = new ListResource.ListEntryComponent();
		for (CohortMembership member : cohort.getMemberships()) {
			if (!member.getVoided() && (member.getEndDate() == null || member.getEndDate().after(new Date()))) {
				entry.setItem(patientReferenceTranslator.toFhirResource(patientDao.getPatientById(member.getPatientId())));
				entry.setDate(member.getStartDate());
				list.add(entry);
			}
		}
		
		return list;
	}
	
	@Override
	public Cohort toOpenmrsType(Cohort existingCohort, List<ListResource.ListEntryComponent> cohortList) {
		if (cohortList == null || cohortList.isEmpty()) {
			return existingCohort;
		}
		
		for (ListResource.ListEntryComponent entry : cohortList) {
			existingCohort.addMembership(new CohortMembership(
			        patientReferenceTranslator.toOpenmrsType(entry.getItem()).getPatientId(), entry.getDate()));
		}
		
		return existingCohort;
	}
}
