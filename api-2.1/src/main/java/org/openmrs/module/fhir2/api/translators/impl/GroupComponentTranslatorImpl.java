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

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.Group;
import org.openmrs.module.fhir2.api.translators.GroupComponentTranslator;
import org.openmrs.module.fhir2.model.GroupMember;
import org.springframework.stereotype.Component;

@Component
public class GroupComponentTranslatorImpl implements GroupComponentTranslator {
	
	@Override
	public Group.GroupMemberComponent toFhirResource(@Nonnull GroupMember member) {
		Group.GroupMemberComponent component = new Group.GroupMemberComponent();
		component.setId(member.getId());
		component.setEntity(member.getEntity());
		component.setEntityTarget(member.getEntityTarget());
		component.setInactive(member.getInactive());
		component.setPeriod(member.getPeriod());
		return component;
	}
	
	@Override
	public GroupMember toOpenmrsType(@Nonnull Group.GroupMemberComponent component) {
		GroupMember member = new GroupMember();
		member.setId(component.getId());
		member.setEntity(component.getEntity());
		member.setEntityTarget(component.getEntityTarget());
		member.setInactive(component.getInactive());
		member.setPeriod(component.getPeriod());
		return member;
	}
}
