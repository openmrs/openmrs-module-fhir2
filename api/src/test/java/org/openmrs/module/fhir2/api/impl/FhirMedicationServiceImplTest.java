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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.util.ArrayList;
import java.util.Collection;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
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
	
	private static final String WRONG_MEDICATION_UUID = "c0938432-1691-11df-97aa-7038c432aaba";
	
	private static final String CODE = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
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
		fhirMedicationService.setTranslator(medicationTranslator);
		fhirMedicationService.setDao(medicationDao);
		
		medication = new Medication();
		medication.setId(MEDICATION_UUID);
		
		drug = new Drug();
		drug.setUuid(MEDICATION_UUID);
	}
	
	@Test
	public void getMedicationByUuid_shouldGetMedicationByUuid() {
		when(medicationDao.get(MEDICATION_UUID)).thenReturn(drug);
		when(medicationTranslator.toFhirResource(drug)).thenReturn(medication);
		
		Medication medication = fhirMedicationService.get(MEDICATION_UUID);
		assertThat(medication, notNullValue());
		assertThat(medication.getId(), notNullValue());
		assertThat(medication.getId(), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void getMedicationByUuid_shouldReturnNullWhenCalledWithUnknownUuid() {
		Medication medication = fhirMedicationService.get(WRONG_MEDICATION_UUID);
		assertThat(medication, nullValue());
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByCode() {
		Collection<Drug> medications = new ArrayList<>();
		medications.add(drug);
		
		TokenAndListParam code = new TokenAndListParam();
		code.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODE)));
		
		when(medicationDao.searchForMedications(argThat(equalTo(code)), isNull(), isNull(), isNull()))
		        .thenReturn(medications);
		Collection<Medication> result = fhirMedicationService.searchForMedications(code, null, null, null);
		assertThat(result.isEmpty(), equalTo(false));
		assertThat(result.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByDosageForm() {
		Collection<Drug> medications = new ArrayList<>();
		medications.add(drug);
		
		TokenAndListParam dosageForm = new TokenAndListParam();
		dosageForm.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODE)));
		
		when(medicationDao.searchForMedications(isNull(), argThat(equalTo(dosageForm)), isNull(), isNull()))
		        .thenReturn(medications);
		Collection<Medication> result = fhirMedicationService.searchForMedications(null, dosageForm, null, null);
		assertThat(result.isEmpty(), equalTo(false));
		assertThat(result.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByIngredientCode() {
		Collection<Drug> medications = new ArrayList<>();
		medications.add(drug);
		
		TokenAndListParam ingredientCode = new TokenAndListParam();
		ingredientCode.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODE)));
		
		when(medicationDao.searchForMedications(isNull(), isNull(), argThat(equalTo(ingredientCode)), isNull()))
		        .thenReturn(medications);
		Collection<Medication> result = fhirMedicationService.searchForMedications(null, null, ingredientCode, null);
		assertThat(result.isEmpty(), equalTo(false));
		assertThat(result.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByStatus() {
		Collection<Drug> medications = new ArrayList<>();
		medications.add(drug);
		
		TokenAndListParam status = new TokenAndListParam();
		status.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue("inactive")));
		
		when(medicationDao.searchForMedications(isNull(), isNull(), isNull(), argThat(equalTo(status))))
		        .thenReturn(medications);
		Collection<Medication> result = fhirMedicationService.searchForMedications(null, null, null, status);
		assertThat(result.isEmpty(), equalTo(false));
		assertThat(result.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void saveMedication_shouldSaveNewMedication() {
		Drug drug = new Drug();
		drug.setUuid(MEDICATION_UUID);
		
		Medication medication = new Medication();
		medication.setId(MEDICATION_UUID);
		
		when(medicationTranslator.toFhirResource(drug)).thenReturn(medication);
		when(medicationTranslator.toOpenmrsType(medication)).thenReturn(drug);
		when(medicationDao.createOrUpdate(drug)).thenReturn(drug);
		
		Medication result = fhirMedicationService.create(medication);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(MEDICATION_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateMedication_shouldThrowInvalidRequestExceptionIfIdIsNull() {
		Medication medication = new Medication();
		medication.setId(MEDICATION_UUID);
		
		fhirMedicationService.update(null, medication);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateMedication_shouldThrowInvalidRequestException() {
		Medication medication = new Medication();
		medication.setId(MEDICATION_UUID);
		
		fhirMedicationService.update(WRONG_MEDICATION_UUID, medication);
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void updateMedication_shouldThrowMethodNotAllowedException() {
		Medication medication = new Medication();
		medication.setId(MEDICATION_UUID);
		
		fhirMedicationService.update(MEDICATION_UUID, medication);
	}
	
	@Test
	public void updateMedication_shouldUpdateMedication() {
		Drug drug = new Drug();
		drug.setUuid(MEDICATION_UUID);
		
		Medication medication = new Medication();
		medication.setId(MEDICATION_UUID);
		medication.setStatus(Medication.MedicationStatus.INACTIVE);
		
		when(medicationDao.get(MEDICATION_UUID)).thenReturn(drug);
		when(medicationTranslator.toFhirResource(drug)).thenReturn(medication);
		when(medicationTranslator.toOpenmrsType(drug, medication)).thenReturn(drug);
		when(medicationDao.createOrUpdate(drug)).thenReturn(drug);
		
		Medication result = fhirMedicationService.update(MEDICATION_UUID, medication);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(Medication.MedicationStatus.INACTIVE));
	}
	
	@Test
	public void deleteMedication_shouldDeleteMedication() {
		medication.setStatus(Medication.MedicationStatus.INACTIVE);
		when(medicationDao.delete(MEDICATION_UUID)).thenReturn(drug);
		when(medicationTranslator.toFhirResource(drug)).thenReturn(medication);
		
		Medication medication = fhirMedicationService.delete(MEDICATION_UUID);
		assertThat(medication, notNullValue());
		assertThat(medication.getId(), equalTo(MEDICATION_UUID));
		assertThat(medication.getStatus(), equalTo(Medication.MedicationStatus.INACTIVE));
	}
}
