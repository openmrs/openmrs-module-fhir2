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
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class GroupMemberTranslatorImplTest {
	
	private static final String COHORT_UUID = "787e12bd-314e-4cc4-9b4d-1cdff9be9545";
	
	private static final String COHORT_NAME = "Patient with VL > 2";
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private FhirPatientDao patientDao;
	
	private GroupMemberTranslatorImpl groupMemberTranslator;
	
	private Cohort cohort;
	
	private Group.GroupMemberComponent groupMemberComponent;
	
	@Before
	public void setup() {
		groupMemberTranslator = new GroupMemberTranslatorImpl();
		groupMemberTranslator.setPatientDao(patientDao);
		groupMemberTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
	}
	
	@Before
	public void init() {
		cohort = new Cohort();
		cohort.setUuid(COHORT_UUID);
		cohort.setName(COHORT_NAME);
		
		groupMemberComponent = new Group.GroupMemberComponent();
		groupMemberComponent.setId(COHORT_UUID);
	}
	
	@Test
	public void shouldTranslateCohortMemberToFHIRType() {
		Reference patientReference = mock(Reference.class);
		Patient patient = mock(Patient.class);
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientReference);
		when(patientDao.getPatientById(1)).thenReturn(patient);
		
		Group.GroupMemberComponent component = groupMemberTranslator.toFhirResource(1);
		assertThat(component, notNullValue());
		assertThat(component.getEntity(), notNullValue());
		assertThat(component.hasEntity(), is(true));
	}
	
	@Test
	public void shouldTranslateGroupMemberComponentToOpenMRSType() {
		Reference patientReference = mock(Reference.class);
		Patient patient = mock(Patient.class);
		when(patient.getPatientId()).thenReturn(1);
		when(patientReferenceTranslator.toOpenmrsType(patientReference)).thenReturn(patient);
		
		Group.GroupMemberComponent component = new Group.GroupMemberComponent();
		component.setEntity(patientReference);
		
		Integer patientId = groupMemberTranslator.toOpenmrsType(component);
		assertThat(patientId, notNullValue());
		assertThat(patientId, is(1));
	}
	
}
