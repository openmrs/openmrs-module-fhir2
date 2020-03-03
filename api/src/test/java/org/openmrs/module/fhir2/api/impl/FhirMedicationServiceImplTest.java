/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.Medication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Drug;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDao;
import org.openmrs.module.fhir2.api.translators.MedicationTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirMedicationServiceImplTest {
	
	private static final String MEDICATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String WRONG_MEDICATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	@Mock
	private MedicationTranslator medicationTranslator;
	
	@Mock
	private FhirMedicationDao medicationDao;
	
	private FhirMedicationServiceImpl fhirMedicationService;
	
	private Medication medication;
	
	private Drug drug;
	
	@Before
	public void setup() {
		fhirMedicationService = new FhirMedicationServiceImpl();
		fhirMedicationService.setMedicationTranslator(medicationTranslator);
		fhirMedicationService.setMedicationDao(medicationDao);
		
		medication = new Medication();
		medication.setId(MEDICATION_UUID);
		
		drug = new Drug();
		drug.setUuid(MEDICATION_UUID);
	}
	
	@Test
	public void getMedicationByUuid_shouldGetMedicationByUuid() {
		when(medicationDao.getMedicationByUuid(MEDICATION_UUID)).thenReturn(drug);
		when(medicationTranslator.toFhirResource(drug)).thenReturn(medication);
		
		Medication medication = fhirMedicationService.getMedicationByUuid(MEDICATION_UUID);
		assertThat(medication, notNullValue());
		assertThat(medication.getId(), notNullValue());
		assertThat(medication.getId(), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void getMedicationByUuid_shouldReturnNullWhenCalledWithUnknownUuid() {
		Medication medication = fhirMedicationService.getMedicationByUuid(WRONG_MEDICATION_UUID);
		assertThat(medication, nullValue());
	}
}
