/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.util;

import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.FhirConstants;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class FhirReferenceUtilsTest {
	
	private static final String GIVEN_NAME = "Ricky";
	
	private static final String FAMILY_NAME = "Morty";
	
	private static final String IDENTIFIER = "34ty5jsd-u";
	
	private static final String PATIENT_UUID = "123456-abc5def-123456";

	private static final String PATIENT_URI = FhirConstants.PATIENT +"/" + PATIENT_UUID;

	private static final String NAME_DISPLAY = "Ricky Morty(identifier:34ty5jsd-u)";

	private Patient patient;

	@Before
	public void setUp() {
		patient = new Patient();
		patient.setUuid(PATIENT_UUID);

		PersonName name = new PersonName();
		name.setFamilyName(FAMILY_NAME);
		name.setGivenName(GIVEN_NAME);

		PatientIdentifier identifier = new PatientIdentifier();
		identifier.setIdentifier(IDENTIFIER);
		patient.addName(name);
		patient.addIdentifier(identifier);
	}
	
	@Test
	public void shouldReturnPatientReference() {
		Reference reference = FhirReferenceUtils.addPatientReference(patient);
		assertThat(reference, notNullValue());
		assertThat(reference.getReference(), notNullValue());
		assertThat(reference.getReference(), equalTo(PATIENT_URI));
	}
	
	@Test
	public void shouldReturnReferenceWithCorrectDisplayName() {
		Reference reference = FhirReferenceUtils.addPatientReference(patient);
		assertThat(reference, notNullValue());
		assertThat(reference.getDisplay(), notNullValue());
		assertThat(reference.getDisplay(), equalTo(NAME_DISPLAY));
	}
	
	@Test
	public void shouldExtractUuidFromUri() {
		String uuid = FhirReferenceUtils.extractUuid(PATIENT_URI);
		assertThat(uuid, notNullValue());
		assertThat(uuid, equalTo(PATIENT_UUID));
	}
	
	@Test
	public void shouldNotExtractUuidFromNullUri() {
		assertThat(FhirReferenceUtils.extractUuid(null), nullValue());
	}
	
}
