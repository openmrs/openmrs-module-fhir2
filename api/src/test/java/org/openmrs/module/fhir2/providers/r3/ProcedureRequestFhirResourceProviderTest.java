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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.MethodOutcome;
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
import org.hl7.fhir.convertors.VersionConvertor_30_40;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirServiceRequestService;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

@RunWith(MockitoJUnitRunner.class)
public class ProcedureRequestFhirResourceProviderTest {
	
	private static final String SERVICE_REQUEST_UUID = "7d13b03b-58c2-43f5-b34d-08750c51aea9";
	
	private static final String WRONG_SERVICE_REQUEST_UUID = "92b04062-e57d-43aa-8c38-90a1ad70080c";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final String PATIENT_GIVEN_NAME = "Meantex";
	
	private static final String CODE = "5089";
	
	private static final String ENCOUNTER_UUID = "y403fafb-e5e4-42d0-9d11-4f52e89d123r";
	
	private static final String PARTICIPANT_IDENTIFIER = "101-6";
	
	private static final String OCCURRENCE = "2020-09-03";
	
	private static final int PREFERRED_PAGE_SIZE = 10;
	
	private static final int COUNT = 1;
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirServiceRequestService serviceRequestService;
	
	private ProcedureRequestFhirResourceProvider resourceProvider;
	
	private ServiceRequest serviceRequest;
	
	@Before
	public void setup() {
		resourceProvider = new ProcedureRequestFhirResourceProvider();
		resourceProvider.setServiceRequestService(serviceRequestService);
	}
	
