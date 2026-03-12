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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationDispense;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirMedicationRequestService;
import org.openmrs.module.fhir2.api.search.param.MedicationRequestSearchParams;

@RunWith(MockitoJUnitRunner.class)
public class MedicationRequestFhirResourceProviderTest {
	
	private static final String MEDICATION_REQUEST_UUID = "d7f5a4dd-019e-4221-85fa-e084505b9695";
	
	private static final String WRONG_MEDICATION_REQUEST_UUID = "862e20a1-e73c-4c92-a4e8-9f922e0cd7f4";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	@Mock
	private FhirMedicationRequestService fhirMedicationRequestService;
	
	private MedicationRequestFhirResourceProvider resourceProvider;
	
	private org.hl7.fhir.r4.model.MedicationRequest medicationRequest;
	
	@Before
	public void setup() {
		resourceProvider = new MedicationRequestFhirResourceProvider();
		resourceProvider.setMedicationRequestService(fhirMedicationRequestService);
		
		medicationRequest = new org.hl7.fhir.r4.model.MedicationRequest();
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(MedicationRequest.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(MedicationRequest.class.getName()));
	}
	
	@Test
	public void getMedicationRequestByUuid_shouldReturnMatchingMedicationRequest() {
		when(fhirMedicationRequestService.get(MEDICATION_REQUEST_UUID)).thenReturn(medicationRequest);
		
		IdType id = new IdType();
		id.setValue(MEDICATION_REQUEST_UUID);
		MedicationRequest medicationRequest = resourceProvider.getMedicationRequestById(id);
		assertThat(medicationRequest, notNullValue());
		assertThat(medicationRequest.getId(), notNullValue());
		assertThat(medicationRequest.getId(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getMedicationRequestByUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_MEDICATION_REQUEST_UUID);
		MedicationRequest medicationRequest = resourceProvider.getMedicationRequestById(id);
		assertThat(medicationRequest, nullValue());
	}
	
	private List<MedicationRequest> get(IBundleProvider results, int theFromIndex, int theToIndex) {
		return results.getResources(theFromIndex, theToIndex).stream().filter(it -> it instanceof MedicationRequest)
		        .map(it -> (MedicationRequest) it).collect(Collectors.toList());
	}
	
	@Test
	public void searchMedicationRequest_shouldReturnMatchingMedicationRequestUsingCode() {
		
		when(fhirMedicationRequestService.searchForMedicationRequests(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(medicationRequest), 10, 1));
		
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setValue("1000");
		code.addAnd(codingToken);
		
		IBundleProvider results = resourceProvider.searchForMedicationRequests(null, null, null, code, null, null, null,
		    null, null, null, null, null);
		
		List<MedicationRequest> resources = get(results, 1, 5);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.MEDICATION_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void searchMedicationRequest_shouldReturnMatchingMedicationRequestWhenPatientParamIsSpecified() {
		
		when(fhirMedicationRequestService.searchForMedicationRequests(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(medicationRequest), 10, 1));
		
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		patientParam.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Patient.SP_NAME)));
		
		IBundleProvider results = resourceProvider.searchForMedicationRequests(patientParam, null, null, null, null, null,
		    null, null, null, null, null, null);
		
