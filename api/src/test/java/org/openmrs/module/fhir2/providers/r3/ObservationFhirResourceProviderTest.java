/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import org.hamcrest.Matchers;
import org.hl7.fhir.convertors.conv30_40.Observation30_40;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Provenance;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirObservationService;

@RunWith(MockitoJUnitRunner.class)
public class ObservationFhirResourceProviderTest extends BaseFhirR3ProvenanceResourceTest<org.hl7.fhir.r4.model.Observation> {
	
	private static final String OBSERVATION_UUID = "1223h34-34nj3-34nj34-34nj";
	
	private static final String WRONG_OBSERVATION_UUID = "hj243h34-cb4vsd-34xxx34-ope4jj";
	
	@Mock
	private FhirObservationService observationService;
	
	@Getter(AccessLevel.PUBLIC)
	private ObservationFhirResourceProvider resourceProvider;
	
	private org.hl7.fhir.r4.model.Observation observation;
	
	@Before
	public void setup() {
		resourceProvider = new ObservationFhirResourceProvider();
		resourceProvider.setObservationService(observationService);
	}
	
	@Before
	public void initObservation() {
		observation = new org.hl7.fhir.r4.model.Observation();
		observation.setId(OBSERVATION_UUID);
		observation.setStatus(org.hl7.fhir.r4.model.Observation.ObservationStatus.UNKNOWN);
		setProvenanceResources(observation);
	}
	
