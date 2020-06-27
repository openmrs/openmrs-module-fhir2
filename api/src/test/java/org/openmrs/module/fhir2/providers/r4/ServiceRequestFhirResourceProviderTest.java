/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
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
public class ServiceRequestFhirResourceProviderTest {
	
	private static final String SERVICE_REQUEST_UUID = "7d13b03b-58c2-43f5-b34d-08750c51aea9";
	
	private static final String WRONG_SERVICE_REQUEST_UUID = "92b04062-e57d-43aa-8c38-90a1ad70080c";
	
	private static final int PREFERRED_PAGE_SIZE = 10;
	
	private static final int COUNT = 1;
	
	private static final int START_INDEX = 1;
	
	private static final int END_INDEX = 5;
	
	@Mock
	private FhirServiceRequestService serviceRequestService;
	
	private ServiceRequestFhirResourceProvider resourceProvider;
	
	private ServiceRequest serviceRequest;
	
	@Before
	public void setup() {
		resourceProvider = new ServiceRequestFhirResourceProvider();
		resourceProvider.setServiceRequestService(serviceRequestService);
	}
	
	@Before
	public void initServiceRequest() {
		serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(ServiceRequest.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(ServiceRequest.class.getName()));
	}
	
	@Test
	public void getServiceRequestById_shouldReturnServiceRequest() {
		IdType id = new IdType();
		id.setValue(SERVICE_REQUEST_UUID);
		when(serviceRequestService.get(SERVICE_REQUEST_UUID)).thenReturn(serviceRequest);
		
		ServiceRequest result = resourceProvider.getServiceRequestById(id);
		assertThat(result.isResource(), is(true));
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getServiceRequestByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_SERVICE_REQUEST_UUID);
		assertThat(resourceProvider.getServiceRequestById(idType).isResource(), is(true));
		assertThat(resourceProvider.getServiceRequestById(idType), nullValue());
	}
	
	@Test
	public void searchServiceRequests_shouldReturnMatchingServiceRequests() {
		serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);
		
		when(serviceRequestService.searchForServiceRequests(any(), any(), any(), any(), any())).thenReturn(
		    new MockIBundleProvider<>(Collections.singletonList(serviceRequest), PREFERRED_PAGE_SIZE, COUNT));
		
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setValue("1000");
		code.addAnd(codingToken);
		
		IBundleProvider results = resourceProvider.searchForProcedureRequests(null, null, code, null, null, null);
		List<IBaseResource> resources = getResources(results, START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
		
	}
	
	@Test
	public void searchServiceRequests_shouldReturnMatchingServiceRequestsWhenPatientParamIsSpecified() {
		serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);
		
		when(serviceRequestService.searchForServiceRequests(any(), any(), any(), any(), any())).thenReturn(
		    new MockIBundleProvider<>(Collections.singletonList(serviceRequest), PREFERRED_PAGE_SIZE, COUNT));
		
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		patientParam.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Patient.SP_NAME)));
		
		IBundleProvider results = resourceProvider.searchForProcedureRequests(patientParam, null, null, null, null, null);
		List<IBaseResource> resources = getResources(results, START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
		
	}
	
	@Test
	public void searchServiceRequests_shouldReturnMatchingServiceRequestsWhenRequesterParamIsSpecified() {
		serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);
		
		when(serviceRequestService.searchForServiceRequests(any(), any(), any(), any(), any())).thenReturn(
		    new MockIBundleProvider<>(Collections.singletonList(serviceRequest), PREFERRED_PAGE_SIZE, COUNT));
		
		ReferenceAndListParam practitionerParam = new ReferenceAndListParam();
		practitionerParam.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Practitioner.SP_NAME)));
		
		IBundleProvider results = resourceProvider.searchForProcedureRequests(null, null, null, null, practitionerParam,
		    null);
		List<IBaseResource> resources = getResources(results, START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
		
	}
	
	@Test
	public void searchServiceRequests_shouldReturnMatchingServiceRequestsWhenDateRangeParamIsSpecified() {
		serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);
		
		when(serviceRequestService.searchForServiceRequests(any(), any(), any(), any(), any())).thenReturn(
		    new MockIBundleProvider<>(Collections.singletonList(serviceRequest), PREFERRED_PAGE_SIZE, COUNT));
		
		DateRangeParam occurence = new DateRangeParam().setLowerBound("lower date").setUpperBound("upper date");
		
		IBundleProvider results = resourceProvider.searchForProcedureRequests(null, null, null, null, null, occurence);
		List<IBaseResource> resources = getResources(results, START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
		
	}
	
	@Test
	public void searchServiceRequests_shouldReturnMatchingServiceRequestsWhenEncounterParamIsSpecified() {
		serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);
		
		when(serviceRequestService.searchForServiceRequests(any(), any(), any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(serviceRequest), 10, 1));
		
		ReferenceAndListParam encounterParam = new ReferenceAndListParam();
		encounterParam.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Encounter.SP_IDENTIFIER)));
		
		IBundleProvider results = resourceProvider.searchForProcedureRequests(null, null, null, encounterParam, null, null);
		List<IBaseResource> resources = getResources(results, START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
		
	}
	
	private List<IBaseResource> getResources(IBundleProvider results, int theFromIndex, int theToIndex) {
		return results.getResources(theFromIndex, theToIndex);
	}
}
