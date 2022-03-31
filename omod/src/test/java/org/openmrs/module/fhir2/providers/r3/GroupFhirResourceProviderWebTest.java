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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.Group;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGroupService;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class GroupFhirResourceProviderWebTest extends BaseFhirR3ResourceProviderWebTest<GroupFhirResourceProvider, Group> {
	
	private static final String COHORT_UUID = "1d64befb-3b2e-48e5-85f5-353d43e23e46";
	
	private static final String BAD_COHORT_UUID = "5c9d032b-6092-4052-93d2-a04202b98462";
	
	private static final String JSON_CREATE_GROUP_DOCUMENT = "org/openmrs/module/fhir2/providers/GroupWebTest_create.json";
	
	private static final String JSON_UPDATE_GROUP_DOCUMENT = "org/openmrs/module/fhir2/providers/GroupWebTest_update.json";
	
	private static final String JSON_UPDATE_GROUP_DOCUMENT_NO_ID = "org/openmrs/module/fhir2/providers/GroupWebTest_updateWithoutId.json";
	
	@Mock
	private FhirGroupService groupService;
	
	@Getter(AccessLevel.PUBLIC)
	private GroupFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws ServletException {
		resourceProvider = new GroupFhirResourceProvider();
		resourceProvider.setGroupService(groupService);
		super.setup();
	}
	
	@Test
	public void getGroupByUuid_shouldReturnGroup() throws Exception {
		org.hl7.fhir.r4.model.Group group = new org.hl7.fhir.r4.model.Group();
		group.setId(COHORT_UUID);
		when(groupService.get(COHORT_UUID)).thenReturn(group);
		
		MockHttpServletResponse response = get("/Group/" + COHORT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(COHORT_UUID));
	}
	
	@Test
	public void shouldReturn404IfGroupNotFound() throws Exception {
		MockHttpServletResponse response = get("/Group/" + BAD_COHORT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void createGroup_shouldCreateGroup() throws Exception {
		String jsonGroup;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_GROUP_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonGroup = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		org.hl7.fhir.r4.model.Group group = new org.hl7.fhir.r4.model.Group();
		group.setId(COHORT_UUID);
		
		when(groupService.create(any(org.hl7.fhir.r4.model.Group.class))).thenReturn(group);
		
		MockHttpServletResponse response = post("/Group").accept(FhirMediaTypes.JSON).jsonContent(jsonGroup).go();
		
		assertThat(response, isCreated());
	}
	
	@Test
	public void updateGroup_shouldUpdateExistingGroup() throws Exception {
		String jsonGroup;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_GROUP_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonGroup = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		org.hl7.fhir.r4.model.Group group = new org.hl7.fhir.r4.model.Group();
		group.setId(COHORT_UUID);
		
		when(groupService.update(anyString(), any(org.hl7.fhir.r4.model.Group.class))).thenReturn(group);
		
		MockHttpServletResponse response = put("/Group/" + COHORT_UUID).accept(FhirMediaTypes.JSON).jsonContent(jsonGroup)
		        .go();
		
		assertThat(response, isOk());
	}
	
	@Test
	public void updateGroup_shouldRaiseExceptionForNoId() throws Exception {
		String jsonGroup;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_GROUP_DOCUMENT_NO_ID)) {
			Objects.requireNonNull(is);
			jsonGroup = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/Group/" + COHORT_UUID).accept(FhirMediaTypes.JSON).jsonContent(jsonGroup)
		        .go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	public void deleteGroup_shouldDeleteGroup() throws Exception {
		MockHttpServletResponse response = delete("/Group/" + COHORT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void deleteGroup_shouldReturn404ForNonExistingGroup() throws Exception {
		doThrow(new ResourceNotFoundException("")).when(groupService).delete(BAD_COHORT_UUID);
		
		MockHttpServletResponse response = delete("/Group/" + BAD_COHORT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
}
