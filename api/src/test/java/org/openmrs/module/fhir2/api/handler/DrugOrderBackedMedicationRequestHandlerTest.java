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
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
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
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;

/**
 * Tests the read/search wiring and dispatch predicates of the default MedicationRequest handler.
 * Orchestrator-level concerns (fan-out, the typed {@code searchForMedicationRequests} entry point)
 * live in {@code FhirMedicationRequestServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DrugOrderBackedMedicationRequestHandlerTest {
	
	private static final String MEDICATION_REQUEST_UUID = "d102c80f-1yz9-4da3-0099-8902ce886891";
	
	private static final String BAD_MEDICATION_REQUEST_UUID = "d102c80f-1yz9-4da3-0099-8902ce886899";
	
	private static final String STATUS = "ACTIVE";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private MedicationRequestTranslator translator;
	
	@Mock
	private FhirMedicationRequestDao dao;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<MedicationRequest> searchQueryInclude;
	
	@Mock
	private SearchQuery<DrugOrder, MedicationRequest, FhirMedicationRequestDao, MedicationRequestTranslator, SearchQueryInclude<MedicationRequest>> searchQuery;
	
	private DrugOrderBackedMedicationRequestHandler handler;
	
	private MedicationRequest medicationRequest;
	
	private DrugOrder drugOrder;
	
	@Before
	public void setup() {
		handler = new DrugOrderBackedMedicationRequestHandler() {
			
			@Override
			protected void validateObject(DrugOrder object) {
			}
		};
		handler.setDao(dao);
		handler.setTranslator(translator);
		handler.setSearchQuery(searchQuery);
		handler.setSearchQueryInclude(searchQueryInclude);
		
		medicationRequest = new MedicationRequest();
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		
		drugOrder = new DrugOrder();
		drugOrder.setUuid(MEDICATION_REQUEST_UUID);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	private IBundleProvider stubbedBundle(SearchParameterMap theParams) {
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(drugOrder));
		when(translator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		return handler.search(theParams);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeMedicationRequestImplicitProfile() {
		assertThat(handler.getImplicitProfile(),
		    equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-medicationrequest"));
	}
	
	@Test
	public void canHandle_shouldAlwaysReturnTrue() {
		assertTrue(handler.canHandle(new MedicationRequest()));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptAnySearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	// ---- get ----
	
	@Test
	public void get_shouldGetMedicationRequestByUuid() {
		when(dao.get(MEDICATION_REQUEST_UUID)).thenReturn(drugOrder);
		when(translator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		
		MedicationRequest result = handler.get(MEDICATION_REQUEST_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void get_shouldThrowResourceNotFoundForBadUuid() {
		assertThrows(ResourceNotFoundException.class, () -> handler.get(BAD_MEDICATION_REQUEST_UUID));
	}
	
	// ---- search ----
	
	@Test
	public void search_shouldReturnMedicationRequestByParticipant() {
		ReferenceAndListParam participant = new ReferenceAndListParam();
		participant.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("1").setChain(Practitioner.SP_IDENTIFIER)));
		
		List<IBaseResource> resultList = get(stubbedBundle(
		    new SearchParameterMap().addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participant)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldReturnMedicationRequestBySubject() {
		ReferenceAndListParam subject = new ReferenceAndListParam();
		subject.addValue(new ReferenceOrListParam().add(new ReferenceParam().setValue("john").setChain(Patient.SP_FAMILY)));
		
		List<IBaseResource> resultList = get(
		    stubbedBundle(new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, subject)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldReturnMedicationRequestByMedicationReference() {
		ReferenceAndListParam medication = new ReferenceAndListParam();
		medication.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue("jdjshd-ksksk").setChain(Medication.SP_IDENTIFIER)));
		
		List<IBaseResource> resultList = get(stubbedBundle(
		    new SearchParameterMap().addParameter(FhirConstants.MEDICATION_REFERENCE_SEARCH_HANDLER, medication)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldReturnMedicationRequestByMedicationCode() {
		TokenAndListParam code = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().addOr(new TokenParam().setValue("25363")));
		
		List<IBaseResource> resultList = get(
		    stubbedBundle(new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldReturnMedicationRequestByEncounter() {
		ReferenceAndListParam encounter = new ReferenceAndListParam();
		encounter.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue("jdjdj-kdkdkkd-kddd").setChain(Encounter.SP_IDENTIFIER)));
		
		List<IBaseResource> resultList = get(stubbedBundle(
		    new SearchParameterMap().addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounter)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldReturnMedicationRequestByUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_REQUEST_UUID));
		
		List<IBaseResource> resultList = get(stubbedBundle(
		    new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldReturnMedicationRequestsByStatus() {
		TokenAndListParam status = new TokenAndListParam().addAnd(new TokenParam(STATUS));
		
		List<IBaseResource> resultList = get(stubbedBundle(new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, MedicationRequest.SP_STATUS, status)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void search_shouldReturnMedicationRequestByLastUpdated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		List<IBaseResource> resultList = get(stubbedBundle(new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void search_shouldAddRelatedResourcesWhenIncluded() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("MedicationRequest:requester"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(drugOrder));
		when(translator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.singleton(new Practitioner()));
		
		List<IBaseResource> resultList = get(handler.search(theParams));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(2));
		assertThat(resultList, hasItem(is(instanceOf(Practitioner.class))));
	}
	
	@Test
	public void search_shouldAddRelatedResourcesWhenRevIncluded() {
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("MedicationDispense:prescription"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER,
		    revIncludes);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(drugOrder));
		when(translator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any()))
		        .thenReturn(Collections.singleton(new MedicationDispense()));
		
		List<IBaseResource> resultList = get(handler.search(theParams));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(2));
		assertThat(resultList, hasItem(is(instanceOf(MedicationDispense.class))));
	}
	
	@Test
	public void search_shouldNotAddRelatedResourcesForEmptyInclude() {
		List<IBaseResource> resultList = get(stubbedBundle(
		    new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, new HashSet<Include>())));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
}
