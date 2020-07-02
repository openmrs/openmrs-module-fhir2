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
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hamcrest.Matchers;
import org.hl7.fhir.dstu3.model.IdType;
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
	public void searchProcedureRequest_shouldReturnMatchingProcedureRequestWhenUUIDIsSpecified() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(SERVICE_REQUEST_UUID));
		
		when(serviceRequestService.searchForServiceRequests(any(), any())).thenReturn(
		    new org.openmrs.module.fhir2.providers.r4.MockIBundleProvider<>(Collections.singletonList(serviceRequest),
		            PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForProcedureRequests(uuid, null);
		
		List<IBaseResource> resources = getResources(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resources, hasSize(Matchers.equalTo(1)));
		assertThat(resources.get(0), Matchers.notNullValue());
		assertThat(resources.get(0).fhirType(), Matchers.equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), Matchers.equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void searchProcedureRequest_shouldReturnMatchingProcedureRequestWhenLastUpdatedIsSpecified() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		when(serviceRequestService.searchForServiceRequests(any(), any())).thenReturn(
		    new MockIBundleProvider<>(Collections.singletonList(serviceRequest), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForProcedureRequests(null, lastUpdated);
		
		List<IBaseResource> resources = getResources(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resources, hasSize(Matchers.equalTo(1)));
		assertThat(resources.get(0), Matchers.notNullValue());
		assertThat(resources.get(0).fhirType(), Matchers.equalTo(FhirConstants.SERVICE_REQUEST));
		assertThat(resources.get(0).getIdElement().getIdPart(), Matchers.equalTo(SERVICE_REQUEST_UUID));
	}
}
