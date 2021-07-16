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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGroupService;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class GroupFhirResourceProviderWebTest extends BaseFhirR4ResourceProviderWebTest<GroupFhirResourceProvider, Group> {
	
	private static final String COHORT_UUID = "1d64befb-3b2e-48e5-85f5-353d43e23e46";
	
	private static final String BAD_COHORT_UUID = "5c9d032b-6092-4052-93d2-a04202b98462";
	
	private static final String MANAGING_ENTITY_UUID = "b566821c-1ad9-473b-836b-9e9c67688e02";
	
	private static final String MANAGING_ENTITY_GIVEN_NAME = "John";
	
	private static final String MANAGING_ENTITY_FAMILY_NAME = "Doe";
	
	private static final String JSON_CREATE_GROUP_DOCUMENT = "org/openmrs/module/fhir2/providers/GroupWebTest_create.json";
	
	private static final String JSON_UPDATE_GROUP_DOCUMENT = "org/openmrs/module/fhir2/providers/GroupWebTest_update.json";
	
	private static final String JSON_UPDATE_GROUP_DOCUMENT_NO_ID = "org/openmrs/module/fhir2/providers/GroupWebTest_updateWithoutId.json";
	
	@Getter(AccessLevel.PUBLIC)
	private GroupFhirResourceProvider resourceProvider;
	
	@Mock
	private FhirGroupService groupService;
	
	@Captor
	private ArgumentCaptor<ReferenceAndListParam> managingEntityCaptor;
	
	@Before
	public void setup() throws ServletException {
		resourceProvider = new GroupFhirResourceProvider();
		resourceProvider.setGroupService(groupService);
		super.setup();
	}
	
	@Test
	public void shouldGetGroupByUuid() throws Exception {
		Group group = new Group();
		group.setId(COHORT_UUID);
		when(groupService.get(COHORT_UUID)).thenReturn(group);
		
		MockHttpServletResponse response = get("/Group/" + COHORT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Group resource = readResponse(response);
		assertThat(resource.getIdElement().getIdPart(), equalTo(COHORT_UUID));
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
		
		Group group = new Group();
		group.setId(COHORT_UUID);
		
		when(groupService.create(any(Group.class))).thenReturn(group);
		
		MockHttpServletResponse response = post("/Group").accept(FhirMediaTypes.JSON).jsonContent(jsonGroup).go();
		
		assertThat(response, isCreated());
	}
	
	@Test
	public void shouldGetGroupByManagingEntityUUID() throws Exception {
		verifyUri(String.format("/Group/?managing-entity:Practitioner=%s", MANAGING_ENTITY_UUID));
		
		verify(groupService).searchForGroups(managingEntityCaptor.capture());
		
		List<ReferenceOrListParam> orListParams = managingEntityCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(managingEntityCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo(null));
		assertThat(referenceParam.getValue(), equalTo(MANAGING_ENTITY_UUID));
	}
	
	@Test
	public void shouldGetGroupsByManagingEntityGivenName() throws Exception {
		verifyUri(String.format("/Group/?managing-entity:Practitioner.given=%s", MANAGING_ENTITY_GIVEN_NAME));
		
		verify(groupService).searchForGroups(managingEntityCaptor.capture());
		
		List<ReferenceOrListParam> orListParams = managingEntityCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(managingEntityCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("given"));
		assertThat(referenceParam.getValue(), equalTo(MANAGING_ENTITY_GIVEN_NAME));
	}
	
	@Test
	public void shouldGetGroupsByManagingEntityFamilyName() throws Exception {
		verifyUri(String.format("/Group/?managing-entity:Practitioner.family=%s", MANAGING_ENTITY_FAMILY_NAME));
		
		verify(groupService).searchForGroups(managingEntityCaptor.capture());
		
		List<ReferenceOrListParam> orListParams = managingEntityCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(managingEntityCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo(MANAGING_ENTITY_FAMILY_NAME));
	}
	
	@Test
	public void shouldGetGroupsByManagingEntityFamilyNameWithOr() throws Exception {
		verifyUri(String.format("/Group/?managing-entity:Practitioner.family=%s,%s", MANAGING_ENTITY_FAMILY_NAME, "Vox"));
		
		verify(groupService).searchForGroups(managingEntityCaptor.capture());
		
		List<ReferenceOrListParam> orListParams = managingEntityCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(managingEntityCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo(MANAGING_ENTITY_FAMILY_NAME));
		assertThat(orListParams.get(0).getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void shouldGetGroupsByManagingEntityFamilyNameWithAnd() throws Exception {
		verifyUri(String.format("/Group/?managing-entity:Practitioner.family=%s&managing-entity:Practitioner.family=%s",
		    MANAGING_ENTITY_FAMILY_NAME, "Vox"));
		
		verify(groupService).searchForGroups(managingEntityCaptor.capture());
		
		List<ReferenceOrListParam> orListParams = managingEntityCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(managingEntityCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo(MANAGING_ENTITY_FAMILY_NAME));
		assertThat(managingEntityCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	private void verifyUri(String uri) throws Exception {
		Group group = new Group();
		group.setId(COHORT_UUID);
		when(groupService.searchForGroups(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(group), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(COHORT_UUID));
	}
	
	@Test
	public void updateGroup_shouldUpdateExistingGroup() throws Exception {
		String jsonGroup;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_GROUP_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonGroup = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		Group group = new Group();
		group.setId(COHORT_UUID);
		
		when(groupService.update(anyString(), any(Group.class))).thenReturn(group);
		
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
		OperationOutcome retVal = new OperationOutcome();
		retVal.setId(COHORT_UUID);
		retVal.getText().setDivAsString("Deleted Successfully");
		
		Group group = new Group();
		group.setId(COHORT_UUID);
		
		when(groupService.delete(COHORT_UUID)).thenReturn(group);
		
		MockHttpServletResponse response = delete("/Group/" + COHORT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
}
