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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ObservationTranslator;

/**
 * Tests the CRUD/search wiring and dispatch predicates of the default Observation handler. The
 * orchestrator-level concerns (fan-out, the typed {@code searchForObservations} /
 * {@code getLastn…Observations} entry points) live in {@code FhirObservationServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ObservationBackedObservationHandlerTest {
	
	private static final String OBS_UUID = "12345-abcde-12345";
	
	private static final String PATIENT_GIVEN_NAME = "Clement";
	
	@Mock
	private FhirObservationDao dao;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<Observation> searchQueryInclude;
	
	@Mock
	private SearchQuery<Obs, Observation, FhirObservationDao, ObservationTranslator, SearchQueryInclude<Observation>> searchQuery;
	
	@Mock
	private ObservationTranslator translator;
	
	@Mock
	private ObsService obsService;
	
	private ObservationBackedObservationHandler handler;
	
	@Before
	public void setup() {
		handler = new ObservationBackedObservationHandler() {
			
			@Override
			protected void validateObject(Obs object) {
			}
		};
		
		handler.setDao(dao);
		handler.setSearchQuery(searchQuery);
		handler.setTranslator(translator);
		handler.setSearchQueryInclude(searchQueryInclude);
		handler.setObsService(obsService);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeObservationImplicitProfile() {
		assertThat(handler.getImplicitProfile(), equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-observation"));
	}
	
	@Test
	public void shouldExposeObservationBackingKey() {
		assertThat(handler.getBackingKey(), equalTo("openmrs.observation"));
	}
	
	@Test
	public void canHandle_shouldAlwaysReturnTrue() {
		// Single backing today — handler claims every incoming Observation.
		assertTrue(handler.canHandle(new Observation()));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptAnySearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	// ---- get ----
	
	@Test
	public void get_shouldReturnObservationByUuid() {
		Obs obs = new Obs();
		obs.setUuid(OBS_UUID);
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		when(dao.get(OBS_UUID)).thenReturn(obs);
		when(translator.toFhirResource(obs)).thenReturn(observation);
		
		Observation result = handler.get(OBS_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(OBS_UUID));
	}
	
	// ---- update (applyUpdate routes saves through ObsService for obs-immutability) ----
	
	@Test
	public void update_shouldSaveThroughObsService() {
		Obs existing = new Obs();
		existing.setUuid(OBS_UUID);
		Obs updated = new Obs();
		updated.setUuid(OBS_UUID);
		
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		
		when(dao.get(OBS_UUID)).thenReturn(existing);
		when(translator.toOpenmrsType(existing, observation)).thenReturn(updated);
		when(obsService.saveObs(updated, "Updated via the FHIR2 API")).thenReturn(updated);
		when(translator.toFhirResource(updated)).thenReturn(observation);
		
		Observation result = handler.update(OBS_UUID, observation);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(OBS_UUID));
		// The override must persist via ObsService.saveObs (void-and-recreate), not dao.createOrUpdate.
		verify(obsService).saveObs(updated, "Updated via the FHIR2 API");
		verify(dao, never()).createOrUpdate(any());
	}
	
	// ---- search ----
	
	@Test
	public void search_shouldReturnObservationsByParameters() {
		Obs obs = new Obs();
		obs.setUuid(OBS_UUID);
		
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		
		ReferenceAndListParam patientReference = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		patient.setValue(PATIENT_GIVEN_NAME);
		patient.setChain(Patient.SP_GIVEN);
		patientReference.addValue(new ReferenceOrListParam().add(patient));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		when(globalPropertyService.getGlobalPropertyAsInteger(anyString(), anyInt())).thenReturn(10);
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(obs));
		when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(translator.toFhirResource(obs)).thenReturn(observation);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		
		IBundleProvider results = handler.search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		assertThat(results.preferredPageSize(), equalTo(10));
		
		List<IBaseResource> resultList = results.getResources(1, 10);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
}
