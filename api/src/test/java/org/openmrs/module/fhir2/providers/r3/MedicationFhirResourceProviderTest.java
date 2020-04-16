/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.util.Collections;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hamcrest.CoreMatchers;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.OperationOutcome;
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
	
	private static final String CODE = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Mock
	private FhirMedicationService fhirMedicationService;
	
	private MedicationFhirResourceProvider resourceProvider;
	
	private org.hl7.fhir.r4.model.Medication medication;
	
	@Before
	public void setup() {
		resourceProvider = new MedicationFhirResourceProvider();
		resourceProvider.setMedicationService(fhirMedicationService);
		
		medication = new org.hl7.fhir.r4.model.Medication();
		medication.setId(MEDICATION_UUID);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Medication.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Medication.class.getName()));
	}
	
	@Test
	public void getMedicationByUuid_shouldReturnMatchingMedication() {
		when(fhirMedicationService.get(MEDICATION_UUID)).thenReturn(medication);
		
		IdType id = new IdType();
		id.setValue(MEDICATION_UUID);
		Medication medication = resourceProvider.getMedicationById(id);
		assertThat(medication, notNullValue());
		assertThat(medication.getId(), notNullValue());
		assertThat(medication.getId(), equalTo(MEDICATION_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getMedicationByUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_MEDICATION_UUID);
		Medication medication = resourceProvider.getMedicationById(id);
		assertThat(medication, nullValue());
	}
	
	@Test
	public void searchForMedication_shouldReturnMatchingBundleOfMedicationByCode() {
		TokenAndListParam code = new TokenAndListParam();
		code.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODE)));
		
		when(fhirMedicationService.searchForMedications(argThat(is(code)), isNull(), isNull(), isNull()))
		        .thenReturn(Collections.singletonList(medication));
		
		Bundle results = resourceProvider.searchForMedication(code, null, null);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForMedication_shouldReturnMatchingBundleOfMedicationByDosageForm() {
		TokenAndListParam dosageFormCode = new TokenAndListParam();
		dosageFormCode.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODE)));
		
		when(fhirMedicationService.searchForMedications(isNull(), argThat(is(dosageFormCode)), isNull(), isNull()))
		        .thenReturn(Collections.singletonList(medication));
		
		Bundle results = resourceProvider.searchForMedication(null, dosageFormCode, null);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForMedication_shouldReturnMatchingBundleOfMedicationByStatus() {
		TokenAndListParam status = new TokenAndListParam();
		status.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue("active")));
		
		when(fhirMedicationService.searchForMedications(isNull(), isNull(), isNull(), argThat(is(status))))
		        .thenReturn(Collections.singletonList(medication));
		
		Bundle results = resourceProvider.searchForMedication(null, null, status);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	//	@Test
	//	public void shouldCreateNewMedication() {
	//		when(fhirMedicationService.create(medication)).thenReturn(medication);
	//
	//		MethodOutcome result = resourceProvider.createMedication(VersionConvertor_30_40.convertMedication(medication));
	//		assertThat(result, CoreMatchers.notNullValue());
	//		assertThat(result.getCreated(), is(true));
	//		assertThat(result.getResource(), CoreMatchers.equalTo(VersionConvertor_30_40.convertMedication(medication)));
	//	}
	//
	//	@Test
	//	public void shouldUpdateMedication() {
	//		org.hl7.fhir.r4.model.Medication med = medication;
	//		med.setStatus(org.hl7.fhir.r4.model.Medication.MedicationStatus.INACTIVE);
	//
	//		when(fhirMedicationService.update(MEDICATION_UUID, medication)).thenReturn(med);
	//
	//		MethodOutcome result = resourceProvider.updateMedication(new IdType().setValue(MEDICATION_UUID), VersionConvertor_30_40.convertMedication(medication));
	//		assertThat(result, CoreMatchers.notNullValue());
	//		assertThat(result.getResource(), CoreMatchers.equalTo(med));
	//	}
	//
	//	@Test(expected = InvalidRequestException.class)
	//	public void updateMedicationShouldThrowInvalidRequestForUuidMismatch() {
	//		when(fhirMedicationService.update(WRONG_MEDICATION_UUID, medication)).thenThrow(InvalidRequestException.class);
	//
	//		resourceProvider.updateMedication(new IdType().setValue(WRONG_MEDICATION_UUID), VersionConvertor_30_40.convertMedication(medication));
	//	}
	//
	//	@Test(expected = MethodNotAllowedException.class)
	//	public void updateMedicationShouldThrowMethodNotAllowedIfDoesNotExist() {
	//		org.hl7.fhir.r4.model.Medication wrongMedication = new org.hl7.fhir.r4.model.Medication();
	//
	//		wrongMedication.setId(WRONG_MEDICATION_UUID);
	//
	//		when(fhirMedicationService.update(WRONG_MEDICATION_UUID, wrongMedication))
	//		        .thenThrow(MethodNotAllowedException.class);
	//
	//		resourceProvider.updateMedication(new IdType().setValue(WRONG_MEDICATION_UUID), VersionConvertor_30_40.convertMedication(wrongMedication));
	//	}
	
	@Test
	public void shouldDeleteMedication() {
		org.hl7.fhir.r4.model.Medication med = medication;
		med.setStatus(org.hl7.fhir.r4.model.Medication.MedicationStatus.INACTIVE);
		
		when(fhirMedicationService.delete(MEDICATION_UUID)).thenReturn(med);
		
		OperationOutcome result = resourceProvider.deleteMedication(new IdType().setValue(MEDICATION_UUID));
		assertThat(result, CoreMatchers.notNullValue());
		assertThat(result.getId(), equalTo(MEDICATION_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void deleteMedicationShouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_MEDICATION_UUID);
		OperationOutcome medication = resourceProvider.deleteMedication(id);
		assertThat(medication, nullValue());
	}
}
