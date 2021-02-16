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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.module.fhir2.api.translators.GroupMemberTranslator;

@RunWith(MockitoJUnitRunner.class)
public class GroupTranslatorImpl_2_1Test {
	
	private static final String COHORT_UUID = "787e12bd-314e-4cc4-9b4d-1cdff9be9545";
	
	private static final String NEW_COHORT_UUID = "787e12bd-314e-4cc4-9b4d-1cdff9be9545";
	
	private static final String COHORT_NAME = "Patient with VL > 2";
	
	@Mock
	private GroupMemberTranslator<CohortMembership> groupMemberTranslator;
	
	private GroupTranslatorImpl_2_1 groupTranslator;
	
	@Before
	public void setup() {
		groupTranslator = new GroupTranslatorImpl_2_1();
		groupTranslator.setGroupMemberTranslator(groupMemberTranslator);
	}
	
	@Test
	public void shouldTranslateUuidToIdFHIRType() {
		Cohort cohort = mock(Cohort.class);
		when(cohort.getUuid()).thenReturn(COHORT_UUID);
		
		Group group = groupTranslator.toFhirResource(cohort);
		assertThat(group, notNullValue());
		assertThat(group.getId(), is(COHORT_UUID));
	}
	
	@Test
	public void shouldTranslateNameToNameFHIRType() {
		Cohort cohort = mock(Cohort.class);
		when(cohort.getName()).thenReturn(COHORT_NAME);
		
		Group group = groupTranslator.toFhirResource(cohort);
		assertThat(group, notNullValue());
		assertThat(group.getName(), is(COHORT_NAME));
	}
	
	@Test
	public void shouldTranslateNameOpenMRSTypeToNameFHIRType() {
		Group group = mock(Group.class);
		when(group.hasName()).thenReturn(true);
		when(group.getName()).thenReturn("Mr. Moon's patient list");
		
		Cohort cohort = groupTranslator.toOpenmrsType(group);
		assertThat(cohort, notNullValue());
		assertThat(cohort.getName(), notNullValue());
		assertThat(cohort.getName(), is("Mr. Moon's patient list"));
	}
	
	@Test
	public void shouldReturnUpdatedNameOpenMRSType() {
		Cohort cohort = new Cohort();
		cohort.setName("Moon's patient list");
		
		Group group = mock(Group.class);
		when(group.hasName()).thenReturn(true);
		when(group.getName()).thenReturn("Mr. Moon's patient list");
		
		Cohort updateCohort = groupTranslator.toOpenmrsType(cohort, group);
		assertThat(updateCohort, notNullValue());
		assertThat(updateCohort.getName(), notNullValue());
		assertThat(updateCohort.getName(), is("Mr. Moon's patient list"));
	}
	
	@Test
	public void shouldTranslateIsVoidedToIsActiveFHIRType() {
		Cohort cohort = mock(Cohort.class);
		when(cohort.getVoided()).thenReturn(false);
		
		Group group = groupTranslator.toFhirResource(cohort);
		assertThat(group, notNullValue());
		assertThat(group.getActive(), is(true));
	}
	
	@Test
	public void shouldTranslateActiveFHIRTypeToIsVoidedOpenMRSType() {
		Group group = mock(Group.class);
		when(group.hasActive()).thenReturn(true);
		when(group.getActive()).thenReturn(true);
		
		Cohort cohort = groupTranslator.toOpenmrsType(group);
		assertThat(cohort, notNullValue());
		assertThat(cohort.getVoided(), is(false));
	}
	
	@Test
	public void shouldUpdateIsVoidedOpenMRSType() {
		Cohort cohort = new Cohort();
		cohort.setVoided(false);
		
		Group group = mock(Group.class);
		when(group.hasActive()).thenReturn(true);
		when(group.getActive()).thenReturn(false);
		
		Cohort updateCohort = groupTranslator.toOpenmrsType(cohort, group);
		assertThat(updateCohort, notNullValue());
		assertThat(updateCohort.getVoided(), is(true));
	}
	
	@Test
	public void shouldTranslateGroupTypeToAlwaysPerson() {
		Cohort cohort = mock(Cohort.class);
		
		Group group = groupTranslator.toFhirResource(cohort);
		assertThat(group, notNullValue());
		assertThat(group.getType(), is(Group.GroupType.PERSON));
	}
	
	@Test
	public void shouldTranslateCohortMembersToFHIRGroupMembers() {
		Cohort cohort = mock(Cohort.class);
		CohortMembership cohortMembership = mock(CohortMembership.class);
		Reference patientReference = mock(Reference.class);
		Group.GroupMemberComponent groupMemberComponent = mock(Group.GroupMemberComponent.class);
		
		when(cohort.getMemberships()).thenReturn(Arrays.asList(cohortMembership, cohortMembership));
		when(groupMemberTranslator.toFhirResource(cohortMembership)).thenReturn(groupMemberComponent);
		when(groupMemberComponent.hasEntity()).thenReturn(true);
		when(groupMemberComponent.getEntity()).thenReturn(patientReference);
		
		Group group = groupTranslator.toFhirResource(cohort);
		assertThat(group, notNullValue());
		assertThat(group.hasMember(), is(true));
		assertThat(group.getMemberFirstRep().hasEntity(), is(true));
		assertThat(group.getMemberFirstRep().getEntity(), is(patientReference));
	}
	
	@Test
	public void shouldTranslateFHIRGroupMembersToOpenMRSCohortMembers() {
		Group group = mock(Group.class);
		CohortMembership cohortMembership = mock(CohortMembership.class);
		Group.GroupMemberComponent groupMemberComponent = mock(Group.GroupMemberComponent.class);
		
		when(group.hasMember()).thenReturn(true);
		when(group.getMember()).thenReturn(Arrays.asList(groupMemberComponent, groupMemberComponent));
		when(groupMemberTranslator.toOpenmrsType(groupMemberComponent)).thenReturn(cohortMembership);
		
		Cohort cohort = groupTranslator.toOpenmrsType(group);
		assertThat(cohort, notNullValue());
		assertThat(cohort.getMemberships().isEmpty(), is(false));
		assertThat(cohort.getMemberships(), hasSize(1));
		assertThat(cohort.getMemberships().iterator().next(), is(cohortMembership));
	}
	
	@Test
	public void shouldUpdateMemberList() {
		CohortMembership cohortMembership = mock(CohortMembership.class);
		Group.GroupMemberComponent component = mock(Group.GroupMemberComponent.class);
		
		Cohort existingCohort = new Cohort();
		existingCohort.setUuid(COHORT_UUID);
		existingCohort.setVoided(false);
		existingCohort.setMemberships(Arrays.asList(cohortMembership, cohortMembership));
		
		Group group = mock(Group.class);
		when(group.hasMember()).thenReturn(true);
		when(groupMemberTranslator.toOpenmrsType(component)).thenReturn(cohortMembership);
		when(group.getMember()).thenReturn(Arrays.asList(component, component));
		
		Cohort updateCohort = groupTranslator.toOpenmrsType(existingCohort, group);
		assertThat(updateCohort, notNullValue());
		assertThat(updateCohort.getMemberships(), notNullValue());
		assertThat(updateCohort.getMemberships(), hasSize(1));
	}
}
