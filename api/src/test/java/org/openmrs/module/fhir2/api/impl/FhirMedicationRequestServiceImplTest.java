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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hamcrest.Matchers;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirMedicationRequestDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.MedicationRequestSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;
import org.openmrs.module.fhir2.api.translators.impl.MedicationRequestTranslatorImpl;

@RunWith(MockitoJUnitRunner.class)
public class FhirMedicationRequestServiceImplTest {
	
	private static final Integer MEDICATION_REQUEST_ID = 123;
	
	private static final String MEDICATION_REQUEST_UUID = "d102c80f-1yz9-4da3-0099-8902ce886891";
	
	private static final String BAD_MEDICATION_REQUEST_UUID = "d102c80f-1yz9-4da3-0099-8902ce886891";
	
	private static final String STATUS = "ACTIVE";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private MedicationRequestTranslator medicationRequestTranslator;
	
	@Mock
	private FhirMedicationRequestDao dao;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<MedicationRequest> searchQueryInclude;
	
	@Mock
	private SearchQuery<DrugOrder, MedicationRequest, FhirMedicationRequestDao, MedicationRequestTranslator, SearchQueryInclude<MedicationRequest>> searchQuery;
	
	private FhirMedicationRequestServiceImpl medicationRequestService;
	
	private MedicationRequest medicationRequest;
	
	private DrugOrder drugOrder;
	
	@Before
	public void setup() {
		medicationRequestService = new FhirMedicationRequestServiceImpl() {
			
			@Override
			protected void validateObject(DrugOrder object) {
			}
		};
		medicationRequestTranslator = mock(MedicationRequestTranslatorImpl.class);
		medicationRequestService.setDao(dao);
		medicationRequestService.setTranslator(medicationRequestTranslator);
		medicationRequestService.setSearchQuery(searchQuery);
		medicationRequestService.setSearchQueryInclude(searchQueryInclude);
		
		medicationRequest = new MedicationRequest();
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		
		drugOrder = new DrugOrder();
		drugOrder.setUuid(MEDICATION_REQUEST_UUID);
	}
	
	@Test
	public void shouldGetMedicationRequestByUuid() {
		when(dao.get(MEDICATION_REQUEST_UUID)).thenReturn(drugOrder);
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		
		MedicationRequest result = medicationRequestService.get(MEDICATION_REQUEST_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void shouldThrowResourceNotFoundForBadMedicationRequestUuid() {
		assertThrows(ResourceNotFoundException.class, () -> medicationRequestService.get(BAD_MEDICATION_REQUEST_UUID));
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(0, 10);
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnCollectionOfMedicationRequestByParticipant() {
		ReferenceAndListParam participant = new ReferenceAndListParam();
		
		participant.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("1").setChain(Practitioner.SP_IDENTIFIER)));
		
		List<DrugOrder> drugOrders = new ArrayList<>();
		
		drugOrders.add(drugOrder);
		
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participant);
		
		when(dao.getSearchResults(any())).thenReturn(drugOrders);
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		when(medicationRequestTranslator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        dao, medicationRequestTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(
		    new MedicationRequestSearchParams(null, null, null, participant, null, null, null, null, null, null, null));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounter_shouldReturnCollectionOfMedicationRequestBySubject() {
		ReferenceAndListParam subject = new ReferenceAndListParam();
		
		subject.addValue(new ReferenceOrListParam().add(new ReferenceParam().setValue("john").setChain(Patient.SP_FAMILY)));
		
		List<DrugOrder> drugOrders = new ArrayList<>();
		
		drugOrders.add(drugOrder);
		
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    subject);
		
		when(dao.getSearchResults(any())).thenReturn(drugOrders);
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		when(medicationRequestTranslator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        dao, medicationRequestTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(
		    new MedicationRequestSearchParams(subject, null, null, null, null, null, null, null, null, null, null));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounter_shouldReturnCollectionOfMedicationRequestByMedicationReference() {
		ReferenceAndListParam medication = new ReferenceAndListParam();
		
		medication.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue("jdjshd-ksksk").setChain(Medication.SP_IDENTIFIER)));
		
		List<DrugOrder> drugOrders = new ArrayList<>();
		
		drugOrders.add(drugOrder);
		
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.MEDICATION_REFERENCE_SEARCH_HANDLER, medication);
		
		when(dao.getSearchResults(any())).thenReturn(drugOrders);
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		when(medicationRequestTranslator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        dao, medicationRequestTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(
		    new MedicationRequestSearchParams(null, null, null, null, medication, null, null, null, null, null, null));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnCollectionOfMedicationRequestByMedicationCode() {
		TokenAndListParam code = new TokenAndListParam();
		code.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue("25363")));
		List<DrugOrder> drugOrders = new ArrayList<>();
		
		drugOrders.add(drugOrder);
		
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		when(dao.getSearchResults(any())).thenReturn(drugOrders);
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		when(medicationRequestTranslator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        dao, medicationRequestTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(
		    new MedicationRequestSearchParams(null, null, code, null, null, null, null, null, null, null, null));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnCollectionOfMedicationRequestByEncounter() {
		ReferenceAndListParam encounter = new ReferenceAndListParam();
		
		encounter.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue("jdjdj-kdkdkkd-kddd").setChain(Encounter.SP_IDENTIFIER)));
		
		List<DrugOrder> drugOrders = new ArrayList<>();
		
		drugOrders.add(drugOrder);
		
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounter);
		
