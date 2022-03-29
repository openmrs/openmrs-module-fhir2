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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hamcrest.Matchers;
import org.hl7.fhir.convertors.conv30_40.Encounter30_40;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Provenance;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Observation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirEncounterService;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

@RunWith(MockitoJUnitRunner.class)
public class EncounterFhirResourceProviderTest extends BaseFhirR3ProvenanceResourceTest<org.hl7.fhir.r4.model.Encounter> {
	
	private static final String ENCOUNTER_UUID = "123xx34-623hh34-22hj89-23hjy5";
	
	private static final String WRONG_ENCOUNTER_UUID = "c3467w-hi4jer83-56hj34-23hjy5";
	
	private static final int PREFERRED_SIZE = 10;
	
	private static final int COUNT = 1;
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirEncounterService encounterService;
	
	private EncounterFhirResourceProvider resourceProvider;
	
	private org.hl7.fhir.r4.model.Encounter encounter;
	
	@Before
	public void setup() {
		resourceProvider = new EncounterFhirResourceProvider();
		resourceProvider.setEncounterService(encounterService);
	}
	
	@Before
	public void initEncounter() {
		encounter = new org.hl7.fhir.r4.model.Encounter();
		encounter.setId(ENCOUNTER_UUID);
		encounter.setStatus(org.hl7.fhir.r4.model.Encounter.EncounterStatus.UNKNOWN);
		setProvenanceResources(encounter);
	}
	
