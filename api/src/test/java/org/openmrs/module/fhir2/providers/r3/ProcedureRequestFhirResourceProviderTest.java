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
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirServiceRequestService;

@RunWith(MockitoJUnitRunner.class)
public class ProcedureRequestFhirResourceProviderTest {
	
	private static final String SERVICE_REQUEST_UUID = "7d13b03b-58c2-43f5-b34d-08750c51aea9";
	
	private static final String WRONG_SERVICE_REQUEST_UUID = "92b04062-e57d-43aa-8c38-90a1ad70080c";
	
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
}
