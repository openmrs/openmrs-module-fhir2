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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hamcrest.Matchers;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Medication;
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
import org.openmrs.module.fhir2.api.dao.FhirMedicationRequestDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirMedicationRequestServiceImplTest {
	
	private static final String MEDICATION_REQUEST_UUID = "d102c80f-1yz9-4da3-0099-8902ce886891";
	
	private static final String BAD_MEDICATION_REQUEST_UUID = "d102c80f-1yz9-4da3-0099-8902ce886891";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	@Mock
	private MedicationRequestTranslator medicationRequestTranslator;
	
	@Mock
	private FhirMedicationRequestDao dao;
	
	@Mock
	private SearchQueryInclude<MedicationRequest> searchQueryInclude;
	
	@Mock
	private SearchQuery<DrugOrder, MedicationRequest, FhirMedicationRequestDao, MedicationRequestTranslator, SearchQueryInclude<MedicationRequest>> searchQuery;
	
	private FhirMedicationRequestServiceImpl medicationRequestService;
	
	private MedicationRequest medicationRequest;
	
	private DrugOrder drugOrder;
	
	@Before
	public void setup() {
		medicationRequestService = new FhirMedicationRequestServiceImpl();
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
	public void shouldReturnNullForBadMedicationRequestUuid() {
		MedicationRequest result = medicationRequestService.get(BAD_MEDICATION_REQUEST_UUID);
		assertThat(result, nullValue());
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(0, 10);
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnCollectionOfMedicationRequestByParticipant() {
		ReferenceAndListParam participant = new ReferenceAndListParam();
		
		participant.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("1").setChain(Practitioner.SP_IDENTIFIER)));
		
		Collection<DrugOrder> drugOrders = new ArrayList<>();
		
		drugOrders.add(drugOrder);
		
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participant);
		
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(drugOrders);
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(MEDICATION_REQUEST_UUID));
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, medicationRequestTranslator, searchQueryInclude));
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(null, null, null, participant, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForEncounter_shouldReturnCollectionOfMedicationRequestBySubject() {
		ReferenceAndListParam subject = new ReferenceAndListParam();
		
		subject.addValue(new ReferenceOrListParam().add(new ReferenceParam().setValue("john").setChain(Patient.SP_FAMILY)));
		
		Collection<DrugOrder> drugOrders = new ArrayList<>();
		
		drugOrders.add(drugOrder);
		
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    subject);
		
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(drugOrders);
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(MEDICATION_REQUEST_UUID));
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, medicationRequestTranslator, searchQueryInclude));
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(subject, null, null, null, null, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForEncounter_shouldReturnCollectionOfMedicationRequestByMedicationReference() {
		ReferenceAndListParam medication = new ReferenceAndListParam();
		
		medication.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue("jdjshd-ksksk").setChain(Medication.SP_IDENTIFIER)));
		
		Collection<DrugOrder> drugOrders = new ArrayList<>();
		
		drugOrders.add(drugOrder);
		
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.MEDICATION_REFERENCE_SEARCH_HANDLER, medication);
		
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(drugOrders);
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(MEDICATION_REQUEST_UUID));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, medicationRequestTranslator, searchQueryInclude));
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(null, null, null, null, medication,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnCollectionOfMedicationRequestByMedicationCode() {
		TokenAndListParam code = new TokenAndListParam();
		code.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue("25363")));
		Collection<DrugOrder> drugOrders = new ArrayList<>();
		
		drugOrders.add(drugOrder);
		
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(drugOrders);
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(MEDICATION_REQUEST_UUID));
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, medicationRequestTranslator, searchQueryInclude));
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(null, null, code, null, null, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnCollectionOfMedicationRequestByEncounter() {
		ReferenceAndListParam encounter = new ReferenceAndListParam();
		
		encounter.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue("jdjdj-kdkdkkd-kddd").setChain(Encounter.SP_IDENTIFIER)));
		
		Collection<DrugOrder> drugOrders = new ArrayList<>();
		
		drugOrders.add(drugOrder);
		
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounter);
		
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(drugOrders);
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(MEDICATION_REQUEST_UUID));
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, medicationRequestTranslator, searchQueryInclude));
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(null, encounter, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnCollectionOfMedicationRequestByUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_REQUEST_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(drugOrder));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(MEDICATION_REQUEST_UUID));
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, medicationRequestTranslator, searchQueryInclude));
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(null, null, null, null, null, uuid,
		    null);
		
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
		
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(drugOrder));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(MEDICATION_REQUEST_UUID));
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, medicationRequestTranslator, searchQueryInclude));
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(null, null, null, null, null, null,
		    lastUpdated);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
}
