/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirPatientService;

@RunWith(MockitoJUnitRunner.class)
public class PatientFhirResourceProviderTest {

	private static final String PATIENT_UUID = "017312a1-cf56-43ab-ae87-44070b801d1c";

	private static final String WRONG_PATIENT_UUID = "017312a1-cf56-43ab-ae87-44070b801d1c";

	private static final String NAME = "Rick";

	private static final String WRONG_NAME = "Wrong name";

	private static final String GIVEN_NAME = "Jeanne";

	private static final String WRONG_GIVEN_NAME = "wrong name";

	private static final String FAMILY_NAME = "Jennifer";

	private static final String WRONG_FAMILY_NAME = "wrong name";

	@Mock
	private FhirPatientService patientService;

	private PatientFhirResourceProvider resourceProvider;

	private Patient patient;

	@Before
	public void setup() {
		resourceProvider = new PatientFhirResourceProvider();
		resourceProvider.setPatientService(patientService);
	}

	@Before
	public void initPatient() {
		HumanName name = new HumanName();
		name.addGiven(GIVEN_NAME);
		name.setFamily(FAMILY_NAME);

		patient = new Patient();
		patient.setId(PATIENT_UUID);
		patient.addName(name);
		patient.setActive(true);
		patient.setBirthDate(new Date());
		patient.setGender(Enumerations.AdministrativeGender.MALE);
	}

	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Patient.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Patient.class.getName()));
	}

	@Test
	public void getPatientById_shouldReturnPatient() {
		IdType id = new IdType();
		id.setValue(PATIENT_UUID);
		when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);

		Patient result = resourceProvider.getPatientById(id);
		assertThat(result.isResource(), is(true));
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(PATIENT_UUID));
	}

	@Test(expected = ResourceNotFoundException.class)
	public void getPatientByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_PATIENT_UUID);
		assertThat(resourceProvider.getPatientById(idType).isResource(), is(true));
		assertThat(resourceProvider.getPatientById(idType), nullValue());
	}

	@Test
	public void findPatientsByName_shouldReturnMatchingBundleOfPatients() {
		when(patientService.findPatientsByName(NAME)).thenReturn(Collections.singletonList(patient));

		Bundle results = resourceProvider.findPatientsByName(NAME);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}

	@Test
	public void findPatientsByWrongName_shouldReturnBundleWithEmptyEntries() {
		Bundle results = resourceProvider.findPatientsByName(WRONG_NAME);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry(), is(empty()));
	}

	@Test
	public void findPatientsByGivenName_shouldReturnMatchingBundleOfPatients() {
		when(patientService.findPatientsByGivenName(GIVEN_NAME)).thenReturn(Collections.singletonList(patient));
		Bundle results = resourceProvider.findPatientsByGivenName(GIVEN_NAME);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}

	@Test
	public void findPatientsByWrongGivenName_shouldReturnBundleWithEmptyEntries() {
		Bundle results = resourceProvider.findPatientsByGivenName(WRONG_GIVEN_NAME);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry(), is(empty()));
	}

	@Test
	public void findPatientsByFamilyName_shouldReturnMatchingBundleOfPatients() {
		when(patientService.findPatientsByFamilyName(FAMILY_NAME)).thenReturn(Collections.singletonList(patient));
		Bundle results = resourceProvider.findPatientsByFamilyName(FAMILY_NAME);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}

	@Test
	public void findPatientsByWrongFamilyName_shouldReturnBundleWithEmptyEntries() {
		Bundle results = resourceProvider.findPatientsByFamilyName(WRONG_FAMILY_NAME);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry(), is(empty()));
	}
}
