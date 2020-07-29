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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ListResource;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Cohort;
import org.openmrs.module.fhir2.api.FhirListService;
import org.openmrs.module.fhir2.providers.BaseFhirProvenanceResourceTest;

@RunWith(MockitoJUnitRunner.class)
public class ListFhirResourceProviderTest extends BaseFhirProvenanceResourceTest<ListResource> {
	
	private static final String LIST_UUID = "c0b1f314-1691-11df-97a5-7038c432aab88";
	
	private static final String UNKNOWN_UUID = "c0b1f314-1691-11df-97a5-7038c432aab99";
	
	private static final String TITLE = "Covid19 patients";
	
	private static final String DESCRIPTION = "Covid19 patients";
	
	@Mock
	private FhirListService<Cohort, ListResource> cohortFhirListService;
	
	private ListFhirResourceProvider listFhirResourceProvider;
	
	private ListResource list;
	
	@Before
	public void setUp() {
		listFhirResourceProvider = new ListFhirResourceProvider();
		listFhirResourceProvider.setCohortFhirListService(cohortFhirListService);
	}
	
	@Before
	public void initListResource() {
		list = new ListResource();
		list.setId(LIST_UUID);
		list.setTitle(TITLE);
		list.setStatus(ListResource.ListStatus.CURRENT);
		list.setDate(new Date());
		list.setNote(Collections.singletonList(new Annotation().setText(DESCRIPTION)));
		list.setMode(ListResource.ListMode.WORKING);
		
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(listFhirResourceProvider.getResourceType(), equalTo(ListResource.class));
		assertThat(listFhirResourceProvider.getResourceType().getName(), equalTo(ListResource.class.getName()));
	}
	
	@Test
	public void getListById_shouldReturnMatchingList() {
		when(cohortFhirListService.get(LIST_UUID)).thenReturn(list);
		IdType id = new IdType();
		id.setValue(LIST_UUID);
		ListResource result = listFhirResourceProvider.getListById(id);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(LIST_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getListWithWrongId_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(UNKNOWN_UUID);
		ListResource result = listFhirResourceProvider.getListById(id);
		assertThat(result, nullValue());
	}
	
	@Test
	public void createList_shouldCreateNewList() {
		when(cohortFhirListService.create(list)).thenReturn(list);
		
		MethodOutcome result = listFhirResourceProvider.creatListResource(list);
		assertThat(result, notNullValue());
		assertThat(result.getCreated(), is(true));
		assertThat(result.getResource(), equalTo(list));
	}
	
	@Test
	public void updateList_shouldUpdateList() {
		when(cohortFhirListService.update(LIST_UUID, list)).thenReturn(list);
		
		MethodOutcome result = listFhirResourceProvider.updateListResource(new IdType().setValue(LIST_UUID), list);
		assertThat(result, notNullValue());
		assertThat(result.getResource(), equalTo(list));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateList_shouldThrowInvalidRequestForUuidMismatch() {
		when(cohortFhirListService.update(UNKNOWN_UUID, list)).thenThrow(InvalidRequestException.class);
		
		listFhirResourceProvider.updateListResource(new IdType().setValue(UNKNOWN_UUID), list);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateList_shouldThrowInvalidRequestForMissingId() {
		ListResource noIdResource = new ListResource();
		
		when(cohortFhirListService.update(LIST_UUID, noIdResource)).thenThrow(InvalidRequestException.class);
		
		listFhirResourceProvider.updateListResource(new IdType().setValue(LIST_UUID), noIdResource);
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void updateList_shouldThrowMethodNotAllowedIfDoesNotExist() {
		ListResource wrongListResource = new ListResource();
		wrongListResource.setId(UNKNOWN_UUID);
		
		when(cohortFhirListService.update(UNKNOWN_UUID, wrongListResource)).thenThrow(MethodNotAllowedException.class);
		
		listFhirResourceProvider.updateListResource(new IdType().setValue(UNKNOWN_UUID), wrongListResource);
	}
	
	@Test
	public void deleteList_shouldDeleteRequestedList() {
		when(cohortFhirListService.delete(LIST_UUID)).thenReturn(list);
		
		OperationOutcome result = listFhirResourceProvider.deleteListResource(new IdType().setValue(LIST_UUID));
		assertThat(result, notNullValue());
		assertThat(result.getIssue(), notNullValue());
		assertThat(result.getIssueFirstRep().getSeverity(), equalTo(OperationOutcome.IssueSeverity.INFORMATION));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getCode(), equalTo("MSG_DELETED"));
		
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void deleteList_shouldThrowResourceNotFoundExceptionWhenIdRefersToNonExistentList() {
		when(cohortFhirListService.delete(UNKNOWN_UUID)).thenReturn(null);
		listFhirResourceProvider.deleteListResource(new IdType().setValue(UNKNOWN_UUID));
		
	}
}
