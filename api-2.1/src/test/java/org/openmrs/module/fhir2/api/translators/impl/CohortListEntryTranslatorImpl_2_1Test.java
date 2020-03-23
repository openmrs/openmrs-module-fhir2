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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ListResource;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class CohortListEntryTranslatorImpl_2_1Test {
	
	private static final String PATIENT_UUID = "c0938432-1691-11df-9pa5-7038c432aaba";
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private FhirPatientDao patientDao;
	
	private CohortListEntryTranslatorImpl_2_1 listEntryTranslatorImpl_2_1;
	
	@Before
	public void setup() {
		listEntryTranslatorImpl_2_1 = new CohortListEntryTranslatorImpl_2_1();
		listEntryTranslatorImpl_2_1.setPatientReferenceTranslator(patientReferenceTranslator);
		listEntryTranslatorImpl_2_1.setPatientDao(patientDao);
	}
	
	@Test
	public void toFhirResource_shouldReturnNullWhenCalledWithNullObject() {
		List<ListResource.ListEntryComponent> list = listEntryTranslatorImpl_2_1.toFhirResource(null);
		assertThat(list, nullValue());
	}
	
	@Test
	public void toFhirResource_shouldTranslateCohortMembershipToListEntries() {
		Patient patient = new Patient();
		patient.setId(1);
		
		Cohort cohort = new Cohort();
		cohort.addMembership(new CohortMembership(1, new Date()));
		
		Reference patientReference = new Reference().setReference(FhirConstants.PATIENT + "/" + PATIENT_UUID)
		        .setType(FhirConstants.PATIENT).setIdentifier(new Identifier().setValue(PATIENT_UUID));
		
		when(patientDao.getPatientById(1)).thenReturn(patient);
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientReference);
		
		List<ListResource.ListEntryComponent> list = listEntryTranslatorImpl_2_1.toFhirResource(cohort);
		assertThat(list, notNullValue());
		assertThat(list.size(), greaterThanOrEqualTo(1));
		assertThat(list.get(0).getDate(), DateMatchers.sameDay(new Date()));
		assertThat(list.get(0).getItem(), equalTo(patientReference));
	}
	
	@Test
	public void toFhirResource_shouldExcludeVoidedCohortMembers() {
		Cohort cohort = new Cohort();
		CohortMembership cohortMembership = new CohortMembership(1, new Date());
		cohortMembership.setVoided(true);
		cohort.addMembership(cohortMembership);
		
		List<ListResource.ListEntryComponent> list = listEntryTranslatorImpl_2_1.toFhirResource(cohort);
		assertThat(list, notNullValue());
		assertThat(list.size(), equalTo(0));
	}
	
	@Test
	public void toFhirResource_shouldExcludeCohortMembersWhoseEndDateHavePassed() {
		Cohort cohort = new Cohort();
		CohortMembership cohortMembership = new CohortMembership(1, new Date());
		cohortMembership.setEndDate(DateUtils.addDays(new Date(), -60));
		cohort.addMembership(cohortMembership);
		
		List<ListResource.ListEntryComponent> list = listEntryTranslatorImpl_2_1.toFhirResource(cohort);
		assertThat(list, notNullValue());
		assertThat(list.size(), equalTo(0));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnCohortAsIsIfCalledWithNullObject() {
		Cohort cohort = new Cohort();
		cohort.setId(1);
		
		Cohort result = listEntryTranslatorImpl_2_1.toOpenmrsType(cohort, null);
		
		assertThat(result, equalTo(cohort));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnCohortAsIsIfCalledWithEmptyList() {
		Cohort cohort = new Cohort();
		cohort.setId(1);
		
		List<ListResource.ListEntryComponent> list = new ArrayList<>();
		
		Cohort result = listEntryTranslatorImpl_2_1.toOpenmrsType(cohort, null);
		
		assertThat(result, equalTo(cohort));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateListEntriesToCohortMembership() {
		Reference patientReference = new Reference().setReference(FhirConstants.PATIENT + "/" + PATIENT_UUID)
		        .setType(FhirConstants.PATIENT).setIdentifier(new Identifier().setValue(PATIENT_UUID));
		
		Cohort cohort = new Cohort();
		cohort.setId(1);
		
		Patient patient = new Patient();
		patient.setId(1);
		
		List<ListResource.ListEntryComponent> list = new ArrayList<>();
		ListResource.ListEntryComponent entryComponent = new ListResource.ListEntryComponent();
		entryComponent.setItem(patientReference);
		entryComponent.setDate(new Date());
		list.add(entryComponent);
		
		when(patientReferenceTranslator.toOpenmrsType(patientReference)).thenReturn(patient);
		
		Cohort result = listEntryTranslatorImpl_2_1.toOpenmrsType(cohort, list);
		assertThat(result, notNullValue());
		assertThat(result.getMemberships(), notNullValue());
		assertThat(result.getMemberships().size(), greaterThanOrEqualTo(1));
		assertThat(result.getMemberships().iterator().next().getPatientId(), equalTo(1));
	}
	
}

