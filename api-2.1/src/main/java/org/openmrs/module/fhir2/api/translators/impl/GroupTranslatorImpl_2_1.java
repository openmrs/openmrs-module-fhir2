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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.collection.spi.PersistentCollection;
import org.hl7.fhir.r4.model.Group;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.dao.FhirCohortMembershipDao;
import org.openmrs.module.fhir2.api.translators.GroupComponentTranslator;
import org.openmrs.module.fhir2.api.translators.GroupMemberTranslator_2_1;
import org.openmrs.module.fhir2.api.translators.GroupTranslator;
import org.openmrs.module.fhir2.model.GroupMember;
import org.openmrs.module.fhir2.model.IdUuidTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Setter(AccessLevel.MODULE)
@OpenmrsProfile(openmrsPlatformVersion = "2.1.* - 2.*")
public class GroupTranslatorImpl_2_1 extends BaseGroupTranslator implements GroupTranslator {
	
	@Autowired
	private FhirCohortMembershipDao cohortMembershipDao;
	
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
			Collection<CohortMembership> memberships = existingCohort.getMemberships();
			if (memberships instanceof PersistentCollection || existingCohort.getId() != null) {
				List<GroupMember> groupMembers = group.getMember().stream().map(componentTranslator::toOpenmrsType)
				        .collect(Collectors.toList());
				
				List<String> groupMemberUuids = group.getMember().stream().map(componentTranslator::toOpenmrsType)
				        .map(m -> m.getIdElement().getIdPart()).collect(Collectors.toList());
				
				LinkedHashMap<String, Integer> uuidToId = cohortMembershipDao.getIdsForUuids(groupMemberUuids).stream()
				        .collect(Collectors.toMap(IdUuidTuple::getUuid, IdUuidTuple::getId, (a, b) -> {
					        throw new IllegalStateException(String.format("Duplicate key %s", a));
				        }, LinkedHashMap::new));
				
				memberships.removeIf(m -> !uuidToId.containsValue(m.getPatientId()));
				
				for (GroupMember member : groupMembers) {
					if (uuidToId.containsKey(member.getIdElement().getIdPart())) {
						Integer patientId = uuidToId.get(member.getIdElement().getIdPart());
						CohortMembership membership = memberships.stream().filter(it -> patientId.equals(it.getPatientId()))
						        .findFirst().orElseGet(() -> newCohortMembership(existingCohort));
						memberships.add(groupMemberTranslator21.toOpenmrsType(membership, member));
					} else {
						memberships.add(groupMemberTranslator21.toOpenmrsType(newCohortMembership(existingCohort), member));
					}
				}
			} else {
				for (Group.GroupMemberComponent groupMemberComponent : group.getMember()) {
					memberships.add(groupMemberTranslator21.toOpenmrsType(newCohortMembership(existingCohort),
					    componentTranslator.toOpenmrsType(groupMemberComponent)));
				}
			}
		}
		
		return finalExistingCohort;
	}
	
	private CohortMembership newCohortMembership(Cohort cohort) {
		CohortMembership m = new CohortMembership();
		m.setCohort(cohort);
		return m;
	}
}
