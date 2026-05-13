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
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.EncounterTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirOpenmrsEncounterServiceImplTest {
	
	private static final String ENCOUNTER_UUID = "344kk343-45hj45-34jk34-34ui33";
	
	private static final String WRONG_ENCOUNTER_UUID = "344kk343-45hj45-34jk34-34ui34";
	
	private static final String ENCOUNTER_DATETIME = "2005-01-01T00:00:00.0";
	
	private static final String ENCOUNTER_ADDRESS_STATE = "MA";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirEncounterDao dao;
	
	@Mock
	private EncounterTranslator<org.openmrs.Encounter> translator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<Encounter> searchQueryInclude;
	
	@Mock
	private SearchQuery<org.openmrs.Encounter, Encounter, FhirEncounterDao, EncounterTranslator<org.openmrs.Encounter>, SearchQueryInclude<Encounter>> searchQuery;
	
	private FhirOpenmrsEncounterServiceImpl service;
	
	private org.openmrs.Encounter openMrsEncounter;
	
	private Encounter fhirEncounter;
	
	@Before
	public void setUp() {
		service = new FhirOpenmrsEncounterServiceImpl() {
			
			@Override
			protected void validateObject(org.openmrs.Encounter object) {
			}
		};
		service.setDao(dao);
		service.setTranslator(translator);
		service.setSearchQuery(searchQuery);
		service.setSearchQueryInclude(searchQueryInclude);
		
		openMrsEncounter = new org.openmrs.Encounter();
		openMrsEncounter.setUuid(ENCOUNTER_UUID);
		
		fhirEncounter = new Encounter();
		fhirEncounter.setId(ENCOUNTER_UUID);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	// ---- get / exists ----
	
	@Test
	public void get_shouldReturnEncounterWhenDaoFindsIt() {
		when(dao.get(ENCOUNTER_UUID)).thenReturn(openMrsEncounter);
		when(translator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		
		Encounter result = service.get(ENCOUNTER_UUID);
		
		assertThat(result.getId(), equalTo(ENCOUNTER_UUID));
	}
	
	// ---- create ----
	
	@Test
	public void create_shouldCreateEncounter() {
		when(translator.toOpenmrsType(fhirEncounter)).thenReturn(openMrsEncounter);
		when(dao.createOrUpdate(openMrsEncounter)).thenReturn(openMrsEncounter);
		when(translator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		
		Encounter encounter = service.create(fhirEncounter);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getId(), equalTo(ENCOUNTER_UUID));
	}
	
	// ---- update ----
	
	@Test
	public void update_shouldUpdateEncounter() {
		when(translator.toOpenmrsType(openMrsEncounter, fhirEncounter)).thenReturn(openMrsEncounter);
		when(dao.createOrUpdate(openMrsEncounter)).thenReturn(openMrsEncounter);
		when(dao.get(ENCOUNTER_UUID)).thenReturn(openMrsEncounter);
		when(translator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		
		Encounter encounter = service.update(ENCOUNTER_UUID, fhirEncounter);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void update_shouldThrowWhenIdMismatch() {
		service.update(WRONG_ENCOUNTER_UUID, fhirEncounter);
	}
	
	// ---- delete ----
	
	@Test
	public void delete_shouldDeleteEncounter() {
		when(dao.delete(ENCOUNTER_UUID)).thenReturn(openMrsEncounter);
		
		service.delete(ENCOUNTER_UUID);
	}
	
	// ---- search ----
	
	@Test
	public void searchForEncounters_shouldReturnEncountersByDate() {
		DateRangeParam dateRangeParam = new DateRangeParam(new DateParam(ENCOUNTER_DATETIME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    dateRangeParam);
		
		List<org.openmrs.Encounter> encounters = new ArrayList<>();
		encounters.add(openMrsEncounter);
		
		when(dao.getSearchResults(any())).thenReturn(encounters);
		lenient().when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(translator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = service.searchForEncounters(theParams);
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchForEncounters_shouldReturnEncountersByLocation() {
		ReferenceAndListParam location = new ReferenceAndListParam();
		location.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(ENCOUNTER_ADDRESS_STATE).setChain(Location.SP_ADDRESS_STATE)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER,
		    location);
		
		List<org.openmrs.Encounter> encounters = new ArrayList<>();
		encounters.add(openMrsEncounter);
		
		when(dao.getSearchResults(any())).thenReturn(encounters);
		lenient().when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(translator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = service.searchForEncounters(theParams);
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounters_shouldAddIncludedResources() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("Encounter:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(openMrsEncounter));
		lenient().when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(translator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.singleton(new Patient()));
		
		IBundleProvider results = service.searchForEncounters(theParams);
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(2));
		assertThat(resultList, hasItem(is(instanceOf(Patient.class))));
	}
	
	@Test
	public void searchForEncounters_shouldAddRevIncludedResources() {
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("Observation:encounter"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER,
		    revIncludes);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(openMrsEncounter));
		lenient().when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(translator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.singleton(new Observation()));
		
		IBundleProvider results = service.searchForEncounters(theParams);
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(2));
		assertThat(resultList, hasItem(is(instanceOf(Observation.class))));
	}
}
