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
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.dstu3.model.ListResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Cohort;
import org.openmrs.module.fhir2.api.FhirListService;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class ListFhirResourceProviderWebTest extends BaseFhirR3ResourceProviderWebTest<ListFhirResourceProvider, ListResource> {
	
	private static final String LIST_UUID = "c0b1f314-1691-11df-97a5-7038c432aab88";
	
	private static final String UNKNOWN_UUID = "c0b1f314-1691-11df-97a5-7038c432aab99";
	
	@Mock
	private FhirListService<Cohort, org.hl7.fhir.r4.model.ListResource> cohortFhirListService;
	
	@Getter(AccessLevel.PUBLIC)
	private ListFhirResourceProvider listFhirResourceProvider;
	
	@Before
	@Override
	public void setup() throws ServletException {
		listFhirResourceProvider = new ListFhirResourceProvider();
		listFhirResourceProvider.setListService(cohortFhirListService);
		super.setup();
	}
	
	@Override
	public ListFhirResourceProvider getResourceProvider() {
		return listFhirResourceProvider;
	}
	
	@Test
	public void getListById_shouldReturnListWithMatchingUuid() throws Exception {
		org.hl7.fhir.r4.model.ListResource listResource = new org.hl7.fhir.r4.model.ListResource();
		listResource.setId(LIST_UUID);
		when(cohortFhirListService.get(LIST_UUID)).thenReturn(listResource);
		
		MockHttpServletResponse response = get("/List/" + LIST_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		ListResource resource = readResponse(response);
		assertThat(resource.getIdElement().getIdPart(), equalTo(LIST_UUID));
	}
	
	@Test
	public void shouldReturn404IfListNotFound() throws Exception {
		when(cohortFhirListService.get(UNKNOWN_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/List/" + UNKNOWN_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
}
