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

import org.hl7.fhir.r4.model.Flag;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.module.fhir2.api.translators.FlagStatusTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.openmrs.module.fhir2.model.FhirFlag;

@RunWith(MockitoJUnitRunner.class)
public class FlagTranslatorImplTest {
	
	private static final String FLAG_UUID = "d0bc5a1b-ccdc-4cd8-a168-1f3259977a35";
	
	@Mock
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private FlagStatusTranslator flagStatusTranslator;
	
	private FlagTranslatorImpl flagTranslator;
	
	private FhirFlag openMrsFlag;
	
	private Flag fhirFlag;
	
	@Before
	public void setup() {
		flagTranslator = new FlagTranslatorImpl();
		flagTranslator.setFlagStatusTranslator(flagStatusTranslator);
		flagTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
		flagTranslator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
		
		openMrsFlag = new FhirFlag();
		fhirFlag = new Flag();
	}
	
	@Test
	public void shouldTranslateId() {
		openMrsFlag.setUuid(FLAG_UUID);
		fhirFlag.setId(FLAG_UUID);
		
		assertThat(flagTranslator.toFhirResource(openMrsFlag).getId(), is(fhirFlag.getId()));
		assertThat(flagTranslator.toOpenmrsType(fhirFlag).getUuid(), is(openMrsFlag.getUuid()));
		assertThat(flagTranslator.toOpenmrsType(new FhirFlag(), fhirFlag).getUuid(), is(openMrsFlag.getUuid()));
	}
	
	@Test
	public void shouldTranslateFlagStatus() {
		openMrsFlag.setStatus(FhirFlag.FlagStatus.ACTIVE);
		fhirFlag.setStatus(Flag.FlagStatus.ACTIVE);
		
		when(flagStatusTranslator.toFhirResource(FhirFlag.FlagStatus.ACTIVE)).thenReturn(Flag.FlagStatus.ACTIVE);
		when(flagStatusTranslator.toOpenmrsType(Flag.FlagStatus.ACTIVE)).thenReturn(FhirFlag.FlagStatus.ACTIVE);
		when(flagStatusTranslator.toOpenmrsType(Flag.FlagStatus.INACTIVE)).thenReturn(FhirFlag.FlagStatus.INACTIVE);
		
		assertThat(flagTranslator.toFhirResource(openMrsFlag).getStatus(), is(fhirFlag.getStatus()));
		assertThat(flagTranslator.toOpenmrsType(fhirFlag).getStatus(), is(openMrsFlag.getStatus()));
		
		// On update
		FhirFlag existingFhirFlag = new FhirFlag();
		existingFhirFlag.setStatus(FhirFlag.FlagStatus.ACTIVE);
		Flag flagToUpdate = new Flag();
		flagToUpdate.setStatus(Flag.FlagStatus.INACTIVE);
		FhirFlag result = flagTranslator.toOpenmrsType(existingFhirFlag, flagToUpdate);
		assertThat(result.getStatus(), notNullValue());
		assertThat(result.getStatus(), is(FhirFlag.FlagStatus.INACTIVE));
	}
	
	@Test
	public void shouldTranslateAuthor() {
		User user = mock(User.class);
		Reference authorReference = mock(Reference.class);
		
		when(user.getUuid()).thenReturn("04ba682c-cab3-4f5f-adb5-0ded626ca2c0");
		when(authorReference.getReference()).thenReturn("Practitioner/04ba682c-cab3-4f5f-adb5-0ded626ca2c0");
		when(practitionerReferenceTranslator.toFhirResource(user)).thenReturn(authorReference);
		when(practitionerReferenceTranslator.toOpenmrsType(authorReference)).thenReturn(user);
		
		openMrsFlag.setCreator(user);
		fhirFlag.setAuthor(authorReference);
		
		Flag flagResult = flagTranslator.toFhirResource(openMrsFlag);
		assertThat(flagResult, notNullValue());
		assertThat(flagResult.hasAuthor(), is(true));
		assertThat(flagResult.getAuthor().getReference(), is(fhirFlag.getAuthor().getReference()));
		
		// Translating to openMrsType
		FhirFlag openMrsFlagResult = flagTranslator.toOpenmrsType(fhirFlag);
		assertThat(openMrsFlagResult.getCreator(), notNullValue());
		assertThat(openMrsFlagResult.getCreator().getUuid(), is(openMrsFlag.getCreator().getUuid()));
		
		assertThat(flagTranslator.toOpenmrsType(new FhirFlag(), fhirFlag).getCreator().getUuid(),
		    is(openMrsFlag.getCreator().getUuid()));
	}
	
	@Test
	public void shouldTranslateSubject() {
		Patient patient = mock(Patient.class);
		Reference fhirSubjectReference = mock(Reference.class);
		//FhirReference subjectReference = mock(FhirReference.class);
		
		when(patient.getPatientId()).thenReturn(13434);
		when(fhirSubjectReference.getReference()).thenReturn("Patient/a7f26bf4-060b-410a-9803-8bc24aea2146");
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(fhirSubjectReference);
		when(patientReferenceTranslator.toOpenmrsType(fhirSubjectReference)).thenReturn(patient);
		
		openMrsFlag.setPatient(patient);
		fhirFlag.setSubject(fhirSubjectReference);
		
		FhirFlag openMrsFlagResult = flagTranslator.toOpenmrsType(fhirFlag);
		assertThat(openMrsFlagResult, notNullValue());
		assertThat(openMrsFlagResult.getPatient(), notNullValue());
		assertThat(openMrsFlagResult.getPatient().getPatientId(), is(13434));
		
		Flag fhirFlagResult = flagTranslator.toFhirResource(openMrsFlag);
		assertThat(fhirFlagResult, notNullValue());
		assertThat(fhirFlagResult.getSubject(), notNullValue());
		assertThat(fhirFlagResult.getSubject().getReference(), is("Patient/a7f26bf4-060b-410a-9803-8bc24aea2146"));
	}
}