	@Before
	public void initServiceRequest() {
		serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);
	}
	
	private List<IBaseResource> getResources(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(ProcedureRequest.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(ProcedureRequest.class.getName()));
	}
	
	@Test
	public void getServiceRequestById_shouldReturnServiceRequest() {
		IdType id = new IdType();
		id.setValue(SERVICE_REQUEST_UUID);
		when(serviceRequestService.get(SERVICE_REQUEST_UUID)).thenReturn(serviceRequest);
		
		ProcedureRequest result = resourceProvider.getProcedureRequestById(id);
		assertThat(result.isResource(), is(true));
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getServiceRequestByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_SERVICE_REQUEST_UUID);
		assertThat(resourceProvider.getProcedureRequestById(idType).isResource(), is(true));
		assertThat(resourceProvider.getProcedureRequestById(idType), nullValue());
	}
	
	@Test
	public void searchProcedureRequest_shouldReturnMatchingProcedureRequestByCode() {
		when(serviceRequestService.searchForServiceRequests(any(), any(), any(), any(), any(), any(), any())).thenReturn(
		    new MockIBundleProvider<>(Collections.singletonList(serviceRequest), PREFERRED_PAGE_SIZE, COUNT));
		
		TokenAndListParam code = new TokenAndListParam().addAnd(new TokenParam(CODE));
		
		IBundleProvider results = resourceProvider.searchForProcedureRequests(null, null, code, null, null, null, null,
		    null);
		
		List<IBaseResource> resources = getResources(results);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void searchProcedureRequest_shouldReturnMatchingProcedureRequestWhenPatientParamIsSpecified() {
		when(serviceRequestService.searchForServiceRequests(any(), any(), any(), any(), any(), any(), any())).thenReturn(
		    new MockIBundleProvider<>(Collections.singletonList(serviceRequest), PREFERRED_PAGE_SIZE, COUNT));
		
		ReferenceAndListParam patientParam = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(Patient.SP_NAME)));
		
		IBundleProvider results = resourceProvider.searchForProcedureRequests(patientParam, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resources = getResources(results);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void searchProcedureRequest_shouldReturnMatchingProcedureRequestWhenSubjectParamIsSpecified() {
		when(serviceRequestService.searchForServiceRequests(any(), any(), any(), any(), any(), any(), any())).thenReturn(
		    new MockIBundleProvider<>(Collections.singletonList(serviceRequest), PREFERRED_PAGE_SIZE, COUNT));
		
		ReferenceAndListParam subjectParam = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(Patient.SP_NAME)));
		
		IBundleProvider results = resourceProvider.searchForProcedureRequests(null, subjectParam, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resources = getResources(results);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void searchProcedureRequest_shouldReturnMatchingProcedureRequestByRequesterParam() {
		when(serviceRequestService.searchForServiceRequests(any(), any(), any(), any(), any(), any(), any())).thenReturn(
		    new MockIBundleProvider<>(Collections.singletonList(serviceRequest), PREFERRED_PAGE_SIZE, COUNT));
		
		ReferenceAndListParam practitionerParam = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PARTICIPANT_IDENTIFIER).setChain(Practitioner.SP_IDENTIFIER)));
		
		IBundleProvider results = resourceProvider.searchForProcedureRequests(null, null, null, null, practitionerParam,
		    null, null, null);
		
		List<IBaseResource> resources = getResources(results);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void searchProcedureRequest_shouldReturnMatchingProcedureRequestByOccurrence() {
		when(serviceRequestService.searchForServiceRequests(any(), any(), any(), any(), any(), any(), any())).thenReturn(
		    new MockIBundleProvider<>(Collections.singletonList(serviceRequest), PREFERRED_PAGE_SIZE, COUNT));
		
		DateRangeParam occurrence = new DateRangeParam().setLowerBound(OCCURRENCE).setUpperBound(OCCURRENCE);
		
		IBundleProvider results = resourceProvider.searchForProcedureRequests(null, null, null, null, null, occurrence, null,
		    null);
		
		List<IBaseResource> resources = getResources(results);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void searchProcedureRequest_shouldReturnMatchingProcedureRequestByEncounter() {
		when(serviceRequestService.searchForServiceRequests(any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(serviceRequest), 10, 1));
		
		ReferenceAndListParam encounterParam = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(ENCOUNTER_UUID).setChain(null)));
		
		IBundleProvider results = resourceProvider.searchForProcedureRequests(null, null, null, encounterParam, null, null,
		    null, null);
		
		List<IBaseResource> resources = getResources(results);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	public void createProcedureRequest_shouldCreateNewProcedureRequest() {
		when(serviceRequestService.create(any(ServiceRequest.class))).thenReturn(serviceRequest);
		
		MethodOutcome result = resourceProvider
		        .createProcedureRequest((ProcedureRequest) VersionConvertor_30_40.convertResource(serviceRequest, false));
		
		assertThat(result, notNullValue());
		assertThat(result.getCreated(), is(true));
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void updateProcedureRequest_shouldUpdateProcedureRequest() {
		when(serviceRequestService.update(eq(SERVICE_REQUEST_UUID), any(ServiceRequest.class))).thenReturn(serviceRequest);
		
		MethodOutcome result = resourceProvider.updateProcedureRequest(new IdType().setValue(SERVICE_REQUEST_UUID),
		    (ProcedureRequest) VersionConvertor_30_40.convertResource(serviceRequest, false));
		
		assertThat(result, notNullValue());
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateProcedureRequest_shouldThrowInvalidRequestForUuidMismatch() {
		when(serviceRequestService.update(eq(WRONG_SERVICE_REQUEST_UUID), any(ServiceRequest.class)))
		        .thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateProcedureRequest(new IdType().setValue(WRONG_SERVICE_REQUEST_UUID),
		    (ProcedureRequest) VersionConvertor_30_40.convertResource(serviceRequest, false));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateProcedureRequest_shouldThrowInvalidRequestForMissingId() {
		ServiceRequest noIdServiceRequest = new ServiceRequest();
		
		when(serviceRequestService.update(eq(SERVICE_REQUEST_UUID), any(ServiceRequest.class)))
		        .thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateProcedureRequest(new IdType().setValue(SERVICE_REQUEST_UUID),
		    (ProcedureRequest) VersionConvertor_30_40.convertResource(noIdServiceRequest, false));
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void updateProcedureRequest_shouldThrowMethodNotAllowedIfDoesNotExist() {
		ServiceRequest wrongServiceRequest = new ServiceRequest();
		wrongServiceRequest.setId(WRONG_SERVICE_REQUEST_UUID);
		
		when(serviceRequestService.update(eq(WRONG_SERVICE_REQUEST_UUID), any(ServiceRequest.class)))
		        .thenThrow(MethodNotAllowedException.class);
		
		resourceProvider.updateProcedureRequest(new IdType().setValue(WRONG_SERVICE_REQUEST_UUID),
		    (ProcedureRequest) VersionConvertor_30_40.convertResource(wrongServiceRequest, false));
	}
	
	@Test
	public void deleteProcedureRequest_shouldDeleteProcedureRequest() {
		when(serviceRequestService.delete(SERVICE_REQUEST_UUID)).thenReturn(serviceRequest);
		
		OperationOutcome result = resourceProvider.deleteProcedureRequest(new IdType().setValue(SERVICE_REQUEST_UUID));
		
		assertThat(result, notNullValue());
		assertThat(result.getIssue(), notNullValue());
		assertThat(result.getIssueFirstRep().getSeverity(), equalTo(OperationOutcome.IssueSeverity.INFORMATION));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getCode(), equalTo("MSG_DELETED"));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getDisplay(),
		    equalTo("This resource has been deleted"));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void deleteProcedureRequest_shouldThrowResourceNotFoundExceptionWhenIdRefersToNonExistentProcedureRequest() {
		when(serviceRequestService.delete(WRONG_SERVICE_REQUEST_UUID)).thenReturn(null);
		
		resourceProvider.deleteProcedureRequest(new IdType().setValue(WRONG_SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void searchProcedureRequest_shouldReturnMatchingProcedureRequestWhenUUIDIsSpecified() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(SERVICE_REQUEST_UUID));
		
		when(serviceRequestService.searchForServiceRequests(any(), any(), any(), any(), any(), any(), any())).thenReturn(
		    new org.openmrs.module.fhir2.providers.r4.MockIBundleProvider<>(Collections.singletonList(serviceRequest),
		            PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForProcedureRequests(null, null, null, null, null, null, uuid,
		    null);
		
		List<IBaseResource> resources = getResources(results);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void searchProcedureRequest_shouldReturnMatchingProcedureRequestWhenLastUpdatedIsSpecified() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		when(serviceRequestService.searchForServiceRequests(any(), any(), any(), any(), any(), any(), any())).thenReturn(
		    new MockIBundleProvider<>(Collections.singletonList(serviceRequest), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForProcedureRequests(null, null, null, null, null, null, null,
		    lastUpdated);
		
		List<IBaseResource> resources = getResources(results);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
	}
}
