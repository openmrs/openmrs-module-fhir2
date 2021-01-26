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
import org.openmrs.Cohort;
import org.openmrs.module.fhir2.api.translators.GroupMemberTranslator;
import org.openmrs.module.fhir2.api.translators.GroupTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.MODULE)
public class GroupTranslatorImpl implements GroupTranslator {
	
	@Autowired
	private GroupMemberTranslator<Integer> groupMemberTranslator;
	
	@Override
	public Group toFhirResource(@Nonnull Cohort cohort) {
		notNull(cohort, "Cohort object should not be null");
		Group group = new Group();
		group.setId(cohort.getUuid());
		group.setActive(!cohort.getVoided());
		
		// Set to always person for now
		group.setType(Group.GroupType.PERSON);
		group.setName(cohort.getName());
		
		cohort.getMemberIds().forEach(id -> group.addMember(groupMemberTranslator.toFhirResource(id)));
		
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
		
		if (group.hasId()) {
			existingCohort.setUuid(group.getId());
		}
		
		if (group.hasName()) {
			existingCohort.setName(group.getName());
		}
		
		if (group.hasActive()) {
			existingCohort.setVoided(!group.getActive());
		}
		
		if (group.hasMember()) {
			group.getMember().forEach(member -> existingCohort.addMember(groupMemberTranslator.toOpenmrsType(member)));
		}
		
		return existingCohort;
	}
}
