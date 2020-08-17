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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Drug;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDao;

@RunWith(MockitoJUnitRunner.class)
public class MedicationReferenceTranslatorImplTest {
	
	private static final String MEDICATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	@Mock
	private FhirMedicationDao dao;
	
	private MedicationReferenceTranslatorImpl medicationReferenceTranslator;
	
	@Before
	public void setup() {
		medicationReferenceTranslator = new MedicationReferenceTranslatorImpl();
		medicationReferenceTranslator.setMedicationDao(dao);
	}
	
	@Test
	public void toFhirResource_shouldConvertDrugToReference() {
		Drug drug = new Drug();
		drug.setUuid(MEDICATION_UUID);
		
		Reference result = medicationReferenceTranslator.toFhirResource(drug);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(FhirConstants.MEDICATION));
		assertThat(medicationReferenceTranslator.getReferenceId(result).orElse(null), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfMedicationNull() {
		Reference result = medicationReferenceTranslator.toFhirResource(null);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldConvertReferenceToDrug() {
		Reference medicationReference = new Reference().setReference(FhirConstants.MEDICATION + "/" + MEDICATION_UUID)
		        .setType(FhirConstants.MEDICATION);
		Drug drug = new Drug();
		drug.setUuid(MEDICATION_UUID);
		when(dao.get(MEDICATION_UUID)).thenReturn(drug);
		
		Drug result = medicationReferenceTranslator.toOpenmrsType(medicationReference);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfReferenceNull() {
		Drug result = medicationReferenceTranslator.toOpenmrsType(null);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfReferenceIdIsNull() {
		Reference medicationReference = new Reference().setReference(FhirConstants.MEDICATION + "/" + MEDICATION_UUID)
		        .setType(FhirConstants.MEDICATION);
		assertThat(medicationReference.getId(), nullValue());
		Drug result = medicationReferenceTranslator.toOpenmrsType(medicationReference);
		
		assertThat(result, nullValue());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void toOpenmrsType_shouldThrowExceptionIfReferenceIsntForMedication() {
		Reference reference = new Reference().setReference("Unknown" + "/" + MEDICATION_UUID).setType("Unknown");
		
		medicationReferenceTranslator.toOpenmrsType(reference);
	}
	
}
