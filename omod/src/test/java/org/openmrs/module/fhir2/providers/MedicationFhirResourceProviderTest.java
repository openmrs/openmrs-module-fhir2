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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Medication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirMedicationService;

@RunWith(MockitoJUnitRunner.class)
public class MedicationFhirResourceProviderTest {
	
	private static final String MEDICATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String WRONG_MEDICATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	@Mock
	private FhirMedicationService fhirMedicationService;
	
	private MedicationFhirResourceProvider resourceProvider;
	
	private Medication medication;
	
	@Before
	public void setup() {
		resourceProvider = new MedicationFhirResourceProvider();
		resourceProvider.setFhirMedicationService(fhirMedicationService);
		
		medication = new Medication();
		medication.setId(MEDICATION_UUID);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Medication.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Medication.class.getName()));
	}
	
	@Test
	public void getMedicationByUuid_shouldReturnMatchingMedication() {
		when(fhirMedicationService.getMedicationByUuid(MEDICATION_UUID)).thenReturn(medication);
		
		IdType id = new IdType();
		id.setValue(MEDICATION_UUID);
		Medication medication = resourceProvider.getMedicationByUuid(id);
		assertThat(medication, notNullValue());
		assertThat(medication.getId(), notNullValue());
		assertThat(medication.getId(), equalTo(MEDICATION_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getMedicationByUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_MEDICATION_UUID);
		Medication medication = resourceProvider.getMedicationByUuid(id);
		assertThat(medication, nullValue());
	}
}