	private List<IBaseResource> get(IBundleProvider results, int from, int to) {
		return results.getResources(from, to);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Observation.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Observation.class.getName()));
	}
	
	@Test
	public void getObservationByUuid_shouldReturnMatchingObservation() {
		when(observationService.get(OBSERVATION_UUID)).thenReturn(observation);
		IdType id = new IdType();
		id.setValue(OBSERVATION_UUID);
		
		Observation result = resourceProvider.getObservationById(id);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(OBSERVATION_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getObservationWithWrongUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_OBSERVATION_UUID);
		
		resourceProvider.getObservationById(id);
	}
	
	@Test
	public void searchObservations_shouldReturnMatchingObservations() {
		when(observationService.searchForObservations(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
		    any(), any(), any(), any(), any()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(observation), 10, 1));
		
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setValue("1000");
		code.addAnd(codingToken);
		
		IBundleProvider results = resourceProvider.searchObservations(null, null, null, null, null, null, null, null, null,
		    code, null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results, 1, 5);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0), notNullValue());
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.OBSERVATION));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(OBSERVATION_UUID));
	}
	
	@Test
	public void searchObservations_shouldReturnMatchingObservationsWhenPatientParamIsSpecified() {
		when(observationService.searchForObservations(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
		    any(), any(), any(), any(), any()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(observation), 10, 1));
		
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		patientParam.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Patient.SP_NAME)));
		
		IBundleProvider results = resourceProvider.searchObservations(null, patientParam, null, null, null, null, null, null,
		    null, null, null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results, 1, 5);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0), notNullValue());
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.OBSERVATION));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(OBSERVATION_UUID));
	}
	
	@Test
	public void searchObservations_shouldAddRelatedResourcesWhenIncluded() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("Observation:patient"));
		
		when(observationService.searchForObservations(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
		    any(), any(), any(), any(), any())).thenReturn(
		        new MockIBundleProvider<>(Arrays.asList(observation, new org.hl7.fhir.r4.model.Patient()), 10, 1));
		
		IBundleProvider results = resourceProvider.searchObservations(null, null, null, null, null, null, null, null, null,
		    null, null, null, null, null, includes, null);
		
		List<IBaseResource> resultList = get(results, 1, 5);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(equalTo(2)));
		assertThat(resultList.get(0), notNullValue());
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.OBSERVATION));
		assertThat(resultList.get(1).fhirType(), equalTo(FhirConstants.PATIENT));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(OBSERVATION_UUID));
	}
	
	@Test
	public void searchObservations_shouldNotAddRelatedResourcesForEmptyInclude() {
		HashSet<Include> includes = new HashSet<>();
		
		when(observationService.searchForObservations(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
		    any(), any(), any(), isNull(), any()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(observation), 10, 1));
		
		IBundleProvider results = resourceProvider.searchObservations(null, null, null, null, null, null, null, null, null,
		    null, null, null, null, null, includes, null);
		
		List<IBaseResource> resultList = get(results, 1, 5);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0), notNullValue());
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.OBSERVATION));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(OBSERVATION_UUID));
	}
	
	@Test
	public void searchObservations_shouldAddRelatedResourcesWhenReverseIncluded() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("DiagnosticReport:result"));
		
		when(observationService.searchForObservations(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
		    any(), any(), any(), any(), any()))
		            .thenReturn(new MockIBundleProvider<>(Arrays.asList(observation, new DiagnosticReport()), 10, 1));
		
		IBundleProvider results = resourceProvider.searchObservations(null, null, null, null, null, null, null, null, null,
		    null, null, null, null, null, includes, null);
		
		List<IBaseResource> resultList = get(results, 1, 5);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(equalTo(2)));
		assertThat(resultList.get(0), notNullValue());
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.OBSERVATION));
		assertThat(resultList.get(1).fhirType(), equalTo(FhirConstants.DIAGNOSTIC_REPORT));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(OBSERVATION_UUID));
	}
	
	@Test
	public void searchObservations_shouldNotAddRelatedResourcesForEmptyReverseInclude() {
		HashSet<Include> includes = new HashSet<>();
		
		when(observationService.searchForObservations(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
		    any(), any(), any(), isNull(), any()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(observation), 10, 1));
		
		IBundleProvider results = resourceProvider.searchObservations(null, null, null, null, null, null, null, null, null,
		    null, null, null, null, null, includes, null);
		
		List<IBaseResource> resultList = get(results, 1, 5);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0), notNullValue());
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.OBSERVATION));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(OBSERVATION_UUID));
	}
	
	@Test
	public void getPatientResourceHistory_shouldReturnListOfResource() {
		IdType id = new IdType();
		id.setValue(OBSERVATION_UUID);
		when(observationService.get(OBSERVATION_UUID)).thenReturn(observation);
		
		List<Resource> resources = resourceProvider.getObservationHistoryById(id);
		
		assertThat(resources, Matchers.notNullValue());
		assertThat(resources, not(empty()));
		assertThat(resources.size(), Matchers.equalTo(2));
	}
	
	@Test
	public void getPatientResourceHistory_shouldReturnProvenanceResources() {
		IdType id = new IdType();
		id.setValue(OBSERVATION_UUID);
		when(observationService.get(OBSERVATION_UUID)).thenReturn(observation);
		
		List<Resource> resources = resourceProvider.getObservationHistoryById(id);
		assertThat(resources, not(empty()));
		assertThat(resources.stream().findAny().isPresent(), is(true));
		assertThat(resources.stream().findAny().get().getResourceType().name(),
		    Matchers.equalTo(Provenance.class.getSimpleName()));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getPatientHistoryByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_OBSERVATION_UUID);
		assertThat(resourceProvider.getObservationHistoryById(idType).isEmpty(), is(true));
		assertThat(resourceProvider.getObservationHistoryById(idType).size(), Matchers.equalTo(0));
	}
	
	@Test
	public void createObservation_shouldCreateNewObservation() {
		when(observationService.create(any(org.hl7.fhir.r4.model.Observation.class))).thenReturn(observation);
		
		MethodOutcome result = resourceProvider.createObservationResource(Observation30_40.convertObservation(observation));
		
		assertThat(result, notNullValue());
		assertThat(result.getCreated(), is(true));
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(OBSERVATION_UUID));
	}
	
	@Test
	public void deleteObservation_shouldDeleteObservation() {
		when(observationService.delete(OBSERVATION_UUID)).thenReturn(observation);
		
		OperationOutcome result = resourceProvider.deleteObservationResource(new IdType().setValue(OBSERVATION_UUID));
		
		assertThat(result, notNullValue());
		assertThat(result.getIssue(), notNullValue());
		assertThat(result.getIssueFirstRep().getSeverity(), equalTo(OperationOutcome.IssueSeverity.INFORMATION));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getCode(), equalTo("MSG_DELETED"));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void deleteObservation_shouldThrowResourceNotFoundExceptionWhenIdRefersToNonExistentObservation() {
		when(observationService.delete(WRONG_OBSERVATION_UUID)).thenReturn(null);
		
		resourceProvider.deleteObservationResource(new IdType().setValue(WRONG_OBSERVATION_UUID));
	}
}
