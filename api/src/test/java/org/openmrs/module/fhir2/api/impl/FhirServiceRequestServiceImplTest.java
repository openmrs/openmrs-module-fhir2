/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hl7.fhir.r4.model.Patient.SP_GIVEN;
import static org.hl7.fhir.r4.model.Practitioner.SP_IDENTIFIER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.FhirConstants.CODED_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.FhirConstants.DATE_RANGE_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hamcrest.Matchers;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.TestOrder;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirServiceRequestDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ServiceRequestTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirServiceRequestServiceImplTest {
	
	private static final String SERVICE_REQUEST_UUID = "249b9094-b812-4b0c-a204-0052a05c657f";
	
	private static final ServiceRequest.ServiceRequestStatus STATUS = ServiceRequest.ServiceRequestStatus.ACTIVE;
	
	private static final ServiceRequest.ServiceRequestPriority PRIORITY = ServiceRequest.ServiceRequestPriority.ROUTINE;
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final String PATIENT_GIVEN_NAME = "Meantex";
	
	private static final String CODE = "5089";
	
	private static final String ENCOUNTER_UUID = "y403fafb-e5e4-42d0-9d11-4f52e89d123r";
	
	private static final String PARTICIPANT_IDENTIFIER = "101-6";
	
	private static final String OCCURRENCE = "2020-09-03";
	
	@Mock
	private ServiceRequestTranslator<TestOrder> translator;
	
	@Mock
	private FhirServiceRequestDao<TestOrder> dao;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<ServiceRequest> searchQueryInclude;
	
	@Mock
	private SearchQuery<TestOrder, ServiceRequest, FhirServiceRequestDao<TestOrder>, ServiceRequestTranslator<TestOrder>, SearchQueryInclude<ServiceRequest>> searchQuery;
	
	private FhirServiceRequestServiceImpl serviceRequestService;
	
	private ServiceRequest fhirServiceRequest;
	
	private TestOrder order;
	
	@Before
	public void setUp() {
		serviceRequestService = new FhirServiceRequestServiceImpl() {
			
			@Override
			protected void validateObject(TestOrder object) {
			}
		};
		
		serviceRequestService.setDao(dao);
		serviceRequestService.setTranslator(translator);
		serviceRequestService.setSearchQuery(searchQuery);
		serviceRequestService.setSearchQueryInclude(searchQueryInclude);
		
		order = new TestOrder();
		order.setUuid(SERVICE_REQUEST_UUID);
		
		fhirServiceRequest = new ServiceRequest();
		fhirServiceRequest.setId(SERVICE_REQUEST_UUID);
		fhirServiceRequest.setStatus(STATUS);
		fhirServiceRequest.setPriority(PRIORITY);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(0, 10);
	}
	
	@Test
	public void shouldRetrieveServiceRequestByUUID() {
		when(dao.get(SERVICE_REQUEST_UUID)).thenReturn(order);
		when(translator.toFhirResource(order)).thenReturn(fhirServiceRequest);
		
		ServiceRequest result = serviceRequestService.get(SERVICE_REQUEST_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void searchForServiceRequest_shouldReturnCollectionOfServiceRequestByPatientParam() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(SP_GIVEN)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(order));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(SERVICE_REQUEST_UUID));
		when(translator.toFhirResource(order)).thenReturn(fhirServiceRequest);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = serviceRequestService.searchForServiceRequests(patientReference, null, null, null, null,
		    null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForServiceRequest_shouldReturnCollectionOfServiceRequestByCode() {
		TokenAndListParam code = new TokenAndListParam().addAnd(new TokenParam(CODE));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(CODED_SEARCH_HANDLER, code);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(order));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(SERVICE_REQUEST_UUID));
		when(translator.toFhirResource(order)).thenReturn(fhirServiceRequest);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = serviceRequestService.searchForServiceRequests(null, code, null, null, null, null, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForServiceRequest_shouldReturnCollectionOfServiceRequestByEncounter() {
		ReferenceAndListParam encounterReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(ENCOUNTER_UUID).setChain(null)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounterReference);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(order));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(SERVICE_REQUEST_UUID));
		when(translator.toFhirResource(order)).thenReturn(fhirServiceRequest);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = serviceRequestService.searchForServiceRequests(null, null, encounterReference, null, null,
		    null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForServiceRequest_shouldReturnCollectionOfServiceRequestByRequester() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PARTICIPANT_IDENTIFIER).setChain(SP_IDENTIFIER)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(order));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(SERVICE_REQUEST_UUID));
		when(translator.toFhirResource(order)).thenReturn(fhirServiceRequest);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = serviceRequestService.searchForServiceRequests(null, null, null, participantReference,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForServiceRequest_shouldReturnCollectionOfServiceRequestByOccurrence() {
		DateRangeParam occurrence = new DateRangeParam().setLowerBound(OCCURRENCE).setUpperBound(OCCURRENCE);
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(DATE_RANGE_SEARCH_HANDLER, occurrence);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(order));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(SERVICE_REQUEST_UUID));
		when(translator.toFhirResource(order)).thenReturn(fhirServiceRequest);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = serviceRequestService.searchForServiceRequests(null, null, null, null, occurrence, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForServiceRequest_shouldReturnCollectionOfServiceRequestByUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(SERVICE_REQUEST_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(order));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(SERVICE_REQUEST_UUID));
		when(translator.toFhirResource(order)).thenReturn(fhirServiceRequest);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = serviceRequestService.searchForServiceRequests(null, null, null, null, null, uuid, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForServiceRequest_shouldReturnCollectionOfServiceRequestByLastUpdated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(order));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(SERVICE_REQUEST_UUID));
		when(translator.toFhirResource(order)).thenReturn(fhirServiceRequest);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = serviceRequestService.searchForServiceRequests(null, null, null, null, null, null,
		    lastUpdated, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPeople_shouldAddRelatedResourcesWhenIncluded() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("ServiceRequest:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(order));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(SERVICE_REQUEST_UUID));
		when(translator.toFhirResource(order)).thenReturn(fhirServiceRequest);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.singleton(new Patient()));
		
		IBundleProvider results = serviceRequestService.searchForServiceRequests(null, null, null, null, null, null, null,
		    includes);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2));
		assertThat(resultList, hasItem(is(instanceOf(Patient.class))));
	}
	
	@Test
	public void searchForPeople_shouldAddRelatedResourcesWhenIncludedR3() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("ProcedureRequest:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(order));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(SERVICE_REQUEST_UUID));
		when(translator.toFhirResource(order)).thenReturn(fhirServiceRequest);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.singleton(new Patient()));
		
		IBundleProvider results = serviceRequestService.searchForServiceRequests(null, null, null, null, null, null, null,
		    includes);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2));
		assertThat(resultList, hasItem(is(instanceOf(Patient.class))));
	}
	
	@Test
	public void searchForPeople_shouldNotAddRelatedResourcesForEmptyInclude() {
		HashSet<Include> includes = new HashSet<>();
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(order));
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(SERVICE_REQUEST_UUID));
		when(translator.toFhirResource(order)).thenReturn(fhirServiceRequest);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = serviceRequestService.searchForServiceRequests(null, null, null, null, null, null, null,
		    includes);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(1));
	}
	
}