		List<MedicationRequest> resources = get(results, 1, 5);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.MEDICATION_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void searchMedicationRequest_shouldReturnMatchingMedicationRequestWhenMedicationParamIsSpecified() {
		
		when(fhirMedicationRequestService.searchForMedicationRequests(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(medicationRequest), 10, 1));
		
		ReferenceAndListParam medicationParam = new ReferenceAndListParam();
		medicationParam.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Medication.SP_RES_ID)));
		
		IBundleProvider results = resourceProvider.searchForMedicationRequests(null, null, null, null, null, medicationParam,
		    null, null, null, null, null, null);
		
		List<MedicationRequest> resources = get(results, 1, 5);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.MEDICATION_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void searchMedicationRequest_shouldReturnMatchingMedicationRequestWhenParticipantParamIsSpecified() {
		
		when(fhirMedicationRequestService.searchForMedicationRequests(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(medicationRequest), 10, 1));
		
		ReferenceAndListParam participantParam = new ReferenceAndListParam();
		participantParam.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Practitioner.SP_NAME)));
		
		IBundleProvider results = resourceProvider.searchForMedicationRequests(null, null, null, null, participantParam,
		    null, null, null, null, null, null, null);
		
		List<MedicationRequest> resources = get(results, 1, 5);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.MEDICATION_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void searchMedicationRequest_shouldReturnMatchingMedicationRequestWhenEncounterParamIsSpecified() {
		
		when(fhirMedicationRequestService.searchForMedicationRequests(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(medicationRequest), 10, 1));
		
		ReferenceAndListParam encounterParam = new ReferenceAndListParam();
		encounterParam.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Encounter.SP_IDENTIFIER)));
		
		IBundleProvider results = resourceProvider.searchForMedicationRequests(null, null, encounterParam, null, null, null,
		    null, null, null, null, null, null);
		
		List<MedicationRequest> resources = get(results, 1, 5);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.MEDICATION_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void searchMedicationRequest_shouldReturnMatchingMedicationRequestWhenUUIDIsSpecified() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_REQUEST_UUID));
		
		when(fhirMedicationRequestService.searchForMedicationRequests(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(medicationRequest), 10, 1));
		
		IBundleProvider results = resourceProvider.searchForMedicationRequests(null, null, null, null, null, null, uuid,
		    null, null, null, null, null);
		
		List<MedicationRequest> resources = get(results, 1, 5);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.MEDICATION_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void searchMedicationRequest_shouldReturnMatchingMedicationRequestWhenLastUpdatedIsSpecified() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		when(fhirMedicationRequestService.searchForMedicationRequests(
		    new MedicationRequestSearchParams(null, null, null, null, null, null, null, null, lastUpdated, null, null)))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(medicationRequest), 10, 1));
		
		IBundleProvider results = resourceProvider.searchForMedicationRequests(null, null, null, null, null, null, null,
		    null, null, lastUpdated, null, null);
		
		List<IBaseResource> resources = results.getResources(1, 5);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.MEDICATION_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void searchMedicationRequest_shouldAddRelatedMedicationsWhenIncluded() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("MedicationRequest:requester"));
		
		when(fhirMedicationRequestService.searchForMedicationRequests(
		    new MedicationRequestSearchParams(null, null, null, null, null, null, null, null, null, includes, null)))
		            .thenReturn(new MockIBundleProvider<>(
		                    Arrays.asList(medicationRequest, new org.hl7.fhir.r4.model.Practitioner()), 10, 1));
		
		IBundleProvider results = resourceProvider.searchForMedicationRequests(null, null, null, null, null, null, null,
		    null, null, null, includes, null);
		
		List<IBaseResource> resources = results.getResources(1, 5);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(2)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.MEDICATION_REQUEST));
		assertThat(resources.get(1).fhirType(), equalTo(FhirConstants.PRACTITIONER));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void searchMedicationRequest_shouldAddRelatedMedicationDispenseWhenRevIncluded() {
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("MedicationDispense:prescription"));
		
		when(fhirMedicationRequestService.searchForMedicationRequests(
		    new MedicationRequestSearchParams(null, null, null, null, null, null, null, null, null, null, revIncludes)))
		            .thenReturn(new org.openmrs.module.fhir2.providers.r4.MockIBundleProvider<>(
		                    Arrays.asList(medicationRequest, new MedicationDispense()), 10, 1));
		
		IBundleProvider results = resourceProvider.searchForMedicationRequests(null, null, null, null, null, null, null,
		    null, null, null, null, revIncludes);
		
		List<IBaseResource> resources = results.getResources(1, 5);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(2)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.MEDICATION_REQUEST));
		assertThat(resources.get(1).fhirType(), equalTo(FhirConstants.MEDICATION_DISPENSE));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void searchMedicationRequest_shouldNotAddRelatedMedicationsForEmptyInclude() {
		HashSet<Include> includes = new HashSet<>();
		
		when(fhirMedicationRequestService.searchForMedicationRequests(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(medicationRequest), 10, 1));
		
		IBundleProvider results = resourceProvider.searchForMedicationRequests(null, null, null, null, null, null, null,
		    null, null, null, includes, null);
		
		List<MedicationRequest> resources = get(results, 1, 5);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.MEDICATION_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void deleteMedicationRequest_shouldDeleteMedicationRequest() {
		OperationOutcome result = resourceProvider.deleteMedicationRequest(new IdType().setValue(MEDICATION_REQUEST_UUID));
		
		assertThat(result, notNullValue());
		assertThat(result.getIssue(), notNullValue());
		assertThat(result.getIssueFirstRep().getSeverity(), equalTo(OperationOutcome.IssueSeverity.INFORMATION));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getCode(), equalTo("MSG_DELETED"));
	}
}
