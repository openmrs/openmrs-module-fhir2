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
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
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
	
	private static final String JSON_CREATE_LIST_PATH = "org/openmrs/module/fhir2/providers/ListResourceWebTest_create.json";
	
	private static final String JSON_UPDATE_LIST_PATH = "org/openmrs/module/fhir2/providers/ListResourceWebTest_update.json";
	
	private static final String JSON_UPDATE_LIST_NO_ID_PATH = "org/openmrs/module/fhir2/providers/ListResourceWebTest_updateWithoutId.json";
	
	private static final String JSON_UPDATE_LIST_WRONG_ID_PATH = "org/openmrs/module/fhir2/providers/ListResourceWebTest_updateWithWrongId.json";
	
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
	
	@Test
	public void createList_shouldCreateNewList() throws Exception {
		org.hl7.fhir.r4.model.ListResource listResource = new org.hl7.fhir.r4.model.ListResource();
		listResource.setId(LIST_UUID);
		String listJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_LIST_PATH)) {
			Objects.requireNonNull(is);
			listJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		when(cohortFhirListService.create(any(org.hl7.fhir.r4.model.ListResource.class))).thenReturn(listResource);
		
		MockHttpServletResponse response = post("/List").jsonContent(listJson).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isCreated());
		assertThat(response.getStatus(), is(201));
	}
	
	@Test
	public void updateList_shouldUpdateRequestedList() throws Exception {
		org.hl7.fhir.r4.model.ListResource listResource = new org.hl7.fhir.r4.model.ListResource();
		listResource.setId(LIST_UUID);
		String listJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_LIST_PATH)) {
			Objects.requireNonNull(is);
			listJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		when(cohortFhirListService.update(any(String.class), any(org.hl7.fhir.r4.model.ListResource.class)))
		        .thenReturn(listResource);
		
		MockHttpServletResponse response = put("/List/" + LIST_UUID).jsonContent(listJson).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
	}
	
	@Test
	public void updateList_shouldErrorForNoId() throws Exception {
		String listJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_LIST_NO_ID_PATH)) {
			Objects.requireNonNull(is);
			listJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/List/" + LIST_UUID).jsonContent(listJson).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	public void updateList_shouldErrorForIdMissMatch() throws Exception {
		String listJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_LIST_WRONG_ID_PATH)) {
			Objects.requireNonNull(is);
			listJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/List/" + UNKNOWN_UUID).jsonContent(listJson).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    containsStringIgnoringCase("body must contain an ID element which matches the request URL"));
	}
	
	@Test
	public void deleteList_shouldDeleteList() throws Exception {
		org.hl7.fhir.r4.model.ListResource listResource = new org.hl7.fhir.r4.model.ListResource();
		listResource.setId(LIST_UUID);
		when(cohortFhirListService.delete(LIST_UUID)).thenReturn(listResource);
		
		MockHttpServletResponse response = delete("/List/" + LIST_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void deleteList_shouldReturn404ForNonExistingList() throws Exception {
		when(cohortFhirListService.delete(UNKNOWN_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = delete("/List/" + UNKNOWN_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
}
