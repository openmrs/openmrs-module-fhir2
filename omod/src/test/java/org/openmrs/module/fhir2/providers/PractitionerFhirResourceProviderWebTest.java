/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.fhir2.web.servlet.BaseFhirResourceProviderTest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class PractitionerFhirResourceProviderWebTest extends BaseFhirResourceProviderTest<PractitionerFhirResourceProvider, Practitioner> {
	
	private static final String PRACTITIONER_UUID = "c51d0879-ed58-4655-a450-6527afba831f";
	
	private static final String WRONG_PRACTITIONER_UUID = "810abbe5-4eca-47de-8e00-3f334ec89036";
	
	private static final String NAME = "Ricky sanchez";
	
	private static final String BAD_NAME = "bad name";
	
	private static final String PRACTITIONER_IDENTIFIER = "eu984ot-k";
	
	private static final String BAD_PRACTITIONER_IDENTIFIER = "bad identifier";
	
	@Getter(AccessLevel.PUBLIC)
	private PractitionerFhirResourceProvider resourceProvider;
	
	@Mock
	private FhirPractitionerService practitionerService;
	
	@Override
	public void setup() throws Exception {
		resourceProvider = new PractitionerFhirResourceProvider();
		resourceProvider.setPractitionerService(practitionerService);
		super.setup();
	}
	
	@Test
	public void getPractitionerById_shouldReturnPractitioner() throws Exception {
		Practitioner practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		when(practitionerService.getPractitionerByUuid(PRACTITIONER_UUID)).thenReturn(practitioner);
		
		MockHttpServletResponse response = get("/Practitioner/" + PRACTITIONER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void getEncounterByWrongUuid_shouldReturn404() throws Exception {
		
		MockHttpServletResponse response = get("/Practitioner/" + WRONG_PRACTITIONER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void findPractitionersByName_shouldReturnBundleOfPractitioners() throws IOException, ServletException {
		Practitioner practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		when(practitionerService.findPractitionerByName(NAME)).thenReturn(Collections.singletonList(practitioner));
		
		MockHttpServletResponse response = get("/Practitioner?name=" + NAME).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findPractitionersByBadName_shouldReturnBundleWithEmptyEntries() throws IOException, ServletException {
		when(practitionerService.findPractitionerByName(BAD_NAME)).thenReturn(new ArrayList<>());
		
		MockHttpServletResponse response = get("/Practitioner?name=" + BAD_NAME).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry(), is(empty()));
	}
	
	@Test
	public void findPractitionersByIdentifier_shouldReturnBundleOfPractitioners() throws IOException, ServletException {
		Practitioner practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		when(practitionerService.findPractitionerByIdentifier(PRACTITIONER_IDENTIFIER))
		        .thenReturn(Collections.singletonList(practitioner));
		
		MockHttpServletResponse response = get("/Practitioner?identifier=" + PRACTITIONER_IDENTIFIER)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findPractitionersByBadIdentifier_shouldReturnBundleWithEmptyEntries() throws IOException, ServletException {
		when(practitionerService.findPractitionerByIdentifier(BAD_PRACTITIONER_IDENTIFIER)).thenReturn(new ArrayList<>());
		
		MockHttpServletResponse response = get("/Practitioner?identifier=" + BAD_PRACTITIONER_IDENTIFIER)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry(), is(empty()));
	}
}
