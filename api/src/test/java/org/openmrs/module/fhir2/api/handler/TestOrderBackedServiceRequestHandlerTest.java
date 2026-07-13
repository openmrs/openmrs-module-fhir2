/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.handler;

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
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

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
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
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

/**
 * Tests the read/search wiring and dispatch predicates of the default ServiceRequest handler.
 * Orchestrator-level concerns (fan-out, the typed {@code searchForServiceRequests} entry point)
 * live in {@code FhirServiceRequestServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestOrderBackedServiceRequestHandlerTest {
	
	private static final String SERVICE_REQUEST_UUID = "249b9094-b812-4b0c-a204-0052a05c657f";
	
	private static final String BAD_UUID = "dd0649b4-efa1-4288-a317-e4c141d89859";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final String PATIENT_GIVEN_NAME = "Meantex";
	
	private static final String CODE = "5089";
	
	private static final String ENCOUNTER_UUID = "y403fafb-e5e4-42d0-9d11-4f52e89d123r";
	
	private static final String PARTICIPANT_IDENTIFIER = "101-6";
	
	private static final String OCCURRENCE = "2020-09-03";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
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
	
	private TestOrderBackedServiceRequestHandler handler;
	
	private ServiceRequest fhirServiceRequest;
	
	private TestOrder order;
	
	@Before
	public void setUp() {
		handler = new TestOrderBackedServiceRequestHandler() {
			
			@Override
			protected void validateObject(TestOrder object) {
			}
		};
		
		handler.setDao(dao);
		handler.setTranslator(translator);
		handler.setSearchQuery(searchQuery);
		handler.setSearchQueryInclude(searchQueryInclude);
		
		order = new TestOrder();
		order.setUuid(SERVICE_REQUEST_UUID);
		
		fhirServiceRequest = new ServiceRequest();
		fhirServiceRequest.setId(SERVICE_REQUEST_UUID);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	private IBundleProvider stubbedBundle(SearchParameterMap theParams) {
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(order));
		when(translator.toFhirResource(order)).thenReturn(fhirServiceRequest);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		return handler.search(theParams);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeServiceRequestImplicitProfile() {
		assertThat(handler.getImplicitProfile(),
		    equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-servicerequest"));
	}
	
	@Test
	public void canHandle_shouldAlwaysReturnTrue() {
		assertTrue(handler.canHandle(new ServiceRequest()));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptAnySearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	// ---- get ----
	
	@Test
	public void get_shouldRetrieveServiceRequestByUUID() {
		when(dao.get(SERVICE_REQUEST_UUID)).thenReturn(order);
		when(translator.toFhirResource(order)).thenReturn(fhirServiceRequest);
		
		ServiceRequest result = handler.get(SERVICE_REQUEST_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void get_shouldThrowResourceNotFoundForBadUuid() {
		assertThrows(ResourceNotFoundException.class, () -> handler.get(BAD_UUID));
	}
	
	// ---- search ----
	
	@Test
	public void search_shouldReturnServiceRequestByPatient() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(SP_GIVEN)));
		
		List<IBaseResource> resultList = get(stubbedBundle(
		    new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void search_shouldReturnServiceRequestByCode() {
		TokenAndListParam code = new TokenAndListParam().addAnd(new TokenParam(CODE));
		
		List<IBaseResource> resultList = get(
		    stubbedBundle(new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void search_shouldReturnServiceRequestByEncounter() {
		ReferenceAndListParam encounterReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(ENCOUNTER_UUID)));
		
		List<IBaseResource> resultList = get(stubbedBundle(
		    new SearchParameterMap().addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounterReference)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void search_shouldReturnServiceRequestByRequester() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PARTICIPANT_IDENTIFIER).setChain(SP_IDENTIFIER)));
		
		List<IBaseResource> resultList = get(stubbedBundle(new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void search_shouldReturnServiceRequestByOccurrence() {
		DateRangeParam occurrence = new DateRangeParam().setLowerBound(OCCURRENCE).setUpperBound(OCCURRENCE);
		
		List<IBaseResource> resultList = get(
		    stubbedBundle(new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, occurrence)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void search_shouldReturnServiceRequestByUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(SERVICE_REQUEST_UUID));
		
		List<IBaseResource> resultList = get(stubbedBundle(
		    new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldReturnServiceRequestByLastUpdated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		List<IBaseResource> resultList = get(stubbedBundle(new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldAddRelatedResourcesWhenIncluded() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("ServiceRequest:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(order));
		when(translator.toFhirResource(order)).thenReturn(fhirServiceRequest);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.singleton(new Patient()));
		
		List<IBaseResource> resultList = get(handler.search(theParams));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2));
		assertThat(resultList, hasItem(is(instanceOf(Patient.class))));
	}
	
	@Test
	public void search_shouldNotAddRelatedResourcesForEmptyInclude() {
		List<IBaseResource> resultList = get(stubbedBundle(
		    new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, new HashSet<Include>())));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(1));
	}
}
