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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.Flag;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirFlagService;

@RunWith(MockitoJUnitRunner.class)
public class FlagFhirResourceProviderTest {
	
	private static final String FLAG_UUID = "ce8bfad7-c87e-4af0-80cd-c2015c7dff93";
	
	private static final String BAD_FLAG_UUID = "51f069dc-e204-40f4-90d6-080385bed91f";
	
	@Mock
	private FhirFlagService fhirFlagService;
	
	FlagFhirResourceProvider resourceProvider;
	
	Flag flag;
	
	@Before
	public void setup() {
		resourceProvider = new FlagFhirResourceProvider();
		resourceProvider.setFlagService(fhirFlagService);
		
		flag = new Flag();
		flag.setId(FLAG_UUID);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Flag.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Flag.class.getName()));
	}
	
	@Test
	public void getFlagByUuid_shouldReturnMatchingFlag() {
		when(fhirFlagService.get(FLAG_UUID)).thenReturn(flag);
		
		IdType id = new IdType();
		id.setValue(FLAG_UUID);
		Flag flag = resourceProvider.getFlagByUuid(id);
		assertThat(flag, notNullValue());
		assertThat(flag.getId(), notNullValue());
		assertThat(flag.getId(), equalTo(FLAG_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getFlagByUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(BAD_FLAG_UUID);
		Flag flag = resourceProvider.getFlagByUuid(id);
		assertThat(flag, nullValue());
	}
	
	@Test
	public void shouldCreateNewFlag() {
		when(fhirFlagService.create(any(org.hl7.fhir.r4.model.Flag.class))).thenReturn(flag);
		
		MethodOutcome result = resourceProvider.createFlag(flag);
		
		assertThat(result, notNullValue());
		assertThat(result.getCreated(), is(true));
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(flag.getId()));
		assertThat(result.getResource().getStructureFhirVersionEnum(), equalTo(FhirVersionEnum.R4));
	}
	
	@Test
	public void shouldUpdateExistingFlag() {
		flag.setStatus(Flag.FlagStatus.ACTIVE);
		
		when(fhirFlagService.update(eq(FLAG_UUID), any(org.hl7.fhir.r4.model.Flag.class))).thenReturn(flag);
		
		MethodOutcome result = resourceProvider.updateFlag(new IdType().setValue(FLAG_UUID), flag);
		
		assertThat(result, notNullValue());
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(flag.getId()));
		assertThat(result.getResource().getStructureFhirVersionEnum(), equalTo(FhirVersionEnum.R4));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateFlagShouldThrowInvalidRequestForUuidMismatch() {
		when(fhirFlagService.update(eq(BAD_FLAG_UUID), any(org.hl7.fhir.r4.model.Flag.class)))
		        .thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateFlag(new IdType().setValue(BAD_FLAG_UUID), flag);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void ShouldThrowInvalidRequestForMissingIdInFlagToUpdate() {
		org.hl7.fhir.r4.model.Flag noIdFlag = new org.hl7.fhir.r4.model.Flag();
		
		when(fhirFlagService.update(eq(FLAG_UUID), any(org.hl7.fhir.r4.model.Flag.class)))
		        .thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateFlag(new IdType().setValue(FLAG_UUID), noIdFlag);
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void shouldThrowMethodNotAllowedIfFlagToUpdateDoesNotExist() {
		org.hl7.fhir.r4.model.Flag wrongFlag = new org.hl7.fhir.r4.model.Flag();
		wrongFlag.setId(BAD_FLAG_UUID);
		
		when(fhirFlagService.update(eq(BAD_FLAG_UUID), any(org.hl7.fhir.r4.model.Flag.class)))
		        .thenThrow(MethodNotAllowedException.class);
		
		resourceProvider.updateFlag(new IdType().setValue(BAD_FLAG_UUID), wrongFlag);
	}
	
	@Test
	public void shouldDeleteRequestedFlag() {
		when(fhirFlagService.delete(FLAG_UUID)).thenReturn(flag);
		
		OperationOutcome result = resourceProvider.deleteFlag(new IdType().setValue(FLAG_UUID));
		
		assertThat(result, notNullValue());
		assertThat(result.getIssue(), notNullValue());
		assertThat(result.getIssueFirstRep().getSeverity(), equalTo(OperationOutcome.IssueSeverity.INFORMATION));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getCode(), equalTo("MSG_DELETED"));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void shouldThrowResourceNotFoundExceptionWhenIdRefersToNonExistentFlag() {
		when(fhirFlagService.delete(BAD_FLAG_UUID)).thenReturn(null);
		
		resourceProvider.deleteFlag(new IdType().setValue(BAD_FLAG_UUID));
	}
}
