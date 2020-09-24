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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosticReportDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.DiagnosticReportTranslator;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;

@RunWith(MockitoJUnitRunner.class)
public class FhirDiagnosticReportServiceImplTest {
	
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
	private SearchQuery<FhirDiagnosticReport, DiagnosticReport, FhirDiagnosticReportDao, DiagnosticReportTranslator> searchQuery;
	
	private FhirDiagnosticReportServiceImpl service;
	
	@Before
	public void setUp() {
		service = new FhirDiagnosticReportServiceImpl() {
			
			@Override
			protected void validateObject(FhirDiagnosticReport object) {
			}
		};
		
		service.setTranslator(translator);
		service.setDao(dao);
		service.setSearchQuery(searchQuery);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void getDiagnosticReportByUuid_shouldRetrieveDiagnosticReportByUuid() {
		FhirDiagnosticReport fhirDiagnosticReport = new FhirDiagnosticReport();
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		
		fhirDiagnosticReport.setUuid(UUID);
		diagnosticReport.setId(UUID);
		
		when(dao.get(UUID)).thenReturn(fhirDiagnosticReport);
		when(translator.toFhirResource(fhirDiagnosticReport)).thenReturn(diagnosticReport);
		
		DiagnosticReport result = service.get(UUID);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(diagnosticReport));
		
	}
	
	@Test
	public void createDiagnosticReport_shouldCreateNewDiagnosticReport() {
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		diagnosticReport.setId(UUID);
		
		FhirDiagnosticReport fhirDiagnosticReport = new FhirDiagnosticReport();
		Obs obsResult = new Obs();
		obsResult.setUuid(CHILD_UUID);
		fhirDiagnosticReport.getResults().add(obsResult);
		fhirDiagnosticReport.setUuid(UUID);
		
		when(translator.toOpenmrsType(diagnosticReport)).thenReturn(fhirDiagnosticReport);
		when(dao.createOrUpdate(fhirDiagnosticReport)).thenReturn(fhirDiagnosticReport);
		when(translator.toFhirResource(fhirDiagnosticReport)).thenReturn(diagnosticReport);
		
		DiagnosticReport result = service.create(diagnosticReport);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(diagnosticReport));
	}
	
	@Test
	public void updateDiagnosticReport_shouldUpdateExistingDiagnosticReport() {
		Date currentDate = new Date();
		
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		diagnosticReport.setId(UUID);
		
		FhirDiagnosticReport fhirDiagnosticReport = new FhirDiagnosticReport();
		FhirDiagnosticReport updatedFhirDiagnosticReport = new FhirDiagnosticReport();
		Obs obsResult = new Obs();
		obsResult.setUuid(CHILD_UUID);
		fhirDiagnosticReport.setUuid(UUID);
		updatedFhirDiagnosticReport.setUuid(UUID);
		updatedFhirDiagnosticReport.setDateCreated(currentDate);
		
		fhirDiagnosticReport.getResults().add(obsResult);
		updatedFhirDiagnosticReport.getResults().add(obsResult);
		
		when(translator.toOpenmrsType(fhirDiagnosticReport, diagnosticReport)).thenReturn(updatedFhirDiagnosticReport);
		when(dao.createOrUpdate(updatedFhirDiagnosticReport)).thenReturn(updatedFhirDiagnosticReport);
		when(dao.get(UUID)).thenReturn(fhirDiagnosticReport);
		when(translator.toFhirResource(updatedFhirDiagnosticReport)).thenReturn(diagnosticReport);
		
		DiagnosticReport result = service.update(UUID, diagnosticReport);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(diagnosticReport));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateTask_shouldThrowInvalidRequestForUuidMismatch() {
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		diagnosticReport.setId(UUID);
		
		service.update(WRONG_UUID, diagnosticReport);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateTask_shouldThrowInvalidRequestForMissingUuid() {
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		
		service.update(UUID, diagnosticReport);
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void updateTask_shouldThrowResourceNotFoundIfTaskDoesNotExist() {
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		diagnosticReport.setId(WRONG_UUID);
		
		when(dao.get(WRONG_UUID)).thenReturn(null);
		
		service.update(WRONG_UUID, diagnosticReport);
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCollectionOfDiagnosticReportsByParameters() {
		FhirDiagnosticReport fhirDiagnosticReport = new FhirDiagnosticReport();
		fhirDiagnosticReport.setUuid(UUID);
		
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		diagnosticReport.setId(UUID);
		
		List<FhirDiagnosticReport> fhirDiagnosticReports = new ArrayList<>();
		fhirDiagnosticReports.add(fhirDiagnosticReport);
		
		SearchParameterMap theParams = new SearchParameterMap();
		
		when(dao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(fhirDiagnosticReports);
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(UUID));
		when(translator.toFhirResource(fhirDiagnosticReport)).thenReturn(diagnosticReport);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService));
		
		IBundleProvider results = service.searchForDiagnosticReports(null, null, null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(UUID))));
	}
}
