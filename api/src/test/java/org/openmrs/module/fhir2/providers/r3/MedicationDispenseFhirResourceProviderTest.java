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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.convertors.conv30_40.resources30_40.MedicationDispense30_40;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationDispense;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirMedicationDispenseService;

@RunWith(MockitoJUnitRunner.class)
public class MedicationDispenseFhirResourceProviderTest {
	
	private static final String MEDICATION_DISPENSE_UUID = "d7f5a4dd-019e-4221-85fa-e084505b9695";
	
	private static final String WRONG_MEDICATION_DISPENSE_UUID = "862e20a1-e73c-4c92-a4e8-9f922e0cd7f4";
	
	@Mock
	private FhirMedicationDispenseService fhirMedicationDispenseService;
	
	private MedicationDispenseFhirResourceProvider resourceProvider;
	
	private org.hl7.fhir.r4.model.MedicationDispense medicationDispense;
	
	private TokenAndListParam idParam;
	
	private ReferenceAndListParam patientParam;
	
	private ReferenceAndListParam subjectParam;
	
	private ReferenceAndListParam encounterParam;
	
	private ReferenceAndListParam medicationRequestParam;
	
	private DateRangeParam lastUpdatedParam;
	
	private HashSet<Include> includeParam;
	
	private SortSpec sortParam;
	
	@Before
	public void setup() {
		resourceProvider = new MedicationDispenseFhirResourceProvider();
		resourceProvider.setFhirMedicationDispenseService(fhirMedicationDispenseService);
		
		medicationDispense = new org.hl7.fhir.r4.model.MedicationDispense();
		medicationDispense.setId(MEDICATION_DISPENSE_UUID);
		
		when(fhirMedicationDispenseService.get(MEDICATION_DISPENSE_UUID)).thenReturn(medicationDispense);
		
		when(fhirMedicationDispenseService.searchMedicationDispenses(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(medicationDispense), 10, 1));
		
		idParam = null;
		patientParam = null;
		subjectParam = null;
		encounterParam = null;
		medicationRequestParam = null;
		lastUpdatedParam = null;
		includeParam = new HashSet<>();
		sortParam = null;
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(MedicationDispense.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(MedicationDispense.class.getName()));
	}
	
	@Test
	public void getMedicationDispenseByUuid_shouldReturnMatchingMedicationDispense() {
		IdType id = new IdType();
		id.setValue(MEDICATION_DISPENSE_UUID);
		MedicationDispense medicationDispense = resourceProvider.getMedicationDispenseByUuid(id);
		assertThat(medicationDispense, notNullValue());
		assertThat(medicationDispense.getId(), notNullValue());
		assertThat(medicationDispense.getId(), equalTo(MEDICATION_DISPENSE_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getMedicationDispenseByUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_MEDICATION_DISPENSE_UUID);
		resourceProvider.getMedicationDispenseByUuid(id);
	}
	
	@Test
	public void createMedicationDispense_shouldCreateMedicationDispense() {
		when(fhirMedicationDispenseService.create(any(org.hl7.fhir.r4.model.MedicationDispense.class)))
		        .thenReturn(medicationDispense);
		MethodOutcome result = resourceProvider
		        .createMedicationDispense(MedicationDispense30_40.convertMedicationDispense(medicationDispense));
		assertThat(result, notNullValue());
		assertThat(result.getCreated(), is(true));
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(medicationDispense.getId()));
	}
	
