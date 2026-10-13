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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosticReportDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.DiagnosticReportTranslator;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;

/**
 * Tests the CRUD/search wiring and dispatch predicates of the default DiagnosticReport handler.
 * Orchestrator-level concerns (fan-out, the typed {@code searchForDiagnosticReports} entry point)
 * live in {@code FhirDiagnosticReportServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FhirDiagnosticReportBackedDiagnosticReportHandlerTest {
	
	private static final String UUID = "249b9094-b812-4b0c-a204-0052a05c657f";
	
	private static final String CHILD_UUID = "07b76ea1-f1b1-4d2c-9958-bf1f6856cf9c";
	
	private static final String WRONG_UUID = "dd0649b4-efa1-4288-a317-e4c141d89859";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirDiagnosticReportDao dao;
	
	@Mock
	private DiagnosticReportTranslator translator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQuery<FhirDiagnosticReport, DiagnosticReport, FhirDiagnosticReportDao, DiagnosticReportTranslator, SearchQueryInclude<DiagnosticReport>> searchQuery;
	
	@Mock
	private SearchQueryInclude<DiagnosticReport> searchQueryInclude;
	
	private FhirDiagnosticReportBackedDiagnosticReportHandler handler;
	
	@Before
	public void setUp() {
		handler = new FhirDiagnosticReportBackedDiagnosticReportHandler() {
			
			@Override
			protected void validateObject(FhirDiagnosticReport object) {
			}
		};
		
		handler.setTranslator(translator);
		handler.setDao(dao);
		handler.setSearchQuery(searchQuery);
		handler.setSearchQueryInclude(searchQueryInclude);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	private IBundleProvider stubbedBundle(SearchParameterMap theParams, FhirDiagnosticReport backing,
	        DiagnosticReport fhir) {
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(backing));
		when(translator.toFhirResource(backing)).thenReturn(fhir);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		return handler.search(theParams);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeDiagnosticReportImplicitProfile() {
		assertThat(handler.getImplicitProfile(),
		    equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-diagnosticreport"));
	}
	
	@Test
	public void canHandle_shouldAlwaysReturnTrue() {
		assertTrue(handler.canHandle(new DiagnosticReport()));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptAnySearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	// ---- get / create / update ----
	
	@Test
	public void get_shouldRetrieveDiagnosticReportByUuid() {
		FhirDiagnosticReport backing = new FhirDiagnosticReport();
		backing.setUuid(UUID);
		DiagnosticReport fhir = new DiagnosticReport();
		fhir.setId(UUID);
		
		when(dao.get(UUID)).thenReturn(backing);
		when(translator.toFhirResource(backing)).thenReturn(fhir);
		
		DiagnosticReport result = handler.get(UUID);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(fhir));
	}
	
	@Test
	public void create_shouldCreateNewDiagnosticReport() {
		DiagnosticReport fhir = new DiagnosticReport();
		fhir.setId(UUID);
		
		FhirDiagnosticReport backing = new FhirDiagnosticReport();
		Obs obsResult = new Obs();
		obsResult.setUuid(CHILD_UUID);
		backing.getResults().add(obsResult);
		backing.setUuid(UUID);
		
		when(translator.toOpenmrsType(fhir)).thenReturn(backing);
		when(dao.createOrUpdate(backing)).thenReturn(backing);
		when(translator.toFhirResource(backing)).thenReturn(fhir);
		
		DiagnosticReport result = handler.create(fhir);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(fhir));
	}
	
	@Test
	public void update_shouldUpdateExistingDiagnosticReport() {
		DiagnosticReport fhir = new DiagnosticReport();
		fhir.setId(UUID);
		
		FhirDiagnosticReport backing = new FhirDiagnosticReport();
		FhirDiagnosticReport updatedBacking = new FhirDiagnosticReport();
		Obs obsResult = new Obs();
		obsResult.setUuid(CHILD_UUID);
		backing.setUuid(UUID);
		updatedBacking.setUuid(UUID);
		backing.getResults().add(obsResult);
		updatedBacking.getResults().add(obsResult);
		
		when(translator.toOpenmrsType(backing, fhir)).thenReturn(updatedBacking);
		when(dao.createOrUpdate(updatedBacking)).thenReturn(updatedBacking);
		when(dao.get(UUID)).thenReturn(backing);
		when(translator.toFhirResource(updatedBacking)).thenReturn(fhir);
		
		DiagnosticReport result = handler.update(UUID, fhir);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(fhir));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void update_shouldThrowInvalidRequestForUuidMismatch() {
		DiagnosticReport fhir = new DiagnosticReport();
		fhir.setId(UUID);
		
		handler.update(WRONG_UUID, fhir);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void update_shouldThrowInvalidRequestForMissingUuid() {
		handler.update(UUID, new DiagnosticReport());
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void update_shouldThrowResourceNotFoundIfReportDoesNotExist() {
		DiagnosticReport fhir = new DiagnosticReport();
		fhir.setId(WRONG_UUID);
		
		when(dao.get(WRONG_UUID)).thenReturn(null);
		
		handler.update(WRONG_UUID, fhir);
	}
	
	// ---- search ----
	
	@Test
	public void search_shouldReturnDiagnosticReportsByParameters() {
		FhirDiagnosticReport backing = new FhirDiagnosticReport();
		backing.setUuid(UUID);
		DiagnosticReport fhir = new DiagnosticReport();
		fhir.setId(UUID);
		
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		List<IBaseResource> resultList = get(stubbedBundle(new SearchParameterMap(), backing, fhir));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(UUID))));
	}
	
	@Test
	public void search_shouldAddRelatedResourcesWhenIncluded() {
		FhirDiagnosticReport backing = new FhirDiagnosticReport();
		backing.setUuid(UUID);
		DiagnosticReport fhir = new DiagnosticReport();
		fhir.setId(UUID);
		
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("DiagnosticReport:patient"));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.singleton(new Patient()));
		List<IBaseResource> resultList = get(stubbedBundle(theParams, backing, fhir));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2));
		assertThat(resultList, hasItem(is(instanceOf(Patient.class))));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(UUID))));
	}
	
	@Test
	public void search_shouldNotAddRelatedResourcesForEmptyInclude() {
		FhirDiagnosticReport backing = new FhirDiagnosticReport();
		backing.setUuid(UUID);
		DiagnosticReport fhir = new DiagnosticReport();
		fhir.setId(UUID);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER,
		    new HashSet<Include>());
		
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		List<IBaseResource> resultList = get(stubbedBundle(theParams, backing, fhir));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(1));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(UUID))));
	}
}
