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
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getLastUpdated;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getVersionId;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Group;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.translators.GroupComponentTranslator;
import org.openmrs.module.fhir2.api.translators.GroupMemberTranslator_2_1;
import org.openmrs.module.fhir2.api.translators.GroupTranslator;
import org.openmrs.module.fhir2.model.GroupMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Setter(AccessLevel.MODULE)
@OpenmrsProfile(openmrsPlatformVersion = "2.1.* - 2.*")
public class GroupTranslatorImpl_2_1 extends BaseGroupTranslator implements GroupTranslator {
	
	@Autowired
	private GroupMemberTranslator_2_1 groupMemberTranslator21;
	
	@Autowired
	private GroupComponentTranslator componentTranslator;
	
	@Override
	public Group toFhirResource(@Nonnull Cohort cohort) {
		notNull(cohort, "Cohort object should not be null");
		Group group = super.toFhirResource(cohort);
		
		Collection<CohortMembership> memberships = cohort.getMemberships();
		log.info("Number of members {} ", memberships.size());
		group.setQuantity(memberships.size());
		memberships.forEach(membership -> group
		        .addMember(componentTranslator.toFhirResource(groupMemberTranslator21.toFhirResource(membership))));
		
		group.getMeta().setLastUpdated(getLastUpdated(cohort));
		group.getMeta().setVersionId(getVersionId(cohort));
		
		return group;
	}
	
	@Override
	public Cohort toOpenmrsType(@Nonnull Group group) {
		notNull(group, "Group resource should not be null");
		return toOpenmrsType(new Cohort(), group);
	}
	
	@Override
	public Cohort toOpenmrsType(@Nonnull Cohort existingCohort, @Nonnull Group group) {
		notNull(group, "group resource object should not be null");
		notNull(existingCohort, "ExistingCohort object should not be null");
		
		Cohort finalExistingCohort = super.toOpenmrsType(existingCohort, group);
		
		if (group.hasMember()) {
			Set<CohortMembership> memberships = new HashSet<>();
			group.getMember().forEach(
			    member -> memberships.add(this.setCohort(existingCohort, componentTranslator.toOpenmrsType(member))));
			existingCohort.setMemberships(memberships);
		}
		
		return finalExistingCohort;
	}
	
	private CohortMembership setCohort(Cohort cohort, GroupMember groupMember) {
		CohortMembership cohortMembership = groupMemberTranslator21.toOpenmrsType(groupMember);
		cohortMembership.setCohort(cohort);
		return cohortMembership;
	}
}
