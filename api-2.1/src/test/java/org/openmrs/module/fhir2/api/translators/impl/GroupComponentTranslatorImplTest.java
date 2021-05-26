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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.model.GroupMember;

public class GroupComponentTranslatorImplTest {
	
	public static final String PATIENT_REF = "Patient/787e12bd-314e-4cc4-9b4d-1cdff9be9545";
	
	private GroupComponentTranslatorImpl translator;
	
	private GroupMember member;
	
	private Group.GroupMemberComponent component;
	
	@Before
	public void setup() {
		translator = new GroupComponentTranslatorImpl();
		component = new Group.GroupMemberComponent();
		member = new GroupMember();
	}
	
	@Test
	public void shouldTranslateEntityToFhirType() {
		Reference patientRef = new Reference(PATIENT_REF);
		member.setEntity(patientRef);
		
		Group.GroupMemberComponent result = translator.toFhirResource(member);
		assertThat(result.getEntity().getReference(), is(PATIENT_REF));
	}
	
	@Test
	public void shouldTranslateInactiveToFhirType() {
		member.setInactive(false);
		
		assertThat(translator.toFhirResource(member).getInactive(), is(false));
	}
	
	@Test
	public void shouldTranslateEntityToOpenmrsType() {
		Reference patientRef = new Reference(PATIENT_REF);
		component.setEntity(patientRef);
		
		GroupMember result = translator.toOpenmrsType(component);
		assertThat(result.getEntity().getReference(), is(PATIENT_REF));
	}
	
	@Test
	public void shouldTranslateInactiveToOpenmrsType() {
		component.setInactive(false);
		
		assertThat(translator.toOpenmrsType(component).getInactive(), is(false));
	}
	
}