	@Test
	public void updateMedicationDispense_shouldUpdateMedicationDispense() {
		when(fhirMedicationDispenseService.update(eq(MEDICATION_DISPENSE_UUID),
		    any(org.hl7.fhir.r4.model.MedicationDispense.class))).thenReturn(medicationDispense);
		
		MethodOutcome result = resourceProvider.updateMedicationDispense(new IdType().setValue(MEDICATION_DISPENSE_UUID),
		    MedicationDispense30_40.convertMedicationDispense(medicationDispense));
		
		assertThat(result, notNullValue());
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(medicationDispense.getId()));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateMedicationDispense_shouldThrowInvalidRequestForUuidMismatch() {
		when(fhirMedicationDispenseService.update(eq(WRONG_MEDICATION_DISPENSE_UUID),
		    any(org.hl7.fhir.r4.model.MedicationDispense.class))).thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateMedicationDispense(new IdType().setValue(WRONG_MEDICATION_DISPENSE_UUID),
		    MedicationDispense30_40.convertMedicationDispense(medicationDispense));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateMedicationDispense_shouldThrowInvalidRequestForMissingId() {
		MedicationDispense noIdMedicationDispense = new MedicationDispense();
		
		when(fhirMedicationDispenseService.update(eq(MEDICATION_DISPENSE_UUID),
		    any(org.hl7.fhir.r4.model.MedicationDispense.class))).thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateMedicationDispense(new IdType().setValue(MEDICATION_DISPENSE_UUID), noIdMedicationDispense);
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void updateMedicationShouldThrowMethodNotAllowedIfDoesNotExist() {
		MedicationDispense wrongMedicationDispense = new MedicationDispense();
		wrongMedicationDispense.setId(WRONG_MEDICATION_DISPENSE_UUID);
		
		when(fhirMedicationDispenseService.update(eq(WRONG_MEDICATION_DISPENSE_UUID),
		    any(org.hl7.fhir.r4.model.MedicationDispense.class))).thenThrow(MethodNotAllowedException.class);
		
		resourceProvider.updateMedicationDispense(new IdType().setValue(WRONG_MEDICATION_DISPENSE_UUID),
		    wrongMedicationDispense);
	}
	
	@Test
	public void deleteTask_shouldDeleteMedicationDispense() {
		OperationOutcome result = resourceProvider.deleteMedicationDispense(new IdType().setValue(MEDICATION_DISPENSE_UUID));
		
		assertThat(result, notNullValue());
		assertThat(result.getIssue(), notNullValue());
		assertThat(result.getIssueFirstRep().getSeverity(), equalTo(OperationOutcome.IssueSeverity.INFORMATION));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getCode(), equalTo("MSG_DELETED"));
	}
	
	@Test
	public void searchMedicationDispense_shouldSearchById() {
		idParam = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_DISPENSE_UUID));
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldSearchByPatientId() {
		patientParam = new ReferenceAndListParam();
		patientParam.addValue(new ReferenceOrListParam().add(new ReferenceParam("patient-reference")));
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldSearchByPatientIdentifier() {
		patientParam = new ReferenceAndListParam();
		patientParam.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Patient.SP_IDENTIFIER)));
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldSearchByPatientGivenName() {
		patientParam = new ReferenceAndListParam();
		patientParam.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Patient.SP_GIVEN)));
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldSearchByPatientFamilyName() {
		patientParam = new ReferenceAndListParam();
		patientParam.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Patient.SP_FAMILY)));
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldSearchByPatientName() {
		patientParam = new ReferenceAndListParam();
		patientParam.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Patient.SP_NAME)));
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldSearchBySubjectId() {
		subjectParam = new ReferenceAndListParam();
		subjectParam.addValue(new ReferenceOrListParam().add(new ReferenceParam("patient-reference")));
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldSearchBySubjectIdentifier() {
		subjectParam = new ReferenceAndListParam();
		subjectParam.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Patient.SP_IDENTIFIER)));
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldSearchBySubjectGivenName() {
		subjectParam = new ReferenceAndListParam();
		subjectParam.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Patient.SP_GIVEN)));
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldSearchBySubjectFamilyName() {
		subjectParam = new ReferenceAndListParam();
		subjectParam.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Patient.SP_FAMILY)));
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldSearchBySubjectName() {
		subjectParam = new ReferenceAndListParam();
		subjectParam.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Patient.SP_NAME)));
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldSearchByEncounter() {
		encounterParam = new ReferenceAndListParam();
		encounterParam.addValue(new ReferenceOrListParam().add(new ReferenceParam("encounter-reference")));
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldSearchByMedicationRequest() {
		medicationRequestParam = new ReferenceAndListParam();
		medicationRequestParam.addValue(new ReferenceOrListParam().add(new ReferenceParam("med-request-ref")));
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldSearchByLastUpdated() {
		lastUpdatedParam = new DateRangeParam();
		lastUpdatedParam.setLowerBound("2021-01-01").setUpperBound("2021-12-31");
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldHandlePatientInclude() {
		includeParam.add(new Include("MedicationDispense:patient"));
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldHandleEncounterInclude() {
		includeParam.add(new Include("MedicationDispense:context"));
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldHandleMedicationRequestInclude() {
		includeParam.add(new Include("MedicationDispense:prescription"));
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldHandlePerformerInclude() {
		includeParam.add(new Include("MedicationDispense:performer"));
		testSearch();
	}
	
	@Test
	public void searchMedicationDispense_shouldHandleSort() {
		sortParam = new SortSpec("status");
		testSearch();
	}
	
	protected void testSearch() {
		IBundleProvider results = resourceProvider.searchForMedicationDispenses(idParam, patientParam, subjectParam,
		    encounterParam, medicationRequestParam, lastUpdatedParam, includeParam, sortParam);
		List<IBaseResource> resources = getResources(results, 1, 5);
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.MEDICATION_DISPENSE));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(MEDICATION_DISPENSE_UUID));
	}
	
	private List<IBaseResource> getResources(IBundleProvider results, int theFromIndex, int theToIndex) {
		return results.getResources(theFromIndex, theToIndex);
	}
}
