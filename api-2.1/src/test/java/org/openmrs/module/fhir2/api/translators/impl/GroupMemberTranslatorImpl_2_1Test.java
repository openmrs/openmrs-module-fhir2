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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Date;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;

@RunWith(MockitoJUnitRunner.class)
public class GroupMemberTranslatorImpl_2_1Test {
	
	private static final String COHORT_UUID = "787e12bd-314e-4cc4-9b4d-1cdff9be9545";
	
	private static final String COHORT_NAME = " John's patientList";
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private FhirPatientDao patientDao;
	
	@Mock
	private PatientTranslator patientTranslator;
	
	private GroupMemberTranslatorImpl_2_1 groupMemberTranslator;
	
	@Before
	public void setup() {
		groupMemberTranslator = new GroupMemberTranslatorImpl_2_1();
		groupMemberTranslator.setPatientDao(patientDao);
		groupMemberTranslator.setPatientTranslator(patientTranslator);
		groupMemberTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
	}
	
	@Test
	public void shouldTranslateCohortMemberUuidToFHIRType() {
		CohortMembership cohortMembership = mock(CohortMembership.class);
		Patient patient = mock(Patient.class);
		Reference patientReference = mock(Reference.class);
		
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientReference);
		when(patientDao.getPatientById(anyInt())).thenReturn(patient);
		when(cohortMembership.getUuid()).thenReturn(COHORT_UUID);
		
