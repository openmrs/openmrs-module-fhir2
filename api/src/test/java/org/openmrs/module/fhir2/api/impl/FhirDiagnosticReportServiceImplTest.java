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
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosticReportDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.DiagnosticReportTranslator;

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
	private SearchQueryInclude<DiagnosticReport> searchQueryInclude;
	
	@Mock
	private SearchQuery<Obs, DiagnosticReport, FhirDiagnosticReportDao, DiagnosticReportTranslator, SearchQueryInclude<DiagnosticReport>> searchQuery;
	
	private FhirDiagnosticReportServiceImpl service;
	
	@Before
	public void setUp() {
		service = new FhirDiagnosticReportServiceImpl();
		service.setTranslator(translator);
		service.setDao(dao);
		service.setSearchQuery(searchQuery);
		service.setSearchQueryInclude(searchQueryInclude);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void getDiagnosticReportByUuid_shouldRetrieveDiagnosticReportByUuid() {
		Obs obsGroup = new Obs();
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		
		obsGroup.setUuid(UUID);
		diagnosticReport.setId(UUID);
		
		when(dao.get(UUID)).thenReturn(obsGroup);
		when(translator.toFhirResource(obsGroup)).thenReturn(diagnosticReport);
		
		DiagnosticReport result = service.get(UUID);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(diagnosticReport));
		
	}
	
	@Test
	public void createDiagnosticReport_shouldCreateNewDiagnosticReport() {
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		diagnosticReport.setId(UUID);
		
		Obs obsGroup = new Obs();
		Obs childObs = new Obs();
		childObs.setUuid(CHILD_UUID);
		obsGroup.setUuid(UUID);
		
		obsGroup.addGroupMember(childObs);
		
		when(translator.toOpenmrsType(diagnosticReport)).thenReturn(obsGroup);
		when(dao.createOrUpdate(obsGroup)).thenReturn(obsGroup);
		when(translator.toFhirResource(obsGroup)).thenReturn(diagnosticReport);
		
		DiagnosticReport result = service.create(diagnosticReport);
		
		assertThat(result, notNullValue());
		assertThat(result, equalTo(diagnosticReport));
	}
	
	@Test
	public void updateDiagnosticReport_shouldUpdateExistingDiagnosticReport() {
		Date currentDate = new Date();
		
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		diagnosticReport.setId(UUID);
		
		Obs obsGroup = new Obs();
		Obs updatedObsGroup = new Obs();
		Obs childObs = new Obs();
		obsGroup.setUuid(UUID);
		updatedObsGroup.setUuid(UUID);
		updatedObsGroup.setDateCreated(currentDate);
		childObs.setUuid(CHILD_UUID);
		
		obsGroup.addGroupMember(childObs);
		updatedObsGroup.addGroupMember(childObs);
		
		when(translator.toOpenmrsType(obsGroup, diagnosticReport)).thenReturn(updatedObsGroup);
		when(dao.createOrUpdate(updatedObsGroup)).thenReturn(updatedObsGroup);
		when(dao.get(UUID)).thenReturn(obsGroup);
		when(translator.toFhirResource(updatedObsGroup)).thenReturn(diagnosticReport);
		
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
	
	@Test(expected = MethodNotAllowedException.class)
	public void updateTask_shouldThrowMethodNotAllowedIfTaskDoesNotExist() {
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		diagnosticReport.setId(WRONG_UUID);
		
		when(dao.get(WRONG_UUID)).thenReturn(null);
		
		service.update(WRONG_UUID, diagnosticReport);
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCollectionOfDiagnosticReportsByParameters() {
		Obs obs = new Obs();
		obs.setUuid(UUID);
		
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		diagnosticReport.setId(UUID);
		
		List<Obs> obsList = new ArrayList<>();
		obsList.add(obs);
		
		SearchParameterMap theParams = new SearchParameterMap();
		
		when(dao.search(any(), any(), anyInt(), anyInt())).thenReturn(obsList);
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(dao.getResultUuids(any())).thenReturn(Collections.singletonList(UUID));
		when(translator.toFhirResource(obs)).thenReturn(diagnosticReport);
		when(searchQuery.getQueryResults(any(), any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, translator, searchQueryInclude));
		
		IBundleProvider results = service.searchForDiagnosticReports(null, null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(UUID))));
	}
}
