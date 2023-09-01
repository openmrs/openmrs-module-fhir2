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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.convertors.conv30_40.resources30_40.Group30_40;
import org.hl7.fhir.dstu3.model.Group;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirGroupService;

@RunWith(MockitoJUnitRunner.class)
public class GroupFhirResourceProviderTest {
	
	private static final String COHORT_UUID = "ce8bfad7-c87e-4af0-80cd-c2015c7dff93";
	
	private static final String BAD_COHORT_UUID = "51f069dc-e204-40f4-90d6-080385bed91f";
	
	@Mock
	private FhirGroupService fhirGroupService;
	
	private GroupFhirResourceProvider resourceProvider;
	
	private org.hl7.fhir.r4.model.Group group;
	
	@Before
	public void setup() {
		resourceProvider = new GroupFhirResourceProvider();
		resourceProvider.setGroupService(fhirGroupService);
		
		group = new org.hl7.fhir.r4.model.Group();
		group.setId(COHORT_UUID);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Group.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Group.class.getName()));
	}
	
	@Test
	public void getGroupByUuid_shouldReturnMatchingGroup() {
		when(fhirGroupService.get(COHORT_UUID)).thenReturn(group);
		
		IdType id = new IdType();
		id.setValue(COHORT_UUID);
		Group group = resourceProvider.getGroupByUuid(id);
		assertThat(group, notNullValue());
		assertThat(group.getId(), notNullValue());
		assertThat(group.getId(), equalTo(COHORT_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getGroupByUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(BAD_COHORT_UUID);
		Group group = resourceProvider.getGroupByUuid(id);
		assertThat(group, nullValue());
	}
	
	@Test
	public void shouldCreateNewGroup() {
		when(fhirGroupService.create(any(org.hl7.fhir.r4.model.Group.class))).thenReturn(group);
		
		MethodOutcome result = resourceProvider.createGroup(Group30_40.convertGroup(group));
		
		assertThat(result, notNullValue());
		assertThat(result.getCreated(), is(true));
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(group.getId()));
		assertThat(result.getResource().getStructureFhirVersionEnum(), equalTo(FhirVersionEnum.DSTU3));
	}
	
	@Test
	public void shouldUpdateExistingGroup() {
		org.hl7.fhir.r4.model.Group.GroupMemberComponent groupMemberComponent = mock(
		    org.hl7.fhir.r4.model.Group.GroupMemberComponent.class);
		
		group.setActual(false);
		group.addMember(groupMemberComponent);
		
		when(fhirGroupService.update(eq(COHORT_UUID), any(org.hl7.fhir.r4.model.Group.class))).thenReturn(group);
		
		MethodOutcome result = resourceProvider.updateGroup(new IdType().setValue(COHORT_UUID),
		    Group30_40.convertGroup(group));
		
		assertThat(result, notNullValue());
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(group.getId()));
		assertThat(result.getResource().getStructureFhirVersionEnum(), equalTo(FhirVersionEnum.DSTU3));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateGroupShouldThrowInvalidRequestForUuidMismatch() {
		when(fhirGroupService.update(eq(BAD_COHORT_UUID), any(org.hl7.fhir.r4.model.Group.class)))
		        .thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateGroup(new IdType().setValue(BAD_COHORT_UUID), Group30_40.convertGroup(group));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void ShouldThrowInvalidRequestForMissingIdInGroupToUpdate() {
		org.hl7.fhir.r4.model.Group noIdGroup = new org.hl7.fhir.r4.model.Group();
		
		when(fhirGroupService.update(eq(COHORT_UUID), any(org.hl7.fhir.r4.model.Group.class)))
		        .thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateGroup(new IdType().setValue(COHORT_UUID), Group30_40.convertGroup(noIdGroup));
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void shouldThrowMethodNotAllowedIfGroupToUpdateDoesNotExist() {
		org.hl7.fhir.r4.model.Group wrongGroup = new org.hl7.fhir.r4.model.Group();
		wrongGroup.setId(BAD_COHORT_UUID);
		
		when(fhirGroupService.update(eq(BAD_COHORT_UUID), any(org.hl7.fhir.r4.model.Group.class)))
		        .thenThrow(MethodNotAllowedException.class);
		
		resourceProvider.updateGroup(new IdType().setValue(BAD_COHORT_UUID), Group30_40.convertGroup(wrongGroup));
	}
	
	@Test
	public void shouldDeleteRequestedGroup() {
		OperationOutcome result = resourceProvider.deleteGroup(new IdType().setValue(COHORT_UUID));
		
		assertThat(result, notNullValue());
		assertThat(result.getIssue(), notNullValue());
		assertThat(result.getIssueFirstRep().getSeverity(), equalTo(OperationOutcome.IssueSeverity.INFORMATION));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getCode(), equalTo("MSG_DELETED"));
	}
}