		when(dao.getSearchResults(any())).thenReturn(drugOrders);
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		when(medicationRequestTranslator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        dao, medicationRequestTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(
		    new MedicationRequestSearchParams(null, encounter, null, null, null, null, null, null, null, null, null));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnCollectionOfMedicationRequestByUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_REQUEST_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(drugOrder));
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		when(medicationRequestTranslator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        dao, medicationRequestTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(
		    new MedicationRequestSearchParams(null, null, null, null, null, uuid, null, null, null, null, null));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnCollectionOfMedicationRequestsByStatus() {
		
		TokenAndListParam status = new TokenAndListParam().addAnd(new TokenParam(STATUS));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    MedicationRequest.SP_STATUS);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(drugOrder));
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		when(medicationRequestTranslator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        dao, medicationRequestTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(
		    new MedicationRequestSearchParams(null, null, null, null, null, null, status, null, null, null, null));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnCollectionOfMedicationRequestByLastUpdated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(drugOrder));
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		when(medicationRequestTranslator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        dao, medicationRequestTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(
		    new MedicationRequestSearchParams(null, null, null, null, null, null, null, null, lastUpdated, null, null));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForMedicationRequests_shouldAddRelatedResourcesWhenIncluded() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_REQUEST_UUID));
		
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("MedicationRequest:requester"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(drugOrder));
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		when(medicationRequestTranslator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        dao, medicationRequestTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.singleton(new Practitioner()));
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(
		    new MedicationRequestSearchParams(null, null, null, null, null, uuid, null, null, null, includes, null));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(2));
		assertThat(resultList, hasItem(is(instanceOf(Practitioner.class))));
	}
	
	@Test
	public void searchForMedicationRequests_shouldAddRelatedResourcesWhenRevIncluded() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_REQUEST_UUID));
		
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("MedicationDispense:prescription"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(drugOrder));
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		when(medicationRequestTranslator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        dao, medicationRequestTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any()))
		        .thenReturn(Collections.singleton(new MedicationDispense()));
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(
		    new MedicationRequestSearchParams(null, null, null, null, null, uuid, null, null, null, null, revIncludes));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(2));
		assertThat(resultList, hasItem(is(instanceOf(MedicationDispense.class))));
	}
	
	@Test
	public void searchForMedicationRequests_shouldNotAddRelatedResourcesForEmptyInclude() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_REQUEST_UUID));
		
		HashSet<Include> includes = new HashSet<>();
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(drugOrder));
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		when(medicationRequestTranslator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        dao, medicationRequestTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(
		    new MedicationRequestSearchParams(null, null, null, null, null, uuid, null, null, null, includes, null));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
}