	private List<Encounter> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX).stream().filter(it -> it instanceof Encounter)
		        .map(it -> (Encounter) it).collect(Collectors.toList());
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Encounter.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Encounter.class.getName()));
	}
	
	@Test
	public void getEncounterByUuid_shouldReturnMatchingEncounter() {
		IdType id = new IdType();
		id.setValue(ENCOUNTER_UUID);
		when(encounterService.get(ENCOUNTER_UUID)).thenReturn(encounter);
		Encounter result = resourceProvider.getEncounterById(id);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(ENCOUNTER_UUID));
		assertThat(result.getStatus(), equalTo(Encounter.EncounterStatus.UNKNOWN));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getEncounterWithWrongUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_ENCOUNTER_UUID);
		
		resourceProvider.getEncounterById(id);
	}
	
	@Test
	public void searchEncounters_shouldReturnMatchingEncounters() {
		List<org.hl7.fhir.r4.model.Encounter> encounters = new ArrayList<>();
		encounters.add(encounter);
		when(encounterService.searchForEncounters(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(encounters, PREFERRED_SIZE, COUNT));
		
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		subjectReference.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Patient.SP_NAME)));
		
		IBundleProvider results = resourceProvider.searchEncounter(null, null, null, subjectReference, null, null, null,
		    null, null, null, null);
		
		List<Encounter> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.ENCOUNTER));
		assertThat(resultList.get(0).getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchEncounters_shouldReturnMatchingEncountersWhenPatientParamIsSpecified() {
		List<org.hl7.fhir.r4.model.Encounter> encounters = new ArrayList<>();
		encounters.add(encounter);
		when(encounterService.searchForEncounters(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(encounters, PREFERRED_SIZE, COUNT));
		
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		patientParam.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Patient.SP_NAME)));
		
		IBundleProvider results = resourceProvider.searchEncounter(null, null, null, null, patientParam, null, null, null,
		    null, null, null);
		
		List<Encounter> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.ENCOUNTER));
		assertThat(resultList.get(0).getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchEncounters_shouldAddRelatedResourcesForInclude() {
		when(encounterService.searchForEncounters(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(Arrays.asList(encounter, new Patient()), PREFERRED_SIZE, COUNT));
		
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("Encounter:patient"));
		
		IBundleProvider results = resourceProvider.searchEncounter(null, null, null, null, null, null, null, null, null,
		    includes, null);
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(2));
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.ENCOUNTER));
		assertThat(resultList.get(1).fhirType(), equalTo(FhirConstants.PATIENT));
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchEncounters_shouldNotAddResourcesForEmptyInclude() {
		when(encounterService.searchForEncounters(any(), any(), any(), any(), any(), any(), any(), any(), isNull(), any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(encounter), PREFERRED_SIZE, COUNT));
		
		HashSet<Include> includes = new HashSet<>();
		
		IBundleProvider results = resourceProvider.searchEncounter(null, null, null, null, null, null, null, null, null,
		    includes, null);
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(1));
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.ENCOUNTER));
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchEncounters_shouldAddRelatedResourcesForRevInclude() {
		when(encounterService.searchForEncounters(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(Arrays.asList(encounter, new Observation()), PREFERRED_SIZE, COUNT));
		
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("Observation:encounter"));
		
		IBundleProvider results = resourceProvider.searchEncounter(null, null, null, null, null, null, null, null, null,
		    null, revIncludes);
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(2));
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.ENCOUNTER));
		assertThat(resultList.get(1).fhirType(), equalTo(FhirConstants.OBSERVATION));
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchEncounters_shouldNotAddResourcesForEmptyRevInclude() {
		when(encounterService.searchForEncounters(any(), any(), any(), any(), any(), any(), any(), any(), any(), isNull()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(encounter), PREFERRED_SIZE, COUNT));
		
		HashSet<Include> revIncludes = new HashSet<>();
		
		IBundleProvider results = resourceProvider.searchEncounter(null, null, null, null, null, null, null, null, null,
		    null, revIncludes);
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(1));
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.ENCOUNTER));
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void getEncounterHistory_shouldReturnProvenanceResources() {
		IdType id = new IdType();
		id.setValue(ENCOUNTER_UUID);
		when(encounterService.get(ENCOUNTER_UUID)).thenReturn(encounter);
		
		List<Resource> resources = resourceProvider.getEncounterHistoryById(id);
		assertThat(resources, not(empty()));
		assertThat(resources.stream().findAny().isPresent(), is(true));
		assertThat(resources.stream().findAny().get().getResourceType().name(),
		    Matchers.equalTo(Provenance.class.getSimpleName()));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getEncounterHistoryByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_ENCOUNTER_UUID);
		assertThat(resourceProvider.getEncounterHistoryById(idType).isEmpty(), is(true));
		assertThat(resourceProvider.getEncounterHistoryById(idType).size(), Matchers.equalTo(0));
	}
	
	@Test
	public void createEncounter_shouldCreateNewEncounter() {
		when(encounterService.create(any(org.hl7.fhir.r4.model.Encounter.class))).thenReturn(encounter);
		
		MethodOutcome result = resourceProvider.createEncounter(Encounter30_40.convertEncounter(encounter));
		assertThat(result, notNullValue());
		assertThat(result.getCreated(), is(true));
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void updateEncounter_shouldUpdateEncounter() {
		when(encounterService.update(eq(ENCOUNTER_UUID), any(org.hl7.fhir.r4.model.Encounter.class))).thenReturn(encounter);
		
		MethodOutcome result = resourceProvider.updateEncounter(new IdType().setValue(ENCOUNTER_UUID),
		    Encounter30_40.convertEncounter(encounter));
		assertThat(result, notNullValue());
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateEncounter_shouldThrowInvalidRequestForUuidMismatch() {
		when(encounterService.update(eq(WRONG_ENCOUNTER_UUID), any(org.hl7.fhir.r4.model.Encounter.class)))
		        .thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateEncounter(new IdType().setValue(WRONG_ENCOUNTER_UUID),
		    Encounter30_40.convertEncounter(encounter));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateEncounter_shouldThrowInvalidRequestForMissingId() {
		org.hl7.fhir.r4.model.Encounter noIdEncounter = new org.hl7.fhir.r4.model.Encounter();
		
		when(encounterService.update(eq(ENCOUNTER_UUID), any(org.hl7.fhir.r4.model.Encounter.class)))
		        .thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateEncounter(new IdType().setValue(ENCOUNTER_UUID),
		    Encounter30_40.convertEncounter(noIdEncounter));
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void updateEncounter_shouldThrowMethodNotAllowedIfDoesNotExist() {
		org.hl7.fhir.r4.model.Encounter wrongEncounter = new org.hl7.fhir.r4.model.Encounter();
		wrongEncounter.setId(WRONG_ENCOUNTER_UUID);
		
		when(encounterService.update(eq(WRONG_ENCOUNTER_UUID), any(org.hl7.fhir.r4.model.Encounter.class)))
		        .thenThrow(MethodNotAllowedException.class);
		
		resourceProvider.updateEncounter(new IdType().setValue(WRONG_ENCOUNTER_UUID),
		    Encounter30_40.convertEncounter(wrongEncounter));
	}
	
	@Test
	public void deleteEncounter_shouldDeleteRequestedEncounter() {
		when(encounterService.delete(ENCOUNTER_UUID)).thenReturn(encounter);
		
		OperationOutcome result = resourceProvider.deleteEncounter(new IdType().setValue(ENCOUNTER_UUID));
		assertThat(result, notNullValue());
		assertThat(result.getIssue(), notNullValue());
		assertThat(result.getIssueFirstRep().getSeverity(), equalTo(OperationOutcome.IssueSeverity.INFORMATION));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getCode(), equalTo("MSG_DELETED"));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void deleteEncounter_shouldThrowResourceNotFoundExceptionWhenIdRefersToNonExistentEncounter() {
		when(encounterService.delete(WRONG_ENCOUNTER_UUID)).thenReturn(null);
		resourceProvider.deleteEncounter(new IdType().setValue(WRONG_ENCOUNTER_UUID));
	}
	
	@Test
	public void getEncounterEverything_shouldReturnEncounterEverything() {
		when(encounterService.getEncounterEverything(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(encounter), 10, 1));
		
		IBundleProvider results = resourceProvider.getEncounterEverything(new IdType(ENCOUNTER_UUID));
		
		List<IBaseResource> resultList = getAllResources(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList.size(), equalTo(1));
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.ENCOUNTER));
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void getEncounterEverything_shouldReturnNullForEncounterEverythingWhenIdParamIsMissing() {
		IBundleProvider results = resourceProvider.getEncounterEverything(null);
		assertThat(results, nullValue());
	}
	
	@Test
	public void getEncounterEverything_shouldReturnNullForEncounterEverythingWhenIdPartIsMissingInIdParam() {
		IBundleProvider results = resourceProvider.getEncounterEverything(new IdType());
		assertThat(results, nullValue());
	}
	
	@Test
	public void getEncounterEverything_shouldReturnNullEncounterEverythingWhenIdPartIsEmptyInIdParam() {
		IBundleProvider results = resourceProvider.getEncounterEverything(new IdType(""));
		assertThat(results, nullValue());
	}
	
	private List<IBaseResource> getAllResources(IBundleProvider result) {
		return result.getAllResources();
	}
}