		Group.GroupMemberComponent component = groupMemberTranslator.toFhirResource(cohortMembership);
		assertThat(component, notNullValue());
		assertThat(component.hasId(), notNullValue());
		assertThat(component.getId(), is(COHORT_UUID));
	}
	
	@Test
	public void shouldTranslateCohortMemberInactiveToFHIRType() {
		CohortMembership cohortMembership = mock(CohortMembership.class);
		Patient patient = mock(Patient.class);
		Reference patientReference = mock(Reference.class);
		
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientReference);
		when(patientDao.getPatientById(anyInt())).thenReturn(patient);
		when(cohortMembership.isActive()).thenReturn(true);
		
		Group.GroupMemberComponent component = groupMemberTranslator.toFhirResource(cohortMembership);
		assertThat(component, notNullValue());
		assertThat(component.hasInactive(), is(true));
		assertThat(component.getInactive(), is(false));
	}
	
	@Test
	public void shouldTranslateCohortMemberStartDateToFHIRType() {
		CohortMembership cohortMembership = mock(CohortMembership.class);
		Patient patient = mock(Patient.class);
		Reference patientReference = mock(Reference.class);
		
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientReference);
		when(patientDao.getPatientById(anyInt())).thenReturn(patient);
		when(cohortMembership.getStartDate()).thenReturn(Date.from(Instant.now()));
		
		Group.GroupMemberComponent component = groupMemberTranslator.toFhirResource(cohortMembership);
		assertThat(component, notNullValue());
		assertThat(component.hasPeriod(), is(true));
		assertThat(component.getPeriod().getStart(), notNullValue());
		assertThat(Date.from(Instant.now()), DateMatchers.sameDay(component.getPeriod().getStart()));
	}
	
	@Test
	public void shouldTranslateCohortMemberEndDateToFHIRType() {
		CohortMembership cohortMembership = mock(CohortMembership.class);
		Patient patient = mock(Patient.class);
		Reference patientReference = mock(Reference.class);
		
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientReference);
		when(patientDao.getPatientById(anyInt())).thenReturn(patient);
		when(cohortMembership.getEndDate()).thenReturn(Date.from(Instant.now()));
		
		Group.GroupMemberComponent component = groupMemberTranslator.toFhirResource(cohortMembership);
		assertThat(component, notNullValue());
		assertThat(component.hasPeriod(), is(true));
		assertThat(component.getPeriod().getEnd(), notNullValue());
		assertThat(Date.from(Instant.now()), DateMatchers.sameDay(component.getPeriod().getEnd()));
	}
	
	@Test
	public void shouldTranslateCohortMemberToFHIRGroupEntity() {
		CohortMembership cohortMembership = mock(CohortMembership.class);
		Patient patient = mock(Patient.class);
		Reference patientReference = mock(Reference.class);
		
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientReference);
		when(patientDao.getPatientById(anyInt())).thenReturn(patient);
		when(cohortMembership.getPatientId()).thenReturn(1);
		
		Group.GroupMemberComponent component = groupMemberTranslator.toFhirResource(cohortMembership);
		assertThat(component, notNullValue());
		assertThat(component.hasEntity(), is(true));
		assertThat(component.getEntity(), is(patientReference));
	}
	
	@Test
	public void shouldGroupEntityToCohortPatientIdOpenMRSType() {
		Reference patientReference = mock(Reference.class);
		Patient patient = mock(Patient.class);
		Group.GroupMemberComponent component = mock(Group.GroupMemberComponent.class);
		
		when(component.hasEntity()).thenReturn(true);
		when(component.getEntity()).thenReturn(patientReference);
		when(patient.getPatientId()).thenReturn(1);
		when(patientReferenceTranslator.toOpenmrsType(patientReference)).thenReturn(patient);
		
		CohortMembership membership = groupMemberTranslator.toOpenmrsType(component);
		assertThat(membership, notNullValue());
		assertThat(membership.getPatientId(), notNullValue());
		assertThat(membership.getPatientId(), equalTo(1));
	}
	
	@Test
	public void shouldGroupPeriodToCohortStartAndEndDateOpenMRSType() {
		Period period = mock(Period.class);
		Group.GroupMemberComponent component = mock(Group.GroupMemberComponent.class);
		
		when(component.hasPeriod()).thenReturn(true);
		when(component.getPeriod()).thenReturn(period);
		when(period.getStart()).thenReturn(Date.from(Instant.now()));
		when(period.getEnd()).thenReturn(Date.from(Instant.now()));
		
		CohortMembership membership = groupMemberTranslator.toOpenmrsType(component);
		assertThat(membership, notNullValue());
		assertThat(membership.getStartDate(), notNullValue());
		assertThat(membership.getEndDate(), notNullValue());
		assertThat(membership.getStartDate(), DateMatchers.sameDay(Date.from(Instant.now())));
		assertThat(membership.getEndDate(), DateMatchers.sameDay(Date.from(Instant.now())));
	}
	
	@Test
	public void shouldUpdatedGroupEntityOrCohortMembersOpenMRSType() {
		Reference patientReference = mock(Reference.class);
		Patient patient = mock(Patient.class);
		Group.GroupMemberComponent component = mock(Group.GroupMemberComponent.class);
		
		when(component.hasEntity()).thenReturn(true);
		when(component.getEntity()).thenReturn(patientReference);
		when(patient.getPatientId()).thenReturn(4);
		when(patientReferenceTranslator.toOpenmrsType(patientReference)).thenReturn(patient);
		
		//Existing cohortMembership with patient id 3
		CohortMembership cohortMembership = new CohortMembership();
		cohortMembership.setPatientId(3);
		
		CohortMembership membership = groupMemberTranslator.toOpenmrsType(cohortMembership, component);
		assertThat(membership, notNullValue());
		assertThat(membership.getPatientId(), notNullValue());
		// Updated cohortMembership with patient id 4
		assertThat(membership.getPatientId(), equalTo(4));
	}
	
	@Test
	public void shouldUpdateCohortMembershipEndDate() {
		Period period = mock(Period.class);
		Group.GroupMemberComponent component = mock(Group.GroupMemberComponent.class);
		
		// Existing cohortMembership
		CohortMembership cohortMembership = new CohortMembership();
		cohortMembership.setEndDate(Date.from(Instant.parse("2020-12-04T08:07:00Z")));
		
		when(component.hasPeriod()).thenReturn(true);
		when(component.getPeriod()).thenReturn(period);
		
		// Mocked updated date is today
		when(period.getEnd()).thenReturn(Date.from(Instant.now()));
		
		CohortMembership membership = groupMemberTranslator.toOpenmrsType(component);
		assertThat(membership, notNullValue());
		assertThat(membership.getEndDate(), notNullValue());
		assertThat(membership.getEndDate(), DateMatchers.sameDay(Date.from(Instant.now())));
	}
	
	@Test
	public void shouldUpdateCohortMembershipStartDate() {
		Period period = mock(Period.class);
		Group.GroupMemberComponent component = mock(Group.GroupMemberComponent.class);
		
		// Existing cohortMembership
		CohortMembership cohortMembership = new CohortMembership();
		cohortMembership.setStartDate(Date.from(Instant.parse("2020-12-04T08:07:00Z")));
		
		when(component.hasPeriod()).thenReturn(true);
		when(component.getPeriod()).thenReturn(period);
		
		// Mocked updated date is today
		when(period.getStart()).thenReturn(Date.from(Instant.now()));
		
		CohortMembership membership = groupMemberTranslator.toOpenmrsType(component);
		assertThat(membership, notNullValue());
		assertThat(membership.getStartDate(), notNullValue());
		assertThat(membership.getStartDate(), DateMatchers.sameDay(Date.from(Instant.now())));
	}
}
